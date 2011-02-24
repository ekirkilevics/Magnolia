/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.admincentral.shell;

import info.magnolia.module.vaadin.gwt.ui.Historian;
import info.magnolia.module.vaadin.shell.AbstractShell;
import info.magnolia.module.vaadin.shell.ConfirmationHandler;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.Application;
import com.vaadin.ui.UriFragmentUtility;


/**
 * Wraps the Vaadin application.
 */
public class ShellImpl extends AbstractShell {

    private static final String APPLICATION_FRAGMENT_ID = "app";

    private Application application;

    protected UriFragmentUtility uriFragmentUtility = new Historian();

    public ShellImpl(Application application) {
        super(APPLICATION_FRAGMENT_ID);
        this.application = application;
        application.getMainWindow().addComponent(uriFragmentUtility);
    }

    public void askForConfirmation(String message, final ConfirmationHandler listener) {
        ConfirmDialog.show(application.getMainWindow(), message, new ConfirmDialog.Listener() {

            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    listener.onConfirm();
                }
                else{
                    listener.onCancel();
                }
            }
        });
    }

    public void showNotification(String message) {
        application.getMainWindow().showNotification(message);
    }

    protected UriFragmentUtility getUriFragmentUtility() {
        return uriFragmentUtility;
    }

}
