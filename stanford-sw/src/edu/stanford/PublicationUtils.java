/*
 * Copyright (c) 2012.  The Board of Trustees of the Leland Stanford Junior University. All rights reserved.
 *
 * Redistribution and use of this distribution in source and binary forms, with or without modification, are permitted provided that: The above copyright notice and this permission notice appear in all copies and supporting documentation; The name, identifiers, and trademarks of The Board of Trustees of the Leland Stanford Junior University are not used in advertising or publicity without the express prior written permission of The Board of Trustees of the Leland Stanford Junior University; Recipients acknowledge that this distribution is made available as a research courtesy, "as is", potentially with defects, without any obligation on the part of The Board of Trustees of the Leland Stanford Junior University to provide support, services, or repair;
 *
 * THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/
package edu.stanford;

import java.util.*;

import org.apache.log4j.Logger;
import org.solrmarc.tools.*;
import org.marc4j.marc.*;

import edu.stanford.enumValues.PubDateGroup;

/**
 * Publication Data Utility methods for StanfordIndexer in SolrMarc project
 *
 * @author Naomi Dushay
 */
public class PublicationUtils {

	static int CURRENT_YEAR_AS_INT = Calendar.getInstance().get(Calendar.YEAR);
	private static String CURRENT_YEAR_AS_STR = Integer.toString(CURRENT_YEAR_AS_INT);

	private static int EARLIEST_VALID_YEAR = 500;
	private static int LATEST_VALID_YEAR = CURRENT_YEAR_AS_INT + 10;

	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */
	private PublicationUtils(){ }


	/**
	 * Gets 260ab and 264ab but ignore s.l in 260a and s.n. in 260b
	 * @param vf26xList - a List of the 260 and 264 fields as VariableField objects
	 * @return Set of strings containing values in 260ab and 264ab, without
	 *  s.l in 260a and without s.n. in 260b
	 */
    @SuppressWarnings("unchecked")
	static Set<String> getPublication(List<VariableField> vf26xList)
    {
		Set<String> resultSet = new LinkedHashSet<String>();
		for (VariableField vf26x : vf26xList)
		{
			DataField df26x = (DataField) vf26x;
			List<Subfield> subFlds = df26x.getSubfields();
			StringBuilder buffer = new StringBuilder("");
			for (Subfield sf : subFlds)
			{
				char sfcode = sf.getCode();
				String sfdata = sf.getData();
				boolean addIt = false;
				if (sfcode == 'a' && !sfdata.matches("(?i).*s\\.l\\..*") && !sfdata.matches("(?i).*place of .* not identified.*"))
					addIt = true;
				else if (sfcode == 'b' && !sfdata.matches("(?i).*s\\.n\\..*") && !sfdata.matches("(?i).*r not identified.*"))
					addIt = true;
				if (addIt)
				{
					if (buffer.length() > 0)
						buffer.append(" ");
					buffer.append(sfdata);
				}
			}
			if (buffer.length() > 0)
				resultSet.add(Utils.cleanData(buffer.toString()));
		}
		return resultSet;
	}

	/**
	 * gets a value from 008 bytes 7-10 if value wasn't already assigned to
	 * one or more of these fields:
	 *    publication_year_isi = custom, get008Date1(est)
	 *    beginning_year_isi = custom, get008Date1(cdmu)
	 *    earliest_year_isi = custom, get008Date1(ik)
	 *    earliest_poss_year_isi = custom, get008Date1(q)
	 *    release_year_isi = custom, get008Date1(p)
	 *    reprint_year_isi = custom, get008Date1(r)
	 *    production_year_isi = custom, get008Date2(p)
	 *    original_year_isi = custom, get008Date2(r)
	 *    copyright_year_isi = custom, get008Date2(t)
	 *
	 * @param cf008 - 008 field as a ControlField object
	 **/
	static String getOtherYear(ControlField cf008, String id, Logger logger)
	{
		if (get008Date1(cf008, "cdeikmpqrstu") == null && get008Date2(cf008, "dikmpqrt") == null && cf008 != null && cf008.getData().length() >= 11)
		{
			String cf008date1 = cf008.getData().substring(7, 11);
			if (cf008date1 != null)
			{
				String result = PublicationUtils.get3or4DigitYear(cf008date1, "0");
				if (yearIsValid(result))
				{
					if (result != null && logger != null)
						logger.warn("Unexpectedly found usable date1 in 008 for record: " + id + ": " + cf008.getData());
					return result;
				}
			}
		}
		return null;
	}


