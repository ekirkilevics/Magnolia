/*
 * Created on Mar 16, 2005
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

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.taglibs.standard.resources.Resources;


/**
 * @author philipp bracher This calss is doing the same thing as javax.servlet.jsp.jastl.fmt.LocalSupport. LocalSupport
 * is using the PageContext which is not avaiable in Servlets, but the most classes do know the request-object and can
 * deliver it to this class. With this class you get the string stricly under the same rules as JSTL does. The most of
 * the code is the same as in the jstl classes. If you are able to pass a PageContext you will use
 * javax.servlet.jsp.jastl.fmt.LocalSupport.
 */

public class ContextMessages extends Messages {

    static final String REQUEST_CHAR_SET = "javax.servlet.jsp.jstl.fmt.request.charset";

    // from setLocal tag
    private static final char HYPHEN = '-';

    private static final char UNDERSCORE = '_';

    private static final Locale EMPTY_LOCALE = new Locale("", "");

    // from Config (this suffix are used to inhibit overwriting in other contextes
    private static final String REQUEST_SCOPE_SUFFIX = ".request";

    private static final String SESSION_SCOPE_SUFFIX = ".session";

    private static final String APPLICATION_SCOPE_SUFFIX = ".application";

    private LocalizationContext loc;

    private HttpServletRequest req;

    private String basename;

    /**
     * Get the bundle and the local from the context
     * @param request
     */
    public ContextMessages(HttpServletRequest req) {
        this.req = req;
        loc = getLocalizationContext(req);
    }

    public ContextMessages(HttpServletRequest req, String basename) {
        this.req = req;
        this.basename = basename;
        loc = getLocalizationContext(req, basename);
    }

