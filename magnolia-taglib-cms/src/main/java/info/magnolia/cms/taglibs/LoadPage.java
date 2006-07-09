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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Aggregator;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Loads another page into actpage. One usage would be within a site-menu structure. loadPage does not nest pages, so
 * the corresponding unloadPage-tag will not revert to the previously loaded page, but restore actpage to the currently
 * displayed page, i.e. the value it held before loadPage was called for the first time.
 * @author Marcel Salathe
 * @version $Revision$ ($Author$)
 */
public class LoadPage extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(LoadPage.class);

    /**
     * Tag attribute: path of the page to be loaded.
     */
    private String path;

    /**
     * Tag attribute: template name.
     */
    private String templateName;

    /**
     * Tag attribute: level.
     */
    private int level;

    /**
     * Setter for the "path" tag attribute.
     * @param path path of the page to be loaded
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Setter for the "templateName" tag attribute.
     * @param templateName
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Setter for the "level" tag attribute.
     * @param level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        Content newActpage = Resource.getCurrentActivePage(req);
        if (StringUtils.isNotEmpty(this.templateName)) {
            Content startPage;
            try {
                startPage = Resource.getCurrentActivePage(req).getAncestor(this.level);
                HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
                newActpage = hm.getPage(startPage.getHandle(), this.templateName);
            }
            catch (RepositoryException e) {
                log.error(e.getMessage());
                return EVAL_PAGE;
            }
        }
        else if (StringUtils.isNotEmpty(this.path)) {
            try {
                newActpage = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE).getContent(this.path);
            }
            catch (Exception e) {
                log.error(e.getMessage());
                return EVAL_PAGE;
            }
        }
        else {
            try {
                newActpage = Resource.getCurrentActivePage(req).getAncestor(this.level);
            }
            catch (Exception e) {
                log.error(e.getMessage());
                return EVAL_PAGE;
            }
        }
        pageContext.setAttribute(Aggregator.CURRENT_ACTPAGE, newActpage, PageContext.REQUEST_SCOPE);
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.path = null;
        this.templateName = null;
        this.level = 0;
    }
}
