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
package info.magnolia.templating.editor.client;


import info.magnolia.templating.editor.client.jsni.LegacyJavascript;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;

/**
 * Client side implementation of the page editor. Outputs ui widgets inside document element (typically the {@code <html>} element).
 *
 * @version $Id$
 */
public class PageEditor extends HTML implements EventListener, EntryPoint {

    public static final String MARKER_EDIT = "cms:edit";
    public static final String MARKER_AREA = "cms:area";

    public static final String SELECTION_TYPE_PAGE = "PAGE";
    public static final String SELECTION_TYPE_AREA_LIST = "AREA_LIST";
    public static final String SELECTION_TYPE_AREA_SINGLE = "AREA_SINGLE";
    public static final String SELECTION_TYPE_COMPONENT_IN_LIST = "COMPONENT_IN_LIST";
    public static final String SELECTION_TYPE_COMPONENT_IN_SINGLE = "COMPONENT_IN_SINGLE";

    private boolean pageEditBarAlreadyProcessed = false;
    private String locale;
    private String url;
    private static Dictionary dictionary;

    @Override
    public void onModuleLoad() {
        Element documentElement = Document.get().getDocumentElement();

        final NodeList<Element> edits = documentElement.getOwnerDocument().getElementsByTagName(MARKER_EDIT);
        GWT.log("found " + edits.getLength() + " cms:edit tags");

        final NodeList<Element> areas = documentElement.getOwnerDocument().getElementsByTagName(MARKER_AREA);
        GWT.log("found " + areas.getLength() + " cms:area tags");

        locale = detectCurrentLocale(documentElement);
        //TODO move messages we need to this module?
        LegacyJavascript.exposeMgnlMessagesToGwtDictionary("info.magnolia.module.admininterface.messages");
        dictionary = Dictionary.getDictionary("mgnlGwtMessages");

        processCmsTags(documentElement, null, edits, areas);

    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
    }

    /**
     * TODO: rename and/or remove arguments no longer needed (collectionName, nodeName).
     */
    public void openDialog(String dialog, String workspace, String path, String collectionName, String nodeName) {
        if (collectionName == null) {
            collectionName = "";
        }
        if (nodeName == null) {
            nodeName = "";
        }

        LegacyJavascript.mgnlOpenDialog(path, collectionName, nodeName, dialog, workspace, "", "", "", locale);
    }

    public void moveComponentStart(String id) {
        LegacyJavascript.mgnlMoveNodeStart(id);
    }

    public void moveComponentEnd(AbstractBarWidget source, String path) {
        LegacyJavascript.mgnlMoveNodeEnd(source.getElement(), path);
    }

    public void moveComponentOver(AbstractBarWidget source) {
        LegacyJavascript.mgnlMoveNodeHigh(source.getElement());
    }

    public void moveComponentOut(AbstractBarWidget source) {
        LegacyJavascript.mgnlMoveNodeReset(source.getElement());
    }

    public void deleteComponent(String path) {
        LegacyJavascript.mgnlDeleteNode(path);
    }

    public void addComponent(String workspace, String path, String collectionName, String nodeName, String availableComponents) {
        if (collectionName == null) {
            collectionName = "";
        }
        if (nodeName == null) {
            nodeName = "mgnlNew";
        }
        if (availableComponents == null) {
            availableComponents = "";
        }
        LegacyJavascript.mgnlOpenDialog(path, collectionName, nodeName, availableComponents, workspace, ".magnolia/dialogs/selectParagraph.html", "", "", locale);
    }

    public void preview(boolean isPreview) {
        LegacyJavascript.mgnlPreview(isPreview);
    }

    public void showTree(String workspace, String path) {
        LegacyJavascript.showTree(workspace, path);

    }

