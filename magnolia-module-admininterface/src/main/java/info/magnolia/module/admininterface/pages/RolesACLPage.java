package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogButton;
import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.admininterface.SimplePageMVCHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;


/**
 * @author Fabrizio Giustina
 * @version $Id:RolesACLPage.java 2516 2006-03-31 13:08:03Z philipp $
 */
public class RolesACLPage extends SimplePageMVCHandler {

    public static int TYPE_ALL = 3; // 11 : subs and this

    public static int TYPE_SUBS = 2; // 10

    public static int TYPE_THIS = 1; // 01

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Do not show this repositories in the dialog. A module may change this list
     */
    public static List excludedRepositories = new ArrayList(Arrays.asList(new String[]{"mgnlVersion", "mgnlSystem"}));

    // todo: permission global available somewhere
    private static final long PERMISSION_ALL = Permission.ALL;

    private static final long PERMISSION_READ = Permission.READ;

    private static final long PERMISSION_NO = 0;

    private static final String CSS_ACL_DIV = "aclDynamicTable"; //$NON-NLS-1$

    public RolesACLPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    private static String getHtmlRowInner(HttpServletRequest request, String dynamicTable, String repository) {
        boolean small = true;
        Messages msgs = MessagesManager.getMessages();

        Select accessRight = new Select();
        accessRight.setSaveInfo(false);
        accessRight.setName("'+prefix+'AccessRight"); //$NON-NLS-1$
        accessRight.setCssClass("mgnlDialogControlSelect"); //$NON-NLS-1$
        accessRight.setOptions(escapeJs(msgs.get("roles.permission.readWrite")), Long.toString(PERMISSION_ALL)); //$NON-NLS-1$
        accessRight.setOptions(escapeJs(msgs.get("roles.permission.readOnly")), Long.toString(PERMISSION_READ)); //$NON-NLS-1$
        accessRight.setOptions(escapeJs(msgs.get("roles.permission.deny")), Long.toString(PERMISSION_NO)); //$NON-NLS-1$
        accessRight.setValue("' + object.accessRight + '"); //$NON-NLS-1$

        Select accessType = new Select();
        accessType.setSaveInfo(false);
        accessType.setName("'+prefix+'AccessType"); //$NON-NLS-1$
        accessType.setCssClass("mgnlDialogControlSelect"); //$NON-NLS-1$
        if (repository.equals(ContentRepository.WEBSITE)) {
            accessType.setOptions(escapeJs(msgs.get("roles.edit.thisAndSubPages")), String.valueOf(TYPE_ALL)); //$NON-NLS-1$ 
            accessType.setOptions(escapeJs(msgs.get("roles.edit.subPages")), String.valueOf(TYPE_SUBS)); //$NON-NLS-1$ 
        }
        else {
            if (repository.equals(ContentRepository.CONFIG)) {
                accessType.setOptions(escapeJs(msgs.get("roles.edit.thisNode")), String.valueOf(TYPE_THIS)); //$NON-NLS-1$ 
            }
            accessType.setOptions(escapeJs(msgs.get("roles.edit.thisAndSubNodes")), String.valueOf(TYPE_ALL)); //$NON-NLS-1$ 
            accessType.setOptions(escapeJs(msgs.get("roles.edit.subNodes")), String.valueOf(TYPE_SUBS)); //$NON-NLS-1$ 
        }
        accessType.setValue("' + object.accessType + '"); //$NON-NLS-1$

        Edit path = new Edit();
        path.setSaveInfo(false);
        path.setName("'+prefix+'Path"); //$NON-NLS-1$
        path.setValue("'+object.path+'"); //$NON-NLS-1$
        path.setCssClass(CssConstants.CSSCLASS_EDIT);
        path.setCssStyles("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        Button choose = new Button();
        choose.setLabel(escapeJs(msgs.get("buttons.choose"))); //$NON-NLS-1$
        choose.setOnclick("aclChoose(\\''+prefix+'\\',\\'" + repository + "\\');"); //$NON-NLS-1$ //$NON-NLS-2$
        choose.setSmall(small);

        Button delete = new Button();
        delete.setLabel(escapeJs(msgs.get("buttons.delete"))); //$NON-NLS-1$
        delete.setOnclick(dynamicTable + ".del('+index+');"); //$NON-NLS-1$
        delete.setSmall(small);

        StringBuffer html = new StringBuffer();
        // set as table since ie/win does not support setting of innerHTML of a
        // tr
        html.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">" //$NON-NLS-1$
            + accessRight.getHtml()
            + "</td>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"mgnlDialogBoxInput\"></td>"); //$NON-NLS-1$

        if (!repository.equals(ContentRepository.USERS) && !repository.equals(ContentRepository.USER_ROLES)) {
            html.append("<td width=\"1\" class=\"" //$NON-NLS-1$
                + CssConstants.CSSCLASS_EDITWITHBUTTON
                + "\">" //$NON-NLS-1$
                + accessType.getHtml()
                + "</td>"); //$NON-NLS-1$
            html.append("<td width=\"1\"></td>"); //$NON-NLS-1$
        }
        else {
            html
                .append("<input type=\"hidden\" id=\"' + prefix + 'AccessType\" name=\"' + prefix + 'AccessType\" value=\"sub\"/>"); //$NON-NLS-1$
        }

        html.append("<td width=\"100%\"class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">" //$NON-NLS-1$
            + path.getHtml()
            + "</td>"); //$NON-NLS-1$
        html.append("<td width=\"1\"></td>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">" //$NON-NLS-1$
            + choose.getHtml()
            + "</td>"); //$NON-NLS-1$
        html.append("<td width=\"1\"></td>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">" //$NON-NLS-1$
            + delete.getHtml()
            + "</td>"); //$NON-NLS-1$
        html.append("</tr></table>"); //$NON-NLS-1$

        return html.toString();
    }