	/**
	 * returns the publication date from a record, if it is present and not
     *  beyond the current year + 1 (and not earlier than EARLIEST_VALID_YEAR if it is a
     *  4 digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
     * Side Effects:  errors in pub date are logged
     * @param date008 - characters 7-10 (0 based index) in 008 field
	 * @param date260c - the date string extracted from the 260c field
	 * @param vf264list  - a List of 264 fields as VariableField objects
	 * @param id - record id for error messages
	 * @param logger - the logger for error messages
	 * @return String containing publication date, or null if none
	 * @deprecated not using pub_date for facet or display with date slider implemented
	 */
	static String getPubDate(final String date008, String date260c, List<VariableField> vf264list, String id, Logger logger)
	{
		if (date008 != null) {
			String errmsg = "Bad Publication Date in record " + id + " from 008/07-10: " + date008;
			if (DateUtils.isdddd(date008)) {
				String result = getValidPubDateStr(date008, date260c, vf264list);
				if (result != null)
					return result;
				else
					logger.error(errmsg);
			} else if (DateUtils.isdddu(date008)) {
				int myFirst3 = Integer.parseInt(date008.substring(0, 3));
				int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
				if (myFirst3 <= currFirst3)
					return date008.substring(0, 3) + "0s";
				else
					logger.error(errmsg);
			} else if (DateUtils.isdduu(date008)) {
				int myFirst2 = Integer.parseInt(date008.substring(0, 2));
				int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
				if (myFirst2 <= currFirst2)
					return DateUtils.getCenturyString(date008.substring(0, 2));
				else
					logger.error(errmsg);
			}
			// 008 is blanks or something like that
			return getValidPubDateStr(date008, date260c, vf264list);

		}

		return null;
	}

	/**
	 * 2013-06-26 this is the OLD way
     * returns the sortable publication date from a record, if it is present
     *  and not beyond the current year + 1, and not earlier than EARLIEST_VALID_YEAR if
     *   a four digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     * @param date008 - characters 7-10 (0 based index) in 008 field
	 * @param date260c - the date string extracted from the 260c field
	 * @param vf264list  - a List of 264 fields as VariableField objects
	 * @return String containing publication date, or null if none
	 * @deprecated handling 008 dates a bit differently now
	 */
	static String getPubDateSort(String date008, String date260c, List<VariableField> vf264list)
	{
		if (date008 != null) {
			// hyphens sort before 0, so the lexical sorting will be correct. I
			// think.
			if (DateUtils.isdddd(date008))
				return getValidPubDateStr(date008, date260c, vf264list);
			else if (DateUtils.isdddu(date008)) {
				int myFirst3 = Integer.parseInt(date008.substring(0, 3));
				int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
				if (myFirst3 <= currFirst3)
					return date008.substring(0, 3) + "-";
			} else if (DateUtils.isdduu(date008)) {
				int myFirst2 = Integer.parseInt(date008.substring(0, 2));
				int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
				if (myFirst2 <= currFirst2)
					return date008.substring(0, 2) + "--";
			}
		}

		return null;
	}


