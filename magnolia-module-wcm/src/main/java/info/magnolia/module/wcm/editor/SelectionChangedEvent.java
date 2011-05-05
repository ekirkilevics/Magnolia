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
package info.magnolia.module.wcm.editor;

import info.magnolia.ui.framework.event.Event;

/**
 * Event fired when selection in page editor changes.
 */
public class SelectionChangedEvent implements Event<SelectionChangedHandler> {

    private String type;
    private String workspace;
    private String path;
    private String collectionName;
    private String nodeName;

    public SelectionChangedEvent(String type, String workspace, String path, String collectionName, String nodeName) {
        this.type = type;
        this.workspace = workspace;
        this.path = path;
        this.collectionName = collectionName;
        this.nodeName = nodeName;
    }

    public String getType() {
        return type;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getPath() {
        return path;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getNodeName() {
        return nodeName;
    }

    @Override
    public void dispatch(SelectionChangedHandler handler) {
        handler.onSelectionChanged(this);
    }
}
