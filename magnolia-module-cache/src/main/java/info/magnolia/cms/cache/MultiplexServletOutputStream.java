package info.magnolia.cms.cache;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * A simple ServletOutputStream implementation that duplicates any output to two different output stream. Very similar
 * to TeeOutputStream from commons-io.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class MultiplexServletOutputStream extends ServletOutputStream {

    private final OutputStream stream1;

    private final OutputStream stream2;

    public MultiplexServletOutputStream(OutputStream stream1, OutputStream stream2) {
        this.stream1 = stream1;
        this.stream2 = stream2;
    }

    public void write(int value) throws IOException {
        stream1.write(value);
        stream2.write(value);
    }

    public void write(byte[] value) throws IOException {
        stream1.write(value);
        stream2.write(value);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        stream1.write(b, off, len);
        stream2.write(b, off, len);
    }

    public void flush() throws IOException {
        stream1.flush();
        stream2.flush();
    }

    public void close() throws IOException {
        try {
            stream1.close();
        }
        finally {
            stream2.close();
        }
    }
}
