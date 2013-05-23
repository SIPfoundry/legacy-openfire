package org.jivesoftware.openfire.group;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.openfire.provider.GroupPropertiesProvider;
import org.jivesoftware.openfire.provider.GroupProvider;
import org.jivesoftware.openfire.provider.ProviderFactory;
import org.jivesoftware.util.PersistableMap;
import org.xmpp.packet.JID;

/**
 * Shared base class for Openfire GroupProvider implementations. By default
 * all mutator methods throw {@link UnsupportedOperationException}. In
 * addition, group search operations are disabled.
 *
 * Subclasses may optionally implement these capabilities, and must also
 * at minimum implement the {@link GroupProvider.getGroup(String)} method.
 *
 * @author Tom Evans
 */
public abstract class AbstractGroupProvider implements GroupProvider {

    /**
     * Provider for underlying storage
     */
	protected final GroupPropertiesProvider propsProvider = ProviderFactory.getGroupPropertiesProvider();

    // Mutator methods disabled for read-only group providers

	/**
	 * @throws UnsupportedOperationException
	 */
	public void addMember(String groupName, JID user, boolean administrator)
    {
        throw new UnsupportedOperationException("Cannot add members to read-only groups");
    }

	/**
	 * @throws UnsupportedOperationException
	 */
	public void updateMember(String groupName, JID user, boolean administrator)
    {
        throw new UnsupportedOperationException("Cannot update members for read-only groups");
    }

	/**
	 * @throws UnsupportedOperationException
	 */
	public void deleteMember(String groupName, JID user)
    {
        throw new UnsupportedOperationException("Cannot remove members from read-only groups");
    }

    /**
     * Always true for a read-only provider
     */
	public boolean isReadOnly() {
        return true;
    }

	/**
	 * @throws UnsupportedOperationException
	 */
	public Group createGroup(String name) {
        throw new UnsupportedOperationException("Cannot create groups via read-only provider");
    }

	/**
	 * @throws UnsupportedOperationException
	 */
	public void deleteGroup(String name) {
        throw new UnsupportedOperationException("Cannot remove groups via read-only provider");
    }

	/**
	 * @throws UnsupportedOperationException
	 */
	public void setName(String oldName, String newName) throws GroupAlreadyExistsException {
        throw new UnsupportedOperationException("Cannot modify read-only groups");
    }

	/**
	 * @throws UnsupportedOperationException
	 */
	public void setDescription(String name, String description) throws GroupNotFoundException {
        throw new UnsupportedOperationException("Cannot modify read-only groups");
    }

    // Search methods may be overridden by read-only group providers

    /**
     * Returns true if the provider supports group search capability. This implementation
     * always returns false.
     */
	public boolean isSearchSupported() {
        return false;
    }

    /**
     * Returns a collection of group search results. This implementation
     * returns an empty collection.
     */
	public Collection<String> search(String query) {
    	return Collections.emptyList();
    }

    /**
     * Returns a collection of group search results. This implementation
     * returns an empty collection.
     */
	public Collection<String> search(String query, int startIndex, int numResults) {
    	return Collections.emptyList();
    }

	// Shared group methods may be overridden by read-only group providers

    /**
     * Returns the name of the groups that are shared groups.
     *
     * @return the name of the groups that are shared groups.
     */
	public Collection<String> getSharedGroupNames() {
        return propsProvider.getSharedGroupsNames();
    }

	public Collection<String> getSharedGroupNames(JID user) {
    	Set<String> answer = new HashSet<String>();
    	Collection<String> userGroups = getGroupNames(user);
    	answer.addAll(userGroups);
    	for (String userGroup : userGroups) {
    		answer.addAll(getVisibleGroupNames(userGroup));
    	}
        answer.addAll(getPublicSharedGroupNames());
        return answer;
    }

	public Collection<String> getVisibleGroupNames(String userGroup) {
		return propsProvider.getVisibleGroupNames(userGroup);
	}

	public Collection<String> getPublicSharedGroupNames() {
        return propsProvider.getPublicSharedGroupNames();
	}

	public boolean isSharingSupported() {
        return true;
    }

	public Collection<String> search(String key, String value) {
		return propsProvider.search(key, value);
	}

    /**
     * Returns a custom {@link Map} that updates the database whenever
     * a property value is added, changed, or deleted.
     *
     * @param name The target group
     * @return The properties for the given group
     */
	public PersistableMap<String,String> loadProperties(Group group) {
        return propsProvider.loadProperties(group);
    }
}