    public ContextMessages(HttpServletRequest req, String basename, Locale locale) {
        this.req = req;
        this.basename = basename;
        loc = getLocalizationContext(req, basename, locale);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.i18n.Messages#setBasename(java.lang.String)
     */
    public void setBasename(String basename) {
        this.basename = basename;
        loc = getLocalizationContext(req, basename, loc.getLocale());
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.i18n.Messages#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale) {
        if (basename == null) {
            throw new IllegalArgumentException("you can use setLocale only with a setted basename");
        }
        loc = getLocalizationContext(req, basename, locale);
    }

    public Locale getLocale() {
        return loc.getLocale();
    }

    public ResourceBundle getBundle() {
        return loc.getResourceBundle();
    }
    
    /**
     * Trys to make a new ContextMessages object. if not possible it creates a new Messages object.
     * @return Messages
     */
    public static Messages getInstanceSafely(HttpServletRequest req) {
        if (req != null) {
            return new ContextMessages(req);
        }
        else {
            log.debug("using i18n-messages without a request inside a control!");
            return new Messages(Messages.DEFAULT_BASENAME);
        }
    }
    
    /**
     * Trys to make a new ContextMessages object. if not possible it creates a new Messages object.
     * @return Messages
     */
    public static Messages getInstanceSafely(PageContext pc) {
        if (pc != null && pc.getRequest() instanceof HttpServletRequest ) {
            return new ContextMessages((HttpServletRequest) pc.getRequest());
        }
        else {
            log.debug("using i18n-messages without a request inside a control!");
            return new Messages(Messages.DEFAULT_BASENAME);
        }
    }
    
    public static String get(HttpServletRequest req, String key) {
        return ContextMessages.getInstanceSafely(req).get(key);
    }

    public static String get(HttpServletRequest req, String key, String basename) {
        return ContextMessages.getInstanceSafely(req).get(key, basename);
    }
    
    public static String get(HttpServletRequest req, String key, Object[] args) {
        return ContextMessages.getInstanceSafely(req).get(key, args);
    }

    public static String get(HttpServletRequest req, String key, String basename, Object[] args) {
        return ContextMessages.getInstanceSafely(req).get(key, basename, args);
    }

    public static String getWithDefault(HttpServletRequest req, String key, String defaultMsg) {
        return ContextMessages.getInstanceSafely(req).getWithDefault(key, defaultMsg);
    }

    public static String getWithDefault(HttpServletRequest req, String key, String basename, String defaultMsg) {
        return ContextMessages.getInstanceSafely(req).getWithDefault(key, basename, defaultMsg);
    }

    public static String getWithDefault(HttpServletRequest req, String key, Object[] args, String defaultMsg) {
        return ContextMessages.getInstanceSafely(req).getWithDefault(key, args, defaultMsg);
    }

    public static String getWithDefault(HttpServletRequest req, String key, String basename, Object[] args, String defaultMsg) {
        return ContextMessages.getInstanceSafely(req).getWithDefault(key, basename, args, defaultMsg);
    }

    /**
     * Gets the default I18N localization context.
     * @param req Request in which to look up the default I18N localization context
     */
    private static LocalizationContext getLocalizationContext(HttpServletRequest req) {
        LocalizationContext locCtxt = null;

        Object obj = find(req, Config.FMT_LOCALIZATION_CONTEXT);
        if (obj == null) {
            return null;
        }

        if (obj instanceof LocalizationContext) {
            locCtxt = (LocalizationContext) obj;
        }
        else {
            // localization context is a bundle basename
            locCtxt = getLocalizationContext(req, (String) obj);
        }

        return locCtxt;
    }

    private static LocalizationContext getLocalizationContext(HttpServletRequest req, String basename) {
        return getLocalizationContext(req, basename, null);
    }

    /**
     * Gets the resource bundle with the given base name, whose locale is determined as follows: Check if a match exists
     * between the ordered set of preferred locales and the available locales, for the given base name. The set of
     * preferred locales consists of a single locale (if the <tt>javax.servlet.jsp.jstl.fmt.locale</tt> configuration
     * setting is present) or is equal to the client's preferred locales determined from the client's browser settings.
     * <p>
     * If no match was found in the previous step, check if a match exists between the fallback locale (given by the
     * <tt>javax.servlet.jsp.jstl.fmt.fallbackLocale</tt> configuration setting) and the available locales, for the
     * given base name.
     * @param req Request in which the resource bundle with the given base name is requested
     * @param basename Resource bundle base name
     * @return Localization context containing the resource bundle with the given base name and the locale that led to
     * the resource bundle match, or the empty localization context if no resource bundle match was found
     */
    private static LocalizationContext getLocalizationContext(HttpServletRequest req, String basename, Locale locale) {
        LocalizationContext locCtxt = null;
        ResourceBundle bundle = null;

        if ((basename == null) || basename.equals("")) {
            return new LocalizationContext();
        }

        // Try preferred locales
        Locale pref;

        if (locale != null) {
            pref = locale;
        }
        else {
            pref = getLocale(req, Config.FMT_LOCALE);
        }

        if (pref != null) {
            // Preferred locale is application-based
            bundle = findMatch(basename, pref);
            if (bundle != null) {
                locCtxt = new LocalizationContext(bundle, pref);
            }
        }
        else {
            // Preferred locales are browser-based
            locCtxt = findMatch(req, basename);
        }

        if (locCtxt == null) {
            // No match found with preferred locales, try using fallback locale
            pref = getLocale(req, Config.FMT_FALLBACK_LOCALE);
            if (pref != null) {
                bundle = findMatch(basename, pref);
                if (bundle != null) {
                    locCtxt = new LocalizationContext(bundle, pref);
                }
            }
        }

        if (locCtxt == null) {
            // try using the root resource bundle with the given basename
            try {
                bundle = ResourceBundle.getBundle(basename, EMPTY_LOCALE, Thread
                    .currentThread()
                    .getContextClassLoader());
                if (bundle != null) {
                    locCtxt = new LocalizationContext(bundle, null);
                }
            }
            catch (MissingResourceException mre) {
                // do nothing
            }
        }

        if (locCtxt == null) {
            locCtxt = new LocalizationContext();
        }

        return locCtxt;
    }

    /**
     * Determines the client's preferred locales from the request, and compares each of the locales (in order of
     * preference) against the available locales in order to determine the best matching locale.
     * @param request the reqest in which the resource bundle with the given base name is requested
     * @param basename the resource bundle's base name
     * @return the localization context containing the resource bundle with the given base name and best matching
     * locale, or <tt> null </tt> if no resource bundle match was found
     */
    private static LocalizationContext findMatch(HttpServletRequest req, String basename) {
        LocalizationContext locCtxt = null;

        // Determine locale from client's browser settings.
        for (Enumeration en = req.getLocales(); en.hasMoreElements();) {
            /*
             * If client request doesn't provide an Accept-Language header, the returned locale Enumeration contains the
             * runtime's default locale, so it always contains at least one element.
             */
            Locale pref = (Locale) en.nextElement();
            ResourceBundle match = findMatch(basename, pref);
            if (match != null) {
                locCtxt = new LocalizationContext(match, pref);
                break;
            }
        }

        return locCtxt;
    }

    /**
     * Gets the resource bundle with the given base name and preferred locale. This method calls
     * java.util.ResourceBundle.getBundle(), but ignores its return value unless its locale represents an exact or
     * language match with the given preferred locale.
     * @param basename the resource bundle base name
     * @param pref the preferred locale
     * @return the requested resource bundle, or <tt> null </tt> if no resource bundle with the given base name exists
     * or if there is no exact- or language-match between the preferred locale and the locale of the bundle returned by
     * java.util.ResourceBundle.getBundle().
     */
    private static ResourceBundle findMatch(String basename, Locale pref) {
        ResourceBundle match = null;

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(basename, pref, Thread
                .currentThread()
                .getContextClassLoader());
            Locale avail = bundle.getLocale();
            if (pref.equals(avail)) {
                // Exact match
                match = bundle;
            }
            else {
                /*
                 * We have to make sure that the match we got is for the specified locale. The way
                 * ResourceBundle.getBundle() works, if a match is not found with (1) the specified locale, it tries to
                 * match with (2) the current default locale as returned by Locale.getDefault() or (3) the root resource
                 * bundle (basename). We must ignore any match that could have worked with (2) or (3). So if an exact
                 * match is not found, we make the following extra tests: - avail locale must be equal to preferred
                 * locale - avail country must be empty or equal to preferred country (the equality match might have
                 * failed on the variant)
                 */
                if (pref.getLanguage().equals(avail.getLanguage())
                    && ("".equals(avail.getCountry()) || pref.getCountry().equals(avail.getCountry()))) {
                    /*
                     * Language match. By making sure the available locale does not have a country and matches the
                     * preferred locale's language, we rule out "matches" based on the container's default locale. For
                     * example, if the preferred locale is "en-US", the container's default locale is "en-UK", and there
                     * is a resource bundle (with the requested base name) available for "en-UK",
                     * ResourceBundle.getBundle() will return it, but even though its language matches that of the
                     * preferred locale, we must ignore it, because matches based on the container's default locale are
                     * not portable across different containers with different default locales.
                     */
                    match = bundle;
                }
            }
        }
        catch (MissingResourceException mre) {
        }

        return match;
    }

