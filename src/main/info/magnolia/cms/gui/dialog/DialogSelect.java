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
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.SelectOption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogSelect extends DialogBox {

    private static Logger log = Logger.getLogger(DialogSelect.class);

    public DialogSelect() {
    }

    public DialogSelect(ContentNode configNode, Content websiteNode) throws RepositoryException {
        super(configNode, websiteNode);
    }

    public void setOptions(ContentNode configNode) {
        ArrayList options = new ArrayList();
        try {
            Iterator it = configNode.getContentNode("options").getChildren().iterator();
            while (it.hasNext()) {
                ContentNode n = (ContentNode) it.next();
                String value = n.getNodeData("value").getString();
                String label = null;
                if (n.getNodeData("label").isExist())
                    label = n.getNodeData("label").getString();
                SelectOption option = new SelectOption(label, value);
                if (n.getNodeData("selected").getBoolean() == true)
                    option.setSelected(true);
                options.add(option);
            }
        }
        catch (RepositoryException re) {
        }
        this.setOptions(options);
    }

    public void drawHtml(JspWriter out) throws IOException {
        Select control = new Select(this.getName(), this.getValue());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING));
        if (this.getConfigValue("saveInfo").equals("false"))
            control.setSaveInfo(false);
        control.setCssClass(CSSCLASS_SELECT);
        control.setCssStyles("width", this.getConfigValue("width", "100%"));
        control.setOptions(this.getOptions());
        this.drawHtmlPre(out);
        out.println(control.getHtml());
        this.drawHtmlPost(out);
    }
}
