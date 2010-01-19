/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.module.mail.handlers.MgnlMailHandler;
import info.magnolia.module.mail.templates.MgnlEmail;
import info.magnolia.module.mail.templates.impl.SimpleEmail;
import info.magnolia.objectfactory.Classes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This reads the repository to know what kind of email to instanciate
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlMailFactory {

    private static Logger log = LoggerFactory.getLogger(MgnlMailFactory.class);

    private Map renderers = new HashMap();


    /**
     * Use getInstance to get the current used instance
     */
    public MgnlMailFactory() {

    }


    public MgnlMailHandler getEmailHandler() {
        return ((MailModule) MailModule.getInstance()).getHandler();
    }


    /**
     * Creates email with no attachments
     */
    public MgnlEmail getEmail(Map params) {
        return getEmail(params, null);
    }

    /**
     * Creates email with no attachments
     */
    public MgnlEmail getEmailFromType(Map params, String type) {

        return getEmailFromType(params, type, null);
    }

    public MgnlEmail getEmailFromType(Map params, String type,
            String contentType, List attachments) {
        Map newParams = new HashMap();
        newParams.putAll(params);
        if(!StringUtils.isEmpty(type)) {
            newParams.put(MailTemplate.MAIL_TYPE, type);
        }
        if(!StringUtils.isEmpty(contentType)) {
            newParams.put(MailTemplate.MAIL_CONTENT_TYPE, contentType);
        }
        return getEmail(newParams, attachments);
    }

    /**
     * Creates email with attachments
     */
    public MgnlEmail getEmailFromType(Map params, String type, List attachments) {
        return getEmailFromType(params, type, null, attachments);
    }

    /**
     * Creates email with attachments
     */
    public MgnlEmail getEmail(Map params, List attachments) {
        MailTemplate template = new MailTemplate();
        return getEmail(params, attachments, template);
    }

    /**
     * Creates email using predefined template
     */
    public MgnlEmail getEmailFromTemplate(String id, List attachments, Map params) throws Exception {
        MailTemplate template = getTemplate(id);
        return getEmail(params, attachments, template);
    }

    /**
     * Creates email using predefined template with no attachments
     */
    public MgnlEmail getEmailFromTemplate(String id, Map params) throws Exception {
        return getEmailFromTemplate(id, null, params);
    }

    protected MgnlEmail getEmail(Map params, List attachments, MailTemplate template) {
        template.setValues(params, attachments);

        MgnlEmail mail;
        try {
            mail = getEmailFromType(template);

        } catch (Exception e) {
            log.error("Couln't instantiate email type: " + template.getType());
            return null;
        }

        return mail;
    }

    protected MgnlEmail getEmailFromType(MailTemplate template) throws Exception {
        MgnlEmail mail = null;
        if(renderers.containsKey(template.getType().toLowerCase())){
            String value = (String) renderers.get(template.getType().toLowerCase());
            mail = Classes.quietNewInstance(value, template);
        }
        else {
            mail = new SimpleEmail(template);
        }

        //set all mail parameters
        if(!StringUtils.isEmpty(template.getFrom())) {
            mail.setFrom(template.getFrom());
        }
        if(!StringUtils.isEmpty(template.getTo())) {
            mail.setToList(template.getTo());
        }
        if(!StringUtils.isEmpty(template.getCc())) {
            mail.setCcList(template.getCc());
        }
        if(!StringUtils.isEmpty(template.getBcc())) {
            mail.setBccList(template.getBcc());
        }
        if(!StringUtils.isEmpty(template.getReplyTo())) {
            mail.setReplyToList(template.getReplyTo());
        }
        if(!StringUtils.isEmpty(template.getSubject())) {
            mail.setSubject(template.getSubject());
        }

        return mail;
    }


    protected MailTemplate getTemplate(String templateName) throws Exception {

        Iterator iterator = MailModule.getInstance().getTemplatesConfiguration().iterator();
        MailTemplate mailTemplate;
        while(iterator.hasNext()) {
            mailTemplate = (MailTemplate)iterator.next();
            if(StringUtils.equals(mailTemplate.getName(), templateName)){
                return (MailTemplate) BeanUtils.cloneBean(mailTemplate);
            }
        }
        return null;
    }


    public Map getRenderers() {
        return renderers;
    }

    public void setRenderers(Map renderers) {
        this.renderers = renderers;
    }

}
