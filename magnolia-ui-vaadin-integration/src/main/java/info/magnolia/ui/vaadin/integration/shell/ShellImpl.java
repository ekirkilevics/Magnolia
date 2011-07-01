/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.vaadin.integration.shell;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.Window.Notification;
import info.magnolia.ui.framework.shell.ConfirmationHandler;
import info.magnolia.ui.vaadin.integration.view.MainWindow;
import info.magnolia.ui.vaadin.integration.widget.historian.Historian;


/**
 * Wraps the Vaadin application.
 *
 * @version $Id$
 */
@Singleton
public class ShellImpl extends AbstractShell {

    private static Logger log = LoggerFactory.getLogger(ShellImpl.class);

    private static final long serialVersionUID = 5700621522722171068L;

    private static final String APPLICATION_FRAGMENT_ID = "app";

    private MainWindow mainWindow;
    private UriFragmentUtility uriFragmentUtility = new Historian();

    @Inject
    public ShellImpl(MainWindow mainWindow) {
        super(APPLICATION_FRAGMENT_ID);
        this.mainWindow = mainWindow;
        this.mainWindow.addPermanentComponent(uriFragmentUtility);
    }

    @Override
    public void askForConfirmation(String message, final ConfirmationHandler listener) {
        ConfirmDialog.show(mainWindow.getMainWindow(), message, new ConfirmDialog.Listener() {

            @Override
            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    listener.onConfirm();
                } else {
                    listener.onCancel();
                }
            }
        });
    }

    @Override
    public void showNotification(String message) {
        mainWindow.getMainWindow().showNotification(message);
    }

    @Override
    public void showError(String message, Exception e) {
        log.error(message, e);
        mainWindow.getMainWindow().showNotification(message, e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
    }

    @Override
    public void openWindow(String uri, String windowName) {
        mainWindow.getMainWindow().open(new ExternalResource(uri), windowName);
    }

    @Override
    protected UriFragmentUtility getUriFragmentUtility() {
        return uriFragmentUtility;
    }

}
