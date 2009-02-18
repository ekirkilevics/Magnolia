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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.misc.CssConstants;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * A simple input text with additional configurable buttons. For most uses you can probably stay with the standard date
 * and link dialogs provided with Magnolia, but if you need additional or custom buttons you can use this control.
 * </p>
 * <p>
 * Sample configuration:
 * </p>
 *
 * <pre>
 * + dialog
 *   + tabText
 *     + sampleEdit
 *       + buttons
 *         + button1
 *           * label      "Say hello"
 *           * onclick    "alert('hello!')"
 *         + button2
 *           * label      "Browse"
 *           * onclick    "mgnlDialogLinkOpenBrowser('link','website','html', false)"
 *         + button3
 *           * label      "Clear"
 *           * onclick    "getElementById('link').value=''"
 *       * type           "String"
 *       * label          "sample link"
 *       * rows           "1"
 *       * name           "link"
 *       * controlType    "editWithButtons"
 * </pre>
 *
 * @author Fabrizio Giustina
 * @since 2.2
 */
public class DialogEditWithCustomButtons extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogEditWithCustomButtons.class);

    /**
     * List of Buttons loaded from configuration.
     */
    private List buttons = new ArrayList();

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);

        try {
            Iterator it = configNode.getContent("buttons").getChildren(ItemType.CONTENTNODE.getSystemName()).iterator(); //$NON-NLS-1$
            while (it.hasNext()) {
                Content n = (Content) it.next();

                Button button = new Button();
                button.setOnclick(n.getNodeData("onclick").getString()); //$NON-NLS-1$

                String label = null;
                if (n.getNodeData("label").isExist()) { //$NON-NLS-1$
                    label = n.getNodeData("label").getString(); //$NON-NLS-1$
                    label = this.getMessage(label);
                }
                button.setLabel(label);

                buttons.add(button);
            }
        }
        catch (RepositoryException e) {
            log.error("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }

    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        Edit control = new Edit(this.getName(), this.getValue());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING)); //$NON-NLS-1$
        if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
            control.setSaveInfo(false);
        }
        control.setCssClass(CssConstants.CSSCLASS_EDIT);
        control.setRows(this.getConfigValue("rows", "1")); //$NON-NLS-1$ //$NON-NLS-2$
        control.setCssStyles("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
        if (this.getConfigValue("onchange", null) != null) { //$NON-NLS-1$
            control.setEvent("onchange", this.getConfigValue("onchange")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.drawHtmlPre(out);
        String width = this.getConfigValue("width", "95%"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"" + width + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("<tr><td width=\"100%\"  class=\"" + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(control.getHtml());

        for (Iterator iter = this.buttons.iterator(); iter.hasNext();) {
            Button button = (Button) iter.next();
            out.write("</td><td></td><td class=\"" + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            out.write(button.getHtml());
        }

        out.write("</td></tr></table>"); //$NON-NLS-1$

        this.drawHtmlPost(out);
    }
}
