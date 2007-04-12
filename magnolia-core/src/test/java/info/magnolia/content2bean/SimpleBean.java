/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.content2bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class SimpleBean {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SimpleBean.class);

    private String prop1;
    private String prop2;

    /**
     * @return the prop1
     */
    public String getProp1() {
        return this.prop1;
    }

    /**
     * @param prop1 the prop1 to set
     */
    public void setProp1(String prop1) {
        this.prop1 = prop1;
    }

    /**
     * @return the prop2
     */
    public String getProp2() {
        return this.prop2;
    }

    /**
     * @param prop2 the prop2 to set
     */
    public void setProp2(String prop2) {
        this.prop2 = prop2;
    }

}
