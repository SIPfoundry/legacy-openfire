/**
 * $Revision$
 * $Date$
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

/**
 * Provider that facilitates access to the underlying server configuration
 * storage.
 *
 * @author Alex Mateescu
 *
 */
public interface PropertiesProvider {

	/**
	 * Load all server configuration properties
	 *
	 * @return Mapping between properties and values
	 */
	Map<String, String> loadProperties();

	/**
	 * Add a server property
	 *
	 * @param name
	 *            The name of the property to insert
	 * @param value
	 *            The initial value of the property
	 */
	void insertProperty(String name, String value);

	/**
	 * Update a server property
	 *
	 * @param name
	 *            The name of the property to update
	 * @param value
	 *            The new value of the property
	 */
	void updateProperty(String name, String value);

	/**
	 * Delete a server property
	 *
	 * @param name
	 *            The name of the property to be deleted
	 */
	void deleteProperty(String name);

}
