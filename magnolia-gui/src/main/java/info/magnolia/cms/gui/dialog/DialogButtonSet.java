/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ButtonSet;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.misc.CssConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogButtonSet extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogButtonSet.class);

    private int buttonType = ControlSuper.BUTTONTYPE_RADIO;

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogButtonSet() {
    }

    public void setOptions(Content configNode, boolean setDefaultSelected) {
        // setDefaultSelected: does not work properly (no difference between never stored and removed...)
        // therefor do only use for radio, not for checkbox
        List options = new ArrayList();
        try {
            Iterator it = configNode.getContent("options") //$NON-NLS-1$
                    .getChildren(ItemType.CONTENTNODE.getSystemName())
                    .iterator();
            while (it.hasNext()) {
                Content n = ((Content) it.next());
                String value = n.getNodeData("value").getString(); //$NON-NLS-1$
                String label = n.getNodeData("label").getString(); //$NON-NLS-1$
                label = this.getMessage(label);
                Button button = new Button(this.getName(), value);
                // if (n.getNodeData("label").isExist()) button.setLabel(n.getNodeData("label").getString());
                button.setLabel(label);

                String iconSrc = n.getNodeData("iconSrc").getString(); //$NON-NLS-1$
                if (StringUtils.isNotEmpty(iconSrc)) {
                    button.setIconSrc(iconSrc);
                }

                if (setDefaultSelected && n.getNodeData("selected").getBoolean()) { //$NON-NLS-1$
                    button.setState(ControlSuper.BUTTONSTATE_PUSHED);
                }
                options.add(button);
            }
        }
        catch (RepositoryException e) {
            if (log.isDebugEnabled())
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        this.setOptions(options);
    }

    public void setOption(Content configNode) {
        // checkboxSwitch -> only one option, value always true/false
        List options = new ArrayList();
        Button button = new Button(this.getName() + "_dummy", StringUtils.EMPTY); //$NON-NLS-1$
        String label = configNode.getNodeData("buttonLabel").getString(); //$NON-NLS-1$
        label = this.getMessage(label);
        button.setLabel(label);

        if (configNode.getNodeData("selected").getBoolean()) { //$NON-NLS-1$
            button.setState(ControlSuper.BUTTONSTATE_PUSHED);
        }

        button.setValue("true"); //$NON-NLS-1$
        button.setOnclick("mgnlDialogShiftCheckboxSwitch('" + this.getName() + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        options.add(button);
        this.setOptions(options);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
            throws RepositoryException {
        super.init(request, response, websiteNode, configNode);

        // confignode can be null if instantiated directly
        if (configNode != null) {
            String controlType = configNode.getNodeData("controlType").getString(); //$NON-NLS-1$

            if (log.isDebugEnabled()) {
                log.debug("Init DialogButtonSet with type=" + controlType); //$NON-NLS-1$
            }

            // custom settings
            if (controlType.equals("radio")) { //$NON-NLS-1$
                setButtonType(ControlSuper.BUTTONTYPE_RADIO);
                setOptions(configNode, true);
            } else if (controlType.equals("checkbox")) { //$NON-NLS-1$
                setButtonType(ControlSuper.BUTTONTYPE_CHECKBOX);
                setOptions(configNode, false);
                setConfig("valueType", "multiple"); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (controlType.equals("checkboxSwitch")) { //$NON-NLS-1$
                setButtonType(ControlSuper.BUTTONTYPE_CHECKBOX);
                setOption(configNode);
            }
        }
    }

    public void drawHtmlPreSubs(Writer out) throws IOException {
        this.drawHtmlPre(out);
    }

    public void drawHtmlPostSubs(Writer out) throws IOException {
        this.drawHtmlPost(out);
    }

    public void setButtonType(int i) {
        this.buttonType = i;
    }

    public int getButtonType() {
        return this.buttonType;
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPre(out);
        ButtonSet control;
        if (this.getConfigValue("valueType").equals("multiple")) { //$NON-NLS-1$ //$NON-NLS-2$
            // checkbox
            control = new ButtonSet(this.getName(), this.getValues());
            control.setValueType(ControlSuper.VALUETYPE_MULTIPLE);
        } else if (this.getButtonType() == ControlSuper.BUTTONTYPE_CHECKBOX) {
            // checkboxSwitch
            control = new ButtonSet(this.getName() + "_SWITCH", this.getValue()); //$NON-NLS-1$
        } else {
            // radio
            control = new ButtonSet(this.getName(), this.getValue());
        }
        control.setButtonType(this.getButtonType());

        // maem: extension to allow for fine grained layout control. E.g. radio buttons with picture
        control.setCssClass(this.getConfigValue("cssClass", CssConstants.CSSCLASS_BUTTONSETBUTTON)); //$NON-NLS-1$

        if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
            control.setSaveInfo(false);
        }
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING)); //$NON-NLS-1$
        String width = this.getConfigValue("width", null); //$NON-NLS-1$
        control.setButtonHtmlPre("<tr><td class=\"" + CssConstants.CSSCLASS_BUTTONSETBUTTON + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        control.setButtonHtmlInter("</td><td class=\"" + CssConstants.CSSCLASS_BUTTONSETLABEL + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        control.setButtonHtmlPost("</td></tr>"); //$NON-NLS-1$
        int cols = Integer.valueOf(this.getConfigValue("cols", "1")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$
        if (cols > 1) {
            width = "100%"; // outer table squeezes inner table if outer's width is not defined... //$NON-NLS-1$
            control.setHtmlPre(control.getHtmlPre() + "<tr>"); //$NON-NLS-1$
            control.setHtmlPost("</tr>" + control.getHtmlPost()); //$NON-NLS-1$
            int item = 1;
            int itemsPerCol = (int) Math.ceil(this.getOptions().size() / ((double) cols));
            for (int i = 0; i < this.getOptions().size(); i++) {
                Button b = (Button) this.getOptions().get(i);
                if (item == 1) {
                    b.setHtmlPre("<td><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" //$NON-NLS-1$
                            + control.getButtonHtmlPre());
                }
                if (item == itemsPerCol) {
                    b.setHtmlPost(control.getButtonHtmlPost() + "</table></td><td class=\"" //$NON-NLS-1$
                            + CssConstants.CSSCLASS_BUTTONSETINTERCOL
                            + "\"></td>"); //$NON-NLS-1$
                    item = 1;
                } else {
                    item++;
                }
            }
            // very last button: close table
            int lastIndex = this.getOptions().size() - 1;
            // avoid ArrayIndexOutOfBoundsException, but should not happen
            if (lastIndex > -1) {
                ((Button) this.getOptions().get(lastIndex)).setHtmlPost(control.getButtonHtmlPost() + "</table>"); //$NON-NLS-1$
            }
        }
        if (width != null) {
            control.setHtmlPre("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"" + width + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        control.setButtons(this.getOptions());
        out.write(control.getHtml());
        if (control.getButtonType() == ControlSuper.BUTTONTYPE_CHECKBOX
                && control.getValueType() != ControlSuper.VALUETYPE_MULTIPLE) {
            // checkboxSwitch: value is stored in a hidden field (allows default selecting)
            String value = this.getValue();
            if (StringUtils.isEmpty(value)) {
                if (this.getConfigValue("selected").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$
                    value = "true"; //$NON-NLS-1$
                } else {
                    value = "false"; //$NON-NLS-1$
                }
            }
            out.write(new Hidden(this.getName(), value).getHtml());
        }
        this.drawHtmlPost(out);
    }
}
