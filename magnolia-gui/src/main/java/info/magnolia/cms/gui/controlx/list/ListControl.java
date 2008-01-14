/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.gui.controlx.list;

import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.controlx.impl.AbstractControl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * A list. Can sort or group data.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ListControl extends AbstractControl {

    /**
     * The type used for rendering
     */
    public static final String RENDER_TYPE = "listControl";

    /**
     * The current itarotor.
     */
    private ListModelIterator iterator;

    /**
     * The underlaying model
     */
    private ListModel model;

    /**
     * The context menu used
     */
    private ContextMenu contextMenu;

    /**
     * Fields on which you can sort
     */
    private List sortableFields = new ArrayList();

    /**
     * Fields you can group
     */
    private List groupableFields = new ArrayList();

    /**
     * Max rows shown per group
     */
    private int maxRowsPerGroup = 5;

    /**
     * Constructor. Setting the render type.
     */
    public ListControl() {
        this.setRenderType(RENDER_TYPE);
    }

    public ListModel getModel() {
        return this.model;
    }

    public void setModel(ListModel model) {
        this.model = model;
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#addColumn(info.magnolia.cms.gui.controlx.list.ListColumn)
     */
    public void addColumn(ListColumn column) {
        this.addChild(column);
    }

    /**
     * Layzy bound iterator.
     * @return Returns the iterator.
     */
    public ListModelIterator getIterator() {
        if (this.iterator == null) {
            this.iterator = this.getModel().getListModelIterator();
        }
        return iterator;
    }

    /**
     * Get the value for a column in the current iterator.
     * @param name
     * @return the value
     */
    public Object getIteratorValue(String name) {
        return this.getIterator().getValue(name);
    }

    /**
     * Get the current object (not the value) in the current iterator.
     * @return the object. corresponds to a row.
     */
    public Object getIteratorValueObject() {
        return this.getIterator().getValueObject();
    }

    public String getIteratorId() {
        return this.getIterator().getId();
    }


    /**
     * Restart the iterator.
     */
    public void resetIterator() {
        this.iterator = null;
    }

    public ContextMenu getContextMenu() {
        return this.contextMenu;
    }

    public void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public List getGroupableFields() {
        return this.groupableFields;
    }

    public List getSortableFields() {
        return this.sortableFields;
    }

    public void addSortableField(String name) {
        this.sortableFields.add(name);
    }

    public void addGroupableField(String name) {
        this.groupableFields.add(name);
    }

    public int getMaxRowsPerGroup() {
        return this.maxRowsPerGroup;
    }

    public void setMaxRowsPerGroup(int maxRowsPerGroup) {
        this.maxRowsPerGroup = maxRowsPerGroup;
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModel#getGroupBy()
     */
    public String getGroupBy() {
        return StringUtils.defaultString(this.model.getGroupBy());
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModel#getGroupByOrder()
     */
    public String getGroupByOrder() {
        return StringUtils.defaultIfEmpty(this.model.getGroupByOrder(), "asc");
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModel#getSortBy()
     */
    public String getSortBy() {
        return StringUtils.defaultString(this.model.getSortBy());
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModel#getSortByOrder()
     */
    public String getSortByOrder() {
        return StringUtils.defaultIfEmpty(this.model.getSortByOrder(), "asc");
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModel#setGroupBy(java.lang.String)
     */
    public void setGroupBy(String name) {
        this.model.setGroupBy(name, this.model.getGroupByOrder());
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModel#setGroupBy(java.lang.String)
     */
    public void setGroupByOrder(String order) {
        this.model.setGroupBy(this.model.getGroupBy(), order);
    }

    /**
     * Get the lable of a specific
     * @param name
     * @return
     */
    public String getColumnLabel(String name) {
        ListColumn column = (ListColumn) this.getChild(name);
        return column.getLabel();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModel#setSortBy(java.lang.String)
     */
    public void setSortBy(String name) {
        this.model.setSortBy(name, this.model.getSortByOrder());
    }

    public void setSortByOrder(String order) {
        this.model.setSortBy(this.model.getSortBy(), order);
    }

}
