package org.jivesoftware.openfire.group;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jivesoftware.openfire.event.GroupEventDispatcher;
import org.jivesoftware.openfire.provider.GroupPropertiesProvider;
import org.jivesoftware.openfire.provider.ProviderFactory;
import org.jivesoftware.util.Immutable;
import org.jivesoftware.util.PersistableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a writable {@link Map} to manage group properties.
 * Updates made to the elements in this map will also be applied to the database.
 * Note this implementation assumes group property changes will be relatively
 * infrequent and therefore does not try to optimize database I/O for performance.
 * Each call to a {@link Map} mutator method (direct or indirect via {@link Iterator})
 * will result in a corresponding synchronous update to the database.
 *
 * @param <K> Property key
 * @param <V> Property value
 */

public class DefaultGroupPropertyMap<K,V> extends PersistableMap<K,V> {

	private static final long serialVersionUID = 3128889631577167040L;
    private static final Logger logger = LoggerFactory.getLogger(DefaultGroupPropertyMap.class);

    private final Group group;
    private final GroupPropertiesProvider provider = ProviderFactory.getGroupPropertiesProvider();
    /**
     * Group properties map constructor; requires associated {@link Group} instance
     * @param group The group that owns these properties
     */
	public DefaultGroupPropertyMap(Group group) {
		this.group = group;
	}

    /**
     * {@inheritDoc}
     */
	@Override
    public V put(K key, V value, boolean persist) {
		V originalValue = super.put(key, value);
		// we only support persistence for <String, String>
		if (persist && key instanceof String && value instanceof String) {
			if (logger.isDebugEnabled()) {
                logger.debug("Persisting group property [" + key + "]: " + value);
            }
			if (originalValue instanceof String) { // existing property
				updateProperty((String)key, (String)value, (String)originalValue);
			} else {
				insertProperty((String)key, (String)value);
			}
		}
		return originalValue;
	}

	@Override
	public V put(K key, V value) {
		if (value == null) { // treat null value as "remove"
			return remove(key);
		}
		return put(key, value, true);
	}

	@Override
	public V remove(Object key) {
		V result = super.remove(key);
		if (key instanceof String) {
			deleteProperty((String)key);
		}
		return result;
	}

	@Override
	public void clear() {
		super.clear();
		deleteAllProperties();
	}

	@Override
	public Set<K> keySet() {
		// custom class needed here to handle key.remove()
		return new PersistenceAwareKeySet<K>(super.keySet());
	}

	@Override
	public Collection<V> values() {
		// custom class needed here to suppress value.remove()
		return new Immutable.Collection<V>(super.values());
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		// custom class needed here to handle entrySet mutators
		return new PersistenceAwareEntrySet<Entry<K,V>>(super.entrySet());
	}

	/**
	 * Persistence-aware {@link Set} for group property keys. This class returns
	 * a custom iterator that can handle property removal.
	 */
	private class PersistenceAwareKeySet<E> extends AbstractSet<K> {

		private final Set<K> delegate;

		/**
		 * Sole constructor; requires wrapped {@link Set} for delegation
		 * @param delegate A collection of keys from the map
		 */
		public PersistenceAwareKeySet(Set<K> delegate) {
			this.delegate = delegate;
		}

		@Override
		public Iterator<K> iterator() {
			return new KeyIterator<E>(delegate.iterator());
		}

		@Override
		public int size() {
			return delegate.size();
		}
	}

	/**
	 * This iterator updates the database when a property key is removed.
	 */
	private class KeyIterator<E> implements Iterator<K> {

		private final Iterator<K> delegate;
		private K current;

		/**
		 * Sole constructor; requires wrapped {@link Iterator} for delegation
		 * @param delegate An iterator for all the keys from the map
		 */
		public KeyIterator(Iterator<K> delegate) {
			this.delegate = delegate;
		}

		/**
		 * Delegated to corresponding method in the backing {@link Iterator}
		 */
        public boolean hasNext() {
			return delegate.hasNext();
		}

