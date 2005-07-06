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
package info.magnolia.cms.beans.runtime;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public class MultipartForm {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(MultipartForm.class);

    /**
     * The name of the request attribute containing a MultipartForm instance.
     */
    public static final String REQUEST_ATTRIBUTE_NAME = "multipartform";

    private Map parameters;

    private Map documents;

    private Map parameterList;

    public MultipartForm() {
        this.parameters = new Hashtable();
        this.documents = new Hashtable();
        this.parameterList = new Hashtable();
    }

    public void addParameter(String name, Object value) {
        this.parameters.put(name, value);
    }

    public void removeParameter(String name) {
        this.parameters.remove(name);
    }

    public Map getParameters() {
        return this.parameters;
    }

    public String getParameter(String name) {
        try {
            return (String) this.parameters.get(name);
        }
        catch (Exception e) {
            return null;
        }
    }

    public String[] getParameterValues(String name) {
        try {
            return ((String[]) this.parameterList.get(name));
        }
        catch (Exception e) {
            return null;
        }
    }

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
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            document.setExtention(StringUtils.EMPTY);
            document.setFileName(fileName);
        }
        else {
            document.setExtention(fileName.substring(lastIndexOfDot + 1));
            document.setFileName(fileName.substring(0, lastIndexOfDot));
        }
        this.documents.put(atomName, document);
    }

    public Document getDocument(String name) {
        return (Document) this.documents.get(name);
    }

    public Map getDocuments() {
        return this.documents;
    }
}
