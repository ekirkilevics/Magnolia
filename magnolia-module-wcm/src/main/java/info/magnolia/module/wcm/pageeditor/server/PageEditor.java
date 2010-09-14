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
package info.magnolia.module.wcm.pageeditor.server;

import info.magnolia.module.wcm.pageeditor.client.VPageEditor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;

/**
 * Server side page editor component. Will receive the uuids from the client and create the edit bars.
 */
@ClientWidget(VPageEditor.class)
public class PageEditor extends AbstractComponentContainer {
    private Map<String, Component> editBars = new HashMap<String, Component>();

    private ToolBox toolBox = new ToolBox();
    
    /**
     * Set if a paragraph has to be updated after saving.
     */
    private String paragraphUUID;

    private String paragraphContent;

    @Override
    public void attach() {
        toolBox.show(getWindow());
    }

    public void replaceComponent(Component oldComponent, Component newComponent) {
        throw new UnsupportedOperationException("not implemented");
    }

    public void changeVariables(Object source, Map<String, Object> variables) {
        HashSet<String> uuidsAtTheClient = new HashSet<String>();
        uuidsAtTheClient.addAll(Arrays.asList((String[]) variables.get("uuids")));

        boolean serverIsUpToDate = editBars.keySet().equals(uuidsAtTheClient);

        if(!serverIsUpToDate){
            // delete old bars
            for (String uuid : editBars.keySet()) {
                if(!uuidsAtTheClient.contains(uuid)){
                    removeComponent(editBars.get(uuid));
                    editBars.remove(uuid);
                }
            }
            // create new bars
            for (String uuid : uuidsAtTheClient) {
                if(!editBars.containsKey(uuid)){
                    Component editBar = new EditBar(uuid);
                    editBars.put(uuid, editBar);
                    addComponent(editBar);
                }
            }
        }
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        for (Component editBar : editBars.values()) {
            editBar.paint(target);
        }
        target.addVariable(this, "uuids", editBars.keySet().toArray(new String[editBars.size()]));
        
        // inform about the updated paragraph
        if(paragraphUUID != null){
            target.addVariable(this, "paragraphUUID", paragraphUUID);
            target.addVariable(this, "paragraphContent", paragraphContent);
            paragraphUUID = null;
            paragraphContent = null;
        }
    }

    public Iterator<Component> getComponentIterator() {
        return editBars.values().iterator();
    }

    /**
     * Shows detail information about the paragraph in the toolbox.
     */
    public void showParagraphInfo(final String uuid, final ClickEvent event) {
        toolBox.setPositionX(event.getClientX());
        toolBox.setPositionY(event.getClientY());
        toolBox.showParagraphInfo(getWindow(), uuid);
    }

    /**
     * Will inform the client side editor about the new paragraph content. The editor will update the HTML page.
     */
    public void updateParagraph(String uuid, String paragraphContent) {
        this.paragraphUUID = uuid;
        this.paragraphContent = paragraphContent;
        // to make sure paintContent(..) is called
        requestRepaint();
    }

}
