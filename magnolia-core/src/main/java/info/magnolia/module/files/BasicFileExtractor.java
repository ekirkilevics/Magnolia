/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