    /**
     * @throws Exception
     * @see info.magnolia.cms.servlets.BasePageServlet#render(HttpServletRequest, HttpServletResponse)
     */
    protected void render(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PrintWriter out = response.getWriter();
        Messages msgs = MessagesManager.getMessages();

        DialogControlImpl dialogControl = (DialogControlImpl) request.getAttribute("dialogObject"); //$NON-NLS-1$
        Content role = dialogControl.getWebsiteNode();

        // select the repository
        Select repositorySelect = getRepositorySelect(request);

        out.print(repositorySelect.getHtml());
        out.print("<p><p/>"); //$NON-NLS-1$
        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();

        while (repositoryNames.hasNext()) {
            String name = (String) repositoryNames.next();
            // exclude system repositories
            if (!excludedRepositories.contains(name)) {
                writeRepositoryTable(request, response, msgs, out, role, name);
            }
        }

        // out.print("<p>&nbsp;<p>&nbsp;<p>&nbsp;<input type=\"button\" onclick=\"aclChangeRepository('website')\">");
        out.println("<script>aclChangeRepository('website');</script>"); //$NON-NLS-1$ 

    }

    /**
     * @param request
     * @param out
     * @param role
     * @throws RepositoryException
     * @throws IOException
     */
    private void writeRepositoryTable(HttpServletRequest request, HttpServletResponse response, Messages msgs,
        PrintWriter out, Content role, String repository) throws RepositoryException, IOException {
        String tableName = "acl" + repository + "Table"; //$NON-NLS-1$ //$NON-NLS-2$
        String dynamicTableName = "acl" + repository + "DynamicTable"; //$NON-NLS-1$ //$NON-NLS-2$
        String hiddenFieldName = "acl" + repository + "List"; //$NON-NLS-1$ //$NON-NLS-2$

        out.println("<div id=\"acl" + repository + "Div\" class=\"" + CSS_ACL_DIV + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        out.println(new Hidden(hiddenFieldName, StringUtils.EMPTY, false).getHtml());

        // the table
        out.println("<table id=\"" //$NON-NLS-1$
            + tableName
            + "\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td></td></tr></table>"); //$NON-NLS-1$

        // add button

        out.println("<table width=\"100%\">"); //$NON-NLS-1$
        DialogButton add = DialogFactory.getDialogButtonInstance(request, response, null, null);
        add.setBoxType(DialogBox.BOXTYPE_1COL);
        add.setConfig("buttonLabel", msgs.get("buttons.add")); //$NON-NLS-1$ //$NON-NLS-2$
        add.setConfig("onclick", dynamicTableName + ".addNew();"); //$NON-NLS-1$ //$NON-NLS-2$
        add.drawHtml(out);
        out.println("</table>"); //$NON-NLS-1$

        out.println("</div>"); //$NON-NLS-1$

        out.println("<script type=\"text/javascript\">"); //$NON-NLS-1$
        // register the repository
        out.println("aclRepositories[aclRepositories.length]= '" + repository + "';"); //$NON-NLS-1$ //$NON-NLS-2$

        // make renderer function
        out.println("function acl" + repository + "RenderFunction(cell, prefix, index, object)"); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("{"); //$NON-NLS-1$

        // get some debug informations
        out.println("mgnlDebug('acl" + repository + "RenderFunction: prefix = ' + prefix, 'acl', object)");
        out.println("cell.innerHTML= '" + getHtmlRowInner(request, dynamicTableName, repository) + "';\n"); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("document.getElementById(prefix + 'AccessType').value = object.accessType;\n"); //$NON-NLS-1$

        out.println("document.getElementById(prefix + 'AccessRight').value = object.accessRight;\n"); //$NON-NLS-1$

        out.println("}"); //$NON-NLS-1$

        // create the dynamicTable
        out.println(dynamicTableName + " = new MgnlDynamicTable('" //$NON-NLS-1$
            + tableName
            + "',document.mgnlFormMain." //$NON-NLS-1$
            + hiddenFieldName
            + ", aclGetNewPermissionObject, aclGetPermissionObject, acl" //$NON-NLS-1$
            + repository
            + "RenderFunction, null);"); //$NON-NLS-1$

        // add existing acls to table (by js, so the same mechanism as at
        // adding rows can be used)
        addExistingAclToTable(out, role, dynamicTableName, repository);

        out.println("</script>"); //$NON-NLS-1$
    }

    /**
     * @param out
     * @param role
     */
    private void addExistingAclToTable(PrintWriter out, Content role, String dynamicTableName, String repository) {
        // keeps acls per path
        ACLS acls = new ACLS();

        Content aclsNode = ContentUtil.getContent(role, "acl_" + repository); //$NON-NLS-1$
        if (aclsNode == null || aclsNode.getChildren().size() == 0) {
            out.println(dynamicTableName + ".addNew();"); //$NON-NLS-1$
            return;
        }

        Iterator it = aclsNode.getChildren().iterator();
        while (it.hasNext()) {
            Content c = (Content) it.next();
            String path = c.getNodeData("path").getString(); //$NON-NLS-1$
            String accessRight = c.getNodeData("permissions").getString(); //$NON-NLS-1$
            acls.register(path, Integer.valueOf(accessRight).intValue());
        }

        for (Iterator iter = acls.values().iterator(); iter.hasNext();) {
            ACL acl = (ACL) iter.next();
            out.println(dynamicTableName + ".add({accessRight:" //$NON-NLS-1$
                + acl.accessRight
                + ",accessType:'" //$NON-NLS-1$
                + acl.type
                + "',path:'" //$NON-NLS-1$
                + acl.path
                + "'});"); //$NON-NLS-1$
        }
    }

    /**
     * @param request
     * @return
     */
    private Select getRepositorySelect(HttpServletRequest request) {
        Select repositorySelect = new Select();
        repositorySelect.setName("aclRepository"); //$NON-NLS-1$
        repositorySelect.setCssClass("mgnlDialogControlSelect"); //$NON-NLS-1$
        repositorySelect.setEvent("onchange", "aclChangeRepository(this.value)"); //$NON-NLS-1$ //$NON-NLS-2$
        repositorySelect.setSaveInfo(false);
        repositorySelect.setValue(ContentRepository.WEBSITE);

        // loop through the repositories
        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String name = (String) repositoryNames.next();
            if (!excludedRepositories.contains(name)) {
                String label = MessagesManager.get("repository." + name); //$NON-NLS-1$
                repositorySelect.setOptions(label, name);
            }
        }
        return repositorySelect;
    }

