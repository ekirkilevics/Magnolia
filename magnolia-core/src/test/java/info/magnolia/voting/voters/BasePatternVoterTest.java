package info.magnolia.voting.voters;

import junit.framework.TestCase;


public class BasePatternVoterTest extends TestCase {

    public void testInitWithoutPattern(){
        BasePatternVoter voter = new BasePatternVoter() {
            protected boolean boolVote(Object value) {
                return false;
            }};
        voter.init();
    }
}
