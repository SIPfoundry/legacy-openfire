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

import org.dom4j.io.SAXReader;
import org.jivesoftware.openfire.OfflineMessage;

/**
 * Provider that facilitates access to the underlying offline messages storage.
 *
 * @author Alex Mateescu
 *
 */
public interface OfflineMessageProvider {

	/**
	 * Adds a message to storage. Messages will be stored and made available for
	 * later delivery.
	 *
	 * @param username
	 *            Recipient's name
	 * @param messageID
	 *            Generated unique id
	 * @param msgXML
	 *            XML representation of the message
	 */
	void addMessage(String username, long messageID, String msgXML);

	/**
	 * Returns a Collection of all messages in the store for a user. Messages
	 * may be deleted after being selected from the database depending on the
	 * delete param.
	 *
	 * @param username
	 *            the username of the user who's messages you'd like to receive.
	 * @param delete
	 *            true if the offline messages should be deleted.
	 * @param xmlReader
	 *            used to parse the stored entry.
	 * @return A collection containing all offline messages.
	 *
	 */
	Collection<OfflineMessage> getMessages(String username, boolean delete,
			SAXReader xmlReader);

	/**
	 * Returns the offline message of the specified user with the given creation
	 * date. The returned message will NOT be deleted from the database.
	 *
	 * @param username
	 *            the username of the user who's message you'd like to receive.
	 * @param creationDate
	 *            the date when the offline message was stored in the database.
	 * @param xmlReader
	 *            used to parse the stored entry.
	 * @return the offline message of the specified user with the given creation
	 *         stamp.
	 */
	OfflineMessage getMessage(String username, Date creationDate,
			SAXReader xmlReader);

	/**
	 * Returns the approximate size (in bytes) of the XML messages stored for a
	 * particular user.
	 *
	 * @param username
	 *            the username of the user.
	 * @return the approximate size of stored messages (in bytes).
	 */
	int getSize(String username);

	/**
	 * Returns the approximate size (in bytes) of the XML messages stored for
	 * all users.
	 *
	 * @return the approximate size of all stored messages (in bytes).
	 */
	int getSize();

	/**
	 * Deletes all offline messages in the store for a user.
	 *
	 * @param username
	 *            the username of the user who's messages are going to be
	 *            deleted.
	 */
	boolean deleteMessages(String username);

	/**
	 * Deletes the specified offline message in the store for a user. The way to
	 * identify the message to delete is based on the creationDate and username.
	 *
	 * @param username
	 *            the username of the user who's message is going to be deleted.
	 * @param creationDate
	 *            the date when the offline message was stored in the database.
	 */
	boolean deleteMessage(String username, Date creationDate);
}
