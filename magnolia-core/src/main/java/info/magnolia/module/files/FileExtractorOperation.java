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
package info.magnolia.module.files;

import java.io.IOException;

/**
 * A single file extraction operation. Every file extraction requires a new instance
 * of this interface, giving it a chance to keep some state. (checksums, ...)
 * The necessary parameters (resourcePath, absoluteTargetPath, ...) must be passed
 * to the constructor as appropriate.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
interface FileExtractorOperation {

    void extract() throws IOException;

}
