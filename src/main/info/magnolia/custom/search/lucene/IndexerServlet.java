package info.magnolia.custom.search.lucene;

import java.util.Timer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * User: Sameer Charles Date: Mar 1, 2004 Time: 3:00:49 PM
 */
public class IndexerServlet extends HttpServlet {

    private static final String TIMER_DELAY = "delay";

    private static final String TIMER_PERIOD = "period";

    private Timer timer;

    private ServletConfig config;

    public void init() throws ServletException {
        this.config = getServletConfig();
        startTimer();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    }

    private int getDelay() {
        return (new Integer(this.config.getInitParameter(TIMER_DELAY))).intValue();
    }

    private int getPeriod() {
        return (new Integer(this.config.getInitParameter(TIMER_PERIOD))).intValue();
    }

    private void startTimer() {
        timer = new Timer(true);
        try {
            timer.schedule(new Task(this.config), getDelay(), getPeriod());
        }
        catch (Exception e) {
        }
    }
}
