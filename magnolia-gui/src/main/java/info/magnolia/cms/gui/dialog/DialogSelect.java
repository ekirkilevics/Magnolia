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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.SelectOption;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogSelect extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogSelect.class);

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogSelect() {
    }

    public void setOptions(Content configNode) {
        List options = new ArrayList();
        try {
            Iterator it = this.getOptionNodes(configNode).iterator(); //$NON-NLS-1$
            while (it.hasNext()) {
                Content n = (Content) it.next();
                String valueNodeData = this.getConfigValue("valueNodeData", "value");
                String labelNodeData = this.getConfigValue("labelNodeData", "label");

                String value = n.getName();
                if (n.hasNodeData(valueNodeData)) {
                    value = NodeDataUtil.getString(n, valueNodeData);
                }
                String label = NodeDataUtil.getString(n, labelNodeData, value);//$NON-NLS-1$

                SelectOption option = new SelectOption(label, value);
                if (n.getNodeData("selected").getBoolean()) { //$NON-NLS-1$
                    option.setSelected(true);
                }
                options.add(option);
            }
        }
        catch (RepositoryException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        this.setOptions(options);
    }

    protected Collection getOptionNodes(Content configNode) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content optionsNode = null;

        if(configNode.hasContent("options")){
            optionsNode = configNode.getContent("options"); //$NON-NLS-1$
        }
        else{
            String repository = this.getConfigValue("repository", ContentRepository.WEBSITE);
            String path = this.getConfigValue("path");
            if(StringUtils.isNotEmpty(path)){
                optionsNode = ContentUtil.getContent(repository, path);
            }
        }

        if(optionsNode != null){
            return ContentUtil.getAllChildren(optionsNode);
        }
        return new ArrayList();
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        if (configNode != null) {
            setOptions(configNode);
        }
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        Select control = new Select(this.getName(), this.getValue());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING)); //$NON-NLS-1$
        if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
            control.setSaveInfo(false);
        }
        control.setCssClass(CssConstants.CSSCLASS_SELECT);
        control.setCssStyles("width", this.getConfigValue("width", "100%")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // translate (not possible in init since not a sub of the tab then)
        for (Iterator iter = this.getOptions().iterator(); iter.hasNext();) {
            SelectOption option = (SelectOption) iter.next();
            option.setLabel(this.getMessage(option.getLabel()));
        }
        control.setOptions(this.getOptions());

        this.drawHtmlPre(out);
        out.write(control.getHtml());
        this.drawHtmlPost(out);
    }
}