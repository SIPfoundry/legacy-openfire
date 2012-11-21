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
package org.jivesoftware.openfire.provider;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.spi.ConversationLogEntry;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoom;
import org.xmpp.packet.JID;

/**
 * Provider that facilitates access to the underlying MUC storage.
 *
 * @author Alex Mateescu
 *
 */
public interface MultiUserChatProvider {

	/**
	 * Loads the list of configured services stored in the database.
	 *
	 * @return Mapping between subdomains and services.
	 */
	Map<String, MultiUserChatService> loadServices();

	/**
	 * Gets a specific subdomain/service's ID number.
	 *
	 * @param subdomain
	 *            Subdomain to retrieve ID for.
	 * @return ID number of service.
	 */
	long loadServiceID(String subdomain);

	/**
	 * Gets a specific subdomain by a service's ID number.
	 *
	 * @param serviceID
	 *            ID to retrieve subdomain for.
	 * @return Subdomain of service.
	 */
	String loadServiceSubdomain(Long serviceID);

	/**
	 * Inserts a new MUC service into the database.
	 *
	 * @param subdomain
	 *            Subdomain of new service.
	 * @param description
	 *            Description of MUC service. Can be null for default
	 *            description.
	 * @param isHidden
	 *            True if the service should be hidden from service listing.
	 */
	void insertService(String subdomain, String description, Boolean isHidden);

	/**
	 * Updates an existing service's subdomain and description in the database.
	 *
	 * @param serviceID
	 *            ID of the service to update.
	 * @param subdomain
	 *            Subdomain to set service to.
	 * @param description
	 *            Description of MUC service. Can be null for default
	 *            description.
	 */
	void updateService(Long serviceID, String subdomain, String description);

	/**
	 * Deletes a service based on service ID.
	 *
	 * @param serviceID
	 *            ID of the service to delete.
	 */
	void deleteService(Long serviceID);

	/**
	 * Returns the reserved room nickname for the bare JID in a given room or
	 * null if none.
	 *
	 * @param room
	 *            the room where the user would like to obtain his reserved
	 *            nickname.
	 * @param bareJID
	 *            The bare jid of the user of which you'd like to obtain his
	 *            reserved nickname.
	 * @return the reserved room nickname for the bare JID or null if none.
	 */
	String getReservedNickname(MUCRoom room, String bareJID);

	/**
	 * Loads the room configuration from the database if the room was
	 * persistent.
	 *
	 * @param room
	 *            the room to load from the database if persistent
	 */
	void loadFromDB(LocalMUCRoom room);

	/**
	 * Save the room configuration to the DB.
	 *
	 * @param room
	 *            The room to save its configuration.
	 */
	void saveToDB(LocalMUCRoom room);

	/**
	 * Removes the room configuration and its affiliates from the database.
	 *
	 * @param room
	 *            the room to remove from the database.
	 */
	void deleteFromDB(MUCRoom room);

	/**
	 * Loads all the rooms that had occupants after a given date from the
	 * database. This query will be executed only when the service is starting
	 * up.
	 *
	 * @param chatserver
	 *            the chat server that will hold the loaded rooms.
	 * @param emptyDate
	 *            rooms that hadn't been used before this date won't be loaded.
	 * @param packetRouter
	 *            the PacketRouter that loaded rooms will use to send packets.
	 * @return a collection with all the persistent rooms.
	 */
	Collection<LocalMUCRoom> loadRoomsFromDB(MultiUserChatService chatserver,
			Date emptyDate, PacketRouter packetRouter);

	/**
	 * Updates the room's subject in the database.
	 *
	 * @param room
	 *            the room to update its subject in the database.
	 */
	void updateRoomSubject(MUCRoom room);

	/**
	 * Updates the room's lock status in the database.
	 *
	 * @param room
	 *            the room to update its lock status in the database.
	 */
	void updateRoomLock(LocalMUCRoom room);

	/**
	 * Updates the room's lock status in the database.
	 *
	 * @param room
	 *            the room to update its lock status in the database.
	 */
	void updateRoomEmptyDate(MUCRoom room);

