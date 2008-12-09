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
package info.magnolia.importexport;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.ie.DataTransporter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Bootstrapper: loads content from xml when a magnolia is started with an uninitialized repository.
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class Bootstrapper {
    private static final Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    /**
     * Used to process an additional filtering for the bootstrap files
     *
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
     *
     * @param bootdirs bootstrap dir
     */
    public static void bootstrapRepositories(String[] bootdirs, BootstrapFilter filter) {
        log.info("-----------------------------------------------------------------");
        log.info("Trying to initialize repositories from: {}", StringUtils.join(bootdirs, ", "));
        for (int i = 0; i < bootdirs.length; i++) {
            log.info(bootdirs[i]);
        }
        log.info("-----------------------------------------------------------------");

        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repositoryName = (String) repositoryNames.next();

            if (!bootstrapRepository(bootdirs, repositoryName, filter)) {
                // exeption was already logged
                break;
            }

            log.info("Repository [{}] has been initialized.", repositoryName); //$NON-NLS-1$
        }
    }

    /**
     * Bootstrap a specific repository.
     */
    public static boolean bootstrapRepository(String[] bootdirs, String repositoryName, BootstrapFilter filter) {
        Set xmlfileset = getBootstrapFiles(bootdirs, repositoryName, filter);

        if (xmlfileset.isEmpty()) {
            log.debug("No bootstrap files found for repository [{}], skipping...", repositoryName); //$NON-NLS-1$
            return true;
        }

        log.info("Trying to import content from {} files into repository [{}]", //$NON-NLS-1$
                Integer.toString(xmlfileset.size()), repositoryName);

        final File[] files = (File[]) xmlfileset.toArray(new File[xmlfileset.size()]);
        return bootstrapFiles(repositoryName, files);
    }

    /**
     * Bootstrap the array of files
     */
    private static boolean bootstrapFiles(String repositoryName, File[] files) {
        try {
            for (int k = 0; k < files.length; k++) {
                File xmlFile = files[k];
                log.debug("execute importfile {}", xmlFile);
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
     *
     * @return the sorted set
     */
    private static SortedSet getBootstrapFiles(String[] bootdirs, final String repositoryName, final BootstrapFilter filter) {
        SortedSet xmlfileset = new TreeSet(new BootstrapFilesComparator());

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
