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
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.exception.NestableException;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * To make the coding of commands as easy as possible the default values set in the config are set and the values of the
 * context are set as properties too if the naming matches. To get a better performance we use an inner object pool. The
 * execution of the command gets a clone from the pool and executes the clone
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class MgnlCommand implements Command {

    /**
     * Logger
     */
    public static Logger log = LoggerFactory.getLogger(MgnlCommand.class);


    /**
     * The default properties. Lazy bound.
     */
    private Map defaultProperties;

    private boolean isClone = false;

    private boolean isEnabled = true;

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
         * @param prototype
         */
        public MgnlCommandFactory(MgnlCommand prototype) {
            this.prototype = prototype;
        }

        public Object makeObject() throws Exception {
            MgnlCommand cmd = (MgnlCommand) BeanUtils.cloneBean(this.prototype);
            cmd.setClone(true);
            return cmd;
        }

        public void activateObject(Object arg0) throws Exception {
            super.activateObject(arg0);
            // set default properties
            BeanUtils.populate(arg0, defaultProperties);
        }

        public void passivateObject(Object cmd) throws Exception {
            ((MgnlCommand) cmd).release();
            super.passivateObject(cmd);
        }

    }

    /**
     * Pool of inner commands
     */
    private StackObjectPool pool;

    /**
     * True if we can use object pooling. Else we synchronize the execution.
     */
    private boolean pooling = true;

    /**
     * Make sure that the context is castable to a magnolia context
     */
    public boolean execute(Context ctx) throws Exception {
        if (!(ctx instanceof info.magnolia.context.Context)) {
            throw new IllegalArgumentException("context must be of type " + info.magnolia.context.Context.class);
        }

        if (this.defaultProperties == null) {
            initDefaultProperties();
        }

        MgnlCommand cmd;

        if (pooling) {
            // do not instantiate until the pool is really needed
            // means: do not create a pool for command objects created in the pool itself
            if (pool == null) {
                pool = new StackObjectPool(new MgnlCommandFactory(this));
            }

            try {
                // try to use the pool
                cmd = (MgnlCommand) pool.borrowObject();
            }
            // this happens if the commons constructor is not public: anonymous classes for example
            catch (Throwable t) {
                pooling = false;
                // start again
                return execute(ctx);
            }
        }
        else {
            cmd = this;
        }

        boolean success = executePooledOrSynchronized(ctx, cmd);
        // convert the confusing true false behavior to fit commons chain
        return !success;
    }

    private boolean executePooledOrSynchronized(Context ctx, MgnlCommand cmd) throws Exception {
        boolean success = false; // break execution

        // populate the command if we are using a pool
        if (pooling) {
            BeanUtils.populate(cmd, ctx);
            // cast to mgnl context class
            try {
                success = cmd.execute((info.magnolia.context.Context) ctx);
            }
            catch (Exception e) {
                AlertUtil.setException(e, (info.magnolia.context.Context) ctx);
                throw new NestableException("exception during executing command", e);
            }
            finally {
                pool.returnObject(cmd);
            }
        }
        else {
            synchronized (cmd) {
                BeanUtils.populate(cmd, ctx);
                try {
                    success = cmd.execute((info.magnolia.context.Context) ctx);
                }
                catch (Exception e) {
                    AlertUtil.setException(e, (info.magnolia.context.Context) ctx);
                    throw new NestableException("exception during executing command", e);
                }
                finally {
                    if (pooling) {
                        pool.returnObject(cmd);
                    }
                    else {
                        cmd.release();
                        BeanUtils.populate(cmd, defaultProperties);
                    }
                }
            }
        }
        return success;
    }

    private void initDefaultProperties() {
        try {
            this.defaultProperties = PropertyUtils.describe(this);
        }
        catch (Exception e) {
            this.defaultProperties = Collections.EMPTY_MAP;
        }
    }

    public abstract boolean execute(info.magnolia.context.Context context) throws Exception;

    /**
     * If a clone is passivated we call this method. Please clean up private properties.
     */
    public void release() {
    }

    /**
     * @return the isClone
     */
    protected boolean isClone() {
        return isClone;
    }

    /**
     * @param isClone the isClone to set
     */
    protected void setClone(boolean isClone) {
        this.isClone = isClone;
    }


    public boolean isEnabled() {
        return this.isEnabled;
    }


    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

}
