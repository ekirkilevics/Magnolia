/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.templatinguicomponents.jsp.test4web;
/**
 *
 * Openutils web test utils (http://www.openmindlab.com/lab/products/testing4web.html)
 * Copyright(C) 2008-2010, Openmind S.r.l. http://www.openmindonline.it
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.apache.jasper.EmbeddedServletOptions;
    import org.apache.jasper.Options;
    import org.apache.jasper.compiler.JspConfig;
    import org.apache.jasper.compiler.TagPluginManager;
    import org.apache.jasper.compiler.TldLocationsCache;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.Map;

/**
 *copied from openmind's test4web
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class TestServletOptions implements Options {

    private EmbeddedServletOptions options;

    /**
     * @param config
     * @param context
     */
    public TestServletOptions(ServletConfig config, ServletContext context)
    {
        options = new EmbeddedServletOptions(config, context);
        options.setTldLocationsCache(new TestTldLocationsCache(context));
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return options.hashCode();
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        return options.equals(obj);
    }

    /**
     * @param name
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getProperty(java.lang.String)
     */
    public String getProperty(String name)
    {
        return options.getProperty(name);
    }

    /**
     * @param name
     * @param value
     * @see org.apache.jasper.EmbeddedServletOptions#setProperty(java.lang.String, java.lang.String)
     */
    public void setProperty(String name, String value)
    {
        options.setProperty(name, value);
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getKeepGenerated()
     */
    public boolean getKeepGenerated()
    {
        return options.getKeepGenerated();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getTrimSpaces()
     */
    public boolean getTrimSpaces()
    {
        return options.getTrimSpaces();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#isPoolingEnabled()
     */
    public boolean isPoolingEnabled()
    {
        return options.isPoolingEnabled();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getMappedFile()
     */
    public boolean getMappedFile()
    {
        return options.getMappedFile();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getSendErrorToClient()
     */
    public boolean getSendErrorToClient()
    {
        return options.getSendErrorToClient();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getClassDebugInfo()
     */
    public boolean getClassDebugInfo()
    {
        return options.getClassDebugInfo();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getCheckInterval()
     */
    public int getCheckInterval()
    {
        return options.getCheckInterval();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getModificationTestInterval()
     */
    public int getModificationTestInterval()
    {
        return options.getModificationTestInterval();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getDevelopment()
     */
    public boolean getDevelopment()
    {
        return options.getDevelopment();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#isSmapSuppressed()
     */
    public boolean isSmapSuppressed()
    {
        return options.isSmapSuppressed();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#isSmapDumped()
     */
    public boolean isSmapDumped()
    {
        return options.isSmapDumped();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#genStringAsCharArray()
     */
    public boolean genStringAsCharArray()
    {
        return options.genStringAsCharArray();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getIeClassId()
     */
    public String getIeClassId()
    {
        return options.getIeClassId();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getScratchDir()
     */
    public File getScratchDir()
    {
        return options.getScratchDir();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getClassPath()
     */
    public String getClassPath()
    {
        return options.getClassPath();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#isXpoweredBy()
     */
    public boolean isXpoweredBy()
    {
        return options.isXpoweredBy();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getCompiler()
     */
    public String getCompiler()
    {
        return options.getCompiler();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getCompilerTargetVM()
     */
    public String getCompilerTargetVM()
    {
        return options.getCompilerTargetVM();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getCompilerSourceVM()
     */
    public String getCompilerSourceVM()
    {
        return options.getCompilerSourceVM();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getErrorOnUseBeanInvalidClassAttribute()
     */
    public boolean getErrorOnUseBeanInvalidClassAttribute()
    {
        return options.getErrorOnUseBeanInvalidClassAttribute();
    }

    /**
     * @param b
     * @see org.apache.jasper.EmbeddedServletOptions#setErrorOnUseBeanInvalidClassAttribute(boolean)
     */
    public void setErrorOnUseBeanInvalidClassAttribute(boolean b)
    {
        options.setErrorOnUseBeanInvalidClassAttribute(b);
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getTldLocationsCache()
     */
    public TldLocationsCache getTldLocationsCache()
    {
        return options.getTldLocationsCache();
    }

    /**
     * @param tldC
     * @see org.apache.jasper.EmbeddedServletOptions#setTldLocationsCache(org.apache.jasper.compiler.TldLocationsCache)
     */
    public void setTldLocationsCache(TldLocationsCache tldC)
    {
        options.setTldLocationsCache(tldC);
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getJavaEncoding()
     */
    public String getJavaEncoding()
    {
        return options.getJavaEncoding();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getFork()
     */
    public boolean getFork()
    {
        return options.getFork();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getJspConfig()
     */
    public JspConfig getJspConfig()
    {
        return options.getJspConfig();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getTagPluginManager()
     */
    public TagPluginManager getTagPluginManager()
    {
        return options.getTagPluginManager();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#isCaching()
     */
    public boolean isCaching()
    {
        return options.isCaching();
    }

    /**
     * @return
     * @see org.apache.jasper.EmbeddedServletOptions#getCache()
     */
    public Map getCache()
    {
        return options.getCache();
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return options.toString();
    }
}
