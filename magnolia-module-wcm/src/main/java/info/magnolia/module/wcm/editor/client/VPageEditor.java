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
package info.magnolia.module.wcm.editor.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * TODO: Add javadoc.
 */
public class VPageEditor extends HTML implements Paintable, EventListener {

    private IFrameElement iFrameElement;

    public VPageEditor() {
        iFrameElement = Document.get().createIFrameElement();
        iFrameElement.setAttribute("width", "100%");
        iFrameElement.setAttribute("height", "100%");
        iFrameElement.setFrameBorder(0);
        getElement().appendChild(iFrameElement);

        hookEvents(iFrameElement, this);
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        String url = uidl.getStringAttribute("url");

        iFrameElement.setSrc(url);
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
    }

    /**
     * Inspired by {@link com.google.gwt.user.client.ui.impl.FormPanelImpl}.
     *
     * TODO probably doesn't work in IE6 as FormPanelImpl has a special impl for it.
     */
    public native void hookEvents(Element iframe, VPageEditor listener) /*-{
        if (iframe) {
            iframe.onload = $entry(function() {
                listener.@info.magnolia.module.wcm.editor.client.VPageEditor::onFrameLoad()();
            });
        }
    }-*/;

    public void onFrameLoad() {
        Window.alert("Bingo");
        Window.alert(iFrameElement.getContentDocument().getDocumentElement().getInnerHTML());
    }
}
