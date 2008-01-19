package info.magnolia.cms.filters;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;


/**
 * <p>
 * Filter that sets cache headers, allowing or dening cache at client-side. By default the filter adds the
 * "Cache-Control: public" and expire directives to resources so that everything can be cached by the browser. Setting
 * the <code>nocache</code> property to <code>true</code> has the opposite effect, forcing browsers to avoid
 * caching.
 * </p>
 * <p>
 * The following example shows how to configure the filter so that static resources (images, css, js) gets cached by the
 * browser, and deny cache for html pages.
 * </p>
 *
 * <pre>
 * + server
 *    + filters
 *      + ...
 *      + headers-cache
 *        - class                  info.magnolia.cms.filters.CacheHeadersFilter
 *        - expirationMinutes      1440 <em>(default)</em>
 *        + bypasses
 *          + extensions
 *            - class              info.magnolia.voting.voters.ExtensionVoter
 *            - allow              gif,jpg,png,swf,css,js
 *            - not                true
 *      + headers-nocache
 *        - class                  info.magnolia.cms.filters.CacheHeadersFilter
 *        + bypasses
 *          + extensions
 *            - class              info.magnolia.voting.voters.ExtensionVoter
 *            - allow              html
 *            - not                true
 * </pre>
 *
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class CacheHeadersFilter extends AbstractMgnlFilter {

    /**
     * Number of minutes this item must be kept in cache.
     */
    private long expirationMinutes = 1440;

    /**
     * Cache should be avoided for filtered items.
     */
    private boolean nocache;

    private FastDateFormat formatter = FastDateFormat.getInstance("EEE, d MMM yyyy HH:mm:ss zzz", TimeZone
        .getTimeZone("GMT"), Locale.ENGLISH);

    /**
     * Sets the expirationMinutes.
     * @param expirationMinutes the expirationMinutes to set
     */
    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    /**
     * Sets the nocache.
     * @param nocache the nocache to set
     */
    public void setNocache(boolean nocache) {
        this.nocache = nocache;
    }

    /**
     * {@inheritDoc}
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (nocache) {
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Expires", "Fri, 30 Oct 1998 14:19:41 GMT");
        }
        else {
            response.setHeader("Pragma", "");
            response.setHeader("Cache-Control", "max-age=" + expirationMinutes * 60 + ", public");
            response.setHeader("Expires", formatter.format(new Date(System.currentTimeMillis()
                + expirationMinutes
                * 60000)));
        }

        chain.doFilter(request, response);
    }
}