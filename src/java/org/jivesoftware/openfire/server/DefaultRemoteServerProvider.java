/**
+ * $Revision: $
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
package org.jivesoftware.openfire.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.provider.RemoteServerProvider;
import org.jivesoftware.openfire.server.RemoteServerConfiguration.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRemoteServerProvider implements RemoteServerProvider {
    private static final Logger Log = LoggerFactory.getLogger(DefaultRemoteServerProvider.class);

    private static final String ADD_CONFIGURATION =
            "INSERT INTO ofRemoteServerConf (xmppDomain,remotePort,permission) VALUES (?,?,?)";
        private static final String DELETE_CONFIGURATION =
            "DELETE FROM ofRemoteServerConf WHERE xmppDomain=?";
        private static final String LOAD_CONFIGURATION =
            "SELECT remotePort,permission FROM ofRemoteServerConf where xmppDomain=?";
        private static final String LOAD_CONFIGURATIONS =
            "SELECT xmppDomain,remotePort FROM ofRemoteServerConf where permission=?";

    /**
     * {@inheritDoc}
     */
	public void addConfiguration(RemoteServerConfiguration configuration) {
        java.sql.Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(ADD_CONFIGURATION);
            pstmt.setString(1, configuration.getDomain());
            pstmt.setInt(2, configuration.getRemotePort());
            pstmt.setString(3, configuration.getPermission().toString());
            pstmt.executeUpdate();
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    public RemoteServerConfiguration getConfiguration(String domain) {
        RemoteServerConfiguration configuration = null;
        java.sql.Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_CONFIGURATION);
            pstmt.setString(1, domain);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                configuration = new RemoteServerConfiguration(domain);
                configuration.setRemotePort(rs.getInt(1));
                configuration.setPermission(Permission.valueOf(rs.getString(2)));
            }
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<RemoteServerConfiguration> getConfigurations(
            Permission permission) {
        Collection<RemoteServerConfiguration> answer =
                new ArrayList<RemoteServerConfiguration>();
        java.sql.Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_CONFIGURATIONS);
            pstmt.setString(1, permission.toString());
            rs = pstmt.executeQuery();
            RemoteServerConfiguration configuration;
            while (rs.next()) {
                configuration = new RemoteServerConfiguration(rs.getString(1));
                configuration.setRemotePort(rs.getInt(2));
                configuration.setPermission(permission);
                answer.add(configuration);
            }
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return answer;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteConfiguration(String domain) {
        // Remove the permission for the entity from the database
        java.sql.Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(DELETE_CONFIGURATION);
            pstmt.setString(1, domain);
            pstmt.executeUpdate();
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
}
