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
package info.magnolia.ui.admincentral.embedded.activity;

import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.admincentral.embedded.place.EmbeddedPlace;
import info.magnolia.ui.admincentral.embedded.view.EmbeddedView;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.ViewPort;

import com.vaadin.terminal.ExternalResource;


/**
 * Shows a target page in an iframe.
 */
public class EmbeddedActivity extends AbstractActivity {

    private EmbeddedPlace place;

    public EmbeddedActivity(EmbeddedPlace place){
        this.place = place;
    }

    @Override
    public void start(ViewPort viewPort, EventBus eventBus) {
        String url;
        if(LinkUtil.isExternalLinkOrAnchor(place.getUrl())){
            url = place.getUrl();
        }
        else{
            url = MgnlContext.getContextPath() + place.getUrl();
        }
        final EmbeddedView view = Components.getComponentProvider().getComponent(EmbeddedView.class);
        view.setSource(new ExternalResource(url));
        viewPort.setView(view);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((place == null) ? 0 : place.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EmbeddedActivity other = (EmbeddedActivity) obj;
        if (place == null) {
            if (other.place != null) {
                return false;
            }
        } else if (!place.getUrl().equals(other.place.getUrl())) {
            return false;
        }
        return true;
    }

}