package info.magnolia.context;

import java.util.Map;

public interface AttributeStrategy {
	void setAttribute(String name, Object value, int scope);
	Object getAttribute(String name, int scope);
	Map getAttributes(int scope);
	void removeAttribute(String name, int scope);
}
