/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;


/**
 * @author Marcel Salathe
 * @version $Revision: $ ($Author: $)
 */
public class LoadPage extends BodyTagSupport
{

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static Logger log = Logger.getLogger(LoadPage.class);

    private String path = "";

    private String templateName = "";

    private int level = 0;

    public int doEndTag()
    {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        Content newActpage = Resource.getCurrentActivePage(req);
        if (!this.templateName.equals(""))
        {
            Content startPage;
            try
            {
                startPage = Resource.getCurrentActivePage(req).getAncestor(this.level);
                HierarchyManager hm = Resource.getHierarchyManager(req);
                newActpage = hm.getPage(startPage.getHandle(), this.templateName);

            }
            catch (Exception e)
            {
                log.error(e.getMessage());
                return SKIP_BODY;
            }
        }
        if (!this.path.equals(""))
        {
            try
            {
                newActpage = Resource.getHierarchyManager(req).getPage(this.path);
            }
            catch (Exception e)
            {
                return SKIP_BODY;
            }
        }
        pageContext.setAttribute(Aggregator.CURRENT_ACTPAGE, newActpage, PageContext.REQUEST_SCOPE);
        return EVAL_BODY_INCLUDE;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }

    public void setLevel(String level)
    {
        this.level = (new Integer(level)).intValue();
    }

}
