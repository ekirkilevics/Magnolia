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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper class for a jcr property.
 *
 * @version $Id$
 * @deprecated since 5.0, use jcr.Property instead.
 */
@Deprecated
public class DefaultNodeData extends AbstractNodeData {
    private static final Logger log = LoggerFactory.getLogger(DefaultNodeData.class);

    protected DefaultNodeData(Content parent, String name) {
        super(parent, name);
    }

    @Override
    public Value getValue() {
        if(isExist()){
            try {
                return getJCRProperty().getValue();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of nodedata " + toString(), e);
            }
        }
        return null;
    }

    @Override
    public Value[] getValues() {
        if(isExist()){
            try {
                if(this.isMultiValue() == MULTIVALUE_TRUE) {
                    return getJCRProperty().getValues();
                }
                //JCR-1464 needed for export of multivalue property with only one item
                return new Value[] { getJCRProperty().getValue() };
            } catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of nodedata " + toString(), e);
            }
        }
        return null;
    }

    @Override
    public String getString() {
        if(isExist()){
            try {
                return getJCRProperty().getString();
            }
            // multi value
            catch(ValueFormatException e){
                final StringBuffer str = new StringBuffer();
                Value[] values = getValues();
                for (Value value : values) {
                    if(str.length()>0){
                        str.append(", ");
                    }
                    try {
                        str.append(value.getString());
                    }
                    catch (RepositoryException e1) {
                        throw new RuntimeException("Can't read multi value nodedata " + toString(), e);
                    }
                }
                return str.toString();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of nodedata " + toString(), e);
            }
        }
        return StringUtils.EMPTY;
    }

    @Override
    public long getLong() {
        if(isExist()){
            try {
                return getJCRProperty().getLong();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of nodedata " + toString(), e);
            }
        }
        return 0;
    }

    @Override
    public double getDouble() {
        if(isExist()){
            try {
                return getJCRProperty().getDouble();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of nodedata " + toString(), e);
            }
        }
        return 0;
    }

    @Override
    public Calendar getDate() {
        if(isExist()){
            try {
                return getJCRProperty().getDate();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of nodedata " + toString(), e);
            }
        }
        return null;
    }

    @Override
    public boolean getBoolean() {
        if(isExist()){
            try {
                return getJCRProperty().getBoolean();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of nodedata " + toString(), e);
            }
        }
        return false;
    }

    @Override
    public InputStream getStream() {
        if (isExist()){
            try {
                return getJCRProperty().getStream();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't read value of nodedata " + toString(), e);
            }
        }
        return null;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    protected Content getContentFromJCRReference() throws RepositoryException {
        return getHierarchyManager().getContent(getJCRProperty().getNode().getPath());
    }

    @Override
    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        getJCRNode().setProperty(name, value);
    }

    @Override
    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        getJCRNode().setProperty(name, value);
    }

    @Override
    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        getJCRNode().setProperty(name, value);
    }

    @Override
    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        getJCRNode().setProperty(name, value);
    }

    @Override
    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        getJCRNode().setProperty(name, value);
    }

    @Override
    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        getJCRNode().setProperty(name, value);    }

    @Override
    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        getJCRNode().setProperty(name, value);    }

    @Override
    public void setValue(Value[] value) throws RepositoryException, AccessDeniedException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        getJCRNode().setProperty(name, value);    }

    @Override
    public void setValue(Content value) throws RepositoryException, AccessDeniedException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        getJCRNode().setProperty(name, value.getJCRNode());
    }

    @Override
    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        throw new UnsupportedOperationException("This operation is only supported for node datas of type BINARY");
    }

    @Override
    public boolean isExist() {
        try {
            return getJCRNode().hasProperty(name);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save() throws RepositoryException {
        if(isExist()){
            getJCRProperty().save();
        }
    }

    @Override
    public void delete() throws RepositoryException {
        Access.tryPermission(getJCRNode().getSession(), Path.getAbsolutePath(this.getHandle()), Session.ACTION_SET_PROPERTY);
        if(isExist()){
            getJCRProperty().remove();
        }
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        if(isExist()){
            getJCRProperty().refresh(keepChanges);
        }
    }
}
