package info.magnolia.cms.core.ie;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.ie.filters.ImportXmlRootFilter;
import info.magnolia.cms.core.ie.filters.MagnoliaV2Filter;
import info.magnolia.cms.core.ie.filters.VersionFilter;
import info.magnolia.cms.util.ContentUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Export import data into/from magnolia
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class DataTransporter {

    private static final int INDENT_VALUE = 2;

    static Logger log = LoggerFactory.getLogger(DataTransporter.class.getName());

    final static int bootstrapImportMode = ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;

    public static final String ZIP = ".zip";

    public static final String GZ = ".gz";

    public static final String XML = ".xml";

    public static final String DOT = ".";

    public static final String SLASH = "/";

    public static final String JCR_ROOT = "jcr:root";

    /**
     * Perform import.
     * @param repository selected repository
     * @param basepath base path in repository
     * @param xmlFile uploaded file
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     * @param importMode a valid value for ImportUUIDBehavior
     * @see ImportUUIDBehavior
     */
    public static synchronized void executeImport(String basepath, String repository, Document xmlFile,
        boolean keepVersionHistory, int importMode, boolean saveAfterImport) throws IOException {

        String fileName = xmlFile.getFileName();
        InputStream in = getInputStreamForFile(fileName, xmlFile.getFile());
        executeImport(basepath, repository, in, fileName, keepVersionHistory, importMode, saveAfterImport, true);
    }

    public static synchronized void executeImport(String basepath, String repository, File xmlfile,
        boolean keepVersionHistory, int importMode, boolean saveAfterImport, boolean createBasepathIfNotExist)
        throws IOException {
        String fileName = xmlfile.getName();
        InputStream in = getInputStreamForFile(fileName, xmlfile);
        executeImport(
            basepath,
            repository,
            in,
            fileName,
            keepVersionHistory,
            importMode,
            saveAfterImport,
            createBasepathIfNotExist);
    }

    private static InputStream getInputStreamForFile(String fileName, File xmlfile) throws IOException {
        InputStream in;
        // looks like the zip one is buggy. It throws exception when trying to use it
        if (fileName.endsWith(ZIP))
            in = new ZipInputStream((new FileInputStream(xmlfile)));
        else if (fileName.endsWith(GZ))
            in = new GZIPInputStream((new FileInputStream(xmlfile)));
        else
            // if(fileName.endsWith(".xml"))
            in = new FileInputStream(xmlfile);
        return in;
    }

    public static void executeBootstrapImport(File xmlfile, String repository) throws IOException {
        String filenameWithoutExt = StringUtils.substringBeforeLast(xmlfile.getName(), DOT);
        if (filenameWithoutExt.endsWith(XML))
            // if file ends in .xml.gz or .xml.zip
            // need to keep the .xml to be able to view it after decompression
            filenameWithoutExt = StringUtils.substringBeforeLast(xmlfile.getName(), DOT);
        String pathName = StringUtils.substringAfter(StringUtils.substringBeforeLast(filenameWithoutExt, DOT), DOT);
        pathName = SLASH + StringUtils.replace(pathName, DOT, SLASH);
        DataTransporter.executeImport(pathName, repository, xmlfile, false, bootstrapImportMode, true, true);
    }

    /**
     * Perform import
     * @param repository selected repository
     * @param basepath base path in repository
     * @param xmlStream an imput stream reading a
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     * @param importMode a valid value for ImportUUIDBehavior
     * @throws IOException
     * @see ImportUUIDBehavior
     */
    public static synchronized void executeImport(String basepath, String repository, InputStream xmlStream,
        String fileName, boolean keepVersionHistory, int importMode, boolean saveAfterImport,
        boolean createBasepathIfNotExist) throws IOException {

        HierarchyManager hr = MgnlContext.getHierarchyManager(repository);
        Workspace ws = hr.getWorkspace();

        if(log.isDebugEnabled())
        log.debug("Importing content into repository: [{}] from File: [{}] into path: {}", //$NON-NLS-1$
            new Object[]{repository, fileName, basepath});

        if (!hr.isExist(basepath) && createBasepathIfNotExist) {
            try {
                ContentUtil.createPath(hr, basepath, ItemType.CONTENT);
            }
            catch (RepositoryException e) {
                log.error("can't create path [{}]", basepath);
            }
        }

        Session session = ws.getSession();

        try {
            if (keepVersionHistory) {
                // do not manipulate
                session.importXML(basepath, xmlStream, importMode);
            }
            else {
                ContentHandler handler = session.getImportContentHandler(basepath, importMode);
                // 
                XMLReader filteredReader = new ImportXmlRootFilter(new VersionFilter(new MagnoliaV2Filter(
                    XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName()))));
                filteredReader.setContentHandler(handler);

                // import it
                try {
                    filteredReader.parse(new InputSource(xmlStream));
                }
                finally {
                    IOUtils.closeQuietly(xmlStream);
                }

                if (((ImportXmlRootFilter) filteredReader).rootNodeFound) {
                    String path = basepath;
                    if (!path.endsWith(SLASH)) {
                        path += SLASH;
                    }

                    Node dummyRoot = (Node) session.getItem(path + JCR_ROOT);
                    for (Iterator iter = dummyRoot.getNodes(); iter.hasNext();) {
                        Node child = (Node) iter.next();
                        // move childs to real root

                        if (session.itemExists(path + child.getName())) {
                            session.getItem(path + child.getName()).remove();
                        }

                        session.move(child.getPath(), path + child.getName());
                    }
                    // delete the dummy node
                    dummyRoot.remove();
                }
            }
        }
        catch (Exception e) {
            throw new NestableRuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(xmlStream);
        }

        try {
            if (saveAfterImport)
                session.save();
        }
        catch (RepositoryException e) {
            log.error(MessageFormat.format(
                "Unable to save changes to the [{0}] repository due to a {1} Exception: {2}.", //$NON-NLS-1$
                new Object[]{repository, e.getClass().getName(), e.getMessage()}), e);
            throw new IOException(e.getMessage());
        }
    }

    public static void executeExport(OutputStream baseOutputStream, boolean keepVersionHistory, boolean format,
        Session session, String basepath, String repository, String ext) throws IOException {
        OutputStream outputStream = baseOutputStream;
        if (ext.endsWith(ZIP))
            outputStream = new ZipOutputStream(baseOutputStream);
        else if (ext.endsWith(GZ))
            outputStream = new GZIPOutputStream(baseOutputStream);

        try {
            if (keepVersionHistory) {
                // use exportSystemView in order to preserve property types
                // http://issues.apache.org/jira/browse/JCR-115
                if (!format) {
                    session.exportSystemView(basepath, outputStream, false, false);
                }
                else {
                    parseAndFormat(outputStream, null, repository, basepath, format, session);
                }
            }
            else {
                // use XMLSerializer and a SAXFilter in order to rewrite the
                // file
                XMLReader reader = new VersionFilter(XMLReaderFactory
                    .createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName()));
                parseAndFormat(outputStream, reader, repository, basepath, format, session);
            }
        }
        catch (Exception e) {
            throw new NestableRuntimeException(e);
        }

        // finish the stream properly if zip stream
        // this is not done by the IOUtils
        if (outputStream instanceof DeflaterOutputStream)
            ((DeflaterOutputStream) outputStream).finish();

        baseOutputStream.flush();
        IOUtils.closeQuietly(baseOutputStream);
    }

    /**
     * This export the content of the repository, and format it if necessary
     * @param stream the stream to write the content to
     * @param reader the reader to use to parse the xml content (so that we can perform filtering), if null instanciate
     * a default one
     * @param repository the repository to export
     * @param basepath the basepath in the repository
     * @param format should we format the xml
     * @param session the session to use to export the data from the repository
     * @throws Exception if anything goes wrong ...
     */
    public static void parseAndFormat(OutputStream stream, XMLReader reader, String repository, String basepath,
        boolean format, Session session) throws Exception {

        if (reader == null)
            reader = XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());

        // write to a temp file and then re-read it to remove version history
        File tempFile = File.createTempFile("export-" + repository + session.getUserID(), "xml"); //$NON-NLS-1$ //$NON-NLS-2$
        OutputStream fileStream = new FileOutputStream(tempFile);

        try {
            session.exportSystemView(basepath, fileStream, false, false);
        }
        finally {
            IOUtils.closeQuietly(fileStream);
        }

        InputStream fileInputStream = new FileInputStream(tempFile);

        OutputFormat forma = new OutputFormat();
        if (format) {
            forma.setIndenting(true);
            forma.setIndent(INDENT_VALUE);
        }
        reader.setContentHandler(new XMLSerializer(stream, forma));
        reader.parse(new InputSource(fileInputStream));

        IOUtils.closeQuietly(fileInputStream);

        if (!tempFile.delete()) {
            log.warn("Could not delete temporary export file {}", tempFile.getAbsolutePath()); //$NON-NLS-1$
        }
    }

}
