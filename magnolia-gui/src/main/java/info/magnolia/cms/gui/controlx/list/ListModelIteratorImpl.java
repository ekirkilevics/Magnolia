/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
