/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.templating.editor.client.dom;
import info.magnolia.templating.editor.client.dom.exception.IllegalCMSCommentException;

import java.util.HashMap;


/**
 * CMSComment.
 */
public class CMSComment {

    private String comment;
    private String tagName;
    private HashMap<String, String> attributes;

    public CMSComment(String comment) throws IllegalCMSCommentException {
        this.comment = comment.trim();


        int delimiter = this.comment.indexOf(" ");
        this.tagName = this.comment.substring(0, delimiter);

        if (!this.tagName.startsWith("cms:")) {
            throw new IllegalCMSCommentException();
        }


        String attributeString = this.comment.substring(delimiter);

        this.attributes = new HashMap<String, String>();
        for (String attribute : attributeString.split(" ")) {
            if (attribute.contains("=")) {
                String[] keyValue = attribute.split("=");
                this.attributes.put(keyValue[0], keyValue[1].replace("\"", ""));
            }
        }
    }

    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    public String getTagName() {
        return this.tagName;
    }

    @Override
    public String toString() {
        return this.comment;
    }

}
