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
package info.magnolia.freemarker;

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.cms.security.User;

/**
 * Exposes User instances to freemarker in such a way that getter methods are tried first,
 * then getProperty() is used. ie ${user.name} internally calls the getName() method,
 * and ${user.fooBar} will eventually return the value of user.getProperty("fooBar").
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class UserModel extends BeanModel {
    private final User user;

    UserModel(User user, MagnoliaContentWrapper wrapper) {
        super(user, wrapper);
        this.user = user;
    }

    public TemplateModel get(String key) throws TemplateModelException {
        final TemplateModel result = super.get(key);
        if (result != null) {
            return result;
        }
        return new SimpleScalar(user.getProperty(key));
    }
}
