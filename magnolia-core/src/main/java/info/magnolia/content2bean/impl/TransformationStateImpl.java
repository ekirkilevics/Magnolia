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
package info.magnolia.content2bean.impl;

import info.magnolia.cms.core.Content;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;

import org.apache.commons.collections.ArrayStack;


/**
 * Transformation state implementation. Contains stacks of info for the types, beans and the content.
 * @author had
 * @version $Id$
 */
public class TransformationStateImpl implements TransformationState {

    protected ArrayStack typeStack = new ArrayStack();

    protected ArrayStack beanStack = new ArrayStack();

    protected ArrayStack contentStack = new ArrayStack();

    public Object getCurrentBean() {
        return beanStack.peek();
    }

    public TypeDescriptor getCurrentType() {
        return (TypeDescriptor) typeStack.peek();
    }

    public Content getCurrentContent() {
        return (Content) contentStack.peek();
    }

    public Object peekBean(int pos) {
        return beanStack.peek(pos);
    }

    public TypeDescriptor peekType(int pos) {
        return (TypeDescriptor) typeStack.peek(pos);
    }

    public Content peekContent(int pos) {
        return (Content) contentStack.peek(pos);
    }

    public void popBean() {
        beanStack.pop();
    }

    public void popType() {
        typeStack.pop();
    }

    public void popContent() {
        contentStack.pop();
    }

    public void pushBean(Object bean) {
        beanStack.push(bean);
    }

    public void pushType(TypeDescriptor type) {
        typeStack.push(type);
    }

    public void pushContent(Content node) {
        contentStack.push(node);
    }

    public int getLevel() {
        return Math.max(Math.max(typeStack.size(), beanStack.size()), contentStack.size());
    }
}
