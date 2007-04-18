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

import info.magnolia.cms.core.Content;

/**
 * This state contains three stacks. This allows the transformer to know the full state created during the
 * transoformation process. A stack for classes, beans, and content nodes are kept.
 * <p/>
 * It is the processors job to populate this state.
 *
 * @author philipp
 * @version $Id$
 */
public interface TransformationState {

    public void pushType(TypeDescriptor type);

    public void popType();

    public TypeDescriptor getCurrentType();

    public TypeDescriptor peekType(int pos);

    public void pushBean(Object bean);

    public void popBean();

    public Object getCurrentBean();

    public Object peekBean(int pos);

    public void pushContent(Content node);

    public void popContent();

    public Content getCurrentContent();

    public Content peekContent(int pos);

    /**
     * Is the value of the biggest stack size
     */
    public int getLevel();

}
