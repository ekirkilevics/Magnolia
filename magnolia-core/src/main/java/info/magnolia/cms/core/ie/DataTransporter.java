package info.magnolia.cms.core.ie;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.ie.filters.ImportXmlRootFilter;
import info.magnolia.cms.core.ie.filters.VersionFilter;
import info.magnolia.cms.util.ContentUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.jcr.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Iterator;


public class DataTransporter {

    static Logger log = Logger.getLogger(DataTransporter.class.getName());

    /**
     * Perform import.
     *
     * @param repository         selected repository
     * @param basepath           base path in repository
     * @param xmlFile            uploaded file
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     * @param importMode         a valid value for ImportUUIDBehavior
     * @see ImportUUIDBehavior
     */
    public static synchronized void executeImport(String basepath, String repository, Document xmlFile,
                                                  boolean keepVersionHistory, int importMode, boolean saveAfterImport) throws IOException {
        executeImport(
                basepath,
                repository,
                xmlFile.getStream(),
                xmlFile.getFileName(),
                keepVersionHistory,
                importMode,
                saveAfterImport, true);
    }


    /**
     * Perform import
     *
     * @param repository         selected repository
     * @param basepath           base path in repository
     * @param xmlStream          an imput stream reading a
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     * @param importMode         a valid value for ImportUUIDBehavior
     * @throws IOException
     * @see ImportUUIDBehavior
     */
    public static synchronized void executeImport(String basepath, String repository, InputStream xmlStream,
                                                  String fileName, boolean keepVersionHistory, int importMode, boolean saveAfterImport, boolean createBasepathIfNotExist) throws IOException {

        HierarchyManager hr = MgnlContext.getHierarchyManager(repository);
        Workspace ws = hr.getWorkspace();

        if (log.isInfoEnabled()) {
            String message = "Importing content into repository: ["
                    + repository
                    + "] from File: ["
                    + fileName
                    + "] into path:"
                    + basepath;
            log.info(message); //$NON-NLS-1$
        }

        if (!hr.isExist(basepath) && createBasepathIfNotExist) {
            checkAndCreateBasepath(hr, basepath, true);
        }

        Session session = ws.getSession();

        try {
            if (keepVersionHistory) {
                // do not manipulate
                session.importXML(basepath, xmlStream, importMode);
            } else {
                ContentHandler handler = session.getImportContentHandler(basepath, importMode);

                XMLReader filteredReader = new ImportXmlRootFilter(new VersionFilter(XMLReaderFactory
                        .createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName())));
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
                    if (!path.endsWith("/")) {
                        path += "/";
                    }

                    Node dummyRoot = (Node) session.getItem(path + "jcr:root");
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


    /**
     * Check the existence of a node and create if if necessary
     *
     * @param hr        hierarchy manager used for creation
     * @param basepath  the path of the node to create
     * @param recursive recursively create parents if they do not exist
     * @throws IOException if something happens.
     */
    public static void checkAndCreateBasepath(HierarchyManager hr, String basepath, boolean recursive) throws IOException {
        try {
            // If the path already exits do nothing
            if (hr.isExist(basepath))
                return;

            // get parent and name of the new node
            int slash = basepath.lastIndexOf("/");
            if (slash < 0)
                throw new Exception("Node has a strange path" + basepath);
            String parent = basepath.substring(0, slash);
            String name = basepath.substring(slash + 1);

            // check if parent exist
            if (!hr.isExist(parent) && recursive)
                checkAndCreateBasepath(hr, parent, true);

            // create new node from the parent since we know it exists
            Content parentNode = hr.getContent(parent);
            ContentUtil.getOrCreateContent(parentNode, name, ItemType.CONTENTNODE);

            // save and logs
            hr.save();
            log.info("Created node at path:" + basepath);
        }
        catch (Exception e) {
            log.warn("Could not create the basepath:[" + basepath + "]. Failing here.", e);
            throw new IOException(e.getMessage());
        }
    }

}
