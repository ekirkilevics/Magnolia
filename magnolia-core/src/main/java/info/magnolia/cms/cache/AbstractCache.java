package info.magnolia.cms.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Abstract implementation of <code>Cache</code> containing common utility methods.
 * @author Andreas Brenk
 * @since 06.02.2006
 */
public abstract class AbstractCache implements Cache {

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Stream from the <code>InputStream</code> to the <code>OutputStream</code> in 8K blocks. Flushes and closes
     * the <code>OutputStream</code>!
     */
    protected void stream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read = 0;
        while ((read = in.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }

        out.flush();
        out.close();
    }

}
