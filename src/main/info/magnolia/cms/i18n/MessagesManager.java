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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.log4j.Logger;

/**
 * @author philipp From this class you get the i18n messages. You should pass a
 *         a request, but if you can't the getMessages method will handle it
 *         properly. The get() methods are easy to use.
 */
public class MessagesManager {

	protected static Logger log = Logger.getLogger(Messages.class);

	public static String FALLBACK_LOCALE = "en";

	public static String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages";

	private static String I18N_CONFIG_NAME = "i18n";

	private static String LOCALE_CONFIG_NAME = "language";

	private static String AVAILABLE_LOCALES_CONFIG_NAME = "availableLanguages";

	private static Locale applicationLocale;

	private static Collection availableLocales = new ArrayList();

	private static ServletConfig config;

	/**
	 * Called through the initialization precess
	 */
	public static void init(ServletConfig config) {
		MessagesManager.config = config;
		ServletContext context = config.getServletContext();
		// setting fallback
		context.setAttribute(Config.FMT_FALLBACK_LOCALE + ".application",
				FALLBACK_LOCALE);

		// setting basename
		context.setAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".application",
				MessagesManager.DEFAULT_BASENAME);

		// setting default language (en)
		MessagesManager.setDefaultLocale(FALLBACK_LOCALE);

		// reading the configuration from the repository
		HierarchyManager configHierarchyManager = ContentRepository
				.getHierarchyManager(ContentRepository.CONFIG);
		try {
			log.info("Config : loading i18n configuration - "
					+ I18N_CONFIG_NAME);
			
			Content serverNode = configHierarchyManager.getContentNode("/server");
			
			ContentNode configNode;
			try{
				configNode = serverNode.getContentNode(I18N_CONFIG_NAME);
			}
			catch(javax.jcr.PathNotFoundException  e) {
				configNode = serverNode.createContentNode(I18N_CONFIG_NAME);
				configHierarchyManager.save();
			}
			
			NodeData languageNodeData = configNode.getNodeData(LOCALE_CONFIG_NAME);
			
			if (languageNodeData.getName() == "") {
				languageNodeData = configNode.createNodeData(LOCALE_CONFIG_NAME);
				languageNodeData.setValue(MessagesManager.FALLBACK_LOCALE);
				configHierarchyManager.save();
			}

			MessagesManager.setDefaultLocale(languageNodeData.getString());

			// get the available languages
			ContentNode availableLanguagesContentNode;

			NodeData availableLanguage;
			
			try{
				availableLanguagesContentNode = configNode.getContentNode(AVAILABLE_LOCALES_CONFIG_NAME);
			}
			catch(javax.jcr.PathNotFoundException  e) {
				availableLanguagesContentNode = configNode.createContentNode(AVAILABLE_LOCALES_CONFIG_NAME);
				availableLanguage = availableLanguagesContentNode.createNodeData(MessagesManager.FALLBACK_LOCALE);
				availableLanguage.setValue(MessagesManager.FALLBACK_LOCALE);
				configHierarchyManager.save();
			}
			
			Collection locales = availableLanguagesContentNode.getChildren(ItemType.NT_NODEDATA);
			for (Iterator iter = locales.iterator(); iter.hasNext();) {
				availableLanguage = (NodeData) iter.next();
				if(!availableLanguage.getName().startsWith("jcr:")){
					MessagesManager.availableLocales.add(availableLanguage.getString());
				}
			}

		} catch (RepositoryException re) {
			log.error("Config : Failed to load i18n configuration - "
					+ I18N_CONFIG_NAME);
			log.error(re.getMessage(), re);
		}

	}

	/**
	 * Trys to make a new ContextMessages object. if not possible it creates a
	 * new Messages object.
	 * 
	 * @return Messages
	 */
	public static Messages getMessages(HttpServletRequest req) {
		if (req != null) {
			return new ContextMessages(req);
		} else {
			log.debug("using i18n-messages without a request!");
			return new Messages(MessagesManager.DEFAULT_BASENAME,
					applicationLocale);
		}
	}

	public static Messages getMessages(HttpServletRequest req, String basename) {
		if (req != null) {
			return new ContextMessages(req, basename);
		} else {
			log.debug("using i18n-messages without a request!");
			return new Messages(basename, applicationLocale);
		}
	}

	public static Messages getMessages(HttpServletRequest req, String basename,
			Locale locale) {
		if (req != null) {
			return new ContextMessages(req, basename, locale);
		} else {
			log.debug("using i18n-messages without a request!");
			return new Messages(basename, locale);
		}
	}

	/**
	 * Trys to make a new ContextMessages object. if not possible it creates a
	 * new Messages object.
	 * 
	 * @return Messages
	 */
	public static Messages getMessages(PageContext pc) {
		if (pc != null && pc.getRequest() instanceof HttpServletRequest) {
			return new ContextMessages((HttpServletRequest) pc.getRequest());
		} else {
			log
					.debug("using i18n-messages without a request inside a control!");
			return new Messages(MessagesManager.DEFAULT_BASENAME,
					applicationLocale);
		}
	}

	public static String get(HttpServletRequest req, String key) {
		return getMessages(req).get(key);
	}

	public static String get(HttpServletRequest req, String key, Object[] args) {
		return getMessages(req).get(key, args);
	}

	public static String getWithDefault(HttpServletRequest req, String key,
			String defaultMsg) {
		return getMessages(req).getWithDefault(key, defaultMsg);
	}

	public static String getWithDefault(HttpServletRequest req, String key,
			Object[] args, String defaultMsg) {
		return getMessages(req).getWithDefault(key, args, defaultMsg);
	}

	/**
	 * @return Returns the defaultLocale.
	 */
	public static Locale getDefaultLocale() {
		return applicationLocale;
	}

	/**
	 * @param defaultLocale
	 *            The defaultLocale to set.
	 */
	public static void setDefaultLocale(String defaultLocale) {
		MessagesManager.applicationLocale = new Locale(defaultLocale);
		config.getServletContext().setAttribute(
				Config.FMT_LOCALE + ".application", defaultLocale);
	}

	/**
	 * @return Returns the availableLocals.
	 */
	public static Collection getAvailableLocales() {
		return availableLocales;
	}
}