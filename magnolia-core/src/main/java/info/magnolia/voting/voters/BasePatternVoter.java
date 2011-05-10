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
package info.magnolia.voting.voters;

import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * Voters which uses the urls to match against the pattern. The returned vote is
 * the length of the pattern. This allows overstearing less precise votes (like
 * allow /something/* but deny /somthing/otherthing/*). You can use the inverse
 * property which will then return the negative value on a match. This is not
 * the same thing as using the not property which will then vote if the pattern
 * does not match at all.
 *
 * @author philipp
 * @version $Id$
 *
 */
public abstract class BasePatternVoter extends AbstractBoolVoter {

    // use default value to prevent NPE when initialized before pattern is set.
    private String pattern = StringUtils.EMPTY;
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

    @Override
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

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
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

    @Override
    public String toString() {
        return super.toString() + " pattern: " + this.getPattern();
    }

}
