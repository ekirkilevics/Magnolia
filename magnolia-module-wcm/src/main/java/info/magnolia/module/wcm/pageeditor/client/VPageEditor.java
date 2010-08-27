/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.wcm.pageeditor.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;

/**
 * The page editor widget which scans for edit bar placeholder divs. Sends the found uuids back to the server.
 */
public class VPageEditor extends ComplexPanel implements Container {

    private ApplicationConnection client;

    private Map<String, Element> editBarDivs;

    private String pid;

    private boolean barsAdded = false;

    public VPageEditor() {
        // ApplicationConnection.registerPaintable(String, Paintable) expects that the widget has an element assigned
        Element div = DOM.createDiv();
        div.getStyle().setProperty("visibility", "hidden");
        this.setElement(div);

        editBarDivs = findDivs("editBar");
    }

    private Map<String, Element> findDivs(String string) {
        HashMap<String, Element> editBarDivs = new HashMap<String, Element>();
        // TODO use something link gquery
        NodeList<Element> divs = Document.get().getElementsByTagName("div");
        for (int i = 0; i < divs.getLength(); i++) {
            Element div = divs.getItem(i);
            if(div.getClassName().equals("editBar")){
                String uuid = div.getAttribute("data-uuid");
                editBarDivs.put(uuid, div);
            }
        }
        return editBarDivs;
    }

    /**
     * Called when ever we get new information from the server. In our case this is when on
     * initialization or when the server side edit bars got created.
     */
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if(this.client == null){
            this.pid = uidl.getId();
            this.client = client;
        }

        if(client.updateComponent(this, uidl, false)){
           return;
        }

        // send the found uuids to the server if he doesn't know them
        if(uidl.getStringArrayVariable("uuids").length != editBarDivs.size()){
            updateServer();
        }

        // we add the edit bar only once and not on every other change
        boolean addBarsNow = false;

        if(!barsAdded && uidl.getChildCount()>0){
            addBarsNow = true;
            barsAdded = true;
        }

        for (final Iterator<Object> it = uidl.getChildIterator(); it.hasNext();) {
            final UIDL childUIDL = (UIDL) it.next();
            final Paintable editBar = client.getPaintable(childUIDL);
            if(addBarsNow){

                String uuid = childUIDL.getStringAttribute("uuid");

                Element editBarDiv = editBarDivs.get(uuid);
                // we have first to wire the widget sets, otherwise Vaadin will make them of size 0:0
                add((Widget) editBar, (com.google.gwt.user.client.Element)editBarDiv.cast());
            }

            // now update the edit bars, this will set
            editBar.updateFromUIDL(childUIDL, client);
        }
    }

    private void updateServer() {
        String[] uuids = editBarDivs.keySet().toArray(new String[editBarDivs.keySet().size()]);
        client.updateVariable(pid, "uuids", uuids, true);
    }

    public RenderSpace getAllocatedSpace(Widget child) {
        return new RenderSpace(Util.getRequiredWidth(child),Util.getRequiredHeight(child));
    }

    public boolean hasChildComponent(Widget component) {
        return getWidgetIndex(component) != -1;
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean requestLayout(Set<Paintable> children) {
        throw new UnsupportedOperationException("not implemented");
    }

    public void updateCaption(Paintable component, UIDL uidl) {
        // ignore
    }



}
