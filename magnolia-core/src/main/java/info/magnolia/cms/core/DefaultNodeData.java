/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper class for a jcr property.
 * @author Sameer Charles
 * @version 2.0 $Id$
 */
public class DefaultNodeData extends AbstractNodeData {
    private static final Logger log = LoggerFactory.getLogger(DefaultNodeData.class);

    protected DefaultNodeData(Content parent, String name) {
        super(parent, name);
    }
    
    public Value getValue() {
        if(isExist()){
            try {
                return getJCRProperty().getValue();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of node data" + toString());
            }
        }
        else{
            return null;
        }
    }

    public Value[] getValues() {
        if(isExist()){
            try {
                if(this.isMultiValue() == MULTIVALUE_TRUE) {
                    return getJCRProperty().getValues();
                } else {
                    //JCR-1464 needed for export of multivalue property with only one item
                    return new Value[] { getJCRProperty().getValue() };
                }
            } catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of node data" + toString());
            }
        }
        else{
            return null;
        }
    }

    public String getString() {
        if(isExist()){
            try {
                return getJCRProperty().getString();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of node data" + toString());
            }
        }
        else{
            return StringUtils.EMPTY;
        }
    }

    public long getLong() {
        if(isExist()){
            try {
                return getJCRProperty().getLong();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of node data" + toString());
            }
        }
        else{
            return 0;
        }
    }

    public double getDouble() {
        if(isExist()){
            try {
                return getJCRProperty().getDouble();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of node data" + toString());
            }
        }
        else{
            return 0;
        }
    }

    public Calendar getDate() {
        if(isExist()){
            try {
                return getJCRProperty().getDate();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of node data" + toString());
            }
        }
        else{
            return null;
        }
    }

    public boolean getBoolean() {
        if(isExist()){
            try {
                return getJCRProperty().getBoolean();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of node data" + toString());
            }
        }
        else{
            return false;
        }
    }

    public InputStream getStream() {
        if (isExist()){
            try {
                return getJCRProperty().getStream();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of node data" + toString());
            }
        }
        else{
            return null;
        }
    }

    public int getType() {
        if (isExist()) {
            try {
                return getJCRProperty().getType();
            }
            catch (Exception e) {
                log.warn("Unable to read property type for {}", name); //$NON-NLS-1$
            }
        }
        return PropertyType.UNDEFINED;
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

    public Property getJCRProperty() {
        try {
            return getJCRNode().getProperty(name);
        }
        catch (PathNotFoundException e) {
            return null;
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    protected Node getJCRNode() {
        return parent.getJCRNode();
    }

    protected Content getContentFromJCRReference() throws RepositoryException {
        return getHierarchyManager().getContent(getJCRProperty().getNode().getPath());
    }

    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getJCRNode().setProperty(name, value);
    }

    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getJCRNode().setProperty(name, value);
    }

    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getJCRNode().setProperty(name, value);
    }

    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getJCRNode().setProperty(name, value);
    }

    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getJCRNode().setProperty(name, value);
    }

    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getJCRNode().setProperty(name, value);    }

    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getJCRNode().setProperty(name, value);    }

    public void setValue(Value[] value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getJCRNode().setProperty(name, value);    }

    public void setValue(Content value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.SET);
        getJCRNode().setProperty(name, value.getJCRNode());
    }

    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        throw new UnsupportedOperationException("This operation is only supported for node datas of type BINARY");
    }
    
    public boolean isExist() {
        try {
            return getJCRNode().hasProperty(name);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() throws RepositoryException {
        if(isExist()){
            getJCRProperty().save();
        }
    }

    public void delete() throws RepositoryException {
        Access.isGranted(getHierarchyManager().getAccessManager(), Path.getAbsolutePath(this.getHandle()), Permission.REMOVE);
        if(isExist()){
            getJCRProperty().remove();
        }
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
        if(isExist()){
            getJCRProperty().refresh(keepChanges);
        }
    }

}
