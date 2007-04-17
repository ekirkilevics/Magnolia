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

import freemarker.template.TemplateException;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.freemarker.FreemarkerContentRenderer;

import java.io.IOException;
import java.io.Writer;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerParagraphRenderer implements ParagraphRenderer {
    private final FreemarkerContentRenderer fmRenderer;

    public FreemarkerParagraphRenderer(FreemarkerContentRenderer fmRenderer) {
        this.fmRenderer = fmRenderer;
    }

    public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
        final String templatePath = paragraph.getTemplatePath();

        if (templatePath == null) {
            throw new IllegalStateException("Unable to render paragraph " + paragraph.getName() + " in page " + content.getHandle() + ": templatePath not set.");
        }

        try {
            fmRenderer.render(templatePath, content, out);
        } catch (TemplateException e) {
            throw new RuntimeException(e); // TODO
        }
    }

}


/**
 action

 RequestFormUtil requestFormUtil = new RequestFormUtil(this.getRequest());
 Map parameters = new HashMap(); // needed, can't directly modify the map returned by request.getParameterMap()
 parameters.putAll(requestFormUtil.getParameters());
 parameters.putAll(requestFormUtil.getDocuments()); // handle uploaded files too

 try {
     BeanUtils.populate(this, parameters);
 }
 catch (Exception e) {
     log.error("can't set properties on the handler", e);
 }
*/