package info.magnolia.module.admininterface.pages;

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
import info.magnolia.cms.servlets.BasePageServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class UserRolesEditIncludeAclDialogPage extends BasePageServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    // todo: permission global available somewhere
    private static final long PERMISSION_ALL = Permission.ALL;

    private static final long PERMISSION_READ = Permission.READ;

    private static final long PERMISSION_NO = 0;

    private static final String CSS_ACL_DIV = "aclDynamicTable";

    private static String getHtmlRowInner(HttpServletRequest request, String dynamicTable, String repository) {
        boolean small = true;
        Messages msgs = MessagesManager.getMessages(request);

        Select accessRight = new Select();
        accessRight.setSaveInfo(false);
        accessRight.setName("'+prefix+'AccessRight");
        accessRight.setCssClass("mgnlDialogControlSelect");
        accessRight.setOptions(msgs.get("roles.permission.readWrite"), Long.toString(PERMISSION_ALL));
        accessRight.setOptions(msgs.get("roles.permission.readOnly"), Long.toString(PERMISSION_READ));
        accessRight.setOptions(msgs.get("roles.permission.deny"), Long.toString(PERMISSION_NO));
        accessRight.setValue("' + object.accessRight + '");

        Select accessType = new Select();
        accessType.setSaveInfo(false);
        accessType.setName("'+prefix+'AccessType");
        accessType.setCssClass("mgnlDialogControlSelect");
        if (repository.equals(ContentRepository.WEBSITE)) {
            accessType.setOptions(msgs.get("roles.edit.thisAndSubPages"), "self");
            accessType.setOptions(msgs.get("roles.edit.subPages"), "sub");
        }
        else {
            accessType.setOptions(msgs.get("roles.edit.thisAndSubNodes"), "self");
            accessType.setOptions(msgs.get("roles.edit.subNodes"), "sub");
        }
        accessType.setValue("' + object.accessType + '");

        Edit path = new Edit();
        path.setSaveInfo(false);
        path.setName("'+prefix+'Path");
        path.setValue("'+object.path+'");
        path.setCssClass(CssConstants.CSSCLASS_EDIT);
        path.setCssStyles("width", "100%");

        Button choose = new Button();
        choose.setLabel(msgs.get("buttons.choose"));
        choose.setOnclick("aclChoose(\\''+prefix+'\\',\\'" + repository + "\\');");
        choose.setSmall(small);

        Button delete = new Button();
        delete.setLabel(msgs.get("buttons.delete"));
        delete.setOnclick(dynamicTable + ".del('+index+');");
        delete.setSmall(small);

        StringBuffer html = new StringBuffer();
        // set as table since ie/win does not support setting of innerHTML of a
        // tr
        html.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
        html.append("<td width=\"1\" class=\""
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">"
            + accessRight.getHtml()
            + "</td>");
        html.append("<td width=\"1\" class=\"mgnlDialogBoxInput\"></td>");

        if (!repository.equals(ContentRepository.USERS) && !repository.equals(ContentRepository.USER_ROLES)) {
            html.append("<td width=\"1\" class=\""
                + CssConstants.CSSCLASS_EDITWITHBUTTON
                + "\">"
                + accessType.getHtml()
                + "</td>");
            html.append("<td width=\"1\"></td>");
        }
        else {
            html
                .append("<input type=\"hidden\" id=\"' + prefix + 'AccessType\" name=\"' + prefix + 'AccessType\" value=\"sub\"/>");
        }

        html.append("<td width=\"100%\"class=\""
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">"
            + path.getHtml()
            + "</td>");
        html.append("<td width=\"1\"></td>");
        html.append("<td width=\"1\" class=\""
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">"
            + choose.getHtml()
            + "</td>");
        html.append("<td width=\"1\"></td>");
        html.append("<td width=\"1\" class=\""
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">"
            + delete.getHtml()
            + "</td>");
        html.append("</tr></table>");

        return html.toString();
    }

    /**
     * @see info.magnolia.cms.servlets.BasePageServlet#draw(HttpServletRequest, HttpServletResponse)
     */
    public void draw(HttpServletRequest request, HttpServletResponse response) throws IOException, RepositoryException {
        PrintWriter out = response.getWriter();
        Messages msgs = MessagesManager.getMessages(request);

        DialogSuper dialogControl = (DialogSuper) request.getAttribute("dialogObject");
        Content role = dialogControl.getWebsiteNode();

        // select the repository
        Select repositorySelect = getReposiotySelect(request);

        out.print(repositorySelect.getHtml());
        out.print("<p><p/>");
        for (int i = 0; i < ContentRepository.ALL_REPOSITORIES.length; i++) {
            writeRepositoryTable(request, response, msgs, out, role, ContentRepository.ALL_REPOSITORIES[i]);
        }
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
        String tableName = "acl" + repository + "Table";
        String dynamicTableName = "acl" + repository + "DynamicTable";
        String hiddenFieldName = "acl" + repository + "List";

        out.println("<div id=\"acl" + repository + "Div\" class=\"" + CSS_ACL_DIV + "\">");
        out.println(new Hidden(hiddenFieldName, "", false).getHtml());

        // the table
        out.println("<table id=\""
            + tableName
            + "\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td></td></tr></table>");

        // add button

        out.println("<table width=\"100%\">");
        DialogButton add = DialogFactory.getDialogButtonInstance(request, response, null, null);
        add.setBoxType(DialogBox.BOXTYPE_1COL);
        add.setConfig("buttonLabel", msgs.get("buttons.add"));
        add.setConfig("onclick", dynamicTableName + ".addNew();");
        add.drawHtml(out);
        out.println("</table>");

        out.println("</div>");

        out.println("<script type=\"text/javascript\">");
        // register the repository
        out.println("aclRepositories[aclRepositories.length]= '" + repository + "';");

        if (repository == ContentRepository.WEBSITE) {
            out.println("document.getElementById('acl" + repository + "Div').style.visibility='visible';");
        }

        // make renderer function
        out.println("function acl" + repository + "RenderFunction(cell, prefix, index, object)");
        out.println("{");
        out.println("cell.innerHTML= '" + getHtmlRowInner(request, dynamicTableName, repository) + "';\n");
        out.println("document.getElementById(prefix + 'AccessType').value = object.accessType;\n");

        if (!repository.equals(ContentRepository.USERS) && !repository.equals(ContentRepository.USER_ROLES)) {
            out.println("document.getElementById(prefix + 'AccessRight').value = object.accessRight;\n");
        }

        out.println("}");

        // create the dynamicTable
        out.println(dynamicTableName
            + " = new MgnlDynamicTable('"
            + tableName
            + "',document.mgnlFormMain."
            + hiddenFieldName
            + ", aclGetNewPermissionObject, aclGetPermissionObject, acl"
            + repository
            + "RenderFunction, null);");

        // add existing acls to table (by js, so the same mechanism as at
        // adding rows can be used)
        addExistingAclToTable(out, role, dynamicTableName, repository);

        out.println("</script>");
    }

    /**
     * @param out
     * @param role
     */
    private void addExistingAclToTable(PrintWriter out, Content role, String dynamicTableName, String repository) {
        boolean noAcl = false;
        try {
            Content acl = role.getContent("acl_" + repository);
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
                    String path = c.getNodeData("path").getString();
                    String accessRight = c.getNodeData("permissions").getString();
                    String accessType;

                    if (path.indexOf("/*") == -1) {
                        // access to self and subs, skip next (which is same
                        // with /*)
                        skipNext = true;
                        accessType = "self";
                    }
                    else {
                        if (path.equals("/*")) {
                            path = "/";
                            accessType = "self";
                        }
                        else {
                            path = path.substring(0, path.lastIndexOf("/*"));
                            accessType = "sub";
                        }
                    }

                    out.println(dynamicTableName
                        + ".add({accessRight:"
                        + accessRight
                        + ",accessType:'"
                        + accessType
                        + "',path:'"
                        + path
                        + "'});");
                }
            }
        }
        catch (Exception e) {
            noAcl = true;
        }
        if (noAcl) {
            out.println(dynamicTableName + ".addNew();");
        }
    }

    /**
     * @param request
     * @return
     */
    private Select getReposiotySelect(HttpServletRequest request) {
        Select repositorySelect = new Select();
        repositorySelect.setName("aclRepository");
        repositorySelect.setCssClass("mgnlDialogControlSelect");
        repositorySelect.setEvent("onchange", "aclChangeRepository(this.value)");
        repositorySelect.setSaveInfo(false);
        repositorySelect.setValue(ContentRepository.WEBSITE);

        // loop through the repositories
        for (int i = 0; i < ContentRepository.ALL_REPOSITORIES.length; i++) {
            String name = ContentRepository.ALL_REPOSITORIES[i];
            String label = MessagesManager.get(request, "repository." + name);
            repositorySelect.setOptions(label, name);
        }
        return repositorySelect;
    }
}