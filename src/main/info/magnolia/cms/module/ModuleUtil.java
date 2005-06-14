/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.module;

import info.magnolia.cms.beans.config.ModuleLoader;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.AccessDeniedException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * This is a util providing some methods for the registration process of a module.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public final class ModuleUtil {

    
    /**
     * Util has no public constructor 
     */
    private ModuleUtil() {
    }
    
    /**
     * blocksize
     */
    public static final int DATA_BLOCK_SIZE = 1024 * 1024;

    /**
     * Extracts files of a jar and stores them in the magnolia file structure
     * @param jar the jar containing the files (jsp, images)
     * @throws Exception io exception
     */
    public static void installFiles(JarFile jar) throws Exception {

        String root = null;
        //Try to get root
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
            throw new Exception("Invalid magnolia " + SystemProperty.MAGNOLIA_APP_ROOTDIR + " path");
        }

        Map files = new HashMap();
        Enumeration entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String name = entry.getName().toUpperCase();
            //Exclude root, dirs, ch-dir, META-INF-dir and jars
            if (!name.equals("/")
                & !name.endsWith("/")
                & !name.startsWith("CH")
                & !name.startsWith("META-INF")
                & !name.endsWith(".JAR")) {
                files.put(new File(root, entry.getName()), entry);
            }
        }

        //Loop throgh files an check writeable
        String error = StringUtils.EMPTY;
        Iterator iter = files.keySet().iterator();
        while (iter.hasNext()) {
            File file = (File) iter.next();
            String s = StringUtils.EMPTY;
            if (!file.getParentFile().exists() & !file.getParentFile().mkdirs()) {
                s = "Can't create directories for " + file.getAbsolutePath();
            }
            else if (!file.getParentFile().canWrite()) {
                s = "Can't write to " + file.getAbsolutePath();
            }
            if (s.length() > 0) {
                if (error.length() > 0) {
                    error += "\r\n";
                }
                error += s;
            }
        }

        if (error.length() > 0) {
            throw new Exception("Errors while installing files: " + error);
        }

        //Copy files
        iter = files.keySet().iterator();
        while (iter.hasNext()) {
            File file = (File) iter.next();
            JarEntry entry = (JarEntry) files.get(file);

            int byteCount = 0;
            byte[] data = new byte[DATA_BLOCK_SIZE];

            InputStream in = null;
            BufferedOutputStream out = null;

            try {
                in = jar.getInputStream(entry);
                out = new BufferedOutputStream(new FileOutputStream(file), DATA_BLOCK_SIZE);

                while ((byteCount = in.read(data, 0, DATA_BLOCK_SIZE)) != -1) {
                    out.write(data, 0, byteCount);
                }
            }
            finally {
                try {
                    out.close();
                }
                catch (Exception e) {
                    // nothing
                }
            }
        }
    }

    /**
     * Create a minimal module configuration
     * 
     * @param node the module node
     * @param name module name
     * @param className the class used
     * @param version version number of the module
     * @return the modified node (not yet stored)
     * @throws AccessDeniedException exception
     * @throws PathNotFoundException exception
     * @throws RepositoryException exception
     */
    public static Content createMinimalConfiguration(Content node, String name, String className, String version)
        throws AccessDeniedException, PathNotFoundException, RepositoryException {
        node.createNodeData("version").setValue(version);
        node.createNodeData("license");
        node.createContent("Config");
        node.createContent("VirtualURIMapping", ItemType.CONTENTNODE);
    
        Content register = node.createContent(ModuleLoader.CONFIG_NODE_REGISTER, ItemType.CONTENTNODE);
        register.createNodeData("moduleName");
        register.createNodeData("moduleDescription");
        register.createNodeData("class").setValue(className);
        register.createNodeData("repository");
        register.createContent("sharedRepositories", ItemType.CONTENTNODE);
        register.createContent("initParams", ItemType.CONTENTNODE);
        return node;
    }

}