	/**
	 * Update the DB with the new affiliation of the user in the room. The new
	 * information will be saved only if the room is_persistent and has already
	 * been saved to the database previously.
	 *
	 * @param room
	 *            The room where the affiliation of the user was updated.
	 * @param bareJID
	 *            The bareJID of the user to update this affiliation.
	 * @param nickname
	 *            The reserved nickname of the user in the room or null if none.
	 * @param newAffiliation
	 *            the new affiliation of the user in the room.
	 * @param oldAffiliation
	 *            the previous affiliation of the user in the room.
	 */
	void saveAffiliationToDB(MUCRoom room, JID bareJID, String nickname,
			MUCRole.Affiliation newAffiliation,
			MUCRole.Affiliation oldAffiliation);

	/**
	 * Removes the affiliation of the user from the DB if the room is
	 * persistent.
	 *
	 * @param room
	 *            The room where the affiliation of the user was removed.
	 * @param bareJID
	 *            The bareJID of the user to remove his affiliation.
	 * @param oldAffiliation
	 *            the previous affiliation of the user in the room.
	 */
	void removeAffiliationFromDB(MUCRoom room, JID bareJID,
			MUCRole.Affiliation oldAffiliation);

	/**
	 * Removes the affiliation of the user from the DB if ANY room that is
	 * persistent.
	 *
	 * @param bareJID
	 *            The bareJID of the user to remove his affiliation from ALL
	 *            persistent rooms.
	 */
	void removeAffiliationFromDB(JID bareJID);

	/**
	 * Saves the conversation log entry to the database.
	 *
	 * @param entry
	 *            the ConversationLogEntry to save to the database.
	 * @return true if the ConversationLogEntry was saved successfully to the
	 *         database.
	 */
	boolean saveConversationLogEntry(ConversationLogEntry entry);

	/**
	 * Returns a Jive property.
	 *
	 * @param subdomain
	 *            the subdomain of the service to retrieve a property from
	 * @param name
	 *            the name of the property to return.
	 * @return the property value specified by name.
	 */
	String getProperty(String subdomain, String name);

	/**
	 * Returns a Jive property. If the specified property doesn't exist, the
	 * <tt>defaultValue</tt> will be returned.
	 *
	 * @param subdomain
	 *            the subdomain of the service to retrieve a property from
	 * @param name
	 *            the name of the property to return.
	 * @param defaultValue
	 *            value returned if the property doesn't exist.
	 * @return the property value specified by name.
	 */
	String getProperty(String subdomain, String name, String defaultValue);

	/**
	 * Returns an integer value Jive property. If the specified property doesn't
	 * exist, the <tt>defaultValue</tt> will be returned.
	 *
	 * @param subdomain
	 *            the subdomain of the service to retrieve a property from
	 * @param name
	 *            the name of the property to return.
	 * @param defaultValue
	 *            value returned if the property doesn't exist or was not a
	 *            number.
	 * @return the property value specified by name or <tt>defaultValue</tt>.
	 */
	int getIntProperty(String subdomain, String name, int defaultValue);

	/**
	 * Returns a long value Jive property. If the specified property doesn't
	 * exist, the <tt>defaultValue</tt> will be returned.
	 *
	 * @param subdomain
	 *            the subdomain of the service to retrieve a property from
	 * @param name
	 *            the name of the property to return.
	 * @param defaultValue
	 *            value returned if the property doesn't exist or was not a
	 *            number.
	 * @return the property value specified by name or <tt>defaultValue</tt>.
	 */
	long getLongProperty(String subdomain, String name, long defaultValue);

	/**
	 * Returns a boolean value Jive property.
	 *
	 * @param subdomain
	 *            the subdomain of the service to retrieve a property from
	 * @param name
	 *            the name of the property to return.
	 * @return true if the property value exists and is set to <tt>"true"</tt>
	 *         (ignoring case). Otherwise <tt>false</tt> is returned.
	 */
	boolean getBooleanProperty(String subdomain, String name);

	/**
	 * Returns a boolean value Jive property. If the property doesn't exist, the
	 * <tt>defaultValue</tt> will be returned.
	 *
	 * If the specified property can't be found, or if the value is not a
	 * number, the <tt>defaultValue</tt> will be returned.
	 *
	 * @param subdomain
	 *            the subdomain of the service to retrieve a property from
	 * @param name
	 *            the name of the property to return.
	 * @param defaultValue
	 *            value returned if the property doesn't exist.
	 * @return true if the property value exists and is set to <tt>"true"</tt>
	 *         (ignoring case). Otherwise <tt>false</tt> is returned.
	 */
	boolean getBooleanProperty(String subdomain, String name,
			boolean defaultValue);

