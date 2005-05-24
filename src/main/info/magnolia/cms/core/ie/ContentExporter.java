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


import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Hashtable;

/**
 * This class is a main export handler, which could be used to instanciate appropriate export
 * handlers using ID
 *
 * @author Sameer Charles
 * $Id :$
 */
public class ContentExporter {

    /* *
     * Logger.
     */
    private static Logger log = Logger.getLogger(ContentExporter.class);

    /**
     * default export handler
     * */
    public final static String DEFAULT_HANDLER_CLASS = "info.magnolia.cms.core.ie.XmlExport";

    public final static String DEFAULT_HANDLER = "defaultHandler";

    /**
     * all initialized exporters
     * */
    private Map handlers = new Hashtable();

    private static ContentExporter contentExporter = new ContentExporter();

    /**
     * Initialize Content exporter with default handler
     * */
    private ContentExporter () {
        try {
            ExportHandler defaultExporter= (ExportHandler) Class.forName(DEFAULT_HANDLER_CLASS).newInstance();
            this.addExportHandler(DEFAULT_HANDLER, defaultExporter);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static ContentExporter getInstance() {
        return ContentExporter.contentExporter;
    }

    public void addExportHandler(String name, ExportHandler export) {
        if (log.isDebugEnabled()) {
            log.debug("Adding export handler "+export.getClass());
        }
        this.handlers.put(name, export);
    }

    public ExportHandler getExportHandler(String name) {
        if (this.handlers.get(name) == null) {
            log.error("No export handler found with name - "+name);
            log.error("Returning default export handler - "+DEFAULT_HANDLER);
            name = DEFAULT_HANDLER;
        }
        return (ExportHandler) this.handlers.get(name);
    }

}
