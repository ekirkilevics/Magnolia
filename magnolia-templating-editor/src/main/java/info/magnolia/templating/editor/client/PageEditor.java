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
package info.magnolia.templating.editor.client;


import info.magnolia.templating.editor.client.dom.CMSBoundary;
import info.magnolia.templating.editor.client.dom.CMSComment;
import info.magnolia.templating.editor.client.dom.Comment;
import info.magnolia.templating.editor.client.jsni.LegacyJavascript;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
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
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Client side implementation of the page editor. Outputs ui widgets inside document element (typically the {@code <html>} element).
 *
 * @version $Id$
 */
public class PageEditor extends HTML implements EventListener, EntryPoint {

    public static final String MARKER_EDIT = "cms:edit";
    public static final String MARKER_AREA = "cms:area";
    public static final String MARKER_COMPONENT = "cms:component";


    private boolean pageEditBarAlreadyProcessed = false;
    private String locale;
    private static Dictionary dictionary;

    @Override
    public void onModuleLoad() {
        Element documentElement = Document.get().getDocumentElement();

        locale = detectCurrentLocale(documentElement);
        //TODO move messages we need to this module?
        LegacyJavascript.exposeMgnlMessagesToGwtDictionary("info.magnolia.module.admininterface.messages");
        dictionary = Dictionary.getDictionary("mgnlGwtMessages");



        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                processCmsComments(Document.get().getDocumentElement(), null);
                VisibilityHelper.getInstance().showRoot();
                VisibilityHelper.getInstance().computeOverlay();


                RootPanel.get().addDomHandler(new MouseUpHandler() {
                    @Override
                    public void onMouseUp(MouseUpEvent event) {
                        VisibilityHelper.getInstance().deSelect();
                        VisibilityHelper.getInstance().showRoot();
                        VisibilityHelper.getInstance().computeOverlay();
                    }
                }, MouseUpEvent.getType());
            }
        });

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
        url.append(LegacyJavascript.getContextPath() + ".magnolia/pageeditor/PageEditorServlet");
        url.append("?action=create");
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
                    Window.alert("An error occurred on the server: response status code is " + status + "\n" + responseText);
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
            Window.alert("An error occurred while trying to send a request to the server: " + e.getMessage());
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

    private void processCmsComments(Node node, CMSBoundary boundary) {
        for (int i = 0; i < node.getChildCount(); i++) {
            Node childNode = node.getChild(i);
            if (childNode.getNodeType() == Comment.COMMENT_NODE) {

                try {
                    CMSComment comment = new CMSComment(((Comment)childNode.cast()).getData());

                    if (comment.isClosing()) {

                        if (boundary != null) {

                            if (!boundary.isEdit()) {
                                Element editBar = null;
                                for (CMSBoundary child : boundary.getChildBoundaries()) {
                                     if (child.isEdit() && child.getWidget() != null){
                                        editBar = child.getWidget().getElement();
                                        break;
                                     }
                                }
                                if (editBar != null) {


                                    if (boundary.isArea() && boundary.getOverlayWidget() != null) {
                                        double height = 0;

                                        for (Node sibling = childNode.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
                                            if (sibling.getNodeType() == Node.ELEMENT_NODE) {

                                                Element element = sibling.cast();
                                                String cn = element.getClassName();
                                                int df = element.getStyle().getDisplay().compareToIgnoreCase(Style.Display.NONE.toString());
                                                int y = element.getOffsetHeight();
                                                if (element.getClassName().equals("mgnlAreaEditBar") || element.getStyle().getDisplay().compareToIgnoreCase(Style.Display.NONE.toString()) == 0 || element.getOffsetHeight() == 0) {
                                                    continue;
                                                }
                                                height = element.getAbsoluteBottom() - boundary.getOverlayWidget().getTop();

                                                boundary.setLastElement(element);

                                                break;
                                            }
                                        }
                                        boundary.getOverlayWidget().setHeight(height);

                                        boundary.getOverlayWidget().attach();
                                    }
                                }
                            }
                            boundary = boundary.getParentBoundary();

                        }
                        continue;
                    }
                    try {
                        boundary = new CMSBoundary(comment, boundary);

                        if( boundary.getParentBoundary() != null)
                            boundary.getParentBoundary().getChildBoundaries().add(boundary);

                        if (boundary.isEdit()) {
                            GWT.log("processing comment " + comment);
                            //We assume the first cms:edit we encounter in DOM is the page edit bar.
                            if (!pageEditBarAlreadyProcessed) {
                                GWT.log("element was detected as page edit bar. Injecting it...");
                                PageBarWidget pageBarWidget = new PageBarWidget(this, comment);
                                pageBarWidget.attach(childNode);
                                pageEditBarAlreadyProcessed = true;

                                boundary.setWidget(pageBarWidget);


                                if (pageBarWidget.isPreviewMode()) {
                                    //we just need the preview bar here
                                    GWT.log("We're in preview mode, stop processing DOM.");
                                    break;
                                }
                            }
                            else if (boundary.getParentBoundary().isComponent()) {
                                GWT.log("element is a plain edit bar. Injecting it...");
                                EditBarWidget editBarWidget = new EditBarWidget(boundary, this);

                                editBarWidget.attach(childNode);

                                boundary.setWidget(editBarWidget);

                            }
                            else if (boundary.getParentBoundary().isArea()) {
                                GWT.log("element was detected as area edit bar. Injecting it...");


                                AreaBarWidget areaBarWidget = new AreaBarWidget(boundary, this);
                                if (areaBarWidget.hasControls) {
                                    areaBarWidget.attach(childNode);
                                    boundary.setWidget(areaBarWidget);

                                }

                            }
                        }
                        else if(boundary.isArea()) {

                            int top = 0;
                            int left = 0;
                            int width = 0;

                            AbstractOverlayWidget overlayWidget = new AreaOverlayWidget(boundary);

                            for (Node sibling = childNode.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
                                if (sibling.getNodeType() == Node.ELEMENT_NODE) {

                                    Element element = sibling.cast();
                                    String cn = element.getClassName();
                                    int df = element.getStyle().getDisplay().compareToIgnoreCase(Style.Display.NONE.toString());
                                    int y = element.getOffsetHeight();
                                    if (element.getClassName().equals("mgnlAreaEditBar") || element.getStyle().getDisplay().compareToIgnoreCase(Style.Display.NONE.toString()) == 0 || element.getOffsetHeight() == 0) {
                                        continue;
                                    }
                                    top = element.getAbsoluteTop();
                                    left = element.getAbsoluteLeft();
                                    width = element.getAbsoluteRight() - element.getAbsoluteLeft();

                                    boundary.setFirstElement(element);
                                    break;
                                }
                            }
                            overlayWidget.setTop(top);
                            overlayWidget.setLeft(left);
                            overlayWidget.setWidth(width);

                            boundary.setOverlayWidget(overlayWidget);
                        }

                    }
                    catch (IllegalArgumentException e) {
                        GWT.log("Not CMSBoundary element, skipping: " + e.toString());
                    }
                }
                catch (IllegalArgumentException e) {
                    GWT.log("Not CMSComment element, skipping: " + e.toString());

                }
            }

            processCmsComments(childNode, boundary);
        }

    }

}
