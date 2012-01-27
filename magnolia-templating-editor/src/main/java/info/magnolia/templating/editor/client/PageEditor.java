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


import info.magnolia.templating.editor.client.PreviewChannelWidget.Orientation;
import info.magnolia.templating.editor.client.dom.CMSComment;
import info.magnolia.templating.editor.client.dom.Comment;
import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.jsni.LegacyJavascript;
import info.magnolia.templating.editor.client.model.ModelStorage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Client side implementation of the page editor. Outputs ui widgets inside document element (typically the {@code <html>} element).
 * Since the DOM manipulations performed by the PageEditor (i.e. dynamic creation of edit bars) happen when all other javascripts have already been loaded
 * (see <a href=http://code.google.com/webtoolkit/doc/latest/DevGuideOrganizingProjects.html#DevGuideBootstrap>GWT bootstrap FAQ</a>),
 * if you have some custom javascript which needs to operate on elements added by the PageEditor, you will have to use the utility javascript method <code>mgnl.PageEditor.onReady(callback)</code>.
 * This will ensure that your handler functions are executed when the PageEditor is actually done.
 * <p>For example:
 * <pre>
 * mgnl.PageEditor.onReady( function() {
 *    alert('hello, page editor is ready.')
 * });
 * </pre>
 * Modules can register multiple callbacks this way. The order in which callbacks are fired is the same in which they were registered.
 *<p>
 * @version $Id$
 *
 * TODO clean up/refactoring: this class is getting messy.
 */
public class PageEditor extends HTML implements EventListener, EntryPoint {

    public static final String SKIP_PAGE_EDITOR_DOM_PROCESSING = "skipPageEditorDOMProcessing";

    private boolean pageEditBarAlreadyProcessed = false;
    private String locale;
    private static ModelStorage storage = ModelStorage.getInstance();
    private LinkedList<MgnlElement> mgnlElements = new LinkedList<MgnlElement>();

    @Override
    public void onModuleLoad() {

        if(Window.Location.getParameter(SKIP_PAGE_EDITOR_DOM_PROCESSING) != null) {
            GWT.log("Found " + SKIP_PAGE_EDITOR_DOM_PROCESSING + " in request, skipping DOM processing...");
            postProcessLinksOnMobilePreview(Document.get().getDocumentElement());
            return;
        }

        locale = LegacyJavascript.detectCurrentLocale();

        long startTime = System.currentTimeMillis();
        processCmsComments(Document.get().getDocumentElement(), null);
        GWT.log("Time spent to process cms comments: " + (System.currentTimeMillis() - startTime) + "ms");
        cleanRootElements();
        storage.getFocusModel().reset();

        RootPanel.get().addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {

                storage.getFocusModel().onMouseUp((Element)event.getNativeEvent().getEventTarget().cast());
                event.stopPropagation();
            }
        }, MouseUpEvent.getType());

        RootPanel.get().addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {

                storage.getFocusModel().onMouseDown((Element)event.getNativeEvent().getEventTarget().cast());
                event.stopPropagation();
            }
        }, MouseDownEvent.getType());

        GWT.log("Trying to run onPageEditorReady callbacks...");
        onPageEditorReady();
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

    //TODO we will get the channel type and consequently the style to be passed to PreviewWidget via UI (dropdown/split button?)
    //What we ideally would need is a split/menu button like these http://www.sencha.com/examples/pages/button/buttons.html
    public void createChannelPreview(final String channelType, final String deviceType, final Orientation orientation) {
        GWT.log("Creating preview for channel type [" + channelType + "] ");
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        urlBuilder.setParameter("mgnlChannel", channelType);
        urlBuilder.setParameter(SKIP_PAGE_EDITOR_DOM_PROCESSING, "true");
        final PreviewChannelWidget previewChannelWidget = new PreviewChannelWidget(urlBuilder.buildString(), orientation, deviceType);
        //this causes the pop up to show
        previewChannelWidget.center();
    }

    private void processCmsComments(Node node, MgnlElement mgnlElement) {
        for (int i = 0; i < node.getChildCount(); i++) {
            Node childNode = node.getChild(i);
            if (childNode.getNodeType() == Comment.COMMENT_NODE) {

                try {

                    CMSComment comment = new CMSComment(((Comment)childNode.cast()).getData());

                    // this should be refactored after cms:edit tag is gone

                    if (comment.getTagName().equals("cms:edit") && !comment.isClosing()) {

                        GWT.log("processing comment " + comment);
                        //We assume the first cms:edit we encounter in DOM is the page edit bar.
                        if (!pageEditBarAlreadyProcessed) {
                            GWT.log("element was detected as page edit bar. Injecting it...");
                            PageBarWidget pageBarWidget = new PageBarWidget(this, comment);
                            pageBarWidget.attach(childNode);
                            pageEditBarAlreadyProcessed = true;

                            storage.addEditBar(mgnlElement, pageBarWidget);


                            if (pageBarWidget.isPreviewState()) {
                                //we just need the preview bar here
                                GWT.log("We're in preview mode, stop processing DOM.");
                                break;
                            }
                        }
                        else if (mgnlElement != null && mgnlElement.isComponent()) {
                            GWT.log("element is a plain edit bar. Injecting it...");
                            EditBarWidget editBarWidget = new EditBarWidget(mgnlElement, comment, this);

                            editBarWidget.attach(childNode);
                            storage.addEditBar(mgnlElement, editBarWidget);

                        }
                        else if (mgnlElement != null && mgnlElement.isArea()) {
                            GWT.log("element was detected as area edit bar. Injecting it...");

                            AreaBarWidget areaBarWidget = new AreaBarWidget(mgnlElement, comment, this);
                            if (areaBarWidget.hasControls) {
                                areaBarWidget.attach(childNode);
                                storage.addEditBar(mgnlElement, areaBarWidget);
                            }
                            AbstractOverlayWidget overlay = new AreaOverlayWidget(mgnlElement);
                            overlay.attach();
                            storage.addOverlay(mgnlElement, overlay);

                        }
                    }
                    else if (mgnlElement != null && comment.isClosing() && !comment.getTagName().equals("cms:edit")) {

                            mgnlElement = mgnlElement.getParent();
                    }
                    else {
                        try {
                            mgnlElement = new MgnlElement(comment, mgnlElement);

                            if (mgnlElement.getParent() == null) {
                                storage.addRoot(mgnlElement);
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
                catch (IllegalArgumentException e) {
                    GWT.log("Not CMSComment element, skipping: " + e.toString());

                }
                catch (Exception e) {
                    GWT.log("Caught undefined exception: " + e.toString());

                }
            }
            else if (childNode.getNodeType() == Element.ELEMENT_NODE && mgnlElement != null) {

                Element element = childNode.cast();
                if (element.hasTagName("A")) {
                    disableLink(element);
                }
                storage.addElement(mgnlElement, element);

                if (!element.getClassName().equals("mgnlAreaEditBar") && !element.getClassName().equals("mgnlEditBar") && element.getStyle().getDisplay().compareToIgnoreCase(Style.Display.NONE.toString()) != 0 && element.getOffsetHeight() != 0) {
                    MgnlElement area = mgnlElement;
                    if (mgnlElement.isComponent()) {
                        area = mgnlElement.getParentArea();
                    }
                    if (area != null) {
                        if (area.getFirstElement() == null) {
                            area.setFirstElement(element);
                        }
                        if (area.getLastElement() == null || !area.getLastElement().isOrHasChild(element)) {
                            area.setLastElement(element);
                        }
                    }
                }

            }

            processCmsComments(childNode, mgnlElement);
        }

    }

    //FIXME submitting forms still renders website channel and edit bars
    private void postProcessLinksOnMobilePreview(Element root) {
        NodeList<Element> anchors = root.getElementsByTagName("a");
        String mobilePreviewParams = "mgnlChannel=mobile&skipPageEditorDOMProcessing=true";
        for (int i = 0; i < anchors.getLength(); i++) {
            AnchorElement anchor = AnchorElement.as(anchors.getItem(i));

            GWT.log("Starting to process link " + anchor.getHref());

            if(LegacyJavascript.isEmpty(anchor.getHref())) {
                continue;
            }
            String manipulatedHref = anchor.getHref().replaceFirst(Window.Location.getProtocol() + "//" + Window.Location.getHost(), "");
            String queryString = Window.Location.getQueryString() != null ? Window.Location.getQueryString() : "";

            GWT.log("query string is " + queryString);

            String queryStringRegex =  queryString.replaceFirst("\\?", "\\\\?");
            manipulatedHref = manipulatedHref.replaceFirst(queryStringRegex, "");
            int indexOfHash = manipulatedHref.indexOf("#");

            if(indexOfHash != -1) {
                manipulatedHref = manipulatedHref.substring(indexOfHash);
            } else {
                if(queryString.startsWith("?")) {
                    queryString += "&" + mobilePreviewParams;
                } else {
                    queryString = "?" + mobilePreviewParams;
                }
                manipulatedHref += queryString;
            }
            GWT.log("Resulting link is " + manipulatedHref);
            anchor.setHref(manipulatedHref);
        }
        /*NodeList<Element> forms = root.getElementsByTagName("form");

        for (int i = 0; i < forms.getLength(); i++) {
            FormElement form = FormElement.as(forms.getItem(i));
            form.setAction(form.getAction().concat("?"+SKIP_PAGE_EDITOR_DOM_PROCESSING+"=true&mgnlChannel=mobile"));
        }*/
    }

    public native void disableLink(Element element) /*-{
        if (element.onclick == null) {
            element.onclick = function() {
              return false;
            };
        }
      }-*/;

    private void cleanRootElements() {

        List<MgnlElement> newRoots = new LinkedList<MgnlElement>();
        GWT.log(String.valueOf(storage.rootElements.size()));
        Iterator<MgnlElement> it = storage.rootElements.iterator();
        while (it.hasNext()) {
            MgnlElement root = it.next();
            if (storage.getEditBar(root) == null) {
                for (MgnlElement child : root.getChildren()) {
                        child.setParent(null);
                        newRoots.add(child);
                }
                it.remove();
            }
        }
        GWT.log(String.valueOf(storage.rootElements.size()));

        storage.rootElements.addAll(newRoots);
        GWT.log(String.valueOf(storage.rootElements.size()));

    }

    private native void onPageEditorReady() /*-{
        var callbacks = $wnd.mgnl.PageEditor.onPageEditorReadyCallbacks
        if(typeof callbacks != 'undefined') {
             for(var i=0; i < callbacks.length; i++) {
                callbacks[i].apply()
             }
         }
    }-*/;

}
