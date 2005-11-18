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
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.HierarchyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Bootstrapper: loads content from xml when a magnolia is started with an uninitialized repository.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class Bootstrapper {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Bootstrapper.class);

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
     * @param bootdir bootstrap dir
     */
    protected static void bootstrapRepositories(String[] bootdirs) {

        System.out.println("\n-----------------------------------------------------------------\n"); //$NON-NLS-1$
        System.out.println("Trying to initialize repositories from [" + ArrayUtils.toString(bootdirs) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        System.out.println("\n-----------------------------------------------------------------\n"); //$NON-NLS-1$

        log.info("Trying to initialize repositories from [" + ArrayUtils.toString(bootdirs) + "]"); //$NON-NLS-1$ //$NON-NLS-2$

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
                        return name.endsWith(".xml"); //$NON-NLS-1$
                    }
                });

                xmlfileset.addAll(Arrays.asList(files));

            }

            if (xmlfileset.isEmpty()) {
                log.info("No xml files found in directory [" + repository + "], skipping..."); //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            }

            log.info("Trying to import content from " + xmlfileset.size() + " files..."); //$NON-NLS-1$ //$NON-NLS-2$

            HierarchyManager hr = ContentRepository.getHierarchyManager(repository);
            Session session = hr.getWorkspace().getSession();

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

                    InputStream filteredStream = null;
                    File filteredFile = null;
                    try {
                        filteredFile = filterVersionsFromFile(xmlfile);
                        filteredStream = new FileInputStream(filteredFile);

                        String pathName = StringUtils.substringAfter(StringUtils.substringBeforeLast(StringUtils
                            .substringBeforeLast(xmlfile.getName(), "."), "."), "."); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$

                        pathName = "/" + StringUtils.replace(pathName, ".", "/");

                        log
                            .info("Importing content from " + xmlfile.getAbsolutePath() + " to path \"" + pathName + "\""); //$NON-NLS-1$

                        session.importXML(
                            pathName,
                            filteredStream,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING); //$NON-NLS-1$
                    }
                    catch (Exception e) {
                        log.error("Unable to load content from " //$NON-NLS-1$
                            + xmlfile.getName() + " due to a " //$NON-NLS-1$
                            + e.getClass().getName() + " Exception: " //$NON-NLS-1$
                            + e.getMessage() + ". Will try to continue.", e); //$NON-NLS-1$
                    }
                    finally {
                        if (filteredFile != null) {
                            filteredFile.delete();
                        }
                        IOUtils.closeQuietly(filteredStream);
                    }
                }

                log.info("Saving changes to [" + repository + "]"); //$NON-NLS-1$ //$NON-NLS-2$

                try {
                    session.save();
                }
                catch (RepositoryException e) {
                    log.error("Unable to save changes to the [" //$NON-NLS-1$
                        + repository + "] repository due to a " //$NON-NLS-1$
                        + e.getClass().getName() + " Exception: " //$NON-NLS-1$
                        + e.getMessage() + ". Will try to continue.", e); //$NON-NLS-1$
                    continue;
                }
            }
            catch (OutOfMemoryError e) {

                int maxMem = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
                int needed = Math.max(256, maxMem + 128);

                log.error("Unable to complete bootstrapping: out of memory.\n" //$NON-NLS-1$
                    + maxMem + "MB were not enough, try to increase the amount of memory available by adding the -Xmx" //$NON-NLS-1$
                    + needed + " parameter to the server startup script.\n" //$NON-NLS-1$
                    + "You will need to completely remove the magnolia webapp before trying again"); //$NON-NLS-1$
                break;
            }

            log.info("Repository [" + repository + "] has been initialized."); //$NON-NLS-1$ //$NON-NLS-2$

        }
    }

    /**
     * Strips all the versioning information from xml using a SaxFilter. The filtered content is written to a temporary
     * file and then returned as an InputStream.
     * @param xmlfile input xml file
     * @return input stream from a filtered xml file
     * @throws IOException for errors in accessing the original or modified file
     * @throws SAXException errors during xml parsing
     */
    protected static File filterVersionsFromFile(File xmlfile) throws IOException, SAXException {

        // create a temporary file and save the trimmed xml
        File strippedFile = File.createTempFile("import", "xml"); //$NON-NLS-1$ //$NON-NLS-2$

        FileOutputStream outstream = new FileOutputStream(strippedFile);

        // use XMLSerializer and a SAXFilter in order to rewrite the file
        XMLReader reader = new VersionFilter(XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class
            .getName()));
        reader.setContentHandler(new XMLSerializer(outstream, new OutputFormat()));

        InputStream stream;
        try {
            stream = new FileInputStream(xmlfile);
        }
        catch (FileNotFoundException e) {
            // should never happen
            throw new NestableRuntimeException(e);
        }

        try {
            reader.parse(new InputSource(stream));
        }
        finally {
            IOUtils.closeQuietly(stream);
        }

        // return the filtered file as an input stream
        return strippedFile;
    }

    /**
     * Sax filter, strips version information from a jcr xml (system view).
     */
    public static class VersionFilter extends XMLFilterImpl {

        /**
         * if != 0 we are in the middle of a filtered element.
         */
        private int inVersionElement;

        /**
         * Instantiates a new version filter.
         * @param parent wrapped XMLReader
         */
        public VersionFilter(XMLReader parent) {
            super(parent);
        }

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#endElement(String, String, String)
         */
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (inVersionElement > 0) {
                inVersionElement--;
                return;
            }

            super.endElement(uri, localName, qName);
        }

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
         */
        public void characters(char[] ch, int start, int length) throws SAXException {
            // filter content
            if (inVersionElement == 0) {
                super.characters(ch, start, length);
            }
        }

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#startElement(String, String, String, Attributes)
         */
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

            if (inVersionElement > 0) {
                inVersionElement++;
                return;
            }
            if ("sv:property".equals(qName)) { //$NON-NLS-1$
                String attName = atts.getValue("sv:name"); //$NON-NLS-1$
                if (attName != null
                    && ("jcr:predecessors".equals(attName) || "jcr:baseVersion".equals(attName) || "jcr:versionHistory" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .equals(attName))) {
                    inVersionElement++;
                    return;
                }
            }

            super.startElement(uri, localName, qName, atts);
        }

    }
}
