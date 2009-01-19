/**
 * This file Copyright (c) 2008-2009 Magnolia International
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

import info.magnolia.voting.Voter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conditional voter. If the "condition" voter is positive, returns the "then" voter's value,
 * otherwise returns the "otherwise" voter's value.
 *
 * @author pbracher
 */
public class IfVoter extends BaseVoterImpl {
    private static final Logger log = LoggerFactory.getLogger(IfVoter.class);

    private Voter condition;

    private Voter then;

    private Voter otherwise = new NullVoter();

    public int vote(Object value) {
        final boolean isTrue = condition.vote(value) > 0;
        log.debug("test of {} was {}", this.condition, Boolean.valueOf(isTrue));
        int outcome;
        if (isTrue) {
            outcome = then.vote(value);
        } else {
            outcome = otherwise.vote(value);
        }
        log.debug("result is {}", Integer.toString(outcome));
        return outcome;

    }

    public Voter getCondition() {
        return condition;
    }

    public void setCondition(Voter condition) {
        this.condition = condition;
    }

    public Voter getThen() {
        return then;
    }

    public void setThen(Voter then) {
        this.then = then;
    }

    public Voter getOtherwise() {
        return otherwise;
    }

    public void setOtherwise(Voter otherwise) {
        this.otherwise = otherwise;
    }

    public String toString() {
        return super.toString() + "if [" + condition + "] then: [" + then + "] otherwise: [" + otherwise + "]";
    }

}
