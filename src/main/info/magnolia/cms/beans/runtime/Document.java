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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * User: sameercharles Date: Apr 28, 2003 Time: 11:20:59 AM
 * @author Sameer Charles
 * @version 1.1
 */
public class Document {

    private String atomName;

    private String fileName;

    private String extension;

    private String type;

    private java.io.File file;

    private FileInputStream inputStream;

    /**
     * package private constructor
     */
    Document() {
    }

    /**
     *
     */
    public void setAtomName(String name) {
        this.atomName = name;
    }

    /**
     *
     */
    public String getAtomName() {
        return this.atomName;
    }

    /**
     *
     */
    public void setFileName(String name) {
        this.fileName = name;
    }

    /**
     *
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     *
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     */
    public String getType() {
        return this.type;
    }

    /**
     *
     */
    public void setFile(java.io.File in) {
        this.file = in;
    }

    /**
     *
     */
    public void setExtention(String ext) {
        this.extension = ext;
    }

    /**
     *
     */
    public String getExtension() {
        return this.extension;
    }

    public long getLength() {
        return this.file.length();
    }

    public InputStream getStream() {
        try {
            return (this.inputStream = (new FileInputStream(this.file)));
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }

    public void delete() {
        try {
            this.inputStream.close();
        }
        catch (IOException e) {
        }
        this.file.delete();
    }
}
