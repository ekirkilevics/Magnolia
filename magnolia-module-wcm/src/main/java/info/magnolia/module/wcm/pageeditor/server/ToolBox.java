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

import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

/**
 * The tool box providing actions and additional information.
 * @version $Id$
 */
public class ToolBox extends Window implements CloseListener {
    
    private Form actionTab;
    private Form infoTab;

    public ToolBox() {
        super("ToolBox", new VerticalLayout());
        
        setVisible(false);
        addListener((CloseListener)this);
        
        setHeight("400px");
        setPositionX(600);
        setPositionY(100);

        actionTab = createActionTab();
        addComponent(actionTab);
        
        infoTab = createInfoTab();
        addComponent(infoTab);
    }

    private Form createInfoTab() {
        Form form = new Form();
        form.setCaption("Info");
        Label name = new Label("Som indo");
        name.setCaption("caption");
        name.setDescription("description");
        form.getLayout().addComponent(name);
        return form;
        
    }

    private Form createActionTab() {
        Form form = new Form();
        form.setCaption("Actions");
        
        form.getLayout().addComponent(createLink("Activate"));
        form.getLayout().addComponent(createLink("Move"));
        form.getLayout().addComponent(createLink("Create Subpage"));
        return form;
    }

    private Button createLink(String caption) {
        Button link = new Button(caption);
        link.setStyleName(BaseTheme.BUTTON_LINK);
        return link;
    }
    
    public void showParagraphInfo(Window window, String uuid){
        infoTab.getLayout().removeAllComponents();
        Label name = new Label(uuid);
        name.setCaption("caption");
        name.setDescription("description");
        infoTab.getLayout().addComponent(name);
        
        // paragraph name
        
        // from metadata
        // modification date
        // user
        show(window);
        
    }

    public void show(Window window) {
        if(!isVisible()){
            setVisible(true);
            window.addWindow(this);
        }
    }

    public void windowClose(CloseEvent e) {
        this.setVisible(false);
    }
    
    
}
