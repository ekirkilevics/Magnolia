/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.runtime;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * User: sameercharles Date: Apr 28, 2003 Time: 11:20:59 AM
 * @author Sameer Charles
 * @version 1.1
 */
public class Document implements Serializable {

    /**
     * Logger
     */
    public static Logger log = LoggerFactory.getLogger(Document.class);

    /**
     * request parameter name.
     */
    private String atomName;

    /**
     * File name, without extension.
     */
    private String fileName;

    /**
     * File extension.
     */
    private String extension;

    /**
     * Mime type.
     */
    private String type;

    /**
     * Underlining file.
     */
    private java.io.File file;

    /**
     * A reference to the file input stream.
     */
    transient private FileInputStream inputStream;

    /**
     * package private constructor
     */
    Document() {
    }

    /**
     * Used to create a document pased on a existing file.
     * @param file
     * @param type
     */
    public Document(java.io.File file, String type) {
        String fileName = file.getName();
        this.setFile(file);
        this.setType(type);
        this.setExtention(StringUtils.substringAfterLast(fileName, "."));
        this.setFileName(StringUtils.substringBeforeLast(fileName, "."));
    }

    /**
     * Sets the parameter name
     * @param name parameter name
     */
    public void setAtomName(String name) {
        this.atomName = name;
    }

    /**
     * Returns the parameter name
     * @return parameter name
     */
    public String getAtomName() {
        return this.atomName;
    }

    /**
     * Sets the file name without extension.
     * @param name file name without extension
     */
    public void setFileName(String name) {
        this.fileName = name;
    }

    /**
     * Returns the file name without extension.
     * @return file name
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Returns the full file name with extension (if existing).
     * @return file name with extension
     */
    public String getFileNameWithExtension() {
        if (StringUtils.isEmpty(this.extension)) {
            return this.fileName;
        }

        return this.fileName + "." + this.extension; //$NON-NLS-1$
    }

    /**
     * Sets the mime type for this file
     * @param type mime type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the mime type for this file
     * @return mime type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets a reference to the uploaded file.
     * @param in file
     */
    public void setFile(java.io.File in) {
        this.file = in;
    }

    /**
     * Sets the file extension.
     * @param ext file extension
     */
    public void setExtention(String ext) {
        this.extension = ext;
    }

    /**
     * Returns the file extension.
     * @return file extension
     */
    public String getExtension() {
        return this.extension;
    }

    /**
     * Returns the file length in bytes
     * @return file length
     */
    public long getLength() {
        return this.file.length();
    }

    /**
     * Returns a stream from the uploaded file. Note that subsequent invocation will always return a reference to the
     * same input stream.
     * @return stream from the uploaded file
     */
    public InputStream getStream() {
        try {
            return (this.inputStream = (new FileInputStream(this.file)));
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Returns the uploaded file. Users should normally use getStream, but getFile() can be used when you need to
     * repeatedly access the file. <strong>The obtained file should never be deleted by the caller</strong>
     * @return a reference to the uploaded file.
     */
    public java.io.File getFile() {
        return this.file;
    }

    /**
     * Delete the file, taking care of closing an open input stream
     */
    public void delete() {
        IOUtils.closeQuietly(inputStream);
        this.file.delete();
    }
}
