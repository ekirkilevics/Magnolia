/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.admincentral.dialog;

import javax.jcr.RepositoryException;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;

/**
 * Window for selecting one out of a set of paragraphs.
 */
public class SelectParagraphWindow extends Window {

    private String repository;
    private String path;
    private String nodeCollection;
    private String nodeName;

    public SelectParagraphWindow(String paragraphs, String repository, String path, String nodeCollection, String nodeName) {
        this.repository = repository;
        this.path = path;
        this.nodeCollection = nodeCollection;
        this.nodeName = nodeName;

        setModal(true);
        setResizable(false);
        setScrollable(false);
        setClosable(false);
        setWidth("400px");
        setCaption("Select paragraph");

        final OptionGroup group = new OptionGroup();

        for (String paragraph : paragraphs.split(",")) {

            Paragraph paragraphInfo = ParagraphManager.getInstance().getParagraphDefinition(paragraph);

            final Messages msgs = MessagesManager.getMessages(paragraphInfo.getI18nBasename());

            String title = msgs.getWithDefault(paragraphInfo.getTitle(), paragraphInfo.getTitle());

            group.addItem(paragraph);
            group.setItemCaption(paragraph, title);
        }

        Button select = new Button("Select", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                String selected = (String) group.getValue();
                if (selected != null) {
                    try {
                        openEditDialog(selected);
                    } catch (RepositoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

                closeWindow();
            }
        });

        Button cancel = new Button("Cancel", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                closeWindow();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(select);
        buttons.setComponentAlignment(select, "right");
        buttons.addComponent(cancel);
        buttons.setComponentAlignment(cancel, "right");

        VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(group);
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, "right");
    }

    private void openEditDialog(String selectedParagraph) throws RepositoryException {
        Window window = new EditParagraphWindow(selectedParagraph, repository, path);
        this.getApplication().getMainWindow().addWindow(window);
    }

    private void closeWindow() {
        // close the window by removing it from the parent window
        ((Window) getParent()).removeWindow(this);
    }
}
