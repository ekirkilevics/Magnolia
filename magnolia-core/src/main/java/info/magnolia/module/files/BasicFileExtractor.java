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
package info.magnolia.module.files;

import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.io.IOException;

/**
 * Basic file extractor; does not do any check but provides the actual file extraction mechanisms,
 * via {@link info.magnolia.module.files.FileExtractorOperation}s.
 *
 * @see FileExtractorOperation
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BasicFileExtractor implements FileExtractor {

    @Override
    public void extractFile(String resourcePath, String absTargetPath) throws IOException {
        newOperation(resourcePath, absTargetPath).extract();
    }

    @Override
    public void extractFile(String resourcePath, Transformer transformer) throws IOException {
        final String absTargetPath = transformer.accept(resourcePath);
        extractFile(resourcePath, absTargetPath);
    }

    @Override
    public void extractFiles(Transformer transformer) throws IOException {
        final String[] resources = collectResources(transformer);
        for (final String resourcePath : resources) {
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

}
