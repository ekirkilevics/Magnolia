/**
 *
 */
package info.magnolia.jcr.node2bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class BeanWithArray extends SimpleBean {
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BeanWithArray.class);

    private SimpleBean[] beans = new SimpleBean[0];

    public SimpleBean[] getBeans(){
        return beans;
    }

    public void setBeans(SimpleBean[] beans) {
        this.beans = beans;
    }
}
