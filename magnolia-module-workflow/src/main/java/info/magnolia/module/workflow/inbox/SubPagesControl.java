/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.workflow.inbox;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.freemarker.FreemarkerUtil;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public class SubPagesControl extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SubPagesControl.class);

    public void drawHtml(Writer out) throws IOException {
        try {
            if (NodeDataUtil.getBoolean(this.getWebsiteNode(), "recursive", false)) {
                this.drawHtmlPre(out);
                out.write(FreemarkerUtil.process(this));
                this.drawHtmlPost(out);
            }
        }
        catch (Exception e) {
            log.error("can't show subpages", e);
            out.write(e.toString());
        }
    }

    public String getWorkItemId(){
        Content itemNode;
        try {
            itemNode = getWebsiteNode().getParent().getParent();
            return itemNode.getNodeData("ID").getString();
        }
        catch (Exception e) {
            log.error("can't evaluate the workitems id", e);
        }
        return "";
    }

}
