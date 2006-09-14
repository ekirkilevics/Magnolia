package info.magnolia.beancoder;

import openwfe.org.jcr.JcrException;
import openwfe.org.jcr.JcrProxy;
import openwfe.org.jcr.Value;

import javax.jcr.RepositoryException;

/**
 * Magnolia Wrapper for value
 */
public class MgnlValue implements Value {

    javax.jcr.Value v;

    public Object getWrappedInstance() throws JcrException {
        return v;
    }

    public MgnlValue(javax.jcr.Value v) {
        this.v = v;
    }

    public String getString() throws JcrException {
        try {
            return v.getString();
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public int getType() {
        return v.getType();
    }


    public long getLong() throws JcrException {
        try {
            return v.getLong();
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage(),e);
        }
    }

    public Object getValue() throws JcrException {
        return JcrProxy.wrapJcrValue(v).getValue();
    }
}
