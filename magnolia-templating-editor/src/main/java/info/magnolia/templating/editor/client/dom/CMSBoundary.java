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

import java.util.LinkedList;

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

    private Coordinate maxCoordinate = new Coordinate();
    private Coordinate minCoordinate = new Coordinate();

    private Element firstElement;
    public Element getFirstElement() {
        return firstElement;
    }

    public void setFirstElement(Element firstElement) {
        this.firstElement = firstElement;
    }

    public Coordinate getMaxCoordinate() {
        return maxCoordinate;
    }

    public Coordinate getMinCoordinate() {
        return minCoordinate;
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

    public LinkedList<CMSBoundary> getDescendants() {

        LinkedList<CMSBoundary> descendants = new LinkedList<CMSBoundary>();

        for (CMSBoundary boundary : getChildBoundaries()) {
            descendants.add(boundary);
            descendants.addAll(boundary.getDescendants());
        }
        return descendants;
    }

    public LinkedList<CMSBoundary> getAscendants() {
        LinkedList<CMSBoundary> ascendants = new LinkedList<CMSBoundary>();
        CMSBoundary parent = this.parentBoundary;
        while (parent != null) {
            ascendants.add(parent);
            parent = parent.getParentBoundary();
        }
        return ascendants;
    }

    public CMSBoundary getParentArea() {

        if (isArea()) {
            return this;
        }
        else if (getParentBoundary() != null) {
            return getParentBoundary().getParentArea();
        }

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

    /**
     * Coordinate.
     */
    public class Coordinate {
        private int left = 0;
        private int top = 0;

        public void setLeft(int left) {
            this.left = left;
        }
        public int getLeft() {
            return left;
        }
        public void setTop(int top) {
            this.top = top;
        }
        public int getTop() {
            return top;
        }

    }
}
