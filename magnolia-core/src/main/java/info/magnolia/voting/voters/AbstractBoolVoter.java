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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used to create boolean voters which can't return integer values. You can
 * stear the returned values by setting the trueValue and falseValue. To inverse
 * the result of the boolVote method use the not property.
 *
 * @author philipp
 * @version $Id$
 */
public abstract class AbstractBoolVoter extends BaseVoterImpl {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(AbstractBoolVoter.class);

    private int trueValue = 1;

    private int falseValue = 0;

    private boolean not;

    public int vote(Object value) {
        boolean vote = boolVote(value);
        if(not){
            vote = !vote;
        }
        return vote ? trueValue : falseValue;
    }

    public int getFalseValue() {
        return this.falseValue;
    }

    public void setFalseValue(int negativeVoteValue) {
        this.falseValue = negativeVoteValue;
    }

    public int getTrueValue() {
        return this.trueValue;
    }

    public void setTrueValue(int positiveVoteValue) {
        this.trueValue = positiveVoteValue;
    }

    abstract protected boolean boolVote(Object value);


    public boolean isNot() {
        return this.not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public String toString() {
        return super.toString() + ": " + (not?"not" : "");
    }

    public int getLevel() {
        return getTrueValue();
    }

    public void setLevel(int level) {
        setTrueValue(level);
    }
}
