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
package info.magnolia.module.templating.paragraphs;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;

import java.io.IOException;
import java.io.Writer;

/**
 * A simple paragraph renderer which just writes the value of the "text" property of the content node.
 * Could be useful for plaintext resources (css, robots.txt, ...)
 * 
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PlainTextParagraphRenderer implements ParagraphRenderer {

    /**
     * The given paragraph is ignored here, since we're just printing the value of the text property.
     */
    public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
        final String s = content.getNodeData("text").getString();
        out.write(s);
    }
}
