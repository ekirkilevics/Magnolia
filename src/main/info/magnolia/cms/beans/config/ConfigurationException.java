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
package info.magnolia.cms.beans.config;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 *
 */
public class ConfigurationException extends Exception {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private Exception root;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Exception root) {
        super(message);
        if (root instanceof ConfigurationException) {
            this.root = ((ConfigurationException) root).getRootException();
        }
        else {
            this.root = root;
        }
    }

    public ConfigurationException(Exception root) {
        this(null, root);
    }

    public Exception getRootException() {
        return this.root;
    }

    public String getMessage() {
        String message = super.getMessage();
        if (this.root == null) {
            return message;
        }
        String rootCause = this.root.getMessage();
        if (rootCause == null) {
            return message;
        }
        return (message + ":" + rootCause);
    }

    public void printStackTrace() {
        synchronized (System.err) {
            super.printStackTrace();
            if (this.root != null) {
                this.root.printStackTrace();
            }
        }
    }

    public void printStackTrace(PrintStream ps) {
        synchronized (ps) {
            super.printStackTrace(ps);
            if (this.root != null) {
                this.root.printStackTrace(ps);
            }
        }
    }

    public void printStackTrace(PrintWriter pw) {
        synchronized (pw) {
            super.printStackTrace(pw);
            if (this.root != null) {
                this.root.printStackTrace(pw);
            }
        }
    }
}
