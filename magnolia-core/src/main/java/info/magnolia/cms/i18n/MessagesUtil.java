/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.i18n;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;


/**
 * Util for handle messages. Allows easy use of chains and provides methods for rendering the javascript messages
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class MessagesUtil {

    /**
     * Adds Variables to a JS witch can be used with the getMessage(key) method
     * @return Javascript-Construct of this textes
     */
    public static String generateJavaScript(Messages messages) {
        StringBuffer str = new StringBuffer();

        str.append("/* ###################################\n"); //$NON-NLS-1$
        str.append("### Generated AbstractMessagesImpl\n"); //$NON-NLS-1$
        str.append("################################### */\n\n"); //$NON-NLS-1$

        for (Iterator iter = messages.keys(); iter.hasNext();) {
            String key = (String) iter.next();

            if (key.endsWith(".js")) { //$NON-NLS-1$
                String msg = javaScriptString(messages.get(key));
                str.append(AbstractMessagesImpl.JS_OBJECTNAME
                    + ".add('"
                    + key
                    + "','"
                    + msg
                    + "','"
                    + messages.getBasename()
                    + "');");
                str.append("\n"); //$NON-NLS-1$
            }
        }
        return str.toString();
    }

    /**
     * @param msgs
     * @param messages
     */
    public static Messages chain(Messages msgs1, Messages msgs2) {
        MessagesChain msgs = new MessagesChain(msgs1);
        msgs.chain(msgs2);
        return msgs;
    }

    public static Messages chain(String[] basenames) {
        Messages head = MessagesManager.getMessages(basenames[0]);
        MessagesChain chain = new MessagesChain(head);
        for (int i = 1; i < basenames.length; i++) {
            String basename = basenames[i];
            Messages msgs = MessagesManager.getMessages(basename);
            chain.chain(msgs);
        }
        chain.chain(MessagesManager.getMessages());
        return chain;
    }

    /**
     * @param messages
     * @param basename
     * @return
     */
    public static Messages chain(Messages msgs1, String basename) {
        Messages msgs2 = MessagesManager.getMessages(basename);
        return chain(msgs1, msgs2);
    }

    /**
     * @param messages
     * @param basename
     * @return
     */
    public static Messages chain(String basename, Messages msgs2) {
        Messages msgs1 = MessagesManager.getMessages(basename);
        return chain(msgs1, msgs2);
    }

    /**
     * @param string
     * @return
     */
    public static Messages chainWithDefault(String basename) {
        Messages msgs1 = MessagesManager.getMessages(basename);
        Messages msgs2 = MessagesManager.getMessages();
        return chain(msgs1, msgs2);
    }

    /**
     * @param title
     * @return
     */
    public static String javaScriptString(String msg) {
        return StringUtils.replace(StringUtils.replace(msg, "'", "\\'"), "\n", "\\n");
    }
    
    public static String get(String key){
        return MessagesManager.getMessages().get(key);
    }

    public static String get(String key,String[] args){
        return MessagesManager.getMessages().get(key, args);
    }

    public static String getWithDefault(String key, String dflt){
        return MessagesManager.getMessages().getWithDefault(key, dflt);
    }

    public static String getWithDefault(String key, String dflt, String[] args){
        return MessagesManager.getMessages().getWithDefault(key, args, dflt);
    }

    public static String get(String key, String basename){
        return MessagesManager.getMessages(basename).get(key);
    }

    public static String get(String key, String basename, String[] args){
        return MessagesManager.getMessages(basename).get(key, args);
    }

    public static String getWithDefault(String key, String dflt, String basename){
        return MessagesManager.getMessages(basename).getWithDefault(key, dflt);
    }

    public static String getWithDefault(String key, String dflt, String basename, String[] args){
        return MessagesManager.getMessages(basename).getWithDefault(key, args, dflt);
    }

    public static String getChained(String key, String[] basenames){
        return chain(basenames).get(key);
    }

    public static String getChained(String key, String[] basenames, String[] args){
        return chain(basenames).get(key, args);
    }

    public static String getChainedWithDefault(String key, String dflt, String[] basenames){
        return chain(basenames).getWithDefault(key, dflt);
    }

    public static String getChainedWithDefault(String key, String dflt, String[] basenames, String[] args){
        return chain(basenames).getWithDefault(key, args, dflt);
    }

}
