/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Used to redeploy the files in the classpath (jsps and stuff).
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DeploymentUtilsPage extends TemplatedMVCHandler {

    /**
     * The interval of the deamon
     */
    private int seconds;

    class RedeployDeamon extends Thread {

        int seconds;

        public RedeployDeamon(int seconds) {
            this.seconds = seconds;
            this.setDaemon(true);
        }

        /**
         * @see java.lang.Thread#run()
         */
        public void run() {
            try {
                while (true) {
                    redeployFiles();
                    sleep(seconds * 1000);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param name
     * @param request
     * @param response
     */
    public DeploymentUtilsPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public String redeploy() {
        try {
            redeployFiles();
            AlertUtil.setMessage("Redeployed");
        }
        catch (Exception e) {
            AlertUtil.setMessage("Can't redeploy files", e);
        }
        return this.show();
    }

    /**
     * @throws Exception
     */
    protected static void redeployFiles() throws Exception {
        String[] moduleFiles = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

            public boolean accept(String name) {
                return name.startsWith("/mgnl-files/");
            }
        });

        ModuleUtil.installFiles(moduleFiles, "/mgnl-files/");
    }

    public String startDeamon() {
        Thread deamon = new RedeployDeamon(this.getSeconds());
        deamon.setDaemon(true);
        deamon.start();
        AlertUtil.setMessage("Deamon started!");
        return this.show();
    }

    public String reloadI18nMessages() {
        try {
            MessagesManager.reload();
            AlertUtil.setMessage("AbstractMessagesImpl reloaded!");
        }
        catch (Exception e) {
            e.printStackTrace();
            AlertUtil.setMessage("Can't reload", e);
        }

        return this.show();
    }

    /**
     * @return Returns the seconds.
     */
    public int getSeconds() {
        return this.seconds;
    }

    /**
     * @param seconds The seconds to set.
     */
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

}
