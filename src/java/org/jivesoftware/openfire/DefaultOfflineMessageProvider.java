package org.jivesoftware.openfire;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.provider.OfflineMessageProvider;
import org.jivesoftware.util.FastDateFormat;
import org.jivesoftware.util.JiveConstants;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOfflineMessageProvider implements OfflineMessageProvider {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultOfflineMessageProvider.class);

	private final FastDateFormat dateFormat = FastDateFormat.getInstance(
			JiveConstants.XMPP_DATETIME_FORMAT, TimeZone.getTimeZone("UTC"));
	private final FastDateFormat dateFormatOld = FastDateFormat.getInstance(
			JiveConstants.XMPP_DELAY_DATETIME_FORMAT,
			TimeZone.getTimeZone("UTC"));

	/**
	 * Pattern to use for detecting invalid XML characters. Invalid XML
	 * characters will be removed from the stored offline messages.
	 */
	private final Pattern pattern = Pattern.compile("&\\#[\\d]+;");

	private static final String INSERT_OFFLINE = "INSERT INTO ofOffline (username, messageID, creationDate, messageSize, stanza) "
			+ "VALUES (?, ?, ?, ?, ?)";
	private static final String LOAD_OFFLINE = "SELECT stanza, creationDate FROM ofOffline WHERE username=?";
	private static final String LOAD_OFFLINE_MESSAGE = "SELECT stanza FROM ofOffline WHERE username=? AND creationDate=?";
	private static final String SELECT_SIZE_OFFLINE = "SELECT SUM(messageSize) FROM ofOffline WHERE username=?";
	private static final String SELECT_SIZE_ALL_OFFLINE = "SELECT SUM(messageSize) FROM ofOffline";
	private static final String DELETE_OFFLINE = "DELETE FROM ofOffline WHERE username=?";
	private static final String DELETE_OFFLINE_MESSAGE = "DELETE FROM ofOffline WHERE username=? AND creationDate=?";

	/**
	 * {@inheritDoc}
	 */
	public void addMessage(String username, long messageID, String msgXML) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(INSERT_OFFLINE);
			pstmt.setString(1, username);
			pstmt.setLong(2, messageID);
			pstmt.setString(3, StringUtils.dateToMillis(new java.util.Date()));
			pstmt.setInt(4, msgXML.length());
			pstmt.setString(5, msgXML);
			pstmt.executeUpdate();
		}

		catch (Exception e) {
			log.error(LocaleUtils.getLocalizedString("admin.error"), e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<OfflineMessage> getMessages(String username,
			boolean delete, SAXReader xmlReader) {
		List<OfflineMessage> messages = new ArrayList<OfflineMessage>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			// Get a sax reader from the pool
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(LOAD_OFFLINE);
			pstmt.setString(1, username);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String msgXML = rs.getString(1);
				Date creationDate = new Date(Long.parseLong(rs.getString(2)
						.trim()));
				OfflineMessage message;
				try {
					message = new OfflineMessage(creationDate, xmlReader.read(
							new StringReader(msgXML)).getRootElement());
				} catch (DocumentException e) {
					// Try again after removing invalid XML chars (e.g. &#12;)
					Matcher matcher = pattern.matcher(msgXML);
					if (matcher.find()) {
						msgXML = matcher.replaceAll("");
					}
					message = new OfflineMessage(creationDate, xmlReader.read(
							new StringReader(msgXML)).getRootElement());
				}

				// Add a delayed delivery (XEP-0203) element to the message.
				Element delay = message.addChildElement("delay",
						"urn:xmpp:delay");
				delay.addAttribute("from", XMPPServer.getInstance()
						.getServerInfo().getXMPPDomain());
				delay.addAttribute("stamp", dateFormat.format(creationDate));
				// Add a legacy delayed delivery (XEP-0091) element to the
				// message. XEP is obsolete and support should be dropped in
				// future.
				delay = message.addChildElement("x", "jabber:x:delay");
				delay.addAttribute("from", XMPPServer.getInstance()
						.getServerInfo().getXMPPDomain());
				delay.addAttribute("stamp", dateFormatOld.format(creationDate));
				messages.add(message);
			}
		} catch (Exception e) {
			log.error("Error retrieving offline messages of username: "
					+ username, e);
		} finally {
			DbConnectionManager.closeConnection(rs, pstmt, con);
		}
		return messages;
	}

	/**
	 * {@inheritDoc}
	 */
	public OfflineMessage getMessage(String username, Date creationDate, SAXReader xmlReader) {
		OfflineMessage message = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			// Get a sax reader from the pool
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(LOAD_OFFLINE_MESSAGE);
			pstmt.setString(1, username);
			pstmt.setString(2, StringUtils.dateToMillis(creationDate));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String msgXML = rs.getString(1);
				message = new OfflineMessage(creationDate, xmlReader.read(
						new StringReader(msgXML)).getRootElement());
				// Add a delayed delivery (XEP-0203) element to the message.
				Element delay = message.addChildElement("delay",
						"urn:xmpp:delay");
				delay.addAttribute("from", XMPPServer.getInstance()
						.getServerInfo().getXMPPDomain());
				delay.addAttribute("stamp", dateFormat.format(creationDate));
				// Add a legacy delayed delivery (XEP-0091) element to the
				// message. XEP is obsolete and support should be dropped in
				// future.
				delay = message.addChildElement("x", "jabber:x:delay");
				delay.addAttribute("from", XMPPServer.getInstance()
						.getServerInfo().getXMPPDomain());
				delay.addAttribute("stamp", dateFormatOld.format(creationDate));
			}
		} catch (Exception e) {
			log.error("Error retrieving offline messages of username: "
					+ username + " creationDate: " + creationDate, e);
		} finally {
			DbConnectionManager.closeConnection(rs, pstmt, con);
		}

		return message;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getSize(String username) {
		int size = 0;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(SELECT_SIZE_OFFLINE);
			pstmt.setString(1, username);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				size = rs.getInt(1);
			}
		} catch (Exception e) {
			log.error(LocaleUtils.getLocalizedString("admin.error"), e);
		} finally {
			DbConnectionManager.closeConnection(rs, pstmt, con);
		}
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getSize() {
		int size = 0;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(SELECT_SIZE_ALL_OFFLINE);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				size = rs.getInt(1);
			}
		} catch (Exception e) {
			log.error(LocaleUtils.getLocalizedString("admin.error"), e);
		} finally {
			DbConnectionManager.closeConnection(rs, pstmt, con);
		}
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteMessages(String username) {
		boolean deleted = false;
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(DELETE_OFFLINE);
			pstmt.setString(1, username);
			pstmt.executeUpdate();

			deleted = true;
		} catch (Exception e) {
			log.error("Error deleting offline messages of username: {}",
					username, e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}

		return deleted;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteMessage(String username, Date creationDate) {
		boolean deleted = false;
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(DELETE_OFFLINE_MESSAGE);
			pstmt.setString(1, username);
			pstmt.setString(2, StringUtils.dateToMillis(creationDate));
			pstmt.executeUpdate();

			deleted = true;
		} catch (Exception e) {
			log.error("Error deleting offline messages of username: "
					+ username + " creationDate: " + creationDate, e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}

		return deleted;
	}

}
