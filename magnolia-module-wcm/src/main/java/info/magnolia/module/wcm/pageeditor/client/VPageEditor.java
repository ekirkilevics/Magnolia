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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

    private Map<String, Element> editBarDivs = new HashMap<String, Element>();

    private Map<String, VEditBar> editBars = new HashMap<String, VEditBar>();

    private String pid;

    public VPageEditor() {
        // ApplicationConnection.registerPaintable(String, Paintable) expects that the widget has an element assigned
        Element div = DOM.createDiv();
        div.getStyle().setProperty("visibility", "hidden");
        this.setElement(div);

        editBarDivs = findDivs();
    }

    private Map<String, Element> findDivs() {
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

        // test if the server has the correct state
        HashSet<String> uuidsAtTheServer = new HashSet<String>();
        uuidsAtTheServer.addAll(Arrays.asList(uidl.getStringArrayVariable("uuids")));
        
        boolean serverIsUpToDate = uuidsAtTheServer.equals(editBarDivs.keySet());

        if(!serverIsUpToDate){
            updateServer();
        }
        // the server is up to date, are we up to date too?
        else {
            for (final Iterator<Object> it = uidl.getChildIterator(); it.hasNext();) {
                final UIDL childUIDL = (UIDL) it.next();
                final VEditBar editBar = (VEditBar) client.getPaintable(childUIDL);
                if(!editBars.containsValue(editBar)){
                    // updateFromUIDL was not yet called so we have to read the attribute manualy
                    String uuid = childUIDL.getStringAttribute("uuid");
                    Element editBarDiv = editBarDivs.get(uuid);

                    // we have first to wire the widget sets, otherwise Vaadin will make them of size 0:0
                    add((Widget) editBar, (com.google.gwt.user.client.Element)editBarDiv.cast());
                    
                    editBars.put(uuid, editBar);
                }
                
                // now update the edit bars, this will set the attributes
                editBar.updateFromUIDL(childUIDL, client);
            }
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
