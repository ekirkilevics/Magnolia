/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
 *
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
     *
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
        if(this.iterator == null){
            this.iterator = this.getModel().iterator();
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

    /**
     * Restart the iterator.
     *
     */
    public void resetIterator(){
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
    public String getColumnLabel(String name){
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

