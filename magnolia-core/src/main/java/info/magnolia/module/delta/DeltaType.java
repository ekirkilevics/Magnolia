/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.delta;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DeltaType {
    public static final DeltaType install = new DeltaType("install");
    public static final DeltaType update = new DeltaType("update");
    public static final DeltaType uninstall = new DeltaType("uninstall");

    private final String name;

    private DeltaType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    // generated :
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeltaType deltaType = (DeltaType) o;

        if (!name.equals(deltaType.name)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
