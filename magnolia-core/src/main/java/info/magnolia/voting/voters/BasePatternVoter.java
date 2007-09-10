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
package info.magnolia.voting.voters;

import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;



/**
 * @author philipp
 * @version $Id$
 *
 */
public abstract class BasePatternVoter extends AbstractBoolVoter {

    private String pattern;
    private boolean inverse;
    private boolean autoTrueValue = true;

    public void init() {
        if(autoTrueValue){
            if(!isInverse()){
                setTrueValue(pattern.length());
            }
            else{
                setTrueValue(-pattern.length());
            }
        }
    }

    public void setTrueValue(int positiveVoteValue) {
        autoTrueValue = false;
        super.setTrueValue(positiveVoteValue);
    }

    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isInverse() {
        return this.inverse;
    }

    public void setInverse(boolean positive) {
        this.inverse = positive;
    }

    protected String resolveURIFromValue(Object value) {
        String uri = null;
        if(value instanceof String){
            uri = (String) value;
        }
        else{
            if(MgnlContext.hasInstance()){
                uri = MgnlContext.getAggregationState().getCurrentURI();
            }
            else{
                if (value instanceof HttpServletRequest) {
                    HttpServletRequest request = (HttpServletRequest) value;
                    uri = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
                }
            }
        }
        return uri;
    }

}