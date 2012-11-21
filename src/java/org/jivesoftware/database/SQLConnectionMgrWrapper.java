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
package org.jivesoftware.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.provider.ConnectionManagerWrapper;

/**
 * Wraps access to SQL-enabled storage
 *
 * @see ConnectionManagerWrapper
 * @author Alex Mateescu
 *
 */
public class SQLConnectionMgrWrapper implements ConnectionManagerWrapper {

	/**
	 * {@inheritDoc}
	 */
	public void shutdown() {
		DbConnectionManager.destroyConnectionProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSetupMode() {
		return DbConnectionManager.isSetupMode();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmbeddedDB() {
		return DbConnectionManager.isEmbeddedDB();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConnectionProvider(ConnectionProvider provider) {
		DbConnectionManager.setConnectionProvider(provider);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectionProvider getConnectionProvider() {
		return DbConnectionManager.getConnectionProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTestQuery(String driver) {
		return DbConnectionManager.getTestSQL(driver);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean checkPluginSchema(Plugin plugin) {
		return DbConnectionManager.getSchemaManager().checkPluginSchema(plugin);
	}

	/**
	 * {@inheritDoc}
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		Connection con = DbConnectionManager.getConnection();
		DatabaseMetaData meta = con.getMetaData();
		con.close();

		return meta;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTransactionIsolation() throws SQLException {
		Connection con = DbConnectionManager.getConnection();
		int isolation = con.getTransactionIsolation();
		con.close();

		return isolation;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isProfilingEnabled() {
		return DbConnectionManager.isProfilingEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProfilingEnabled(boolean enable) {
		DbConnectionManager.setProfilingEnabled(enable);
	}
}
