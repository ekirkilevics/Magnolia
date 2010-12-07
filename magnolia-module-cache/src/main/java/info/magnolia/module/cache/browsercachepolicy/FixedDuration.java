/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.cache.browsercachepolicy;

import info.magnolia.module.cache.BrowserCachePolicyResult;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.voting.Voter;
import info.magnolia.voting.voters.FalseVoter;
import info.magnolia.voting.voters.TrueVoter;


/**
 * Uses a fix expiration time (in minutes) if {@link #voter} votes positive or sets a far future
 * expiration - more than a year - if {@link #farFutureVoter} votes true. Default to 30 minutes.
 *
 * @author pbracher
 * @version $Revision$ ($Author$)
 */
public class FixedDuration extends AbstractVoterBased {


    /**
     * Far future expiration is a year.
     */
    private static final long ONE_YEAR_IN_MILLISECONDS = 365L * 24 * 60 * 60 *  1000;

    private static final int MINUTE_IN_MILLIS = 60 * 1000;

    /**
     * Used to test if the request should be cached by the browser. Default {@link TrueVoter}.
     */
    private Voter voter = new TrueVoter();

    /**
     * Used to test if the request should be cache for ever. Default {@link FalseVoter}.
     */
    private Voter farFutureVoter = new FalseVoter();

    private int expirationMinutes = 30;

    @Override
    protected BrowserCachePolicyResult getPositiveVoteResult(CachePolicyResult cachePolicyResult) {
        if(farFutureVoter.vote(cachePolicyResult)>0){
            return new BrowserCachePolicyResult(System.currentTimeMillis() + ONE_YEAR_IN_MILLISECONDS);
        }
        else if(voter.vote(cachePolicyResult)>0){
            // cast to long as the operation might exceed the int range
            long expirationInMilliseconds = (long) this.getExpirationMinutes() * MINUTE_IN_MILLIS;
            return new BrowserCachePolicyResult(System.currentTimeMillis() + expirationInMilliseconds);
        }
        else{
            return BrowserCachePolicyResult.NO_CACHE;
        }
    }

    public int getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(int expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public Voter getVoter() {
        return voter;
    }

    public void setVoter(Voter voter) {
        this.voter = voter;
    }

    public Voter getFarFutureVoter() {
        return farFutureVoter;
    }

    public void setFarFutureVoter(Voter farFutureVoter) {
        this.farFutureVoter = farFutureVoter;
    }

}
