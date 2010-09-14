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

import java.io.StringWriter;

import info.magnolia.cms.core.Content;
import info.magnolia.module.admincentral.dialog.EditParagraphWindow;
import info.magnolia.module.templating.MagnoliaTemplatingUtilities;
import info.magnolia.module.wcm.pageeditor.client.VEditBar;

import javax.jcr.RepositoryException;

import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;

/**
 * The edit bar. Knows the uuid it is assigned to.
 */
@ClientWidget(VEditBar.class)
public class EditBar extends CustomComponent {
    private String uuid;

    public EditBar(final String uuid) {
        super();

        this.uuid = uuid;
        //setStyleName("editBar");

        GridLayout layout = new GridLayout(2, 1);
        Button editButton = new Button("Edit");
        editButton.setStyleName("small");
        editButton.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    EditParagraphWindow dialog = Hacks.getDialogWindow(uuid);
                    // render the new paragraph content after saving and inform the editor
                    dialog.setPostSaveListener(new EditParagraphWindow.PostSaveListener() {
                        public void postSave(Content paragraphNode) {
                            try {
                                StringWriter out = new StringWriter();
                                new MagnoliaTemplatingUtilities().renderParagraph(paragraphNode, out);
                                String paragraphContent = out.toString();
                                getPageEditor().updateParagraph(uuid, paragraphContent);
                            }
                            catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            //getApplication().getMainWindow().executeJavaScript("location.reload(true);");
                        }
                    });
                    getApplication().getMainWindow().addWindow(dialog);

                }
                catch (RepositoryException e) {
                    e.printStackTrace();
                    getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE );
                }

            }
        });

        layout.setWidth("100%");
        layout.addComponent(editButton,0,0);
        layout.setColumnExpandRatio(0, 1);

        Button deleteButton = new Button("Delete");
        deleteButton.setStyleName("small");
        layout.addComponent(deleteButton,1,0);
        layout.setColumnExpandRatio(1, 0);

        layout.setComponentAlignment(deleteButton, Alignment.MIDDLE_LEFT);

        Panel panel = new Panel(layout);
        panel.addListener(new ClickListener() {
            public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
                getPageEditor().showParagraphInfo(uuid);
            }
        });
        setCompositionRoot(panel);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        target.addAttribute("uuid", uuid);
        super.paintContent(target);
    }

    private PageEditor getPageEditor() {
        return (PageEditor)getParent();
    }

}
