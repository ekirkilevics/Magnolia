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
public class BeanWithSubBean extends SimpleBean {

    private SimpleBean sub;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BeanWithSubBean.class);


    /**
     * @return the sub
     */
    public SimpleBean getSub() {
        return this.sub;
    }


    /**
     * @param sub the sub to set
     */
    public void setSub(SimpleBean sub) {
        this.sub = sub;
    }

}
