/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.genuinecentral.rest;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RestEndpointManager extends ObservedManager {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestEndpointManager.class);

    private List<String> endpointClasses = new ArrayList<String>();
    private RestEndpointRegistrar endpointRegistrar;

    @Override
    protected void onRegister(Content node) {
        Iterator it = node.getChildren(ItemType.CONTENTNODE).iterator();
        while (it.hasNext()) {
            Content x = (Content) it.next();
            NodeData nodeData = x.getNodeData("class");
            if (nodeData.isExist()) {
                String clazz = nodeData.getString();
                endpointClasses.add(clazz);
                if (endpointRegistrar != null) {
                    registerEndpoint(endpointRegistrar, clazz);
                }
            }
        }
    }

    @Override
    protected void onClear() {
        endpointClasses.clear();
        if (endpointRegistrar != null) {
            for (String endpointClass : endpointClasses) {
                unregisterEndpoint(endpointClass);
            }
        }
        endpointClasses.clear();
    }

    public void setEndpointRegistrar(RestEndpointRegistrar endpointRegistrar) {
        this.endpointRegistrar = endpointRegistrar;
        for (String endpointClass : endpointClasses) {
            registerEndpoint(endpointRegistrar, endpointClass);
        }
    }

    private void registerEndpoint(RestEndpointRegistrar endpointRegistrar, String endpointClass) {
        try {
            Class<?> clazz = Classes.getClassFactory().forName(endpointClass);
            Object instance = Components.getSingleton(clazz);
            endpointRegistrar.registerEndpoint(instance);
        } catch (ClassNotFoundException e) {
            log.error("REST Endpoint class not found, class=[" + endpointClass + "]", e);
        }
    }

    private void unregisterEndpoint(String endpointClass) {
        try {
            Class<?> clazz = Classes.getClassFactory().forName(endpointClass);
            endpointRegistrar.unregisterEndpoint(clazz);
        } catch (ClassNotFoundException e) {
            log.error("REST Endpoint class not found, class=[" + endpointClass + "]", e);
        }
    }

    public static RestEndpointManager getInstance() {
        return Components.getSingleton(RestEndpointManager.class);
    }
}
