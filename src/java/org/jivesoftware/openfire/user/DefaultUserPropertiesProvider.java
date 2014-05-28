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
package org.jivesoftware.openfire.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.provider.UserPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default user properties provider. Enables access to SQL enabled storage.
 *
 * @author Alex Mateescu
 *
 */
public class DefaultUserPropertiesProvider implements UserPropertiesProvider {

	private static final Logger Log = LoggerFactory
			.getLogger(DefaultUserPropertiesProvider.class);

    private static final String LOAD_PROPERTIES =
            "SELECT name, propValue FROM ofUserProp WHERE username=?";
        private static final String LOAD_PROPERTY =
            "SELECT propValue FROM ofUserProp WHERE username=? AND name=?";
        private static final String DELETE_PROPERTY =
            "DELETE FROM ofUserProp WHERE username=? AND name=?";
        private static final String UPDATE_PROPERTY =
            "UPDATE ofUserProp SET propValue=? WHERE name=? AND username=?";
        private static final String INSERT_PROPERTY =
            "INSERT INTO ofUserProp (username, name, propValue) VALUES (?, ?, ?)";
        private static final String DELETE_USER_PROPS =
                "DELETE FROM ofUserProp WHERE username=?";

    /**
     * {@inheritDoc}
     */
    public String getPropertyValue(String username, String propertyName) {
        String propertyValue = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_PROPERTY);
            pstmt.setString(1, username);
            pstmt.setString(2, propertyName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                propertyValue = rs.getString(1);
            }
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return propertyValue;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> loadProperties(String username) {
    	Map<String, String> properties = new HashMap<String, String>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_PROPERTIES);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                properties.put(rs.getString(1), rs.getString(2));
            }
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return properties;
    }

    /**
     * {@inheritDoc}
     */
    public void insertProperty(String username, String propName, String propValue) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(INSERT_PROPERTY);
            pstmt.setString(1, username);
            pstmt.setString(2, propName);
            pstmt.setString(3, propValue);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateProperty(String username, String propName, String propValue) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_PROPERTY);
            pstmt.setString(1, propValue);
            pstmt.setString(2, propName);
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean deleteUserProperties(String username) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean abortTransaction = false;
        boolean deleted = false;

        try {
            // Delete all of the users's extended properties
            con = DbConnectionManager.getTransactionConnection();
            pstmt = con.prepareStatement(DELETE_USER_PROPS);
            pstmt.setString(1, username);
            pstmt.execute();
            deleted = true;
            DbConnectionManager.fastcloseStmt(pstmt);
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            abortTransaction = true;
        } finally {
            DbConnectionManager.closeStatement(pstmt);
            DbConnectionManager.closeTransactionConnection(pstmt, con, abortTransaction);
        }

        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteProperty(String username, String propName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(DELETE_PROPERTY);
            pstmt.setString(1, username);
            pstmt.setString(2, propName);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
}
