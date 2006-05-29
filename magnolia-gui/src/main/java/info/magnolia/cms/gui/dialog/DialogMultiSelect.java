/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.util.FreeMarkerUtil;


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
     * Render the Html using a template
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPre(out);
        out.write(FreeMarkerUtil.process(DialogMultiSelect.class, this));
        this.drawHtmlPost(out);
    }

    /**
     * The button to add a new row
     */
    public String getAddButton() {
        Button add = new Button();
        add.setLabel(getMessage("buttons.add")); //$NON-NLS-1$ //$NON-NLS-2$
        add.setOnclick(this.getName() + "DynamicTable.addNew();"); //$NON-NLS-1$ //$NON-NLS-2$
        add.setSmall(true);
        return add.getHtml();
    }

    /**
     * If this control has a choose button
     */
    public String getChooseButton() {
        String chooseOnclick = this.getConfigValue(CONFIG_CHOOSE_ONCLICK);
        if (StringUtils.isNotEmpty(chooseOnclick)) {
            Button choose = new Button();
            choose.setLabel(this.getMessage("buttons.choose")); //$NON-NLS-1$
            choose.setOnclick(chooseOnclick); //$NON-NLS-1$ //$NON-NLS-2$

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
        String name = "/" + StringUtils.replace(DialogMultiSelect.class.getName(), ".", "/") + "Inner.html";
        Map map = new HashMap();
        map.put("this", this);
        return FreeMarkerUtil.process(name, map);
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
        else {
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
    }

    public String getSaveInfo() {
        Boolean renderSaveInfo =  BooleanUtils.toBooleanObject(this.getConfigValue("saveInfo"));
        if(BooleanUtils.toBooleanDefaultIfNull(renderSaveInfo, true)){
            ControlSuper dummy = new ControlSuper(this.getName(), (String)null);
            if (!isSaveAsList() && !isSaveAsJSON()) {
                dummy.setValueType(ControlSuper.VALUETYPE_MULTIPLE);
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
        else {
            return this.getName() + "Persisted";
        }
    }

}
