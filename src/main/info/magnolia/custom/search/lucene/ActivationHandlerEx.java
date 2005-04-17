package info.magnolia.custom.search.lucene;

import info.magnolia.cms.exchange.simple.SimpleExchangeServlet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;


/**
 * User: Sameer Charles Date: Mar 5, 2004 Time: 11:19:42 AM
 */
public class ActivationHandlerEx extends SimpleExchangeServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private ServletConfig config;

    public void init() {
        this.config = getServletConfig();
    }

    public void activate(HttpServletRequest request) throws Exception {
        String handle = getOperatedHandle(request);
        super.activate(request);
        try {
            (new BaseTask(this.config)).indexPage(handle);
        }
        catch (Exception e) {
        }
    }

    public void deactivate(HttpServletRequest request) throws Exception {

        try {
            (new BaseTask(this.config)).deleteFromIndex(getOperatedHandle(request));
        }
        catch (Exception e) {
        }
        super.deactivate(request);
    }
}
