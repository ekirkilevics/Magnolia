/*
 * Created on Apr 6, 2005
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;


/**
 * @author philipp From this class you get the i18n messages. You should pass a a request, but if you can't the
 * getMessages method will handle it properly. The get() methods are easy to use.
 */
public class MessagesManager {

    protected static Logger log = Logger.getLogger(Messages.class);

    /**
     * Trys to make a new ContextMessages object. if not possible it creates a new Messages object.
     * @return Messages
     */
    public static Messages getMessages(HttpServletRequest req) {
        if (req != null) {
            return new ContextMessages(req);
        }
        else {
            log.debug("using i18n-messages without a request!");
            return new Messages(Messages.DEFAULT_BASENAME);
        }
    }

    public static Messages getMessages(HttpServletRequest req, String basename) {
        if (req != null) {
            return new ContextMessages(req, basename);
        }
        else {
            log.debug("using i18n-messages without a request!");
            return new Messages(basename);
        }
    }

    public static Messages getMessages(HttpServletRequest req, String basename, Locale locale) {
        if (req != null) {
            return new ContextMessages(req, basename, locale);
        }
        else {
            log.debug("using i18n-messages without a request!");
            return new Messages(basename, locale);
        }
    }

    /**
     * Trys to make a new ContextMessages object. if not possible it creates a new Messages object.
     * @return Messages
     */
    public static Messages getMessages(PageContext pc) {
        if (pc != null && pc.getRequest() instanceof HttpServletRequest) {
            return new ContextMessages((HttpServletRequest) pc.getRequest());
        }
        else {
            log.debug("using i18n-messages without a request inside a control!");
            return new Messages(Messages.DEFAULT_BASENAME);
        }
    }

    public static String get(HttpServletRequest req, String key) {
        return getMessages(req).get(key);
    }

    public static String get(HttpServletRequest req, String key, Object[] args) {
        return getMessages(req).get(key, args);
    }

    public static String getWithDefault(HttpServletRequest req, String key, String defaultMsg) {
        return getMessages(req).getWithDefault(key, defaultMsg);
    }

    public static String getWithDefault(HttpServletRequest req, String key, Object[] args, String defaultMsg) {
        return getMessages(req).getWithDefault(key, args, defaultMsg);
    }

}