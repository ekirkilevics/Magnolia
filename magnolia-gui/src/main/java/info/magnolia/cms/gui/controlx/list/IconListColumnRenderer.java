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
package info.magnolia.cms.gui.controlx.list;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.gui.controlx.Control;
import info.magnolia.context.MgnlContext;


/**
 * Render a an icon. The value returned by the column must be the src value. Context path is added.
 * @author philipp
 *
 */
public class IconListColumnRenderer extends ListColumnRenderer {
    
    public String render(Control control) {
        ListColumn column = (ListColumn) control;
        String src = (String) column.getValue();

        if(StringUtils.isNotEmpty(src)){
            return "<img src=\""
            + MgnlContext.getContextPath()
            + src
            + "\"/>"; 
        }
        
        return "" ;
    }

}
