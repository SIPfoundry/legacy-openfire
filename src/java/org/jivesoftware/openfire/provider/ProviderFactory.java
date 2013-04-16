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

import org.jivesoftware.util.ClassUtils;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class used to retrieve any and all providers.
 *
 * @author Alex Mateescu
 *
 */
public class ProviderFactory {

	private static Logger log = LoggerFactory.getLogger(ProviderFactory.class);

	private static ConnectionManagerWrapper wrapper;

	private ProviderFactory() {
		// don't need to instantiate this
	}

	public static UserProvider getUserProvider() {
		String className = loadProviderClassName(Provider.USER);

		return createInstance(className);
	}

	public static UserPropertiesProvider getUserPropertiesProvider() {
		String className = loadProviderClassName(Provider.USER_PROPS);

		return createInstance(className);
	}

	public static GroupProvider getGroupProvider() {
		String className = loadProviderClassName(Provider.GROUP);

		return createInstance(className);
	}

	public static GroupPropertiesProvider getGroupPropertiesProvider() {
		String className = loadProviderClassName(Provider.GROUP_PROPS);

		return createInstance(className);
	}

	public static RosterItemProvider getRosterProvider() {
		String className = loadProviderClassName(Provider.ROSTER);

		return createInstance(className);
	}

	public static VCardProvider getVCardProvider() {
		String className = loadProviderClassName(Provider.VCARD);

		return createInstance(className);
	}

	public static PresenceProvider getPresenceProvider() {
		String className = loadProviderClassName(Provider.PRESENCE);

		return createInstance(className);
	}

	public static MultiUserChatProvider getMUCProvider() {
		String className = loadProviderClassName(Provider.MUC);

		return createInstance(className);
	}

	public static PubSubProvider getPubsubProvider() {
		String className = loadProviderClassName(Provider.PUBSUB);

		return createInstance(className);
	}

	public static OfflineMessageProvider getOfflineProvider() {
		String className = loadProviderClassName(Provider.OFFLINE);

		return createInstance(className);
	}

	public static PrivacyListProvider getPrivacyListProvider() {
		String className = loadProviderClassName(Provider.PRIVACY);

		return createInstance(className);
	}

	public static PrivateStorageProvider getPrivateStorageProvider() {
		String className = loadProviderClassName(Provider.PRIVATE_STORAGE);

		return createInstance(className);
	}

	public static AuthProvider getAuthProvider() {
		String className = loadProviderClassName(Provider.AUTH);

		return createInstance(className);
	}

	public static SecurityAuditProvider getSecurityAuditProvider() {
		String className = loadProviderClassName(Provider.SECURITY_AUDIT);

		return createInstance(className);
	}

	public static RemoteServerProvider getRemoteServerProvider() {
		String className = loadProviderClassName(Provider.REMOTE_SERVER);

		return createInstance(className);
	}

	public static ExternalComponentProvider getExternalComponentProvider() {
		String className = loadProviderClassName(Provider.EXTERNAL_COMPONENT);

		return createInstance(className);
	}

	public static UIDProvider getUIDProvider() {
		String className = loadProviderClassName(Provider.UID);

		return createInstance(className);
	}

	public static ConnectivityProvider getConnectivityProvider() {
		String className = loadProviderClassName(Provider.CONNECTIVITY);

		return createInstance(className);
	}

	public static LockOutProvider getLockoutProvider() {
		String className = loadProviderClassName(Provider.LOCKOUT);

		return createInstance(className);
	}

	public static PropertiesProvider getPropertiesProvider() {
		// cannot use JiveGlobals to read this, as JiveGlobals uses this
		// provider to read all other properties (it would be a chicken and egg
		// situation)
		String className = System.getProperty(Provider.PROPERTIES.propertyName(), Provider.PROPERTIES.defaultClassName());

		return createInstance(className);
	}

	public static ConnectionManagerWrapper getConnectionManagerWrapper() {
		if (wrapper == null) {
			synchronized (new byte[0]) {
				String className = JiveGlobals.getProperty(
						Provider.CONNECTION_MGR.propertyName(), Provider.CONNECTION_MGR.defaultClassName());

				wrapper = createInstance(className);
				// migrate property needs access to the wrapper, so we can't
				// migrate before we have a wrapper handy
				JiveGlobals.migrateProperty(className);
			}
		}

		return wrapper;
	}

	private static String loadProviderClassName(Provider p) {
		JiveGlobals.migrateProperty(p.propertyName());
		String className = JiveGlobals.getProperty(p.propertyName(), p.defaultClassName());

		return className;
	}

	private static <T> T createInstance(String className) {
		Class<T> clazz;
		T provider = null;

		try {
			clazz = ClassUtils.forName(className);
			provider = clazz.newInstance();
		} catch (Exception e) {
			log.error("Error loading provider class {}", className, e);
		}

		return provider;
	}
}
