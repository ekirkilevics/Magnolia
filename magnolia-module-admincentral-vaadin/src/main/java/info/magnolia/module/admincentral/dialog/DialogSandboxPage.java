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
package info.magnolia.module.admincentral.dialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * AdminCentral Page/Section/View (main container content) for testing dialog stuff, temporary stuff.
 */
public class DialogSandboxPage extends VerticalLayout {

    public DialogSandboxPage() {

        setMargin(true);

        //GridLayout grid = createMockDialogGrid();

        final TextField field = new TextField();
        field.setRequired(true);
        field.setRequiredError("please specify paragraph to edit first");
        field.setMaxLength(500);
        field.setInputPrompt("paragraph handle");
        field.setRows(1);
        addComponent(field);



//        options = DialogRegistry.getInstance().getDialog(dialogName);
//
//        for (Map.Entry<String, String> entry : options.entrySet()) {
//            comboBox.addItem(entry.getKey());
//            comboBox.setItemCaption(entry.getKey(), entry.getValue());
//        }
//        addComponent(grid);

        addComponent(new Button("New/Edit paragraph", new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                try {
                    String paragraphHandle = (String) field.getValue();
                    if (StringUtils.isBlank(paragraphHandle)) {
                        return;
                    }
                    Content paragraph = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE).getContent(paragraphHandle);
                    String paragraphTemplate = paragraph.getMetaData().getTemplate();
                    Paragraph paragraphDef = ParagraphManager.getInstance().getParagraphDefinition(paragraphTemplate);
                    String pageHandle = getPageHandle(paragraph);
                    String nodeCollection = StringUtils.substringBeforeLast(StringUtils.substringAfter(paragraphHandle, pageHandle + "/"), "/" + paragraph.getName());
                    if ("/".equals(nodeCollection)) {
                        // no collection at all
                        nodeCollection = null;
                    }

                    getApplication().getMainWindow().addWindow(new EditParagraphWindow(paragraphDef.getDialog(),
                            ContentRepository.WEBSITE, pageHandle, nodeCollection, paragraph.getName()));
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }

        }));

        /*
        addComponent(new Button("Select paragraph", new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                getApplication().getMainWindow().addWindow(new SelectParagraphWindow((String) paragraphs.getValue(),
                        (String) repository.getValue(), (String) path.getValue(), (String) nodeCollectionName.getValue(), (String) nodeName.getValue()));
            }
        }));
        */
    }

    private String getPageHandle(Content content) throws RepositoryException {
        while (content != null) {
            if (content.getItemType().equals(ItemType.CONTENT)) {
                return content.getHandle();
            }
            content = content.getParent();
        }
        return null;
    }

    private GridLayout createMockDialogGrid() {
        final TextField dialog = createLongTextField("mock");
        final TextField repository = createLongTextField(ContentRepository.CONFIG);
        final TextField path = createLongTextField("/modules/genuine-vaadin-central");
        final TextField nodeCollectionName = createLongTextField("");
        final TextField nodeName = createLongTextField("foobar");
        final TextField paragraphs = createLongTextField("samplesHowToJSP,samplesHowToFTL");

        GridLayout grid = new GridLayout(2, 1);
        grid.addComponent(new Label("Dialog"));
        grid.addComponent(dialog);
        grid.newLine();
        grid.addComponent(new Label("Repository"));
        grid.addComponent(repository);
        grid.newLine();
        grid.addComponent(new Label("Path"));
        grid.addComponent(path);
        grid.newLine();
        grid.addComponent(new Label("NodeCollection"));
        grid.addComponent(nodeCollectionName);
        grid.newLine();
        grid.addComponent(new Label("Node"));
        grid.addComponent(nodeName);
        grid.newLine();
        grid.addComponent(new Label("Paragraphs"));
        grid.addComponent(paragraphs);
        return grid;
    }

    private TextField createLongTextField(String value) {
        TextField field = new TextField();
        field.setWidth("300px");
        field.setValue(value);
        return field;
    }
}
