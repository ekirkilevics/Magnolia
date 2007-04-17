package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;

import org.apache.commons.collections.ArrayStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TransformationStateImpl implements TransformationState {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(TransformationStateImpl.class);

    protected ArrayStack classStack = new ArrayStack();

    protected ArrayStack beanStack = new ArrayStack();

    protected ArrayStack contentStack = new ArrayStack();

    public Object getCurrentBean() {
        return beanStack.peek();
    }

    public Class getCurrentClass() {
        return (Class) classStack.peek();
    }

    public Content getCurrentContent() {
        return (Content) contentStack.peek();
    }

    public Object peekBean(int pos) {
        return beanStack.peek(pos);
    }

    public Class peekClass(int pos) {
        return (Class) classStack.peek(pos);
    }

    public Content peekContent(int pos) {
        return (Content) contentStack.peek(pos);
    }

    public void popBean() {
        beanStack.pop();
    }

    public void popClass() {
        classStack.pop();
    }

    public void popContent() {
        contentStack.pop();
    }

    public void pushBean(Object bean) {
        beanStack.push(bean);
    }

    public void pushClass(Class klass) {
        classStack.push(klass);
    }

    public void pushContent(Content node) {
        contentStack.push(node);
    }

    public int getLevel() {
        return Math.max(Math.max(classStack.size(), beanStack.size()), contentStack.size());
    }
}
