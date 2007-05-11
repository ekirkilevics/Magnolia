/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.i18n;

import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Get the current I18NSupport object from this factory. Observation is used.
 * @author philipp
 * @version $Id$
 *
 */
public class I18NSupportFactory extends ObservedManager{

    /**
     * Path to the node in the config repository
     */
    public static final String I18N_SUPPORT_PATH = "/server/i18n/content";

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(I18NSupportFactory.class);

    /**
     * The i18n support object delivered by this factory
     */
    protected I18NSupport i18nSupport;

    public I18NSupportFactory() {
        Content node;
        try {
            node = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG).getContent(I18N_SUPPORT_PATH);
            register(node);
        }
        catch (RepositoryException e) {
            log.error("can't read i18n support configuration", e);
        }
    }

    protected void onRegister(Content node) {
        try {
            this.i18nSupport = (I18NSupport) Content2BeanUtil.toBean(node, true);
        }
        catch (Exception e) {
            log.error("can't instantiate the i18n support object", e);
        }
    }

    protected void onClear() {
        this.i18nSupport = null;
    }

    public static I18NSupportFactory getInstance(){
        return (I18NSupportFactory) FactoryUtil.getSingleton(I18NSupportFactory.class);
    }

    public static I18NSupport getI18nSupport() {
        I18NSupportFactory instance =  getInstance();
        synchronized (instance) {
            return instance.i18nSupport;
        }
    }


}
