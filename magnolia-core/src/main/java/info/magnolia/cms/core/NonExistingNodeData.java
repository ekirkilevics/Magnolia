/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import javax.jcr.ItemNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.InputStream;
import java.util.Calendar;


/**
 * Represents an non-mutable empty node data. This is returned by
 * {@link Content#getNodeData(String)} in case the node data does not exist. This is the case
 * because jcr doesn't support empty properties.
 * 
 * All reading methods will return a default value which might be null in some cases. All writing
 * methods will throw an {@link ItemNotFoundException}.
 * 
 * @author pbaerfuss
 * @version $Id$
 * 
 */
public class NonExistingNodeData extends AbstractNodeData {

    public NonExistingNodeData(Content parent, String name) {
        super(parent, name);
    }

    @Override
    protected Content getContentFromJCRReference() throws RepositoryException {
        return null;
    }

    @Override
    public void delete() throws RepositoryException {
        throw new ItemNotFoundException("Can't delete a non-existing node data");
    }

    @Override
    public boolean getBoolean() {
        return false;
    }

    @Override
    public long getContentLength() {
        return 0;
    }

    @Override
    public Calendar getDate() {
        return null;
    }

    @Override
    public double getDouble() {
        return 0;
    }

    @Override
    public Property getJCRProperty() {
        return null;
    }

    @Override
    public long getLong() {
        return 0;
    }

    @Override
    public InputStream getStream() {
        return null;
    }

    @Override
    public String getString() {
        return "";
    }
    
    @Override
    public String getAttribute(String name) {
        return "";
    }

    @Override
    public int getType() {
        return PropertyType.UNDEFINED;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public Value[] getValues() {
        return null;
    }

    @Override
    public boolean isExist() {
        return false;
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
    }

    @Override
    public void save() throws RepositoryException {
        throw new ItemNotFoundException("Can't save a non-existing node data");
    }

    @Override
    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

    @Override
    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

    @Override
    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

    @Override
    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

    @Override
    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

    @Override
    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

    @Override
    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

    @Override
    public void setValue(Content value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

    @Override
    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

    @Override
    public void setValue(Value[] value) throws RepositoryException, AccessDeniedException {
        throw new ItemNotFoundException("Can't set a non-existing node data");
    }

}
