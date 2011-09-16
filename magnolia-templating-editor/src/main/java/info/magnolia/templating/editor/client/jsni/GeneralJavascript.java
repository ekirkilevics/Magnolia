/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.templating.editor.client.jsni;

/**
 * A JSNI wrapper around native javascript functions found in general.js and some utilities.
 * @version $Id$
 *
 */
public final class GeneralJavascript {

    private GeneralJavascript() {
        //do not instantiate it.
    }

    public static native void mgnlOpenDialog(String path, String collectionName, String nodeName, String paragraph, String workspace, String dialogPage, String width, String height, String locale) /*-{
        $wnd.mgnlOpenDialog(path, collectionName, nodeName, paragraph, workspace, dialogPage, width, height, locale);
    }-*/;

    public static native void mgnlMoveNodeStart(String id) /*-{
        $wnd.mgnlMoveNodeStart('',id,'__'+id);
    }-*/;

    public static native void mgnlMoveNodeHigh(Object source) /*-{
        $wnd.mgnlMoveNodeHigh(source);
    }-*/;

    public static native void mgnlMoveNodeEnd(Object source, String path) /*-{
        $wnd.mgnlMoveNodeEnd(source, path);
    }-*/;

    public static native void mgnlMoveNodeReset(Object source) /*-{
        $wnd.mgnlMoveNodeReset(source);
    }-*/;

    public static native void mgnlDeleteNode(String path) /*-{
        $wnd.mgnlDeleteNode(path,'', '');
    }-*/;

    /**
     * Exposes the messages object corresponding to the passed in basename as a global (thus accessible by GWT Dictionary) variable named <em>mgnlGwtMessages</em>.
     */
    public static native void exposeMgnlMessagesToGwtDictionary(String basename) /*-{
        $wnd.mgnlGwtMessages = $wnd.mgnlMessages.messages[basename];
    }-*/;
}