package info.magnolia.cms.cache;

import java.io.Serializable;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class CacheableEntry implements Serializable {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    byte[] out;

    private String contentType;

    private String characterEncoding;

    public CacheableEntry(byte[] out) {
        this.out = out;
    }

    /**
     * Getter for <code>out</code>.
     * @return Returns the out.
     */
    public byte[] getOut() {
        return this.out;
    }

    public int getSize() {
        return this.out.length;
    }

    /**
     * Getter for <code>contentType</code>.
     * @return Returns the contentType.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Setter for <code>contentType</code>.
     * @param contentType The contentType to set.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Getter for <code>characterEncoding</code>.
     * @return Returns the characterEncoding.
     */
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    /**
     * Setter for <code>characterEncoding</code>.
     * @param characterEncoding The characterEncoding to set.
     */
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

}
