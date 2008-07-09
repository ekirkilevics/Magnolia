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
package info.magnolia.cms.core.ie;

import info.magnolia.cms.util.ClassUtil;

import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is a main export handler, which could be used to instanciate appropriate export handlers using ID
 * @author Sameer Charles $Id :$
 *
 * @deprecated deprecated since 3.6 but wasn't used before - MAGNOLIA-405
 */
public final class ContentExporter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentExporter.class);

    /**
     * default export handler
     */
    public static final String DEFAULT_HANDLER_CLASS = "info.magnolia.cms.core.ie.XmlExport"; //$NON-NLS-1$

    public static final String DEFAULT_HANDLER = "defaultHandler"; //$NON-NLS-1$

    /**
     * all initialized exporters
     */
    private Map handlers = new Hashtable();

    private static ContentExporter contentExporter = new ContentExporter();

    /**
     * Initialize Content exporter with default handler
     */
    private ContentExporter() {
        try {
            ExportHandler defaultExporter = (ExportHandler) ClassUtil.newInstance(DEFAULT_HANDLER_CLASS);
            this.addExportHandler(DEFAULT_HANDLER, defaultExporter);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static ContentExporter getInstance() {
        return ContentExporter.contentExporter;
    }

    public void addExportHandler(String name, ExportHandler export) {
        if (log.isDebugEnabled()) {
            log.debug("Adding export handler " + export.getClass()); //$NON-NLS-1$
        }
        this.handlers.put(name, export);
    }

    public ExportHandler getExportHandler(String name) {
        if (this.handlers.get(name) == null) {
            log.error("No export handler found with name - " + name); //$NON-NLS-1$
            log.error("Returning default export handler - " + DEFAULT_HANDLER); //$NON-NLS-1$
            return (ExportHandler) this.handlers.get(DEFAULT_HANDLER);
        }
        return (ExportHandler) this.handlers.get(name);
    }

}
