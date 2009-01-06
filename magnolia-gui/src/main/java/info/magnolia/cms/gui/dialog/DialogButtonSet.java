/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ButtonSet;
import info.magnolia.cms.gui.control.ControlImpl;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogButtonSet extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogButtonSet.class);

    private int buttonType = ControlImpl.BUTTONTYPE_RADIO;

    public void setOptions(Content configNode, boolean setDefaultSelected) {
        // setDefaultSelected: does not work properly (no difference between never stored and removed...)
        // therefore do only use for radio, not for checkbox
        List options = new ArrayList();
        try {
            Iterator it = getOptionNodes(configNode).iterator();
            while (it.hasNext()) {
                Content n = ((Content) it.next());
                String valueNodeData = this.getConfigValue("valueNodeData", "value");
                String labelNodeData = this.getConfigValue("labelNodeData", "label");

                String value = NodeDataUtil.getString(n, valueNodeData);//$NON-NLS-1$
                String label = NodeDataUtil.getString(n, labelNodeData);//$NON-NLS-1$

                //label = this.getMessage(label);
                Button button = new Button(this.getName(), value);
                // if (n.getNodeData("label").isExist()) button.setLabel(n.getNodeData("label").getString());
                button.setLabel(label);

                String iconSrc = n.getNodeData("iconSrc").getString(); //$NON-NLS-1$
                if (StringUtils.isNotEmpty(iconSrc)) {
                    button.setIconSrc(iconSrc);
                }

                if (setDefaultSelected && n.getNodeData("selected").getBoolean()) { //$NON-NLS-1$
                    button.setState(ControlImpl.BUTTONSTATE_PUSHED);
                }
                options.add(button);
            }
        }
        catch (RepositoryException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        this.setOptions(options);
    }

    protected Collection getOptionNodes(Content configNode) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content optionsNode = null;

        if(configNode.hasContent("options")){
            optionsNode = configNode.getContent("options"); //$NON-NLS-1$
        }
        else{
            String repository = this.getConfigValue("repository", ContentRepository.WEBSITE);
            String path = this.getConfigValue("path");
            if(StringUtils.isNotEmpty(path)){
                optionsNode = ContentUtil.getContent(repository, path);
            }
        }

        if(optionsNode != null){
            return ContentUtil.getAllChildren(optionsNode);
        }
        return new ArrayList();
    }

    public void setOption(Content configNode) {
        // checkboxSwitch -> only one option, value always true/false
        List options = new ArrayList();
        Button button = new Button(this.getName() + "_dummy", StringUtils.EMPTY); //$NON-NLS-1$
        String label = configNode.getNodeData("buttonLabel").getString(); //$NON-NLS-1$
        //label = this.getMessage(label);
        button.setLabel(label);

        if (configNode.getNodeData("selected").getBoolean()) { //$NON-NLS-1$
            button.setState(ControlImpl.BUTTONSTATE_PUSHED);
        }

        button.setValue("true"); //$NON-NLS-1$
        button.setOnclick("mgnlDialogShiftCheckboxSwitch('" + this.getName() + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        options.add(button);
        this.setOptions(options);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);

        // confignode can be null if instantiated directly
        if (configNode != null) {
            String controlType = this.getConfigValue("selectType",this.getConfigValue("controlType")); //$NON-NLS-1$

            if (log.isDebugEnabled()) {
                log.debug("Init DialogButtonSet with type=" + controlType); //$NON-NLS-1$
            }

            // custom settings
            if (controlType.equals("radio")) { //$NON-NLS-1$
                setButtonType(ControlImpl.BUTTONTYPE_RADIO);
                setOptions(configNode, true);
            }
            else if (controlType.equals("checkbox")) { //$NON-NLS-1$
                setButtonType(ControlImpl.BUTTONTYPE_CHECKBOX);
                setOptions(configNode, false);
                setConfig("valueType", "multiple"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else if (controlType.equals("checkboxSwitch")) { //$NON-NLS-1$
                setButtonType(ControlImpl.BUTTONTYPE_CHECKBOX);
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
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPre(out);

        // translate
        for (int i = 0; i < this.getOptions().size(); i++) {
            Button b = (Button) this.getOptions().get(i);
            b.setLabel(this.getMessage(b.getLabel()));
        }

        ButtonSet control;
        if (this.getConfigValue("valueType").equals("multiple")) { //$NON-NLS-1$ //$NON-NLS-2$
            // checkbox
            control = new ButtonSet(this.getName(), this.getValues());
            control.setValueType(ControlImpl.VALUETYPE_MULTIPLE);
        }
        else if (this.getButtonType() == ControlImpl.BUTTONTYPE_CHECKBOX) {
            // checkboxSwitch
            control = new ButtonSet(this.getName() + "_SWITCH", this.getValue()); //$NON-NLS-1$
        }
        else {
            // radio
            control = new ButtonSet(this.getName(), this.getValue());
        }
        control.setButtonType(this.getButtonType());

        // maem: extension to allow for fine grained layout control. E.g. radio buttons with picture
        control.setCssClass(this.getConfigValue("cssClass", CssConstants.CSSCLASS_BUTTONSETBUTTON)); //$NON-NLS-1$

        if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
            control.setSaveInfo(false);
        }
        final String type = this.getConfigValue("type", PropertyType.TYPENAME_STRING);
        control.setType(type); //$NON-NLS-1$
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
                }
                else {
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
        if (control.getButtonType() == ControlImpl.BUTTONTYPE_CHECKBOX
            && control.getValueType() != ControlImpl.VALUETYPE_MULTIPLE) {
            // checkboxSwitch: value is stored in a hidden field (allows default selecting)
            String value = this.getValue();
            if (StringUtils.isEmpty(value)) {
                if (this.getConfigValue("selected").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$
                    value = "true"; //$NON-NLS-1$
                }
                else {
                    value = "false"; //$NON-NLS-1$
                }
            }
            Hidden hiddenValueField = new Hidden(this.getName(), value);
            hiddenValueField.setType(type);

            out.write(hiddenValueField.getHtml());
        }
        this.drawHtmlPost(out);
    }
}
