/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.controlx.list;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles $Id:ListModelIteratorImpl.java 2492 2006-03-30 08:30:43Z scharles $
 */
public class ListModelIteratorImpl implements ListModelIterator {

    private AbstractListModel model;

    /**
     * list holding all objects/records
     */
    private final List list;

    /**
     * next position
     */
    private int pos;

    /**
     * next content object (prefetched)
     */
    private Object next;

    /**
     * object on current pointer
     */
    private Object current;

    /**
     * key name on which provided list is grouped
     */
    private String groupKey;

    private ValueProvider valueProvider;

    public ListModelIteratorImpl(AbstractListModel model, List list) {
        this.model = model;
        this.list = new ArrayList(list);
        this.groupKey = model.getGroupBy();
        this.pos = 0;
        this.setValueProvider(model.getValueProvider());

        // prefetch next object
        prefetchNext();
    }

    /**
     * prefetch object for the list
     */
    private void prefetchNext() {
        this.next = null;
        while (this.next == null && this.pos < this.list.size()) {
            this.next = this.list.get(pos);
        }
    }

    /**
     * get named value
     * @param name its a key to which value is attached in this record
     */
    public Object getValue(String name) {
        return this.getValue(name, this.current);
    }

    /**
     * get value from a specified object
     * @param name its a key to which value is attached in this record
     * @param node
     */
    protected Object getValue(String name, Object node) {
        return this.getValueProvider().getValue(name, node);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModelIterator#getValueObject()
     */
    public Object getValueObject() {
        return this.current;
    }

    /**
     * get group name
     * @return name of the group of the current record
     */
    public String getGroupName() {
        if (StringUtils.isEmpty(this.groupKey)) {
            return StringUtils.EMPTY;
        }
        return (String) this.getValue(this.groupKey, this.current);
    }

    /**
     * move next
     */
    public Object next() {
        if (this.next == null) {
            throw new NoSuchElementException();
        }
        this.current = this.next;
        this.pos++;
        prefetchNext();

        return this.current;
    }

    /**
     * jump to next group
     */
    public Object nextGroup() {
        Object tmp = null;
        while (this.hasNextInGroup()) {
            tmp = this.next();
        }
        return tmp;
    }

    /**
     * checks if there is next record
     * @return true if not EOF
     */
    public boolean hasNext() {
        return this.next != null;
    }

    /**
     * checks if there are more records in the current group
     * @return true if not EOF
     */
    public boolean hasNextInGroup() {
        if (StringUtils.isEmpty(this.groupKey)) {
            return this.hasNext(); // no group key defined, its all one group
        }
        else if (this.hasNext()) {
            if (this.current != null) {
                String currentValue = (String) this.getValue(this.groupKey, this.current);
                String nextValue = (String) this.getValue(this.groupKey, this.next);
                return StringUtils.equalsIgnoreCase(currentValue, nextValue);
            }
        }
        else {
            return false;
        }
        return true;
    }

    public String getId() {
        return this.model.resolveId(pos-1, this.getValueObject());
    }

    public void remove() {
        // not implemented
    }

    public void setValueProvider(ValueProvider valueProvider) {
        this.valueProvider = valueProvider;
    }

    public ValueProvider getValueProvider() {
        return valueProvider;
    }

}
