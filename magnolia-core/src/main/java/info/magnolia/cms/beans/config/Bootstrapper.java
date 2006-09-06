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
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.context.MgnlContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Bootstrapper: loads content from xml when a magnolia is started with an uninitialized repository.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class Bootstrapper {
    
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    /**
     * Used to process an additional filtering for the bootstrap files
     * @author philipp
     */
    public interface BootstrapFilter {

        boolean accept(String filename);
    }

    /**
     * don't instantiate
     */
    private Bootstrapper() {
        // unused
    }

    /**
     * Repositories appears to be empty and the <code>"magnolia.bootstrap.dir</code> directory is configured in
     * web.xml. Loops over all the repositories and try to load any xml file found in a subdirectory with the same name
     * of the repository. For example the <code>config</code> repository will be initialized using all the
     * <code>*.xml</code> files found in <code>"magnolia.bootstrap.dir</code><strong>/config</strong> directory.
     * @param bootdirs bootstrap dir
     */
    public static void bootstrapRepositories(String[] bootdirs, BootstrapFilter filter) {

        if (log.isInfoEnabled()) {
            log.info("-----------------------------------------------------------------"); //$NON-NLS-1$
            log.info("Trying to initialize repositories from:");
            for (int i = 0; i < bootdirs.length; i++) {
                log.info(bootdirs[i]);
            }
            log.info("-----------------------------------------------------------------"); //$NON-NLS-1$
        }

        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repositoryName = (String) repositoryNames.next();

            if (!bootstrapRepository(repositoryName, filter, bootdirs)) {
                // exeption was already logged
                break;
            }

            log.info("Repository [{}] has been initialized.", repositoryName); //$NON-NLS-1$
        }
    }

    /**
     * Bootstrap a repository using the default bootstrap directories
     * @param repositoryName
     * @param filter
     * @return true if succeeded
     */
    public static boolean bootstrapRepository(String repositoryName, BootstrapFilter filter) {
        return bootstrapRepository(repositoryName, filter, getBootstrapDirs());
    }

    /**
     * Bootstrap a specific repository
     * @param repositoryName
     * @param filter
     * @param bootdirs
     * @return
     */
    public static boolean bootstrapRepository(String repositoryName, BootstrapFilter filter, String[] bootdirs) {
        Set xmlfileset = getBootstrapFiles(bootdirs, repositoryName, filter);

        if (xmlfileset.isEmpty()) {
            log.info("No bootstrap files found in directory [{}], skipping...", repositoryName); //$NON-NLS-1$
            return true;
        }

        log.info("Trying to import content from {} files into repository [{}]", //$NON-NLS-1$
                Integer.toString(xmlfileset.size()), repositoryName);

        return bootstrapFiles(repositoryName, xmlfileset);
    }

    /**
     * Bootstrap the passed set of files
     * @param repositoryName
     * @param filesSet
     * @return
     */
    public static boolean bootstrapFiles(String repositoryName, Set filesSet) {
        File[] files = (File[]) filesSet.toArray(new File[filesSet.size()]);
        return bootstrapFiles(repositoryName, files);
    }

    /**
     * Bootstrap the array of files
     * @param repositoryName
     * @param files
     * @return
     */
    public static boolean bootstrapFiles(String repositoryName, File[] files) {
        try {
            for (int k = 0; k < files.length; k++) {
                File xmlFile = files[k];
                if(log.isDebugEnabled()){
                    log.debug("execute importfile {}", xmlFile);
                }
                DataTransporter.executeBootstrapImport(xmlFile, repositoryName);
            }
        }
        catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
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

    /**
     * Get the files to bootstrap. The method garantees that only one file is imported if it occures twice in the
     * bootstrap dir. The set is returned sorted, so that the execution fo the import will import the upper most nodes
     * first. This is done using the filelength.
     * @param bootdirs
     * @param repositoryName
     * @param filter
     * @return the sorted set
     */
    public static SortedSet getBootstrapFiles(String[] bootdirs, final String repositoryName, final BootstrapFilter filter) {
        SortedSet xmlfileset = new TreeSet(new Comparator() {

            // remove file with the same name in different dirs
            public int compare(Object file1obj, Object file2obj) {
                File file1 = (File) file1obj;
                File file2 = (File) file2obj;

                String name1 = getName(file1); //$NON-NLS-1$
                String name2 = getName(file2); //$NON-NLS-1$
                
                String ext1 = getExtension(file1);
                String ext2 = getExtension(file2);
                
                if(StringUtils.equals(ext1, ext2)){
                    // a simple way to detect nested nodes
                    if (name1.length() != name2.length()) {
                        return name1.length() - name2.length();
                    }
                }
                else{
                    // import xml first
                    if(ext1.equalsIgnoreCase("xml")){
                        return -1;
                    }
                    else if(ext2.equalsIgnoreCase("properties")){
                        return 1;
                    }
                }
                
                return name1.compareTo(name2);
            }
            
        });

        for (int j = 0; j < bootdirs.length; j++) {
            String bootdir = bootdirs[j];
            File xmldir = new File(bootdir);
            if (!xmldir.exists() || !xmldir.isDirectory()) {
                continue;
            }

            Collection files = FileUtils.listFiles(xmldir, new IOFileFilter(){
                public boolean accept(File file) {
                    return accept(file.getParentFile(), file.getName());
                }
                public boolean accept(File dir, String name) {
                    return name.startsWith(repositoryName + ".")
                        && filter.accept(name)
                        && (name.endsWith(DataTransporter.XML) || name.endsWith(DataTransporter.ZIP) || name
                            .endsWith(DataTransporter.GZ) || name.endsWith(DataTransporter.PROPERTIES));
                }
            }, FileFilterUtils.trueFileFilter());

            xmlfileset.addAll(files);
        }
        
        return xmlfileset;
    }

    private static String getExtension(File file){
        String ext = StringUtils.substringAfterLast(file.getName(),".");
        if(("." + ext).equals(DataTransporter.GZ) || ("." + ext).equals(DataTransporter.ZIP)){
            ext = StringUtils.substringAfterLast(StringUtils.substringBeforeLast(file.getName(), "."), ".");
        }
        return ext;
    }

    private static String getName(File file){
        String name = StringUtils.substringBeforeLast(file.getName(),".");
        if(name.endsWith(DataTransporter.XML) || name.endsWith(DataTransporter.PROPERTIES)){
            name = StringUtils.substringBeforeLast(file.getName(),".");
        }
        return name;
    }
    
    /**
     * Return the standard bootstrap dirs defined in the magnolia.properies file
     * @return Array of directory names
     */
    public static String[] getBootstrapDirs() {
        String bootdirProperty = SystemProperty.getProperty(SystemProperty.MAGNOLIA_BOOTSTRAP_ROOTDIR);

        if (StringUtils.isEmpty(bootdirProperty)) {
            return new String[0];
        }

        String[] bootDirs = StringUtils.split(bootdirProperty);

        // converts to absolute paths
        for (int j = 0; j < bootDirs.length; j++) {
            bootDirs[j] = Path.getAbsoluteFileSystemPath(bootDirs[j]);
        }
        return bootDirs;
    }

}
