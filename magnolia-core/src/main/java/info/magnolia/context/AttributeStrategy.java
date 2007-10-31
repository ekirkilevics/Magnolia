package info.magnolia.context;

public interface AttributeStrategy {
	void setAttribute(String name, Object value, int scope);
	Object getAttribute(String name, int scope);
}
