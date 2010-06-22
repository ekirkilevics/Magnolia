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
package info.magnolia.module.rest.json;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.trees.WebsiteTreeHandler;
import info.magnolia.module.rest.dialogx.Dialog;
import info.magnolia.module.rest.dialogx.DialogItemFactory;
import info.magnolia.module.rest.dialogx.DialogRegistry;
import info.magnolia.module.rest.dialogx.ValidationResult;
import info.magnolia.module.rest.tree.WebsitePage;
import info.magnolia.module.rest.tree.WebsitePageList;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Path("/website")
public class WebsiteJsonEndpoint {

    // We need to decide on suitable verbs for activate, deactive, move, copy and so on..

    @GET
    public WebsitePageList getRootNode() throws RepositoryException {
        return readRepository("/");
    }

    @GET
    @Path("{path:(.)*}")
    public WebsitePageList getNode(@PathParam("path") String path) throws RepositoryException {

        if (path.equals("mock")) {
            WebsitePageList pages = new WebsitePageList();
            pages.add(createMockPage("news", "News Desk", true));
            pages.add(createMockPage("about", "About", false));
            return pages;
        } else if (path.equals("mock/news")) {
            WebsitePageList pages = new WebsitePageList();
            pages.add(createMockPage("merger", "QWE merges with RTY", false));
            pages.add(createMockPage("hiring", "New position available", false));
            return pages;
        } else if (path.equals("mock/news/merger")) {
            return new WebsitePageList();
        } else if (path.equals("mock/news/hiring")) {
            return new WebsitePageList();
        }

        return readRepository("/" + path);
    }

    private static class WebsiteAccessor extends WebsiteTreeHandler {

        public WebsiteAccessor(String name, HttpServletRequest request, HttpServletResponse response) {
            super(name, request, response);
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    // This should be PUT... but we use POST for now since IPSecurity blocks PUT
    @POST
    @Path("{path:(.)*}/create")
    public WebsitePage create(@PathParam("path") String path) {

        WebsiteAccessor website = new WebsiteAccessor("website", null, null);

        website.setCreateItemType("mgnl:content");
        website.setPath("/" + path);
        website.createNode();

        return new WebsitePage();
    }

    @POST
    @Path("{path:(.)*}/update")
    public WebsitePage update(@PathParam("path") String path, WebsitePage page) {

        // read

        // set name, title and template

        // save

        // return

        return new WebsitePage();
    }

    @POST
    @Path("{path:(.)*}/delete")
    public WebsitePage delete(@PathParam("path") String path) throws Exception {

        WebsiteAccessor website = new WebsiteAccessor("website", null, null);

        path = "/" + path;

        // This is AdminTreeMVCHandler.createNode(String path)...
        String parentPath = StringUtils.substringBeforeLast(path, "/"); //$NON-NLS-1$
        String label = StringUtils.substringAfterLast(path, "/"); //$NON-NLS-1$
        // ...with this fix added for the simple case '/untitled'
        if (StringUtils.isEmpty(parentPath))
            parentPath = "/";
        website.deleteNode(parentPath, label);

        return new WebsitePage();
    }

    @POST
    @Path("{path:(.)*}/edit")
    public Dialog edit(@PathParam("path") String path, @QueryParam("dialogName") String dialogName) throws RepositoryException {

        path = "/" + path;

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        if (!hierarchyManager.isExist(path))
            return null;

        Content storageNode = hierarchyManager.getContent(path);

        Content dialogConfigNode = DialogRegistry.getInstance().getDialogConfigNode(dialogName);

        Dialog dialog = DialogItemFactory.getInstance().createDialog(dialogConfigNode);

        dialog.bind(storageNode);

        return dialog;
    }

    @POST
    @Path("{path:(.)*}/save")
    public ValidationResult save(@PathParam("path") String path, @QueryParam("dialogName") String dialogName, @Context UriInfo uriInfo) throws Exception {

        path = "/" + path;

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        if (!hierarchyManager.isExist(path))
            return null;

        Content storageNode = hierarchyManager.getContent(path);

        Content dialogConfigNode = DialogRegistry.getInstance().getDialogConfigNode(dialogName);

        Dialog dialog = DialogItemFactory.getInstance().createDialog(dialogConfigNode);

        dialog.bind(storageNode);
        dialog.bind(uriInfo.getQueryParameters());

        ValidationResult validationResult = new ValidationResult();

        dialog.validate(validationResult);

        if (validationResult.isSuccess()) {
            dialog.save(storageNode);
            synchronized(ExclusiveWrite.getInstance()) {
                storageNode.save();
            }
        }

        // was it successful ?

        return validationResult;
    }

    private WebsitePage createMockPage(String name, String title, boolean hasChildren) {

        List<String> templates = new ArrayList<String>();
        templates.add("main");
        templates.add("section");

        WebsitePage page = new WebsitePage();
        page.setName(name);
        page.setTitle(title);
        page.setLastModified(new Date());
        page.setStatus("active");
        page.setTemplate("main");
        page.setHasChildren(hasChildren);
        page.setAvailableTemplates(templates);
        return page;
    }

    private WebsitePageList readRepository(String path) throws RepositoryException {

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        if (!hierarchyManager.isExist(path))
            return null;

        Content parentNode;
            parentNode = hierarchyManager.getContent(path);

        WebsitePageList pages = new WebsitePageList();

        Iterator<Content> contentIterator = parentNode.getChildren(ItemType.CONTENT).iterator();

        while (contentIterator.hasNext()) {
            Content content = contentIterator.next();

            boolean permissionWrite = content.isGranted(info.magnolia.cms.security.Permission.WRITE);
            boolean isActivated = content.getMetaData().getIsActivated();
            boolean hasChildren = !content.getChildren(ItemType.CONTENT).isEmpty();

            String title = content.getNodeData("title").getString();

            WebsitePage page = new WebsitePage();
            page.setName(content.getName());
            page.setPath(content.getHandle());
            page.setHasChildren(hasChildren);
            page.setStatus(isActivated?"activated":"modified");
            page.setTemplate(getTemplateName(content));
            page.setTitle(title);
            page.setAvailableTemplates(getAvailableTemplates(content));
            pages.add(page);
        }

        return pages;
    }

    private List<String> getAvailableTemplates(Content content) {
        TemplateManager templateManager = TemplateManager.getInstance();
        Iterator<Template> templates = templateManager.getAvailableTemplates(content);
        ArrayList<String> list = new ArrayList<String>();
        while (templates.hasNext()) {
            Template template = templates.next();
            list.add(template.getI18NTitle());
        }
        return list;
    }

    public String getTemplateName(Content content) {
        TemplateManager templateManager = TemplateManager.getInstance();
        String templateName = content.getMetaData().getTemplate();
        Template template = templateManager.getTemplateDefinition(templateName);
        return template != null ? template.getI18NTitle() : StringUtils.defaultString(templateName);
    }
}
