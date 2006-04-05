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



/**
 * DConfigers the search based on the dialog.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class DialogBasedSearchConfig extends SearchConfigImpl {

    /**
     * The name of the dialog used to configure
     */
    private String dialogName;
    
    /**
     * @param dialogName
     */
    public DialogBasedSearchConfig(String dialogName) {
        this.dialogName = dialogName;
    }

    /**
     * gets all the controls defined in the dialog
     * @throws Exception
     */
    protected void initControls() throws Exception {
        /*
        HierarchyManager configHm = SessionAccessControl.getHierarchyManager(request, dialogRepository);

        // ordered by the order number and the alphabeticaly
        Set ordered = new TreeSet( new Comparator(){
            public int compare(Object c1, Object c2) {
                // if the same ordernumber (or none)
                if(((SearchControl)c1).getOrderNumber() == ((SearchControl)c2).getOrderNumber()){
                    return ((SearchControl)c1).getLabel().compareTo((((SearchControl)c2).getLabel()));
                }
                else{
                    return ((SearchControl)c1).getOrderNumber() - ((SearchControl)c2).getOrderNumber();
                }
            }
        });
        
        Content dialog = configHm.getContent(dialogPath);

        // for all tabs
        Collection tabNodes = dialog.getChildren(ItemType.CONTENTNODE.getSystemName(), Content.SORT_BY_SEQUENCE);

        for (Iterator iter = tabNodes.iterator(); iter.hasNext();) {
            Content tabNode = (Content) iter.next();

            Collection controlNodes = tabNode.getChildren(
                ItemType.CONTENTNODE.getSystemName(),
                Content.SORT_BY_SEQUENCE);

            for (Iterator iter2 = controlNodes.iterator(); iter2.hasNext();) {
                Content controlNode = (Content) iter2.next();

                if (controlNode.hasNodeData("searchType")) {
                    SearchControl field = createSearchControl(controlNode);

                    if (field != null) {
                        ordered.add(field);
                    }
                }
            }
        }

        for (Iterator iter = ordered.iterator(); iter.hasNext();) {
            SearchControl control = (SearchControl) iter.next();
            controls.put(control.getName(), control);
        }
        */
        
    }

    protected SearchControl createSearchControl(Content controlNode) throws Exception {
        /*
        SearchControl field = null;
        String controlType = controlNode.getNodeData("searchType").getString();
        if (controlType.equals("select")) {
            field = new SelectSearchControl(controlNode, request);
        }
        else if (controlType.equals("date")) {
            field = new DateSearchControl(controlNode, request);
        }
        else if (controlType.equals("edit")) {
            field = new SearchControl(controlNode, request);
        }
        else if (controlType.equals("indexed")) {
            field = new IndexedSearchControl(controlNode, request);
        }
        return field;
        
        */
        return null;
    }
    
    public void configureControl(SearchControlDefinition def){
        /*
        this.type = "select";
        Collection optionNodes = node.getContent("options").getChildren();

        for(Iterator iter = optionNodes.iterator(); iter.hasNext();){
            Content optionNode = (Content) iter.next();
            String value = optionNode.getNodeData("value").getString();
            String label = optionNode.getNodeData("value").getString();
            options.put(value, label);
        }
        */
    }

    
    /**
     * @return Returns the dialogName.
     */
    public String getDialogName() {
        return this.dialogName;
    }

    
    /**
     * @param dialogName The dialogName to set.
     */
    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }
}
