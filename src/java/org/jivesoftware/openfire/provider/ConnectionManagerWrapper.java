/**
 * $RCSfile$
 * $Revision: 3055 $
 * $Date: 2005-11-10 21:57:51 -0300 (Thu, 10 Nov 2005) $
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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.jivesoftware.database.ConnectionProvider;
import org.jivesoftware.openfire.container.Plugin;

/**
 * Wraps access to the underlying storage connection. While various providers
 * can access the connection directly, some functionality must be exposed to
 * other layers. In order to keep them decoupled from the actual connection, we
 * wrap access inside this class
 *
 * @author Alex Mateescu
 *
 */
public interface ConnectionManagerWrapper {

	/**
	 * Checks whether the server setup has completed and the storage layer is
	 * accessible.
	 *
	 * @return <code>true</code> if the server is running in setup mode,
	 *         <code>false</code> when the server setup has completed
	 */
	boolean isSetupMode();

	/**
	 * Checks whether the server is using an embedded DB
	 *
	 * @return <code>true</code> whether the DB in use is an embedded one
	 */
	boolean isEmbeddedDB();

	/**
	 * Sets a {@linkplain ConnectionProvider} for the DB in use
	 *
	 * @param provider
	 */
	void setConnectionProvider(ConnectionProvider provider);

	/**
	 * Retrieves the {@linkplain ConnectionProvider} used by the current DB
	 *
	 * @return the connection provider
	 */
	ConnectionProvider getConnectionProvider();

	/**
	 * Retrieves data about the capabilities of the current storage handler
	 *
	 * @return @see java.sql.DatabaseMetaData
	 * @throws SQLException
	 */
	DatabaseMetaData getMetaData() throws SQLException;

	/**
	 * Gets the transaction isolation supported by the current storage. Metadata
	 * should be examined first to check for transaction support availability.
	 *
	 * @return code for the supported isolation level
	 * @throws SQLException
	 */
	int getTransactionIsolation() throws SQLException;

	/**
	 * Retrieves a query that can be used to test the connectivity state of the
	 * current storage. This should be a simple query, that executes as fast as
	 * possible.
	 *
	 * @param driver
	 *            the name of the driver in use; used to discriminate between
	 *            several backends wrapped together
	 * @return a query
	 */
	String getTestQuery(String driver);

	/**
	 * Checks whether the schema is up to date. Performs any necessary updates,
	 * if applicable.
	 *
	 * @param plugin
	 *            the name of the plugin whose schema needs to be verified
	 * @return <code>true</code> whether the schema was up to date or was
	 *         updated successfully, <code>false</code> when the schema update
	 *         fails
	 */
	boolean checkPluginSchema(Plugin plugin);

	/**
	 * Check whether the connection profiling is enabled
	 *
	 * @return <code>true</code> if profiling is enabled, <code>false</code>
	 *         otherwise
	 */
	boolean isProfilingEnabled();

	/**
	 * Sets the connection profiling state
	 *
	 * @param enable
	 *            self-explaining
	 */
	void setProfilingEnabled(boolean enable);

	/**
	 * Perform any necessary cleanup when the server is shutting down.
	 */
	void shutdown();
}