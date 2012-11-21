/**
 * $RCSfile$
 * $Revision: 1759 $
 * $Date: 2005-08-09 19:32:51 -0300 (Tue, 09 Aug 2005) $
 *
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

package org.jivesoftware.openfire;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.openfire.container.BasicModule;
import org.jivesoftware.openfire.event.UserEventDispatcher;
import org.jivesoftware.openfire.event.UserEventListener;
import org.jivesoftware.openfire.provider.PrivateStorageProvider;
import org.jivesoftware.openfire.provider.ProviderFactory;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Private storage for user accounts (JEP-0049). It is used by some XMPP systems
 * for saving client settings on the server.
 *
 * @author Iain Shigeoka
 */
public class PrivateStorage extends BasicModule implements UserEventListener {

	private static final Logger Log = LoggerFactory.getLogger(PrivateStorage.class);

    private static final int POOL_SIZE = 10;

    // Currently no delete supported, we can detect an add of an empty element and
    // use that to signal a delete but that optimization doesn't seem necessary.
    // private static final String DELETE_PRIVATE =
    //     "DELETE FROM ofPrivate WHERE userID=? AND name=? AND namespace=?";

    private boolean enabled = JiveGlobals.getBooleanProperty("xmpp.privateStorageEnabled", true);

    /**
     * Pool of SAX Readers. SAXReader is not thread safe so we need to have a pool of readers.
     */
    private BlockingQueue<SAXReader> xmlReaders = new LinkedBlockingQueue<SAXReader>(POOL_SIZE);

	/**
	 * Provider for underlying storage
	 */
	private final PrivateStorageProvider provider = ProviderFactory.getPrivateStorageProvider();

	/**
     * Constructs a new PrivateStore instance.
     */
    public PrivateStorage() {
        super("Private user data storage");
    }

    /**
     * Returns true if private storage is enabled.
     *
     * @return true if private storage is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether private storage is enabled.
     *
     * @param enabled true if this private store is enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        JiveGlobals.setProperty("xmpp.privateStorageEnabled", Boolean.toString(enabled));
    }

    /**
     * Stores private data. If the name and namespace of the element matches another
     * stored private data XML document, then replace it with the new one.
     *
     * @param data the data to store (XML element)
     * @param username the username of the account where private data is being stored
     */
    public void add(String username, Element data) {
        if (enabled) {
        	provider.add(username, data);
        }
    }

    /**
     * Returns the data stored under a key corresponding to the name and namespace
     * of the given element. The Element must be in the form:<p>
     *
     * <code>&lt;name xmlns='namespace'/&gt;</code><p>
     *
     * If no data is currently stored under the given key, an empty element will be
     * returned.
     *
     * @param data an XML document who's element name and namespace is used to
     *      match previously stored private data.
     * @param username the username of the account where private data is being stored.
     * @return the data stored under the given key or the data element.
     */
	public Element get(String username, Element data) {
		if (enabled) {
			SAXReader xmlReader = null;

			try {
				// Get a sax reader from the pool
				xmlReader = xmlReaders.take();
				provider.get(username, data, xmlReader);
			} catch (Exception e) {
				Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
			} finally {
				// Return the sax reader to the pool
				if (xmlReader != null) {
					xmlReaders.add(xmlReader);
				}
			}
		}
		return data;
	}

    public void userCreated(User user, Map<String,Object> params) {
        //Do nothing
    }

    public void userDeleting(User user, Map<String,Object> params) {
    	provider.userDeleting(user, params);
    }

    public void userModified(User user, Map<String,Object> params) {
        //Do nothing
    }

    @Override
	public void start() throws IllegalStateException {
        super.start();
        // Initialize the pool of sax readers
        for (int i=0; i<POOL_SIZE; i++) {
            SAXReader xmlReader = new SAXReader();
            xmlReader.setEncoding("UTF-8");
            xmlReaders.add(xmlReader);
        }
        // Add this module as a user event listener so we can delete
        // all user properties when a user is deleted
        UserEventDispatcher.addListener(this);
    }

    @Override
	public void stop() {
        super.stop();
        // Clean up the pool of sax readers
        xmlReaders.clear();
        // Remove this module as a user event listener
        UserEventDispatcher.removeListener(this);
    }
}