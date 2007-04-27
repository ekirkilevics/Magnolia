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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class I18NSupport {

    private static Logger log = LoggerFactory.getLogger(I18NSupport.class);

    protected static final String CURRENT_LANGUAGE = "info.magnolia.cms.i18n.support.current";

    private String fallbackLanguage = "en";

    public static I18NSupport getInstance() {
        return (I18NSupport) FactoryUtil.getSingleton(I18NSupport.class);
    }

    public String getCurrentLanguage(){
       // TODO set the language by the handle filter
        String lang = (String) MgnlContext.getAttribute(CURRENT_LANGUAGE);
        if(lang == null){
            lang = Resource.getSelector();
            setCurrentLanguage(lang);
        }
        return lang;
    }

    public void setCurrentLanguage(String currentLanguage){
        MgnlContext.setAttribute(CURRENT_LANGUAGE, currentLanguage);
    }

    public String getFallbackLanguage() {
        return this.fallbackLanguage;
    }

    public void setFallbackLanguage(String fallbackLanguage) {
        this.fallbackLanguage = fallbackLanguage;
    }

    public String toI18NURI(String uri, String repository){
        if(repository.equals(ContentRepository.WEBSITE)){
            String lang = getCurrentLanguage();
            if(StringUtils.isNotEmpty(lang)){
                String ext = StringUtils.substringAfterLast(uri, ".");
                String path = StringUtils.substringBeforeLast(uri, ".");
                return path + "." + lang + "." + ext;
            }
        }
        return uri;
    }

    public String toURI(String i18nURI){
        // TODO
        throw new NotImplementedException();
    }

    public String languageFromURI(String i18nURI){
        // TODO
        throw new NotImplementedException();
    }

    public NodeData getNodeData(Content node, String name, String lang) throws RepositoryException{
        String nodeDataName = name + "_" + lang;
        if(node.hasNodeData(nodeDataName)){
            return node.getNodeData(nodeDataName);
        }
        return null;
    }

    public NodeData getNodeData(Content node, String name){
        NodeData nd = null;

        // this is not an i18n field
        try {
            if(node.hasNodeData(name)){
                return node.getNodeData(name);
            }

            // test for the current language
            String lang = getCurrentLanguage();
            nd = getNodeData(node, name, lang);
            if(!isEmpty(nd)){
                return nd;
            }

            // fallback
            lang = getFallbackLanguage();

            nd = getNodeData(node, name, lang);
            if(!isEmpty(nd)){
                return nd;
            }
        }
        catch (RepositoryException e) {
            log.error("can't read i18n nodeData " + name + " from node " + node, e);
        }

        // return the not existing node data
        return node.getNodeData(name);
    }

    protected boolean isEmpty(NodeData nd) {
        if(nd != null){
            // TODO use a better way to find out if it is empty
            return StringUtils.isEmpty(NodeDataUtil.getValueString(nd));
        }
        return true;
    }
}
