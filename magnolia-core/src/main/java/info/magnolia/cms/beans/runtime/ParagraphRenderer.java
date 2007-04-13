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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.beans.config.Paragraph;

import java.io.IOException;
import java.io.Writer;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ParagraphRenderer {

    // TODO : we probably need the content node here !
    void render(Paragraph paragraph, Writer out) throws IOException;

}
