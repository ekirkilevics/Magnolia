/**
 * This file Copyright (c) 2007-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia.info/mna.html
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
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewerPage extends TemplatedMVCHandler {
	private static final Logger log = LoggerFactory
			.getLogger(LogViewerPage.class);

	private final String LOGS_FOLDER = "magnolia.logs.dir";

	public LogViewerPage(String name, HttpServletRequest request,
			HttpServletResponse response) {
		super(name, request, response);

		//initialize the folder variable
		logsFolder = Path.getAbsoluteFileSystemPath(SystemProperty.getProperty(LOGS_FOLDER));
	}

	private String logsFolder = "";

	private String fileName = "";

	private String text = "";

	private Collection namesList = null;

    private int maxNumLinesPerPage = 50;

    private int pageNumber = 0;

    private int totalPages = 0;

    private int currentPosition = 0;

    private int fileSizeInLines = 0;

	/**
	 * the content of the select
	 * @return
	 */
    public Collection getLogFiles() {

		ArrayList urls = new ArrayList();

		File logDir = new File(logsFolder);
		Collection files = null;
		if (logDir.exists()) {
			files = FileUtils.listFiles(logDir, new TrueFileFilter() {
			}, new TrueFileFilter() {
			});

			Iterator filesIterator = files.iterator();
			String name = "";
			while (filesIterator.hasNext()) {
				name = ((File) filesIterator.next()).getName();
				urls.add(name);
			}
		}
		return urls;
	}

	public String next() {
	    displayFileContent();
	    return VIEW_SHOW;
	}

	public String previous() {
	    if(currentPosition >= maxNumLinesPerPage*2 ) {
	    currentPosition -= maxNumLinesPerPage*2;
	    } else {
	        currentPosition = 0;
	    }
	    displayFileContent();
	    return VIEW_SHOW;
    }

	public String begin() {
	    currentPosition = 0;
	    displayFileContent();
        return VIEW_SHOW;
    }


	public String end() {
	    if(fileSizeInLines > maxNumLinesPerPage) {
	        currentPosition = fileSizeInLines - maxNumLinesPerPage;
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

        try{
            sendUnCompressed(is, this.getResponse());
        }
        catch(Exception e){
            log.info("File download failed [{}]: {}", fileName, e.getMessage());
        }
        finally{
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
                fileSizeInLines = countLines(file);
                PrintWriter writer = new PrintWriter(str);

                input = new LineNumberReader(logFile);

                String line;
                int numLines = 0;
                while ((line = input.readLine()) != null) {

                    if (input.getLineNumber() >= currentPosition) {
                        writer.write(line);
                        writer.write("<br/>");
                        numLines++;
                    }
                    if (numLines >= maxNumLinesPerPage) {
                        currentPosition = input.getLineNumber() + 1;
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
            File file = new File(logsFolder + "/" + this.fileName);
            return file;
        }
        return null;
    }

    private void setFieldValues() {
        pageNumber = currentPosition/maxNumLinesPerPage;
        totalPages = fileSizeInLines/maxNumLinesPerPage;
        if(pageNumber == 0 || totalPages == 0) {
            pageNumber = 1;
            totalPages = 1;
        }

    }

    /* gets the number of lines for pagination*/
    private int countLines(File file) {
        int count = 0;
        FileReader fileReader = null;
        LineNumberReader lineReader = null;
        try {
            fileReader = new FileReader(file);
            lineReader = new LineNumberReader(fileReader);

            lineReader.skip(file.length() - 1);
            count = lineReader.getLineNumber() - 1;

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
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Collection getNamesList() {
		if (namesList == null) {
			namesList = getLogFiles();
		}
		return namesList;
	}

	public String getText() {
		return text;
	}

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getFileSizeInLines() {
        return fileSizeInLines;
    }

    public void setFileSizeInLines(int fileSizeInLines) {
        this.fileSizeInLines = fileSizeInLines;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getMaxNumLinesPerPage() {
        return maxNumLinesPerPage;
    }

}