	/**
     * returns the sortable publication date from a record, if it is present
     *  and not beyond the current year + 1, and not earlier than EARLIEST_VALID_YEAR if
     *   a four digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     *
     *  use 008 date1 if it is 3 or 4 digits and in valid range
     *  If not, check for a 4 digit date in the 264c if 2nd ind is 1
     *  If not, take usable 260c date
     *  If not, take any other usable date in the 264c
     *  If still without date, look at 008 date2
     *
     *  If still without date, use dduu from date 1 as dd00
     *  If still without date, use dduu from date 2 as dd99
     *
     * @param cf008 - 008 field as a ControlField object
	 * @param date260c - the date string extracted from the 260c field
	 * @param vf264list  - a List of 264 fields as VariableField objects
	 * @return String containing publication date, or null if none
	 */
	static String getPubDateSort(ControlField cf008, String date260c, List<VariableField> vf264list)
	{
		String possRawDate1 = null;
		String possRawDate2 = null;
		if (cf008 != null && cf008.getData().length() >= 11)
		{
			// use date1 from 008 if it is 3 or 4 digits and in range
			possRawDate1 = cf008.getData().substring(7, 11);
			String date1Str = get3or4DigitYear(possRawDate1, "0");
			if (yearIsValid(date1Str))
				return date1Str;

			// use date2 from 008 if it is 3 or 4 digits and in range
			if (cf008.getData().length() >= 15)
			{
				possRawDate2 = cf008.getData().substring(11, 15);
				String date2Str = get3or4DigitYear(possRawDate2, "9");
				if (yearIsValid(date2Str))
					return date2Str;
			}
		}

		// look for a result in 264c and/or 260c
		String result = getValidPubDateStr(null, date260c, vf264list);
		if (result != null)
			return result;

		// use date 1 from 008 if it is 2 digits and in range
		if (possRawDate1 != null && DateUtils.isdduu(possRawDate1))
		{
			int myFirst2 = Integer.parseInt(possRawDate1.substring(0, 2));
			int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
			if (myFirst2 <= currFirst2)
				// hyphens sort before 0, so the lexical sorting will be correct. I think.
				return possRawDate1.substring(0, 2) + "--";
		}

		// use date 2 from 008 if it is 2 digits and in range
		if (possRawDate2 != null && DateUtils.isdduu(possRawDate2))
		{
			int myFirst2 = Integer.parseInt(possRawDate2.substring(0, 2));
			int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
			if (myFirst2 <= currFirst2)
				// colons sort after 9, so the lexical sorting will be correct. I think.
				return possRawDate2.substring(0, 2) + "--";
		}
		return null;
	}


	/**
     * returns the publication year(s) from a record, based on 008 bytes 6-15
     *  (see https://jirasul.stanford.edu/jira/browse/SW-666)
     *  also ensures years are not earlier than EARLIEST_VALID_YEAR
     *   and not later than the current year + 1
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     * @param cf008 - 008 field as a ControlField object
	 * @param df260subcSet - Set of Strings containing values in 260 sub c
	 * @return Set<String> containing publication years, or empty set if none
	 */
	static Set<String> getPubDateSliderVals(ControlField cf008, Set<String> df260subcSet, String id, Logger logger)
	{
		Set<String> results = new HashSet<String>();
		if (cf008 != null && cf008.getData().length() >= 15)
		{
			char c6 = cf008.getData().charAt(6);
			String rawDate1 = cf008.getData().substring(7, 11);
			String date1Str = get3or4DigitYear(rawDate1, "0");
			int date1Int = -1;
			if (date1Str != null)
				date1Int = Integer.valueOf(date1Str);
			String rawDate2 = cf008.getData().substring(11, 15);
			String date2Str = get3or4DigitYear(rawDate2, "9");
			int date2Int = -1;
			if (date2Str != null)
				date2Int = Integer.valueOf(date2Str);

			switch (c6)
			{
				case 'd':
				case 'i':
				case 'k':
				case 'q':
					// index start, end and years between
					if (date1Str != null)
						results.add(date1Str);
					if (date2Str != null)
						results.add(date2Str);
					if (date1Int != -1 && date2Int != -1)
					{
						for (int year = date1Int; year < date2Int; year++)
							results.add(String.valueOf(year));
					}
					break;
				case 'm':
					if (date1Str != null)
						results.add(date1Str);
					if (!rawDate2.equals("9999") && date2Str != null)
					{
						// index end year and years between
						results.add(date2Str);
						if (date1Int != -1 && date2Int != -1)
							for (int year = date1Int; year < date2Int; year++)
								results.add(String.valueOf(year));
					}
					break;
				case 'c':
					// if open range, index all thru present
					if (date1Str != null && PublicationUtils.yearIsValid(date1Str))
						results.add(date1Str);
					if (rawDate2 != null && rawDate2.equals("9999") // if open range
							&& PublicationUtils.yearIsValid(date1Str))
					{
						for (int year = date1Int; year <= CURRENT_YEAR_AS_INT; year++)
							results.add(String.valueOf(year));
					}
					else if (rawDate2 != null && !rawDate2.equals("9999")
							&& PublicationUtils.yearIsValid(date1Str))
					{
						// log message if we have a usable date1Str
						if (logger != null)
							logger.warn("Unexpected date2 for type c after usable date1 in 008 for record " + id + ": " + cf008.getData());
					}
					else if (rawDate2 != null && !rawDate2.equals("9999"))
						// log messages if we don't have a usable date1Str
						if (logger != null)
							logger.warn("Unexpected date2 for type c in 008 for record " + id + ": " + cf008.getData());
					break;
				case 'p':
				case 'r':
				case 't':
					// index only start and end
					if (date1Str != null)
						results.add(date1Str);
					if (date2Str != null)
						results.add(date2Str);
					break;
				case 'e':
				case 's':
				case 'u':
					if (date1Str != null)
						results.add(date1Str);
					break;
				case 'b':
				case 'n':
				case '|':
				default:
					// we'll take date1 if it's valid, even if the 008 is bad
					// NOTE:  error message is logged in getOtherYear method
					if (date1Str != null)
						results.add(date1Str);
					break;
			} // end switch
		} // end if 008

		if (results.size() == 0 && df260subcSet != null && df260subcSet.size() > 0)
		{
			for (String raw : df260subcSet)
			{
				String val = DateUtils.getYearFromString(raw);
				if (val != null)
					results.add(val);
//				if (val != null)
//				{
//					int date260int = Integer.parseInt(val);
//					if (date260int != 0 &&
//						date260int <= upperLimit && date260int >= lowerLimit)
//						results.add(val);
//				}
			}
		}

		return results;
	}

