/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.module;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * This is a util providing some methods for the registration process of a module.
 * @author philipp
 * @version $Revision$ ($Author$)
 * @deprecated most methods here should be replaced by implementations of info.magnolia.module.delta.Task
 */
public final class ModuleUtil {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ModuleUtil.class);

    /**
     * Util has no public constructor
     */
    private ModuleUtil() {
    }

    public static void bootstrap(String[] resourceNames, boolean saveAfterImport, int importUUIDBehavior) throws IOException, RepositoryException {
        // sort by length --> import parent node first
        List list = new ArrayList(Arrays.asList(resourceNames));

        Collections.sort(list, new Comparator() {

            public int compare(Object name1, Object name2) {
                return ((String) name1).length() - ((String) name2).length();
            }
        });

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            String resourceName = (String) iter.next();

            // windows again
            resourceName = StringUtils.replace(resourceName, "\\", "/");

            String name = StringUtils.removeEnd(StringUtils.substringAfterLast(resourceName, "/"), ".xml");

            String repository = StringUtils.substringBefore(name, ".");
            String pathName = StringUtils.substringAfter(StringUtils.substringBeforeLast(name, "."), "."); //$NON-NLS-1$
            String nodeName = StringUtils.substringAfterLast(name, ".");
            String fullPath;
            if (StringUtils.isEmpty(pathName)) {
                pathName = "/";
                fullPath = "/" + nodeName;
            }
            else {
                pathName = "/" + StringUtils.replace(pathName, ".", "/");
                fullPath = pathName + "/" + nodeName;
            }

            log.debug("Will bootstrap {}", resourceName);
            final InputStream stream = ModuleUtil.class.getResourceAsStream(resourceName);
            if (stream == null) {
                throw new IOException("Can't find resource to bootstrap at " + resourceName);
            }

            // if the path already exists --> delete it
            try {
                final HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

                // hm can be null if module is not properly registered and the repository has not been created
                if (hm != null && hm.isExist(fullPath)) {
                    hm.delete(fullPath);
                    log.warn("already existing node [{}] deleted", fullPath);
                }
            } catch (RepositoryException e) {
                throw new RepositoryException("can't register bootstrap file: [" + name + "]", e);
            }

            DataTransporter.importXmlStream(stream, repository, pathName, name, false,
                importUUIDBehavior,
                saveAfterImport,
                true);
        }
    }


}
