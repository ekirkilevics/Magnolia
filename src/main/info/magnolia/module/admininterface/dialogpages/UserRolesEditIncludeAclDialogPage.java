package info.magnolia.module.admininterface.dialogpages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogButton;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.module.admininterface.DialogPageMVCHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class UserRolesEditIncludeAclDialogPage extends DialogPageMVCHandler {

    public UserRolesEditIncludeAclDialogPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    // todo: permission global available somewhere
    private static final long PERMISSION_ALL = Permission.ALL;

    private static final long PERMISSION_READ = Permission.READ;

    private static final long PERMISSION_NO = 0;

    private static final String CSS_ACL_DIV = "aclDynamicTable"; //$NON-NLS-1$

    private static String getHtmlRowInner(HttpServletRequest request, String dynamicTable, String repository) {
        boolean small = true;
        Messages msgs = MessagesManager.getMessages(request);

        Select accessRight = new Select();
        accessRight.setSaveInfo(false);
        accessRight.setName("'+prefix+'AccessRight"); //$NON-NLS-1$
        accessRight.setCssClass("mgnlDialogControlSelect"); //$NON-NLS-1$
        accessRight.setOptions(msgs.get("roles.permission.readWrite"), Long.toString(PERMISSION_ALL)); //$NON-NLS-1$
        accessRight.setOptions(msgs.get("roles.permission.readOnly"), Long.toString(PERMISSION_READ)); //$NON-NLS-1$
        accessRight.setOptions(msgs.get("roles.permission.deny"), Long.toString(PERMISSION_NO)); //$NON-NLS-1$
        accessRight.setValue("' + object.accessRight + '"); //$NON-NLS-1$

        Select accessType = new Select();
        accessType.setSaveInfo(false);
        accessType.setName("'+prefix+'AccessType"); //$NON-NLS-1$
        accessType.setCssClass("mgnlDialogControlSelect"); //$NON-NLS-1$
        if (repository.equals(ContentRepository.WEBSITE)) {
            accessType.setOptions(msgs.get("roles.edit.thisAndSubPages"), "self"); //$NON-NLS-1$ //$NON-NLS-2$
            accessType.setOptions(msgs.get("roles.edit.subPages"), "sub"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
            accessType.setOptions(msgs.get("roles.edit.thisAndSubNodes"), "self"); //$NON-NLS-1$ //$NON-NLS-2$
            accessType.setOptions(msgs.get("roles.edit.subNodes"), "sub"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        accessType.setValue("' + object.accessType + '"); //$NON-NLS-1$

        Edit path = new Edit();
        path.setSaveInfo(false);
        path.setName("'+prefix+'Path"); //$NON-NLS-1$
        path.setValue("'+object.path+'"); //$NON-NLS-1$
        path.setCssClass(CssConstants.CSSCLASS_EDIT);
        path.setCssStyles("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        Button choose = new Button();
        choose.setLabel(msgs.get("buttons.choose")); //$NON-NLS-1$
        choose.setOnclick("aclChoose(\\''+prefix+'\\',\\'" + repository + "\\');"); //$NON-NLS-1$ //$NON-NLS-2$
        choose.setSmall(small);

        Button delete = new Button();
        delete.setLabel(msgs.get("buttons.delete")); //$NON-NLS-1$
        delete.setOnclick(dynamicTable + ".del('+index+');"); //$NON-NLS-1$
        delete.setSmall(small);

        StringBuffer html = new StringBuffer();
        // set as table since ie/win does not support setting of innerHTML of a
        // tr
        html.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">" //$NON-NLS-1$
            + accessRight.getHtml() + "</td>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"mgnlDialogBoxInput\"></td>"); //$NON-NLS-1$

        if (!repository.equals(ContentRepository.USERS) && !repository.equals(ContentRepository.USER_ROLES)) {
            html.append("<td width=\"1\" class=\"" //$NON-NLS-1$
                + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">" //$NON-NLS-1$
                + accessType.getHtml() + "</td>"); //$NON-NLS-1$
            html.append("<td width=\"1\"></td>"); //$NON-NLS-1$
        }
        else {
            html
                .append("<input type=\"hidden\" id=\"' + prefix + 'AccessType\" name=\"' + prefix + 'AccessType\" value=\"sub\"/>"); //$NON-NLS-1$
        }

        html.append("<td width=\"100%\"class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">" //$NON-NLS-1$
            + path.getHtml() + "</td>"); //$NON-NLS-1$
        html.append("<td width=\"1\"></td>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">" //$NON-NLS-1$
            + choose.getHtml() + "</td>"); //$NON-NLS-1$
        html.append("<td width=\"1\"></td>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">" //$NON-NLS-1$
            + delete.getHtml() + "</td>"); //$NON-NLS-1$
        html.append("</tr></table>"); //$NON-NLS-1$

        return html.toString();
    }

    /**
     * @throws Exception
     * @see info.magnolia.cms.servlets.BasePageServlet#draw(HttpServletRequest, HttpServletResponse)
     */
    protected void draw(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PrintWriter out = response.getWriter();
        Messages msgs = MessagesManager.getMessages(request);

        DialogSuper dialogControl = (DialogSuper) request.getAttribute("dialogObject"); //$NON-NLS-1$
        Content role = dialogControl.getWebsiteNode();

        // select the repository
        Select repositorySelect = getReposiotySelect(request);

        out.print(repositorySelect.getHtml());
        out.print("<p><p/>"); //$NON-NLS-1$
        for (int i = 0; i < ContentRepository.getAllRepositoryNames().length; i++) {
            writeRepositoryTable(request, response, msgs, out, role, ContentRepository.getAllRepositoryNames()[i]);
        }
        // out.print("<p>&nbsp;<p>&nbsp;<p>&nbsp;<input type=\"button\" onclick=\"aclChangeRepository('website')\">");
        out.println("<script>aclChangeRepository('website');</script>"); //$NON-NLS-1$ //$NON-NLS-2$

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
            + tableName + "',document.mgnlFormMain." //$NON-NLS-1$
            + hiddenFieldName + ", aclGetNewPermissionObject, aclGetPermissionObject, acl" //$NON-NLS-1$
            + repository + "RenderFunction, null);"); //$NON-NLS-1$

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
        boolean noAcl = false;
        try {
            Content acl = role.getContent("acl_" + repository); //$NON-NLS-1$
            if (acl.getChildren().size() == 0) {
                noAcl = true;
            }
            Iterator it = acl.getChildren().iterator();
            boolean skipNext = false;
            while (it.hasNext()) {
                Content c = (Content) it.next();

                if (skipNext) {
                    skipNext = false;
                }
                else {
                    String path = c.getNodeData("path").getString(); //$NON-NLS-1$
                    String accessRight = c.getNodeData("permissions").getString(); //$NON-NLS-1$
                    String accessType;

                    if (!StringUtils.contains(path, "/*")) { //$NON-NLS-1$
                        // access to self and subs, skip next (which is same with /*)
                        skipNext = true;
                        accessType = "self"; //$NON-NLS-1$
                    }
                    else {
                        if (path.equals("/*")) { //$NON-NLS-1$
                            path = "/"; //$NON-NLS-1$
                            accessType = "self"; //$NON-NLS-1$
                        }
                        else {
                            path = StringUtils.substringBeforeLast(path, "/*"); //$NON-NLS-1$
                            accessType = "sub"; //$NON-NLS-1$
                        }
                    }

                    out.println(dynamicTableName + ".add({accessRight:" //$NON-NLS-1$
                        + accessRight + ",accessType:'" //$NON-NLS-1$
                        + accessType + "',path:'" //$NON-NLS-1$
                        + path + "'});"); //$NON-NLS-1$
                }
            }
        }
        catch (Exception e) {
            noAcl = true;
        }
        if (noAcl) {
            out.println(dynamicTableName + ".addNew();"); //$NON-NLS-1$
        }
    }

    /**
     * @param request
     * @return
     */
    private Select getReposiotySelect(HttpServletRequest request) {
        Select repositorySelect = new Select();
        repositorySelect.setName("aclRepository"); //$NON-NLS-1$
        repositorySelect.setCssClass("mgnlDialogControlSelect"); //$NON-NLS-1$
        repositorySelect.setEvent("onchange", "aclChangeRepository(this.value)"); //$NON-NLS-1$ //$NON-NLS-2$
        repositorySelect.setSaveInfo(false);
        repositorySelect.setValue(ContentRepository.WEBSITE);

        // loop through the repositories
        for (int i = 0; i < ContentRepository.getAllRepositoryNames().length; i++) {
            String name = ContentRepository.getAllRepositoryNames()[i];
            String label = MessagesManager.get(request, "repository." + name); //$NON-NLS-1$
            repositorySelect.setOptions(label, name);
        }
        return repositorySelect;
    }
}