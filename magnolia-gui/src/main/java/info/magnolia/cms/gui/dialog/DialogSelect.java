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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.SelectOption;
import info.magnolia.cms.gui.misc.CssConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
            Iterator it = configNode.getContent("options").getChildren(ItemType.CONTENTNODE.getSystemName()).iterator(); //$NON-NLS-1$
            while (it.hasNext()) {
                Content n = (Content) it.next();
                String value = n.getNodeData("value").getString(); //$NON-NLS-1$
                String label = null;
                if (n.getNodeData("label").isExist()) { //$NON-NLS-1$
                    label = n.getNodeData("label").getString(); //$NON-NLS-1$
                    label = this.getMessage(label);
                }
                SelectOption option = new SelectOption(label, value);
                if (n.getNodeData("selected").getBoolean()) { //$NON-NLS-1$
                    option.setSelected(true);
                }
                options.add(option);
            }
        }
        catch (RepositoryException e) {
            if (log.isDebugEnabled())
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        this.setOptions(options);
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
        control.setOptions(this.getOptions());
        this.drawHtmlPre(out);
        out.write(control.getHtml());
        this.drawHtmlPost(out);
    }
}