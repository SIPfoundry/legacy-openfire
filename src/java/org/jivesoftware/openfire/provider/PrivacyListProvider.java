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

import org.jivesoftware.openfire.privacy.PrivacyList;

/**
 * Provider that facilitates access to the underlying privacy list storage.
 *
 * @author Alex Mateescu
 *
 */
public interface PrivacyListProvider {

	/**
	 * Returns the names of the existing privacy lists indicating which one is the
	 * default privacy list associated to a user.
	 *
	 * @param username the username of the user to get his privacy lists names.
	 * @return the names of the existing privacy lists with a default flag.
	 */
	public abstract Map<String, Boolean> getPrivacyLists(String username);

	/**
	 * Loads the requested privacy list from the database. Returns <tt>null</tt> if a list
	 * with the specified name does not exist.
	 *
	 * @param username the username of the user to get his privacy list.
	 * @param listName name of the list to load.
	 * @return the privacy list with the specified name or <tt>null</tt> if a list
	 *         with the specified name does not exist.
	 */
	public abstract PrivacyList loadPrivacyList(String username, String listName);

	/**
	 * Loads the default privacy list of a given user from the database. Returns <tt>null</tt>
	 * if no list was found.
	 *
	 * @param username the username of the user to get his default privacy list.
	 * @return the default privacy list or <tt>null</tt> if no list was found.
	 */
	public abstract PrivacyList loadDefaultPrivacyList(String username);

	/**
	 * Creates and saves the new privacy list to the database.
	 *
	 * @param username the username of the user that created a new privacy list.
	 * @param list the PrivacyList to save.
	 */
	public abstract void createPrivacyList(String username, PrivacyList list);

	/**
	 * Updated the existing privacy list in the database.
	 *
	 * @param username the username of the user that updated a privacy list.
	 * @param list the PrivacyList to update in the database.
	 */
	public abstract void updatePrivacyList(String username, PrivacyList list);

	/**
	 * Deletes an existing privacy list from the database.
	 *
	 * @param username the username of the user that deleted a privacy list.
	 * @param listName the name of the PrivacyList to delete.
	 */
	public abstract void deletePrivacyList(String username, String listName);

	/**
	 * Deletes all existing privacy list from the database for the given user.
	 *
	 * @param username the username of the user whose privacy lists are going to be deleted.
	 */
	public abstract void deletePrivacyLists(String username);

}