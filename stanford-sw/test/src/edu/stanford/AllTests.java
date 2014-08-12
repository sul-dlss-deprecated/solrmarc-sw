package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	AccessTests.class,
    	AllSearchTests.class,
    	AuthorTests.class,
        AuthorTitleMappingTests.class,
        BibOnlyXmlTests.class,
        CallNumberTests.class,
        CallNumFacetSimTests.class,
        CallNumLaneTests.class,
        CallNumLCLoppingUnitTests.class,
        CallNumLibLocComboLopTests.class,
        CallNumLongestComnPfxTests.class,
        CallNumLoppingUnitTests.class,
        CallNumUtilsLoppingUnitTests.class,
        DatabaseAZSubjectTests.class,
        DiacriticTests.class,
        FormatDatabaseTests.class,
        FormatMainTests.class,
        FormatOldTests.class,
        FormatPhysicalTests.class,
        GenreTests.class,
        GeographicFacetTests.class,
        IncrementalUpdateTests.class,
        ItemDisplayCallnumLoppingTests.class,
        ItemInfoTests.class,
        ItemLACTests.class,
        ItemMissingTests.class,
        ItemNoCallNumberTests.class,
        ItemObjectTests.class,
        ItemOnlineTests.class,
        ItemSkippedTests.class,
        ItemsSplitTests.class,
        ItemUtilsUnitTests.class,
        LanguageTests.class,
        MergeMhldFldsIntoBibsReaderTests.class,
        MhldDisplayUnitTests.class,
        MhldMappingTests.class,
        MiscellaneousFieldTests.class,
        NoteFieldsTests.class,
        PhysicalTests.class,
        PhysicsLibraryItemTests.class,
        PublicationTests.class,
        PublicationUtilsUnitTests.class,
        SeriesTests.class,
        StandardNumberTests.class,
        SubjectSearchTests.class,
        SubjectTests.class,
        TitleSearchTests.class,
        TitleSearchVernTests.class,
        TitleTests.class,
        UrlTests.class,
        VernFieldsTests.class,
        WordDelimiterTests.class
        })


public class AllTests
{
}
