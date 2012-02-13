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
package info.magnolia.templating.editor.client.jsni;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.i18n.client.Dictionary;

/**
 * A JSNI wrapper around native javascript functions found in general.js, inline.js and others plus some utilities.
 * @version $Id$
 *
 */
public final class JavascriptUtils {

    private static Dictionary dictionary;

    static {
        //TODO move messages we need to this module?
        JavascriptUtils.exposeMgnlMessagesToGwtDictionary("info.magnolia.module.admininterface.messages");
        dictionary = Dictionary.getDictionary("mgnlGwtMessages");
    }

    private JavascriptUtils() {
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

    public static native void showTree(String workspace, String path) /*-{
        $wnd.MgnlAdminCentral.showTree(workspace, path)
    }-*/;

    public static native String getContextPath() /*-{
        return $wnd.location.protocol + "//"+ $wnd.location.host + $wnd.contextPath + "/"
    }-*/;

    public static boolean isNotEmpty(final String string) {
        return !isEmpty(string);
    }

    public static boolean isEmpty(final String string) {
        return string == null || string.length() == 0;
    }

    /**
     * This method will look for the specified key inside a GWT {@link Dictionary} named <code>mgnlGwtMessages</code>. If the key does not exist it will return a string
     * in the form <code>???missing.key???</code>. The keys looked for must reside in <code>info.magnolia.module.admininterface.messages</code> and
     * MUST end with the special suffix <code>.js</code> (i.e. <code>my.cool.i18nkey.js</code>).
     * <p><strong>WARNING: this way of exposing i18n messages to GWT is very likely to change in 5.0</strong>
     */
    public static String getI18nMessage(final String key) {
        try {
            return dictionary.get(key);
        } catch(RuntimeException e) {
            GWT.log("key ["+ key +"] was not found in dictionary", e);
            return "???" + key + "???";
        }
    }

    /**
     * A String representing the value for the GWT meta property whose content is <em>locale</em>.
     * See also <a href='http://code.google.com/webtoolkit/doc/latest/DevGuideI18nLocale.html#LocaleSpecifying'>GWT Dev Guide to i18n</a>
     */
    public static String detectCurrentLocale() {
        String locale = "en";
        final NodeList<Element> meta = Document.get().getDocumentElement().getElementsByTagName("meta");
        for (int i = 0; i < meta.getLength(); i++) {
            MetaElement metaTag = MetaElement.as(meta.getItem(i));
            if ("gwt:property".equals(metaTag.getName()) && metaTag.getContent().contains("locale")) {
                String[] split = metaTag.getContent().split("=");
                locale = split.length == 2 ? split[1] : "en";
                GWT.log("Detected Locale " + locale);
                break;
            }
        }
        return locale;
    }
}