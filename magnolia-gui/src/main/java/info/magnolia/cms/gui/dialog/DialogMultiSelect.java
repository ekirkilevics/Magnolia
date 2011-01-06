/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ControlImpl;
import info.magnolia.freemarker.FreemarkerUtil;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Control to select multiple values. The values can get stored as list, in JSON format or as a multiple values
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DialogMultiSelect extends DialogBox {

    /**
     * The values are saved a properties
     */
    public static final String SAVE_MODE_MULTIPLE = "multiple";

    /**
     * The values are saved as a single json string
     */
    public static final String SAVE_MODE_JSON = "json";

    /**
     * The values are saved as semicolon separated list
     */
    public static final String SAVE_MODE_LIST = "list";

    /**
     * Set the save mode
     */
    public static final String CONFIG_SAVE_MODE = "saveMode";

    /**
     * Set the onclick js code of the coose button. If non is set the button is not rendered.
     */
    private static final String CONFIG_CHOOSE_ONCLICK = "chooseOnclick";
    
    /**
     * If you like to select the data from a tree, just define the config value tree instead of chooseOnclick
     */
    private static final String CONFIG_TREE = "tree";    

    /**
     * Render the Html using a template
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPre(out);
        // this could be replaced by
        //   out.write(FreemarkerUtil.process(this));
        // except this could cause problems with subclasses
        out.write(FreemarkerUtil.process(DialogMultiSelect.class, this));
        this.drawHtmlPost(out);
    }

    /**
     * The button to add a new row
     */
    public String getAddButton() {
        Button add = new Button();
        add.setLabel(getMessage("buttons.add")); //$NON-NLS-1$ 
        add.setOnclick(this.getName() + "DynamicTable.addNew();"); //$NON-NLS-1$ 
        add.setSmall(true);
        return add.getHtml();
    }

    /**
     * If this control has a choose button
     */
    public String getChooseButton() {
        
        String chooseOnclick = this.getConfigValue(CONFIG_CHOOSE_ONCLICK);
        if(StringUtils.isEmpty(chooseOnclick)){
            String tree = this.getConfigValue(CONFIG_TREE);
            if(StringUtils.isNotEmpty(tree)){
                chooseOnclick = "mgnlOpenTreeBrowserWithControl($('${prefix}'), '" + tree + "');";

            }
        }

        if (StringUtils.isNotEmpty(chooseOnclick)) {
            Button choose = new Button();
            choose.setLabel(this.getMessage("buttons.choose")); //$NON-NLS-1$
            choose.setOnclick(chooseOnclick);

            choose.setSmall(true);
            return choose.getHtml();
        }
        return "";
    }

    /**
     * Button for deleting a row
     */
    public String getDeleteButton() {
        Button delete = new Button();
        delete.setLabel(this.getMessage("buttons.delete")); //$NON-NLS-1$
        delete
            .setOnclick(this.getName() + "DynamicTable.del('${index}');" + this.getName() + "DynamicTable.persist();"); //$NON-NLS-1$
        delete.setSmall(true);
        return delete.getHtml();
    }

    /**
     * Called by the template. It renders the dynamic inner row using trimpaths templating mechanism.
     */
    public String getInnerHtml() {
        // TODO - this could potentially be replaced by
        //   return FreemarkerUtil.process(this, "Inner", "html");
        // except this might cause problems with subclasses
        String name = "/" + StringUtils.replace(DialogMultiSelect.class.getName(), ".", "/") + "Inner.html";
        Map map = new HashMap();
        map.put("this", this);
        return FreemarkerUtil.process(name, map);
    }

    /**
     * JS function used to create an object out of the input fields
     */
    public String getGetObjectFunction() {
        return "function(prefix, index){return {value: $(prefix).value }}";
    }

    /**
     * JS function used to create a new empty object
     */
    public String getNewObjectFunction() {
        return "function(){return {value: ''}}";
    }

    /**
     * Create the object to initialize the table
     */
    public String getJSON() {
        if (this.isSaveAsJSON()) {
            return this.getValue();
        }

        List values;
        if (this.isSaveAsList()) {
            values = Arrays.asList(this.getValue().split(","));
        }
        else {
            values = this.getValues();
        }

        if (values.size() == 0) {
            return "[{value:''}]";
        }

        List objects = new ArrayList();
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            String value = (String) iter.next();
            objects.add("{value: '" + value + "'}");
        }
        return "[" + StringUtils.join(objects.iterator(), ",") + "]";
    }

    public String getSaveInfo() {
        Boolean renderSaveInfo = BooleanUtils.toBooleanObject(this.getConfigValue("saveInfo"));
        if (BooleanUtils.toBooleanDefaultIfNull(renderSaveInfo, true)) {
            ControlImpl dummy = new ControlImpl(this.getName(), (String) null);
            if (!isSaveAsList() && !isSaveAsJSON()) {
                dummy.setValueType(ControlImpl.VALUETYPE_MULTIPLE);
            }
            return dummy.getHtmlSaveInfo();
        }
        // don' create the save info
        return "";
    }

    public boolean isSaveAsList() {
        return StringUtils.equals(this.getConfigValue(CONFIG_SAVE_MODE), SAVE_MODE_LIST);
    }

    public boolean isSaveAsJSON() {
        return StringUtils.equals(this.getConfigValue(CONFIG_SAVE_MODE), SAVE_MODE_JSON);
    }

    /**
     * If the values are saved using the valueType multiple, we can not use the same name for the hidden field we use
     * for persisting the data.
     * @return the name of the hidden field
     */
    public String getHiddenFieldName() {
        if (this.isSaveAsList()) {
            return this.getName();
        }

        return this.getName() + "Persisted";
    }

}
