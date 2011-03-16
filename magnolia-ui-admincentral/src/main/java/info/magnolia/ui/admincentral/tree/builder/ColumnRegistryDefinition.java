/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.admincentral.tree.builder;

/**
 * Defines a ColumnRegistry entry for the TreeBuilder.
 *
 * @author dlipp
 *
 * TODO: check whether String to Class<?> mapping can directly be done via Content2Bean.
 */
public class ColumnRegistryDefinition {
    /**
     * Class-name to map from.
     */
    private String from;

    /**
     * Class-name to map from.
     */
    private Class<?> fromClass;
    /**
     * Class-name to map to.
     */
    private String to;

    /**
     * Class-name to map to.
     */
    private Class<?> toClass;

    public void setFrom(String from) throws ClassNotFoundException {
        this.from = from;
        this.fromClass = Class.forName(from);
    }

    public String getFrom() {
        return from;
    }

    public void setTo(String to) throws ClassNotFoundException {
        this.to = to;
        this.toClass = Class.forName(to);
    }

    public String getTo() {
        return to;
    }

    public Class<?> getFromClass() {
        return this.fromClass;
    }

    public Class<?> getToClass() {
        return this.toClass;
    }
}
