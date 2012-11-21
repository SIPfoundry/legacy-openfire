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

import org.jivesoftware.database.DefaultUIDProvider;
import org.jivesoftware.database.SQLConnectionMgrWrapper;
import org.jivesoftware.openfire.DefaultConnectivityProvider;
import org.jivesoftware.openfire.DefaultOfflineMessageProvider;
import org.jivesoftware.openfire.DefaultPrivateStorageProvider;
import org.jivesoftware.openfire.auth.DefaultAuthProvider;
import org.jivesoftware.openfire.component.DefaultExternalComponentProvider;
import org.jivesoftware.openfire.group.DefaultGroupPropertiesProvider;
import org.jivesoftware.openfire.group.DefaultGroupProvider;
import org.jivesoftware.openfire.lockout.DefaultLockOutProvider;
import org.jivesoftware.openfire.muc.DefaultMultiUserChatProvider;
import org.jivesoftware.openfire.privacy.DefaultPrivacyListProvider;
import org.jivesoftware.openfire.pubsub.PubSubPersistenceManager;
import org.jivesoftware.openfire.roster.DefaultRosterItemProvider;
import org.jivesoftware.openfire.security.DefaultSecurityAuditProvider;
import org.jivesoftware.openfire.server.DefaultRemoteServerProvider;
import org.jivesoftware.openfire.spi.DefaultPresenceProvider;
import org.jivesoftware.openfire.user.DefaultUserPropertiesProvider;
import org.jivesoftware.openfire.user.DefaultUserProvider;
import org.jivesoftware.openfire.vcard.DefaultVCardProvider;
import org.jivesoftware.util.DefaultPropertiesProvider;

/**
 * Each provider has a property used to configure it and a default
 * implementation. This enum is used to avoid declaring a lot of strings within
 * other classes.
 *
 * @author Alex Mateescu
 *
 */
public enum Provider {
	USER,				// @see org.jivesoftware.openfire.provider.UserProvider
	USER_PROPS,			// @see org.jivesoftware.openfire.provider.UserPropertiesProvider
	GROUP,				// @see org.jivesoftware.openfire.provider.GroupProvider
	GROUP_PROPS,		// @see org.jivesoftware.openfire.provider.GroupPropertiesProvider
	ROSTER,				// @see org.jivesoftware.openfire.provider.RosterItemProvider
	VCARD,				// @see org.jivesoftware.openfire.provider.VCardProvider
	PRESENCE,			// @see org.jivesoftware.openfire.provider.PresenceProvider
	MUC,				// @see org.jivesoftware.openfire.provider.MultiUserChatProvider
	PUBSUB,				// @see org.jivesoftware.openfire.provider.PubSubProvider
	OFFLINE,			// @see org.jivesoftware.openfire.provider.OfflineProvider
	PRIVACY,			// @see org.jivesoftware.openfire.provider.PrivacyListProvider
	PRIVATE_STORAGE,	// @see org.jivesoftware.openfire.provider.PrivateStorageProvider
	AUTH,				// @see org.jivesoftware.openfire.provider.AuthProvider
	SECURITY_AUDIT,		// @see org.jivesoftware.openfire.provider.SecurityAuditProvider
	REMOTE_SERVER,		// @see org.jivesoftware.openfire.provider.RemoteServerProvider
	EXTERNAL_COMPONENT,	// @see org.jivesoftware.openfire.provider.ExternalComponentProvider
	UID,				// @see org.jivesoftware.openfire.provider.UIDProvider
	CONNECTIVITY,		// @see org.jivesoftware.openfire.provider.ConnectivityProvider
	LOCKOUT,			// @see org.jivesoftware.openfire.provider.LockOutProvider
	PROPERTIES,			// @see org.jivesoftware.openfire.provider.PropertiesProvider
	CONNECTION_MGR,		// @see org.jivesoftware.openfire.provider.ConnectionManagerWrapper
	;

	/**
	 *
	 * @return string denoting the property used to configure this provider
	 */
	public String propertyName() {
		switch (this) {
		case USER:
			return "provider.user.className";
		case USER_PROPS:
			return "provider.userprops.className";
		case GROUP:
			return "provider.group.className";
		case GROUP_PROPS:
			return "provider.groupprops.className";
		case ROSTER:
			return "provider.roster.className";
		case VCARD:
			return "provider.vcard.className";
		case PRESENCE:
			return "provider.presence.className";
		case MUC:
			return "provider.muc.className";
		case PUBSUB:
			return "provider.pubsub.className";
		case OFFLINE:
			return "provider.offline.className";
		case PRIVACY:
			return "provider.privacylist.className";
		case PRIVATE_STORAGE:
			return "provider.privatestorage.className";
		case AUTH:
			return "provider.auth.className";
		case SECURITY_AUDIT:
			return "provider.securityAudit.className";
		case REMOTE_SERVER:
			return "provider.remoteServer.className";
		case EXTERNAL_COMPONENT:
			return "provider.externalComponent.className";
		case UID:
			return "provider.uid.className";
		case CONNECTIVITY:
			return "provider.connectivity.className";
		case LOCKOUT:
			return "provider.lockout.className";
		case PROPERTIES:
			return "provider.properties.className";
		case CONNECTION_MGR:
			return "provider.connectionmgr.className";
		default:
			return "";
		}
	}

	/**
	 *
	 * @return string denoting the default class name used by this provider
	 */
	public String defaultClassName() {
		switch (this) {
		case USER:
			return DefaultUserProvider.class.getName();
		case USER_PROPS:
			return DefaultUserPropertiesProvider.class.getName();
		case GROUP:
			return DefaultGroupProvider.class.getName();
		case GROUP_PROPS:
			return DefaultGroupPropertiesProvider.class.getName();
		case ROSTER:
			return DefaultRosterItemProvider.class.getName();
		case VCARD:
			return DefaultVCardProvider.class.getName();
		case PRESENCE:
			return DefaultPresenceProvider.class.getName();
		case MUC:
			return DefaultMultiUserChatProvider.class.getName();
		case PUBSUB:
			return PubSubPersistenceManager.class.getName();
		case OFFLINE:
			return DefaultOfflineMessageProvider.class.getName();
		case PRIVACY:
			return DefaultPrivacyListProvider.class.getName();
		case PRIVATE_STORAGE:
			return DefaultPrivateStorageProvider.class.getName();
		case AUTH:
			return DefaultAuthProvider.class.getName();
		case SECURITY_AUDIT:
			return DefaultSecurityAuditProvider.class.getName();
		case REMOTE_SERVER:
			return DefaultRemoteServerProvider.class.getName();
		case EXTERNAL_COMPONENT:
			return DefaultExternalComponentProvider.class.getName();
		case UID:
			return DefaultUIDProvider.class.getName();
		case CONNECTIVITY:
			return DefaultConnectivityProvider.class.getName();
		case LOCKOUT:
			return DefaultLockOutProvider.class.getName();
		case PROPERTIES:
			return DefaultPropertiesProvider.class.getName();
		case CONNECTION_MGR:
			return SQLConnectionMgrWrapper.class.getName();
		default:
			return "";
		}
	}
}
