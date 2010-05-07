/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewerPage extends TemplatedMVCHandler {
    private static final Logger log = LoggerFactory.getLogger(LogViewerPage.class);

    private final static String LOGS_FOLDER_PROPERTY = "magnolia.logs.dir";

    public LogViewerPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        // initialize the folder variable
        String temp = SystemProperty.getProperty(LOGS_FOLDER_PROPERTY);
        if (temp != null) {
            logsFolder = Path.getAbsoluteFileSystemPath(temp);
        }
    }

    private String logsFolder = "";

    private String fileName = "";

    private String text = "";

    private Collection namesList = null;

    private long maxNumLinesPerPage = 50;

    private long pageNumber = 0;

    private long totalPages = 0;

    private long currentPosition = 0;

    private long fileSizeInLines = 0;

    /**
     * the content of the select
     *
     * @return
     */
    public Collection getLogFiles() {

        ArrayList urls = new ArrayList();

        File logDir = new File(this.logsFolder);
        Collection files = null;
        if (logDir.exists()) {
            files = FileUtils.listFiles(logDir, new WildcardFileFilter("*.log*"), TrueFileFilter.TRUE);

            Iterator filesIterator = files.iterator();
            String name = "";
            while (filesIterator.hasNext()) {
                name = ((File) filesIterator.next()).getName();
                urls.add(name);
            }
        }
        return urls;
    }

    public String refresh() {
        displayFileContent();
        return VIEW_SHOW;
    }

    public String next() {
        this.currentPosition = Math.min(this.currentPosition + this.maxNumLinesPerPage, this.fileSizeInLines - 1);
        displayFileContent();
        return VIEW_SHOW;
    }

    public String previous() {
        this.currentPosition = Math.max(0, this.currentPosition - this.maxNumLinesPerPage);
        displayFileContent();
        return VIEW_SHOW;
    }

    public String begin() {
        this.currentPosition = 0;
        displayFileContent();
        return VIEW_SHOW;
    }

    public String end() {
        if (this.fileSizeInLines > this.maxNumLinesPerPage) {
            this.currentPosition = this.fileSizeInLines - (this.fileSizeInLines % this.maxNumLinesPerPage);
        } else {
            currentPosition = 0;
        }
        displayFileContent();
        return VIEW_SHOW;
    }

    public String download() throws FileNotFoundException {
        // set mime/type
        File file = getFile();
        this.getResponse().setContentType("html/text");
        this.getResponse().setHeader("Content-Disposition", "attachment; filename=" + fileName);

        FileInputStream is = new FileInputStream(file);
        this.getResponse().setContentLength((int) file.length());

        try {
            sendUnCompressed(is, this.getResponse());
        } catch (Exception e) {
            log.info("File download failed [{}]: {}", fileName, e.getMessage());
        } finally {
            IOUtils.closeQuietly(is);
        }

        return "";
    }

    private void sendUnCompressed(java.io.InputStream is, HttpServletResponse res) throws Exception {
        ServletOutputStream os = res.getOutputStream();
        byte[] buffer = new byte[8192];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
        }
        os.flush();
        os.close();
    }

    public String displayFileContent() {

        StringWriter str = new StringWriter();
        if (StringUtils.isNotEmpty(this.fileName)) {
            FileReader logFile = null;
            LineNumberReader input = null;
            try {
                // need file to get size
                File file = getFile();

                logFile = new FileReader(file);
                this.fileSizeInLines = countLines(file);
                PrintWriter writer = new PrintWriter(str);

                input = new LineNumberReader(logFile);

                String line;
                int numLines = 0;
                while ((line = input.readLine()) != null) {

                    if (input.getLineNumber() >= this.currentPosition) {
                        writer.write(StringEscapeUtils.escapeHtml(line));
                        writer.write("<br/>");
                        numLines++;
                    }
                    if (numLines >= this.maxNumLinesPerPage) {
                        break;
                    }
                }
                writer.flush();
                this.text = str.toString();
                writer.close();

                setFieldValues();

            } catch (Exception e) {
                this.text = e.getMessage();
                log.error("Error can't read file:", e);
                return VIEW_SHOW;
            } finally {

                closeFile(logFile, input);
            }
        }

        return VIEW_SHOW;
    }

    private File getFile() {
        if (this.fileName.length() > 0) {
            File file = new File(this.logsFolder + "/" + this.fileName);
            return file;
        }
        return null;
    }

    private void setFieldValues() {
        this.pageNumber = (this.currentPosition / this.maxNumLinesPerPage) + 1;
        this.totalPages = (this.fileSizeInLines / this.maxNumLinesPerPage) + 1;
        long mod = this.currentPosition % this.maxNumLinesPerPage;
        if (mod > 0) {
            // someone switched page size in between the exact multiples of the pages or there is only one page in total
            this.pageNumber++;
            this.totalPages++;
        }
    }

    /* gets the number of lines for pagination */
    private long countLines(File file) {
        int count = 0;
        FileReader fileReader = null;
        LineNumberReader lineReader = null;
        try {
            fileReader = new FileReader(file);
            lineReader = new LineNumberReader(fileReader);

            lineReader.skip(file.length() );
            count = lineReader.getLineNumber() + 1;

        } catch (Exception e) {
            count = 0;
        } finally {

            closeFile(fileReader, lineReader);
        }

        return count;
    }

    private void closeFile(FileReader fileReader, LineNumberReader lineReader) {
        try {
            if (fileReader != null) {
                fileReader.close();

            }
            if (lineReader != null) {
                lineReader.close();
            }
        } catch (Exception e) {

        }
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Collection getNamesList() {
        if (this.namesList == null) {
            this.namesList = getLogFiles();
        }
        return this.namesList;
    }

    public String getText() {
        return this.text;
    }

    public long getCurrentPosition() {
        return this.currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        // make sure number never exceeds bounds
        this.currentPosition = Math.max(0, Math.min(currentPosition, this.fileSizeInLines - (this.fileSizeInLines % this.maxNumLinesPerPage)));
    }

    public long getFileSizeInLines() {
        return this.fileSizeInLines;
    }

    public void setFileSizeInLines(long fileSizeInLines) {
        this.fileSizeInLines = fileSizeInLines;
    }

    public long getPageNumber() {
        return this.pageNumber;
    }

    public long getTotalPages() {
        return this.totalPages;
    }

    public long getMaxNumLinesPerPage() {
        return this.maxNumLinesPerPage;
    }

    /**
     * Sets new maximum value in the range <1, LONG_MAX>. 0 or negative values are ignored.
     * @param maxNumLinesPerPage
     */
    public void setMaxNumLinesPerPage(long maxNumLinesPerPage) {
        if (maxNumLinesPerPage < 1) {
            return;
        }
        this.maxNumLinesPerPage = maxNumLinesPerPage;
    }

}
