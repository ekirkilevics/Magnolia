package info.magnolia.context;

import java.util.HashMap;
import java.util.Map;

public class MapAttributeStrategy implements AttributeStrategy {

	private Map map = new HashMap();
	
	public MapAttributeStrategy() {	
	}
	
	/**
     * Use the Map.put()
     */
    public void setAttribute(String name, Object value, int scope) {
        this.map.put(name, value);
    }

    /**
     * Use the Map.get()
     */
    public Object getAttribute(String name, int scope) {
        return this.map.get(name);
    }

    /**
     * use the Map.remove()
     */
    public void removeAttribute(String name, int scope) {
        this.map.remove(name);
    }

    /**
     * Ignore scope and return the inner map
     */
    public Map getAttributes(int scope) {
        return this.getAttributes();
    }

    /**
     * Returns the inner map
     */
    public Map getAttributes() {
        return this.map;
    }

    
    public Map getMap() {
        return map;
    }

    
    public void setMap(Map map) {
        this.map = map;
    }

}
