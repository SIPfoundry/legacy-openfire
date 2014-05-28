package org.jivesoftware.openfire;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.provider.PrivateStorageProvider;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.util.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPrivateStorageProvider implements PrivateStorageProvider {
    private static final Logger Log = LoggerFactory.getLogger(DefaultPrivateStorageProvider.class);

    private static final String LOAD_PRIVATE =
            "SELECT privateData FROM ofPrivate WHERE username=? AND namespace=?";
        private static final String INSERT_PRIVATE =
            "INSERT INTO ofPrivate (privateData,name,username,namespace) VALUES (?,?,?,?)";
        private static final String UPDATE_PRIVATE =
            "UPDATE ofPrivate SET privateData=?, name=? WHERE username=? AND namespace=?";
        private static final String DELETE_PRIVATES =
            "DELETE FROM ofPrivate WHERE username=?";

    /**
     * {@inheritDoc}
     */
    public void add(String username, Element data) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            StringWriter writer = new StringWriter();
            data.write(writer);
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_PRIVATE);
            pstmt.setString(1, username);
            pstmt.setString(2, data.getNamespaceURI());
            rs = pstmt.executeQuery();
            boolean update = false;
            if (rs.next()) {
                update = true;
            }
            DbConnectionManager.fastcloseStmt(rs, pstmt);
            if (update) {
                pstmt = con.prepareStatement(UPDATE_PRIVATE);
            }
            else {
                pstmt = con.prepareStatement(INSERT_PRIVATE);
            }
            pstmt.setString(1, writer.toString());
            pstmt.setString(2, data.getName());
            pstmt.setString(3, username);
            pstmt.setString(4, data.getNamespaceURI());
            pstmt.executeUpdate();
        }
        catch (Exception e) {
            Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Element get(String username, Element data, SAXReader xmlReader) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_PRIVATE);
            pstmt.setString(1, username);
            pstmt.setString(2, data.getNamespaceURI());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                data.clearContent();
                String result = rs.getString(1).trim();
                Document doc = xmlReader.read(new StringReader(result));
                data = doc.getRootElement();
            }
        } catch (Exception e) {
            Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return data;
    }

	/**
	 * {@inheritDoc}
	 */
    public void userDeleting(User user, Map<String,Object> params) {
        // Delete all private properties of the user
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(DELETE_PRIVATES);
            pstmt.setString(1, user.getUsername());
            pstmt.executeUpdate();
        }
        catch (Exception e) {
            Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
}