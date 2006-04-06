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

import info.magnolia.cms.gui.controlx.impl.TemplatedRenderer;

/**
 * Renders a list view.
 * 
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class ListControlRenderer extends TemplatedRenderer {
    
    /**
     * Default template used.
     */
    public ListControlRenderer() {
        super();
    }

    /**
     * Pass the template to use.
     * @param templateName
     */
    public ListControlRenderer(String templateName) {
        super(templateName);
    }

    /**
     * Return asc or desc. 
     * @param list
     * @param field
     * @return
     */
    public String nextSortByOrder(ListControl list, String field){
        if(list.getSortBy().equals(field)){
            if(list.getSortByOrder().equals("asc")){
                return "desc";
            }
        }
        return "asc";
    }

    /**
     * Return asc or desc.
     * @param list
     * @param field
     * @return
     */
    public String nextGroupByOrder(ListControl list, String field){
        if(list.getGroupBy().equals(field)){
            if(list.getGroupByOrder().equals("asc")){
                return "desc";
            }
        }
        return "asc";
    }

    /**
     * Render the click event
     * @param iter
     * @return
     */
    public String onClick(ListModelIterator iter){
        return "";
    }
    
    /**
     * Render the double click event
     * @param iter
     * @return
     */
    public String onDbClick(ListModelIterator iter){
        return "";
    }
    
}
