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
package info.magnolia.module.workflow;

/**
 * Date: Mar 28, 2006 Time: 5:34:32 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public final class WorkflowConstants {

    final static public String PARTICIPANT_PREFIX_USER = "user-";

    final static public String PARTICIPANT_PREFIX_GROUP = "group-";

    final static public String PARTICIPANT_PREFIX_ROLE = "role-";

    final static public String PARTICIPANT_PREFIX_COMMAND = "command-";

    public static final String ATTRIBUTE_USERNAME = "userName";

    final static public String ATTRIBUTE_ACTION = "action";

    public static final String ATTRIBUTE_ASSIGN_TO = "assignTo";

    public static final String ATTRIBUTE_START_DATE = "startDate";

    public static final String ATTRIBUTE_END_DATE = "endDate";

    public static final String ATTRIBUTE_EDIT_DIALOG = "editDialog";

    final static public String ATTRIBUTE_WORKFLOW_DEFINITION_URL = "field:__definition__";

    final static public String ATTRIBUTE_DEFINITION = "__definition__";

    public static final String ACTION_PROCEED = "proceed";

    public static final String ACTION_REJECT = "reject";

    public static final String ACTION_CANCEL = "cancel";

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

    public static final String STORE_ITERATOR_QUERY = "select * from " + NODENAME_EXPRESSION;

    public static final String OPENWFE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";

    public static final String NODEDATA_WORKITEM = "wi";

    public static final String DEFAULT_EDIT_DIALOG = "editWorkItem";

    /**
     * 
     */
    public static final String DEFAULT_WORKFLOW = "default";

}
