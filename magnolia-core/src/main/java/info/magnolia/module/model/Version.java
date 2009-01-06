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
package info.magnolia.module.model;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a module version. Format is x.y.z-classifier. y,z and classifier are
 * optional. The classifier string is ignored in version comparisons.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class Version {
    private static final Logger log = LoggerFactory.getLogger(Version.class);

    public static final Version UNDEFINED_FROM = new UndefinedEarlierVersion();
    public static final Version UNDEFINED_TO = new UndefinedLaterVersion();
    public static final Version UNDEFINED_DEVELOPMENT_VERSION = new UndefinedDevelopmentVersion();

    private static final Pattern classifierValidation = Pattern.compile("[A-Za-z0-9-_]+");
    private final short major;
    private final short minor;
    private final short patch;
    private final String classifier;

    /**
     * Convenience constructor that could be used to register Deltas or update tasks.
     */
    protected Version(int major, int minor, int patch) {
        this.major = (short) major;
        this.minor = (short) minor;
        this.patch = (short) patch;
        this.classifier = null;
    }

    private Version(String versionStr) {
        final String numbers;
        final int classifierIdx = versionStr.indexOf('-');
        if (classifierIdx > 0) {
            classifier = versionStr.substring(classifierIdx + 1);
            if (!classifierValidation.matcher(classifier).matches()) {
                throw new IllegalArgumentException("Invalid classifier: \"" + classifier + "\" in version \"" + versionStr + "\"");
            }
            numbers = versionStr.substring(0, classifierIdx);
        } else {
            classifier = null;
            numbers = versionStr;
        }

        final String[] strings = numbers.split("\\.", -1);
        if (strings.length > 0) {
            major = getShortFor("major revision", versionStr, strings[0]);
        } else {
            major = getShortFor("major revision", versionStr, versionStr);
        }
        if (strings.length > 1) {
            minor = getShortFor("minor revision", versionStr, strings[1]);
        } else {
            minor = 0;
        }
        if (strings.length > 2) {
            patch = getShortFor("patch revision", versionStr, strings[2]);
        } else {
            patch = 0;
        }
    }

    /**
     * Factory method that will parse a version string and return the correct Version implementation.
     * @param versionStr version as string, for example <code>1.2.3-test</code>. The String
     * <code>${project.version}</code> is interpreted as an undefined version during development ant it will always
     * match version ranges
     * @return a Version implementation, never null
     */
    public static Version parseVersion(String versionStr) {

        versionStr = versionStr.trim();

        log.debug("parsing version [{}]", versionStr);

        if (UndefinedDevelopmentVersion.KEY.equals(versionStr)) {
            // development mode.
            return UNDEFINED_DEVELOPMENT_VERSION;
        }

        return new Version(versionStr);
    }

    public static Version parseVersion(int major, int minor, int patch) {
        return new Version(major, minor, patch);
    }

    /**
     * Compares major, minor and patch revisions of this Version against the given Version.
     * Classifier is ignored.
     */
    public boolean isEquivalent(final Version other) {
        if(other == UNDEFINED_DEVELOPMENT_VERSION){
            return true;
        }
        return this.getMajor() == other.getMajor() &&
                this.getMinor() == other.getMinor() &&
                this.getPatch() == other.getPatch();
    }

    public boolean isStrictlyAfter(final Version other) {
        if(isEquivalent(other)){
            return false;
        }
        if (this.getMajor() != other.getMajor()) {
            return this.getMajor() > other.getMajor();
        }
        if (this.getMinor() != other.getMinor()) {
            return this.getMinor() > other.getMinor();
        }
        if (this.getPatch() != other.getPatch()) {
            return this.getPatch() > other.getPatch();
        }
        return false;
    }

    public boolean isBeforeOrEquivalent(final Version other) {
        return !isStrictlyAfter(other);
    }

    public short getMajor() {
        return major;
    }

    public short getMinor() {
        return minor;
    }

    public short getPatch() {
        return patch;
    }

    public String getClassifier() {
        return classifier;
    }

    public String toString() {
        return major + "." + minor + "." + patch + (classifier != null ? "-" + classifier : "");
    }

    private short getShortFor(String message, String versionStr, String input) {
        try {
            return Short.parseShort(input);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid " + message + ": \"" + input + "\" in version \"" + versionStr + "\"");
        }
    }

    private static final class UndefinedLaterVersion extends Version {
        public UndefinedLaterVersion() {
            super(Short.MAX_VALUE, Short.MAX_VALUE, Short.MAX_VALUE);
        }

        public String toString() {
            return "*";
        }
    }

    private static final class UndefinedEarlierVersion extends Version {
        public UndefinedEarlierVersion() {
            super(Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE);
        }

        public String toString() {
            return "*";
        }
    }

    /**
     * A undefined developer version being always equivalent to other versions.
     * This is mainly used to avoid updates during
     */
    static final class UndefinedDevelopmentVersion extends Version {

        static final String KEY = "${project.version}";

        public UndefinedDevelopmentVersion() {
            super(0, 0, 0);
        }

        public boolean isEquivalent(Version other) {
            return true;
        }

        public String toString() {
            return KEY;
        }
    }

    // generated methods:
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version version = (Version) o;

        if (major != version.major) {
            return false;
        }
        if (minor != version.minor) {
            return false;
        }
        if (patch != version.patch) {
            return false;
        }
        if (classifier != null ? !classifier.equals(version.classifier) : version.classifier != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (int) major;
        result = 31 * result + (int) minor;
        result = 31 * result + (int) patch;
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        return result;
    }
}
