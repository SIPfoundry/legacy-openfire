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

import org.jivesoftware.openfire.component.ExternalComponentConfiguration;
import org.jivesoftware.openfire.component.ExternalComponentConfiguration.Permission;

/**
 * Provider that facilitates access to the underlying external component
 * storage.
 *
 * @author Alex Mateescu
 *
 */
public interface ExternalComponentProvider {

	/**
	 * Returns the configuration for an external component. A query for the
	 * exact requested subdomain will be made. If nothing was found and using
	 * wildcards is requested then another query will be made but this time
	 * using wildcards.
	 *
	 * @param subdomain
	 *            the subdomain of the external component.
	 * @param useWildcard
	 *            true if an attempt to find a subdomain with wildcards should
	 *            be attempted.
	 * @return the configuration for an external component.
	 */
	ExternalComponentConfiguration getConfiguration(String subdomain,
			boolean useWildcard);

	/**
	 * Adds a new permission for the specified external component.
	 *
	 * @param configuration
	 *            the new configuration for a component.
	 */
	void addConfiguration(ExternalComponentConfiguration configuration);

	/**
	 * Retrieves all stored external component configurations for a given
	 * permission.
	 *
	 * @param permission
	 *            The permission to retrieve external component configurations
	 *            for.
	 * @return Associated external component configurations
	 */
	Collection<ExternalComponentConfiguration> getConfigurations(
			Permission permission);

	/**
	 * Removes any existing defined permission and configuration for the
	 * specified external component from the database.
	 *
	 * @param configuration
	 *            the external component configuration to delete.
	 */
	void deleteConfigurationFromDB(ExternalComponentConfiguration configuration);

}