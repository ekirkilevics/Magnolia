package info.magnolia.cms.mail;

/**
 * This exception is used to alert the user of a bad usage of the Magnolia Mail API.
 * Date: Apr 3, 2006
 * Time: 10:43:24 AM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailException extends Exception {
    public MailException(Throwable throwable) {
        super(throwable);
    }

    public MailException(String string) {
        super(string);
    }

    public MailException(String string, Throwable throwable) {
        super(string, throwable);
    }
}
