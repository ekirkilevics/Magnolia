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
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.security.SessionAccessControl;

import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @version 2.0
 */
public class RequestInterceptor extends HttpServlet
{

    private HttpServletRequest request;

    private HttpServletResponse response;

    /**
     * <p>
     * Request and Response here is same as receivced by the original page so it includes all post/get data
     * </p>
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        this.request = request;
        this.response = response;
        this.action();
    }

    public void setRequest(HttpServletRequest request)
    {
        this.request = request;
    }

    public HttpServletRequest getRequest()
    {
        return this.request;
    }

    public void setResponse(HttpServletResponse response)
    {
        this.response = response;
    }

    public HttpServletResponse getResponse()
    {
        return this.response;
    }

    /**
     * <p>
     * Sub action could be called from here once this action finishes, it will continue loading the requested page
     * </p>
     */
    private void action()
    {
        String action = this.getRequest().getParameter(EntryServlet.INTERCEPT);

        String repository = this.getRequest().getParameter("mgnlRepository");
        if (repository == null)
            repository = ContentRepository.WEBSITE;
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), repository);

        if (action.equals("PREVIEW"))
        {
            // preview mode (button in main bar)
            String preview = this.getRequest().getParameter("mgnlPreview");
            if (preview != null)
            {
                if (preview.equals("true"))
                    this.getRequest().getSession().setAttribute("mgnlPreview", "true");
                else
                    request.getSession().removeAttribute("mgnlPreview");
            }
        }
        else if (action.equals("NODE_DELETE"))
        {
            // delete paragraph
            try
            {
                String path = this.getRequest().getParameter("mgnlPath");
                hm.delete(path);
            }
            catch (Exception e)
            {
            }
        }
        else if (action.equals("NODE_SORT"))
        {
            // sort paragrpahs
            try
            {
                String pathSelected = this.getRequest().getParameter("mgnlPathSelected");
                String pathSortAbove = this.getRequest().getParameter("mgnlPathSortAbove");
                String pathParent = pathSelected.substring(0, pathSelected.lastIndexOf("/"));

                Iterator it = hm.getContentNode(pathParent).getChildren().iterator();

                long seqPos0 = 0;
                long seqPos1 = 0;

                while (it.hasNext())
                {
                    Content c = (Content) it.next();
                    if (c.getHandle().equals(pathSortAbove))
                    {
                        seqPos1 = c.getMetaData().getSequencePosition();
                        break;
                    }
                    seqPos0 = c.getMetaData().getSequencePosition();
                }

                Content nodeSelected = hm.getContent(pathSelected);
                if (seqPos0 == 0)
                {
                    // move to first position -> 1000*coefficient above seqPos1 (old first)
                    nodeSelected
                        .getMetaData()
                        .setSequencePosition(seqPos1 - (MetaData.SEQUENCE_POS_COEFFICIENT * 1000));
                }
                else if (seqPos1 == 0)
                {
                    // move to last position (pathSortAbove not found)
                    nodeSelected.getMetaData().setSequencePosition();
                }
                else
                {
                    // move between two paragraphs
                    nodeSelected.getMetaData().setSequencePosition((seqPos0 + seqPos1) / 2);
                }
                nodeSelected.save();

            }
            catch (Exception e)
            {
            }

        }

    }

}
