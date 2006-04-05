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

import java.util.List;

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
    private List sortableFields;
    
    /**
     * Fields you can group
     */
    private List groupableFields;
    
    /**
     * Max rows shown per group
     */
    private int maxRowsPerGroup = 5;

    /**
     * Current sorting
     */
    private String sortField = "";

    /**
     * Current sort direction
     */
    private String sortDirection = "asc";

    /**
     * Current group field
     */
    private String groupField = "";

    /**
     * Current grouping direction
     */
    private String groupDirection = "asc";
    
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
     * @return Returns the groupDirection.
     */
    public String getGroupDirection() {
        return groupDirection;
    }

    /**
     * @param groupDirection The groupDirection to set.
     */
    public void setGroupDirection(String groupDirection) {
        this.groupDirection = groupDirection;
    }

    /**
     * @return Returns the groupField.
     */
    public String getGroupField() {
        return groupField;
    }

    /**
     * @param groupField The groupField to set.
     */
    public void setGroupField(String groupField) {
        this.groupField = groupField;
    }

    /**
     * @return Returns the sortDirection.
     */
    public String getSortDirection() {
        return sortDirection;
    }

    /**
     * @param sortDirection The sortDirection to set.
     */
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    /**
     * @return Returns the sortField.
     */
    public String getSortField() {
        return sortField;
    }

    /**
     * @param sortField The sortField to set.
     */
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

}