	/**
	 * 2013-06-26  this is the OLD way
     * returns the publication dates from a record, if it is present
     *  and not beyond the current year + 1, and not earlier than EARLIEST_VALID_YEAR if
     *   a four digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     * @param cf008 - 008 field as a ControlField object
	 * @param date260c - the date string extracted from the 260c field
	 * @param vf264list  - a List of 264 fields as VariableField objects
	 * @return Set<String> containing publication years, or empty set if none
	 * @deprecated
	 */
	static Set<String> getPubDateSliderVals(ControlField cf008, String date260c, List<VariableField> vf264list)
	{
		Set<String> results = new HashSet<String>();
		if (cf008 != null && cf008.getData().length() >= 15)
		{
			char f008char6 = cf008.getData().charAt(6);
			String date1Str = getValidPubYearStrOrNull(cf008.getData().substring(7, 11), date260c, vf264list);
			int date1Int = -1;
			if (date1Str != null)
				date1Int = Integer.valueOf(date1Str);
			String rawDate2 = cf008.getData().substring(11, 15);
			String date2Str = getValidPubYearStrOrNull(rawDate2);
			int date2Int = -1;
			if (date2Str != null)
				date2Int = Integer.valueOf(date2Str);

			switch (f008char6)
			{
				case 'd':
				case 'i':
				case 'k':
				case 'q':
					// index start, end and years in between
					if (date1Str != null)
						results.add(date1Str);
					if (date2Str != null)
						results.add(date2Str);
					if (date1Int != -1 && date2Int != -1)
					{
						for (int year = date1Int; year < date2Int; year++)
							results.add(String.valueOf(year));
					}
					break;
				case 'm':
					if (date1Str != null)
						results.add(date1Str);
					if (!rawDate2.equals("9999") && date2Str != null)
					{
						// index end date and dates between
						results.add(date2Str);
						if (date1Int != -1 && date2Int != -1)
						{
							for (int year = date1Int; year < date2Int; year++)
								results.add(String.valueOf(year));
						}
					}
					break;
				case 'p':
				case 'r':
				case 't':
					// index only start and end
					if (date1Str != null)
						results.add(date1Str);
					if (date2Str != null)
						results.add(date2Str);
					break;
				case 'b':
				case 'c':
				case 'e':
				case 'n':
				case 's':
				case 'u':
				default:
					if (date1Str != null)
						results.add(date1Str);
					break;
			} // end switch
		} // end if 008

		return results;
	}


