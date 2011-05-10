/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.mail;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.mail.handlers.MgnlMailHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mail module.
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailModule implements ModuleLifecycle {
    private static final Logger log = LoggerFactory.getLogger(MailModule.class);

    /**
     * @deprecated since 5.0, use IoC !
     */
    private static MailModule instance;

    private MgnlMailHandler handler;

    private MgnlMailFactory factory;

    private Map<String, String> smtp = new HashMap<String, String>();

    private List<MailTemplate> templatesConfiguration = new ArrayList<MailTemplate>();


    public MailModule() {
        instance = this;
    }

    /**
     * @deprecated since 5.0, use IoC !
     */
    public static MailModule getInstance() {
        DeprecationUtil.isDeprecated("Use IoC!");
        return instance;
    }


    public Map<String, String> getSmtp() {
        return smtp;
    }

    public void setSmtp(Map<String, String> smtp) {
        this.smtp = smtp;
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {

    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {

    }

    public List<MailTemplate> getTemplatesConfiguration() {
        return templatesConfiguration;
    }

    public void setTemplatesConfiguration(List<MailTemplate> templatesConfiguration) {
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
