/*
 * Created on 04.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package info.magnolia.cms.servlets;

import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * Default implementation of a MVCHandler. Calls the command through reflection.
 *
 * @author Philipp Bracher
 * @version $Id: AdminInterfaceServlet.java 661 2005-05-03 14:10:45Z philipp $
 **/
public abstract class MVCServletHandlerImpl implements MVCServletHandler {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(MVCServletHandlerImpl.class);

    private String name;
    
    protected HttpServletRequest request;

    protected HttpServletResponse response;

    protected static final String VIEW_ERROR = "error";
    

    protected MVCServletHandlerImpl(String name, HttpServletRequest request, HttpServletResponse response) {
        super();
        this.name = name;
        this.request = request;
        this.response = response;
    }
    

    /* (non-Javadoc)
     * @see info.magnolia.cms.servlets.MVCServletHandler#getName()
     */
    public String getName() {
        return name;
    }


    /**
     * Call the method through reflection
     * 
     * @param command
     * @return the name of the view to show (used in renderHtml)
     */
    public String execute(String command) {
        String view =VIEW_ERROR; 
        Method method;
        try {
            method = this.getClass().getMethod(command, new Class[]{});
            //method.setAccessible(true);
            view = (String) method.invoke(this, new Object[]{});
        }
        catch (Exception e) {
            log.error("can't call command: " + command, e);
        }
        return view;
    }

}
