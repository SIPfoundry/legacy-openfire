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

package org.jivesoftware.util;

/**
 * A type safe enumeration object. Used for indicating distinct states
 * in a generic manner. Most child classes should extend Enum and
 * create static instances.
 *
 * @author Iain Shigeoka
 */
public class Enum {
    private String name;

    protected Enum(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the enum.
     *
     * @return the name of the enum.
     */
    public String getName() {
        return name;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        else if ((this.getClass().isInstance(object)) && name.equals(((Enum)object).name)) {
            return true;
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }
}
