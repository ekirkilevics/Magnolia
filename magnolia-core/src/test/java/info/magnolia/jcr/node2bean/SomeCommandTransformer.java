/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.jcr.node2bean;

import info.magnolia.commands.MgnlCommand;
import info.magnolia.commands.chain.Chain;
import info.magnolia.commands.chain.Command;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.objectfactory.ComponentProvider;

import java.util.Iterator;
import java.util.Map;


/**
 * Transformer used in Node2BeanTest.
 */
public class SomeCommandTransformer extends Node2BeanTransformerImpl {

    @Override
    public Object newBeanInstance(TransformationState state, Map<String, Object> values, ComponentProvider componentProvider) throws Node2BeanException {
        final Class<?> type = state.getCurrentType().getType();
        if (type.isInterface() && type.isAssignableFrom(Command.class)) {
            return new Node2BeanTest.MyChain();
        }
        return super.newBeanInstance(state, values, componentProvider);
    }

    @Override
    public void initBean(TransformationState state, Map values) throws Node2BeanException {
        Object bean = state.getCurrentBean();
        if (bean instanceof Chain) {
            Chain chain = (Chain) bean;
            for (Iterator iterator = values.values().iterator(); iterator.hasNext();) {
                Object cmd = iterator.next();
                if (cmd instanceof MgnlCommand) {
                    chain.addCommand((Command) cmd);
                }
            }
        }
        super.initBean(state, values);
    }

}
