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

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class BeanWithArrayAndAdder extends SimpleBean {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BeanWithArrayAndAdder.class);

    private SimpleBean[] beans = new SimpleBean[0];

    public SimpleBean[] getBeans(){
        return beans;
    }

    public void addBeans(SimpleBean bean){
        beans = (SimpleBean[]) ArrayUtils.add(beans, bean);
    }
}
