package info.magnolia.module.workflow.flows;

import org.apache.commons.lang.exception.NestableException;


public class FlowDefinionException extends NestableException {

    public FlowDefinionException() {
        super();
    }

    public FlowDefinionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public FlowDefinionException(String msg) {
        super(msg);
    }

    public FlowDefinionException(Throwable cause) {
        super(cause);
    }
}
