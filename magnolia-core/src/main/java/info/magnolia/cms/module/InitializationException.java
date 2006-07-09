package info.magnolia.cms.module;

import org.apache.commons.lang.exception.NestableException;


public class InitializationException extends NestableException {

    /**
     * 
     */
    private static final long serialVersionUID = -1300543420918121871L;

    public InitializationException() {
        super();
    }

    public InitializationException(String arg0) {
        super(arg0);
    }

    public InitializationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public InitializationException(Throwable arg0) {
        super(arg0);
    }

}
