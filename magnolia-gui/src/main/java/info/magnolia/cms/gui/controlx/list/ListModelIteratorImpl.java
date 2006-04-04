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

import info.magnolia.cms.core.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;

/**
 * @author Sameer Charles
 * $Id:ListModelIteratorImpl.java 2492 2006-03-30 08:30:43Z scharles $
 */
public class ListModelIteratorImpl implements ListModelIterator {

    /**
     * list holding all objects/records
     * */
    private final List list;

    /**
     *  next position
     * */
    private int pos;

    /**
     * next content object (prefetched)
     * */
    private Content next;

    /**
     * object on current pointer
     * */
    private Content current;

    /**
     *  key name on which provided list is grouped
     * */
    private String groupKey;

    /**
     * creates a new ListModelIterator
     * @param list of content objects
     * */
    public ListModelIteratorImpl(List list, String groupKey) {
        this.list = new ArrayList(list);
        this.groupKey = groupKey;
        this.pos = 0;
        // prefetch next object
        prefetchNext();
    }

    /**
     * prefetch object for the list
     *
     * */
    private void prefetchNext() {
        this.next = null;
        while (this.next == null && this.pos < this.list.size()) {
            try {
                this.next = (Content) this.list.get(pos);
            } catch (ClassCastException e) {
                // invalid object, remove it and try again
                this.list.remove(pos); // will try again
            }
        }
    }

    /**
     * get named value
     *
     * @param name its a key to which value is attached in this record
     */
    public Object getValue(String name) {
        return this.current.getNodeData(name).getString();
    }

    /**
     * get group name
     *
     * @return name of the group of the current record
     */
    public String getGroupName() {
        if (StringUtils.isEmpty(this.groupKey)) return StringUtils.EMPTY;
        return this.current.getNodeData(this.groupKey).getString();
    }

    /**
     * move next
     */
    public void next() {
        if (this.next == null) {
            throw new NoSuchElementException();
        }
        this.current = this.next;
        this.pos++;
        prefetchNext();
    }

    /**
     * jump to next group
     */
    public void nextGroup() {
        while (this.hasNextInGroup()) {
            this.next();
        }
    }

    /**
     * checks if there is next record
     *
     * @return true if not EOF
     */
    public boolean hasNext() {
        return this.next != null;
    }

    /**
     * checks if there are more records in the current group
     *
     * @return true if not EOF
     */
    public boolean hasNextInGroup() {
        if (StringUtils.isEmpty(this.groupKey)) return this.hasNext(); // no group key defined, its all one group
        else if (this.hasNext()) {
            if (this.current != null) {
                String currentValue = this.current.getNodeData(this.groupKey).getString();
                String nextValue = this.next.getNodeData(this.groupKey).getString();
                return StringUtils.equalsIgnoreCase(currentValue, nextValue);
            }
        } else {
            return false;
        }
        return true;
    }

}
