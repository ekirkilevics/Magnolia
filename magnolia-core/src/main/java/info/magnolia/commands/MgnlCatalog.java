/**
 * This file Copyright (c) 2003-2013 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.commands;

import info.magnolia.commands.chain.Command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Named command catalogs. Used to choose from multiple commands of the same
 * name. The commands from catalog with name matching the repository will be
 * used.
 * 
 * Replaces the functionality of the org.apache.commons.chain.CatalogBase class.
 * 
 */
public class MgnlCatalog {

    // ----------------------------------------------------- Instance Variables
    private String name;
    protected Map commands = Collections.synchronizedMap(new HashMap());

    // --------------------------------------------------------- Constructors

    public MgnlCatalog() {
    }

    public MgnlCatalog(Map commands) {
        this.commands = Collections.synchronizedMap(commands);
    }

    // --------------------------------------------------------- Public Methods
    public void addCommand(String name, Command command) {
        commands.put(name, command);
    }

    public Command getCommand(String name) {
        return ((Command) commands.get(name));
    }

    public Iterator getNames() {
        return (commands.keySet().iterator());
    }

    public String toString() {
        Iterator names = getNames();
        StringBuffer str = new StringBuffer("[" + this.getClass().getName() + ": ");
        while (names.hasNext()) {
            str.append(names.next());
            if (names.hasNext()) {
                str.append(", ");
            }
        }
        str.append("]");
        return str.toString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
