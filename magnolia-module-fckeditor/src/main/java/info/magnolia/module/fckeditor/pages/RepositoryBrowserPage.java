/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
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
