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
package info.magnolia.ui.admincentral.dialog.registry;

import info.magnolia.ui.admincentral.dialog.definition.DialogDefinition;

import javax.jcr.RepositoryException;

/**
 * Mock DialogProvider that instantiates a dialog useful while prototyping.
 */
public class MockDialogProvider implements DialogProvider {

    public DialogDefinition getDialogDefinition() throws RepositoryException {

        DialogDefinition dialog = new DialogDefinition();
/*
        DialogTab tab1 = new DialogTab();
        tab1.setLabel("Content");
        tab1.addField(createCheckBox("visible", "Visible", "Toggle to show or hide this paragraph"));
        tab1.addField(createDate("date", "Date", "Date of publication"));
        tab1.addField(createEdit("title", "Title", "Title of the paragraph"));
        tab1.addField(createFile("image", "Image", "An image"));
        tab1.addField(createLink("link", "Target page", "Page to link to"));
        tab1.addField(createRadio("textSize", "Text size", "Size of text",options()));
        tab1.addField(createSelect("city", "City", "", options()));
        tab1.addField(createSlider("fontWeight", "Font Weight", ""));
        tab1.addField(createStatic("This is static text long enough to use the full width"));
        dialog.addTab(tab1);

        DialogTab tab2 = new DialogTab();
        tab2.setLabel("Margins");
        tab2.addField(createEditNumeric("leftMargin", "Left margin", "Margin on the left"));
        tab2.addField(createEditNumeric("rightMargin", "Right margin", "Margin on the right"));
        tab2.addField(createRichText("bodyText", "Body text", "Text in paragraph body"));
        dialog.addTab(tab2);
*/
        return dialog;
    }
/*
    private EditControl createEditNumeric(String name, String label, String description) {
        EditControl control = new EditControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        control.setInputPrompt("Enter a number..");
        control.setValidationPattern("^[0-9]+$");
        control.setValidationMessage("Field '" + label + "' must be numeric");
        return control;
    }

    private StaticControl createStatic(String label) {
        StaticControl staticControl = new StaticControl();
        staticControl.setLabel(label);
        return staticControl;
    }

    private SliderControl createSlider(String name, String label, String description) {
        SliderControl control = new SliderControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        control.setMin(0);
        control.setMax(100);
        control.setResolution(5);
        return control;
    }

    private SelectControl createSelect(String name, String label, String description, Map<String, String> options) {
        SelectControl control = new SelectControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        control.setOptions(options);
        return control;
    }

    private RadioControl createRadio(String name, String label, String description, Map<String, String> options) {
        RadioControl control = new RadioControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        control.setOptions(options);
        return control;
    }

    private LinkControl createLink(String name, String label, String description) {
        LinkControl control = new LinkControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        return control;
    }

    private UuidLinkControl createUuidLink(String name, String label, String description) {
        UuidLinkControl control = new UuidLinkControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        return control;
    }

    private FileControl createFile(String name, String label, String description) {
        FileControl control = new FileControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        return control;
    }

    private CheckBoxControl createCheckBox(String name, String label, String description) {
        CheckBoxControl control = new CheckBoxControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        return control;
    }

    private DateControl createDate(String name, String label, String description) {
        DateControl control = new DateControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        control.setTime(true);
        return control;
    }

    private EditControl createEdit(String name, String label, String description) {
        EditControl control = new EditControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        control.setRequired(true);
        control.setRequiredErrorMessage("Must not be empty");
        control.setMaxLength(15);
        control.setInputPrompt("Type here..");
        return control;
    }

    private RichTextControl createRichText(String name, String label, String description) {
        RichTextControl control = new RichTextControl();
        control.setName(name);
        control.setLabel(label);
        control.setDescription(description);
        return control;
    }

    private Map<String, String> options() {
        Map<String, String> options = new HashMap<String, String>();
        options.put("de001", "Berlin");
        options.put("fr023", "Paris");
        options.put("es015", "Madrid");
        return options;
    }
*/
}