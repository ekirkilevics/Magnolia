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

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
 * @version $Revision: $ ($Author: $)
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
    protected static void bootstrapRepositories(String bootdir) {

        System.out.println("\n-----------------------------------------------------------------\n");
        System.out.println("Trying to initialize repositories from [" + bootdir + "]");
        System.out.println("\n-----------------------------------------------------------------\n");

        log.info("Trying to initialize repositories from [" + bootdir + "]");

        for (int j = 0; j < ContentRepository.ALL_REPOSITORIES.length; j++) {
            String repository = ContentRepository.ALL_REPOSITORIES[j];

            File xmldir = new File(bootdir, repository);

            if (!xmldir.exists() || !xmldir.isDirectory()) {
                log.info("Directory [" + repository + "] not found for repository [" + repository + "], skipping...");
                continue;
            }

            File[] files = xmldir.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });

            if (files.length == 0) {
                log.info("No xml files found in directory [" + repository + "], skipping...");
                continue;
            }

            log.info("Trying to import content from " + files.length + " files...");

            HierarchyManager hr = ContentRepository.getHierarchyManager(repository);
            Session session = hr.getWorkspace().getSession();

            for (int k = 0; k < files.length; k++) {
                File xmlfile = files[k];

                InputStream filteredStream = null;

                try {
                    filteredStream = filterVersionsFromFile(xmlfile);

                    log.info("Importing content from " + xmlfile.getName());
                    session.importXML("/", filteredStream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
                }
                catch (Exception e) {
                    log.error("Unable to load content from "
                        + xmlfile.getName()
                        + " due to a "
                        + e.getClass().getName()
                        + " Exception: "
                        + e.getMessage()
                        + ". Will try to continue.", e);
                }
                finally {
                    try {
                        filteredStream.close();
                    }
                    catch (IOException e) {
                        // ignore
                    }
                }

            }

            log.info("Saving changes to [" + repository + "]");

            try {
                session.save();
            }
            catch (RepositoryException e) {
                log.error("Unable to save changes to the ["
                    + repository
                    + "] repository due to a "
                    + e.getClass().getName()
                    + " Exception: "
                    + e.getMessage()
                    + ". Will try to continue.", e);
                continue;
            }

            log.info("Repository [" + repository + "] has been initialized.");

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
    protected static InputStream filterVersionsFromFile(File xmlfile) throws IOException, SAXException {

        // create a temporary file and save the trimmed xml
        File strippedFile = File.createTempFile("import", "xml");
        strippedFile.deleteOnExit();

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
            stream.close();
        }

        // return the filtered file as an input stream
        return new FileInputStream(strippedFile);
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
            if ("sv:property".equals(qName)) {
                String attName = atts.getValue("sv:name");
                if (attName != null
                    && ("jcr:predecessors".equals(attName) || "jcr:baseVersion".equals(attName) || "jcr:versionHistory"
                        .equals(attName))) {
                    inVersionElement++;
                    return;
                }
            }

            super.startElement(uri, localName, qName, atts);
        }

    }
}
