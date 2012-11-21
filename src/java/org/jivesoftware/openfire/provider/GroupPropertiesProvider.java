/**
 * $Revision: $
 * $Date: $
 *
 * Copyright (C) 2005-2008 Jive Software. All rights reserved.
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
package org.jivesoftware.openfire.provider;

import java.util.Map;
import java.util.Set;

import org.jivesoftware.openfire.group.GroupAlreadyExistsException;

/**
 * Provider that facilitates access to the underlying group properties storage.
 *
 * @author Alex Mateescu
 *
 */
public interface GroupPropertiesProvider {

	/**
	 * Load all properties for a given group
	 *
	 * @param groupName
	 *            The name of the group whose properties we want to retrieve
	 * @return Mapping between properties and values
	 */
	Map<String, String> loadProperties(String groupName);

	/**
	 * Add a property for a specific group
	 *
	 * @param groupName
	 *            The name of the group
	 * @param propName
	 *            The name of the property to insert
	 * @param propValue
	 *            The initial value of the property
	 */
	void insertProperty(String groupName, String propName, String propValue);

	/**
	 * Update a property for a group
	 *
	 * @param groupName
	 *            The name of the group whose property we want to update
	 * @param propName
	 *            The name of the property to update
	 * @param propValue
	 *            The new value of the property
	 */
	void updateProperty(String groupName, String propName, String propValue);

	/**
	 * Delete a specific property for a group
	 *
	 * @param groupName
	 *            The name of the group whose property we want to delete
	 * @param propName
	 *            The name of the property to be deleted
	 */
	void deleteProperty(String groupName, String propName);

	/**
	 * Returns the name of the groups that are shared groups.
	 *
	 * @return the name of the groups that are shared groups.
	 */
	Set<String> getSharedGroupsNames();

	/**
	 * Rename a group
	 *
	 * @param oldName
	 *            Name for the group to rename
	 * @param newName
	 *            New name of the group
	 * @return <code>true</code> if the properties were deleted,
	 *         <code>false</code> otherwise
	 * @throws UnsupportedOperationException
	 * @throws GroupAlreadyExistsException
	 */
	boolean setName(String oldName, String newName)
			throws UnsupportedOperationException, GroupAlreadyExistsException;

	/**
	 * Delete all properties for a user
	 *
	 * @param groupName
	 *            The name of the group whose properties we want to delete
	 * @return <code>true</code> if the properties were deleted,
	 *         <code>false</code> otherwise
	 */
	boolean deleteGroupProperties(String groupName);

}