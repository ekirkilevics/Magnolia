/**
 * This file Copyright (c) 2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.setup.for3_6;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class CleanBootstrapFiles{

    public static void main(String[] args) throws FileNotFoundException, JDOMException, IOException{
        for (Iterator iter = findFiles(new File(".")); iter.hasNext();) {
            File file = (File) iter.next();
            System.out.println("Processing: " + file);
            if(cleanFile(file)){
                System.out.println("Cleaned: " + file);
            }
            else{
                System.out.println("Nothing to clean: " + file);
            }
        };
    }

    public static boolean cleanFile(File file) throws FileNotFoundException, JDOMException, IOException {
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final boolean cleaned = clean(in, buffer);
        if(cleaned){
            IOUtils.closeQuietly(in);
            FileUtils.writeByteArrayToFile(file, buffer.toByteArray());
        }
        else{
            IOUtils.closeQuietly(in);
        }
        return cleaned;
    }

    public static Iterator findFiles(File root){
        return FileUtils.iterateFiles(root, new AbstractFileFilter(){
            public boolean accept(File file) {
                return file.getName().endsWith(".xml") && file.getPath().contains(File.separator + "mgnl-bootstrap");
            }
        }, TrueFileFilter.TRUE);
    }

    private static boolean clean(InputStream in, OutputStream out) throws JDOMException, IOException{
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);

        XPath xpath = XPath.newInstance("//sv:property[@sv:name='jcr:mixinTypes']/sv:value[text()='mix:versionable']");
        Collection nodes = xpath.selectNodes(doc.getRootElement());
        if(nodes.isEmpty()){
            return false;
        }
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Element node = (Element) iter.next();
            if(node.getParentElement().getChildren().size()==1){
                node.getParentElement().detach();
            }
            else{
                node.detach();
            }
        }
        final Format format = Format.getPrettyFormat();
        format.setLineSeparator("\n");
        new XMLOutputter(format).output(doc, out);
        return true;
   }
}
