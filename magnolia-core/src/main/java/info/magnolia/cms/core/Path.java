/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.core;

import java.io.File;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.safehaus.uuid.UUIDGenerator;


/**
 * Utility class to retrieve files or directory used by Magnolia. Examples: cache directory, tmp files, ..
 * @version 2.0 $Id$
 */
public final class Path {
    /**
     * New unlabeled nodes default name.
     */
    private static final String DEFAULT_UNTITLED_NODE_NAME = "untitled";

    public static final String SELECTOR_DELIMITER = "~";

    /**
     * Utility class, don't instantiate.
     */
    private Path() {
        // unused
    }

    /**
     * Gets the cache directory path (cms.cache.startdir) as set with Java options while startup or in web.xml.
     * @return Cache directory path
     */
    public static String getCacheDirectoryPath() {
        return getCacheDirectory().getAbsolutePath();
    }

    public static File getCacheDirectory() {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_CACHE_STARTDIR);
        File dir = isAbsolute(path) ? new File(path) : new File(getAppRootDir(), path);
        dir.mkdirs();
        return dir;
    }

    /**
     * Gets the temporary directory path (cms.upload.tmpdir) as set with Java options while startup or in web.xml.
     * @return Temporary directory path
     */
    public static String getTempDirectoryPath() {
        return getTempDirectory().getAbsolutePath();
    }

    public static File getTempDirectory() {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR);
        File dir = isAbsolute(path) ? new File(path) : new File(getAppRootDir(), path);
        dir.mkdirs();
        return dir;
    }

    /**
     * Gets cms.exchange.history file location as set with Java options while startup or in web.xml.
     * @return exchange history file location
     */
    public static String getHistoryFilePath() {
        return getHistoryFile().getAbsolutePath();
    }

    public static File getHistoryFile() {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_EXCHANGE_HISTORY);
        return isAbsolute(path) ? new File(path) : new File(getAppRootDir(), path);
    }

    /**
     * Gets repositories file location as set with Java options while startup or in web.xml.
     * @return file location
     */
    public static String getRepositoriesConfigFilePath() {
        return getRepositoriesConfigFile().getAbsolutePath();
    }

    public static File getRepositoriesConfigFile() {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG);
        return isAbsolute(path) ? new File(path) : new File(getAppRootDir(), path);
    }

    /**
     * Gets the root directory for the magnolia web application.
     * @return magnolia root dir
     */
    public static File getAppRootDir() {
        return new File(SystemProperty.getProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR));
    }

    /**
     * Gets absolute filesystem path, adds application root if path is not absolute.
     */
    public static String getAbsoluteFileSystemPath(String path) {
        if (isAbsolute(path)) {
            return path;
        }
        // using the file() constructor will allow relative paths in the form ../../apps
        return new File(getAppRootDir(), path).getAbsolutePath();
    }

    public static String getUniqueLabel(HierarchyManager hierarchyManager, String parent, String label) {
        if (parent.equals("/")) {
            parent = StringUtils.EMPTY;
        }
        while (hierarchyManager.isExist(parent + "/" + label)) {
            label = createUniqueName(label);
        }
        return label;
    }

    public static String getUniqueLabel(Session session, String parent, String label) throws RepositoryException {
        if (parent.equals("/")) {
            parent = StringUtils.EMPTY;
        }
        while (session.itemExists(parent + "/" + label)) {
            label = createUniqueName(label);
        }
        return label;
    }

    public static String getUniqueLabel(Content parent, String label) {
        try {
            while (parent.hasContent(label) || parent.hasNodeData(label)) {
                label = createUniqueName(label);
            }
        }
        catch (RepositoryException e) {
            label = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
        }
        return label;
    }

    public static boolean isAbsolute(String path) {

        if (path == null) {
            return false;
        }

        if (path.startsWith("/") || path.startsWith(File.separator)) {
            return true;
        }

        // windows c:
        if (path.length() >= 3 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
            return true;
        }

        return false;
    }

    /**
     * Replace illegal characters based on system property magnolia.ut8.enabled.
     * @param label label to validate
     * @return validated label
     */
    public static String getValidatedLabel(String label)
    {
        String charset = StringUtils.EMPTY;
        if ((SystemProperty.getBooleanProperty(SystemProperty.MAGNOLIA_UTF8_ENABLED)))
        {
            charset = "UTF-8";
        }
        return getValidatedLabel(label, charset);

    }

    /**
     * If charset equals <code>UTF-8</code>, replaces the following characters with a dash <code>-</code> :
     * <p>
     * Jackrabbit not allowed {@code 32: [ ] 91: [[] 93: []] 42: [*] 34: ["] 58 [:] 92: [\] 39 :[']}
     * <p>
     * URL not valid {@code 59: [;] 47: [/] 63: [?] 43: [+] 37: [%] 33: [!] 35:[#] 94: [^]}.
     * <p>
     * Otherwise, replaces illegal characters with a dash <code>-</code> except for {@code [_] [0-9], [A-Z], [a-z], [-], [_], [.]}.
     * <p>
     * Please notice that a valid label can not begin with dot or period <code>[.]</code>.
     *
     * @return a validated label for a node.
     */
    public static String getValidatedLabel(String label, String charset)
    {
        if(StringUtils.isEmpty(label)) {
            return DEFAULT_UNTITLED_NODE_NAME;
        }
        final StringBuilder newLabel = new StringBuilder(label.length());

        //label cannot begin with . (dot)
        int ch = label.charAt(0);
        if(!isCharValid(ch, charset) || ch == 46) {
            newLabel.append("-");
        } else {
            newLabel.append(label.charAt(0));
        }

        for (int i = 1; i < label.length(); i++)
        {
            int charCode = label.charAt(i);
            if (isCharValid(charCode, charset))
            {
                newLabel.append(label.charAt(i));
            }
            else
            {
                newLabel.append("-");
            }
        }
        if (newLabel.length() == 0)
        {
            newLabel.append(DEFAULT_UNTITLED_NODE_NAME);
        }
        return newLabel.toString();
    }

    /**
     * @param charCode char code
     * @param charset charset (ex. UTF-8)
     * @return true if char can be used as a content name
     */
    public static boolean isCharValid(int charCode, String charset)
    {
        //TODO fgrilli: we now allow dots (.) in JR local names but actually in JR 2.0 other chars could be allowed as well
        //(see http://www.day.com/specs/jcr/2.0/3_Repository_Model.html paragraph 2.2 and org.apache.jackrabbit.util.XMLChar.isValid()).
        //Also, now that we're on java 6 and JR 2.0 should the check for the charset be dropped?

        // http://www.ietf.org/rfc/rfc1738.txt
        // safe = "$" | "-" | "_" | "." | "+"
        // extra = "!" | "*" | "'" | "(" | ")" | ","
        // national = "{" | "}" | "|" | "\" | "^" | "~" | "[" | "]" | "`"
        // punctuation = "<" | ">" | "#" | "%" | <">
        // reserved = ";" | "/" | "?" | ":" | "@" | "&" | "="

        if ("UTF-8".equals(charset))
        {
            // jackrabbit not allowed 32: [ ] 91: [[] 93: []] 42: [*] 34: ["] 46: [.] 58 [:] 92: [\] 39 :[']
            // url not valid 59: [;] 47: [/] 63: [?] 43: [+] 37: [%] 33: [!] 35:[#]
            if (charCode != 32
                && charCode != '['
                && charCode != ']'
                && charCode != '*'
                && charCode != '"'
                && charCode != ':'
                && charCode != 92
                && charCode != 39
                && charCode != ';'
                && charCode != '/'
                && charCode != '?'
                && charCode != '+'
                && charCode != '%'
                && charCode != '!'
                && charCode != '#'
                && charCode != '@'
                && charCode != '&'
                && charCode != '=')
            {
                return true;
            }
        }
        else
        {
            // charCodes: 48-57: [0-9]; 65-90: [A-Z]; 97-122: [a-z]; 45: [-]; 95:[_]
            if (((charCode >= 48) && (charCode <= 57))
                    || ((charCode >= 65) && (charCode <= 90))
                    || ((charCode >= 97) && (charCode <= 122))
                    || charCode == 45
                    || charCode == 46
                    || charCode == 95)
            {
                return true;
            }

        }
        return false;

    }

    private static String createUniqueName(String baseName) {
        int pos;
        for (pos = baseName.length() - 1; pos >= 0; pos--) {
            char c = baseName.charAt(pos);
            if (c < '0' || c > '9') {
                break;
            }
        }
        String base;
        int cnt;
        if (pos == -1) {
            if (baseName.length() > 1) {
                pos = baseName.length() - 2;
            }
        }
        if (pos == -1) {
            base = baseName;
            cnt = -1;
        }
        else {
            pos++;
            base = baseName.substring(0, pos);
            if (pos == baseName.length()) {
                cnt = -1;
            }
            else {
                cnt = new Integer(baseName.substring(pos)).intValue();
            }
        }
        return (base + ++cnt);
    }

    public static String getAbsolutePath(String path, String label) {
        if (StringUtils.isEmpty(path) || (path.equals("/"))) {
            return "/" + label;
        }

        return path + "/" + label;
    }

    public static String getAbsolutePath(String path) {
        if (!path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    /**
     * @deprecated since 4.0 - untested and unused
     */
    @Deprecated
    public static String getNodePath(String path, String label) {
        if (StringUtils.isEmpty(path) || (path.equals("/"))) {
            return label;
        }
        return getNodePath(path + "/" + label);
    }

    /**
     * @deprecated since 4.0 - untested and unused
     */
    @Deprecated
    public static String getNodePath(String path) {
        if (path.startsWith("/")) {
            return path.replaceFirst("/", StringUtils.EMPTY);
        }
        return path;
    }

    /**
     * @deprecated since 4.0 - untested and unused
     */
    @Deprecated
    public static String getParentPath(String path) {
        int lastIndexOfSlash = path.lastIndexOf("/");
        if (lastIndexOfSlash > 0) {
            return StringUtils.substringBefore(path, "/");
        }
        return "/";
    }
}
