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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;

import java.util.Calendar;
import java.util.Iterator;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

/**
 * The tool box providing actions and additional information.
 * @version $Id$
 */
public class ToolBox extends Window implements CloseListener {

    private static final long serialVersionUID = 1L;

    private static final Resource ACTION_ICON = new ExternalResource(MgnlContext.getContextPath()+ "/.resources/icons/24/gears.gif");
    private static final Resource INFO_ICON = new ExternalResource(MgnlContext.getContextPath()+ "/.resources/icons/24/view.gif");
    private Accordion accordion = new Accordion();
    private Tab actionTab;
    private Tab infoTab;

    public ToolBox() {
        super("ToolBox", new VerticalLayout());

        setVisible(false);
        addListener((CloseListener)this);

        setHeight("400px");
        setWidth("400px");
        setPositionX(600);
        setPositionY(100);

       actionTab  = createActionTab();
       infoTab = createInfoTab(null);
       this.addComponent(accordion);
    }

    private Tab createInfoTab(Content info) {
        if(info == null){
            final Label infoLabel = new Label("Please, click on a paragraph bar to get info about it.");
            return accordion.addTab(infoLabel, "Info", INFO_ICON);
        }

        final GridLayout layout = new GridLayout(2,4);
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.setWidth("100%");
        layout.setColumnExpandRatio(0, 5.0f);
        layout.setColumnExpandRatio(1, 10.0f);
        final Label name = new Label("Title");
        layout.addComponent(name, 0, 0);
        layout.addComponent(new Label(info.getTitle()), 1, 0);

        final MetaData metaData = info.getMetaData();

        final int activationStatus = metaData.getActivationStatus();
        final Label activationStatusLabel = new Label("Status");
        layout.addComponent(activationStatusLabel, 0, 1);
        final Embedded statusIcon = new Embedded();
        statusIcon.setType(Embedded.TYPE_IMAGE);

        switch(activationStatus){
            case MetaData.ACTIVATION_STATUS_ACTIVATED:
                statusIcon.setSource(new ExternalResource(MgnlContext.getContextPath() + "/.resources/icons/16/indicator_green.gif"));
                break;
            case MetaData.ACTIVATION_STATUS_MODIFIED:
                statusIcon.setSource(new ExternalResource(MgnlContext.getContextPath() + "/.resources/icons/16/indicator_yellow.gif"));
                break;
            case MetaData.ACTIVATION_STATUS_NOT_ACTIVATED:
                statusIcon.setSource(new ExternalResource(MgnlContext.getContextPath() + "/.resources/icons/16/indicator_red.gif"));
                break;
            default:
                    //TODO Raise exception?
        }
        layout.addComponent(statusIcon, 1, 1);

        final Label author = new Label("Author");
        final String authorId = metaData.getAuthorId();
        layout.addComponent(author, 0, 2);
        layout.addComponent(new Label(authorId), 1, 2);

        final Calendar modificationDate = metaData.getModificationDate();
        final Label modificationDateLabel = new Label("Mod. Date");
        layout.addComponent(modificationDateLabel, 0, 3);
        layout.addComponent(new Label(modificationDate.getTime().toString()), 1, 3);

        return accordion.addTab(layout, "Info", INFO_ICON);
    }

    private Tab createActionTab() {
        final GridLayout layout = new GridLayout(1,1);
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.addComponent(createLink("Activate", new ExternalResource(MgnlContext.getContextPath() + "/.resources/icons/16/arrow_right_green.gif")));
        layout.addComponent(createLink("Move", new ExternalResource(MgnlContext.getContextPath() + "/.resources/icons/16/up_down.gif")));
        layout.addComponent(createLink("Create Subpage", new ExternalResource(MgnlContext.getContextPath() + "/.resources/icons/16/document_plain_earth_add.gif")));
        return accordion.addTab(layout, "Actions", ACTION_ICON);
    }

    private Button createLink(String caption, Resource icon) {
        Button link = new Button(caption);
        link.setStyleName(BaseTheme.BUTTON_LINK);
        link.setIcon(icon);
        return link;
    }

    public void showParagraphInfo(Window window, String uuid){
        accordion.removeComponent(getComponentByTab(infoTab));
        Content content = ContentUtil.getContentByUUID("website", uuid);
        if(content == null){
            getApplication().getMainWindow().showNotification("No content returned for this paragraph", "Tried to retrieve content from website with uuid "+uuid, Notification.TYPE_WARNING_MESSAGE);
            return;
        }
        infoTab = createInfoTab(content);
        accordion.setSelectedTab(getComponentByTab(infoTab));
        show(window);
    }

    private Component getComponentByTab(Tab tab) {
        for(Iterator<Component> iter= accordion.getComponentIterator(); iter.hasNext();){
            final Component comp = iter.next();
            final Tab tmp = accordion.getTab(comp);
            if(tmp==tab){
                return comp;
            }
        }
        return null;
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
