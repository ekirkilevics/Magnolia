/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.util;

import info.magnolia.cms.core.ie.DataTransporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.jcr.ImportUUIDBehavior;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class BootstrapUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BootstrapUtil.class);

    /**
     * Bootstraps content from the given set of files. Repository name and base path will be determined from filenames directly.
     * @return success
     * @throws FileNotFoundException 
     */
    public static boolean bootstrap(Set filenames) {
        return bootstrap(filenames, true);
    }

    public static boolean bootstrap(Set filenames, boolean saveAfterImport) {
        for (Iterator iter = filenames.iterator(); iter.hasNext(); ) {
            final String filename = (String) iter.next();
            boolean success = bootstrap(new File(filename), saveAfterImport);
            
            if (!success)
                return false;
        }
        return true;
    }
    
    public static boolean bootstrap(File[] files) {
        return bootstrap(files, true);
    }
    
    public static boolean bootstrap(File[] files, boolean saveAfterImport) {
        for (int k = 0; k < files.length; k++) {
            boolean success = bootstrap(files[k], saveAfterImport);
            
            if (!success)
                return false;
        }
        return true;
    }

    /**
     * Bootstraps content from the given file. Repository name and base path will be determined from filename directly.
     * @return success
     * @throws FileNotFoundException 
     */
    public static boolean bootstrap(File file) {
        return bootstrap(file, true);
    }

    public static boolean bootstrap(File file, boolean saveAfterImport) {
        final String filename = file.getName();
        final String repositoryName = determineRepository(filename);
        final String basePath = determineBasePath(filename);
        
        return bootstrap(repositoryName, basePath, file, true);
    }

    /**
     * Bootstraps content from the given files. Base path will be determined from filenames directly.
     * @return success
     * @throws FileNotFoundException 
     */
    public static boolean bootstrap(String repositoryName, File[] files) {
        return bootstrap(repositoryName, files, true);
    }
    
    public static boolean bootstrap(String repositoryName, File[] files, boolean saveAfterImport) {
        for (int k = 0; k < files.length; k++) {
            boolean success = bootstrap(repositoryName, files[k], saveAfterImport);
            
            if (!success)
                return false;
        }
        return true;
    }

    /**
     * Bootstraps content from the given file. Base path will be determined from filename directly.
     * @return success
     * @throws FileNotFoundException 
     */
    public static boolean bootstrap(String repositoryName, File file) {
        return bootstrap(repositoryName, file, true);
    }

    public static boolean bootstrap(String repositoryName, File file, boolean saveAfterImport) {
        return bootstrap(repositoryName, determineBasePath(file.getName()), file, saveAfterImport);
    }

    /**
     * Bootstraps content from the given file.
     * @param saveAfterImport TODO
     * @return success
     * @throws FileNotFoundException 
     */
    public static boolean bootstrap(String repositoryName, String basePath, File file, boolean saveAfterImport) {
        try {
            return bootstrap(repositoryName, basePath, new FileInputStream(file), file.getName(), saveAfterImport);
        }
        catch (FileNotFoundException e) {
            log.error("Cannot create stream for file " + file.getPath(), e);
        }
        return false;
    }

    public static boolean bootstrap(String repositoryName, String basePath, InputStream input, String filename, boolean saveAfterImport) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Bootstrapping file " + filename + " into " + repositoryName + " repository, with base path " + basePath);
            }
            
            if (filename.endsWith(DataTransporter.PROPERTIES)){
                Properties properties = new Properties();
                properties.load(input);
                DataTransporter.importProperties(properties, repositoryName);
            }
            else {
                // TODO !! this ImportUUIDBehavior might import nodes in the wrong place !!!
                DataTransporter.importXmlStream(input, repositoryName, basePath, filename, false, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING, saveAfterImport, true);
            }
        }
        catch (IOException e) {
            log.error("Cannot bootstrap from file " + filename, e);
        }
        catch (OutOfMemoryError e) {
            int maxMem = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
            int needed = Math.max(256, maxMem + 128);
            log.error("Unable to complete bootstrapping: out of memory.\n" //$NON-NLS-1$
                + "{} MB were not enough, try to increase the amount of memory available by adding the -Xmx{}m parameter to the server startup script.\n" //$NON-NLS-1$
                + "You will need to completely remove the magnolia webapp before trying again", //$NON-NLS-1$
                Integer.toString(maxMem), Integer.toString(needed));
            return false;
        }
        return true;
    }
    
    public static String determineRepository(String filename) {
        return StringUtils.substringBefore(cleanupFilename(filename), ".");
    }
    
    public static String determinePath(String filename) {
        String withoutExtensionAndRepository = StringUtils.substringAfter(cleanupFilename(filename), ".");
        String path = StringUtils.replace(withoutExtensionAndRepository, ".", "/");
        return (StringUtils.isEmpty(path) ? "/" : "/" + path);
    }
    
    public static String determineBasePath(String filename) {
        final String fullPath = determinePath(filename);
        final String basePath = StringUtils.substringBeforeLast(fullPath, "/");
        return (StringUtils.isEmpty(basePath) ? "/" : basePath);
    }
    
    public static String determineNodeName(String filename) {
        final String fullPath = determinePath(filename);
        return StringUtils.substringAfterLast(fullPath, "/");
    }
    
    public static String[] determinePaths(String[] filenames) {
        String[] paths = new String[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            paths[i] = determinePath(filenames[i]);
        }
        return paths;
    }
    
    /**
     * Cleans up the filename. Takes extensions like ".properties", ".xml", ".xml.gz" into account
     * @param filename
     * @return
     */
    private static String cleanupFilename(String filename) {
        filename = StringUtils.replace(filename, "\\", "/");
        filename = StringUtils.substringAfterLast(filename, "/");
        filename = StringUtils.substringBeforeLast(filename, ".");
        
        return StringUtils.removeEnd(filename, DataTransporter.XML);
    }
}
