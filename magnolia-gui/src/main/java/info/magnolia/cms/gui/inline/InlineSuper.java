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
package info.magnolia.cms.gui.inline;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class InlineSuper {

    private String path = StringUtils.EMPTY;

    private String paragraph = StringUtils.EMPTY;

    public void setPath(String s) {
        this.path = s;
    }

    public String getPath() {
        return this.path;
    }

    public void setParagraph(String s) {
        this.paragraph = s;
    }

    public String getParagraph() {
        return this.paragraph;
    }
}
