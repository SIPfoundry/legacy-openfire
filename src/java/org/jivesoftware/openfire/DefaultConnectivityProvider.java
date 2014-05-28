/**
 * $RCSfile$
 * $Revision$
 * $Date$
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
package org.jivesoftware.openfire;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.provider.ConnectivityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default connectivity provider. Enables access to SQL enabled storage.
 * 
 * @author Alex Mateescu
 * 
 */
public class DefaultConnectivityProvider implements ConnectivityProvider {
	private static Logger log = LoggerFactory
			.getLogger(DefaultConnectivityProvider.class);

	/**
	 * {@inheritDoc}
	 */
	public void verifyDataSource() {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement("SELECT count(*) FROM ofID");
			rs = pstmt.executeQuery();
			rs.next();
		} catch (Exception e) {
			System.err.println("Database setup or configuration error: "
					+ "Please verify your database settings and check the "
					+ "logs/error.log file for detailed error messages.");
			log.error("Database could not be accessed", e);
			throw new IllegalArgumentException(e);
		} finally {
			DbConnectionManager.closeConnection(rs, pstmt, con);
		}
	}

}