    public void createComponent(String workspace, String parent, String relPath, String itemType) {
        GWT.log("Creating [" + itemType + "] in workspace [" + workspace + "] at path [" + parent + "/" + relPath + "]");

        final StringBuilder url = new StringBuilder();
        url.append(LegacyJavascript.getContextPath() + ".magnolia/pageeditor/PageEditorServlet?");
        url.append("action=create");
        url.append("&workspace=" + workspace);
        url.append("&parent=" + parent);
        url.append("&relPath=" + relPath);
        url.append("&itemType=" + itemType);

        RequestBuilder req = new RequestBuilder(RequestBuilder.GET, URL.encode(url.toString()));
        req.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                int status = response.getStatusCode();
                String responseText = "";
                boolean reload = false;

                switch (status) {
                    case Response.SC_OK:
                        reload = true;
                        break;
                    case Response.SC_UNAUTHORIZED:
                        responseText = "Is your session expired? Please, try to login again.";
                        break;
                    default:
                        responseText = "See logs for more details.";
                }

                if (reload) {
                    UrlBuilder urlBuilder = Window.Location.createUrlBuilder();

                    urlBuilder.removeParameter("mgnlIntercept");
                    urlBuilder.removeParameter("mgnlPath");

                    Window.Location.replace(urlBuilder.buildString());
                } else {
                    Window.alert("An error occured on the server: response status code is " + status + "\n" + responseText);
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert(exception.getMessage());
            }
        });
        try {
            req.send();
        } catch (RequestException e) {
            Window.alert("An error occured whilst trying to send a request to the server: " + e.getMessage());
        }

    }

    /**
     * Provides dynamic string lookup of key/value string pairs defined in a module's host HTML page.
     */
    public static Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * A String representing the value for the GWT meta property whose content is <em>locale</em>.
     * See also <a href='http://code.google.com/webtoolkit/doc/latest/DevGuideI18nLocale.html#LocaleSpecifying'>GWT Dev Guide to i18n</a>
     */
    private String detectCurrentLocale(Element element) {
        final NodeList<Element> meta = element.getOwnerDocument().getElementsByTagName("meta");
        for (int i = 0; i < meta.getLength(); i++) {
            MetaElement metaTag = ((MetaElement) meta.getItem(i));
            if ("gwt:property".equals(metaTag.getName()) && metaTag.getContent().contains("locale")) {
                String[] split = metaTag.getContent().split("=");
                locale = split.length == 2 ? split[1] : "en";
                GWT.log("Detected Locale " + locale);
                break;
            }
        }
        return locale;
    }

    private void processCmsTags(Element element, AreaBarWidget parentBar, NodeList<Element> edits, NodeList<Element> areas) {
        for (int i = 0; i < element.getChildCount(); i++) {
            Node childNode = element.getChild(i);
            if (childNode.getNodeType() == Element.ELEMENT_NODE) {
                Element child = (Element) childNode;
                if (child.getTagName().equalsIgnoreCase(MARKER_EDIT)) {
                    GWT.log("processing element " + child);
                    //We assume the first cms:edit we encounter in DOM is the page edit bar.
                    if (!pageEditBarAlreadyProcessed) {
                        GWT.log("element was detected as page edit bar. Injecting it...");
                        PageBarWidget pageBarWidget = new PageBarWidget(this, child);
                        pageBarWidget.attach(child);
                        pageEditBarAlreadyProcessed = true;

                        if (pageBarWidget.isPreviewMode()) {
                            //we just need the preview bar here
                            GWT.log("We're in preview mode, stop processing DOM.");
                            break;
                        }
                        //avoid processing cms:edit marker twice if this is an area
                    } else if (!isAreaEditBar(child, areas)) {
                        GWT.log("element is a plain edit bar. Injecting it...");
                        EditBarWidget editBarWidget = new EditBarWidget(parentBar, this, child);
                        editBarWidget.attach(child);
                    }
                } else if (child.getTagName().equalsIgnoreCase(MARKER_AREA)) {
                    GWT.log("processing element " + child);
                    Element edit = findCmsEditMarkerForArea(child, edits);
                    if (edit != null) {
                        GWT.log("element was detected as area edit bar. Injecting it...");
                        AreaBarWidget areaBarWidget = new AreaBarWidget(parentBar, this, child);
                        areaBarWidget.attach(edit);
                        parentBar = areaBarWidget;
                    }
                }
                processCmsTags(child, parentBar, edits, areas);
            }
        }
    }

    /**
     * Tries to find a match between the provided edit bar tag and the area tags found in DOM. Best match is when area and edit tags have the exact same <code>content</code> attribute value.
     * However there might be the case where an optional area is in DOM but still needs to be created (manually via the UI). In that case content will be null,
     * therefore we rely on name and optional attributes on having the same values in area and edit tags.
     */
    private boolean isAreaEditBar(Element edit, NodeList<Element> areas) {

        String content = edit.getAttribute("content");
        String name = edit.getAttribute("name");
        boolean optional = Boolean.valueOf(edit.getAttribute("optional"));
        String bestMatch = optional ? name : content;

        for (int j = 0; j < areas.getLength(); j++) {

            Element area = areas.getItem(j);

            String areaContent = area.getAttribute("content");
            String areaName = area.getAttribute("name");
            boolean areaOptional = Boolean.valueOf(area.getAttribute("optional"));
            boolean created = Boolean.valueOf(area.getAttribute("created"));

            String areaMatch = areaContent + (LegacyJavascript.isNotEmpty(areaName) ? "/" + areaName : "");

            if (areaOptional && !created) {
                areaMatch = areaName;
            }

            if (bestMatch.equals(areaMatch)) {
                GWT.log("element is an area edit bar (matched with [" + areaMatch + "])");
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to find a match between the provided edit area tag and the edit tags found in DOM. Best match is when area and edit tags have the exact same <code>content</code> attribute value.
     * However there might be the case where an optional area is in DOM but still needs to be created (manually via the UI). In that case content will be null,
     * therefore we rely on name and optional attributes on having the same values in area and edit tags.
     */
    private Element findCmsEditMarkerForArea(Element area, NodeList<Element> edits) {
        String content = area.getAttribute("content");
        String name = area.getAttribute("name");
        boolean optional = Boolean.valueOf(area.getAttribute("optional"));
        boolean created = Boolean.valueOf(area.getAttribute("created"));
        //if area is optional and not yet created, best match is its name, else is content + name
        String bestMatch = optional && !created ? name : content + (LegacyJavascript.isNotEmpty(name) ? "/" + name : "");

        GWT.log("Best match for " + (optional ? "optional" : "required") + " area and edit bar is [" + bestMatch + "]");

        for (int i = 0; i < edits.getLength(); i++) {
            Element edit = edits.getItem(i);
            String toMatch = edit.getAttribute("content");
            String editName = edit.getAttribute("name");
            toMatch += (LegacyJavascript.isNotEmpty(editName) ? "/" + editName : "");
            boolean editOptional = Boolean.valueOf(edit.getAttribute("optional"));

            if (toMatch.equals(bestMatch) || (optional && editOptional && bestMatch.equals(editName))) {
                GWT.log("found match with element " + edit);
                return edit;
            }
        }
        GWT.log("No match found. Area won't have an edit bar associated.");
        return null;
    }
}
