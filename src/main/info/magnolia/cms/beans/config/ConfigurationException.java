package info.magnolia.cms.beans.config;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Date: Jan 17, 2005
 * Time: 11:45:03 AM
 *
 */


public class ConfigurationException extends Exception {



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
            this.root = ((ConfigurationException)root).getRootException();
        } else {
            this.root = root;
        }
    }




    public ConfigurationException(Exception root) {
        this(null,root);
    }



    public Exception getRootException() {
        return this.root;
    }



    public String getMessage() {
        String message = super.getMessage();
        if (this.root == null) {
            return message;
        } else {
            String rootCause = this.root.getMessage();
            if (rootCause == null)
                return message;
            else
                return (message+":"+rootCause);
        }
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
