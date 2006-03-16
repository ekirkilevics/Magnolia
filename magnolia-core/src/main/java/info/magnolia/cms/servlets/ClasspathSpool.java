package info.magnolia.cms.servlets;

import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple spool servlet that load resources from the classpath. A simple rule for accessible resources: only files
 * into a <code>mgnl-resources</code> folder will be loaded by this servlet (corresponding to the mapped url
 * <code>/.resources/*</code>. This servlet should be used for authoring-only resources, like rich editor images and
 * scripts. It's not suggested for public website resources. Content length and last modification date are not set on
 * files returned from the classpath.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class ClasspathSpool extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Spool.class);

    /**
     * All static resource requests are handled here.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException for error in accessing the resource or the servlet output stream
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream in = null;
        
        // this method caches content if possible and checks the magnolia.debug property to avoid
        // caching during the developement process
        try{
            in = ClasspathResourcesUtil.getStream("/mgnl-resources" + request.getPathInfo());
        }
        catch(IOException e){
            // in is null
        }

        if (in == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            ServletOutputStream os = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = in.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
            os.flush();
            IOUtils.closeQuietly(os);
        }
        catch (IOException e) {
            // only log at debug level
            // tomcat usually throws a ClientAbortException anytime the user stop loading the page
            log.debug("Unable to spool resource due to a {} exception", e.getClass().getName()); //$NON-NLS-1$
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        finally {
            IOUtils.closeQuietly(in);
        }
    }

}
