/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.init.properties;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Abstract implementation of {@link info.magnolia.init.PropertySource}, providing the basic mechanisms
 * for loading {@link Properties} from a given {@link InputStream}.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractStreamBasedPropertySource extends AbstractPropertySource {
    private final String path;

    protected AbstractStreamBasedPropertySource(InputStream stream, String path) throws IOException {
        super(new ConstructingProperties(stream, path));
        this.path = path;
    }

    @Override
    public String describe() {
        return "[" + getClass().getSimpleName() + " from " + path + "]";
    }

    /**
     * A subclass of {@link Properties} which populates itself with a given InputStream.
     * The given InputStream is silently closed after properties have been loaded.
     */
    public static class ConstructingProperties extends Properties {
        private final String path;

        /**
         * @param in the InputStream to load properties from.
         * @param path optional reference to a path describing where the given InputStream has been opened from.
         */
        public ConstructingProperties(InputStream in, String path) throws IOException {
            super();
            this.path = path;
            loadAndClose(in);
        }

        protected void loadAndClose(InputStream in) throws IOException {
            if (in == null) {
                throw new FileNotFoundException("Null stream for path " + path);
            }
            try {
                load(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

        @Override
        public String toString() {
            return "[Properties loaded from " + path + ": <" + super.toString() + ">]";
        }
    }

}