		/**
		 * Delegated to corresponding method in the backing {@link Iterator}
		 */
        public K next() {
			current = delegate.next();
			return current;
		}

		/**
		 * Removes the property corresponding to the current key from
		 * the underlying map. Also applies update to the database.
		 */
        public void remove() {
			delegate.remove();
			if (current instanceof String) {
				deleteProperty((String)current);
			}
			current = null;
		}
	}

	/**
	 * Persistence-aware {@link Set} for group properties (as {@link Map.Entry})
	 */
	private class PersistenceAwareEntrySet<E> implements Set<Entry<K, V>> {

		private final Set<Entry<K, V>> delegate;

		/**
		 * Sole constructor; requires wrapped {@link Set} for delegation
		 * @param delegate A collection of entries ({@link Map.Entry}) from the map
		 */
		public PersistenceAwareEntrySet(Set<Entry<K, V>> delegate) {
			this.delegate = delegate;
		}

		/**
		 * Returns a custom iterator for the entries in the backing map
		 */
        public Iterator<Entry<K, V>> iterator() {
			return new EntryIterator<Entry<K,V>>(delegate.iterator());
		}

		/**
		 * Removes the given key from the backing map, and applies the
		 * corresponding update to the database.
		 *
		 * @param o A {@link Map.Entry} within this set
		 * @return True if the set contained the given key
		 */
        public boolean remove(Object o) {
			boolean propertyExists = delegate.remove(o);
			if (propertyExists) {
				deleteProperty((String)((Entry<K,V>)o).getKey());
			}
			return propertyExists;
		}

		/**
		 * Removes all the elements in the set, and applies the
		 * corresponding update to the database.
		 */
        public void clear() {
			delegate.clear();
			deleteAllProperties();
		}

		// these methods are problematic (and not really necessary),
		// so they are not implemented

		/**
		 * @throws UnsupportedOperationException
		 */
        public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws UnsupportedOperationException
		 */
        public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		// per docs for {@link Map.entrySet}, these methods are not supported

		/**
		 * @throws UnsupportedOperationException
		 */
        public boolean add(Entry<K, V> o) {
			return delegate.add(o);
		}

		/**
		 * @throws UnsupportedOperationException
		 */
        public boolean addAll(Collection<? extends Entry<K, V>> c) {
			return delegate.addAll(c);
		}

		// remaining {@link Set} methods can be delegated safely

		/**
		 * Delegated to corresponding method in the backing {@link Set}
		 */
        public int size() {
			return delegate.size();
		}

		/**
		 * Delegated to corresponding method in the backing {@link Set}
		 */
        public boolean isEmpty() {
			return delegate.isEmpty();
		}

		/**
		 * Delegated to corresponding method in the backing {@link Set}
		 */
        public boolean contains(Object o) {
			return delegate.contains(o);
		}

		/**
		 * Delegated to corresponding method in the backing {@link Set}
		 */
        public Object[] toArray() {
			return delegate.toArray();
		}

		/**
		 * Delegated to corresponding method in the backing {@link Set}
		 */
        public <T> T[] toArray(T[] a) {
			return delegate.toArray(a);
		}

		/**
		 * Delegated to corresponding method in the backing {@link Set}
		 */
        public boolean containsAll(Collection<?> c) {
			return delegate.containsAll(c);
		}

		/**
		 * Delegated to corresponding method in the backing {@link Set}
		 */
		@Override
        public boolean equals(Object o) {
			return delegate.equals(o);
		}

		/**
		 * Delegated to corresponding method in the backing {@link Set}
		 */
		@Override
        public int hashCode() {
			return delegate.hashCode();
		}
	}

	/**
	 * Remove group property from the database when the {@link Iterator.remove}
	 * method is invoked via the {@link Map.entrySet} set
	 */
	private class EntryIterator<E> implements Iterator<Entry<K, V>> {

		private final Iterator<Entry<K,V>> delegate;
		private EntryWrapper<E> current;

