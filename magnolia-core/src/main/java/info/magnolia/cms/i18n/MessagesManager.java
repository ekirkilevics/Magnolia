package info.magnolia.cms.i18n;

import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;

import java.util.Collection;
import java.util.Locale;


/**
 * From this class you get the i18n messages. You should pass a a request, but if you can't the getMessages method will
 * handle it properly. The get() methods are easy to use.
 * @author philipp
 * @author molaschi
 */
public abstract class MessagesManager {

    /**
     * Use this locale if no other provided.
     */
    public static final String FALLBACK_LOCALE = "en"; //$NON-NLS-1$

    /**
     * Use this basename if no other is provided.
     */
    public static final String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages"; //$NON-NLS-1$

    /**
     * The node name where the configuration for i18n is stored.
     */
    public static final String I18N_CONFIG_PATH = "/server/i18n/system"; //$NON-NLS-1$

    /**
     * The name of the property to store the current system language.
     */
    public static final String FALLBACK_NODEDATA = "fallbackLanguage"; //$NON-NLS-1$

    /**
     * Under this node all the available languages are stored. They are showed in the user dialog.
     */
    public static final String LANGUAGES_NODE_NAME = "languages"; //$NON-NLS-1$

    public static MessagesManager getInstance() {
        return (MessagesManager) FactoryUtil.getSingleton(MessagesManager.class);
    }

    public static Messages getMessages() {
        return getMessages(null, getCurrentLocale());
    }

    public static Messages getMessages(String basename) {
        return getMessages(basename, getCurrentLocale());
    }

    public static Messages getMessages(Locale locale) {
        return getMessages(null, locale);
    }

    public static Messages getMessages(String basename, Locale locale) {
        return getInstance().getMessagesInternal(basename, locale);
    }

    public static String get(String key) {
        return getMessages().get(key);
    }

    /**
     * Get a message with parameters inside: the value {0} must be a number.
     * @param key key to find
     * @param args replacement strings
     * @return message
     */
    public static String get(String key, Object[] args) {
        return getMessages().get(key, args);
    }

    /**
     * Use a default string.
     * @param key key to find
     * @param defaultMsg default message
     * @return message
     */
    public static String getWithDefault(String key, String defaultMsg) {
        return getMessages().getWithDefault(key, defaultMsg);
    }

    /**
     * Get a message with parameters inside: the value {0} must be a number. Use a default message.
     * @param key key to find
     * @param args replacement strings
     * @param defaultMsg default message
     * @return message
     */
    public static String getWithDefault(String key, Object[] args, String defaultMsg) {
        return getMessages().getWithDefault(key, args, defaultMsg);
    }

    private static Locale getCurrentLocale() {
        return MgnlContext.getInstance().getLocale();
    }

    public abstract void init();

    public abstract Collection getAvailableLocales();

    public abstract Locale getDefaultLocale();

    public abstract Messages getMessagesInternal(String basename, Locale locale);

    public abstract void reload();

    abstract void setDefaultBasename(String basename);
}
