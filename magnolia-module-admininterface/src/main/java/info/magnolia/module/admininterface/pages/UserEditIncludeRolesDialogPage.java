package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.SimplePageMVCHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class UserEditIncludeRolesDialogPage extends SimplePageMVCHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(UserEditIncludeRolesDialogPage.class);

    public UserEditIncludeRolesDialogPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static String getHtmlRowInner(HttpServletRequest request) {
        boolean small = true;
        Messages msgs = MessagesManager.getMessages();

        Button choose = new Button();
        choose.setLabel(msgs.get("buttons.choose")); //$NON-NLS-1$
        choose.setOnclick("mgnlAclChoose('+index+',\\\'" + ContentRepository.USER_ROLES + "\\\');"); //$NON-NLS-1$ //$NON-NLS-2$

        choose.setSmall(small);

        Button delete = new Button();
        delete.setLabel(msgs.get("buttons.delete")); //$NON-NLS-1$
        delete.setOnclick("mgnlAclDelete('+index+');"); //$NON-NLS-1$
        delete.setSmall(small);

        StringBuffer html = new StringBuffer();
        // set as table since ie/win does not support setting of innerHTML of a tr
        html.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr style=\"height:20px\">"); //$NON-NLS-1$

        // todo: show name instead of path again (adapt linkBrowser.jsp, resp. Tree.java)
        // html.append("<td id=\"acl'+index+'TdName\" width=\"100%\" class=\"mgnlDialogAcl\">'+name+'</td>");

        html.append("<td width=\"100%\" class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDITWITHBUTTON
            + "\"><input name=\"acl'+index+'Path\" id=\"acl'+index+'Path\" class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_EDIT
            + "\" type=\"text\" style=\"width:100%;\" value=\"'+path+'\" /></td>"); //$NON-NLS-1$
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
     * @see info.magnolia.cms.servlets.BasePageServlet#render(HttpServletRequest, HttpServletResponse)
     */
    protected void render(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        DialogSuper dialogControl = (DialogSuper) request.getAttribute("dialogObject"); //$NON-NLS-1$
        Content user = dialogControl.getWebsiteNode();

        out.println(new Hidden("aclList", StringUtils.EMPTY, false).getHtml()); //$NON-NLS-1$

        out.println("<table id=\"aclTable\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"></table>"); //$NON-NLS-1$

        out.println("<script type=\"text/javascript\">"); //$NON-NLS-1$
        out.println("function mgnlAclGetHtmlRow(index,path,name)"); //$NON-NLS-1$
        out.println("{"); //$NON-NLS-1$
        out.println("return '" + getHtmlRowInner(request) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("}"); //$NON-NLS-1$

        // add existing acls to table (by js, so the same mechanism as at adding rows can be used)
        try {
            Content acl = user.getContent("roles"); //$NON-NLS-1$
            Iterator it = acl.getChildren().iterator();
            while (it.hasNext()) {
                Content c = (Content) it.next();
                String path = c.getNodeData("path").getString(); //$NON-NLS-1$
                String name = StringUtils.EMPTY;

                HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.USER_ROLES);
                Content role = null;
                try {
                    role = hm.getContent(path);
                    name = role.getTitle();
                }
                catch (RepositoryException re) {
                    if (log.isDebugEnabled()) {
                        log.debug("Repository exception: " + re.getMessage(), re); //$NON-NLS-1$
                    }
                }

                out.println("mgnlAclAdd(true,-1,'" + path + "','" + name + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        catch (Exception e) {
            out.println("mgnlAclAdd(true,-1);"); //$NON-NLS-1$
        }

        out.println("</script>"); //$NON-NLS-1$

    }
}
