package info.magnolia.maven.setproperty;

/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public abstract class ValueProviderImpl implements ValueProvider {

    private String name;

    private String scope;

    private String value;

    private String defaultValue;

    /**
     * Getter for <code>name</code>.
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>name</code>.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for <code>scope</code>.
     * @return Returns the scope.
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * Setter for <code>scope</code>.
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Getter for <code>value</code>.
     * @return Returns the value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Setter for <code>value</code>.
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Getter for <code>defaultValue</code>.
     * @return Returns the defaultValue.
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Setter for <code>defaultValue</code>.
     * @param defaultValue The defaultValue to set.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
