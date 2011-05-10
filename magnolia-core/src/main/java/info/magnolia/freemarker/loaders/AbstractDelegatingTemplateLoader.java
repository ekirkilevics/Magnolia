/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import freemarker.cache.TemplateLoader;

import java.io.IOException;
import java.io.Reader;

/**
 * An abstract TemplateLoader that will attempt to initialize its delegate TemplateLoader
 * and use a null object implementation until it succeeds. This allows us to wrap existing,
 * constructor-configured TemplateLoader implementations, into content2bean-able beans,
 * thus making them configurable in Magnolia's config repository.
 *
 * @author gjoseph
 * @version $Id: $
 */
public abstract class AbstractDelegatingTemplateLoader implements TemplateLoader {
    private static final TemplateLoader NULL = new NullTemplateLoader();

    private TemplateLoader delegate;

    @Override
    public Object findTemplateSource(String name) throws IOException {
        return getDelegate().findTemplateSource(name);
    }

    @Override
    public long getLastModified(Object templateSource) {
        return getDelegate().getLastModified(templateSource);
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        return getDelegate().getReader(templateSource, encoding);
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
        getDelegate().closeTemplateSource(templateSource);
    }

    /**
     * First attempts to initialize a new delegate if needed. If it doesn't succeed, use the NullTemplateLoader.
     */
    protected TemplateLoader getDelegate() {
        if (delegate == null) {
            delegate = newDelegate();
        }
        if (delegate == null) {
            return NULL;
        }
        return delegate;
    }

    /**
     * Implementations should return null if it is too early to instantiate the delegate.
     */
    protected abstract TemplateLoader newDelegate();

    private static class NullTemplateLoader implements TemplateLoader {
        @Override
        public Object findTemplateSource(String name) throws IOException {
            return null;
        }

        @Override
        public long getLastModified(Object templateSource) {
            return -1;
        }

        @Override
        public Reader getReader(Object templateSource, String encoding) throws IOException {
            return null;
        }

        @Override
        public void closeTemplateSource(Object templateSource) throws IOException {
        }
    }
}
