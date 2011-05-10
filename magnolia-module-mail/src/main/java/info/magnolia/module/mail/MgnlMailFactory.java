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

import info.magnolia.module.mail.handlers.MgnlMailHandler;
import info.magnolia.module.mail.templates.MailAttachment;
import info.magnolia.module.mail.templates.MgnlEmail;
import info.magnolia.module.mail.templates.impl.SimpleEmail;
import info.magnolia.objectfactory.Classes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;


/**
 * This reads the repository to know what kind of email to instantiate.
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlMailFactory {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MgnlMailFactory.class);

    private Map<String, String> renderers = new HashMap<String, String>();


    /**
     * Use getInstance to get the current used instance.
     */
    public MgnlMailFactory() {

    }

    public MgnlMailHandler getEmailHandler() {
        return MailModule.getInstance().getHandler();
    }


    /**
     * Creates email with no attachments.
     */
    public MgnlEmail getEmail(Map<String, Object> params) {
        return getEmail(params, null);
    }

    /**
     * Creates email with no attachments.
     */
    public MgnlEmail getEmailFromType(Map<String, Object> params, String type) {

        return getEmailFromType(params, type, null);
    }

    public MgnlEmail getEmailFromType(Map<String, Object> params, String type, String contentType, List<MailAttachment> attachments) {
        Map<String, Object> newParams = new HashMap<String, Object>();
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
     * Creates email with attachments.
     */
    public MgnlEmail getEmailFromType(Map<String, Object> params, String type, List<MailAttachment> attachments) {
        return getEmailFromType(params, type, null, attachments);
    }

    /**
     * Creates email with attachments.
     */
    public MgnlEmail getEmail(Map<String, Object> params, List<MailAttachment> attachments) {
        MailTemplate template = new MailTemplate();
        return getEmail(params, attachments, template);
    }

    /**
     * Creates email using predefined template.
     */
    public MgnlEmail getEmailFromTemplate(String id, List<MailAttachment> attachments, Map<String, Object> params) throws Exception {
        MailTemplate template = getTemplate(id);
        if (template == null) {
            log.error("Template {} can't be found", id);
            return null;
        }
        return getEmail(params, attachments, template);
    }

    /**
     * Creates email using predefined template with no attachments.
     */
    public MgnlEmail getEmailFromTemplate(String id, Map<String, Object> params) throws Exception {
        return getEmailFromTemplate(id, null, params);
    }

    protected MgnlEmail getEmail(Map<String, Object> params, List<MailAttachment> attachments, MailTemplate template) {
        template.setValues(params, attachments);

        try {
            return getEmailFromType(template);

        } catch (Exception e) {
            log.error("Couln't instantiate email type: " + template.getType(), e);
            return null;
        }
    }

    protected MgnlEmail getEmailFromType(MailTemplate template) throws Exception {
        final MgnlEmail mail;
        if(renderers.containsKey(template.getType().toLowerCase())){
            String rendererClass = renderers.get(template.getType().toLowerCase());
            mail = Classes.quietNewInstance(rendererClass, template);
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
        final List<MailTemplate> configuration = MailModule.getInstance().getTemplatesConfiguration();
        for (MailTemplate mailTemplate : configuration) {
            if (StringUtils.equals(mailTemplate.getName(), templateName)) {
                return (MailTemplate) BeanUtils.cloneBean(mailTemplate);
            }
        }
        return null;
    }


    public Map<String, String> getRenderers() {
        return renderers;
    }

    public void setRenderers(Map<String, String> renderers) {
        this.renderers = renderers;
    }

}
