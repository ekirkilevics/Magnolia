/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.templating.editor.client.widget;


import static info.magnolia.templating.editor.client.jsni.JavascriptUtils.getI18nMessage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * PreviewChannelWidget.
 * TODO javadoc bien sure!
 * TODO extract base class with common functionality. Make this a MobilePreviewChannelWidget extending an AbstractPreviewChannelWidget.
 */
public class PreviewChannel extends PopupPanel implements ClickHandler, HasClickHandlers {

    private String landscapeCssStyleSuffix = "Landscape";
    private String portraitCssStyleSuffix = "Portrait";
    private String channel = "";

    /**
     * Orientation modes for this widget.
     */
    public enum Orientation {
        PORTRAIT, LANDSCAPE
    }

    private Orientation currentOrientation = Orientation.LANDSCAPE;

    public PreviewChannel(final String url, final Orientation orientation, final String channel) {
        this.channel = channel;
        this.currentOrientation = orientation;

        setStylePrimaryName("mobilePreview");
        //TODO have a look at GWT add dependent style mechanism instead of doing it yourself.
        addStyleName(orientation == Orientation.LANDSCAPE ? channel + landscapeCssStyleSuffix : channel + portraitCssStyleSuffix);

        setAnimationEnabled(true);
        setAutoHideEnabled(true);
        setModal(true);
        setGlassEnabled(true);
        setGlassStyleName("mgnlEditorPreviewBackground");

        getElement().setTitle(getI18nMessage("editor.preview.rotate.js"));

        addClickHandler(this);

        final Frame iframe = new Frame(url);
        iframe.setStylePrimaryName("mobilePreviewIframe");
        iframe.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
               //TODO nice animation before displaying the page preview?
                //Window.alert("iframe onload");
            }
        });

        add(iframe);
    }

    public Orientation getOrientation() {
        return currentOrientation;
    }

    public String getDeviceType() {
        return channel;
    }

    public String getLandscapeCssStyleSuffix() {
        return landscapeCssStyleSuffix;
    }

    public String getPortraitCssStyleSuffix() {
        return portraitCssStyleSuffix;
    }

    @Override
    public void onClick(ClickEvent event) {
        //change orientation
        GWT.log("currentOrientation is "+currentOrientation.toString());

        if(currentOrientation == Orientation.LANDSCAPE) {
            currentOrientation = Orientation.PORTRAIT;
            removeStyleName(getDeviceType() + landscapeCssStyleSuffix);
            addStyleName(getDeviceType() + portraitCssStyleSuffix);
        } else {
            currentOrientation = Orientation.LANDSCAPE;
            removeStyleName(getDeviceType() + portraitCssStyleSuffix);
            addStyleName(getDeviceType() + landscapeCssStyleSuffix);
        }
        center();
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    //key press or key down handlers have issues, that's why we had to resort to this. See also http://code.google.com/p/google-web-toolkit/issues/detail?id=5558.
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        if (event.getTypeInt() == Event.ONKEYDOWN && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
            hide();
        }
    }
}
