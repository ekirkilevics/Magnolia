/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.commands;

import info.magnolia.cms.util.AlertUtil;

import java.util.Collections;
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

    public static Logger log = LoggerFactory.getLogger(MgnlCommand.class);

    /**
     * The default properties. Lazy bound.
     */
    private Map defaultProperties;

    private boolean isClone = false;

    private boolean isEnabled = true;

    /**
     * Command factory creating clones of the master/prototype command and pooling the cloned instances.
     * @author Philipp Bracher
     * @version $Id$
     */
    class MgnlCommandFactory extends BasePoolableObjectFactory {

        /**
         * The prototype we clone for faster execution.
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
     * Pool of inner commands.
     */
    private StackObjectPool pool;

    /**
     * True if we can use object pooling. Else we synchronize the execution.
     */
    private boolean pooling = true;

    /**
     * Make sure that the context is castable to a magnolia context.
     * @return true on success, false otherwise
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
