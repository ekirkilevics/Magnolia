/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.owfe;

import info.magnolia.cms.util.MgnlCoreConstants;
import openwfe.org.engine.workitem.StringAttribute;


/**
 * Date: Mar 28, 2006 Time: 5:34:32 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public final class MgnlConstants {

    final static public String INFLOW_PARAM = "workItem";

    final static public String INTREE_PARAM = "treeParam";

    final static public String P_CONTEXT = "context";

    final static public String P_REQUEST = "request";

    final static public String P_RESULT = "__RESULT__";

    final static public String P_PATH = "pathSelected";

    final static public String P_RECURSIVE = "recursive";

    final static public String P_ACTION = "action";

    final static public String P_WORKFLOW_DEFINITION_URL = "field:__definition__";

    final static public String P_DEFINITION = "__definition__";

    final static public String P_MAILTO = "mailTo";

    final static public String P_OK = "OK";

    final static public String P_START_DATE = "startDate";

    final static public String P_END_DATE = "endDate";

    final static public String P_TREE = "tree";

    final static public String P_HM = "hm";

    final static public String P_MAILTEMPLATE = "mailTemplate";

    final static public String P_DEFAULT_ACTIVATION_FLOW = "webActivation";

    final static public String P_DEFAULT_DEACTIVATION_FLOW = "webDeactivation";

    final static public String P_DEFAULT_SCHEDULEDACTIVATION_FLOW = "webScheduledActivation";

    final static public StringAttribute ATT_TRUE = new StringAttribute(MgnlCoreConstants.TRUE);

    final static public StringAttribute ATT_FALSE = new StringAttribute(MgnlCoreConstants.FALSE);

    final static public String WEBSITE_REPOSITORY = "website";

    final static public String WORKFLOW_EMAIL_TEMPLATE = "workflowEmail";

    final static public String WORKFLOW_EMAIL_FROM_FIELD = "MagnoliaWorkflow";

    final static public String WORKFLOW_EMAIL_SUBJECT_FIELD = "Workflow Request";
    
    public static final String REPO_OWFE = "magnolia";

    public final static String WORKSPACE_EXPRESSION = "Expressions";

    public final static String NODENAME_EXPRESSION = "expression";
    
    public final static String WORKSPACE_STORE = "Store";

    public final static String WORKSPACE_ENGINE = "Engine";
    
    public final static String NODENAME_WORKITEM = "workItem";

	public static final String ATT_ASSIGN_TO = "assignTo";

	public static final String ATT_OK = "OK";
    
}
