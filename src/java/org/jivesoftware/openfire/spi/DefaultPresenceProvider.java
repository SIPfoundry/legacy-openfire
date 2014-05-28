package org.jivesoftware.openfire.spi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.provider.PresenceProvider;
import org.jivesoftware.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default presence provider. Enables access to SQL enabled storage.
 *
 * @author Alex Mateescu
 *
 */
public class DefaultPresenceProvider implements PresenceProvider {
    private static final Logger Log = LoggerFactory.getLogger(DefaultPresenceProvider.class);

    private static final String LOAD_OFFLINE_PRESENCE =
            "SELECT offlinePresence, offlineDate FROM ofPresence WHERE username=?";
    private static final String INSERT_OFFLINE_PRESENCE =
            "INSERT INTO ofPresence(username, offlinePresence, offlineDate) VALUES(?,?,?)";
    private static final String DELETE_OFFLINE_PRESENCE =
            "DELETE FROM ofPresence WHERE username=?";

    private static final String NULL_STRING = "NULL";

	/**
	 * {@inheritDoc}
	 */
    public TimePresence loadOfflinePresence(String username) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        TimePresence tp = null;

        try {
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(LOAD_OFFLINE_PRESENCE);
                pstmt.setString(1, username);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    String offlinePresence = DbConnectionManager.getLargeTextField(rs, 1);
                    if (rs.wasNull()) {
                        offlinePresence = NULL_STRING;
                    }
                    long offlineDate = Long.parseLong(rs.getString(2).trim());
                    tp = new TimePresence(offlineDate, offlinePresence);
                }
                else {
                    tp = new TimePresence(NULL_LONG, NULL_STRING);
                }
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        
        return tp;
    }

    /**
     * {@inheritDoc}
     */
    public void insertOfflinePresenceIntoDB(String username, String offlinePresence, Date offlinePresenceDate) {
        // Insert data into the database.
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(INSERT_OFFLINE_PRESENCE);
            pstmt.setString(1, username);
            if (offlinePresence != null) {
                DbConnectionManager.setLargeTextField(pstmt, 2, offlinePresence);
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }
            pstmt.setString(3, StringUtils.dateToMillis(offlinePresenceDate));
            pstmt.execute();
        } catch (SQLException sqle) {
            Log.error("Error storing offline presence of user: " + username, sqle);
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteOfflinePresenceFromDB(String username) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(DELETE_OFFLINE_PRESENCE);
            pstmt.setString(1, username);
            pstmt.execute();
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
}
