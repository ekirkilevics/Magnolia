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
import info.magnolia.templating.editor.client.AreaBarWidget;
import info.magnolia.templating.editor.client.EditBarWidget;

import java.util.LinkedList;

/**
 * CMSBoundaryTag.
 */
public class CMSBoundary {

    private LinkedList<AbstractBarWidget> areaWidgets = new LinkedList<AbstractBarWidget>();
    private LinkedList<AbstractBarWidget> componentWidgets = new LinkedList<AbstractBarWidget>();
    private CMSComment comment;
    private CMSBoundary parentBoundary;
    private LinkedList<CMSBoundary> childBoundaries = new LinkedList<CMSBoundary>();

    public CMSBoundary(CMSComment comment, CMSBoundary parentBoundary) {
        this.comment = comment;
        this.setParentBoundary(parentBoundary);
    }

    public void addAreaWidget(AreaBarWidget widget) {
        areaWidgets.add(widget);
    }

    public void addComponentWidget(EditBarWidget widget) {
        componentWidgets.add(widget);
    }

    public String getContent() {
        return this.comment.getAttribute("cms:content");
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

    public LinkedList<CMSBoundary> getSiblings() {
        LinkedList<CMSBoundary> siblings = new LinkedList<CMSBoundary>();
        if (this.parentBoundary != null) {
            siblings.addAll(this.parentBoundary.getChildBoundaries());
        }
        return siblings;
    }

    public LinkedList<AbstractBarWidget> getAreaWidgets() {
        return areaWidgets;
    }

    public LinkedList<AbstractBarWidget> getComponentWidgets() {
        return componentWidgets;
    }
}
