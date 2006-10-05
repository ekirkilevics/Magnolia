package info.magnolia.maven.bootstrap.post;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.exception.NestableException;

/**
 * We use this util to keep class SetActivationStatus independent of any magnolia classes
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public class ActivationStatusUtil {
    
    public static void setStatus(final String repository, final String path, final boolean activated) throws Exception{
    
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(repository);
    
        try {
            Content root = hm.getContent(path);
            ContentUtil.visit(root, new ContentUtil.Visitor(){
                public void visit(Content node) throws Exception {
                    if(activated){
                        node.getMetaData().setActivated();
                    }
                    else{
                        node.getMetaData().setUnActivated();
                    }
                    node.getMetaData().setLastActivationActionDate();
                    node.save();
                }
            });
        }
        catch (Exception e) {
            throw new NestableException("can't read path ", e);
        }
    }

}
