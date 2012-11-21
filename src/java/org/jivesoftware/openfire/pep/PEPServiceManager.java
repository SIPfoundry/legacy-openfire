/**
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
package org.jivesoftware.openfire.pep;

import java.util.concurrent.locks.Lock;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.provider.ProviderFactory;
import org.jivesoftware.openfire.provider.PubSubProvider;
import org.jivesoftware.openfire.pubsub.CollectionNode;
import org.jivesoftware.openfire.pubsub.Node;
import org.jivesoftware.openfire.pubsub.PubSubEngine;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

/**
 * Manages the creation, persistence and removal of {@link PEPService}
 * instances.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 *
 */
public class PEPServiceManager {

	public static final Logger Log = LoggerFactory
			.getLogger(PEPServiceManager.class);

	/**
	 * Cache of PEP services. Table, Key: bare JID (String); Value: PEPService
	 */
	private final Cache<String, PEPService> pepServices = CacheFactory
			.createCache("PEPServiceManager");

	private PubSubEngine pubSubEngine = null;

    /**
     * Provider for underlying storage
     */
    private final PubSubProvider provider = ProviderFactory.getPubsubProvider();

    /**
	 * Retrieves a PEP service -- attempting first from memory, then from the
	 * database.
	 *
	 * @param jid
	 *            the bare JID of the user that owns the PEP service.
	 * @return the requested PEP service if found or null if not found.
	 */
	public PEPService getPEPService(String jid) {
		PEPService pepService = null;

		final Lock lock = CacheFactory.getLock(jid, pepServices);
		try {
			lock.lock();
			// lookup in cache
			pepService = pepServices.get(jid);
			if (pepService == null) {
				// lookup in database.
				pepService = loadPEPServiceFromDB(jid);
			}
		} finally {
			lock.unlock();
		}

		return pepService;
	}

	public PEPService create(JID owner) {
		// Return an error if the packet is from an anonymous, unregistered user
		// or remote user
		if (!XMPPServer.getInstance().isLocal(owner)
				|| !UserManager.getInstance().isRegisteredUser(owner.getNode())) {
			throw new IllegalArgumentException(
					"Request must be initiated by a local, registered user, but is not: "
							+ owner);
		}

		PEPService pepService = null;
		final String bareJID = owner.toBareJID();
		final Lock lock = CacheFactory.getLock(owner, pepServices);
		try {
			lock.lock();

			pepService = pepServices.get(bareJID);
			if (pepService == null) {
				pepService = new PEPService(XMPPServer.getInstance(), bareJID);
				pepServices.put(bareJID, pepService);
				pubSubEngine.start(pepService);

				if (Log.isDebugEnabled()) {
					Log.debug("PEPService created for : " + bareJID);
				}
			}
		} finally {
			lock.unlock();
		}

		return pepService;
	}

	/**
	 * Deletes the {@link PEPService} belonging to the specified owner.
	 *
	 * @param owner
	 *            The JID of the owner of the service to be deleted.
	 */
	public void delete(JID owner) {
		PEPService service = null;

		final Lock lock = CacheFactory.getLock(owner, pepServices);
		try {
			lock.lock();
			service = pepServices.remove(owner.toBareJID());
			stop(service);
			if (Log.isDebugEnabled()) {
				Log.debug("PEP: Deleted PEPService. Number left="
						+ Integer.toString(pepServices.size()));
			}
		} finally {
			lock.unlock();
		}

		if (service == null) {
			return;
		}

		// Delete the user's PEP nodes from memory and the database.
		CollectionNode rootNode = service.getRootCollectionNode();
		for (final Node node : service.getNodes()) {
			if (rootNode.isChildNode(node)) {
				node.delete();
			}
		}
		rootNode.delete();
	}

	/**
	 * Removes the {@link PEPService} belonging to the specified owner from
	 * memory.
	 *
	 * @param owner
	 *            The JID of the owner of the service to be removed.
	 */
	public void remove(JID owner) {
		PEPService service = null;

		final Lock lock = CacheFactory.getLock(owner, pepServices);
		try {
			lock.lock();
			service = pepServices.remove(owner.toBareJID());
			stop(service);
			if (Log.isDebugEnabled()) {
				Log.debug("PEP: Removed PEPService. Number left="
						+ Integer.toString(pepServices.size()));
			}
		} finally {
			lock.unlock();
		}

		if (service == null) {
			return;
		}
	}

	public void start(PEPService pepService) {
		pubSubEngine.start(pepService);
	}

	public void start() {
		pubSubEngine = new PubSubEngine(XMPPServer.getInstance()
				.getPacketRouter());
	}

	public void stop() {

		for (PEPService service : pepServices.values()) {
			pubSubEngine.shutdown(service);
		}

		pubSubEngine = null;
	}

	public void stop(PEPService pepService) {
		pubSubEngine.shutdown(pepService);
	}

	public void process(PEPService service, IQ iq) {
		pubSubEngine.process(service, iq);
	}

	public boolean hasCachedService(JID owner) {
		return pepServices.get(owner) != null;
	}

	private PEPService loadPEPServiceFromDB(String jid) {
		PEPService pepService = null;

		String serviceName = provider.loadPEPServiceFromDB(jid);
		if (serviceName != null) {
			// Create a new PEPService
			pepService = new PEPService(XMPPServer.getInstance(), serviceName);
			pepServices.put(serviceName, pepService);
			start(pepService);
		}

		return pepService;
	}
}
