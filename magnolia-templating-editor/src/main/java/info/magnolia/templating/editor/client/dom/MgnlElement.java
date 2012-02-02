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

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.dom.client.Element;

/**
* MgnlElement Constructor.
*
*/
public class MgnlElement {

    private CMSComment comment;
    private MgnlElement parent;
    private boolean isArea = false;
    private boolean isComponent = false;

    private Element firstElement;
    private Element lastElement;

    private LinkedList<MgnlElement> children = new LinkedList<MgnlElement>();
    private Element componentElement;
    private Element areaElement;
    private Element editElement;

    private static final String MARKER_AREA = "cms:area";
    private static final String MARKER_COMPONENT = "cms:component";

/**
 * @throws IllegalArgumentException if comments tagname is not a defined marker.
 */
    public MgnlElement(CMSComment comment, MgnlElement parent) throws IllegalArgumentException {

        if (!isMgnlElement(comment.getTagName())) {
            throw new IllegalArgumentException("The tagname must be one of the defined marker Strings.");
        }
        this.comment = comment;
        this.parent = parent;
    }
    public boolean isMgnlElement(String tagName) {
        if (tagName.equals(MARKER_AREA)) {
            this.isArea = true;
            return true;
        }
        else if (tagName.equals(MARKER_COMPONENT)) {
            this.isComponent = true;
            return true;
        }

        return false;
    }

    public boolean isArea() {
        return isArea;
    }

    public boolean isComponent() {
        return isComponent;
    }

    public MgnlElement getParent() {
        return parent;
    }

    public void setParent(MgnlElement parent) {
        this.parent = parent;
    }

    public LinkedList<MgnlElement> getChildren() {
        return children;
    }

    public List<MgnlElement> getDescendants() {

        List<MgnlElement> descendants = new LinkedList<MgnlElement>();

        for (MgnlElement element : getChildren()) {
            descendants.add(element);
            descendants.addAll(element.getDescendants());
        }
        return descendants;
    }

    public List<MgnlElement> getAscendants() {
        List<MgnlElement> ascendants = new LinkedList<MgnlElement>();
        while (parent != null) {
            ascendants.add(parent);
            parent = parent.getParent();
        }
        return ascendants;
    }

    public MgnlElement getRootArea() {
        MgnlElement rootArea = null;
        for (MgnlElement parent = this; parent != null; parent = parent.getParent()) {
            if (parent.isArea()) {
                rootArea = parent;
            }
        }
        return rootArea;
    }

    public MgnlElement getParentArea() {
        MgnlElement parentArea = null;
        for (MgnlElement parent = this; parent != null; parent = parent.getParent()) {
            if (parent.isArea()) {
                parentArea = parent;
                break;
            }
        }
        return parentArea;
    }

    public CMSComment getComment() {
        return comment;
    }

    public List<MgnlElement> getComponents() {
        List<MgnlElement> components = new LinkedList<MgnlElement>();
        for (MgnlElement element : getChildren()) {
            if (element.isComponent()) {
                components.add(element);
            }
        }
        return components;
    }
    public List<MgnlElement> getAreas() {
        List<MgnlElement> areas = new LinkedList<MgnlElement>();
        for (MgnlElement element : getChildren()) {
            if (element.isArea()) {
                areas.add(element);
            }
        }
        return areas;
    }

    public MgnlElement getRoot() {
        MgnlElement root = null;
        for (MgnlElement parent = this; parent != null; parent = parent.getParent()) {
                root = parent;
        }
        return root;
    }

    public boolean isRelated(MgnlElement relative) {

        if (relative != null && this.getRoot() == relative.getRoot()) {
                return true;
        }
        return false;
    }

    public void delete() {
        for (MgnlElement child : getChildren()) {
            if (getParent() != null) {
                getParent().getChildren().add(child);
            }
            child.setParent(getParent());
        }
    }

    public Element getFirstElement() {
        return firstElement;
    }

    public void setFirstElement(Element firstElement) {
        this.firstElement = firstElement;
    }

    public Element getLastElement() {
        return lastElement;
    }

    public void setLastElement(Element lastElement) {
        this.lastElement = lastElement;
    }

    public void setComponentElement(Element componentElement) {
        this.componentElement = componentElement;
    }

    public void setAreaElement(Element areaElement) {
        this.areaElement = areaElement;
    }

    public void setEditElement(Element editElement) {
        this.editElement = editElement;
    }

    public Element getComponentElement() {
        return componentElement;
    }

    public Element getAreaElement() {
        return areaElement;
    }

    public Element getEditElement() {
        return editElement;
    }

}
