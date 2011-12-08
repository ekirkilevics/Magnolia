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

import info.magnolia.templating.editor.client.dom.CMSBoundary;

/**
 * Helper class to keep tack on selected items.
 */
public class VisibilityHelper {

    private static VisibilityHelper helper;
    private CMSBoundary boundary;
    private AbstractBarWidget widget;

    public static VisibilityHelper getInstance() {
        if (helper == null) {
            helper = new VisibilityHelper();
        }
        return helper;
    }

    public void setBoundary(CMSBoundary boundary) {
        this.boundary = boundary;
    }

    public CMSBoundary getBoundary() {
        return boundary;
    }

    public void setWidget(AbstractBarWidget widget) {
        this.widget = widget;
    }

    public AbstractBarWidget getWidget() {
        return widget;
    }

    public void toggleVisibility (CMSBoundary boundary) {

        if (getBoundary() != null && !boundary.isRelated(getBoundary())) {
            deSelect(getBoundary().getRoot());
        }
        select(boundary);
        setBoundary(boundary);
    }

    protected void select(CMSBoundary boundary) {

        if (boundary != null) {

                if (boundary.getOverlayWidget() != null) {
                    boundary.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                }

                for (CMSBoundary area : boundary.getAreas()) {
                    if (area.getEdit() != null) {
                        area.getEdit().getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                    }
                    if (area.getOverlayWidget() != null)
                        area.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                }

                for (CMSBoundary component : boundary.getComponents()) {
                    if (component.getEdit() != null) {
                        component.getEdit().getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                    }
                    if (component.getOverlayWidget() != null)
                        component.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);

                    for (CMSBoundary area : component.getAreas()) {
                        if (area.getEdit() != null) {
                            area.getEdit().getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                        }
                        if (area.getOverlayWidget() != null)
                            area.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                    }
                }
                /*
                for (CMSBoundary child : boundary.getChildBoundaries()) {
                    if (child.getOverlayWidget() != null) {
                        child.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                    }
                    if (child.getWidget() != null) {
                        child.getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                    }
                    for (CMSBoundary grandChild : child.getChildBoundaries()) {
                        if (grandChild.getOverlayWidget() != null) {
                            grandChild.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                        }
                        if (grandChild.getWidget() != null) {
                            grandChild.getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                        }
                    }
                }*/


        }

    }

    protected void deSelect(CMSBoundary boundary) {
        if (boundary!= null) {

            if (boundary.getOverlayWidget() != null) {
                boundary.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
            }
            for (CMSBoundary descendant : boundary.getDescendants()) {
                if (descendant.getOverlayWidget() != null) {
                    descendant.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                }
                if (descendant.getWidget() != null) {
                    if (!descendant.getParentBoundary().equals(boundary))
                        descendant.getWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                }

            }

        }
    }
}
