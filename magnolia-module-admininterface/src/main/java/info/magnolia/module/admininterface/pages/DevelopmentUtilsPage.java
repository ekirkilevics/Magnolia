/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.files.BasicFileExtractor;
import info.magnolia.module.files.FileExtractor;
import info.magnolia.module.files.ModuleFileExtractorTransformer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utilities that can be used during development.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class DevelopmentUtilsPage extends TemplatedMVCHandler {

    private boolean templates;

    private boolean paragraphs;

    private boolean dialogs;

    private boolean pages;

    private boolean website;

    private boolean users;

    private boolean groups;

    private boolean roles;

    private boolean virtualURIs;

    private String rootdir;

    private String parentpath;

    private String repository;

    private String module;

    /**
     * Logger.
     */
    public static Logger log = LoggerFactory.getLogger(DevelopmentUtilsPage.class);

    /**
     * @param name
     * @param request
     * @param response
     */
    public DevelopmentUtilsPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        rootdir = StringUtils.defaultIfEmpty(NodeDataUtil.getString(
            ContentRepository.CONFIG,
            "/modules/adminInterface/config/developmentUtils/exportpath"), "WEB-INF/bootstrap/common");
        module = StringUtils.defaultIfEmpty(NodeDataUtil.getString(
            ContentRepository.CONFIG,
            "/modules/adminInterface/config/developmentUtils/module"), "templating");
    }

    /**
     * Getter for <code>templates</code>.
     * @return Returns the templates.
     */
    public boolean isTemplates() {
        return this.templates;
    }

    /**
     * Getter for <code>paragraphs</code>.
     * @return Returns the paragraphs.
     */
    public boolean isParagraphs() {
        return this.paragraphs;
    }

    /**
     * Getter for <code>dialogs</code>.
     * @return Returns the dialogs.
     */
    public boolean isDialogs() {
        return this.dialogs;
    }

    /**
     * Getter for <code>pages</code>.
     * @return Returns the pages.
     */
    public boolean isPages() {
        return this.pages;
    }

    /**
     * Setter for <code>pages</code>.
     * @param pages The pages to set.
     */
    public void setPages(boolean pages) {
        this.pages = pages;
    }

    /**
     * Getter for <code>website</code>.
     * @return Returns the website.
     */
    public boolean isWebsite() {
        return this.website;
    }

    /**
     * Getter for <code>users</code>.
     * @return Returns the users.
     */
    public boolean isUsers() {
        return this.users;
    }

    /**
     * Getter for <code>groups</code>.
     * @return Returns the groups.
     */
    public boolean isGroups() {
        return this.groups;
    }

    /**
     * Getter for <code>roles</code>.
     * @return Returns the roles.
     */
    public boolean isRoles() {
        return this.roles;
    }

    /**
     * Getter for <code>rootdir</code>.
     * @return Returns the rootdir.
     */
    public String getRootdir() {
        return this.rootdir;
    }

    /**
     * Getter for <code>parentpath</code>.
     * @return Returns the parentpath.
     */
    public String getParentpath() {
        return this.parentpath;
    }

    /**
     * Getter for <code>repository</code>.
     * @return Returns the repository.
     */
    public String getRepository() {
        return this.repository;
    }

    /**
     * Setter for <code>dialogs</code>.
     * @param dialogs The dialogs to set.
     */
    public void setDialogs(boolean dialogs) {
        this.dialogs = dialogs;
    }

    /**
     * Setter for <code>paragraphs</code>.
     * @param paragraphs The paragraphs to set.
     */
    public void setParagraphs(boolean paragraphs) {
        this.paragraphs = paragraphs;
    }

    /**
     * Setter for <code>templates</code>.
     * @param templates The templates to set.
     */
    public void setTemplates(boolean templates) {
        this.templates = templates;
    }

    /**
     * Setter for <code>rootdir</code>.
     * @param rootdir The rootdir to set.
     */
    public void setRootdir(String rootdir) {
        this.rootdir = rootdir;
    }

    /**
     * Setter for <code>website</code>.
     * @param website The website to set.
     */
    public void setWebsite(boolean website) {
        this.website = website;
    }

    /**
     * Setter for <code>parentpath</code>.
     * @param parentpath The parentpath to set.
     */
    public void setParentpath(String parentpath) {
        this.parentpath = parentpath;
    }

    /**
     * Setter for <code>groups</code>.
     * @param groups The groups to set.
     */
    public void setGroups(boolean groups) {
        this.groups = groups;
    }

    /**
     * Setter for <code>roles</code>.
     * @param roles The roles to set.
     */
    public void setRoles(boolean roles) {
        this.roles = roles;
    }

    /**
     * Setter for <code>users</code>.
     * @param users The users to set.
     */
    public void setUsers(boolean users) {
        this.users = users;
    }

    /**
     * Getter for <code>module</code>.
     * @return Returns the module.
     */
    public String getModule() {
        return this.module;
    }

    /**
     * Setter for <code>module</code>.
     * @param module The module to set.
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Setter for <code>repository</code>.
     * @param repository The repository to set.
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Getter for <code>virtualURIs</code>.
     * @return Returns the virtualURIs.
     */
    public boolean isVirtualURIs() {
        return this.virtualURIs;
    }

    /**
     * Setter for <code>virtualURIs</code>.
     * @param virtualURIs The virtualURIs to set.
     */
    public void setVirtualURIs(boolean virtualURIs) {
        this.virtualURIs = virtualURIs;
    }

    public Iterator getRepositories() {
        return ContentRepository.getAllRepositoryNames();
    }

    public Set getModules() {
        return ModuleRegistry.Factory.getInstance().getModuleNames();
    }

    // ---- operations ----
    public String extractModuleFiles() {
        final FileExtractor extractor = new BasicFileExtractor();
        try {
            extractor.extractFiles(new ModuleFileExtractorTransformer(module));
            AlertUtil.setMessage("Files extracted");
        }
        catch (IOException e) {
            AlertUtil.setMessage("Could not extract files for module " + module + ": " + e.getMessage(), e);
        }

        return this.show();
    }

    public String reloadI18nMessages() {
        try {
            MessagesManager.getInstance().reload();
            AlertUtil.setMessage("Messages reloaded.");
        }
        catch (Exception e) {
            e.printStackTrace();
            AlertUtil.setMessage("Can't reload: " + e.getMessage(), e);
        }

        return this.show();
    }

    public String backup() {
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        Session session = hm.getWorkspace().getSession();

        try {
            Content moduleroot = hm.getContent("/modules/" + module);
            if (templates) {
                exportChildren(ContentRepository.CONFIG, session, moduleroot, "templates", new ItemType[]{
                    ItemType.CONTENT,
                    ItemType.CONTENTNODE}, false);
            }
            if (paragraphs) {
                exportChildren(ContentRepository.CONFIG, session, moduleroot, "paragraphs", new ItemType[]{
                    ItemType.CONTENT,
                    ItemType.CONTENTNODE}, false);
            }
            if (pages) {
                exportChildren(ContentRepository.CONFIG, session, moduleroot, "pages", new ItemType[]{
                    ItemType.CONTENT,
                    ItemType.CONTENTNODE}, false);
            }
            if (dialogs) {
                exportChildren(ContentRepository.CONFIG, session, moduleroot, "dialogs", new ItemType[]{
                    ItemType.CONTENT,
                    ItemType.CONTENTNODE}, true);
            }
            if (virtualURIs) {
                exportChildren(
                    ContentRepository.CONFIG,
                    session,
                    moduleroot,
                    "virtualURIMapping",
                    new ItemType[]{ItemType.CONTENTNODE},
                    true);
            }
            AlertUtil.setMessage("Backup done to "
                + new File(Path.getAbsoluteFileSystemPath(rootdir)).getCanonicalPath());
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            AlertUtil.setMessage("Error while processing module " + module, e);
        }

        if (website) {
            extractWorkspaceRoots(ContentRepository.WEBSITE);
        }

        if (users) {
            backupChildren(ContentRepository.USERS, "/admin");
        }

        if (groups) {
            extractWorkspaceRoots(ContentRepository.USER_GROUPS);
        }

        if (roles) {
            extractWorkspaceRoots(ContentRepository.USER_ROLES);
        }

        return this.show();
    }

    /**
     * @param repositoryName
     */
    private void extractWorkspaceRoots(String repositoryName) {
        try {
            HierarchyManager hm = MgnlContext.getHierarchyManager(repositoryName);
            Content wesiteRoot = hm.getRoot();

            Iterator children = wesiteRoot.getChildren(ContentUtil.MAGNOLIA_FILTER).iterator();
            while (children.hasNext()) {
                Content exported = (Content) children.next();
                exportNode(repositoryName, hm.getWorkspace().getSession(), exported);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            AlertUtil.setMessage("Error while processing " + repositoryName + " repository", e);
        }
    }

    public String backupChildren() {
        backupChildren(this.repository, this.parentpath);

        String path = Path.getAbsoluteFileSystemPath(rootdir);

        try {
            path = new File(path).getCanonicalPath();
        }
        catch (IOException e) {
            // should never happen
        }

        AlertUtil.setMessage("Backup done to " + path);

        return this.show();
    }

    private void backupChildren(String repository, String parentpath) {
        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

        Content parentNode = null;
        try {
            parentNode = hm.getContent(parentpath);
        }
        catch (RepositoryException e) {
            // ignore
            return;
        }
        try {
            Iterator children = parentNode.getChildren(ContentUtil.ALL_NODES_EXCEPT_JCR_CONTENT_FILTER).iterator();
            while (children.hasNext()) {
                Content exported = (Content) children.next();
                exportNode(repository, hm.getWorkspace().getSession(), exported);
            }

        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            AlertUtil.setMessage("Error while processing actions", e);
        }

    }

    /**
     * @param session
     * @param moduleroot
     * @param exportContentContainingContentNodes
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void exportChildren(String repository, Session session, Content moduleroot, String path,
        ItemType[] itemTypes, boolean exportContentContainingContentNodes) throws PathNotFoundException,
        RepositoryException, AccessDeniedException, FileNotFoundException, IOException {
        Content templateRoot = null;
        try {
            templateRoot = moduleroot.getContent(path);
        }
        catch (PathNotFoundException e) {
            // ignore
            return;
        }

        // we need to track exported paths, or it will export any single control for dialogs
        Set alreadyExported = new HashSet();

        Iterator children = ContentUtil.collectAllChildren(templateRoot, itemTypes).iterator();
        while (children.hasNext()) {
            Content exported = (Content) children.next();
            if (!exported.getNodeDataCollection().isEmpty() // ignore "directories"
                || (exportContentContainingContentNodes && exported.hasChildren(ItemType.CONTENTNODE.getSystemName()))) {

                String current = exported.getHandle();
                boolean dontexport = false;

                for (Iterator iterator = alreadyExported.iterator(); iterator.hasNext();) {
                    String already = (String) iterator.next();
                    if (current.startsWith(already)) {
                        dontexport = true;
                        break;
                    }
                }

                if (!dontexport) {
                    alreadyExported.add(exported.getHandle() + "/");
                    exportNode(repository, session, exported);
                }
            }
        }
    }

    /**
     * @param session
     * @param exported
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void exportNode(String repository, Session session, Content exported) throws FileNotFoundException,
        IOException {
        String handle = exported.getHandle();

        String xmlName = repository + StringUtils.replace(handle, "/", ".") + ".xml";

        // create necessary parent directories
        File folder = new File(Path.getAbsoluteFileSystemPath(rootdir));
        folder.mkdirs();
        File xmlFile = new File(folder.getAbsoluteFile(), xmlName);
        FileOutputStream fos = new FileOutputStream(xmlFile);

        try {
            DataTransporter.executeExport(fos, false, true, session, handle, repository, DataTransporter.XML);
        }
        finally {
            IOUtils.closeQuietly(fos);
        }
    }
}
