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

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import info.magnolia.templating.editor.client.dom.CMSBoundary;

/**
 * Helper class to keep tack on selected items.
 */
public class VisibilityHelper {

    private static VisibilityHelper helper;
    private CMSBoundary boundary;
    private AbstractBarWidget widget;
    private List<CMSBoundary> rootBoundaries = new LinkedList<CMSBoundary>();

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

        hideRoot();
        if (getBoundary() != null) {
            deSelect(getBoundary());
        }
        select(boundary);

        setBoundary(boundary);
    }

    protected void select(CMSBoundary boundary) {

        if (boundary != null) {

                if (boundary.getOverlayWidget() != null) {
                    //boundary.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                    boundary.getOverlayWidget().setVisible(false);
                }
                if (boundary.getEdit() != null) {
                    if (boundary.getEdit().getWidget() != null) {
                        //boundary.getEdit().getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                        boundary.getEdit().getWidget().setVisible(true);
                    }
                }

/*                for (CMSBoundary area : boundary.getAreas()) {
                    if (area.getEdit() != null) {
                        area.getEdit().getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                    }
                }*/

                for (CMSBoundary component : boundary.getComponents()) {
                    if (component.getEdit() != null) {
                        //component.getEdit().getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                        component.getEdit().getWidget().setVisible(true);
                    }
                    if (component.getOverlayWidget() != null) {
                        //component.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                        component.getOverlayWidget().setVisible(false);
                    }

                    /*
                    for (CMSBoundary area : component.getAreas()) {
                        if (area.getEdit() != null) {
                            //area.getEdit().getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                            area.getEdit().getWidget().setVisible(true);
                        }
                        if (area.getOverlayWidget() != null)
                            area.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                    }
*/
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
                for (CMSBoundary parentArea = boundary.getParentArea(); parentArea != null; parentArea= parentArea.getParentArea()) {
                    if (parentArea.getOverlayWidget() != null) {
                        parentArea.getOverlayWidget().setVisible(false);

                    }
                }
                computeOverlay();

           }


    }

    public void deSelect() {
        if (getBoundary() != null) {
            deSelect(getBoundary());
        }
    }

    public void deSelect(CMSBoundary boundary) {
        boundary = boundary.getRoot();
        if (boundary != null) {

            if (boundary.getOverlayWidget() != null) {
                //boundary.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                boundary.getOverlayWidget().setVisible(true);

            }
            for (CMSBoundary descendant : boundary.getDescendants()) {
                if (descendant.getOverlayWidget() != null) {
                    //descendant.getOverlayWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                    descendant.getOverlayWidget().setVisible(true);

                }
                if (descendant.getWidget() != null) {
                    if (!descendant.getParentBoundary().equals(boundary)) {
                        //descendant.getWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                        descendant.getWidget().setVisible(false);
                    }

                }

            }
            computeOverlay();

        }
    }

    public void showRoot() {
        for (CMSBoundary root : rootBoundaries) {
            if (root.getEdit() != null && root.getEdit().getWidget() != null) {
                //root.getEdit().getWidget().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                root.getEdit().getWidget().setVisible(true);

            }
        }
    }
    public void hideRoot() {
        for (CMSBoundary root : rootBoundaries) {
            if (root.getEdit() != null && root.getEdit().getWidget() != null) {
                //root.getEdit().getWidget().getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                root.getEdit().getWidget().setVisible(false);

            }
        }
    }

    public void addRoot(CMSBoundary boundary) {
        this.rootBoundaries.add(boundary);
    }

    public void computeOverlay () {
        for (CMSBoundary root : rootBoundaries) {
            List<CMSBoundary> boundaries = root.getDescendants();
            boundaries.add(root);
            for (CMSBoundary boundary : boundaries) {

                if (boundary.getOverlayWidget() == null) {
                    continue;
                }

                Element firstElement = boundary.getFirstElement();
                if (firstElement == null) {
                    continue;
                }
                boundary.getOverlayWidget().getElement().getStyle().setTop(firstElement.getAbsoluteTop(), Unit.PX);
                boundary.getOverlayWidget().getElement().getStyle().setLeft(firstElement.getAbsoluteLeft(), Unit.PX);
                boundary.getOverlayWidget().getElement().getStyle().setWidth(firstElement.getAbsoluteRight() - firstElement.getAbsoluteLeft(), Unit.PX);

                Element lastElement = boundary.getLastElement();
                if (lastElement != null) {
                    boundary.getOverlayWidget().getElement().getStyle().setHeight(lastElement.getAbsoluteBottom() - boundary.getOverlayWidget().getTop(), Unit.PX);
                }
            }
        }
    }
}
