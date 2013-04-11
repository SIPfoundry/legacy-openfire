package org.jivesoftware.openfire.pubsub;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.provider.PubSubProvider;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.LinkedList;
import org.jivesoftware.util.LinkedListNode;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePubSubProvider implements PubSubProvider {
	private static final Logger log = LoggerFactory
			.getLogger(BasePubSubProvider.class);

	private static final int MAX_ITEMS_FLUSH = JiveGlobals.getIntProperty(
			"xmpp.pubsub.flush.max", 1000);

	private static final int MAX_ROWS_FETCH = JiveGlobals.getIntProperty(
			"xmpp.pubsub.fetch.max", 2000);

	/**
	 * Queue that holds the items that need to be added to the database.
	 */
	private static LinkedList<PublishedItem> itemsToAdd = new LinkedList<PublishedItem>();

	/**
	 * Queue that holds the items that need to be deleted from the database.
	 */
	private static LinkedList<PublishedItem> itemsToDelete = new LinkedList<PublishedItem>();

	/**
	 * Keeps reference to published items that haven't been persisted yet so
	 * they can be removed before being deleted.
	 */
	private static final Map<String, LinkedListNode<PublishedItem>> itemsPending = new HashMap<String, LinkedListNode<PublishedItem>>();

	/**
	 * Cache name for recently accessed published items.
	 */
	private static final String ITEM_CACHE = "Published Items";

	/**
	 * Cache for recently accessed published items.
	 */
	private static final Cache<String, PublishedItem> itemCache = CacheFactory
			.createCache(ITEM_CACHE);

	private static PubSubTimers timers;

	public BasePubSubProvider() {
		if (timers == null) {
			synchronized (new byte[0]) {
				if (timers == null) {
					timers = PubSubTimers.getInstance(this);
				}
			}
		}
	}

	protected static int getMaxRowsFetch() {
		return MAX_ROWS_FETCH;
	}

	protected static LinkedListNode<PublishedItem> getFirstToAdd() {
		return itemsToAdd.getFirst();
	}

	protected static LinkedListNode<PublishedItem> getLastToAdd() {
		return itemsToAdd.getLast();
	}

	protected static LinkedListNode<PublishedItem> getFirstToDelete() {
		return itemsToDelete.getFirst();
	}

	protected static LinkedListNode<PublishedItem> getLastToDelete() {
		return itemsToDelete.getLast();
	}

	// protected static Map<String, LinkedListNode<PublishedItem>>
	// getItemsPending() {
	// return itemsPending;
	// }

	/**
	 * Creates and stores the published item in the database. Duplicate item (if
	 * found) is removed before storing the item.
	 *
	 * @param item
	 *            The published item to save.
	 */
	public void savePublishedItem(PublishedItem item) {
		String itemKey = item.getItemKey();
		itemCache.put(itemKey, item);
		log.debug("Added new (inbound) item to cache");
		synchronized (itemsPending) {
			LinkedListNode<PublishedItem> itemToReplace = itemsPending
					.remove(itemKey);
			if (itemToReplace != null) {
				itemToReplace.remove(); // remove duplicate from itemsToAdd
										// linked list
			}
			itemsToDelete.addLast(item); // delete stored duplicate (if any)
			LinkedListNode<PublishedItem> listNode = itemsToAdd.addLast(item);
			itemsPending.put(itemKey, listNode);
		}
		if (itemsPending.size() > MAX_ITEMS_FLUSH) {
			flushPendingItems();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePublishedItem(PublishedItem item) {
		String itemKey = item.getItemKey();
		itemCache.remove(itemKey);
		synchronized (itemsPending) {
			itemsToDelete.addLast(item);
			LinkedListNode<PublishedItem> itemToAdd = itemsPending
					.remove(itemKey);
			if (itemToAdd != null) {
				itemToAdd.remove(); // drop from itemsToAdd linked list
			}
		}
	}

	public PublishedItem getPublishedItem(LeafNode node, String itemID) {
		String itemKey = PublishedItem.getItemKey(node, itemID);

		// try to fetch from cache first without locking
		PublishedItem result = itemCache.get(itemKey);
		if (result == null) {
			Lock itemLock = CacheFactory.getLock(ITEM_CACHE, itemCache);
			try {
				// Acquire lock, then re-check cache before reading from DB;
				// allows clustered item cache to be primed by first request
				itemLock.lock();
				result = itemCache.get(itemKey);
				if (result == null) {
					loadItem(node, itemID);
				} else {
					log.debug("Found cached item on second attempt (after acquiring lock)");
				}
			} finally {
				itemLock.unlock();
			}
		} else {
			log.debug("Found cached item on first attempt (no lock)");
		}

		itemCache.put(itemKey, result);

		return result;
	}

	/**
	 * Fetches all the results for the specified node, limited by
	 * {@link LeafNode#getMaxPublishedItems()}.
	 *
	 * @param node
	 *            the leaf node to load its published items.
	 */
	public List<PublishedItem> getPublishedItems(LeafNode node) {
		return getPublishedItems(node, node.getMaxPublishedItems());
	}

	public abstract PublishedItem loadItem(LeafNode node, String itemID);

	public void purgeNode(LeafNode leafNode) {
		if (purgeNodeFromDB(leafNode)) {
			// Delete all the entries from the itemsToAdd list and pending map
			// that match this node.
			synchronized (itemsPending) {
				Iterator<Map.Entry<String, LinkedListNode<PublishedItem>>> pendingIt = itemsPending
						.entrySet().iterator();

				while (pendingIt.hasNext()) {
					LinkedListNode<PublishedItem> itemNode = pendingIt.next()
							.getValue();

					if (itemNode.object.getNodeID()
							.equals(leafNode.getNodeID())) {
						itemNode.remove();
						pendingIt.remove();
					}
				}
			}
		}
	}

	protected abstract boolean purgeNodeFromDB(LeafNode leafNode);

	/**
	 * Flush the cache of items to be persisted and deleted.
	 */
	public void flushPendingItems() {
		flushPendingItems(ClusterManager.isClusteringEnabled());
	}

	public abstract void flushPendingItems(boolean sendToCluster);

	protected static void movePendingToAdd() {
		// Swap pending items so we can parse and save the contents from this
		// point in time
		// while not blocking new entries from being cached.
		synchronized (itemsPending) {
			itemsToAdd = new LinkedList<PublishedItem>();
			itemsToDelete = new LinkedList<PublishedItem>();

			// Ensure pending items are available via the item cache;
			// this allows the item(s) to be fetched by other thread(s)
			// while being written to the DB from this thread
			int copied = 0;
			for (String key : itemsPending.keySet()) {
				if (!itemCache.containsKey(key)) {
					itemCache.put(key, itemsPending.get(key).object);
					copied++;
				}
			}
			if (log.isDebugEnabled() && copied > 0) {
				log.debug("Added " + copied
						+ " pending items to published item cache");
			}
			itemsPending.clear();
		}
	}

	protected void safeFlushPendingItems() {
		Lock itemLock = CacheFactory.getLock(ITEM_CACHE, itemCache);
		try {
			// NOTE: force other requests to wait for DB I/O to complete
			itemLock.lock();
			flushPendingItems();
		} finally {
			itemLock.unlock();
		}
	}

	protected static void evictFromCache(LeafNode leafNode) {
		// drop cached items for purged node
		synchronized (itemCache) {
			for (PublishedItem item : itemCache.values()) {
				if (leafNode.getNodeID().equals(item.getNodeID())) {
					itemCache.remove(item.getItemKey());
				}
			}
		}

	}

	protected abstract void purgeItems();

	public void shutdown() {
		flushPendingItems();
		purgeItems();
	}
}
