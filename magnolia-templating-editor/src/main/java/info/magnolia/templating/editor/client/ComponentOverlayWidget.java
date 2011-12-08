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
package info.magnolia.templating.editor.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;


import info.magnolia.templating.editor.client.dom.CMSBoundary;


/**
 * Class for Area Overlay Widget.
 */
public class ComponentOverlayWidget extends AbstractOverlayWidget {

    public ComponentOverlayWidget(CMSBoundary boundary) {
        super(boundary);
        this.addStyleName("component");

        addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                //select();
            }
        }, MouseDownEvent.getType());

        addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                VisibilityHelper.getInstance().deSelect(VisibilityHelper.getInstance().getBoundary());

                // remove overlay of component
                getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);


                 CMSBoundary parentArea = getBoundary().getParentArea();
                 // get area of component and set its editbar visible
/*                for(CMSBoundary child : parentArea.getChildBoundaries()) {
                    if (child.getWidget() != null) {
                        child.getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                    }

                }*/
                // same?
                for(CMSBoundary child : getBoundary().getChildBoundaries()) {
                    if (child.getWidget() != null) {
                        child.getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                    }
                }

                for(CMSBoundary parent = getBoundary().getParentBoundary(); parent!= null; parent = parent.getParentBoundary()) {
                      for(CMSBoundary child : parent.getChildBoundaries()) {
                        if (child.getWidget() != null) {
                            child.getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                        }
                    }
                    // remove overlays of all parent areas and components (?)
                    if (parent.getOverlayWidget() != null) {
                        parent.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                    }
                    parentArea = parent;
                }
                VisibilityHelper.getInstance().setBoundary(getBoundary().getRoot());
            }
        }, MouseUpEvent.getType());

    }

}
