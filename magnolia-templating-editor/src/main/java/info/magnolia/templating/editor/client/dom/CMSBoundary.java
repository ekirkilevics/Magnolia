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
package info.magnolia.templating.editor.client.dom;

import info.magnolia.templating.editor.client.AbstractBarWidget;
import info.magnolia.templating.editor.client.AbstractOverlayWidget;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.dom.client.Element;

/**
* CMSBoundary Constructor.
*
* @throws IllegalArgumentException if comments tagname is not a defined marker.
*/
public class CMSBoundary {

    private AbstractBarWidget widget;
    private CMSComment comment;
    private CMSBoundary parentBoundary;
    private boolean isArea = false;
    private boolean isComponent = false;

    private AbstractOverlayWidget overlayWidget;

    private Element firstElement;
    public Element getFirstElement() {
        return firstElement;
    }

    public void setFirstElement(Element firstElement) {
        this.firstElement = firstElement;
    }

    private LinkedList<CMSBoundary> childBoundaries = new LinkedList<CMSBoundary>();
    private boolean isEdit;

    public static final String MARKER_AREA = "cms:area";
    public static final String MARKER_COMPONENT = "cms:component";
    public static final String MARKER_EDIT = "cms:edit";

    public CMSBoundary(CMSComment comment, CMSBoundary parentBoundary) throws IllegalArgumentException {

        if (!isCmsBoundary(comment.getTagName())) {
            throw new IllegalArgumentException("The tagname must be one of the defined marker Strings.");
        }
        this.setComment(comment);
        this.setParentBoundary(parentBoundary);
    }

    public String getContent() {
        return this.getComment().getAttribute("cms:content");
    }

    public void setParentBoundary(CMSBoundary parentBoundary) {
        this.parentBoundary = parentBoundary;
    }

    public CMSBoundary getParentBoundary() {
        return parentBoundary;
    }

    public void setChildBoundaries(LinkedList<CMSBoundary> childBoundaries) {
        this.childBoundaries = childBoundaries;
    }

    public LinkedList<CMSBoundary> getChildBoundaries() {
        return childBoundaries;
    }

    public boolean hasChildBoundaries() {
        return (getChildBoundaries().size() > 0);
    }

    public List<CMSBoundary> getDescendants() {

        List<CMSBoundary> descendants = new LinkedList<CMSBoundary>();

        for (CMSBoundary boundary : getChildBoundaries()) {
            descendants.add(boundary);
            descendants.addAll(boundary.getDescendants());
        }
        return descendants;
    }

    public List<CMSBoundary> getAscendants() {
        List<CMSBoundary> ascendants = new LinkedList<CMSBoundary>();
        CMSBoundary parent = this.parentBoundary;
        while (parent != null) {
            ascendants.add(parent);
            parent = parent.getParentBoundary();
        }
        return ascendants;
    }

    public CMSBoundary getParentArea() {

        if (getParentBoundary() == null) return null;
        else if (getParentBoundary().isArea())
            return getParentBoundary();
        else
            return getParentBoundary().getParentArea();
    }

    public boolean isCmsBoundary(String tagName) {
        if (tagName.equals(MARKER_AREA)) {
            this.isArea = true;
            return true;
        }
        else if (tagName.equals(MARKER_COMPONENT)) {
            this.isComponent = true;
            return true;
        }
        else if (tagName.equals(MARKER_EDIT)) {
            this.isEdit = true;
            return true;
        }

        return false;
    }
    public boolean isArea() {
        return isArea;
    }
    public boolean isEdit() {
        return isEdit;
    }

    public boolean isComponent() {
        return isComponent;
    }
    public void setComment(CMSComment comment) {
        this.comment = comment;
    }

    public CMSComment getComment() {
        return comment;
    }

    public void setWidget(AbstractBarWidget widget) {
        this.widget = widget;
    }

    public AbstractBarWidget getWidget() {
        return widget;
    }

    public void setOverlayWidget(AbstractOverlayWidget overlayWidget) {
        this.overlayWidget = overlayWidget;
    }

    public AbstractOverlayWidget getOverlayWidget() {
        return overlayWidget;
    }

    public List<CMSBoundary> getComponents() {
        List<CMSBoundary> components = new LinkedList<CMSBoundary>();
        for (CMSBoundary boundary : getChildBoundaries()) {
            if (boundary.isComponent()) {
                components.add(boundary);
            }
        }
        return components;
    }
    public List<CMSBoundary> getAreas() {
        List<CMSBoundary> areas = new LinkedList<CMSBoundary>();
        for (CMSBoundary boundary : getChildBoundaries()) {
            if (boundary.isArea()) {
                areas.add(boundary);
            }
        }
        return areas;
    }
    public CMSBoundary getEdit() {
        CMSBoundary edit = null;
        for (CMSBoundary boundary : getChildBoundaries()) {
            if (boundary.isEdit()) {
                edit = boundary;
            }
        }
        return edit;
    }
    public CMSBoundary getRoot() {
        CMSBoundary root = null;
        for (CMSBoundary parent = this; parent != null; parent = parent.getParentBoundary()) {
            if (parent.getEdit() != null && parent.getEdit().getWidget() != null) {
                root = parent;
            }
        }
        return root;
    }

    public boolean isRelated(CMSBoundary relative) {

        if (relative != null && this.getRoot() == relative.getRoot()) {
                return true;
        }
        return false;
    }
}
