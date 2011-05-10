/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.workbench.event;

import info.magnolia.ui.framework.event.Event;
import info.magnolia.ui.framework.event.EventHandler;


/**
 * Global event fired if content was changed, deleted, added.
 * FIXME introduce more granular events
 */
public class ContentChangedEvent implements Event<ContentChangedEvent.Handler> {

    /**
     * Handles {@link ContentChangedEvent} events.
     */
    public static interface Handler extends EventHandler {
        void onContentChanged(ContentChangedEvent event);
    }

    private String workspace;

    private String path;

    @Override
    public void dispatch(Handler handler) {
        handler.onContentChanged(this);
    }

    public ContentChangedEvent(String workspace, String path) {
        this.workspace = workspace;
        this.path = path;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getPath() {
        return path;
    }
}
