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
package info.magnolia.cms.core.ie;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * Date: May 24, 2005 Time: 4:42:21 PM
 * @author Sameer Charles $Id :$
 */
public class ContentImporter {

    /*******************************************************************************************************************
     * Logger.
     */
    private static Logger log = Logger.getLogger(ContentImporter.class);

    /**
     * default import handler
     */
    public static final String DEFAULT_HANDLER_CLASS = "info.magnolia.cms.core.ie.XmlImport";

    public static final String DEFAULT_HANDLER = "defaultHandler";

    /**
     * all initialized importers
     */
    private Map handlers = new Hashtable();

    private static ContentImporter contentImporter = new ContentImporter();

    /**
     * Initialize Content importer with default handler
     */
    private ContentImporter() {
        try {
            ImportHandler defaultImporter = (ImportHandler) Class.forName(DEFAULT_HANDLER_CLASS).newInstance();
            this.addImportHandler(DEFAULT_HANDLER, defaultImporter);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static ContentImporter getInstance() {
        return ContentImporter.contentImporter;
    }

    public void addImportHandler(String name, ImportHandler importhandler) {
        if (log.isDebugEnabled()) {
            log.debug("Adding import handler " + importhandler.getClass());
        }
        this.handlers.put(name, importhandler);
    }

    public ImportHandler getImportHandler(String name) {
        if (this.handlers.get(name) == null) {
            log.error("No import handler found with name - " + name);
            log.error("Returning default import handler - " + DEFAULT_HANDLER);
            name = DEFAULT_HANDLER;
        }
        return (ImportHandler) this.handlers.get(name);
    }

}
