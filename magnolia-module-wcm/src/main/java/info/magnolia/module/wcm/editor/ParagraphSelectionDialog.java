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
package info.magnolia.module.wcm.editor;

import org.apache.commons.lang.StringUtils;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;

/**
 * Dialog for selecting a paragraph to add.
 *
 * @version $Id$
 */
public class ParagraphSelectionDialog extends Window {

    private OptionGroup optionGroup;

    public ParagraphSelectionDialog(String paragraphs) {

        setCaption("Select paragraph");
        setModal(true);
        setResizable(true);
        setClosable(false);
        setWidth("800px");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        Button save = new Button("Save", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                String paragraphName = (String) optionGroup.getValue();

                // TODO validate that something is selected

                Paragraph paragraph = ParagraphManager.getInstance().getParagraphDefinition(paragraphName);

                if (paragraph != null) {

                    close();

                    onClosed(paragraph);
                }
            }
        });
        save.addStyleName("primary");
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.CTRL);
        buttons.addComponent(save);
        buttons.setComponentAlignment(save, "right");

        Button cancel = new Button("Cancel", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        buttons.addComponent(cancel);
        buttons.setComponentAlignment(cancel, "right");

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        // TODO use IoC
        ParagraphManager paragraphManager = ParagraphManager.getInstance();

        optionGroup = new OptionGroup();

        String[] paragraphsArray = StringUtils.split(paragraphs, ", \t\n");

        for (String paragraph : paragraphsArray) {
            Paragraph paragraphDefinition = paragraphManager.getParagraphDefinition(paragraph);
            if (paragraphDefinition == null) {
                continue;
            }

            optionGroup.addItem(paragraph);
            // TODO i18n
            optionGroup.setItemCaption(paragraph, paragraphDefinition.getTitle());
        }

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(optionGroup);
        layout.addComponent(horizontalLayout);

        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, "right");

        super.getContent().addComponent(layout);
    }

    protected void onClosed(Paragraph paragraph) {
    }
}
