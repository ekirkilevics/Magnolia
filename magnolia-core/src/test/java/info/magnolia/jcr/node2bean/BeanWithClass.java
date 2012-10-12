/**
 *
 */
package info.magnolia.jcr.node2bean;

/**
 *
 */
public class BeanWithClass {
    private String foo;
    private Class<?> clazz;

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }
}
