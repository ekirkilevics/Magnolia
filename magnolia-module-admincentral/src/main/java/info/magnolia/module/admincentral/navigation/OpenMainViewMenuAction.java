/**
 * This file Copyright (c) 2010-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admincentral.navigation;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.AdminCentralApplication;
import info.magnolia.module.admincentral.views.GenericTreeTableView;
import info.magnolia.module.admincentral.views.IFrameView;
import info.magnolia.objectfactory.Classes;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Depending on the menu configuration, opens a tree, an iframe or a custom component in the application main view. The default implementations for the tree and iframe
 * views are, respectively, {@link GenericTreeTableView} and {@link IFrameView}.
 * @see {@link MenuItemConfiguration}
 * @author fgrilli
 *
 */
public class OpenMainViewMenuAction extends AdminCentralAction {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(OpenMainViewMenuAction.class);

    private static final String defaultTreeClassName = GenericTreeTableView.class.getName();

    private static final String defaultIFrameClassName = IFrameView.class.getName();

    public OpenMainViewMenuAction(String label) {
        super(label);
    }

    @Override
    public void handleAction(Object sender, Object target) {
        if(sender == null || !(sender instanceof MenuItemConfiguration)){
            throw new IllegalArgumentException("sender cannot be null and must be of type MenuItemConfiguration");
        }
        if(target == null || !(target instanceof AdminCentralApplication)){
            throw new IllegalArgumentException("target cannot be null and must be of type AdminCentralVaadinApplication");
        }
        final MenuItemConfiguration menuConfig = (MenuItemConfiguration) sender;
        final ComponentContainer main = ((AdminCentralApplication) target).getMainContainer();
        main.removeAllComponents();
        String viewName = null;
        try {
            if(menuConfig.getRepo() != null){
                viewName = menuConfig.getView() != null? menuConfig.getView() : defaultTreeClassName;
                log.info("view class is {}, repository is {} ", viewName, menuConfig.getRepo());
                main.addComponent((Component) Classes.newInstance(viewName, menuConfig.getRepo()));
            } else if(menuConfig.getViewTarget() != null) {
                viewName = menuConfig.getView() != null? menuConfig.getView() : defaultIFrameClassName;
                log.info("view class is {}, viewTarget is {} ", viewName, menuConfig.getViewTarget());
                main.addComponent((Component) Classes.newInstance(viewName, MgnlContext.getContextPath() + menuConfig.getViewTarget()));
            }else {
                if(StringUtils.isEmpty(menuConfig.getView())){
                    throw new IllegalStateException("No valid action configuration was found. You either have to specify a valid view class or a valid repository or view target (e.g. an html page in the application classpath which will be embedded in an iframe )");
                }
                log.info("view class is {}", menuConfig.getView());
                main.addComponent((Component) Classes.newInstance(menuConfig.getView()));
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
 }

