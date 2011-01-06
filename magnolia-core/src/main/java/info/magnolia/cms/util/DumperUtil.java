/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used to dump 1:1 repository content. The level defines how deep the recursion should go.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class DumperUtil {

    private static Logger log = LoggerFactory.getLogger(DumperUtil.class);

    public static String dump(Content content) {
        return dump(content, 1);
    }

    /**
     * Used to dump into a String.
     * @param content
     * @param level
     * @return
     */
    public static String dump(Content content, int level) {
        if (content == null) {
            return "";
        }

        StringWriter str = new StringWriter();
        try {
            PrintWriter writer = new PrintWriter(str);
            dump(content.getJCRNode(), level, writer);
            writer.flush();
        }
        catch (RepositoryException e) {
            log.error("can't dump", e);
        }
        return str.toString();
    }

    /**
     * Dump to a stream.
     * @param content
     * @param level
     * @param out
     */
    public static void dump(Content content, int level, PrintStream out) {
        if (content == null) {
            return;
        }
        try {
            PrintWriter writer = new PrintWriter(out);
            dump(content.getJCRNode(), level, writer);
            writer.flush();
        }
        catch (RepositoryException e) {
            log.error("can't dump", e);
        }
    }

    /**
     * Dump this node to a stream.
     * @param content
     * @param out
     */
    public static void dump(Content content, PrintStream out) {
        dump(content, 1, out);
    }

    /**
     * Dump a JCR Node to a Writer.
     * @param n
     * @param level
     * @param out
     * @throws RepositoryException
     */
    public static void dump(Node n, int level, PrintWriter out) throws RepositoryException {
        out.println(n.getPath());

        PropertyIterator pit = n.getProperties();
        while (pit.hasNext()) {
            Property p = pit.nextProperty();
            out.print(p.getPath() + "=");
            if (p.getDefinition().isMultiple()) {
                Value[] values = p.getValues();
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        out.println(",");
                    }
                    out.println(values[i].getString());
                }
            }
            else if (p.getType() == PropertyType.BINARY) {
                out.print("binary");
            }
            else {
                out.print(p.getString());
            }
            out.println();
        }

        level--;

        NodeIterator nit = n.getNodes();
        while (nit.hasNext()) {
            Node cn = nit.nextNode();
            if (level > 0) {
                dump(cn, level, out);
            }
            else{
                out.println(cn.getPath() + "[" + cn.getPrimaryNodeType().getName()  + "]");
            }
        }
    }

    /**
     * Dump only this JCR-Node to a writer.
     * @param n
     * @param out
     * @throws RepositoryException
     */
    public static void dump(Node n, PrintWriter out) throws RepositoryException {
        dump(n, 1, out);
    }

    public static void dumpChanges(HierarchyManager hm) {
        PrintWriter writer = new PrintWriter(System.out);
        try {
            dumpChanges(hm.getWorkspace().getSession(), writer);
        }
        catch (Exception e) {
            log.error("can't dump", e);
        }
        writer.flush();
    }

    public static void dumpChanges(Session session, PrintWriter out) throws RepositoryException {
        if (session.hasPendingChanges()) {
            dumpChanges(session.getRootNode(), out);
        }
    }

    private static void dumpChanges(Node node, PrintWriter out) throws RepositoryException {
        if (node.isModified()) {
            out.println(node.getPath() + " is modified");
        }
        else if (node.isNew()) {
            out.println(node.getPath() + " is new");
        }
        for (Iterator iter = node.getNodes(); iter.hasNext();) {
            Node child = (Node) iter.next();
            dumpChanges(child, out);
        }
    }

}
