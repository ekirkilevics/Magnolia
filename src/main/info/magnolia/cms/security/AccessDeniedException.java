package info.magnolia.cms.security;

import javax.jcr.RepositoryException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Date: Dec 29, 2004
 * Time: 4:16:36 PM
 *
 * @author Sameer Charles
 * @version 2.01
 */



public class AccessDeniedException extends RepositoryException {



    private Exception root;


    public AccessDeniedException() {
        super();
    }




    public AccessDeniedException(String message) {
        super(message);
    }




    public AccessDeniedException(String message, Exception root) {
        super(message);

        if (root instanceof AccessDeniedException) {
            this.root = ((AccessDeniedException)root).getRootException();
        } else {
            this.root = root;
        }
    }




    public AccessDeniedException(Exception root) {
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