    /**
     * Returns the locale specified by the named scoped attribute or context configuration parameter.
     * <p>
     * The named scoped attribute is searched in the page, request, session (if valid), and application scope(s) (in
     * this order). If no such attribute exists in any of the scopes, the locale is taken from the named context
     * configuration parameter.
     * @param pageContext the page in which to search for the named scoped attribute or context configuration parameter
     * @param name the name of the scoped attribute or context configuration parameter
     * @return the locale specified by the named scoped attribute or context configuration parameter, or <tt> null </tt>
     * if no scoped attribute or configuration parameter with the given name exists
     */
    private static Locale getLocale(HttpServletRequest req, String name) {
        Locale loc = null;

        Object obj = find(req, name);
        if (obj != null) {
            if (obj instanceof Locale) {
                loc = (Locale) obj;
            }
            else {
                loc = parseLocale((String) obj);
            }
        }

        return loc;
    }

    /**
     * See parseLocale(String, String) for details.
     */
    private static Locale parseLocale(String locale) {
        return parseLocale(locale, null);
    }

    /**
     * Parses the given locale string into its language and (optionally) country components, and returns the
     * corresponding <tt>java.util.Locale</tt> object. If the given locale string is null or empty, the runtime's
     * default locale is returned.
     * @param locale the locale string to parse
     * @param variant the variant
     * @return <tt>java.util.Locale</tt> object corresponding to the given locale string, or the runtime's default
     * locale if the locale string is null or empty
     * @throws IllegalArgumentException if the given locale does not have a language component or has an empty country
     * component
     */
    private static Locale parseLocale(String locale, String variant) {

        Locale ret = null;
        String language = locale;
        String country = null;
        int index = -1;

        if (((index = locale.indexOf(HYPHEN)) > -1) || ((index = locale.indexOf(UNDERSCORE)) > -1)) {
            language = locale.substring(0, index);
            country = locale.substring(index + 1);
        }

        if ((language == null) || (language.length() == 0)) {
            throw new IllegalArgumentException(Resources.getMessage("LOCALE_NO_LANGUAGE"));
        }

        if (country == null) {
            if (variant != null)
                ret = new Locale(language, "", variant);
            else
                ret = new Locale(language, "");
        }
        else if (country.length() > 0) {
            if (variant != null)
                ret = new Locale(language, country, variant);
            else
                ret = new Locale(language, country);
        }
        else {
            throw new IllegalArgumentException(Resources.getMessage("LOCALE_EMPTY_COUNTRY"));
        }

        return ret;
    }

