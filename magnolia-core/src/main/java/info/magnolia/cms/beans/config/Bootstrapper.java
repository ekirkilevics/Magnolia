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

import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.context.MgnlContext;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

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
    protected static void bootstrapRepositories(String[] bootdirs) {

        if (log.isInfoEnabled()) {
            log.info("-----------------------------------------------------------------"); //$NON-NLS-1$
            log.info("Trying to initialize repositories from:");
            for (int i = 0; i < bootdirs.length; i++) {
                log.info(bootdirs[i]);
            }
            log.info("-----------------------------------------------------------------"); //$NON-NLS-1$
        }

        MgnlContext.setInstance(MgnlContext.getSystemContext());

        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repository = (String) repositoryNames.next();

            Set xmlfileset = new TreeSet(new Comparator() {

                // remove file with the same name in different dirs
                public int compare(Object file1obj, Object file2obj) {
                    File file1 = (File) file1obj;
                    File file2 = (File) file2obj;
                    String fn1 = file1.getParentFile().getName() + '/' + file1.getName();
                    String fn2 = file2.getParentFile().getName() + '/' + file2.getName();
                    return fn1.compareTo(fn2);
                }
            });

            for (int j = 0; j < bootdirs.length; j++) {
                String bootdir = bootdirs[j];
                File xmldir = new File(bootdir, repository);
                if (!xmldir.exists() || !xmldir.isDirectory()) {
                    continue;
                }

                File[] files = xmldir.listFiles(new FilenameFilter() {

                    public boolean accept(File dir, String name) {
                        return name.endsWith(DataTransporter.XML)
                            || name.endsWith(DataTransporter.ZIP)
                            || name.endsWith(DataTransporter.GZ); 
                    }
                });

                xmlfileset.addAll(Arrays.asList(files));

            }

            if (xmlfileset.isEmpty()) {
                log.info("No bootstrap files found in directory [{}], skipping...", repository); //$NON-NLS-1$
                continue;
            }

            log
                .info(
                    "Trying to import content from {} files into repository [{}]", Integer.toString(xmlfileset.size()), repository); //$NON-NLS-1$

            File[] files = (File[]) xmlfileset.toArray(new File[xmlfileset.size()]);
            Arrays.sort(files, new Comparator() {

                public int compare(Object file1, Object file2) {
                    String name1 = StringUtils.substringBeforeLast(((File) file1).getName(), "."); //$NON-NLS-1$
                    String name2 = StringUtils.substringBeforeLast(((File) file2).getName(), "."); //$NON-NLS-1$
                    // a simple way to detect nested nodes
                    return name1.length() - name2.length();
                }
            });

            try {
                for (int k = 0; k < files.length; k++) {

                    File xmlfile = files[k];
                    DataTransporter.executeBootstrapImport(xmlfile, repository);
                }

            }
            catch (IOException ioe) {
                log.error(ioe.getMessage(), ioe);
            }
            catch (OutOfMemoryError e) {
                int maxMem = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
                int needed = Math.max(256, maxMem + 128);
                log
                    .error(
                        "Unable to complete bootstrapping: out of memory.\n" //$NON-NLS-1$
                            + "{} MB were not enough, try to increase the amount of memory available by adding the -Xmx{}m parameter to the server startup script.\n" //$NON-NLS-1$
                            + "You will need to completely remove the magnolia webapp before trying again", Integer.toString(maxMem), Integer.toString(needed)); //$NON-NLS-1$
                break;
            }

            log.info("Repository [{}] has been initialized.", repository); //$NON-NLS-1$

        }
    }

}
