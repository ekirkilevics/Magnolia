package info.magnolia.cms.gui.dialog.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.ContextMessages;
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
    static final long PERMISSION_ALL = Permission.ALL;

    static final long PERMISSION_READ = Permission.READ;

    static final long PERMISSION_NO = 0;

    private static final String getHtmlRowInner(HttpServletRequest request) {
        boolean small = true;
        Messages msgs = MessagesManager.getMessages(request);

        Select accessRight = new Select();
        accessRight.setSaveInfo(false);
        accessRight.setName("acl'+index+'AccessRight");
        accessRight.setCssClass("mgnlDialogControlSelect");
        accessRight.setOptions(msgs.get("roles.permission.readWrite"), Long.toString(PERMISSION_ALL));
        accessRight.setOptions(msgs.get("roles.permission.readOnly"), Long.toString(PERMISSION_READ));
        accessRight.setOptions(msgs.get("roles.permission.deny"), Long.toString(PERMISSION_NO));

        Select accessType = new Select();
        accessType.setSaveInfo(false);
        accessType.setName("acl'+index+'AccessType");
        accessType.setCssClass("mgnlDialogControlSelect");
        accessType.setOptions(msgs.get("roles.edit.thisAndSubPages"), "self");
        accessType.setOptions(msgs.get("roles.edit.subPages"), "sub");

        Edit path = new Edit();
        path.setSaveInfo(false);
        path.setName("acl'+index+'Path");
        path.setValue("'+path+'");
        path.setCssClass(CssConstants.CSSCLASS_EDIT);
        path.setCssStyles("width", "100%");

        Button choose = new Button();
        choose.setLabel(msgs.get("buttons.choose"));
        choose.setOnclick("mgnlAclChoose('+index+',\\'" + ContentRepository.WEBSITE + "\\');");
        choose.setSmall(small);

        Button delete = new Button();
        delete.setLabel(msgs.get("buttons.delete"));
        delete.setOnclick("mgnlAclDelete('+index+');");
        delete.setSmall(small);

        StringBuffer html = new StringBuffer();
        // set as table since ie/win does not support setting of innerHTML of a tr
        html.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
        html.append("<td width=\"1\" class=\""
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">"
            + accessRight.getHtml()
            + "</td>");
        html.append("<td width=\"1\" class=\"mgnlDialogBoxInput\">&nbsp;</td>");
        html.append("<td width=\"1\" class=\""
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">"
            + accessType.getHtml()
            + "</td>");
        html.append("<td width=\"1\">&nbsp;</td>");
        html.append("<td width=\"100%\"class=\""
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">"
            + path.getHtml()
            + "</td>");
        html.append("<td width=\"1\">&nbsp;</td>");
        html.append("<td width=\"1\" class=\""
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\">"
            + choose.getHtml()
            + "</td>");
        html.append("<td width=\"1\">&nbsp;</td>");
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

        DialogSuper dialogControl = (DialogSuper) request.getAttribute("dialogObject");
        Content role = dialogControl.getWebsiteNode();

        out.println(new Hidden("aclList", "", false).getHtml());

        out.println("<table id=\"aclTable\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"></table>");

        out.println("<script type=\"text/javascript\">");
        out.println("function mgnlAclGetHtmlRow(index,path,name)");
        out.println("{");
        out.println("return '" + getHtmlRowInner(request) + "'\n");
        out.println("}");

        // add existing acls to table (by js, so the same mechanism as at adding rows can be used)
        boolean noAcl = false;
        try {
            ContentNode acl = role.getContentNode("acl_website");
            if (acl.getChildren().size() == 0)
                noAcl = true;
            Iterator it = acl.getChildren().iterator();
            boolean skipNext = false;
            while (it.hasNext()) {
                ContentNode c = (ContentNode) it.next();

                if (skipNext) {
                    skipNext = false;
                }
                else {
                    String path = c.getNodeData("path").getString();
                    String accessRight = c.getNodeData("permissions").getString();
                    String accessType;

                    if (path.indexOf("/*") == -1) {
                        // access to self and subs, skip next (which is same with /*)
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

                    out.println("mgnlAclAdd(false,-1,'" + path + "','','" + accessRight + "','" + accessType + "');");
                }
            }
        }
        catch (Exception e) {
            noAcl = true;
        }
        if (noAcl)
            out.println("mgnlAclAdd(false,-1);");

        out.println("</script>");

    }
}
