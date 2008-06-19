/**
 * This file Copyright (c) 2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.voting.voters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.voting.Voter;

/**
 * Conditional voter. Use the condition, then and otherwise voter.
 * @author pbracher
 *
 */
public class IfVoter extends BaseVoterImpl {

    private static Logger log = LoggerFactory.getLogger(IfVoter.class);

    private Voter condition;

    private Voter then;

    private Voter otherwise;

    public int vote(Object value) {
        boolean isTrue = condition.vote(value)>0;
        log.debug("test of {} was {}", this.condition, Boolean.valueOf(isTrue));
        int outcome;
        if(isTrue){
            outcome = then.vote(value);
        }
        else{
            if(otherwise != null){
                outcome = otherwise.vote(value);
            }
            outcome = 0 ;
        }
        log.debug("result is {}", Integer.valueOf(outcome));
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
