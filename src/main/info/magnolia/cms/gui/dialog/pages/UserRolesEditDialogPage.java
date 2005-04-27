package info.magnolia.cms.gui.dialog.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.control.Save;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogEdit;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogInclude;
import info.magnolia.cms.gui.dialog.DialogStatic;
import info.magnolia.cms.gui.dialog.DialogTab;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.servlets.BasePageServlet;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class UserRolesEditDialogPage extends BasePageServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    static Logger log = Logger.getLogger("roles dialog");

    // todo: permission global available somewhere
    static final long PERMISSION_ALL = Permission.ALL;

    static final long PERMISSION_READ = Permission.READ;

    static final long PERMISSION_NO = 0;

    // this is not longer true
    // static final String NODE_ACL = "acl_website";

    private class MyDialog {

        private String path = "";

        private String nodeCollectionName = "";

        private String nodeName = "";

        private String paragraph = "";

        private String richE = "";

        private String richEPaste = "";

        private PrintWriter out;

        private Messages msgs;

        private MultipartForm form;

        private HierarchyManager hm;

        private boolean create = false;

        HttpServletRequest request;

        HttpServletResponse response;

        Content role;

        MyDialog(HttpServletRequest request, HttpServletResponse response) throws IOException {
            this.request = request;
            this.response = response;
            this.out = response.getWriter();
            this.msgs = MessagesManager.getMessages(request);
            this.form = Resource.getPostedForm(request);
            this.hm = new HierarchyManager(request);

            if (form != null) {
                path = form.getParameter("mgnlPath");
            }
            else {
                path = request.getParameter("mgnlPath");
            }

            if (path.equals(""))
                create = true;

            try {
                Session t = SessionAccessControl.getSession(request, ContentRepository.USER_ROLES);
                Node rootNode = t.getRootNode();
                hm.init(rootNode);
            }
            catch (Exception e) {
            }

            if (!create) {
                try {
                    role = hm.getContent(path);
                }
                catch (RepositoryException re) {
                    re.printStackTrace();
                }
            }
        }

        public void execute() throws IOException, RepositoryException {

            if (form != null) {

                save();

                out.println("<html>");
                out.println(new Sources(request.getContextPath()).getHtmlJs());
                out.println("<script type=\"text/javascript\">");
                out.println("opener.mgnlTree.refresh();");
                out.println("window.close();");
                out.println("</script></html>");

            }
            else {
                nodeCollectionName = request.getParameter("mgnlNodeCollection");
                nodeName = request.getParameter("mgnlNode");
                paragraph = request.getParameter("mgnlParagraph");
                richE = request.getParameter("mgnlRichE");
                richEPaste = request.getParameter("mgnlRichEPaste");

                DialogDialog dialog = createDialog();
                dialog.drawHtml(out);
            }
        }

        /**
         * @return
         * @throws RepositoryException
         */
        private DialogDialog createDialog() throws RepositoryException {
            DialogDialog dialog = DialogFactory.getDialogDialogInstance(request, response, role, null);
            dialog.setConfig("path", path);
            dialog.setConfig("nodeCollection", nodeCollectionName);
            dialog.setConfig("node", nodeName);
            dialog.setConfig("paragraph", paragraph);
            dialog.setConfig("richE", richE);
            dialog.setConfig("richEPaste", richEPaste);
            dialog.setConfig("repository", ContentRepository.USER_ROLES);
            dialog.setJavascriptSources(request.getContextPath() + "/admindocroot/js/dialogs/DynamicTable.js");
            dialog.setJavascriptSources(request.getContextPath()
                + "/admindocroot/js/dialogs/pages/userRolesEditDialogPage.js");
            dialog.setCssSources(request.getContextPath()
                + "/admindocroot/css/dialogs/pages/userRolesEditDialogPage.css");
            dialog.setConfig("height", 600);

            if (create)
                dialog.setLabel(msgs.get("roles.edit.create"));
            else
                dialog.setLabel(msgs.get("roles.edit.edit"));

            DialogTab tab0 = dialog.addTab(msgs.get("roles.edit.properties"));

            DialogTab tab1 = dialog.addTab(msgs.get("roles.edit.accessControlList"));

            DialogStatic spacer = DialogFactory.getDialogStaticInstance(request, response, null, null);
            spacer.setConfig("line", false);

            if (!create) {
                DialogStatic name = DialogFactory.getDialogStaticInstance(request, response, null, null);
                // name.setConfig("line",false);
                name.setLabel("<strong>" + msgs.get("roles.edit.rolename") + "</strong>");
                name.setValue("<strong>" + role.getName() + "</strong>");
                tab0.addSub(name);
            }
            else {
                DialogEdit name = DialogFactory.getDialogEditInstance(request, response, null, null);
                name.setName("name");
                name.setLabel("<strong>" + msgs.get("roles.edit.rolename") + "</strong>");
                name.setSaveInfo(false);
                name.setDescription("Legal characters: a-z, 0-9, _ (underscore), - (divis)");
                tab0.addSub(name);
            }

            tab0.addSub(spacer);

            DialogEdit title = DialogFactory.getDialogEditInstance(request, response, role, null);
            title.setName("title");
            title.setLabel(msgs.get("roles.edit.fullname"));
            if (create) {
                title.setConfig("onchange", "mgnlAclSetName(this.value);");
            }
            tab0.addSub(title);

            tab0.addSub(spacer);

            DialogEdit desc = DialogFactory.getDialogEditInstance(request, response, role, null);
            desc.setName("description");
            desc.setLabel(msgs.get("roles.edit.description"));
            desc.setConfig("rows", 6);
            tab0.addSub(desc);

            DialogInclude acl = DialogFactory.getDialogIncludeInstance(request, response, role, null);
            acl.setBoxType(DialogBox.BOXTYPE_1COL);
            acl.setName("aclRolesRepository");
            acl.setConfig("file", "/admintemplates/adminCentral/dialogs/userRolesEdit/includeAcl.jsp");
            tab1.addSub(acl);

            dialog.setConfig("saveOnclick", "aclFormSubmit();");
            return dialog;
        }

        /**
         * 
         */
        private void save() {
            // save
            // create new role
            if (create) {
                String name = form.getParameter("name");
                path = "/" + name;
                try {
                    role = hm.createPage("/", name);
                }
                catch (RepositoryException re) {
                    re.printStackTrace();
                }
            }

            // ######################
            // # write (controls with saveInfo (full name, password))
            // ######################
            Save nodeXml = new Save(form, request);
            nodeXml.setPath(path);
            nodeXml.save();

            // for each repository

            for (int x = 0; x < ContentRepository.ALL_REPOSITORIES.length; x++) {
                String repository = ContentRepository.ALL_REPOSITORIES[x];

                // ######################
                // # acl
                // ######################
                // remove existing
                try {
                    role.delete("acl_" + repository);
                }
                catch (RepositoryException re) {
                }
                // rewrite
                try {
                    Content acl = role.createContent("acl_" + repository, ItemType.NT_CONTENTNODE);
                    String aclValueStr = form.getParameter("acl" + repository + "List");
                    if (aclValueStr != null && !aclValueStr.equals("")) {
                        String[] aclEntries = aclValueStr.split(";");
                        for (int i = 0; i < aclEntries.length; i++) {
                            String path = "";
                            long accessRight = 0;
                            String accessType = "";

                            String[] aclValuePairs = aclEntries[i].split(",");
                            for (int j = 0; j < aclValuePairs.length; j++) {
                                String[] aclValuePair = aclValuePairs[j].split(":");
                                String aclName = aclValuePair[0].trim();
                                String aclValue = "";
                                if (aclValuePair.length > 1)
                                    aclValue = aclValuePair[1].trim();

                                if (aclName.equals("path")) {
                                    path = aclValue;
                                }
                                else if (aclName.equals("accessType")) {
                                    accessType = aclValue;
                                }
                                else if (aclName.equals("accessRight")) {
                                    accessRight = Long.parseLong(aclValue);
                                }
                            }

                            if (path.equals("/")) {
                                // needs only one entry: "/*"
                                accessType = "sub";
                                path = "";
                            }

                            if (accessType.equals("self")) {
                                try {
                                    String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0");
                                    Content r = acl.createContent(newLabel, ItemType.NT_CONTENTNODE);
                                    r.createNodeData("path").setValue(path);
                                    r.createNodeData("permissions").setValue(accessRight);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0");
                                Content r = acl.createContent(newLabel, ItemType.NT_CONTENTNODE);
                                r.createNodeData("path").setValue(path + "/*");
                                r.createNodeData("permissions").setValue(accessRight);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    hm.save();
                }
                catch (RepositoryException re) {
                    log.error(re.getMessage(), re);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.servlets.BasePageServlet#draw(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void draw(HttpServletRequest request, HttpServletResponse response) throws IOException,
        RepositoryException {
        // TODO Auto-generated method stub
        MyDialog innerDialog = new MyDialog(request, response);
        innerDialog.execute();
    }
}