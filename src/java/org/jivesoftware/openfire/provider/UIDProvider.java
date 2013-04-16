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
package org.jivesoftware.openfire.provider;

/**
 * Provider that facilitates access to a UID generator.
 *
 * @author Alex Mateescu
 *
 */
public interface UIDProvider {

	/**
	 * Performs a lookup to get the next available ID block.
	 *
	 * @param type
	 *            The type for which a new ID is requested
	 * @param blockSize
	 *            Block size, a.k.a. the increment size for the next id
	 *
	 */
	long[] getNextBlock(int type, int blockSize);

}