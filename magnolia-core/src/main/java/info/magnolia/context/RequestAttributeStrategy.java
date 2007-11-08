package info.magnolia.context;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RequestAttributeStrategy implements AttributeStrategy {

    private static final Logger log = LoggerFactory.getLogger(RequestAttributeStrategy.class);

    private static final long serialVersionUID = 222L;

    private HttpServletRequest request;

    public RequestAttributeStrategy() {
    }

    public RequestAttributeStrategy(HttpServletRequest request) {
        setRequest(request);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public Object getAttribute(String name, int scope) {
        switch (scope) {
            case Context.LOCAL_SCOPE:
                Object obj = this.request.getAttribute(name);
                if (obj == null) {
                    obj = this.request.getParameter(name);
                }
                if (obj == null) {
                    // we also expose some of the request properties as attributes
                    if (WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING.equals(name)) {
                        obj = request.getCharacterEncoding();
                    }
                    else if (WebContext.ATTRIBUTE_REQUEST_URI.equals(name)) {
                        obj = request.getRequestURI();
                    }
                }
                return obj;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    return null;
                }
                return httpsession.getAttribute(name);
            case Context.APPLICATION_SCOPE:
                return MgnlContext.getSystemContext().getAttribute(name, Context.APPLICATION_SCOPE);
            default:
                log.error("illegal scope passed");
                return null;
        }
    }

    public Map getAttributes(int scope) {
        Map map = new HashMap();
        Enumeration keysEnum;
        switch (scope) {
            case Context.LOCAL_SCOPE:
                // add parameters
                Enumeration paramEnum = this.request.getParameterNames();
                while (paramEnum.hasMoreElements()) {
                    final String name = (String) paramEnum.nextElement();
                    map.put(name, this.request.getParameter(name));
                }
                // attributes have higher priority
                keysEnum = this.request.getAttributeNames();
                while (keysEnum.hasMoreElements()) {
                    String key = (String) keysEnum.nextElement();
                    Object value = getAttribute(key, scope);
                    map.put(key, value);
                }
                return map;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    return map;
                }
                keysEnum = httpsession.getAttributeNames();
                while (keysEnum.hasMoreElements()) {
                    String key = (String) keysEnum.nextElement();
                    Object value = getAttribute(key, scope);
                    map.put(key, value);
                }
                return map;
            case Context.APPLICATION_SCOPE:
                return MgnlContext.getSystemContext().getAttributes(Context.APPLICATION_SCOPE);
            default:
                log.error("no illegal scope passed");
                return map;
        }
    }

    public void removeAttribute(String name, int scope) {
        switch (scope) {
            case Context.LOCAL_SCOPE:
                this.request.removeAttribute(name);
                break;
            case Context.SESSION_SCOPE:
                HttpSession httpsession = request.getSession(false);
                if (httpsession != null) {
                    httpsession.removeAttribute(name);
                }
                break;
            case Context.APPLICATION_SCOPE:
                MgnlContext.getSystemContext().removeAttribute(name, Context.APPLICATION_SCOPE);
                break;
            default:
                log.error("no illegal scope passed");
        }
    }

    public void setAttribute(String name, Object value, int scope) {
        if (value == null) {
            removeAttribute(name, scope);
            return;
        }

        switch (scope) {
            case Context.LOCAL_SCOPE:
                this.request.setAttribute(name, value);
                break;
            case Context.SESSION_SCOPE:
                if (!(value instanceof Serializable)) {
                    log.warn("Trying to store a non-serializable attribute in session: "
                        + name
                        + ". Object type is "
                        + value.getClass().getName(), new Throwable(
                        "This stacktrace has been added to provide debugging information"));
                    return;
                }

                HttpSession httpsession = request.getSession(false);
                if (httpsession == null) {
                    log
                        .debug(
                            "Session initialized in order to set attribute '{}' to '{}'. You should avoid using session when possible!",
                            name,
                            value);
                    httpsession = request.getSession(true);
                }

                httpsession.setAttribute(name, value);
                break;
            case Context.APPLICATION_SCOPE:
                MgnlContext.getSystemContext().setAttribute(name, value, Context.APPLICATION_SCOPE);
                break;
            default:
                this.request.setAttribute(name, value);
                log.debug("Undefined scope, setting attribute [{}] in request scope", name);
        }
    }

}
