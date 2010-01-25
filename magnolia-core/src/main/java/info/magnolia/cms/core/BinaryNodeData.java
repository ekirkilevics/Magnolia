/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * A node data hiding the fact that node datas of type BINARY are stored as nodes of type {@link ItemType#NT_RESOURCE}.
 * @author pbaerfuss
 * @version $Id$
 */
public class BinaryNodeData extends AbstractNodeData {

    /**
     * The node containing the binary and attributes.
     */
    private Node binaryNode;
    
    /**
     * The property containing the binary.
     * @see ItemType.JCR_DATA
     */
    private Property binaryProperty;

    protected BinaryNodeData(Content parent, String name) {
        super(parent, name);
    }

    public Property getJCRProperty() {
        if(binaryProperty == null){
            if(isExist()){
                try {
                    binaryProperty = getBinaryNode(false).getProperty(ItemType.JCR_DATA);
                }
                catch (RepositoryException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return binaryProperty;
    }
    
    protected Node getBinaryNode(boolean createIfNotExisting) {
        if(binaryNode == null){
            Node parentJCRNode = parent.getJCRNode();
            try {
                if(parentJCRNode.hasNode(name)){
                    binaryNode = parentJCRNode.getNode(name);
                }
                
                if(createIfNotExisting){
                    binaryNode = parentJCRNode.addNode(name, ItemType.NT_RESOURCE);
                }
            }
            catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        return binaryNode;
    }

    public boolean isExist() {
        return getBinaryNode(false) != null;
    }

    public InputStream getStream() {
        if (isExist()) {
            try {
                return getJCRProperty().getStream();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of node data" + toString());
            }
        }
        else {
            return null;
        }
    }
    
    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getBinaryNode(true).setProperty(ItemType.JCR_DATA, value);
    }

    public void delete() throws RepositoryException {
        Access.isGranted(getHierarchyManager().getAccessManager(), getHandle(), Permission.REMOVE);
        if(isExist()){
            getBinaryNode(false).remove();
        }
    }

    public void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getBinaryNode(true).setProperty(name, value);
    }

    public void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getBinaryNode(true).setProperty(name, value);
    }

    public String getAttribute(String name) {
        if(isExist()){
            Node binaryNode = getBinaryNode(false);
            try {
                if(binaryNode.hasProperty(name)){
                    return binaryNode.getProperty(name).getString();
                }
            }
            catch (RepositoryException e) {
                throw new IllegalStateException("Can't read attribute", e);
            }
        }
        return "";
    }

    public Collection<String> getAttributeNames() throws RepositoryException {
        Collection<String> names = new ArrayList<String>();
        if(isExist()){
            PropertyIterator properties = getBinaryNode(false).getProperties();
            while (properties.hasNext()) {
                String name = properties.nextProperty().getName();
                if (!name.equalsIgnoreCase(ItemType.JCR_DATA)) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    public int getType() {
        return PropertyType.BINARY;
    }

    public Value getValue() {
        if(isExist()){
            try {
                return getJCRProperty().getValue();
            }
            catch (RepositoryException e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

    public long getContentLength() {
        if(!isExist()){
            return 0;
        }

        try {
            return getJCRProperty().getLength();
        }
        catch (RepositoryException re) {
            throw new RuntimeException(re);
        }
    }

    public int isMultiValue() {
        return MULTIVALUE_FALSE;
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
        if(isExist()){
            getBinaryNode(false).refresh(keepChanges);
        }
    }

    public void save() throws RepositoryException {
        if(isExist()){
            getBinaryNode(false).save();
        }
    }

    public Content getReferencedContent() throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public Content getReferencedContent(String repositoryId) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    protected Content getContentFromJCRReference() throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public String getString() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public Calendar getDate() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public boolean getBoolean() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }
    
    public double getDouble() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public long getLong() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public Value[] getValues() {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(String value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(int value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(long value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(double value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(boolean value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(Calendar value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(Content value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(Value value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }

    public void setValue(Value[] value) throws RepositoryException {
        throw new UnsupportedOperationException("This operation is not supported on node datas of type BINARY");
    }
}