    /**
     * Finds the value associated with a specific configuration setting identified by its context initialization
     * parameter name.
     * <p>
     * For each of the JSP scopes (page, request, session, application), get the value of the configuration variable
     * identified by <tt>name</tt> using method <tt>get()</tt>. Return as soon as a non-null value is found. If no
     * value is found, get the value of the context initialization parameter identified by <tt>name</tt>.
     * @param request Request context in which the configuration setting is to be searched
     * @param name Context initialization parameter name of the configuration setting
     * @return The <tt>java.lang.Object</tt> associated with the configuration setting identified by <tt>name</tt>,
     * or null if it is not defined.
     */
    private static Object find(HttpServletRequest req, String name) {
        Object ret = get(req, name, PageContext.REQUEST_SCOPE);
        if (ret == null) {
            if (req.getSession() != null) {
                // check session only if a session is present
                ret = get(req, name, PageContext.SESSION_SCOPE);
            }
            if (ret == null) {
                ret = get(req, name, PageContext.APPLICATION_SCOPE);
                if (ret == null) {
                    ret = req.getSession().getServletContext().getInitParameter(name);
                }
            }
        }
        return ret;
    }

    /**
     * Looks up a configuration variable in the given scope.
     * <p>
     * The lookup of configuration variables is performed as if each scope had its own name space, that is, the same
     * configuration variable name in one scope does not replace one stored in a different scope.
     * @param req Request context in which the configuration variable is to be looked up
     * @param name Configuration variable name
     * @param scope Scope in which the configuration variable is to be looked up
     * @return The <tt>java.lang.Object</tt> associated with the configuration variable, or null if it is not defined.
     */
    private static Object get(HttpServletRequest req, String name, int scope) {
        switch (scope) {
            case PageContext.REQUEST_SCOPE :
                return req.getAttribute(name + REQUEST_SCOPE_SUFFIX);
            case PageContext.SESSION_SCOPE :
                return get(req.getSession(), name);
            case PageContext.APPLICATION_SCOPE :
                return req.getSession().getServletContext().getAttribute(name + APPLICATION_SCOPE_SUFFIX);
            default :
                throw new IllegalArgumentException("unknown scope");
        }
    }

    /**
     * Looks up a configuration variable in the "session" scope.
     * <p>
     * The lookup of configuration variables is performed as if each scope had its own name space, that is, the same
     * configuration variable name in one scope does not replace one stored in a different scope.
     * @param session Session object in which the configuration variable is to be looked up
     * @param name Configuration variable name
     * @return The <tt>java.lang.Object</tt> associated with the configuration variable, or null if it is not defined,
     * if session is null, or if the session is invalidated.
     */
    private static Object get(HttpSession session, String name) {
        Object ret = null;
        if (session != null) {
            try {
                ret = session.getAttribute(name + SESSION_SCOPE_SUFFIX);
            }
            catch (IllegalStateException ex) {
            } // when session is invalidated
        }
        return ret;
    }

}