	/**
	 * Return all immediate children property names of a parent Jive property as
	 * a list of strings, or an empty list if there are no children. For
	 * example, given the properties <tt>X.Y.A</tt>, <tt>X.Y.B</tt>,
	 * <tt>X.Y.C</tt> and <tt>X.Y.C.D</tt>, then the immediate child properties
	 * of <tt>X.Y</tt> are <tt>A</tt>, <tt>B</tt>, and <tt>C</tt> (<tt>C.D</tt>
	 * would not be returned using this method).
	 * <p>
	 *
	 * @param subdomain
	 *            the subdomain of the service to retrieve a property from
	 * @param parent
	 *            the root "node" of the properties to retrieve
	 * @return a List of all immediate children property names (Strings).
	 */
	List<String> getPropertyNames(String subdomain, String parent);

	/**
	 * Return all immediate children property values of a parent Jive property
	 * as a list of strings, or an empty list if there are no children. For
	 * example, given the properties <tt>X.Y.A</tt>, <tt>X.Y.B</tt>,
	 * <tt>X.Y.C</tt> and <tt>X.Y.C.D</tt>, then the immediate child properties
	 * of <tt>X.Y</tt> are <tt>X.Y.A</tt>, <tt>X.Y.B</tt>, and <tt>X.Y.C</tt>
	 * (the value of <tt>X.Y.C.D</tt> would not be returned using this method).
	 * <p>
	 *
	 * @param subdomain
	 *            the subdomain of the service to retrieve a property from
	 * @param parent
	 *            the name of the parent property to return the children for.
	 * @return all child property values for the given parent.
	 */
	List<String> getProperties(String subdomain, String parent);

	/**
	 * Returns all MUC service property names.
	 *
	 * @param subdomain
	 *            the subdomain of the service to retrieve a property from
	 * @return a List of all property names (Strings).
	 */
	List<String> getPropertyNames(String subdomain);

	/**
	 * Sets a Jive property. If the property doesn't already exists, a new one
	 * will be created.
	 *
	 * @param subdomain
	 *            the subdomain of the service to set a property for
	 * @param name
	 *            the name of the property being set.
	 * @param value
	 *            the value of the property being set.
	 */
	void setProperty(String subdomain, String name, String value);

	void setLocalProperty(String subdomain, String name, String value);

	/**
	 * Sets multiple Jive properties at once. If a property doesn't already
	 * exists, a new one will be created.
	 *
	 * @param subdomain
	 *            the subdomain of the service to set properties for
	 * @param propertyMap
	 *            a map of properties, keyed on property name.
	 */
	void setProperties(String subdomain, Map<String, String> propertyMap);

	/**
	 * Deletes a Jive property. If the property doesn't exist, the method does
	 * nothing. All children of the property will be deleted as well.
	 *
	 * @param subdomain
	 *            the subdomain of the service to delete a property from
	 * @param name
	 *            the name of the property to delete.
	 */
	void deleteProperty(String subdomain, String name);

	void deleteLocalProperty(String subdomain, String name);

	/**
	 * Resets (reloads) the properties for a specified subdomain.
	 *
	 * @param subdomain
	 *            the subdomain of the service to reload properties for.
	 */
	void refreshProperties(String subdomain);

	/**
	 * Load MUC service properties.
	 *
	 * @param properties
	 *            Map that stores the service properties
	 * @param serviceID
	 *            id of the service to load properties for
	 */
	void loadProperties(Map<String, String> properties, Long serviceID);

	/**
	 * Inserts a new service property
	 *
	 * @param name
	 *            Name of the property to insert
	 * @param value
	 *            Initial property value
	 * @param serviceID
	 *            id of the service to load properties for
	 */
	void insertProperty(String name, String value, Long serviceID);

	/**
	 * Updates a service property
	 *
	 * @param name
	 *            Name of the property to update
	 * @param value
	 *            New value for the property
	 * @param serviceID
	 *            id of the service to load properties for
	 */
	void updateProperty(String name, String value, Long serviceID);

	/**
	 * Deletes a service property
	 *
	 * @param name
	 *            Name of the property to delete
	 * @param serviceID
	 *            id of the service to delete property for
	 */
	void deleteProperty(String name, Long serviceID);

}