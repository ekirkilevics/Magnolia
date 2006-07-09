package info.magnolia.cms.gui.controlx.search;

import java.util.Collection;


/**
 * Defines the advanced search. Fields, ...
 * @author philipp
 */
public interface SearchConfig {

    /**
     * @return Returns the controls.
     */
    public Collection getControlDefinitions();

    public void addControlDefinition(SearchControlDefinition def);

    public SearchControlDefinition getControlDefinition(String field);

}