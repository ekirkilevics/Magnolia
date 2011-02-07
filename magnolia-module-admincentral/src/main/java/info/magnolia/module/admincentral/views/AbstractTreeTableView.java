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
package info.magnolia.module.admincentral.views;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import info.magnolia.module.admincentral.components.MagnoliaBaseComponent;
import info.magnolia.module.admincentral.tree.JcrBrowser;
import info.magnolia.module.admincentral.tree.container.ContainerItemId;

/**
 * A {@link MagnoliaBaseComponent} implementation that hosts a JcrBrowser and provides synchronization with the URI
 * fragment.
 *
 * TODO It might be preferable to let this class look up the TreeDefinition from TreeRegistry instead of JcrBrowser as
 * that would make JcrBrowser more reusable.
 *
 * @author fgrilli
 * @author tmattsson
 */
public abstract class AbstractTreeTableView extends MagnoliaBaseComponent {

    private static Logger log = LoggerFactory.getLogger(AbstractTreeTableView.class);

    private static final long serialVersionUID = -1135599469729524071L;

    private JcrBrowser jcrBrowser;

    public AbstractTreeTableView(String treeName) throws RepositoryException {
        jcrBrowser = new JcrBrowser(treeName);
        setCompositionRoot(jcrBrowser);
        setSizeFull();
        jcrBrowser.addListener(new ItemClickEvent.ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {
                String currentUriFragment = getUriFragmentUtility().getFragment();
                if (StringUtils.isNotEmpty(currentUriFragment)) {
                    String[] tokens = currentUriFragment.split(";");
                    // we already have an item id in the uri fragment, replace it
                    if (tokens.length == 2) {
                        currentUriFragment = tokens[0];
                    }

                    try {
                        Item item = jcrBrowser.getContainer().getJcrItem((ContainerItemId) event.getItemId());

                        currentUriFragment = currentUriFragment + ";" + item.getPath();

                    } catch (RepositoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

                log.info("currentUriFragment is {}", currentUriFragment);
                getUriFragmentUtility().setFragment(currentUriFragment, false);
            }
        });
    }

    /**
     * Selects the tree node to open based on the uri fragment value.
     */
    public void fragmentChanged(FragmentChangedEvent source) {
        final String fragment = source.getUriFragmentUtility().getFragment();
        if (fragment != null) {
            final String[] uriFragmentTokens = fragment.split(";");
            if (uriFragmentTokens.length <= 1)
                return;
            final String path = uriFragmentTokens[1];
            //leaf is expanded and selected
            jcrBrowser.setExpanded(path, true);
        }
    }
}
