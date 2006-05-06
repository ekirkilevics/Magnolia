/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.pages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.module.admininterface.TemplatedMVCHandler;


/**
 * Used to redeploy the files in the classpath (jsps and stuff).
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class DeploymentUtilsPage extends TemplatedMVCHandler {
    
    /**
     * The interval of the deamon
     */
    private int seconds;
    
    class RedeployDeamon extends Thread{
        
        int seconds;
        
        public RedeployDeamon(int seconds) {
            this.seconds = seconds;
            this.setDaemon(true);
        }
        
        /**
         * @see java.lang.Thread#run()
         */
        public void run() {
            try{
                while(true){
                    redeployFiles();
                    sleep(seconds * 1000);
                }
            }
            catch(Exception e){
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
    
    public String redeploy(){
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
    private static void redeployFiles() throws Exception {
        String[] moduleFiles = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {
            public boolean accept(String name) {
                return name.startsWith("/mgnl-files/");
            }
        });
        
        ModuleUtil.installFiles(moduleFiles, "/mgnl-files/");
    }
    
    public String startDeamon(){
        Thread deamon = new RedeployDeamon(this.getSeconds());
        deamon.setDaemon(true);
        deamon.start();
        AlertUtil.setMessage("Deamon started!");
        return this.show();
    }
    
    public String reloadI18nMessages(){
        try {
            MessagesManager.reload();
            AlertUtil.setMessage("Messages reloaded!");
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
