package info.magnolia.cms.gui.dialog.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.servlets.BasePageServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class UserEditIncludeRolesDialogPage extends BasePageServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String getHtmlRowInner(HttpServletRequest request) {
        boolean small = true;
        Messages msgs = MessagesManager.getMessages(request);

        Button choose = new Button();
        choose.setLabel(msgs.get("buttons.choose"));
        choose.setOnclick("mgnlAclChoose('+index+',\\\'" + ContentRepository.USER_ROLES + "\\\');");

        choose.setSmall(small);

        Button delete = new Button();
        delete.setLabel(msgs.get("buttons.delete"));
        delete.setOnclick("mgnlAclDelete('+index+');");
        delete.setSmall(small);

        StringBuffer html = new StringBuffer();
        // set as table since ie/win does not support setting of innerHTML of a tr
        html.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr style=\"height:20px\">");

        // todo: show name instead of path again (adapt linkBrowser.jsp, resp. Tree.java)
        // html.append("<td id=\"acl'+index+'TdName\" width=\"100%\" class=\"mgnlDialogAcl\">'+name+'</td>");

        html.append("<td width=\"100%\" class=\""
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\"><input name=\"acl'+index+'Path\" id=\"acl'+index+'Path\" class=\""
            + CssConstants.CSSCLASS_EDIT
            + "\" type=\"text\" style=\"width:100%;\" value=\"'+path+'\" /></td>");
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
        Content user = dialogControl.getWebsiteNode();

        out.println(new Hidden("aclList", "", false).getHtml());

        out.println("<table id=\"aclTable\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"></table>");

        out.println("<script type=\"text/javascript\">");
        out.println("function mgnlAclGetHtmlRow(index,path,name)");
        out.println("{");
        out.println("return '" + getHtmlRowInner(request) + "'");
        out.println("}");

        // add existing acls to table (by js, so the same mechanism as at adding rows can be used)
        try {
            Content acl = user.getContent("roles");
            Iterator it = acl.getChildren().iterator();
            while (it.hasNext()) {
                Content c = (Content) it.next();
                String path = c.getNodeData("path").getString();
                String name = "";
                try {
                    HierarchyManager hm = new HierarchyManager(request);

                    try {
                        Session t = SessionAccessControl.getSession(request, ContentRepository.USER_ROLES);
                        Node rootNode = t.getRootNode();
                        hm.init(rootNode);
                    }
                    catch (Exception e) {
                    }
                    Content role = null;
                    try {
                        role = hm.getContent(path);
                        name = role.getTitle();
                    }
                    catch (RepositoryException re) {
                    }
                }
                catch (Exception e) {
                }

                out.println("mgnlAclAdd(true,-1,'" + path + "','" + name + "');");
            }
        }
        catch (Exception e) {
            out.println("mgnlAclAdd(true,-1);");
        }

        out.println("</script>");

    }
}
