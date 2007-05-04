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
package info.magnolia.voting;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class DefaultVoting implements Voting {

    public int vote(Voter[] voters, Object value) {
        int highestVote = 0;
        for (int i = 0; i < voters.length; i++) {
            Voter voter = voters[i];
            if(voter.isEnabled()){
                int vote  = voter.vote(value);

                if(Math.abs(vote) > Math.abs(highestVote)){
                    highestVote = vote;
                }
                // same value but not same sign
                else if (vote == -highestVote){
                    highestVote = Math.abs(vote);
                }
            }
        }
        return highestVote;
    }
}
