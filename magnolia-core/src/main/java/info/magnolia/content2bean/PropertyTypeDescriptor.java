/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.content2bean;

import java.lang.reflect.Method;

/**
 * Describes a property. Type, type used for ceating concrete instances, is it a map/collection, adder method, ...
 * @author philipp
 * @version $Id$
 *
 */
public class PropertyTypeDescriptor {
    private String name;
    private TypeDescriptor type;
    private TypeDescriptor collectionEntryType;
    private TypeDescriptor collectionKeyType;
    private Method addMethod;

    public Method getAddMethod() {
        return this.addMethod;
    }

    public void setAddMethod(Method addMethod) {
        this.addMethod = addMethod;
    }

    public TypeDescriptor getCollectionEntryType() {
        return this.collectionEntryType;
    }

    public void setCollectionEntryType(TypeDescriptor collectionEntryType) {
        this.collectionEntryType = collectionEntryType;
    }

    public TypeDescriptor getCollectionKeyType() {
        return this.collectionKeyType;
    }

    public void setCollectionKeyType(TypeDescriptor collectionKeyType) {
        this.collectionKeyType = collectionKeyType;
    }

    public boolean isCollection() {
        return getType().isCollection();
    }

    public boolean isMap() {
        return getType().isMap();
    }

    public TypeDescriptor getType() {
        return this.type;
    }

    public void setType(TypeDescriptor type) {
        this.type = type;
    }


    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }
}