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
package info.magnolia.cms.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.log4j.Logger;


/**
 * ReverseFileReader alows backword reading of <code>RandomAccessFile</code>
 * @author Sameer Charles
 */
public class ReverseFileReader extends RandomAccessFile {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ReverseFileReader.class);

    private long filelength;

    private long pointer = 3;

    public ReverseFileReader(File file, String mode) throws FileNotFoundException {
        super(file, mode);
        this.movePointerToEnd();
    }

    public ReverseFileReader(String file, String mode) throws FileNotFoundException {
        super(file, mode);
        this.movePointerToEnd();
    }

    private void movePointerToEnd() {
        try {
            this.filelength = this.length();
            this.seek(this.length() - 2); // "-2" line separator.
        }
        catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * @return record after the current file pointer
     */
    public String getRecord() {
        String fileRecord = null;
        byte b = 0;
        try {
            while (b != 10) {
                this.seek(this.filelength - this.pointer);
                b = this.readByte();
                this.pointer++;
            }
            fileRecord = this.readLine();
        }
        catch (IOException e) {
            log.error(e.getMessage());
        }
        return fileRecord;
    }
}
