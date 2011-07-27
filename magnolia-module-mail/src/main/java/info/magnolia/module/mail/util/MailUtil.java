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
package info.magnolia.module.mail.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.mail.MailConstants;
import info.magnolia.module.mail.handlers.LoggingLevel;
import info.magnolia.module.mail.templates.MailAttachment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides static utility methods to work with emails in Magnolia.
 *
 */
public class MailUtil {

    public static Logger log = LoggerFactory.getLogger(MailUtil.class);
    /**
     * Transforms a string name=value\r\nname=value.. into a hashmap
     */
    public static Map<String, String> convertToMap(String parameters) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        ByteArrayInputStream string = new ByteArrayInputStream(parameters.getBytes());
        Properties props = new Properties();
        props.load(string);

        Iterator iter = props.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            map.put(key, (String) props.get(key));
        }

        return map;
    }

    /**
     * Creates a list with the documents uploaded in the form.
     * @return
     */
    public static List<MailAttachment> createAttachmentList() {
        List<MailAttachment> attachments = new ArrayList<MailAttachment>();
        try {
            // get any possible attachment
            if(MgnlContext.getPostedForm() != null) {
                MultipartForm form = MgnlContext.getPostedForm();
                Map<String, Document> docs = form.getDocuments();

                for (Entry<String, Document> entry : docs.entrySet()) {
                    Document doc = entry.getValue();

                    if (doc != null) {
                        attachments.add(new MailAttachment(doc.getFile().toURL(), entry.getKey()));
                    }
                }
            }

        } catch (Exception e) {

        }
        return attachments;
    }

    public static List<MailAttachment> createAttachmentList(Map parameters) {
        List<MailAttachment> attachments = new ArrayList<MailAttachment>();
        if(parameters.containsKey("attachments")) {
            Iterator iterator = ((List)parameters.get("attachments")).iterator();
            while(iterator.hasNext()) {
                String name = (String)iterator.next();
                try {
                    attachments.add(new MailAttachment(new URL(name), name));
                } catch (MalformedURLException e) {
                    log.error("sending attachment" + name, e);
                }
            }

        } else {
            //find if there are in the form
            return createAttachmentList();
        }
        return attachments;
    }


    /**
     * convert email address mapping<br>.
     * <code>user-</code> will be replace by the email address of the user as stored in the user repository
     * <code>group-</code> will
     */
    public static String convertEmailList(String mailTo) {
        final UserManager manager = Security.getUserManager();
        StringBuffer ret = new StringBuffer();
        if(StringUtils.isEmpty(mailTo)){
            return "";
        }

        String[] list = mailTo.split(";");
        if (list == null) {
            return "";
        }
        for (int i = 0; i < list.length; i++) { // for each item
            final String token = list[i];
            if (i != 0) {
                ret.append("\n");
            }
            if (token.startsWith(MailConstants.PREFIX_USER)) {
                final String userName = StringUtils.removeStart(token, MailConstants.PREFIX_USER);
                log.debug("username = {}", userName);
                User user = manager.getUser(userName);
                ret.append(getUserMail(user));
            }
            else if (token.startsWith(MailConstants.PREFIX_GROUP)) {
                final String groupName = StringUtils.removeStart(token, MailConstants.PREFIX_GROUP);
                getGroupMembersMails(manager, ret, groupName);
            }
            else if (token.startsWith(MailConstants.PREFIX_ROLE)) {
                final String roleName = StringUtils.removeStart(token, MailConstants.PREFIX_ROLE);
                try {
                    Collection users = getAllUserNodes();
                    Iterator iter = users.iterator();
                    while(iter.hasNext()){
                        Content userNode = ((Content) iter.next());
                        User user = manager.getUser(userNode.getName());
                        if (user.getRoles().contains(roleName)){
                            ret.append(getUserMail(user));
                            ret.append("\n");
                        }
                    }
                }
                catch (Exception e) {
                    log.error("can not get user email info.");
                }
            }
            else {
                // none of the above, just add the mail to the list
                ret.append(token);
            }
        }
        return ret.toString();
    }

    protected static void getGroupMembersMails(final UserManager manager, StringBuffer ret, final String groupName) {
        log.debug("group = {}", groupName);
        try {
            Collection users = getAllUserNodes();
            Iterator iter = users.iterator();
            while(iter.hasNext()){
                Content userNode = ((Content) iter.next());
                User user = manager.getUser(userNode.getName());
                if (user.getAllGroups().contains(groupName)) {
                    String mail = getUserMail(user);
                    log.debug("user {} will be notified using mail address {}.", user.getName(), mail);
                    if (mail != null) {
                        ret.append(mail);
                        ret.append("\n");
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("can not get user email info.", e);
        }
        log.debug("found:" + ret.toString());
    }

    /**
     * TODO use UserManager. Will be fixed with MAGNOLIA-1947 / MAGNOLIA-1948
     * @return
     * @throws RepositoryException
     */
    protected static Collection<Content> getAllUserNodes() throws RepositoryException {
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USERS);
        Collection<Content> users = hm.getContent(Realm.REALM_ADMIN.getName()).getChildren(ItemType.USER);
        users.addAll(hm.getContent(Realm.REALM_SYSTEM.getName()).getChildren(ItemType.USER));
        return users;
    }

    protected static  String getUserMail(User user) {
        return user.getProperty("email");
    }

    public static Object getParameter(Map<String, Object> param, String name, String defaultValue) {

        if(param != null && param.containsKey(name)) {
            return param.get(name);
        } else {
            return defaultValue;
        }
    }

    public static void logMail(Map params, String loggerName) {
        Iterator i = params.entrySet().iterator();
        StringBuffer buf = new StringBuffer();
        while (i.hasNext()) {
            Entry pairs = (Entry) i.next();
            buf.append(" " + pairs.getKey() + " : " + pairs.getValue()
                    + ",");
        }
        org.apache.log4j.Logger.getLogger(loggerName).log(LoggingLevel.MAIL_TRAIL,
                StringUtils.remove(StringUtils.chomp(buf.toString(), ","), System.getProperty("line.separator")));
    }

}