		/**
		 * Sole constructor; requires wrapped {@link Iterator} for delegation
		 * @param delegate An iterator for all the keys from the map
		 */
		public EntryIterator(Iterator<Entry<K,V>> delegate) {
			this.delegate = delegate;
		}
		/**
		 * Delegated to corresponding method in the backing {@link Iterator}
		 */
        public boolean hasNext() {
			return delegate.hasNext();
		}

		/**
		 * Delegated to corresponding method in the backing {@link Iterator}
		 */
        public Entry<K,V> next() {
			current = new EntryWrapper<E>(delegate.next());
			return current;
		}

		/**
		 * Removes the property corresponding to the current key from
		 * the underlying map. Also applies update to the database.
		 */
        public void remove() {
			delegate.remove();
			K key = current.getKey();
			if (key instanceof String) {
				deleteProperty((String)key);
			}
			current = null;
		}
	}

	/**
	 * Update the database when a group property is updated via {@link Map.Entry.setValue}
	 */
	private class EntryWrapper<E> implements Entry<K,V> {
		private final Entry<K,V> delegate;

		/**
		 * Sole constructor; requires wrapped {@link Map.Entry} for delegation
		 * @param delegate The corresponding entry from the map
		 */
		public EntryWrapper(Entry<K,V> delegate) {
			this.delegate = delegate;
		}

		/**
		 * Delegated to corresponding method in the backing {@link Map.Entry}
		 */
        public K getKey() {
			return delegate.getKey();
		}

		/**
		 * Delegated to corresponding method in the backing {@link Map.Entry}
		 */
        public V getValue() {
			return delegate.getValue();
		}

		/**
		 * Set the value of the property corresponding to this entry. This
		 * method also updates the database as needed depending on the new
		 * property value. A null value will cause the property to be deleted
		 * from the database.
		 *
		 * @param value The new property value
		 * @return The old value of the corresponding property
		 */
        public V setValue(V value) {
			V oldValue = delegate.setValue(value);
			K key = delegate.getKey();
			if (key instanceof String) {
				if (value instanceof String) {
					if (oldValue == null) {
						insertProperty((String) key, (String) value);
					} else if (!value.equals(oldValue)) {
						updateProperty((String)key,(String)value, (String)oldValue);
					}
				} else {
					deleteProperty((String)key);
				}
			}
			return oldValue;
		}
	}

	/**
	 * Persist a new group property to the database for the current group
	 *
	 * @param key Property name
	 * @param value Property value
	 */
	private synchronized void insertProperty(String key, String value) {
		provider.insertProperty(group.getName(), key, value);

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("propertyKey", key);
        event.put("type", "propertyAdded");
        GroupEventDispatcher.dispatchEvent(group,
                GroupEventDispatcher.EventType.group_modified, event);
    }

	/**
	 * Update the value of an existing group property for the current group
	 *
	 * @param key Property name
	 * @param value Property value
	 * @param originalValue Original property value
	 */
    private synchronized void updateProperty(String key, String value, String originalValue) {
        provider.updateProperty(group.getName(), key, value);

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("propertyKey", key);
        event.put("type", "propertyModified");
        event.put("originalValue", originalValue);
        GroupEventDispatcher.dispatchEvent(group,
                GroupEventDispatcher.EventType.group_modified, event);
    }

    /**
     * Delete a group property from the database for the current group
     *
     * @param key Property name
     */
    private synchronized void deleteProperty(String key) {
        provider.deleteProperty(group.getName(), key);

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("type", "propertyDeleted");
        event.put("propertyKey", key);
        GroupEventDispatcher.dispatchEvent(group,
            GroupEventDispatcher.EventType.group_modified, event);
    }

    /**
     * Delete all properties from the database for the current group
     */
    private synchronized void deleteAllProperties() {
        provider.deleteGroupProperties(group.getName());

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("type", "propertyDeleted");
        event.put("propertyKey", "*");
        GroupEventDispatcher.dispatchEvent(group,
            GroupEventDispatcher.EventType.group_modified, event);
    }
}
