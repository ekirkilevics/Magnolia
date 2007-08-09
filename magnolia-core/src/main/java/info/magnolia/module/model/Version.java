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
package info.magnolia.module.model;

import java.util.regex.Pattern;
import java.util.Map;

/**
 * Represents a module version. Format is x.y.z-classifier. y,z and classifier are
 * optional. The classifier string is ignored in version comparisons.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class Version {
    public static final Version UNDEFINED_FROM = new UndefinedEarlierVersion();
    public static final Version UNDEFINED_TO = new UndefinedLaterVersion();

    private static final Pattern classifierValidation = Pattern.compile("[A-Za-z0-9]+");
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
        final int classifierIdx = versionStr.indexOf('-');
        if (classifierIdx > 0) {
            classifier = versionStr.substring(classifierIdx + 1);
            if (!classifierValidation.matcher(classifier).matches()) {
                throw new RuntimeException("Invalid classifier: " + classifier);
            }
            versionStr = versionStr.substring(0, classifierIdx);
        } else {
            classifier = null;
        }

        final String[] strings = versionStr.split("\\.", -1);
        if (strings.length > 0) {
            major = getShortFor("major revision", strings[0]);
        } else {
            major = getShortFor("major revision", versionStr);
        }
        if (strings.length > 1) {
            minor = getShortFor("minor revision", strings[1]);
        } else {
            minor = 0;
        }
        if (strings.length > 2) {
            patch = getShortFor("patch revision", strings[2]);
        } else {
            patch = 0;
        }
    }

    /**
     * Factory method that will parse a version string and return the correct Version implementation,
     * @param versionStr version as string, for example <code>1.2.3-test</code>. The String
     * <code>${project.version}</code> is interpreted as an undefined version during development ant it will always
     * match version ranges
     * @return a Version implementation, never null
     */
    public static Version parseVersion(String versionStr) {

        versionStr = versionStr.trim();
        if (UndefinedDevelopmentVersion.KEY.equals(versionStr)) {
            // development mode.
            return new UndefinedDevelopmentVersion();
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
        return this.getMajor() == other.getMajor() &&
                this.getMinor() == other.getMinor() &&
                this.getPatch() == other.getPatch();
    }

    public boolean isStrictlyAfter(final Version other) {
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

    private short getShortFor(String message, String input) {
        try {
            return Short.parseShort(input);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid " + message + ": " + input);
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

    private static final class UndefinedDevelopmentVersion extends Version {
        private static final String KEY = "${project.version}";

        public UndefinedDevelopmentVersion() {
            super(0, 0, 0);
        }

        public boolean isBeforeOrEquivalent(Version other) {
            return true;
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
