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
package org.jivesoftware.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jivesoftware.openfire.provider.UIDProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default UID provider. Enables access to SQL enabled storage.
 *
 * @author Alex Mateescu
 *
 */
public class DefaultUIDProvider implements UIDProvider {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultUIDProvider.class);

	private static final String CREATE_ID = "INSERT INTO ofID (id, idType) VALUES (1, ?)";

	private static final String LOAD_ID = "SELECT id FROM ofID WHERE idType=?";

	private static final String UPDATE_ID = "UPDATE ofID SET id=? WHERE idType=? AND id=?";

	/**
	 * {@inheritDoc}
	 * </br> The algorithm is as follows:
	 * <ol>
	 * <li>Select currentID from appropriate db row.
	 * <li>Increment id returned from db.
	 * <li>Update db row with new id where id=old_id.
	 * <li>If update fails another process checked out the block first; go back
	 * to step 1. Otherwise, done.
	 * </ol>
	 */
	public long[] getNextBlock(int type, int blockSize) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean abortTransaction = false;
		boolean success = false;
		long[] ids = null;

		try {
			con = DbConnectionManager.getTransactionConnection();
			// Get the current ID from the database.
			pstmt = con.prepareStatement(LOAD_ID);
			pstmt.setInt(1, type);
			rs = pstmt.executeQuery();

			long currentID = 1;
			if (rs.next()) {
				currentID = rs.getLong(1);
			} else {
				createNewID(con, type);
			}
			DbConnectionManager.fastcloseStmt(rs, pstmt);

			// Increment the id to define our block.
			long newID = currentID + blockSize;
			// The WHERE clause includes the last value of the id. This ensures
			// that an update will occur only if nobody else has performed an
			// update first.
			pstmt = con.prepareStatement(UPDATE_ID);
			pstmt.setLong(1, newID);
			pstmt.setInt(2, type);
			pstmt.setLong(3, currentID);
			// Check to see if the row was affected. If not, some other process
			// already changed the original id that we read. Therefore, this
			// round failed and we'll have to try again.
			success = pstmt.executeUpdate() == 1;
			if (success) {
				ids = new long[2];
				ids[0] = currentID;
				ids[1] = newID;
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			abortTransaction = true;
		} finally {
			DbConnectionManager.closeStatement(rs, pstmt);
			DbConnectionManager.closeTransactionConnection(con,
					abortTransaction);
		}

		return ids;
	}

	private void createNewID(Connection con, int type) throws SQLException {
		log.warn("Autocreating jiveID row for type '{}'", type);

		// create new ID row
		PreparedStatement pstmt = null;

		try {
			pstmt = con.prepareStatement(CREATE_ID);
			pstmt.setInt(1, type);
			pstmt.execute();
		} finally {
			DbConnectionManager.closeStatement(pstmt);
		}
	}
}
