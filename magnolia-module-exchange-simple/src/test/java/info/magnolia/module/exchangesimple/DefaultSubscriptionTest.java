package info.magnolia.module.exchangesimple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DefaultSubscriptionTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testVoteEnabled() {
        DefaultSubscription subscription = new DefaultSubscription();
        // enabled by defauls
        assertTrue(subscription.isEnabled());
        subscription.setEnabled(false);
        assertFalse(subscription.isEnabled());
    }

    @Test
    public void testVoteNull() {
        DefaultSubscription subscription = new DefaultSubscription();
        subscription.setFromURI(null);
        subscription.setToURI(null);
        assertEquals(-1, subscription.vote("/bla/boo"));
    }

    @Test
    public void testVoteFail() {
        DefaultSubscription subscription = new DefaultSubscription();
        subscription.setFromURI("/foo/bar");
        assertEquals(-1, subscription.vote("/bla/boo"));
    }

    @Test
    public void testVoteMatch() {
        DefaultSubscription subscription = new DefaultSubscription();
        subscription.setFromURI("/foo/bar");
        // match value is determined by length
        assertEquals(8, subscription.vote("/foo/bar"));
        assertEquals(9, subscription.vote("/foo/bar/"));
        subscription.setFromURI("/foo/bar/");
        // this will not match!! we do not strip slash from set uri
        assertEquals(-1, subscription.vote("/foo/bar"));
        assertEquals(9, subscription.vote("/foo/bar/"));
    }

}
