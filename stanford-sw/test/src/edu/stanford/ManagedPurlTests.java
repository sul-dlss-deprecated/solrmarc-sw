package edu.stanford;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's managed Purls from StanfordSync
 * @author Laney McGlohon
 */
public class ManagedPurlTests extends AbstractStanfordTest
{
	private final String testDataFname = "managedPurlTests.xml";

@Before
	public final void setup()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshIx(testDataFname);
	}

	/**
	 * test managed_purl_urls field
	 */
@Test
	public final void testManagedPurlUrls()
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "managed_purl_urls";

		assertDocHasFieldValue("managedPurlItem1Collection", fldName, "https://purl.stanford.edu/nz353cp1092");
		assertDocHasFieldValue("managedPurlItem3Collections", fldName, "https://purl.stanford.edu/wd297xz1362");
		assertDocHasFieldValue("managedPurlCollection", fldName, "https://purl.stanford.edu/ct961sj2730");
		assertDocHasFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "https://purl.stanford.edu/ct961sj2730");

		assertDocHasNoField("NoManagedPurlItem", fldName);
		assertDocHasNoFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "https://purl.stanford.edu/yy000zz0000");
	}

	/**
	 * Test method for display_type.
	 */
@Test
	public final void testDisplayType() throws IOException, ParserConfigurationException, SAXException
	{
		String fldName = "display_type";

		assertDocHasFieldValue("managedPurlItem3Collections", fldName, "file");
		assertDocHasFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "image");

		assertDocHasFieldValue("NoManagedPurlItem", fldName, "sirsi");
		assertDocHasNoFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "book");
	}

/**
 * Test method for fileId.
 */
@Test
	public final void testSetFileId() throws IOException, ParserConfigurationException, SAXException
	{
		String fldName = "file_id";

		assertDocHasFieldValue("managedPurlItem1Collection", fldName, "file1.jpg");
		assertDocHasFieldValue("managedPurlItem3Collections", fldName, "file1.jpg");
		assertDocHasNoField("managedPurlCollection", fldName);
		assertDocHasNoField("ManagedAnd2UnmanagedPurlCollection", fldName);
		assertDocHasNoField("NoManagedPurlItem", fldName);
	}

	/**
	 * Test collection field contents
	 */
@Test
	public final void testCollection()
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "collection";

		assertDocHasFieldValue("managedPurlItem1Collection", fldName, "9615156");
		assertDocHasFieldValue("managedPurlItem3Collections", fldName, "yy000zz1111");
		assertDocHasFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "sirsi");
		assertDocHasFieldValue("NoManagedPurlItem", fldName, "sirsi");
	}

	/**
	 * Test collection_with_title field contents
	 */
@Test
	public final void testCollectionWithTitle()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "collection_with_title";

		assertDocHasFieldValue("managedPurlItem1Collection", fldName, "9615156-|-Francis E. Stafford photographs, 1909-1933");
		assertDocHasFieldValue("managedPurlItem3Collections", fldName, "yy000zz1111-|-Test Collection2, 1968-2015");
		assertDocHasNoField("managedPurlCollection", fldName);
		assertDocHasNoField("ManagedAnd2UnmanagedPurlCollection", fldName);
		assertDocHasNoField("NoManagedPurlItem", fldName);
	}

	/**
	 * test collection_type
	 */
@Test
	public final void testCollectionType()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "collection_type";
		assertDocHasFieldValue("managedPurlCollection", fldName, "Digital Collection");
		assertDocHasFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "Digital Collection");
		assertDocHasNoField("NoManagedPurlItem", fldName);
		assertDocHasNoField("managedPurlItem1Collection", fldName);
		assertDocHasNoField("managedPurlItem3Collections", fldName);
	}

	/**
	 * test set
	 */
@Test
	public final void testSet()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "set";
		assertDocHasFieldValue("managedPurlItem1Set1Collection", fldName, "123456789");
		assertDocHasNoField("managedPurlItem1Collection", fldName);
		assertDocHasNoField("managedPurlCollection", fldName);
		assertDocHasNoField("ManagedAnd2UnmanagedPurlCollection", fldName);
		assertDocHasNoField("NoManagedPurlItem", fldName);
	}

	/**
	 * test set_with_title
	 */
@Test
	public final void testSetsWithTitles()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "set_with_title";
		assertDocHasFieldValue("managedPurlItem1Set1Collection", fldName, "123456789-|-Test Set, 1963-2015");
		assertDocHasFieldValue("managedPurlItem3Sets2Collections", fldName,"aa000bb1111-|-Test set1");
		assertDocHasFieldValue("managedPurlItem3Sets2Collections", fldName,"yy000zz1111-|-Test set2");
		assertDocHasFieldValue("managedPurlItem3Sets2Collections", fldName,"987654-|-Test set3");
		assertDocHasNoField("managedPurlCollection", fldName);
		assertDocHasNoField("ManagedAnd2UnmanagedPurlCollection", fldName);
		assertDocHasNoField("NoManagedPurlItem", fldName);

	}
}
