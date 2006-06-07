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



/**
 * Date: Mar 28, 2006 Time: 5:34:32 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public final class WorkflowConstants {

    final static public String WEBSITE_REPOSITORY = "website";

    final static public String WORKFLOW_EMAIL_TEMPLATE = "workflowEmail";

    final static public String WORKFLOW_EMAIL_FROM_FIELD = "workflow@magnolia.info";

    final static public String WORKFLOW_EMAIL_SUBJECT_FIELD = "Workflow Request";
    
    public static final String REPO_OWFE = "magnolia";

    public final static String WORKSPACE_EXPRESSION = "Expressions";

    public final static String NODENAME_EXPRESSION = "expression";
    
    public final static String WORKSPACE_STORE = "Store";

    public final static String WORKSPACE_ENGINE = "WorkflowModule";
    
    public final static String NODENAME_WORKITEM = "workItem";

	public static final String BAR = "|";

	public static final String COLON = ":";

	public static final String DOT = ".";

	public static final String SLASH = "/";

	public static final String NODEDATA_PARTICIPANT = "participant";

	public static final String NODEDATA_ID = "ID";

	public static final String NODEDATA_VALUE = "value";

	public final static String ROOT_PATH_FOR_FLOW = "/modules/workflow/config/flows/";

	public final static String FLOW_VALUE = "value";

	public static final String ENGINE_NAME = "owfe";

    public static final String STORE_ITERATOR_QUERY = "select * from "+NODENAME_EXPRESSION;
    
    public static final String ATT_ASSIGN_TO = "assignTo";

    public static final String START_DATE = "startDate";

    public static final String END_DATE = "endDate";
                                                                                                    ;
    public static final String OPENWFE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
}
