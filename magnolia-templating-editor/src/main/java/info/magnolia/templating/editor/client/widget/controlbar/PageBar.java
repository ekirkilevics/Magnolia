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
package info.magnolia.templating.editor.client.widget.controlbar;


import static info.magnolia.templating.editor.client.jsni.JavascriptUtils.getI18nMessage;
import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.CMSComment;
import info.magnolia.templating.editor.client.jsni.JavascriptUtils;
import info.magnolia.templating.editor.client.model.ModelStorage;
import info.magnolia.templating.editor.client.widget.PreviewChannel.Orientation;
import info.magnolia.templating.editor.client.widget.button.LocaleSelector;
import info.magnolia.templating.editor.client.widget.button.PreviewButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.MenuItem;

/**
 * Page bar. The HTML output by this widget contains an empty <code>span</code> element with an id called <code>mgnlEditorMainbarPlaceholder</code> as a convenience which can be used by other modules to inject
 * their own DOM elements into the main bar, <strong>once the page editor is loaded (see {@link PageEditor} and <code>mgnl.PageEditor.onReady(..)</code>)</strong>.
 * <p>I.e., assuming usage of jQuery, a module's own javascript could do something like this
 * <p>
 * {@code
 *  jQuery('#mgnlEditorMainbarPlaceholder').append('<p>Blah</p>')
 * }
 * <p>The placeholder is styled to be automatically centered in the main bar. See this module's editor.css file (id selector #mgnlEditorMainbarPlaceholder).
 *
 * TODO: review and clean up, especially the private Command classes.
 */
public class PageBar extends AbstractBar {


    private String workspace;
    private String path;
    private String dialog;
    private String currentURI;
    private Map<String,String> availableLocales = new HashMap<String, String>();

    public PageBar(final CMSComment comment) {
        super(null);

        String content = comment.getAttribute("content");
        int i = content.indexOf(':');
        workspace = content.substring(0, i);
        path = content.substring(i + 1);
        dialog = comment.getAttribute("dialog");

        currentURI = comment.getAttribute("currentURI");

        boolean isPreview = Boolean.parseBoolean(comment.getAttribute("preview"));
        PageEditor.setPreview(isPreview);

        if(PageEditor.isPreview()){
            createPreviewModeBar();
        } else {

            String availableLocalesAttribute = comment.getAttribute("availableLocales");

            if(JavascriptUtils.isNotEmpty(availableLocalesAttribute)) {
                String[] localeAndUris = availableLocalesAttribute.split(",");

                for(String localeAndUri: localeAndUris) {
                    String[] tmp = localeAndUri.split(":");
                    if(tmp.length != 2) {
                        GWT.log("Could not split string [" + tmp + "] while getting locales and uris");
                        continue;
                    }
                    GWT.log("Found available locale [" + tmp[0] + "," + tmp[1] + "]");
                    availableLocales.put(tmp[0],tmp[1]);
                }
            }
            createAuthoringModeBar();
        }

        addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                ModelStorage.getInstance().getFocusModel().toggleRootAreaBar(true);
                event.stopPropagation();
            }
        }, MouseDownEvent.getType());

        addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                event.stopPropagation();
            }
        }, MouseUpEvent.getType());

    }

    private void createAuthoringModeBar() {
        InlineLabel mainbarPlaceholder = new InlineLabel();
        mainbarPlaceholder.getElement().setId("mgnlEditorMainbarPlaceholder");
        mainbarPlaceholder.setStylePrimaryName("mgnlMainbarPlaceholder");
        //the placeholder must be added as the first child of the bar element (before the buttons wrapper) so that the style applied to it centers it correctly.
        getElement().insertFirst(mainbarPlaceholder.getElement());


        Button properties = new Button(getI18nMessage("buttons.properties.js"));
        properties.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PageEditor.openDialog(dialog, workspace, path);
            }
        });
        addButton(properties, Float.RIGHT);

        if(!availableLocales.isEmpty()) {
            LocaleSelector localeSelector = new LocaleSelector(availableLocales, currentURI);
            addButton(localeSelector, Float.RIGHT, "mgnlEditorLocaleSelector");
        }

        MenuItem desktop = new MenuItem(getI18nMessage("buttons.preview.desktop.js"), true, new DesktopPreviewCommand());
        desktop.addStyleName("desktop");
        MenuItem smartphone = new MenuItem(getI18nMessage("buttons.preview.smartphone.js"), true, new SmartphonePreviewCommand(Orientation.PORTRAIT));
        smartphone.addStyleName("smartphone");
        MenuItem tablet = new MenuItem(getI18nMessage("buttons.preview.tablet.js"), true, new TabletPreviewCommand(Orientation.LANDSCAPE));
        tablet.addStyleName("tablet");

        List<MenuItem> options = new ArrayList<MenuItem>();
        options.add(desktop);
        options.add(smartphone);
        options.add(tablet);

        PreviewButton preview = new PreviewButton(getI18nMessage("buttons.preview.js"), new DesktopPreviewCommand(), options);
        preview.setTitle(getI18nMessage("buttons.preview.switchToPreview.js"));

        addButton(preview, Float.LEFT, "mgnlEditorPreviewButton");

        Button adminCentral = new Button(getI18nMessage("buttons.admincentral.js"));
        adminCentral.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PageEditor.showTree(workspace, path);
            }
        });
        addButton(adminCentral, Float.LEFT);

        addStyleName("mgnlEditorMainbar");

    }

    private void createPreviewModeBar() {
        Button preview = new Button(getI18nMessage("buttons.preview.hidden.js"));
        preview.setTitle(getI18nMessage("buttons.preview.switchToEdit.js"));

        preview.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PageEditor.enablePreview(false);
            }
        });
        addButton(preview, Float.LEFT);
        setStyleName("mgnlEditorMainbarPreview");
    }

    private class SmartphonePreviewCommand implements Command {

        private Orientation orientation;

        public SmartphonePreviewCommand(final Orientation orientation) {
           this.orientation = orientation;
        }

        @Override
        public void execute() {
            PageEditor.createChannelPreview("smartphone", orientation);
        }
    }

    private class DesktopPreviewCommand implements Command {

        @Override
        public void execute() {
            PageEditor.enablePreview(true);
        }
    }

    @Override
    public void attach() {
        Document.get().getBody().insertFirst(getElement());
        onAttach();
    }

    private class TabletPreviewCommand implements Command {

        private Orientation orientation;

        public TabletPreviewCommand(final Orientation orientation) {
           this.orientation = orientation;
        }

        @Override
        public void execute() {
            PageEditor.createChannelPreview("tablet", orientation);
        }
    }
}
