package info.magnolia.content2bean.impl;

import info.magnolia.cms.core.Content;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;

import org.apache.commons.collections.ArrayStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TransformationStateImpl implements TransformationState {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(TransformationStateImpl.class);

    protected ArrayStack typeStack = new ArrayStack();

    protected ArrayStack beanStack = new ArrayStack();

    protected ArrayStack contentStack = new ArrayStack();

    public Object getCurrentBean() {
        return beanStack.peek();
    }

    public TypeDescriptor getCurrentType() {
        return (TypeDescriptor) typeStack.peek();
    }

    public Content getCurrentContent() {
        return (Content) contentStack.peek();
    }

    public Object peekBean(int pos) {
        return beanStack.peek(pos);
    }

    public TypeDescriptor peekType(int pos) {
        return (TypeDescriptor) typeStack.peek(pos);
    }

    public Content peekContent(int pos) {
        return (Content) contentStack.peek(pos);
    }

    public void popBean() {
        beanStack.pop();
    }

    public void popType() {
        typeStack.pop();
    }

    public void popContent() {
        contentStack.pop();
    }

    public void pushBean(Object bean) {
        beanStack.push(bean);
    }

    public void pushType(TypeDescriptor type) {
        typeStack.push(type);
    }

    public void pushContent(Content node) {
        contentStack.push(node);
    }

    public int getLevel() {
        return Math.max(Math.max(typeStack.size(), beanStack.size()), contentStack.size());
    }
}
