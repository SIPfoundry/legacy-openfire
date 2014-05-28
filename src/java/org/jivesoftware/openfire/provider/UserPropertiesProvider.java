/**
 * $RCSfile$
 * $Revision: 1321 $
 * $Date: 2005-05-05 15:31:03 -0300 (Thu, 05 May 2005) $
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
package org.jivesoftware.openfire.provider;

import java.util.Map;

/**
 * Provider that facilitates access to the underlying user properties storage.
 *
 * @author Alex Mateescu
 *
 */
public interface UserPropertiesProvider {

	/**
	 * Load all properties for a given user
	 *
	 * @param username
	 *            The name of the user whose properties we want to retrieve
	 * @return Mapping between properties and values
	 */
	Map<String, String> loadProperties(String username);

	/**
	 * Add a property for a specific user
	 *
	 * @param username
	 *            The name of the user
	 * @param propName
	 *            The name of the property to insert
	 * @param propValue
	 *            The initial value of the property
	 */
	void insertProperty(String username, String propName, String propValue);

	/**
	 * Returns the value of the specified property for the given username. This
	 * method is an optimization to avoid loading a user to get a specific
	 * property.
	 *
	 * @param username
	 *            the username of the user to get a specific property value.
	 * @param propertyName
	 *            the name of the property to retrieve.
	 * @return the value of the specified property for the given username.
	 */
	String getPropertyValue(String username, String propertyName);

	/**
	 * Update a property for a user
	 *
	 * @param username
	 *            The name of the user whose property we want to update
	 * @param propName
	 *            The name of the property to update
	 * @param propValue
	 *            The new value of the property
	 */
	void updateProperty(String username, String propName, String propValue);

	/**
	 * Delete all properties for a user
	 *
	 * @param username
	 *            The name of the user whose properties we want to delete
	 * @return <code>true</code> if the properties were deleted,
	 *         <code>false</code> otherwise
	 */
	boolean deleteUserProperties(String username);

	/**
	 * Delete a specific property for a user
	 *
	 * @param username
	 *            The name of the user whose property we want to delete
	 * @param propName
	 *            The name of the property to be deleted
	 */
	void deleteProperty(String username, String propName);
}
