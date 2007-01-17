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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.NodeDataUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Configers the search based on the dialog.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DialogBasedSearchConfig extends SearchConfigImpl {

    Logger log = LoggerFactory.getLogger(DialogBasedSearchConfig.class);

    /**
     * @param dialogPath
     */
    public DialogBasedSearchConfig(Content dialogNode) {
        try {
            init(dialogNode);
        }
        catch (Exception e) {
            log.error("can't configure the search", e);
        }
    }

    /**
     * gets all the controls defined in the dialog
     * @throws Exception
     */
    protected void init(Content dialogNode) throws Exception {
        // ordered definition
        SortedMap sortedByOrder = new TreeMap();
        SortedMap sortedByName = new TreeMap();
        
        // for all tabs
        Collection tabNodes = dialogNode.getChildren(ItemType.CONTENTNODE);

        for (Iterator iter = tabNodes.iterator(); iter.hasNext();) {
            Content tabNode = (Content) iter.next();

            Collection controlNodes = tabNode.getChildren();

            for (Iterator iter2 = controlNodes.iterator(); iter2.hasNext();) {
                Content controlNode = (Content) iter2.next();
                String searchable = NodeDataUtil.getString(controlNode, "searchable", "true");
                if (!searchable.equals("false")) {
                    SearchControlDefinition def = createSearchControl(controlNode);

                    try{
                        Integer order = new Integer(searchable);
                        sortedByOrder.put(order, def);
                    }
                    catch(NumberFormatException e){
                        sortedByName.put(def.getLabel(), def);
                    }
                }
            }
        }

        // add them now (after ordering)
        for (Iterator iter = sortedByOrder.values().iterator(); iter.hasNext();) {
            this.addControlDefinition((SearchControlDefinition) iter.next());
        }
        
        for (Iterator iter = sortedByName.values().iterator(); iter.hasNext();) {
            this.addControlDefinition((SearchControlDefinition) iter.next());
        }
    }

    protected SearchControlDefinition createSearchControl(Content controlNode) throws Exception {
        String contolType = controlNode.getNodeData("controlType").getString();
        String searchType = NodeDataUtil.getString(controlNode, "searchType", contolType);
        
        String name = controlNode.getNodeData("name").getString();
        String label = NodeDataUtil.getI18NString(controlNode, "label");

        return createSearchControl(name, label, searchType, controlNode);
    }

    protected SearchControlDefinition createSearchControl(String name, String label, String searchType, Content controlNode) throws RepositoryException {
        if (searchType.equals("select")) {
            SelectSearchControlDefinition select = new SelectSearchControlDefinition(name, label);
            
            Collection optionNodes = controlNode.getContent("options").getChildren();

            for (Iterator iter = optionNodes.iterator(); iter.hasNext();) {
                Content optionNode = (Content) iter.next();
                String optionValue = optionNode.getNodeData("name").getString();
                String optionLabel = optionNode.getNodeData("value").getString();
                select.addOption(optionValue, optionLabel);
            }
            return select;
        }
        else{
            try {
                Class defClass = ClassUtil.classForName(searchType);
                try {
                    SearchControlDefinition def = (SearchControlDefinition) ConstructorUtils.invokeConstructor(defClass, new Object[]{name, label});
                    if(def instanceof DialogBasedSearchControlDefinition){
                        ((DialogBasedSearchControlDefinition)def).init(controlNode); 
                    }
                    return def;
                }
                catch (Exception e) {
                    log.error("can't instantiate search control definition " + defClass , e);
                }
            }
            catch (ClassNotFoundException e) {
                // this happens if the search Type is not a class
            }
        }
        // default to the normal search field
        return new SearchControlDefinition(name, label, searchType);
    }
}
