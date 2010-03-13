/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogButton;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.admininterface.AdminInterfaceModule;
import info.magnolia.module.admininterface.config.AclTypeConfiguration;
import info.magnolia.module.admininterface.config.PermissionConfiguration;
import info.magnolia.module.admininterface.config.RepositoryConfiguration;
import info.magnolia.module.admininterface.config.SecurityConfiguration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;


/**
 * @author Fabrizio Giustina
 * @version $Id:RolesACLPage.java 2516 2006-03-31 13:08:03Z philipp $
 */
public class ACLSDialogControl extends DialogBox {

    private static final String CSS_ACL_DIV = "aclDynamicTable"; //$NON-NLS-1$

    private SecurityConfiguration securityConf = AdminInterfaceModule.getInstance().getSecurityConfiguration();

    private static String getHtmlRowInner(String dynamicTable, RepositoryConfiguration repoConf) {
        boolean small = true;
        Messages msgs = MessagesManager.getMessages();

        Select accessRight = new Select();
        accessRight.setSaveInfo(false);
        accessRight.setName("'+prefix+'AccessRight"); //$NON-NLS-1$
        accessRight.setCssClass("mgnlDialogControlSelect"); //$NON-NLS-1$

        for (Iterator iter = repoConf.getPermissions().iterator(); iter.hasNext();) {
            PermissionConfiguration permission = (PermissionConfiguration) iter.next();
            accessRight.setOptions(escapeJs(permission.getI18nLabel()), Long.toString(permission.getValue())); //$NON-NLS-1$
        }

        accessRight.setValue("' + object.accessRight + '"); //$NON-NLS-1$

        Select accessType = new Select();
        accessType.setSaveInfo(false);
        accessType.setName("'+prefix+'AccessType"); //$NON-NLS-1$
        accessType.setCssClass("mgnlDialogControlSelect"); //$NON-NLS-1$

        for (Iterator iter = repoConf.getAclTypes().iterator(); iter.hasNext();) {
            AclTypeConfiguration patternType = (AclTypeConfiguration) iter.next();
            accessType.setOptions(escapeJs(patternType.getI18nLabel()), String.valueOf(patternType.getType())); //$NON-NLS-1$
        }

        accessType.setValue("' + object.accessType + '"); //$NON-NLS-1$

        Edit path = new Edit();
        path.setSaveInfo(false);
        path.setName("'+prefix+'Path"); //$NON-NLS-1$
        path.setValue("'+object.path+'"); //$NON-NLS-1$
        path.setCssClass(CssConstants.CSSCLASS_EDIT);
        path.setCssStyles("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        Button choose = null;
        if(repoConf.isChooseButton()){
            choose = new Button();
            choose.setLabel(escapeJs(msgs.get("buttons.choose"))); //$NON-NLS-1$
            choose.setOnclick("aclChoose(\\''+prefix+'\\',\\'" + repoConf.getName() + "\\');"); //$NON-NLS-1$ //$NON-NLS-2$
            choose.setSmall(small);
        }

        Button delete = new Button();
        delete.setLabel(escapeJs(msgs.get("buttons.delete"))); //$NON-NLS-1$
        delete.setOnclick(dynamicTable + ".del('+index+');"); //$NON-NLS-1$
        delete.setSmall(small);

        StringBuffer html = new StringBuffer();
        // set as table since ie/win does not support setting of innerHTML of a
        // tr
        html.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"" + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">").append(accessRight.getHtml()).append("</td>"); //$NON-NLS-1$
        html.append("<td width=\"1\" class=\"mgnlDialogBoxInput\"></td>"); //$NON-NLS-1$

        // do we add the type selection dropdown?
        if(!repoConf.getAclTypes().isEmpty()){
            html.append("<td width=\"1\" class=\"" + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">").append(accessType.getHtml()).append("</td>"); //$NON-NLS-1$
            html.append("<td width=\"1\"></td>"); //$NON-NLS-1$
        }
        else {
            html.append("<input type=\"hidden\" id=\"' + prefix + 'AccessType\" name=\"' + prefix + 'AccessType\" value=\"sub\"/>"); //$NON-NLS-1$
        }

        html.append("<td width=\"100%\"class=\"" + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">").append(path.getHtml()).append("</td>"); //$NON-NLS-1$
        html.append("<td width=\"1\"></td>"); //$NON-NLS-1$

        if (choose != null) {
            html.append("<td width=\"1\" class=\"" + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">").append(choose.getHtml()).append("</td>"); //$NON-NLS-1$
            html.append("<td width=\"1\"></td>"); //$NON-NLS-1$
        }

        html.append("<td width=\"1\" class=\"" + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">").append(delete.getHtml()).append("</td>"); //$NON-NLS-1$
        html.append("</tr></table>"); //$NON-NLS-1$

        return html.toString();
    }

    public void drawHtml(Writer w) throws IOException {
        PrintWriter out = (PrintWriter) w;
        this.drawHtmlPre(out);
        renderACLS(out);
        this.drawHtmlPost(out);

    }

