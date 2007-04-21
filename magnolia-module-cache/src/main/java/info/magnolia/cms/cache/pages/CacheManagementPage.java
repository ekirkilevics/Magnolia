/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2007 Magnolia. All rights reserved.
 *
 */
package info.magnolia.cms.cache.pages;

import info.magnolia.api.HierarchyManager;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.cache.CacheManager;
import info.magnolia.cms.cache.CacheManagerFactory;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Cache management page.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class CacheManagementPage extends TemplatedMVCHandler {

    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(CacheManagementPage.class);

    private CacheManager cacheManager = CacheManagerFactory.getCacheManager();

    /**
     * @param name
     * @param request
     * @param response
     */
    public CacheManagementPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public Messages getMessages() {
        return MessagesManager.getMessages();
    }

    public Server getServer() {
        return Server.getInstance();
    }

    public String cacheAll() {

        try {
            HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
            cachePage(hm.getRoot(), request);
            cachePage(hm.getRoot(), request);
            AlertUtil.setMessage(MessagesManager.get("cacheservlet.success"));
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            AlertUtil.setException("Error while generating cache " + e.getMessage(), e);
        }

        return this.show();
    }

    private void cachePage(Content page, HttpServletRequest request) {
        // Collection children = page.getChildren();
        // Iterator iter = children.iterator();
        // while (iter.hasNext()) {
        // Content item = (Content) iter.next();
        // MockCacheRequest mock = new MockCacheRequest(request, item);
        // // @todo fix the canCompress parameter
        // this.cacheManager.cacheRequest(mock, true);
        // if (log.isDebugEnabled()) {
        // log.debug("Trying to cache request:" + mock.getRequestURL());
        // }
        // cachePage(item, request);
        // }
    }

}
