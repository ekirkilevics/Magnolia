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
package info.magnolia.templating.editor.client;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * PreviewWidget.
 * FIXME: do it right.
 */
public class PreviewWidget extends PopupPanel {

    public PreviewWidget(final String url, final String style, boolean setUpPositionAndShow) {

        setStylePrimaryName(style);
        setGlassStyleName("mgnlEditorPreviewBackground");
        setAnimationEnabled(true);
        setAutoHideEnabled(true);
        setModal(true);
        setGlassEnabled(true);

        if(setUpPositionAndShow) {
            setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                public void setPosition(int offsetWidth, int offsetHeight) {
                  int left = (Window.getClientWidth() - offsetWidth) / 3;
                  int top = (Window.getClientHeight() - offsetHeight) / 3;
                  setPopupPosition(left, top);
                }
              });
        }

        final Frame iframe = new Frame(url);
        //TODO use style here
        iframe.getElement().getStyle().setCursor(Cursor.WAIT);
        iframe.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
               getElement().getStyle().clearCursor();

            }
        });
        iframe.setHeight("100%");
        iframe.setWidth("100%");
        add(iframe);
    }
}
