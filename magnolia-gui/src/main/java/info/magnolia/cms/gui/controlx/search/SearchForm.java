/**
 *  Copyright 2005 obinary ag.  All rights reserved.
 *  See license distributed with this file and available
 *  online at http://www.magnolia.info/dms-license.html
 */

package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.gui.controlx.impl.AbstractControl;

/**
 * @author philipp Style - Code Templates
 */
public class SearchForm extends AbstractControl {

    /**
     * 
     */
    public static final String RENDER_TYPE = "searchForm";


    private SearchConfig config;
    
   
    /**
     * String representation of the controls
     */
    private String json;

    private String searchStr = "";

    private boolean searchAdvanced = false;

    public SearchForm(){
        this.setRenderType(RENDER_TYPE);
    }

    protected void initInstances(){
        /*
        this.json = RequestFormUtil.getURLParameterDecoded(request, "advancedSearchParameters", "UTF-8");

        if (StringUtils.isEmpty(this.json)) {
            SearchControl first = (SearchControl) controls.get(controls.firstKey());
            this.json = "[{field:'" + first.getName() + "', constraint:'contains', type:'" + first.getType() + "', value:''}]";
        }


        JSONArray objects = new JSONArray(this.json);

        for (int i = 0; i < objects.length(); i++) {
            JSONObject object = objects.getJSONObject(i);
            String field = object.getString("field");
            try {
                AdvancedSearchParameter parameter = ((SearchControl) controls.get(field)).getSearchParameter(i, object);
                parameters.add(parameter);
            }
            catch (SearchParameterException e) {
                AlertUtil.setMessage(e.getMessage());
            }
        }
        */
    }

    /**
     * @return the fields for the javascript ADSerach class
     */
    public String getJsFields() {
        /*
        Collection definitions = this.config.getControlDefinitions();
        String str = "";
        str += "{";

        for (Iterator iter = controls.iterator(); iter.hasNext();) {
            SearchControl control = (SearchControl) iter.next();
            str += control.getJsField() + ",\n";
        }
        // remove last ,
        str = StringUtils.removeEnd(str, ",\n");

        str += "}";
        return str;
        */
        return "";
    }

    
    public String getJson() {
        return json;
    }

    
    /**
     * @return Returns the config.
     */
    public SearchConfig getConfig() {
        return this.config;
    }

    
    /**
     * @param config The config to set.
     */
    public void setConfig(SearchConfig config) {
        this.config = config;
    }

    /**
     * @return Returns the searchAdvanced.
     */
    public boolean isSearchAdvanced() {
        return searchAdvanced;
    }

    /**
     * @param searchAdvanced The searchAdvanced to set.
     */
    public void setSearchAdvanced(boolean searchAdvanced) {
        this.searchAdvanced = searchAdvanced;
    }

    /**
     * @return Returns the searchStr.
     */
    public String getSearchStr() {
        return searchStr;
    }

    /**
     * @param searchStr The searchStr to set.
     */
    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

}
