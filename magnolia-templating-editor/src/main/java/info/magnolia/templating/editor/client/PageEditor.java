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


import info.magnolia.templating.editor.client.dom.Comment;
import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.dom.processor.CommentProcessor;
import info.magnolia.templating.editor.client.dom.processor.ElementProcessor;
import info.magnolia.templating.editor.client.dom.processor.MgnlElementProcessor;
import info.magnolia.templating.editor.client.dom.processor.MgnlElementProcessorFactory;
import info.magnolia.templating.editor.client.jsni.JavascriptUtils;
import info.magnolia.templating.editor.client.model.ModelStorage;
import info.magnolia.templating.editor.client.widget.PreviewChannel;
import info.magnolia.templating.editor.client.widget.PreviewChannel.Orientation;
import info.magnolia.templating.editor.client.widget.controlbar.AbstractBar;


import java.util.LinkedList;
import java.util.List;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
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
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.Window.ScrollHandler;
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
public class PageEditor extends HTML implements EntryPoint {

    private static final String MGNL_PREVIEW_ATTRIBUTE = "mgnlPreview";
    private static final String MGNL_INTERCEPT_ATTRIBUTE = "mgnlIntercept";
    private static final String MGNL_CHANNEL_ATTRIBUTE = "mgnlChannel";

    private static String locale;
    public final static ModelStorage model = ModelStorage.getInstance();
    private LinkedList<MgnlElement> mgnlElements = new LinkedList<MgnlElement>();

    // In case we're in preview mode, we will stop processing the document, after the pagebar has been injected.
    public static boolean process = true;
    private static boolean isPreview= false;

