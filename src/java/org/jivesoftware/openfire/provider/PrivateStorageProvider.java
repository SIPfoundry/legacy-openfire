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

import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.openfire.user.User;

/**
 * Provider that facilitates access to the underlying private storage.
 *
 * @author Alex Mateescu
 *
 */
public interface PrivateStorageProvider {

	/**
	 * Stores private data. If the name and namespace of the element matches
	 * another stored private data XML document, then replace it with the new
	 * one.
	 *
	 * @param data
	 *            the data to store (XML element)
	 * @param username
	 *            the username of the account where private data is being stored
	 */
	public abstract void add(String username, Element data);

	/**
	 * Returns the data stored under a key corresponding to the name and
	 * namespace of the given element. The Element must be in the form:
	 * <p>
	 *
	 * <code>&lt;name xmlns='namespace'/&gt;</code>
	 * <p>
	 *
	 * If no data is currently stored under the given key, an empty element will
	 * be returned.
	 *
	 * @param data
	 *            an XML document who's element name and namespace is used to
	 *            match previously stored private data.
	 * @param username
	 *            the username of the account where private data is being
	 *            stored.
	 * @return the data stored under the given key or the data element.
	 */
	public abstract Element get(String username, Element data,
			SAXReader xmlReader);

	public abstract void userDeleting(User user, Map<String, Object> params);

}
