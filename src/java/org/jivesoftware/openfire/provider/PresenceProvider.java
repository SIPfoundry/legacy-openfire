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

import java.util.Date;

/**
 * Provider that facilitates access to the underlying presence storage.
 *
 * @author Alex Mateescu
 *
 */
public interface PresenceProvider {

	/**
	 * Encoding for when there is no date stored
	 */
	final long NULL_LONG = -1L;

	/**
	 * Loads offline presence data for the user into cache.
	 *
	 * @param username
	 *            the user name.
	 * @return
	 */
	TimePresence loadOfflinePresence(String username);

	/**
	 * Deletes the offline presence from storage
	 *
	 * @param username
	 *            the user name.
	 */
	void deleteOfflinePresenceFromDB(String username);

	/**
	 * Inserts the offline presence into storage
	 *
	 * @param username
	 *            the user name.
	 * @param offlinePresence
	 *            offline message
	 * @param offlinePresenceDate
	 *            offline timestamp
	 */
	void insertOfflinePresenceIntoDB(String username, String offlinePresence,
			Date offlinePresenceDate);

	/**
	 * Bean. Used to return the loaded offline message and timestamp.
	 *
	 * @author Alex Mateescu
	 *
	 */
	public static class TimePresence {
		private final long lastActivity;
		private final String presence;

		public TimePresence(long lastActivity, String presence) {
			this.lastActivity = lastActivity;
			this.presence = presence;
		}

		public long getLastActivity() {
			return lastActivity;
		}

		public String getPresence() {
			return presence;
		}
	}
}