    @Override
    public void onModuleLoad() {

        String mgnlChannel = Window.Location.getParameter(MGNL_CHANNEL_ATTRIBUTE);
        if(mgnlChannel != null) {
            GWT.log("Found " + mgnlChannel + " in request, post processing links...");
            postProcessLinksOnMobilePreview(Document.get().getDocumentElement(), mgnlChannel);
            return;
        }

        // save x/y positon
        Window.addWindowScrollHandler(new ScrollHandler() {

            @Override
            public void onWindowScroll(ScrollEvent event) {
                Cookies.setCookie("editor-position", event.getScrollLeft() + ":" + event.getScrollTop());
            }
        });

        String position = Cookies.getCookie("editor-position");
        if(position!=null){
            String[] tokens = position.split(":");
            int left = Integer.parseInt(tokens[0]);
            int top = Integer.parseInt(tokens[1]);
            Window.scrollTo(left, top);
        }

        locale = JavascriptUtils.detectCurrentLocale();

        long startTime = System.currentTimeMillis();
        processDocument(Document.get().getDocumentElement(), null);

        processMgnlElements();
        GWT.log("Time spent to process cms comments: " + (System.currentTimeMillis() - startTime) + "ms");

        model.getFocusModel().reset();

        String contentId = Cookies.getCookie("editor-content-id");
        if(contentId != null){
            MgnlElement selectedMgnlElement = model.findMgnlElementByContentId(contentId);
            if(selectedMgnlElement != null){
                model.setSelectedMgnlElement(selectedMgnlElement);
                model.getFocusModel().toggleRootAreaBar(false);
                model.getFocusModel().toggleSelection(selectedMgnlElement, true);
            }
            else{
                Cookies.removeCookie("editor-content-id");
            }
        }

        RootPanel.get().addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {

                model.getFocusModel().onMouseUp((Element)event.getNativeEvent().getEventTarget().cast());
                event.stopPropagation();
            }
        }, MouseUpEvent.getType());

        RootPanel.get().addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {

                model.getFocusModel().onMouseDown((Element)event.getNativeEvent().getEventTarget().cast());
                event.stopPropagation();
            }
        }, MouseDownEvent.getType());

        GWT.log("Trying to run onPageEditorReady callbacks...");
        onPageEditorReady();
    }

    /**
     * TODO: rename and/or remove arguments no longer needed (collectionName, nodeName).
     */
    public static void openDialog(String dialog, String workspace, String path, String collectionName, String nodeName) {
        if (collectionName == null) {
            collectionName = "";
        }
        if (nodeName == null) {
            nodeName = "";
        }

        JavascriptUtils.mgnlOpenDialog(path, collectionName, nodeName, dialog, workspace, "", "", "", locale);
    }

    public static void moveComponentStart(String id) {
        JavascriptUtils.mgnlMoveNodeStart(id);
    }

    public static void moveComponentEnd(AbstractBar source, String path) {
        JavascriptUtils.mgnlMoveNodeEnd(source.getElement(), path);
    }

    public static void moveComponentOver(AbstractBar source) {
        JavascriptUtils.mgnlMoveNodeHigh(source.getElement());
    }

    public static void moveComponentOut(AbstractBar source) {
        JavascriptUtils.mgnlMoveNodeReset(source.getElement());
    }

    public static void deleteComponent(String path) {
        JavascriptUtils.mgnlDeleteNode(path);
    }

    public static void addComponent(String workspace, String path, String nodeName, String availableComponents) {

        // Not used anymore. The node is passed together with the path
        String collectionName = null;

        if (nodeName == null) {
            nodeName = "mgnlNew";
        }
        if (availableComponents == null) {
            availableComponents = "";
        }
        JavascriptUtils.mgnlOpenDialog(path, collectionName, nodeName, availableComponents, workspace, ".magnolia/dialogs/selectParagraph.html", "", "", locale);
    }

    public static void showTree(String workspace, String path) {
        JavascriptUtils.showTree(workspace, path);

    }

    public static void createComponent(String workspace, String path, String itemType) {
        GWT.log("Creating [" + itemType + "] in workspace [" + workspace + "] at path [" + path + "]");

        final StringBuilder url = new StringBuilder();
        url.append(JavascriptUtils.getContextPath() + ".magnolia/pageeditor/PageEditorServlet");
        url.append("?action=create");
        url.append("&workspace=" + workspace);
        url.append("&path=" + path);
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

    public static void createChannelPreview(final String channelType, final String deviceType, final Orientation orientation) {
        GWT.log("Creating preview for channel type [" + channelType + "] ");
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        urlBuilder.setParameter(MGNL_CHANNEL_ATTRIBUTE, channelType);
        final PreviewChannel previewChannelWidget = new PreviewChannel(urlBuilder.buildString(), orientation, deviceType);
        //this causes the pop up to show
        previewChannelWidget.center();
    }

    private void processDocument(Node node, MgnlElement mgnlElement) {
        if(process) {
            for (int i = 0; i < node.getChildCount(); i++) {
                Node childNode = node.getChild(i);
                if (childNode.getNodeType() == Comment.COMMENT_NODE) {

                    try {
                        mgnlElement = CommentProcessor.process(childNode, mgnlElement);
                    }
                    catch (IllegalArgumentException e) {
                        GWT.log("Not CMSComment element, skipping: " + e.toString());

                    }
                    catch (Exception e) {
                        GWT.log("Caught undefined exception: " + e.toString());
                    }
                }
                else if (childNode.getNodeType() == Node.ELEMENT_NODE && mgnlElement != null) {
                    ElementProcessor.process(childNode, mgnlElement);
                }

                processDocument(childNode, mgnlElement);
            }
        }
    }



    private void processMgnlElements() {
        List<MgnlElement> rootElements = new LinkedList<MgnlElement>(model.getRootElements());
        for (MgnlElement root : rootElements) {
            LinkedList<MgnlElement> elements = new LinkedList<MgnlElement>();
            elements.add(root);
            elements.addAll(root.getDescendants());

            for (MgnlElement mgnlElement : elements) {
                try {
                    MgnlElementProcessor processor = MgnlElementProcessorFactory.getProcessor(mgnlElement);
                    processor.process();
                }
                catch (IllegalArgumentException e) {
                    GWT.log("MgnlFactory could not instantiate class. The element is neither an area nor component.");
                }
            }
        }

    }

    //FIXME submitting forms still renders website channel and edit bars
    private void postProcessLinksOnMobilePreview(Element root, String channel) {
        NodeList<Element> anchors = root.getElementsByTagName("a");

        final String mobilePreviewParams = MGNL_CHANNEL_ATTRIBUTE+"="+channel;

        for (int i = 0; i < anchors.getLength(); i++) {
            AnchorElement anchor = AnchorElement.as(anchors.getItem(i));

            GWT.log("Starting to process link " + anchor.getHref());

            if(JavascriptUtils.isEmpty(anchor.getHref())) {
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
                if(!queryString.contains(mobilePreviewParams)) {
                    if(queryString.startsWith("?")) {
                        queryString += "&" + mobilePreviewParams;
                    } else {
                        queryString = "?" + mobilePreviewParams;
                    }
                }
                manipulatedHref += queryString;
            }
            GWT.log("Resulting link is " + manipulatedHref);
            anchor.setHref(manipulatedHref);
        }
        NodeList<Element> forms = root.getElementsByTagName("form");

        for (int i = 0; i < forms.getLength(); i++) {
            FormElement form = FormElement.as(forms.getItem(i));
            form.setAction(form.getAction().concat("?"+ mobilePreviewParams));
        }
    }


    private native void onPageEditorReady() /*-{
        var callbacks = $wnd.mgnl.PageEditor.onPageEditorReadyCallbacks
        if(typeof callbacks != 'undefined') {
             for(var i=0; i < callbacks.length; i++) {
                callbacks[i].apply()
             }
         }
    }-*/;

    public static void enablePreview(boolean preview) {
        setPreview(preview);
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        GWT.log("Current url is [" + urlBuilder.buildString() + "], setting preview to " + isPreview());

        //always cleanup the url
        urlBuilder.removeParameter(MGNL_PREVIEW_ATTRIBUTE);
        urlBuilder.removeParameter(MGNL_INTERCEPT_ATTRIBUTE);

        urlBuilder.setParameter(MGNL_INTERCEPT_ATTRIBUTE, "PREVIEW");
        urlBuilder.setParameter(MGNL_PREVIEW_ATTRIBUTE, String.valueOf(isPreview()));

        final String newUrl = urlBuilder.buildString();
        GWT.log("New url is [" + newUrl + "]");

        Window.Location.replace(newUrl);
    }

    /**
     * @return <code>true</code> if the current page is in preview mode, <code>false</code> otherwise.
     */
    public static boolean isPreview() {
        return isPreview;
    }

    public static void setPreview(boolean preview) {
        isPreview = preview;
    }

}
