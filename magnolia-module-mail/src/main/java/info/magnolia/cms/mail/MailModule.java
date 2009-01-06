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
package info.magnolia.cms.mail;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.magnolia.cms.mail.handlers.MgnlMailHandler;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailModule implements ModuleLifecycle {

    private static final Logger log = LoggerFactory.getLogger(MailModule.class);

    private static MailModule instance;

    private MgnlMailHandler handler;

    private MgnlMailFactory factory;

    private Map smtp = new HashMap();

    private List templatesConfiguration = new ArrayList();


    public MailModule() {
        instance = this;
    }

    public static MailModule getInstance() {
        return instance;
    }


    public Map getSmtp() {
        return smtp;
    }

    public void setSmtp(Map smtp) {
        this.smtp = smtp;
    }

    public void start(ModuleLifecycleContext moduleLifecycleContext) {

    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {

    }

    public List getTemplatesConfiguration() {
        return templatesConfiguration;
    }

    public void setTemplatesConfiguration(List templatesConfiguration) {
        this.templatesConfiguration = templatesConfiguration;
    }

    public void addTemplatesConfiguration(MailTemplate mailTemplate) {
        this.templatesConfiguration.add(mailTemplate);
    }

    public MgnlMailHandler getHandler() {
        return handler;
    }

    public void setHandler(MgnlMailHandler handler) {
        this.handler = handler;
    }

    public MgnlMailFactory getFactory() {
        return factory;
    }

    public void setFactory(MgnlMailFactory factory) {
        this.factory = factory;
    }



}
