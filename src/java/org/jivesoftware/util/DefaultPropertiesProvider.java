/**
 * $Revision$
 * $Date$
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
package org.jivesoftware.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.provider.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default properties provider. Enables access to SQL enabled storage.
 *
 * @author Alex Mateescu
 *
 */
public class DefaultPropertiesProvider implements PropertiesProvider {
	private static final Logger log = LoggerFactory
			.getLogger(DefaultPropertiesProvider.class);

	private static final String LOAD_PROPERTIES = "SELECT name, propValue FROM ofProperty";
	private static final String INSERT_PROPERTY = "INSERT INTO ofProperty(name, propValue) VALUES(?,?)";
	private static final String UPDATE_PROPERTY = "UPDATE ofProperty SET propValue=? WHERE name=?";
	private static final String DELETE_PROPERTY = "DELETE FROM ofProperty WHERE name LIKE ?";

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> loadProperties() {
		Map<String, String> properties = new HashMap<String, String>();

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(LOAD_PROPERTIES);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);
				String value = rs.getString(2);
				properties.put(name, value);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DbConnectionManager.closeConnection(rs, pstmt, con);
		}

		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	public void insertProperty(String name, String value) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(INSERT_PROPERTY);
			pstmt.setString(1, name);
			pstmt.setString(2, value);
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
	public void updateProperty(String name, String value) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(UPDATE_PROPERTY);
			pstmt.setString(1, value);
			pstmt.setString(2, name);
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
	public void deleteProperty(String name) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(DELETE_PROPERTY);
			pstmt.setString(1, name + "%");
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

}
