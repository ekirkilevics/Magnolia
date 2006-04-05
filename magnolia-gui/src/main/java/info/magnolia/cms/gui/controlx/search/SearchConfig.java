package info.magnolia.cms.gui.controlx.search;

import org.apache.commons.collections.OrderedMap;

/**
 * Defines the advanced search. Fields, ...
 * @author philipp
 *
 */
public interface SearchConfig {

    /**
     * @return Returns the controls.
     */
    public OrderedMap getControlDefinitions();

    public void addControlDefinition(SearchControlDefinition def);
    
}