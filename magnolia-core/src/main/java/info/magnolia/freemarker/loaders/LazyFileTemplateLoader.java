/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.freemarker.loaders;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

import java.io.File;
import java.io.IOException;

/**
 * A TemplateLoader wrapping freemarker's FileTemplateLoader, initializing it
 * lazily, thus allowing setting its parameters with content2bean.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class LazyFileTemplateLoader extends AbstractDelegatingTemplateLoader {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LazyFileTemplateLoader.class);

    private String basePath;
    private boolean allowLinking;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public boolean isAllowLinking() {
        return allowLinking;
    }

    public void setAllowLinking(boolean allowLinking) {
        this.allowLinking = allowLinking;
    }

    protected TemplateLoader newDelegate() {
        try {
            return new FileTemplateLoader(new File(basePath), allowLinking);
        } catch (IOException e) {
            log.error("Can't initialize FileTemplateLoader: " + e.getMessage(), e);
            throw new IllegalArgumentException("Can't initialize FileTemplateLoader: " + e.getMessage());
        }
    }
}
