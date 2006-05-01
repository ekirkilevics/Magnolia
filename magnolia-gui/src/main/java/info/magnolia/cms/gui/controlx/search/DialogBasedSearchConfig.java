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
package info.magnolia.cms.gui.controlx.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;



/**
 * Configers the search based on the dialog.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class DialogBasedSearchConfig extends SearchConfigImpl {
    Logger log = LoggerFactory.getLogger(DialogBasedSearchConfig.class);

    /**
     * @param dialogPath
     */
    public DialogBasedSearchConfig(Content dialogNode) {
        try{
            init(dialogNode);
        }
        catch(Exception e){
            log.error("can't configure the search", e);
        }
    }

    /**
     * gets all the controls defined in the dialog
     * @throws Exception
     */
    protected void init(Content dialogNode) throws Exception {
        // ordered definition
        SortedMap sortMap = new TreeMap();
        
        // for all tabs
        Collection tabNodes = dialogNode.getChildren(ItemType.CONTENTNODE);

        for (Iterator iter = tabNodes.iterator(); iter.hasNext();) {
            Content tabNode = (Content) iter.next();

            Collection controlNodes = tabNode.getChildren();

            for (Iterator iter2 = controlNodes.iterator(); iter2.hasNext();) {
                Content controlNode = (Content) iter2.next();

                if (controlNode.hasNodeData("searchable")) {
                    String searchable = NodeDataUtil.getString(controlNode, "searchable");
                   
                    SearchControlDefinition def = createSearchControl(controlNode);
                    
                    sortMap.put(searchable, def);
                }
            }
        }

        // add them now (after ordering)
        for (Iterator iter = sortMap.values().iterator(); iter.hasNext();) {
            SearchControlDefinition def = (SearchControlDefinition) iter.next();
            this.addControlDefinition(def);
        }
    }

    protected SearchControlDefinition createSearchControl(Content controlNode) throws Exception {
        String type = controlNode.getNodeData("searchType").getString();
        String name = controlNode.getNodeData("name").getString();
        String label = controlNode.getNodeData("label").getString();
        
        if (type.equals("select")) {
            SelectSearchControlDefinition select = new SelectSearchControlDefinition(name, label);
            configureSelect(select, controlNode);
            return select;
        }
        else{
            return new SearchControlDefinition(name, label, type);
        }
    }
    
    public void configureSelect(SelectSearchControlDefinition def, Content node) throws RepositoryException{
        Collection optionNodes = node.getContent("options").getChildren();

        for(Iterator iter = optionNodes.iterator(); iter.hasNext();){
            Content optionNode = (Content) iter.next();
            String value = optionNode.getNodeData("value").getString();
            String label = optionNode.getNodeData("value").getString();
            def.addOption(value, label);
        }
    }

}
