/**
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.magnolia.cms.util.uuid;

/**
 * <p>
 * Constant values commonly needed in the uuid classes.
 * </p>
 * <p>
 * Copied from the Jakarta Commons-Id project
 * </p>
 * <p/>
 * @author Commons-Id Team
 * @version $Revision: 1.3 $ $Date: 2004/07/30 06:51:47 $
 */
public final class Constants {

    /** Bits in a UUID. */
    protected static final int UUID_BIT_LENGTH = 128;

    /** Number of bytes in a UUID. */
    protected static final int UUID_BYTE_LENGTH = 16;

    // ** Formatting and validation constants
    /** Chars in a UUID String. */
    protected static final int UUID_UNFORMATTED_LENGTH = 32;

    /** Chars in a UUID String. */
    protected static final int UUID_FORMATTED_LENGTH = 36;

    /** Token length of '-' separated tokens. */
    protected static final int TOKENS_IN_UUID = 5;

    /** Array to check tokenized UUID's segment lengths */
    protected static final int[] TOKEN_LENGTHS = {8, 4, 4, 4, 12};

    /** Insertion point 1 for dashes in the string format */
    protected static final int FORMAT_POSITION1 = 8;

    /** Insertion point 2 for dashes in the string format */
    protected static final int FORMAT_POSITION2 = 13;

    /** Insertion point 3 for dashes in the string format */
    protected static final int FORMAT_POSITION3 = 18;

    /** Insertion point 4 for dashes in the string format */
    protected static final int FORMAT_POSITION4 = 23;

    /** The string prefix for a urn UUID identifier. */
    protected static final String URN_PREFIX = "urn:uuid:";

    // ** UUID Variant Constants
    /**
     * UUID variant bits described in the IETF Draft MSB order, this is the "Reserved, NCS backward compatibility field"
     * 0 x x with unknown bits as 0
     */
    protected static final int VARIANT_NCS_COMPAT = 0;

    /**
     * UUID variant bits described in the IETF Draft MSB order, this is the IETF Draft memo variant field 1 0 x with
     * unknown bits as 0
     */
    protected static final int VARIANT_IETF_DRAFT = 2;

    /**
     * UUID variant bits described in the IETF Draft MSB order, this is the IETF Draft "Microsoft Corporation" field
     * variant 1 1 0 x with unknown bits as 0
     */
    protected static final int VARIANT_MS = (byte) 6;

    /**
     * UUID variant bits described in the IETF Draft MSB order, this is the "Future Reserved variant 1 1 1 x with
     * unknown bits as 0
     */
    protected static final int VARIANT_FUTURE = 7;

    // ** UUID Version Constants
    /** Version one constant for UUID version one of four */
    protected static final int VERSION_ONE = 1;

    /** Version two constant for UUID version two of four */
    protected static final int VERSION_TWO = 2;

    /** Version three constant for UUID version three of four */
    protected static final int VERSION_THREE = 3;

    /** Version four constant for UUID version four of four */
    protected static final int VERSION_FOUR = 4;

    // ** Exception message constants
    /** Message indicating this is not a version one UUID */
    protected static final String WRONG_VAR_VER_MSG = "Not a ietf variant 2 or version 1 (time-based UUID)";

    // ** Array positions and lengths of UUID fields ** //
    /** Byte length of time low field */
    protected static final int TIME_LOW_BYTE_LEN = 4;

    /** Byte length of time low field */
    protected static final int TIME_MID_BYTE_LEN = 2;

    /** Byte length of time low field */
    protected static final int TIME_HI_BYTE_LEN = 2;

    /** Timestamp byte[] position of time low field */
    protected static final int TIME_LOW_TS_POS = 4;

    /** Timestamp byte[] position mid field */
    protected static final int TIME_MID_TS_POS = 2;

    /** Timestamp byte[] position hi field */
    protected static final int TIME_HI_TS_POS = 0;

    /** uuid array position start of time low field */
    protected static final int TIME_LOW_START_POS = 0;

    /** uuid array position start of mid field */
    protected static final int TIME_MID_START_POS = 4;

    /** uuid array position start of hi field */
    protected static final int TIME_HI_START_POS = 6;

    /** Byte position of the clock sequence and reserved field */
    protected static final short TIME_HI_AND_VERSION_BYTE_6 = 6;

    /** Byte position of the clock sequence and reserved field */
    protected static final short CLOCK_SEQ_HI_AND_RESERVED_BYTE_8 = 8;

    /**
     * XXX added by stefan@apache.org: hexdigits for converting numerics to hex
     */
    protected static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Utility class, don't instantiate.
     */
    private Constants() {
        // unused
    }
}
