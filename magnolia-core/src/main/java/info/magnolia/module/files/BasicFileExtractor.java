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
package info.magnolia.module.files;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BasicFileExtractor implements FileExtractor {

    public void extractFile(String resourcePath, String absTargetPath) throws IOException {
        newOperation(resourcePath, absTargetPath).extract();
    }

    public void extractFile(String resourcePath, Transformer transformer) throws IOException {
        final String absTargetPath = transformer.accept(resourcePath);
        extractFile(resourcePath, absTargetPath);
    }

    public void extractFiles(Transformer transformer) throws IOException {
        final String[] resources = collectResources(transformer);
        for (int i = 0; i < resources.length; i++) {
            final String resourcePath = resources[i];
            final String absTargetPath = transformer.accept(resourcePath);
            extractFile(resourcePath, absTargetPath);
        }
    }

    protected FileExtractorOperation newOperation(String resourcePath, String absTargetPath) {
        return new BasicFileExtractorOperation(resourcePath, absTargetPath);
    }

    protected String[] collectResources(Transformer transformer) {
        final ClasspathResourcesFilterAdapter filter = new ClasspathResourcesFilterAdapter(transformer);
        return ClasspathResourcesUtil.findResources(filter);
    }

    /**
     * @deprecated should not be used directly. Use FileExtractor with a ModuleFileExtractorTransformer instead
     */
    public void installFiles(String[] names, String prefix) throws IOException {

        checkRoot();

        final StringBuffer error = new StringBuffer();
        for (int j = 0; j < names.length; j++) {
            final String resourcePath = names[j];


            final String relTargetPath = StringUtils.removeStart(resourcePath, prefix);
            final String absTargetPath = Path.getAbsoluteFileSystemPath(relTargetPath);

            try {
                extractFile(resourcePath, absTargetPath);
            } catch (IOException e) {
                error.append(e.getMessage()).append("\n");
            }
        }

        if (error.length() > 0) {
            throw new IOException("Errors while installing files: " + error.toString());
        }

    }

    // TODO : this should probably be in Path.getAppRootDir()
    private void checkRoot() throws IOException {
        String root = null;
        // Try to get root
        try {
            File f = new File(SystemProperty.getProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR));
            if (f.isDirectory()) {
                root = f.getAbsolutePath();
            }
        }
        catch (Exception e) {
            // nothing
        }

        if (root == null) {
            throw new IOException("Invalid magnolia " + SystemProperty.MAGNOLIA_APP_ROOTDIR + " path"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
