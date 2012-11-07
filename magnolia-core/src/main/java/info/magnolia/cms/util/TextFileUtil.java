/**
 * This file Copyright (c) 2007-2012 Magnolia International
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
package info.magnolia.cms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility to check textFiles.
 *
 * @version $Id$
 */
public class TextFileUtil {

    public static List<String> getTrimmedLinesMatching(String fileName, String regExp) {
        final List<String> lines = getLines(fileName);
        final List<String> matchingLines = new ArrayList<String>();
        String currentLine = null;
        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
            currentLine = iterator.next().trim();
            if (currentLine.matches(regExp)) {
                matchingLines.add(currentLine);
            }
        }
        return matchingLines;
    }

    public static List<String> getLines(String fileName) {
        final List<String> names = new ArrayList<String>();
        BufferedReader jaasConfig = null;
        try {
            final File file = new File(fileName);
            if (!file.exists()) {
                throw new FileNotFoundException(fileName);
            }

            jaasConfig = new BufferedReader(new FileReader(fileName));

            String dataRow = jaasConfig.readLine();

            while (dataRow != null) {
                names.add(dataRow);
                dataRow = jaasConfig.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (jaasConfig != null) {
                    jaasConfig.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return names;
    }
}
