/**
 * $RCSfile$
 * $Revision: 2911 $
 * $Date: 2005-10-03 12:35:52 -0300 (Mon, 03 Oct 2005) $
 *
 * Copyright (C) 2004-2008 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.dom4j.io.SAXReader;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.container.BasicModule;
import org.jivesoftware.openfire.event.UserEventDispatcher;
import org.jivesoftware.openfire.event.UserEventListener;
import org.jivesoftware.openfire.provider.OfflineMessageProvider;
import org.jivesoftware.openfire.provider.ProviderFactory;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.JiveConstants;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * Represents the user's offline message storage. A message store holds messages
 * that were sent to the user while they were unavailable. The user can retrieve
 * their messages by setting their presence to "available". The messages will
 * then be delivered normally. Offline message storage is optional, in which
 * case a null implementation is returned that always throws
 * UnauthorizedException when adding messages to the store.
 *
 * @author Iain Shigeoka
 */
public class OfflineMessageStore extends BasicModule implements
		UserEventListener {

	private static final Logger Log = LoggerFactory
			.getLogger(OfflineMessageStore.class);

	private static final int POOL_SIZE = 10;

	private Cache<String, Integer> sizeCache;

	/**
	 * Returns the instance of <tt>OfflineMessageStore</tt> being used by the
	 * XMPPServer.
	 *
	 * @return the instance of <tt>OfflineMessageStore</tt> being used by the
	 *         XMPPServer.
	 */
	public static OfflineMessageStore getInstance() {
		return XMPPServer.getInstance().getOfflineMessageStore();
	}

	/**
	 * Pool of SAX Readers. SAXReader is not thread safe so we need to have a
	 * pool of readers.
	 */
	private BlockingQueue<SAXReader> xmlReaders = new LinkedBlockingQueue<SAXReader>(
			POOL_SIZE);

	/**
	 * Provider for underlying storage
	 */
	private final OfflineMessageProvider provider = ProviderFactory
			.getOfflineProvider();

	/**
	 * Constructs a new offline message store.
	 */
	public OfflineMessageStore() {
		super("Offline Message Store");
		sizeCache = CacheFactory.createCache("Offline Message Size");
	}

	/**
	 * Adds a message to this message store. Messages will be stored and made
	 * available for later delivery.
	 *
	 * @param message
	 *            the message to store.
	 */
	public void addMessage(Message message) {
		if (message == null) {
			return;
		}
		if (message.getBody() == null || message.getBody().length() == 0) {
			// ignore empty bodied message (typically chat-state notifications).
			return;
		}
		JID recipient = message.getTo();
		String username = recipient.getNode();
		// If the username is null (such as when an anonymous user), don't
		// store.
		if (username == null
				|| !UserManager.getInstance().isRegisteredUser(recipient)) {
			return;
		} else if (!XMPPServer.getInstance().getServerInfo().getXMPPDomain()
				.equals(recipient.getDomain())) {
			// Do not store messages sent to users of remote servers
			return;
		}

		long messageID = SequenceManager.nextID(JiveConstants.OFFLINE);

		// Get the message in XML format.
		String msgXML = message.getElement().asXML();

		provider.addMessage(username, messageID, msgXML);

		// Update the cached size if it exists.
		if (sizeCache.containsKey(username)) {
			int size = sizeCache.get(username);
			size += msgXML.length();
			sizeCache.put(username, size);
		}
	}

	/**
	 * Returns a Collection of all messages in the store for a user. Messages
	 * may be deleted after being selected from the database depending on the
	 * delete param.
	 *
	 * @param username
	 *            the username of the user who's messages you'd like to receive.
	 * @param delete
	 *            true if the offline messages should be deleted.
	 * @return An iterator of packets containing all offline messages.
	 */
	public Collection<OfflineMessage> getMessages(String username,
			boolean delete) {
		List<OfflineMessage> messages = new ArrayList<OfflineMessage>();
		SAXReader xmlReader = null;

		try {
			// Get a sax reader from the pool
			xmlReader = xmlReaders.take();

			messages.addAll(provider.getMessages(username, delete, xmlReader));
			// Check if the offline messages loaded should be deleted, and that
			// there are
			// messages to delete.
			if (delete && !messages.isEmpty()) {
				provider.deleteMessages(username);
			}
		} catch (Exception e) {
			Log.error("Error retrieving offline messages of username: {}",
					username, e);
		} finally {
			if (xmlReader != null) {
				xmlReaders.add(xmlReader);
			}
		}

		return messages;
	}

	/**
	 * Returns the offline message of the specified user with the given creation
	 * date. The returned message will NOT be deleted from the database.
	 *
	 * @param username
	 *            the username of the user who's message you'd like to receive.
	 * @param creationDate
	 *            the date when the offline message was stored in the database.
	 * @return the offline message of the specified user with the given creation
	 *         stamp.
	 */
	public OfflineMessage getMessage(String username, Date creationDate) {
		OfflineMessage message = null;
		SAXReader xmlReader = null;

		try {
			// Get a sax reader from the pool
			xmlReader = xmlReaders.take();

			message = provider.getMessage(username, creationDate, xmlReader);
		} catch (Exception e) {
			Log.error("Error retrieving offline messages of username: "
					+ username + " creationDate: " + creationDate, e);
		} finally {
			// Return the sax reader to the pool
			if (xmlReader != null) {
				xmlReaders.add(xmlReader);
			}
		}

		return message;
	}

	/**
	 * Deletes all offline messages in the store for a user.
	 *
	 * @param username
	 *            the username of the user who's messages are going to be
	 *            deleted.
	 */
	public void deleteMessages(String username) {
		boolean deleted = provider.deleteMessages(username);

		if (deleted) {
			// Force a refresh for next call to getSize(username),
			// it's easier than loading the message to be deleted just
			// to update the cache.
			removeUsernameFromSizeCache(username);
		}
	}

	private void removeUsernameFromSizeCache(String username) {
		// Update the cached size if it exists.
		if (sizeCache.containsKey(username)) {
			sizeCache.remove(username);
		}
	}

	/**
	 * Deletes the specified offline message in the store for a user. The way to
	 * identify the message to delete is based on the creationDate and username.
	 *
	 * @param username
	 *            the username of the user who's message is going to be deleted.
	 * @param creationDate
	 *            the date when the offline message was stored in the database.
	 */
	public void deleteMessage(String username, Date creationDate) {
		boolean deleted = provider.deleteMessage(username, creationDate);

		if (deleted) {
			// Force a refresh for next call to getSize(username),
			// it's easier than loading the message to be deleted just
			// to update the cache.
			removeUsernameFromSizeCache(username);
		}
	}

	public int getSize() {
		return provider.getSize();
	}

	/**
	 * Returns the approximate size (in bytes) of the XML messages stored for a
	 * particular user.
	 *
	 * @param username
	 *            the username of the user.
	 * @return the approximate size of stored messages (in bytes).
	 */
	public int getSize(String username) {
		// See if the size is cached.
		if (sizeCache.containsKey(username)) {
			return sizeCache.get(username);
		}
		int size = provider.getSize(username);

		// Add the value to cache.
		sizeCache.put(username, size);

		return size;
	}

	public void userCreated(User user, Map<String, Object> params) {
		// Do nothing
	}

	public void userDeleting(User user, Map<String, Object> params) {
		// Delete all offline messages of the user
		deleteMessages(user.getUsername());
	}

	public void userModified(User user, Map<String, Object> params) {
		// Do nothing
	}

	@Override
	public void start() throws IllegalStateException {
		super.start();
		// Initialize the pool of sax readers
		for (int i = 0; i < POOL_SIZE; i++) {
			SAXReader xmlReader = new SAXReader();
			xmlReader.setEncoding("UTF-8");
			xmlReaders.add(xmlReader);
		}
		// Add this module as a user event listener so we can delete
		// all offline messages when a user is deleted
		UserEventDispatcher.addListener(this);
	}

	@Override
	public void stop() {
		super.stop();
		// Clean up the pool of sax readers
		xmlReaders.clear();
		// Remove this module as a user event listener
		UserEventDispatcher.removeListener(this);
	}
}