    private static String escapeJs(String value) {
        return StringUtils.replace(value, "'", "\\'");
    }

    /**
     * A concrete gui acl
     * @author Philipp Bracher
     * @version $Revision$ ($Author$)
     */
    private class ACL {

        int type = 0;

        String path;

        int accessRight;

        void registerEntry(String path) {
            if (path.equals("/*")) {
                type = TYPE_ALL;
            }
            else if (path.endsWith("/*")) {
                type = type | TYPE_SUBS;
            }
            else {
                type = type | TYPE_THIS;
            }
        }
    }

    /**
     * Used to create the gui values out of the entries in the repository
     * @author Philipp Bracher
     * @version $Revision$ ($Author$)
     */
    private class ACLS extends ListOrderedMap {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Register an entry
         * @param path the not cleaned path
         * @param accessRight the access right
         */
        void register(String path, int accessRight) {
            String cleanPath = StringUtils.removeEnd(path, "/*");
            if (StringUtils.isEmpty(cleanPath)) {
                cleanPath = "/";
            }
            String key = cleanPath + ":" + accessRight;
            if (!this.containsKey(key)) {
                ACL acl = new ACL();
                acl.path = cleanPath;
                acl.accessRight = accessRight;
                this.put(key, acl);
            }
            ((ACL) this.get(key)).registerEntry(path);
        }
    }

}