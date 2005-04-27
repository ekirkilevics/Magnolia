/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.SelectOption;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.TemplateMessagesUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogSelect extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogSelect.class);

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogSelect() {
    }

    public void setOptions(Content configNode) {
        List options = new ArrayList();
        try {
            Iterator it = configNode.getContent("options").getChildren().iterator();
            while (it.hasNext()) {
                Content n = (Content) it.next();
                String value = n.getNodeData("value").getString();
                String label = null;
                if (n.getNodeData("label").isExist()) {
                    label = n.getNodeData("label").getString();
                    label = TemplateMessagesUtil.get(this, label);
                }
                SelectOption option = new SelectOption(label, value);
                if (n.getNodeData("selected").getBoolean()) {
                    option.setSelected(true);
                }
                options.add(option);
            }
        }
        catch (RepositoryException e) {
            log.info("Exception caught: " + e.getMessage(), e);
        }
        this.setOptions(options);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        if (configNode != null){
            setOptions(configNode);
        }
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        Select control = new Select(this.getName(), this.getValue());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING));
        if (this.getConfigValue("saveInfo").equals("false")) {
            control.setSaveInfo(false);
        }
        control.setCssClass(CssConstants.CSSCLASS_SELECT);
        control.setCssStyles("width", this.getConfigValue("width", "100%"));
        control.setOptions(this.getOptions());
        this.drawHtmlPre(out);
        out.write(control.getHtml());
        this.drawHtmlPost(out);
    }
}