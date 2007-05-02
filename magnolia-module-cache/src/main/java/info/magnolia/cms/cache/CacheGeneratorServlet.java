package info.magnolia.cms.cache;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 * @deprecated
 */
public class CacheGeneratorServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CacheGeneratorServlet.class);

    /**
     * {@inheritDoc}
     */
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        log.warn("\n***********\nCacheGeneratorServlet has been removed in Magnolia 3.1, please update your web.xml "
            + "and remove the servlet definition and mapping\n***********");

        super.init(config);
    }

}