    protected void renderACLS(PrintWriter out) throws IOException {
        Messages msgs = MessagesManager.getMessages();
        Content role = getStorageNode();
        HttpServletRequest request = this.getRequest();
        HttpServletResponse response = this.getResponse();

        // select the repository
        Select repositorySelect = getRepositorySelect();

        out.print(repositorySelect.getHtml());
        out.print("<p><p/>"); //$NON-NLS-1$

        // process with the real existing repositories
        for (Iterator iter = securityConf.getVisibleRepositories().iterator(); iter.hasNext();) {
            RepositoryConfiguration repositoryConf = (RepositoryConfiguration) iter.next();
            try {
                writeRepositoryTable(request, response, msgs, out, role, repositoryConf);
            }
            catch (RepositoryException e) {
                throw new RuntimeException("can't list ", e);
            }
        }

        // out.print("<p>&nbsp;<p>&nbsp;<p>&nbsp;<input type=\"button\" onclick=\"aclChangeRepository('website')\">");
        out.println("<script type=\"text/javascript\">aclChangeRepository('website');</script>"); //$NON-NLS-1$
    }

    /**
     * @param request
     * @param out
     * @param role
     * @throws RepositoryException
     * @throws IOException
     */
    protected void writeRepositoryTable(HttpServletRequest request,  HttpServletResponse response, Messages msgs,
        PrintWriter out, Content role, RepositoryConfiguration repoConf) throws RepositoryException, IOException {
        String tableName = "acl" + repoConf.getName() + "Table"; //$NON-NLS-1$ //$NON-NLS-2$
        String dynamicTableName = "acl" + repoConf.getName() + "DynamicTable"; //$NON-NLS-1$ //$NON-NLS-2$
        String hiddenFieldName = "acl" + repoConf.getName() + "List"; //$NON-NLS-1$ //$NON-NLS-2$

        out.println("<div id=\"acl" + repoConf.getName() + "Div\" class=\"" + CSS_ACL_DIV + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
        out.println("aclRepositories[aclRepositories.length]= '" + repoConf.getName() + "';"); //$NON-NLS-1$ //$NON-NLS-2$

        // make renderer function
        out.println("function acl" + repoConf.getName() + "RenderFunction(cell, prefix, index, object)"); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("{"); //$NON-NLS-1$

        // get some debug informations
        out.println("mgnlDebug('acl" + repoConf.getName() + "RenderFunction: prefix = ' + prefix, 'acl', object)");
        out.println("cell.innerHTML= '" + getHtmlRowInner(dynamicTableName, repoConf) + "';\n"); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("document.getElementById(prefix + 'AccessType').value = object.accessType;\n"); //$NON-NLS-1$
        out.println("document.getElementById(prefix + 'AccessRight').value = object.accessRight;\n"); //$NON-NLS-1$

        out.println("}"); //$NON-NLS-1$

        // create the dynamicTable
        out.println(dynamicTableName + " = new MgnlDynamicTable('" //$NON-NLS-1$
            + tableName
            + "',document.getElementById('mgnlFormMain')." //$NON-NLS-1$
            + hiddenFieldName
            + ", aclGetNewPermissionObject, aclGetPermissionObject, acl" //$NON-NLS-1$
            + repoConf.getName()
            + "RenderFunction, null);"); //$NON-NLS-1$

        // add existing acls to table (by js, so the same mechanism as at
        // adding rows can be used)
        addExistingAclToTable(out, role, dynamicTableName, repoConf);

        out.println("</script>"); //$NON-NLS-1$
    }

    /**
     * @param out
     * @param role
     */
    private void addExistingAclToTable(PrintWriter out, Content role, String dynamicTableName,
        RepositoryConfiguration repoConf) {
        // keeps acls per path
        ACLS acls = new ACLS();

        Content aclsNode = ContentUtil.getContent(role, "acl_" + repoConf.getName()); //$NON-NLS-1$
        if (aclsNode == null || aclsNode.getChildren().size() == 0) {
            out.println(dynamicTableName + ".addNew();"); //$NON-NLS-1$
            return;
        }

        Iterator it = aclsNode.getChildren().iterator();
        while (it.hasNext()) {
            Content c = (Content) it.next();
            String path = c.getNodeData("path").getString(); //$NON-NLS-1$
            String accessRight = c.getNodeData("permissions").getString(); //$NON-NLS-1$
            acls.register(path, Integer.valueOf(accessRight).intValue(), repoConf);
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

    private Select getRepositorySelect() {
        Select repositorySelect = new Select();
        repositorySelect.setName("aclRepository"); //$NON-NLS-1$
        repositorySelect.setCssClass("mgnlDialogControlSelect"); //$NON-NLS-1$
        repositorySelect.setEvent("onchange", "aclChangeRepository(this.value)"); //$NON-NLS-1$ //$NON-NLS-2$
        repositorySelect.setSaveInfo(false);
        repositorySelect.setValue(ContentRepository.WEBSITE);

        for (Iterator iter = securityConf.getVisibleRepositories().iterator(); iter.hasNext();) {
            RepositoryConfiguration repoConf = (RepositoryConfiguration) iter.next();
            repositorySelect.setOptions(repoConf.getI18nLabel(), repoConf.getName());
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
    protected class ACL {

        int type = 0;

        String path;

        int accessRight;

        void registerEntry(String path) {
            if (path.equals("/*")) {
                type = AclTypeConfiguration.TYPE_ALL;
            }
            else if (path.endsWith("/*")) {
                type = type | AclTypeConfiguration.TYPE_SUBS;
            }
            else {
                type = type | AclTypeConfiguration.TYPE_THIS;
            }
        }
    }

    /**
     * Used to create the gui values out of the entries in the repository
     * @author Philipp Bracher
     * @version $Revision$ ($Author$)
     */
    protected class ACLS extends ListOrderedMap {

        /**
         * Register an entry
         * @param path the not cleaned path
         * @param accessRight the access right
         */
        void register(String path, int accessRight, RepositoryConfiguration repoConf) {
            String cleanPath = repoConf.toViewPattern(path);

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