	/**
	 * returns the publication date groupings from a record, if pub date is
     *  given and is no later than the current year + 1, and is not earlier
     *  than EARLIEST_VALID_YEAR if it is a 4 digit year.
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
	 * @param date260c - the date string extracted from the 260c field
	 * @param vf264list  - a List of 264 fields as VariableField objects
	 * @return Set of Strings containing the publication date groupings
	 *         associated with the publish date
	 * @deprecated not using pub date groups with date slider
	 */
	static Set<String> getPubDateGroups(String date008, String date260c, List<VariableField> vf264list)
	{
		Set<String> resultSet = new HashSet<String>();

		// get the pub date, with decimals assigned for inclusion in ranges
		if (date008 != null) {
			if (DateUtils.isdddd(date008)) // exact year
			{
				String myDate = getValidPubDateStr(date008, date260c, vf264list);
				if (myDate != null) {
					int year = Integer.parseInt(myDate);
					// "this year" and "last three years" are for 4 digits only
					if (year >= (CURRENT_YEAR_AS_INT - 1))
						resultSet.add(PubDateGroup.THIS_YEAR.toString());
					if (year >= (CURRENT_YEAR_AS_INT - 3))
						resultSet.add(PubDateGroup.LAST_3_YEARS.toString());
					resultSet.addAll(getPubDateGroupsForYear(year));
				}
			}
			else if (DateUtils.isdddu(date008)) // decade
			{
				String first3Str = date008.substring(0, 3);
				int first3int = Integer.parseInt(first3Str);
				int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
				if (first3int <= currFirst3) {
					if (first3Str.equals(CURRENT_YEAR_AS_STR.substring(0, 3))) // this decade?
					{
						resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
						resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
						if (CURRENT_YEAR_AS_INT % 10 <= 3)
							resultSet.add(PubDateGroup.LAST_3_YEARS.toString());
					}
					else
					{ // not current decade
						if (CURRENT_YEAR_AS_INT % 10 <= 4) // which half of decade?
						{
							// first half of decade - current year ends in 0-4
							if (first3int == (CURRENT_YEAR_AS_INT / 10) - 1)
								resultSet.add(PubDateGroup.LAST_10_YEARS.toString());

							if (first3int >= (CURRENT_YEAR_AS_INT / 10) - 5)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						}
						else {
							// second half of decade - current year ends in 5-9
							if (first3int > (CURRENT_YEAR_AS_INT / 10) - 5)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						}
					}

				}
			}
			else if (DateUtils.isdduu(date008)) { // century
				String first2Str = date008.substring(0, 2);
				int first2int = Integer.parseInt(first2Str);
				int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
				if (first2int <= currFirst2) {
					if (first2Str.equals(CURRENT_YEAR_AS_STR.substring(0, 2))) {
						// current century
						resultSet.add(PubDateGroup.LAST_50_YEARS.toString());

						if (CURRENT_YEAR_AS_INT % 100 <= 19)
							resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
					}
					else {
						if (first2int == (CURRENT_YEAR_AS_INT / 100) - 1)
						{
							// previous century
							if (CURRENT_YEAR_AS_INT % 100 <= 25)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						}
						else
							resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
					}
				}
			}
			// we don't work with duuu or uuuu or other date strings
		}

		return resultSet;
	}


	/**
     * returns the publication date from a record, if it is present
     *  and not beyond the LATEST_VALID_YEAR, and not earlier than EARLIEST_VALID_YEAR if
     *   a four digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     * @param dateFrom008 - 4 character date from characters 7-10 or 11-14  in 008 field
	 * @param date260c - the date string extracted from the 260c field
	 * @param vf264list  - a List of 264 fields as VariableField objects
	 * @return String containing publication date, or null if none
	 */
	private static String getValidPubYearStrOrNull(String dateFrom008, String date260c, List<VariableField> vf264list)
	{
		String resultStr = null;
		if (DateUtils.isdddd(dateFrom008))
			return getValidPubDateStr(dateFrom008, date260c, vf264list);
		else if (DateUtils.isdddu(dateFrom008)) {
			int myFirst3 = Integer.parseInt(dateFrom008.substring(0, 3));
			int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
			if (myFirst3 <= currFirst3)
				resultStr = dateFrom008.substring(0, 3) + "0";
		} else if (DateUtils.isdduu(dateFrom008)) {
			int myFirst2 = Integer.parseInt(dateFrom008.substring(0, 2));
			int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
			if (myFirst2 <= currFirst2)
				resultStr = dateFrom008.substring(0, 2) + "00";
		} else {
			// last ditch try from 264 and 260c
			String validDate = getValidPubDateStr("-1", date260c, vf264list);
			if (validDate != null)
				return validDate;
		}

		if (yearIsValid(resultStr))
			return resultStr;
		return null;
	}

