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
package info.magnolia.cms.i18n;

import java.util.Iterator;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;


/**
 * Util for handle messages. Allows easy use of chains and provides methods for rendering the javascript messages
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class MessagesUtil {

    /**
     * @deprecated since 4.0, use generateJavaScript(Writer out, Messages messages) instead.
     */
    public static String generateJavaScript(Messages messages) {
        final StringWriter out = new StringWriter();
        try {
            generateJavaScript(out, messages);
            return out.toString();
        } catch (IOException e) {
            // can't happen with a StringWriter
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds Variables to a JS witch can be used with the getMessage(key) method
     * @return Javascript-Construct of this textes
     */
    public static void generateJavaScript(Writer out, Messages messages) throws IOException {
        out.write("/* ###################################\n"); //$NON-NLS-1$
        out.write("### Generated AbstractMessagesImpl\n"); //$NON-NLS-1$
        out.write("################################### */\n\n"); //$NON-NLS-1$

        for (Iterator iter = messages.keys(); iter.hasNext();) {
            String key = (String) iter.next();

            if (key.endsWith(".js")) { //$NON-NLS-1$
                String msg = javaScriptString(messages.get(key));
                out.write(AbstractMessagesImpl.JS_OBJECTNAME
                    + ".add('"
                    + key
                    + "','"
                    + msg
                    + "','"
                    + messages.getBasename()
                    + "');");
                out.write("\n"); //$NON-NLS-1$
            }
        }
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
