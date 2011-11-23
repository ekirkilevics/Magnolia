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

import java.util.HashMap;

/**
* CMSComment Constructor.
*
* @throws IllegalArgumentException if the tagname does not start with cms:
*/
public class CMSComment {


    private String comment;
    private String tagName;
    private boolean isClosing = false;

    private HashMap<String, String> attributes;

    public CMSComment(String comment) throws IllegalArgumentException {
        this.comment = comment.trim();


        int delimiter = this.comment.indexOf(" ");
        String attributeString = "";

        if (delimiter < 0){
            this.tagName = this.comment;
        }
        else {
            this.tagName = this.comment.substring(0, delimiter);
            attributeString = this.comment.substring(delimiter + 1);
        }

        if (this.tagName.startsWith("/")) {
            setClosing(true);
            this.tagName = this.tagName.substring(1);
        }


        if (this.tagName.startsWith("cms:")) {

            this.attributes = new HashMap<String, String>();
            for (String attribute : attributeString.split(" ")) {
                if (attribute.contains("=")) {
                    String[] keyValue = attribute.split("=");
                    this.attributes.put(keyValue[0], keyValue[1].replace("\"", ""));
                }
            }
        }
        else {
            throw new IllegalArgumentException("Tagname must start with 'cms:'.");
        }

    }

    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean isClosing) {
        this.isClosing = isClosing;
    }


    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    public boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
    }

    public String getTagName() {
        return this.tagName;
    }

    @Override
    public String toString() {
        return this.comment;
    }

}
