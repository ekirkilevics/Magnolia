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

package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
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
                    if (i > 0)
                        out.println(",");
                    out.println(values[i].getString());
                }
            }
            else if (p.getType() == PropertyType.BINARY) {
                out.print("Binary");
            }
            else {
                out.print(p.getString());
            }
            out.println();
        }

        level--;
        if (level > 0) {
            NodeIterator nit = n.getNodes();
            while (nit.hasNext()) {
                Node cn = nit.nextNode();
                dump(cn, level, out);
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

}