	/**
     * returns the publication date from a record, if it is present
     *  and not beyond the LATEST_VALID_YEAR, and not earlier than EARLIEST_VALID_YEAR if
     *   a four digit year
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     * @param dateStr - 4 character date from characters 7-10 or 11-14  in 008 field
	 * @return String containing publication date, or null if none
	 */
	private static String getValidPubYearStrOrNull(String dateStr)
	{
		String resultStr = null;
		if (DateUtils.isdddd(dateStr))
			resultStr = dateStr;
		else if (DateUtils.isdddu(dateStr)) {
			int myFirst3 = Integer.parseInt(dateStr.substring(0, 3));
			int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
			if (myFirst3 <= currFirst3)
				resultStr = dateStr.substring(0, 3) + "0";
		} else if (DateUtils.isdduu(dateStr)) {
			int myFirst2 = Integer.parseInt(dateStr.substring(0, 2));
			int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
			if (myFirst2 <= currFirst2)
				resultStr = dateStr.substring(0, 2) + "00";
		}

		if (yearIsValid(resultStr))
			return resultStr;

		return null;
	}

	static boolean yearIsValid(String dateStr)
	{
    	try
    	{
    		int dateInt = Integer.parseInt(dateStr);
    		if (dateInt <= LATEST_VALID_YEAR && dateInt >= EARLIEST_VALID_YEAR)
    			return true;
    	} catch (NumberFormatException e) {
    		return false;
    	}
    	return false;
	}


	/**
     * returns 4 digit year as a string if:
     *  pattern of year is dddd  or  dddu   and
     *  not beyond the LATEST_VALID_YEAR, and not earlier than EARLIEST_VALID_YEAR
     *  NOTE: errors in year value are not logged;  that is done in getPubDate()
     * @param yearFrom008 - 4 character year from 008 field bytes 7-10 or 11-14
     * @param toReplaceU - String to replace any u chars in the date value
	 * @return String containing year, or null if none
	 */
	static String get3or4DigitYear(String yearFrom008, String toReplaceU)
	{
		String resultStr = null;
		if (DateUtils.isdddd(yearFrom008) && !yearFrom008.equals("9999"))
			resultStr = yearFrom008;
		else if (DateUtils.isdddu(yearFrom008)) {
			int myFirst3 = Integer.parseInt(yearFrom008.substring(0, 3));
			int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
			if (myFirst3 <= currFirst3)
				resultStr = yearFrom008.substring(0, 3) + toReplaceU;
		}

		if (yearIsValid(resultStr))
			return resultStr;

		return null;
	}

	/**
     * check if a 4 digit year for a pub date is within valid range.
     *  If not, check for a 4 digit date in the 264c if 2nd ind is 1
     *  If not, take usable 260c date
     *  If not, take any other usable date in the 264c
	 * @param dateToCheck - String containing 4 digit date to check
	 * @param date260c - the date string extracted from the 260c field
	 * @param vf264list  - a List of 264 fields as VariableField objects
	 * @return String containing a 4 digit valid publication date, or null
	 */
	static String getValidPubDateStr(String dateToCheck, String date260c, List<VariableField> vf264list)
	{
		return getValidPubDateStr(dateToCheck, LATEST_VALID_YEAR, EARLIEST_VALID_YEAR, date260c, vf264list);
	}

