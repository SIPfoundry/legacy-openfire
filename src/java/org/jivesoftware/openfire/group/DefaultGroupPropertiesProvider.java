/**
 * $RCSfile$
 * $Revision: 3127 $
 * $Date: 2005-11-30 15:26:07 -0300 (Wed, 30 Nov 2005) $
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
package org.jivesoftware.openfire.group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.provider.GroupPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGroupPropertiesProvider implements GroupPropertiesProvider {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultGroupPropertiesProvider.class);

    private static final String GROUPLIST_CONTAINERS = "SELECT groupName from ofGroupProp where name='sharedRoster.groupList' AND propValue LIKE ?";
    private static final String GROUPS_FOR_PROP = "SELECT groupName from ofGroupProp WHERE name=? AND propValue=?";
	private static final String LOAD_PROPERTIES = "SELECT name, propValue FROM ofGroupProp WHERE groupName=?";
	private static final String DELETE_PROPERTY = "DELETE FROM ofGroupProp WHERE groupName=? AND name=?";
	private static final String UPDATE_PROPERTY = "UPDATE ofGroupProp SET propValue=? WHERE name=? AND groupName=?";
	private static final String INSERT_PROPERTY = "INSERT INTO ofGroupProp (groupName, name, propValue) VALUES (?, ?, ?)";
    private static final String LOAD_SHARED_GROUPS = "SELECT groupName FROM ofGroupProp WHERE name='sharedRoster.showInRoster' AND propValue IS NOT NULL AND propValue <> 'nobody'";
	private static final String SET_GROUP_NAME_2 = "UPDATE ofGroupProp SET groupName=? WHERE groupName=?";
	private static final String DELETE_PROPERTIES = "DELETE FROM ofGroupProp WHERE groupName=?";

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> loadProperties(Group group) {
    	// custom map implementation persists group property changes
    	// whenever one of the standard mutator methods are called
    	String name = group.getName();
    	DefaultGroupPropertyMap<String,String> result = new DefaultGroupPropertyMap<String,String>(group);
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_PROPERTIES);
            pstmt.setString(1, name);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String key = rs.getString(1);
                String value = rs.getString(2);
                if (key != null) {
                    if (value == null) {
                        result.remove(key);
                        log.warn("Deleted null property " + key + " for group: " + name);
                    } else {
                    	result.put(key, value, false); // skip persistence during load
                    }
                }
                else { // should not happen, but ...
                    log.warn("Ignoring null property key for group: " + name);
                }
            }
        }
        catch (SQLException sqle) {
            log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void insertProperty(String groupName, String propName,
			String propValue) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(INSERT_PROPERTY);
			pstmt.setString(1, groupName);
			pstmt.setString(2, propName);
			pstmt.setString(3, propValue);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateProperty(String groupName, String propName,
			String propValue) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(UPDATE_PROPERTY);
			pstmt.setString(1, propValue);
			pstmt.setString(2, propName);
			pstmt.setString(3, groupName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteProperty(String groupName, String propName) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(DELETE_PROPERTY);
			pstmt.setString(1, groupName);
			pstmt.setString(2, propName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getSharedGroupsNames() {
        Set<String> groupNames = new HashSet<String>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_SHARED_GROUPS);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                groupNames.add(rs.getString(1));
            }
        }
        catch (SQLException sqle) {
            log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

		return groupNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean setName(String oldName, String newName)
			throws UnsupportedOperationException, GroupAlreadyExistsException {
		boolean renamed = false;
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();
			pstmt = con.prepareStatement(SET_GROUP_NAME_2);
			pstmt.setString(1, newName);
			pstmt.setString(2, oldName);
			pstmt.executeUpdate();
			renamed = true;
			DbConnectionManager.fastcloseStmt(pstmt);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			abortTransaction = true;
		} finally {
			DbConnectionManager.closeStatement(pstmt);
			DbConnectionManager.closeTransactionConnection(con,
					abortTransaction);
		}

		return renamed;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteGroupProperties(String groupName) {
		boolean deleted = false;
		Connection con = null;
		PreparedStatement pstmt = null;
		boolean abortTransaction = false;
		try {
			con = DbConnectionManager.getTransactionConnection();

			// Remove all properties of the group.
			pstmt = con.prepareStatement(DELETE_PROPERTIES);
			pstmt.setString(1, groupName);
			pstmt.executeUpdate();
			deleted = true;
			DbConnectionManager.fastcloseStmt(pstmt);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			abortTransaction = true;
		} finally {
			DbConnectionManager.closeStatement(pstmt);
			DbConnectionManager.closeTransactionConnection(con,
					abortTransaction);
		}

		return deleted;
	}

	public Collection<String> getVisibleGroupNames(String userGroup) {
		Set<String> groupNames = new HashSet<String>();
        Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
		    con = DbConnectionManager.getConnection();
		    pstmt = con.prepareStatement(GROUPLIST_CONTAINERS);
		    pstmt.setString(1, "%" + userGroup + "%");
		    rs = pstmt.executeQuery();
		    while (rs.next()) {
		        groupNames.add(rs.getString(1));
		    }
		}
		catch (SQLException sqle) {
		    log.error(sqle.getMessage(), sqle);
		}
		finally {
		    DbConnectionManager.closeConnection(rs, pstmt, con);
		}
		return groupNames;
	}

	public Collection<String> getPublicSharedGroupNames() {
        return search("sharedRoster.showInRoster", "everybody");
	}

	public Collection<String> search(String key, String value) {
		Set<String> groupNames = new HashSet<String>();
        Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
		    con = DbConnectionManager.getConnection();
		    pstmt = con.prepareStatement(GROUPS_FOR_PROP);
		    pstmt.setString(1, key);
		    pstmt.setString(2, value);
		    rs = pstmt.executeQuery();
		    while (rs.next()) {
		        groupNames.add(rs.getString(1));
		    }
		}
		catch (SQLException sqle) {
		    log.error(sqle.getMessage(), sqle);
		}
		finally {
		    DbConnectionManager.closeConnection(rs, pstmt, con);
		}
		return groupNames;
	}
}
