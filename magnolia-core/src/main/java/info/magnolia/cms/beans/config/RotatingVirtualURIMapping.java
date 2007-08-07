package info.magnolia.cms.beans.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;


/**
 * <p>
 * An extension of {@link RegexpVirtualURIMapping} that allows a rotation between different destination urls. In order
 * to rotate <code>toURI</code> must contain the <code>*</code> that will be replaced by a random number between
 * <code>start</code> (default is <code>1</code>) and <code>end</code> (defaults is <code>3</code>).
 * </p>
 * <p>
 * An additional property <code>padding</code> can specify the left 0 padding for numbers (defaults is <code>2</code>).
 * So for example a destination url like <code>forward:/banner/image_*.jpg</code> will randomly forward the request to
 * <code>/banner/image_01.jpg</code>, <code>/banner/image_02.jpg</code> or <code>/banner/image_03.jpg</code>
 * </p>
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class RotatingVirtualURIMapping extends RegexpVirtualURIMapping {

    /**
     * Placeholder that will be replaced by a random number.
     */
    private static final String RANDOM_PLACEHOLDER = "*";

    /**
     * Lower bound for the generated random number.
     */
    private int start = 1;

    /**
     * Upper bound for the generated random number.
     */
    private int end = 3;

    /**
     * Left padding (using 0s).
     */
    private int padding = 2;

    /**
     * Sets the start.
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Sets the end.
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Sets the padding.
     * @param padding the padding to set
     */
    public void setPadding(int padding) {
        this.padding = padding;
    }

    /**
     * {@inheritDoc}
     */
    public MappingResult mapURI(String uri) {
        // delegate the initial processing to RegexpVirtualURIMapping
        MappingResult mr = super.mapURI(uri);

        int randomNumber = RandomUtils.nextInt(end - start) + 1;
        String randomAsString = StringUtils.leftPad(Integer.toString(randomNumber), padding, '0');

        mr.setToURI(StringUtils.replace(mr.getToURI(), RANDOM_PLACEHOLDER, randomAsString));

        return mr;
    }
}