	/**
     * check if a 4 digit year for a pub date is within the range.
     *  If not, check for a 4 digit date in the 264c if 2nd ind is 1
     *  If not, take usable 260c date
     *  If not, take any other usable date in the 264c
	 * @param dateToCheck - String containing 4 digit date to check
	 * @param upperLimit - highest valid year (inclusive)
	 * @param lowerLimit - lowest valid year (inclusive)
	 * @param date260c - the date string extracted from the 260c field
	 * @param vf264list  - a List of 264 fields as VariableField objects
	 * @return String containing a 4 digit valid publication date, or null
	 */
    static String getValidPubDateStr(String dateToCheck, int upperLimit, int lowerLimit, String date260c, List<VariableField> vf264list)
    {
    	try
    	{
    		int dateInt = Integer.parseInt(dateToCheck);
    		if (dateInt <= upperLimit && dateInt >= lowerLimit)
    			return dateToCheck;
    	} catch (NumberFormatException e) {
    	}

		// try to get date from 260 or 264
		String usable264cdateStr = null;
		if (vf264list != null)
		{
			for (VariableField vf264 : vf264list)
			{
				DataField df264 = (DataField) vf264;
				char ind2 = df264.getIndicator2();
				List<String> subcList = MarcUtils.getSubfieldStrings(df264, 'c');
				for (String date264cStr : subcList)
				{
					try
					{
						String possYear = DateUtils.getYearFromString(date264cStr);
						if (possYear != null)
						{
							int date264int = Integer.parseInt(possYear);
		    				if (date264int != 0 &&
		    					date264int <= upperLimit && date264int >= lowerLimit)
		    				{
		    					String yearStr = String.valueOf(date264int);
		    					if (ind2 == '1')
			    					return yearStr;
		    					else if (usable264cdateStr == null)
		    						usable264cdateStr = yearStr;
		    				}
						}
					}
					catch (NumberFormatException e)
					{
					}
				}
			}
		}

		// if we didn't find a 264 with 2nd ind '1' and sub c with usable year
		if (date260c != null) {
			String possYear = DateUtils.getYearFromString(date260c);
			if (possYear != null)
			{
				int date260int = Integer.parseInt(possYear);
				if (date260int != 0 &&
					date260int <= upperLimit && date260int >= lowerLimit)
					return String.valueOf(date260int);
			}
		}

		// if we didn't find a usable 260c date, then did we have any usable 264 date?
		if (usable264cdateStr != null)
			return usable264cdateStr;

		return null;
	}


	static int getCurrentYearAsInt() {
		return CURRENT_YEAR_AS_INT;
	}

	static Set<String> getPubDateGroupsForYear(int year)
	{
		Set<String> resultSet = new HashSet<String>();

		if (year >= (CURRENT_YEAR_AS_INT - 10))
			resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
		if (year >= (CURRENT_YEAR_AS_INT - 50))
			resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
		if (year < (CURRENT_YEAR_AS_INT - 50) && (year > -1.0))
			resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
		return resultSet;
	}

	/**
	 * gets the value from 008 bytes 7-10 if 008 byte 6 is in byte6vals; null otherwise
	 *
	 * @param cf008 the 008 field, as a ControlField object
	 * @param byte6vals a String containing the desired values of 008 byte 6
	 * @return a four digit year if 008 byte 6 matched and there was a four
	 *         digit year in 008 bytes 7-10, null otherwise
	 */
	static String get008Date1(ControlField cf008, String byte6vals)
	{
		if (cf008 != null && cf008.getData().length() >= 11)
		{
			char c6 = ((ControlField) cf008).getData().charAt(6);
			if (byte6vals.indexOf(c6) >= 0)
			{
				String cf008date1 = cf008.getData().substring(7, 11);
				return PublicationUtils.get3or4DigitYear(cf008date1, "0");
			}
			else
				return null;
		}
		else
			return null;
	}

	/**
	 * gets the value from 008 bytes 11-14 if 008 byte 6 is in byte6vals;
	 *   null otherwise,  and null if date is 9999
	 *
	 * @param cf008 the 008 field, as a ControlField object
	 * @param byte6vals a String containing the desired values of 008 byte 6
	 * @return a four digit year if 008 byte 6 matched and there was a four
	 *         digit year in 008 bytes 7-10, null otherwise
	 */
	static String get008Date2(ControlField cf008, String byte6vals)
	{
		if (cf008 != null && cf008.getData().length() >= 15)
		{
			char c6 = ((ControlField) cf008).getData().charAt(6);
			if (byte6vals.indexOf(c6) >= 0)
			{
				String cf008date2 = cf008.getData().substring(11, 15);
				return PublicationUtils.get3or4DigitYear(cf008date2, "9");
			}
			else
				return null;
		}
		else
			return null;
	}

}
