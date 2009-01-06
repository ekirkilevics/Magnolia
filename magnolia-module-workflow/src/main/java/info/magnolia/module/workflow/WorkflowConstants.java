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

    public final static String ROOT_PATH_FOR_FLOW = "/modules/workflow/config/flows";

    public final static String FLOW_VALUE = "value";

    public static final String ENGINE_NAME = "owfe";

    public static final String STORE_ITERATOR_QUERY = "select * from " + NODENAME_EXPRESSION;

    public static final String OPENWFE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";

    public static final String NODEDATA_WORKITEM = "wi";

    public static final String DEFAULT_EDIT_DIALOG = "editWorkItem";

    public static final String DEFAULT_ACTIVATION_EDIT_DIALOG = "editActivationWorkItem";

    public static final String DEFAULT_WORKFLOW = "default";

    public static final String DEFAULT_ACTIVATION_WORKFLOW = "activation";
}
