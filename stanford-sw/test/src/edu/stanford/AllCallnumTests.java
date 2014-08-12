package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CallNumberTests.class,
        CallNumFacetSimTests.class,
        CallNumLaneTests.class,
        CallNumLCLoppingUnitTests.class,
        CallNumLibLocComboLopTests.class,
        CallNumLongestComnPfxTests.class,
        CallNumLoppingUnitTests.class,
        CallNumUtilsLoppingUnitTests.class,
        ItemDisplayCallnumLoppingTests.class,
        ItemInfoTests.class,
        ItemMissingTests.class,
        ItemNoCallNumberTests.class,
        ItemObjectTests.class,
        ItemOnlineTests.class,
        ItemUtilsUnitTests.class
//        org.solrmarc.index.CallNumberUnitTests.class
        })


public class AllCallnumTests
{
}
