package info.magnolia.module.owfe;

import openwfe.org.engine.workitem.StringAttribute;

/**
 * Date: Mar 28, 2006
 * Time: 5:34:32 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public final class MgnlConstants {


    final static public String INFLOW_PARAM = "workItem";
    final static public String INTREE_PARAM = "treeParam";


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

    final static public String P_DEFAULT_ACTIVATION_FLOW = "webActivation";
    final static public String P_DEFAULT_DEACTIVATION_FLOW = "webDeactivation";
    final static public String P_DEFAULT_SCHEDULEDACTIVATION_FLOW = "webScheduledActivation";

    final static public String TRUE = "true";
    final static public String FALSE = "false";
    final static public StringAttribute ATT_TRUE = new StringAttribute(TRUE);
    final static public StringAttribute ATT_FALSE = new StringAttribute(FALSE);

    final static public String WEBSITE_REPOSITORY = "website";

    final static public String PREFIX_USER = "user-";
    final static public String PREFIX_GROUP = "group-";
    final static public String PREFIX_ROLE = "role-";


    final static public int PREFIX_USER_LEN = PREFIX_USER.length();
    final static public int PREFIX_GROUP_LEN = PREFIX_GROUP.length();
    final static public int PREFIX_ROLE_LEN = PREFIX_ROLE.length();

}
