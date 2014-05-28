/**
 * $RCSfile$
 * $Revision$
 * $Date$
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

package org.jivesoftware.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.openfire.provider.ProviderFactory;
import org.jivesoftware.openfire.provider.UIDProvider;
import org.jivesoftware.util.JiveConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages sequences of unique ID's that get stored in the database. Database support for sequences
 * varies widely; some don't use them at all. Instead, we handle unique ID generation with a
 * combination VM/database solution.
 * <p>
 * A special table in the database doles out blocks of unique ID's to each
 * virtual machine that interacts with Jive. This has the following consequences:</p>
 * <ul>
 * <li>There is no need to go to the database every time we want a new unique id.
 * <li>Multiple app servers can interact with the same db without id collision.
 * <li>The order of unique id's may not correspond to the creation date of objects.
 * <li>There can be gaps in ID's after server restarts since blocks will get "lost" if the block
 * size is greater than 1.
 * </ul>
 * Each sequence type that this class manages has a different block size value. Objects that aren't
 * created often have a block size of 1, while frequently created objects such as entries and
 * comments have larger block sizes.
 *
 * @author Matt Tucker
 * @author Bruce Ritchie
 */
public class SequenceManager {

	private static final Logger Log = LoggerFactory.getLogger(SequenceManager.class);

    // Statically startup a sequence manager for each of the sequence counters.
    private static Map<Integer, SequenceManager> managers = new ConcurrentHashMap<Integer, SequenceManager>();

    /**
     * Provider for underlying storage
     */
    private final UIDProvider provider = ProviderFactory.getUIDProvider();

    static {
        new SequenceManager(JiveConstants.ROSTER, 5);
        new SequenceManager(JiveConstants.OFFLINE, 1);
        new SequenceManager(JiveConstants.MUC_ROOM, 1);
    }

    /**
     * Returns the next ID of the specified type.
     *
     * @param type the type of unique ID.
     * @return the next unique ID of the specified type.
     */
    public static long nextID(int type) {
        if (managers.containsKey(type)) {
            return managers.get(type).nextUniqueID();
        }
        else {
            // Verify type is valid from the db, if so create an instance for the type
            // And return the next unique id
            SequenceManager manager = new SequenceManager(type, 1);
            return manager.nextUniqueID();
        }
    }

    /**
     * Returns the next id for an object that has defined the annotation {@link JiveID}.
     * The JiveID annotation value is the synonymous for the type integer.<p>
     *
     * The annotation JiveID should contain the id type for the object (the same number you would
     * use to call nextID(int type)). Example class definition:</p>
     * <code>
     * \@JiveID(10)
     * public class MyClass {
     *
     * }
     * </code>
     *
     * @param o object that has annotation JiveID.
     * @return the next unique ID.
     * @throws IllegalArgumentException If the object passed in does not defined {@link JiveID}
     */
    public static long nextID(Object o) {
        JiveID id = o.getClass().getAnnotation(JiveID.class);

        if (id == null) {
            Log.error("Annotation JiveID must be defined in the class " + o.getClass());
            throw new IllegalArgumentException(
                    "Annotation JiveID must be defined in the class " + o.getClass());
        }

        return nextID(id.value());
    }

    /**
     * Used to set the blocksize of a given SequenceManager. If no SequenceManager has
     * been registered for the type, the type is verified as valid and then a new
     * sequence manager is created.
     *
     * @param type the type of unique id.
     * @param blockSize how many blocks of ids we should.
     */
    public static void setBlockSize(int type, int blockSize) {
        if (managers.containsKey(type)) {
            managers.get(type).blockSize = blockSize;
        }
        else {
            new SequenceManager(type, blockSize);
        }
    }

    private int type;
    private long currentID;
    private long maxID;
    private int blockSize;

    /**
     * Creates a new DbSequenceManager.
     *
     * @param seqType the type of sequence.
     * @param size the number of id's to "checkout" at a time.
     */
    public SequenceManager(int seqType, int size) {
        managers.put(seqType, this);
        this.type = seqType;
        this.blockSize = size;
        currentID = 0l;
        maxID = 0l;
    }

    /**
     * Returns the next available unique ID. Essentially this provides for the functionality of an
     * auto-increment database field.
     */
    public synchronized long nextUniqueID() {
        if (!(currentID < maxID)) {
            // Get next block -- make 5 attempts at maximum.
            getNextBlock(5);
        }
        long id = currentID;
        currentID++;
        return id;
    }

    /**
     * Performs a lookup to get the next available ID block from UID provider. The algorithm is implemented by each provider, e.g. for SQL:
     * <ol>
     * <li> Select currentID from appropriate db row.
     * <li> Increment id returned from db.
     * <li> Update db row with new id where id=old_id.
     * <li> If update fails another process checked out the block first; go back to step 1.
     * Otherwise, done.
     * </ol>
     */
    private void getNextBlock(int count) {
    	long[] ids = provider.getNextBlock(type, blockSize);
    	if (ids != null) {
    		currentID = ids[0];
    		maxID = ids[1];
    	} else {
            Log.error("WARNING: failed to obtain next ID block due to " +
                    "thread contention. Trying again...");
            // Call this method again, but sleep briefly to try to avoid thread contention.
            try {
                Thread.sleep(75);
            }
            catch (InterruptedException ie) {
                // Ignore.
            }
            getNextBlock(count - 1);
        }
    }
}