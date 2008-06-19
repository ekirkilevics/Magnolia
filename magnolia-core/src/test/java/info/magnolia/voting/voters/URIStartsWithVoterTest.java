package info.magnolia.voting.voters;

import junit.framework.TestCase;


public class URIStartsWithVoterTest extends TestCase {

    public void testBasics(){
        URIStartsWithVoter voter = new URIStartsWithVoter();
        voter.setPattern("/test");
        voter.init();

        assertTrue(voter.vote("/test/huhu")>0);
        assertTrue(voter.vote("/gugu") == 0);
    }

    public void testNotVoter(){
        URIStartsWithVoter voter = new URIStartsWithVoter();
        voter.setPattern("/test");
        voter.setNot(true);
        voter.init();
        assertTrue(voter.vote("/test/huhu") == 0);
        assertTrue(voter.vote("/gugu") > 0);
    }

    public void testInverseVoter(){
        URIStartsWithVoter voter = new URIStartsWithVoter();
        voter.setPattern("/test");
        voter.setInverse(true);
        voter.init();
        assertTrue(voter.vote("/test/huhu") < 0);
        assertTrue(voter.vote("/gugu") == 0);
    }


}
