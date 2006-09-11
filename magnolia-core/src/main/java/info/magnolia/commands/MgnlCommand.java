/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.commands;

import info.magnolia.cms.util.AlertUtil;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;


/**
 * To make the coding of commands as easy as possible the default values set in the config are set and the values of the
 * context are set as properties too if the naming matches. To get a better performance we use an inner object pool. The
 * execution of the command gets a clone from the pool and executes the clone
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class MgnlCommand implements Command {

    /**
     * Create clones
     * @author Philipp Bracher
     * @version $Id$
     */
    class MgnlCommandFactory extends BasePoolableObjectFactory {

        /**
         * The prototype we clone for faster execution
         */
        private MgnlCommand prototype;

        /**
         * The default properties. Lazy bound.
         */
        private Map defaultProperties;

        /**
         * @param prototype
         */
        public MgnlCommandFactory(MgnlCommand prototype) {
            this.prototype = prototype;
        }

        public Object makeObject() throws Exception {
            return BeanUtils.cloneBean(this.prototype);
        }

        public void activateObject(Object arg0) throws Exception {
            super.activateObject(arg0);
            // set default properties
            BeanUtils.populate(arg0, this.getDefaultProperties());
        }

        /**
         * Get them before we clone the first time. This ensures that the default properties are set
         * @return
         */
        private Map getDefaultProperties() {
            if (this.defaultProperties == null) {
                try {
                    this.defaultProperties = BeanUtils.describe(prototype);
                }
                catch (Exception e) {
                    this.defaultProperties = Collections.EMPTY_MAP;
                }
            }

            return this.defaultProperties;
        }

    }

    /**
     * Pool of inner commands
     */
    private StackObjectPool pool;

    public MgnlCommand() {
        pool = new StackObjectPool(new MgnlCommandFactory(this));
    }

    /**
     * Make sure that the context is castable to a magnolia context
     */
    public boolean execute(Context ctx) throws Exception {
        if(!(ctx instanceof info.magnolia.context.Context)){
            throw new IllegalArgumentException("context must be of type " + info.magnolia.context.Context.class);
        }
        
        MgnlCommand cmd = (MgnlCommand) pool.borrowObject();
        boolean ret = true; // break execution

        try {
            // populate the command
            BeanUtils.populate(cmd, ctx);
            // convert the confusing true false behavior and cast to mgnl context class
            ret = !cmd.execute((info.magnolia.context.Context) ctx);
        }
        catch(Exception e){
            AlertUtil.setException(e, (info.magnolia.context.Context)ctx);
        }
        finally {
            pool.returnObject(cmd);
        }

        return ret;
    }

    public abstract boolean execute(info.magnolia.context.Context context) throws Exception;

}
