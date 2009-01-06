/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;

/**
 * This state contains three stacks. This allows the transformer to know the full state created during the
 * transoformation process. A stack for classes, beans, and content nodes are kept.
 * <p/>
 * It is the processors job to populate this state.
 *
 * @author philipp
 * @version $Id$
 */
public interface TransformationState {

    public void pushType(TypeDescriptor type);

    public void popType();

    public TypeDescriptor getCurrentType();

    public TypeDescriptor peekType(int pos);

    public void pushBean(Object bean);

    public void popBean();

    public Object getCurrentBean();

    public Object peekBean(int pos);

    public void pushContent(Content node);

    public void popContent();

    public Content getCurrentContent();

    public Content peekContent(int pos);

    /**
     * Is the value of the biggest stack size
     */
    public int getLevel();

}
