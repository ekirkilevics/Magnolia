package info.magnolia.custom.search.lucene;

import info.magnolia.cms.servlets.SimpleExchange;
import javax.servlet.ServletConfig;


/**
 * User: Sameer Charles Date: Mar 5, 2004 Time: 11:19:42 AM
 */
public class ActivationHandlerEx extends SimpleExchange {

    private ServletConfig config;

    private String handle;

    public void init() {
        this.config = getServletConfig();
    }

    public void activate() throws Exception {
        this.handle = super.getOperatedHandle();
        super.activate();
        try {
            (new BaseTask(this.config)).indexPage(this.handle);
        }
        catch (Exception e) {
        }
    }

    public void deactivate() throws Exception {
        this.handle = super.getOperatedHandle();
        try {
            (new BaseTask(this.config)).deleteFromIndex(this.handle);
        }
        catch (Exception e) {
        }
        super.deactivate();
    }
}
