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
package info.magnolia.module.fckeditor.pages;

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.fckeditor.FCKEditorModule;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class RepositoryBrowserPage extends TemplatedMVCHandler {

    private FCKEditorModule moduleConfig;
    private String selectedPath;
    private String selectedRepository;
    private String absoluteURI;
    
    public RepositoryBrowserPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        moduleConfig = (FCKEditorModule) ModuleRegistry.Factory.getInstance().getModuleInstance(FCKEditorModule.MODULE_FCKEDITOR);
    }
    
    public String resolveAbsoluteURI() {
        if (StringUtils.isNotEmpty(getSelectedRepository()) && StringUtils.isNotEmpty(getSelectedPath())) {
            final URI2RepositoryManager manager = URI2RepositoryManager.getInstance();
            final String absoluteURI = manager.getURI(getSelectedRepository(), getSelectedPath());
            setAbsoluteURI(absoluteURI);
        } else {
            setAbsoluteURI(StringUtils.EMPTY);
        }
        
        return "submit";
    }

    public String select() {
        final URI2RepositoryManager manager = URI2RepositoryManager.getInstance();
        
        if (StringUtils.isNotEmpty(absoluteURI)) {
            final String repository = manager.getRepository(absoluteURI);
            final String path = manager.getHandle(absoluteURI);
            
            setSelectedRepository(repository);
            setSelectedPath(path);
        }
        
        return "select";
    }
    
    public Collection getRepositories() {
        return moduleConfig.getBrowsableRepositories();
    }
    
    public String getSelectedPath() {
        return selectedPath;
    }
    
    public void setSelectedPath(String selectedPath) {
        this.selectedPath = selectedPath;
    }

    public String getSelectedRepository() {
        return selectedRepository;
    }
    
    public void setSelectedRepository(String selectedRepository) {
        this.selectedRepository = selectedRepository;
    }
    
    public String getAbsoluteURI() {
        return absoluteURI;
    }
    
    public void setAbsoluteURI(String absoluteURI) {
        this.absoluteURI = absoluteURI;
    }
}
