/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ModuleLoader;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

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

    private boolean website;

    private String rootdir;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DevelopmentUtilsPage.class);

    /**
     * @param name
     * @param request
     * @param response
     */
    public DevelopmentUtilsPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
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

    public String backup() {

        Iterator modules = ModuleLoader.getInstance().getModuleInstances().keySet().iterator();
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        Session session = hm.getWorkspace().getSession();

        while (modules.hasNext()) {
            String moduleName = (String) modules.next();

            if (!"templating".equals(moduleName)) {
                // @todo temporary, this is what I need for now...
                continue;
            }
            try {
                Content moduleroot = hm.getContent("/modules/" + moduleName);
                if (templates) {
                    backupChildren(ContentRepository.CONFIG, session, moduleroot, "templates", new ItemType[]{
                        ItemType.CONTENT,
                        ItemType.CONTENTNODE});
                }
                if (paragraphs) {
                    backupChildren(ContentRepository.CONFIG, session, moduleroot, "paragraphs", new ItemType[]{
                        ItemType.CONTENT,
                        ItemType.CONTENTNODE});
                }
                if (dialogs) {
                    backupChildren(
                        ContentRepository.CONFIG,
                        session,
                        moduleroot,
                        "dialogs",
                        new ItemType[]{ItemType.CONTENT});
                }
                AlertUtil.setMessage("Backupped!");
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
                AlertUtil.setMessage("Error while processing module " + moduleName, e);
            }

        }

        if (website) {
            try {
                HierarchyManager hmWeb = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
                Content wesiteRoot = hmWeb.getRoot();

                Iterator children = wesiteRoot.getChildren(ItemType.CONTENT).iterator();
                while (children.hasNext()) {
                    Content exported = (Content) children.next();
                    exportNode(ContentRepository.WEBSITE, hmWeb.getWorkspace().getSession(), exported);
                }
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
                AlertUtil.setMessage("Error while processing website repository", e);
            }

        }

        return this.show();
    }

    /**
     * @param session
     * @param moduleroot
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void backupChildren(String repository, Session session, Content moduleroot, String path,
        ItemType[] itemTypes) throws PathNotFoundException, RepositoryException, AccessDeniedException,
        FileNotFoundException, IOException {
        Content templateRoot = moduleroot.getContent(path);

        Iterator children = ContentUtil.collectAllChildren(templateRoot, itemTypes).iterator();
        while (children.hasNext()) {
            Content exported = (Content) children.next();
            if (!exported.getNodeDataCollection().isEmpty()) { // ignore "directories"
                exportNode(repository, session, exported);
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

        FileOutputStream fos = new FileOutputStream(new File(Path.getAbsoluteFileSystemPath(rootdir), repository
            + "/"
            + xmlName));
        try {
            DataTransporter.executeExport(fos, false, true, session, handle, repository, DataTransporter.XML);
        }
        finally {
            IOUtils.closeQuietly(fos);
        }
    }
}
