package info.magnolia.cms.cache;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;


/**
 * A simple ServletOutputStream implementation that duplicates any output to two different output stream.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class MultiplexServletOutputStream extends ServletOutputStream {

    OutputStream stream1;

    OutputStream stream2;

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
}
