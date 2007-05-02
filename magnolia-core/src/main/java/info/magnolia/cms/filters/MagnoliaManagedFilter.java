package info.magnolia.cms.filters;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 * @deprecated use {@link MagnoliaMainFilter} instead
 */
public class MagnoliaManagedFilter extends MagnoliaMainFilter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MagnoliaManagedFilter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.warn("\n***********\nMagnoliaManagedFilter is deprecated in Magnolia 3.1, please update your web.xml "
            + "and change the class name to info.magnolia.cms.filters.MagnoliaMainFilter\n***********");

        super.init(filterConfig);
    }

}
