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
package org.jivesoftware.openfire.component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.component.ExternalComponentConfiguration.Permission;
import org.jivesoftware.openfire.provider.ExternalComponentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default external component provider. Enables access to SQL enabled storage.
 *
 * @author Alex Mateescu
 *
 */
public class DefaultExternalComponentProvider implements ExternalComponentProvider {
    private static final Logger Log = LoggerFactory.getLogger(DefaultExternalComponentProvider.class);

    private static final String ADD_CONFIGURATION =
            "INSERT INTO ofExtComponentConf (subdomain,wildcard,secret,permission) VALUES (?,?,?,?)";
        private static final String DELETE_CONFIGURATION =
            "DELETE FROM ofExtComponentConf WHERE subdomain=? and wildcard=?";
        private static final String LOAD_CONFIGURATION =
            "SELECT secret,permission FROM ofExtComponentConf where subdomain=? AND wildcard=0";
        private static final String LOAD_WILDCARD_CONFIGURATION =
            "SELECT secret,permission FROM ofExtComponentConf where ? like subdomain AND wildcard=1";
        private static final String LOAD_CONFIGURATIONS =
            "SELECT subdomain,wildcard,secret FROM ofExtComponentConf where permission=?";


        /**
         * {@inheritDoc}
         */
        public void deleteConfigurationFromDB(ExternalComponentConfiguration configuration) {
            if (configuration == null) {
                // Do nothing
                return;
            }
            // Remove the permission for the entity from the database
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(DELETE_CONFIGURATION);
                pstmt.setString(1, configuration.getSubdomain() + (configuration.isWildcard() ? "%" : ""));
                pstmt.setInt(2, configuration.isWildcard() ? 1 : 0);
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
        public void addConfiguration(ExternalComponentConfiguration configuration) {
            // Remove the permission for the entity from the database
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(ADD_CONFIGURATION);
                pstmt.setString(1, configuration.getSubdomain() + (configuration.isWildcard() ? "%" : ""));
                pstmt.setInt(2, configuration.isWildcard() ? 1 : 0);
                pstmt.setString(3, configuration.getSecret());
                pstmt.setString(4, configuration.getPermission().toString());
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
        public ExternalComponentConfiguration getConfiguration(String subdomain, boolean useWildcard) {
            ExternalComponentConfiguration configuration = null;
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                // Check if there is a configuration for the subdomain
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(LOAD_CONFIGURATION);
                pstmt.setString(1, subdomain);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    configuration = new ExternalComponentConfiguration(subdomain, false, Permission.valueOf(rs.getString(2)),
                            rs.getString(1));
                }
            }
            catch (SQLException sqle) {
                Log.error(sqle.getMessage(), sqle);
            }
            finally {
                DbConnectionManager.closeConnection(rs, pstmt, con);
            }

            if (configuration == null && useWildcard) {
                // Check if there is a configuration that is using wildcards for domains
                try {
                    // Check if there is a configuration for the subdomain
                    con = DbConnectionManager.getConnection();
                    pstmt = con.prepareStatement(LOAD_WILDCARD_CONFIGURATION);
                    pstmt.setString(1, subdomain);
                    rs = pstmt.executeQuery();

                    while (rs.next()) {
                        configuration = new ExternalComponentConfiguration(subdomain, true, Permission.valueOf(rs.getString(2)),
                                rs.getString(1));
                    }
                }
                catch (SQLException sqle) {
                    Log.error(sqle.getMessage(), sqle);
                }
                finally {
                    DbConnectionManager.closeConnection(rs, pstmt, con);
               }
            }
            return configuration;
        }

        /**
         * {@inheritDoc}
         */
        public Collection<ExternalComponentConfiguration> getConfigurations(
                Permission permission) {
            Collection<ExternalComponentConfiguration> answer =
                    new ArrayList<ExternalComponentConfiguration>();
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(LOAD_CONFIGURATIONS);
                pstmt.setString(1, permission.toString());
                rs = pstmt.executeQuery();
                ExternalComponentConfiguration configuration;
                while (rs.next()) {
                    String subdomain = rs.getString(1);
                    boolean wildcard = rs.getInt(2) == 1;
                    // Remove the trailing % if using wildcards
                    subdomain = wildcard ? subdomain.substring(0, subdomain.length()-1) : subdomain;
                    configuration = new ExternalComponentConfiguration(subdomain, wildcard, permission,
                            rs.getString(3));
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
}