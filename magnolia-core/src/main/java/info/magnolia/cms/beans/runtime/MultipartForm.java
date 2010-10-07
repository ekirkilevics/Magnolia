/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.beans.runtime;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;


/**
 * A util class to access file uploads in multipart post requests. Use {@link #getDocument(String)} to access an uploaded file.
 * @author Sameer Charles
 * @version 1.1
 */
public class MultipartForm {

    /**
     * The name of the request attribute containing a MultipartForm instance.
     */
    public static final String REQUEST_ATTRIBUTE_NAME = "multipartform"; //$NON-NLS-1$

    private final Map<String, String[]> parameters;

    private final Map<String, Document> documents;

    private final Map parameterList;

    public MultipartForm() {
        this.parameters = new Hashtable<String, String[]>();
        this.documents = new Hashtable<String, Document>();
        this.parameterList = new Hashtable();
    }

    /**
     * @deprecated since 4.0 - should not be needed anymore since MAGNOLIA-2449 - request parameters should be correctly wrapped.
     */
    public void addParameter(String name, Object value) {
        if (value instanceof String[]) {
            this.parameters.put(name, (String[]) value);
        } else {
            this.parameters.put(name, new String[]{(String) value });
        }
    }

    /**
     * @deprecated since 4.0 - should not be needed anymore since MAGNOLIA-2449 - request parameters should be correctly wrapped.
     */
    public void removeParameter(String name) {
        this.parameters.remove(name);
    }

    /**
     * @deprecated since 4.0 - should not be needed anymore since MAGNOLIA-2449 - request parameters should be correctly wrapped.
     */
    public Map<String, String[]> getParameters() {
        return this.parameters;
    }

    /**
     * @deprecated since 4.0 - should not be needed anymore since MAGNOLIA-2449 - request parameters should be correctly wrapped.
     */
    public String getParameter(String name) {
        try {
            String[] params = this.parameters.get(name);
            if (params != null && params.length > 0) {
                return params[0];
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @deprecated since 4.0 - should not be needed anymore since MAGNOLIA-2449 - request parameters should be correctly wrapped.
     */
    public String[] getParameterValues(String name) {
        try {
            return ((String[]) this.parameterList.get(name));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @deprecated since 4.0 - should not be needed anymore since MAGNOLIA-2449 - request parameters should be correctly wrapped.
     */
    public void addparameterValues(String name, String[] values) {
        this.parameterList.put(name, values);
    }

    public void addDocument(String atomName, String fileName, String type, File file) {
        if (StringUtils.isEmpty(fileName)) {
            return;
        }
        Document document = new Document();
        document.setAtomName(atomName);
        document.setType(type);
        document.setFile(file);
        if (!StringUtils.contains(fileName, ".")) { //$NON-NLS-1$
            document.setExtension(StringUtils.EMPTY);
            document.setFileName(fileName);
        } else {
            document.setExtension(StringUtils.substringAfterLast(fileName, ".")); //$NON-NLS-1$
            document.setFileName(StringUtils.substringBeforeLast(fileName, ".")); //$NON-NLS-1$
        }
        this.documents.put(atomName, document);
    }

    public Document getDocument(String name) {
        return this.documents.get(name);
    }

    public Map<String, Document> getDocuments() {
        return this.documents;
    }

    /**
     * @deprecated since 4.0 - should not be needed anymore since MAGNOLIA-2449 - request parameters should be correctly wrapped.
     */
    public Enumeration<String> getParameterNames() {
        return ((Hashtable<String, String[]>) this.parameters).keys();
    }
}
