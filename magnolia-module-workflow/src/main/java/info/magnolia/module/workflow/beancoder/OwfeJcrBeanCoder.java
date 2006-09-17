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

package info.magnolia.module.workflow.beancoder;

import openwfe.org.jcr.Node;
import openwfe.org.jcr.beancoder.JcrBeanCoder;
import openwfe.org.engine.workitem.StringAttribute;


/**
 * An extension of JcrBeanCoder that takes care of choosing nice
 * node names for StringAttributes...
 *
 * <p><font size=2>CVS Info :
 * <br>$Author$
 * <br>$Id$ </font>
 *
 * @author Nicolas Modrzyk
 * @author john.mettraux@openwfe.org
 */
public class OwfeJcrBeanCoder extends JcrBeanCoder
{
    public OwfeJcrBeanCoder
            (final String ns, final Node startNode)
    {
        super(ns, startNode);
    }

    public OwfeJcrBeanCoder
            (final String ns, final Node startNode, final String beanNodeName)
    {
        super(ns, startNode, beanNodeName);
    }

    protected Object[] asPropertyMapEntry(Object key, Object value) {
        if ((key instanceof StringAttribute) && isSafeForAnItemName(key.toString()))
            if(value instanceof StringAttribute)return new Object[] { key.toString(), value.toString()};
        return super.asPropertyMapEntry(key, value);
    }
}
