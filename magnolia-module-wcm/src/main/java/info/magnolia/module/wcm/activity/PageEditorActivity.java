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
package info.magnolia.module.wcm.activity;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import info.magnolia.module.wcm.place.PageEditorPlace;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

/**
 * Activity for page editing.
 */
public class PageEditorActivity extends AbstractActivity {

    private PageEditorPlace place;

    public PageEditorActivity(PageEditorPlace place) {
        this.place = place;
    }

    public void start(ViewPort viewPort, EventBus eventBus) {
        viewPort.setView(new Apa(place.getPath()));
    }

    private static class Apa implements View, IsVaadinComponent {

        private String path;
        private Component component;

        private Apa(String path) {
            this.path = path;
            component = new Label("Editing " + path);
        }

        public Component asVaadinComponent() {
            return component;
        }
    }
}
