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

import java.util.List;

import org.jivesoftware.openfire.pubsub.DefaultNodeConfiguration;
import org.jivesoftware.openfire.pubsub.LeafNode;
import org.jivesoftware.openfire.pubsub.Node;
import org.jivesoftware.openfire.pubsub.NodeAffiliate;
import org.jivesoftware.openfire.pubsub.NodeSubscription;
import org.jivesoftware.openfire.pubsub.PubSubService;
import org.jivesoftware.openfire.pubsub.PublishedItem;

/**
 * Provider that facilitates access to the underlying pubsub storage.
 * 
 * @author Alex Mateescu
 * 
 */
public interface PubSubProvider {

	/**
	 * Creates and stores the node configuration in the database.
	 * 
	 * @param service
	 *            The pubsub service that is hosting the node.
	 * @param node
	 *            The newly created node.
	 */
	void createNode(Node node);

	/**
	 * Updates the node configuration in the database.
	 * 
	 * @param service
	 *            The pubsub service that is hosting the node.
	 * @param node
	 *            The updated node.
	 */
	void updateNode(Node node);

	/**
	 * Removes the specified node from the DB.
	 * 
	 * @param service
	 *            The pubsub service that is hosting the node.
	 * @param node
	 *            The node that is being deleted.
	 * @return true If the operation was successful.
	 */
	boolean removeNode(Node node);

	/**
	 * Loads all nodes from the database and adds them to the PubSub service.
	 * 
	 * @param service
	 *            the pubsub service that is hosting the nodes.
	 */
	void loadNodes(PubSubService service);

	void loadNode(PubSubService service, String nodeId);

	/**
	 * Update the DB with the new affiliation of the user in the node.
	 * 
	 * @param service
	 *            The pubsub service that is hosting the node.
	 * @param node
	 *            The node where the affiliation of the user was updated.
	 * @param affiliate
	 *            The new affiliation of the user in the node.
	 * @param create
	 *            True if this is a new affiliate.
	 */
	void saveAffiliation(Node node, NodeAffiliate affiliate, boolean create);

	/**
	 * Removes the affiliation and subsription state of the user from the DB.
	 * 
	 * @param service
	 *            The pubsub service that is hosting the node.
	 * @param node
	 *            The node where the affiliation of the user was updated.
	 * @param affiliate
	 *            The existing affiliation and subsription state of the user in
	 *            the node.
	 */
	void removeAffiliation(Node node, NodeAffiliate affiliate);

	void loadSubscription(Node node, String subId);

	/**
	 * Updates the DB with the new subsription of the user to the node.
	 * 
	 * @param service
	 *            The pubsub service that is hosting the node.
	 * @param node
	 *            The node where the user has subscribed to.
	 * @param subscription
	 *            The new subscription of the user to the node.
	 * @param create
	 *            True if this is a new affiliate.
	 */
	void saveSubscription(Node node, NodeSubscription subscription,
			boolean create);

	/**
	 * Removes the subscription of the user from the DB.
	 * 
	 * @param service
	 *            The pubsub service that is hosting the node.
	 * @param node
	 *            The node where the user was subscribed to.
	 * @param subscription
	 *            The existing subsription of the user to the node.
	 */
	void removeSubscription(NodeSubscription subscription);

	/**
	 * Loads and adds the published items to the specified node.
	 * 
	 * @param service
	 *            the pubsub service that is hosting the node.
	 * @param node
	 *            the leaf node to load its published items.
	 */
	// void loadItems(PubSubService service, LeafNode node);

	/**
	 * Creates and stores the published item in the database.
	 * 
	 * @param service
	 *            the pubsub service that is hosting the node.
	 * @param item
	 *            The published item to save.
	 * @return true if the item was successfully saved to the database.
	 */
	// boolean createPublishedItem(PubSubService service,
	// PublishedItem item);
	PublishedItem getPublishedItem(LeafNode node, String itemID);

	List<PublishedItem> getPublishedItems(LeafNode node);

	List<PublishedItem> getPublishedItems(LeafNode node, int maxRows);

	PublishedItem getLastPublishedItem(LeafNode node);

	void removePublishedItem(PublishedItem item);

	void savePublishedItem(PublishedItem item);

	void flushPendingItems();

	void flushPendingItems(boolean sendToCluster);

	void purgeNode(LeafNode leafNode);

	/**
	 * Removes the specified published item from the DB.
	 * 
	 * @param service
	 *            the pubsub service that is hosting the node.
	 * @param item
	 *            The published item to delete.
	 * @return true if the item was successfully deleted from the database.
	 */
	// boolean removePublishedItem(PubSubService service,
	// PublishedItem item);

	/**
	 * Loads from the database the default node configuration for the specified
	 * node type and pubsub service.
	 * 
	 * @param service
	 *            the default node configuration used by this pubsub service.
	 * @param isLeafType
	 *            true if loading default configuration for leaf nodes.
	 * @return the loaded default node configuration for the specified node type
	 *         and service or <tt>null</tt> if none was found.
	 */
	DefaultNodeConfiguration loadDefaultConfiguration(PubSubService service,
			boolean isLeafType);

	/**
	 * Creates a new default node configuration for the specified service.
	 * 
	 * @param service
	 *            the default node configuration used by this pubsub service.
	 * @param config
	 *            the default node configuration to create in the database.
	 */
	void createDefaultConfiguration(PubSubService service,
			DefaultNodeConfiguration config);

	/**
	 * Updates the default node configuration for the specified service.
	 * 
	 * @param service
	 *            the default node configuration used by this pubsub service.
	 * @param config
	 *            the default node configuration to update in the database.
	 */
	void updateDefaultConfiguration(PubSubService service,
			DefaultNodeConfiguration config);

	/**
	 * Loads a PEP service from the database, if it exists.
	 * 
	 * @param jid
	 *            the JID of the owner of the PEP service.
	 * @return the loaded PEP service, or null if not found.
	 */
	String loadPEPServiceFromDB(String jid);

	void shutdown();
}
