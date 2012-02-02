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
package info.magnolia.templating.editor.client.dom.processor;

import info.magnolia.templating.editor.client.PageBarWidget;
import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.CMSComment;
import info.magnolia.templating.editor.client.dom.Comment;
import info.magnolia.templating.editor.client.dom.MgnlElement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;

/**
 * Processor for comment elements.
 * @author espen
 *
 */
public class CommentProcessor {

    public static MgnlElement process (Node node, MgnlElement mgnlElement) throws Exception {

        CMSComment comment = new CMSComment((Comment)node.cast());

        GWT.log("processing comment " + comment);

        if (!comment.isClosing()) {

            if (comment.getTagName().equals("cms:page")) {
                GWT.log("element was detected as page edit bar. Injecting it...");
                PageBarWidget pageBarWidget = new PageBarWidget(comment);
                pageBarWidget.attach();

                if (PageEditor.isPreview()) {
                    //we just need the preview bar here
                    GWT.log("We're in preview mode, stop processing DOM.");
                    PageEditor.process  = false;
                }
            }

            else {
                try {
                    mgnlElement = new MgnlElement(comment, mgnlElement);

                    if (mgnlElement.getParent() == null) {
                        PageEditor.model.addRoot(mgnlElement);
                    }
                    else {
                        mgnlElement.getParent().getChildren().add(mgnlElement);
                    }

                }
                catch (IllegalArgumentException e) {
                    GWT.log("Not MgnlElement, skipping: " + e.toString());
                }
            }
        }

        else if (mgnlElement != null) {
            mgnlElement = mgnlElement.getParent();
        }

        return mgnlElement;

    }
}
