/**
 *
 */
package info.magnolia.jcr.node2bean;

/**
 *
 */
public class BeanWithSubBean extends SimpleBean {

    private SimpleBean sub;

    public SimpleBean getSub() {
        return sub;
    }

    public void setSub(SimpleBean bean) {
        this.sub = bean;
    }


}
