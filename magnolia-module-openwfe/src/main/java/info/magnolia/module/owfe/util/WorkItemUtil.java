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
package info.magnolia.module.owfe.util;

import info.magnolia.cms.util.MgnlCoreConstants;
import openwfe.org.engine.workitem.StringAttribute;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class WorkItemUtil {

    final static public StringAttribute ATT_TRUE = new StringAttribute(MgnlCoreConstants.TRUE);

    final static public StringAttribute ATT_FALSE = new StringAttribute(MgnlCoreConstants.FALSE);
}
