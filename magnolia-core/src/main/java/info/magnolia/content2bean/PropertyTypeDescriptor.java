/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.content2bean;

import java.lang.reflect.Method;

/**
 * Describes a property. Type, type used for ceating concrete instances, is it a map/collection, adder method, ...
 * @author philipp
 * @version $Id$
 *
 */
public class PropertyTypeDescriptor {
    private String name;
    private TypeDescriptor type;
    private TypeDescriptor collectionEntryType;
    private TypeDescriptor collectionKeyType;
    private Method addMethod;

    public Method getAddMethod() {
        return this.addMethod;
    }

    public void setAddMethod(Method addMethod) {
        this.addMethod = addMethod;
    }

    public TypeDescriptor getCollectionEntryType() {
        return this.collectionEntryType;
    }

    public void setCollectionEntryType(TypeDescriptor collectionEntryType) {
        this.collectionEntryType = collectionEntryType;
    }

    public TypeDescriptor getCollectionKeyType() {
        return this.collectionKeyType;
    }

    public void setCollectionKeyType(TypeDescriptor collectionKeyType) {
        this.collectionKeyType = collectionKeyType;
    }

    public boolean isCollection() {
        return getType().isCollection();
    }

    public boolean isMap() {
        return getType().isMap();
    }

    public TypeDescriptor getType() {
        return this.type;
    }

    public void setType(TypeDescriptor type) {
        this.type = type;
    }


    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }
}
