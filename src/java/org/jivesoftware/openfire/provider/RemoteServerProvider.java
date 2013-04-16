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

import java.util.Collection;

import org.jivesoftware.openfire.server.RemoteServerConfiguration;
import org.jivesoftware.openfire.server.RemoteServerConfiguration.Permission;

/**
 * Provider that facilitates access to the underlying remote server storage.
 *
 * @author Alex Mateescu
 *
 */
public interface RemoteServerProvider {

	/**
	 * Adds a new permission for the specified remote server.
	 *
	 * @param configuration
	 *            the new configuration for a remote server
	 */
	void addConfiguration(RemoteServerConfiguration configuration);

	/**
	 * Returns the configuration for a remote server or <tt>null</tt> if none
	 * was found.
	 *
	 * @param domain
	 *            the domain of the remote server.
	 * @return the configuration for a remote server or <tt>null</tt> if none
	 *         was found.
	 */
	RemoteServerConfiguration getConfiguration(String domain);

	/**
	 * Retrieves all stored configurations for a given permission.
	 *
	 * @param permission
	 *            The permission to retrieve configurations for.
	 * @return Associated configurations
	 */
	Collection<RemoteServerConfiguration> getConfigurations(
			Permission permission);

	/**
	 * Removes any existing defined permission and configuration for the
	 * specified remote server.
	 *
	 * @param domain
	 *            the domain of the remote server.
	 */
	void deleteConfiguration(String domain);

}