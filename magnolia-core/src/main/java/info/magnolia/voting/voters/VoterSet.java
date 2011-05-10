/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
import info.magnolia.voting.Voting;

import org.apache.commons.lang.ArrayUtils;

/**
 * Can take a set of voters. The voting can be set as well as the returned
 * level. If the level is not set (0) the votings result is returned.
 *
 * @author pbracher
 *
 */
public class VoterSet extends BaseVoterImpl {

    /**
     * AND/OR or null for the default voting. See {@link #getVoting()}.
     */
    String op;

    /**
     * If 0 the outcome of the voting is used.
     */
    int level;

    /**
     * Outcome will be inverse if true.
     */
    boolean not = false;

    private Voter[] voters = new Voter[0];

    private Voting voting;

    public Voter[] getVoters() {
        return voters;
    }

    public void addVoter(Voter voter){
        if(voter.isEnabled()){
            voters = (Voter[]) ArrayUtils.add(voters, voter);
        }
    }

    @Override
    public int vote(Object value) {
        int outcome = getVoting().vote(voters, value);
        if(level != 0){
            if(outcome < 0){
                outcome = -level;
            }
            if(outcome > 0){
                outcome = level;
            }
        }
        if(not){
            outcome = -outcome;
        }
        return outcome;
    }

    public Voting getVoting() {
        if(voting == null){
            if("AND".equalsIgnoreCase(op)){
                voting = Voting.AND;
            }
            else if("OR".equalsIgnoreCase(op)){
                voting = Voting.OR;
            }
            else{
                voting = Voting.HIGHEST_LEVEL;
            }
        }
        return voting;
    }

    public void setVoting(Voting voting) {
        this.voting = voting;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return super.toString() + " set: " + (not ? "not " : "") + ArrayUtils.toString(voters);
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }
}
