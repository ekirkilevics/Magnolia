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
