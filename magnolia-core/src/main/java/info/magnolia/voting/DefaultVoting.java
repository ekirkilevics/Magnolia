/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.voting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The highest returned vote is taken (8 or -10, ..) and returned. The voting
 * can return 0 if no voter has anything to say.
 *
 * @author philipp
 * @version $Id$
 *
 */
public class DefaultVoting implements Voting {

    Logger log = LoggerFactory.getLogger(DefaultVoting.class);
    public int vote(Voter[] voters, Object value) {
        int highestVote = 0;
        for (int i = 0; i < voters.length; i++) {
            Voter voter = voters[i];
            if(voter.isEnabled()){
                int vote  = voter.vote(value);
                if(log.isDebugEnabled()){
                    log.debug("voter [{}] fired {}", voter, Integer.toString(vote));
                }
                if(Math.abs(vote) > Math.abs(highestVote)){
                    highestVote = vote;
                    log.debug("highest vote is now {}", Integer.toString(highestVote));
                }
                // same value but not same sign
                else if (vote == -highestVote){
                    highestVote = Math.abs(vote);
                    log.debug("highest vote is now {}", Integer.toString(highestVote));
                }
            }
        }
        return highestVote;
    }
}
