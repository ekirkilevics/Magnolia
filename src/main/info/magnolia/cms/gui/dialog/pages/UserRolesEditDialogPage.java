package info.magnolia.cms.gui.dialog.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.control.Save;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogButton;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogEdit;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogInclude;
import info.magnolia.cms.gui.dialog.DialogStatic;
import info.magnolia.cms.gui.dialog.DialogTab;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.servlets.BasePageServlet;
import info.magnolia.cms.util.Path;
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

    static final String NODE_ACL = "acl_website";

    /**
     * @see info.magnolia.cms.servlets.BasePageServlet#draw(HttpServletRequest, HttpServletResponse)
     */
    public void draw(HttpServletRequest request, HttpServletResponse response) throws IOException, RepositoryException {
        PrintWriter out = response.getWriter();

        MultipartForm form = Resource.getPostedForm(request);
        boolean drawDialog = true;

        String path = "";
        String nodeCollectionName = "";
        String nodeName = "";
        String paragraph = "";
        String richE = "";
        String richEPaste = "";
        String repository = ContentRepository.WEBSITE;

        if (form != null) {
            path = form.getParameter("mgnlPath");
            repository = form.getParameter("mgnlRepository");
        }
        else {
            path = request.getParameter("mgnlPath");
            repository = request.getParameter("mgnlRepository");
        }

        HierarchyManager hm = new HierarchyManager(request);
        boolean create = false;
        if (path.equals(""))
            create = true;

        try {
            Session t = SessionAccessControl.getSession(request, repository);
            Node rootNode = t.getRootNode();
            hm.init(rootNode);
        }
        catch (Exception e) {
        }

        Content role = null;
        if (!create) {
            try {
                role = hm.getPage(path);
            }
            catch (RepositoryException re) {
                re.printStackTrace();
            }
        }

        if (form != null) {
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

            // ######################
            // # acl
            // ######################
            // remove existing
            try {
                role.deleteContentNode(NODE_ACL);
            }
            catch (RepositoryException re) {
            }
            // rewrite
            try {
                ContentNode acl = role.createContentNode(NODE_ACL);
                String aclValueStr = form.getParameter("aclList");
                if (aclValueStr != null && !aclValueStr.equals("")) {
                    String[] aclValue = aclValueStr.split(";");
                    for (int i = 0; i < aclValue.length; i++) {
                        String[] currentAclValue = aclValue[i].split(",");
                        String currentPath = currentAclValue[0];
                        long currentAccessRight = Long.parseLong(currentAclValue[1]);
                        String currentAccessType = currentAclValue[2];

                        if (currentPath.equals("/")) {
                            // needs only one entry: "/*"
                            currentAccessType = "sub";
                            currentPath = "";
                        }

                        if (currentAccessType.equals("self")) {
                            try {
                                String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0");
                                ContentNode r = acl.createContentNode(newLabel);
                                r.createNodeData("path").setValue(currentPath);
                                r.createNodeData("permissions").setValue(currentAccessRight);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0");
                            ContentNode r = acl.createContentNode(newLabel);
                            r.createNodeData("path").setValue(currentPath + "/*");
                            r.createNodeData("permissions").setValue(currentAccessRight);
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

            out.println("<html>");
            out.println(new Sources(request.getContextPath()).getHtmlJs());
            out.println("<script type=\"text/javascript\">");
            out.println("opener.mgnlTree.refresh();");
            out.println("window.close();");
            out.println("</script></html>");
            drawDialog = false;
        }
        else {
            nodeCollectionName = request.getParameter("mgnlNodeCollection");
            nodeName = request.getParameter("mgnlNode");
            paragraph = request.getParameter("mgnlParagraph");
            richE = request.getParameter("mgnlRichE");
            richEPaste = request.getParameter("mgnlRichEPaste");
        }

        if (drawDialog) {

            DialogDialog dialog = DialogFactory.getDialogDialogInstance(request, response, role, null);
            dialog.setConfig("path", path);
            dialog.setConfig("nodeCollection", nodeCollectionName);
            dialog.setConfig("node", nodeName);
            dialog.setConfig("paragraph", paragraph);
            dialog.setConfig("richE", richE);
            dialog.setConfig("richEPaste", richEPaste);
            dialog.setConfig("repository", repository);
            dialog.setJavascriptSources("/admindocroot/js/dialogs/acl.js");

            dialog.setConfig("height", 400);

            if (create)
                dialog.setLabel("Create new role");
            else
                dialog.setLabel("Edit role");
            // dialog.setConfig("saveLabel","OK");

            DialogTab tab0 = dialog.addTab();
            tab0.setLabel("Properties");

            DialogTab tab1 = dialog.addTab("Access control list");

            DialogStatic spacer = DialogFactory.getDialogStaticInstance(request, response, null, null);
            spacer.setConfig("line", false);

            if (!create) {
                DialogStatic name = DialogFactory.getDialogStaticInstance(request, response, null, null);
                // name.setConfig("line",false);
                name.setLabel("<strong>Role name</strong>");
                name.setValue("<strong>" + role.getName() + "</strong>");
                tab0.addSub(name);
            }
            else {
                DialogEdit name = DialogFactory.getDialogEditInstance(request, response, null, null);
                name.setName("name");
                name.setLabel("<strong>Role name</strong>");
                name.setSaveInfo(false);
                name.setDescription("Legal characters: a-z, 0-9, _ (underscore), - (divis)");
                tab0.addSub(name);
            }

            tab0.addSub(spacer);

            DialogEdit title = DialogFactory.getDialogEditInstance(request, response, role, null);
            title.setName("title");
            title.setLabel("Full name");
            if (create) {
                title.setConfig("onchange", "mgnlAclSetName(this.value);");
            }
            tab0.addSub(title);

            tab0.addSub(spacer);

            DialogEdit desc = DialogFactory.getDialogEditInstance(request, response, role, null);
            desc.setName("description");
            desc.setLabel("Description");
            desc.setConfig("rows", 6);
            tab0.addSub(desc);

            DialogInclude acl = DialogFactory.getDialogIncludeInstance(request, response, role, null);
            acl.setBoxType(DialogBox.BOXTYPE_1COL);
            acl.setName("aclRolesRepository");
            acl.setConfig("file", "/admintemplates/adminCentral/dialogs/userRolesEdit/includeAcl.jsp");
            tab1.addSub(acl);

            DialogButton add = DialogFactory.getDialogButtonInstance(request, response, null, null);
            add.setBoxType(DialogBox.BOXTYPE_1COL);
            add.setConfig("buttonLabel", "Add");
            add.setConfig("onclick", "mgnlAclAdd(false,-1);");
            tab1.addSub(add);

            dialog.setConfig("saveOnclick", "mgnlAclFormSubmit(false);");

            dialog.drawHtml(out);
        }

    }
}
