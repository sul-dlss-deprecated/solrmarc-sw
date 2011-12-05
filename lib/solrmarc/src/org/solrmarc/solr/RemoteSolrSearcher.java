package org.solrmarc.solr;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.*;
import org.marc4j.marc.Record;

public class RemoteSolrSearcher
{
    static boolean verbose = false;
    static boolean veryverbose = false;
    Object solrSearcher = null;
    String solrBaseURL;
    String solrFieldContainingEncodedMarcRecord;
    MarcStreamWriter output;
    String query;
    
    public RemoteSolrSearcher(String solrBaseURL, String query, String solrFieldContainingEncodedMarcRecord)
    {
//      refedSolrSearcher = solrCore.getSearcher();
//      solrSearcher = refedSolrSearcher.get();
        this.solrBaseURL = solrBaseURL;  
        this.solrFieldContainingEncodedMarcRecord = solrFieldContainingEncodedMarcRecord;
        this.query = query;
        if (verbose) System.err.println("URL = "+ solrBaseURL + "  query = "+ query);
    }
    
    public int handleAll()
    {
        output = new MarcStreamWriter(System.out, "UTF8", true);
        if (solrFieldContainingEncodedMarcRecord == null) solrFieldContainingEncodedMarcRecord = "marc_display";
        /*String queryparts[] = query.split(":");
        if (queryparts.length != 2) 
        {
            //System.err.println("Error query must be of the form    field:term");
            System.out.println("Error: query must be of the form    field:term  " + query);
            return 0;
        }*/
        String encQuery;
        try
        {
            encQuery = java.net.URLEncoder.encode(query, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            encQuery = query;
        }
        if (verbose) System.err.println("encoded query = "+ encQuery);
        String resultSet[] = getIdSet(encQuery);
        String recordStr = null;
        for (String id : resultSet)
        {
            recordStr = getFieldFromDocumentGivenDocID(id, solrFieldContainingEncodedMarcRecord);
            Record record = null;
            if (recordStr.startsWith("<?xml version"))
            {
                record = getRecordFromXMLString(recordStr);            
            }
            else if (recordStr.startsWith("{\""))
            {
                record = getRecordFromJsonString(recordStr);
            }
            else
            {
                record = getRecordFromRawMarc(recordStr);
            }
            if (record != null)  
            {
                output.write(record);
                System.out.flush();
            }
        }
        output.close();
        return 0;
    }
   
    private String getFieldFromDocumentGivenDocID(String id, String solrFieldContainingEncodedMarcRecord2)
    {
        String fullURLStr = solrBaseURL + "/select/?q=id%3A"+id+"&wt=json&indent=on&qt=standard&fl="+solrFieldContainingEncodedMarcRecord2;
        if (verbose) System.err.println("encoded document retrieval url = "+ fullURLStr);
        URL fullURL = null;
        try
        {
            fullURL = new URL(fullURLStr);
        }
        catch (MalformedURLException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        BufferedReader sIn = null;
        try
        {
            sIn = new BufferedReader( new InputStreamReader(fullURL.openStream(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String line;
        int numFound = 0;
        String result = null;
        int count = 0;
        try
        {
            while ((line = sIn.readLine()) != null)
            {
                if (line.contains(solrFieldContainingEncodedMarcRecord2+"\":"))
                {
                    if (line.contains("\"<?xml version"))
                    {
                        result = line.replaceFirst(".*<\\?xml", "<?xml");
                        result = result.replaceFirst("</collection>.*", "</collection>");
                        result = result.replaceAll("\\\\\"", "\"");
                    }
                    else if (line.contains(solrFieldContainingEncodedMarcRecord2+"\":[\"{"))
                    {
                        result = line.replaceFirst("[^:]*:\\[\"[{]", "{");
                        result = result.replaceFirst("\\\\n\"][}]]", "");
                        result = result.replaceAll("\\\\\"", "\"");
                        result = result.replace("\\\\", "\\");
                    }
                    else 
                    {
                        result = line.replaceFirst("[^:]*:\"", "");
                        result = result.replaceFirst("\"}]$", "");
                        result = result.replaceAll("\\\\\"", "\"");
                        result = normalizeUnicode(result);
                    }
                }
                else
                {
                    continue;
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(result);
    }

    private String normalizeUnicode(String string)
    {
        Pattern pattern = Pattern.compile("(\\\\u([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]))|(#(29|30|31);)");
        Matcher matcher = pattern.matcher(string);
        StringBuffer result = new StringBuffer();
        int prevEnd = 0;
        while(matcher.find())
        {
            result.append(string.substring(prevEnd, matcher.start()));
            result.append(getChar(matcher.group()));
            prevEnd = matcher.end();
        }
        result.append(string.substring(prevEnd));
        string = result.toString();
        return(string);
    }
    
    private String getChar(String charCodePoint)
    {
        int charNum;
        if (charCodePoint.startsWith("\\u"))
        {
            charNum = Integer.parseInt(charCodePoint.substring(2), 16);
        }
        else
        {
            charNum = Integer.parseInt(charCodePoint.substring(1, 3));
        }
        String result = ""+((char)charNum);
        return(result);
    }
    
    
    public String[] getIdSet(String query) 
    {
        int setSize = getIdSetSize(query);
        String resultSet[] = new String[setSize];

        String fullURLStr = solrBaseURL + "/select/?q="+query+"&wt=json&qt=standard&indent=on&fl=id&start=0&rows="+setSize;
        if (verbose) System.err.println("Full URL for search = "+ fullURLStr);
        URL fullURL = null;
        try
        {
            fullURL = new URL(fullURLStr);
        }
        catch (MalformedURLException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        BufferedReader sIn = null;
        try
        {
            sIn = new BufferedReader( new InputStreamReader(fullURL.openStream(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String line;
        int numFound = 0;
        int count = 0;
        try
        {
            while ((line = sIn.readLine()) != null)
            {
                if (line.contains("\"id\":")) 
                {
                    String id = line.replaceFirst(".*:[^\"]?\"([-A-Za-z0-9_]*).*", "$1");
                    if (veryverbose) System.err.println("record num = "+ (count) + "  id = " + id);
                    resultSet[count++] = id;
                }
            }
        }
        catch (NumberFormatException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(resultSet);
    }
    
    public int getIdSetSize(String query) 
    {
        String fullURLStr = solrBaseURL + "/select/?q="+query+"&wt=json&qt=standard&indent=on&start=0&rows=0";
        if (verbose) System.err.println("Full URL for search = "+ fullURLStr);
        URL fullURL = null;
        try
        {
            fullURL = new URL(fullURLStr);
        }
        catch (MalformedURLException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        BufferedReader sIn = null;
        try
        {
            sIn = new BufferedReader( new InputStreamReader(fullURL.openStream(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String line;
        int numFound = 0;
        try
        {
            while ((line = sIn.readLine()) != null)
            {
                if (line.contains("\"numFound\""))
                {
                    String numFoundStr = line.replaceFirst(".*numFound[^0-9]*([0-9]*).*", "$1");
                    numFound = Integer.parseInt(numFoundStr);
                    if (verbose) System.err.println("numFound = "+ numFound);
                }
            }
        }
        catch (NumberFormatException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(numFound);
    }
    /**
     * Extract the marc record from binary marc
     * @param marcRecordStr
     * @return
     */
    private Record getRecordFromRawMarc(String marcRecordStr)
    {
        MarcStreamReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcStreamReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next(); 
                    return(record);
                }
            }
            catch( MarcException me)
            {
                me.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        } while (tryAgain);
        return(null);
    }
    /**
     * Extract the marc record from JSON string
     * @param marcRecordStr
     * @return
     */
    private Record getRecordFromJsonString(String marcRecordStr)
    {
        MarcJsonReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcJsonReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next(); 
                    return(record);
                }
            }
            catch( MarcException me)
            {
                me.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        } while (tryAgain);
        return(null);
    }
    
    // error output
    static BufferedWriter errOut = null;
    
    /**
     * Extract marc record from MarcXML
     * @param marcRecordStr MarcXML string
     * @return marc4j Record
     */
    public Record getRecordFromXMLString(String marcRecordStr)
    {
        MarcXmlReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcXmlReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next(); 
//                    if (verbose)
//                    {
//                        System.out.println(record.toString());
//                        System.out.flush();
//                    }
                    return(record);
                }
            }
            catch( MarcException me)
            {
                try
                {
                    errOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("badRecs.xml"))));
                    errOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
                }
                catch (FileNotFoundException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                catch (IOException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                String trimmed = marcRecordStr.substring(marcRecordStr.indexOf("<record>"));
                trimmed = trimmed.replaceFirst("</collection>", "");
                trimmed = trimmed.replaceAll("><", ">\n<");
                try
                {
                    errOut.write(trimmed);
                }
                catch (IOException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                if (marcRecordStr.contains("<subfield code=\"&#31;\">"))
                {
                    // rewrite input string and try again.
                    marcRecordStr = marcRecordStr.replaceAll("<subfield code=\"&#31;\">(.)", "<subfield code=\"$1\">");
                    tryAgain = true;
                }
                else if (extractLeader(marcRecordStr).contains("&#")) //.("<leader>[^<>&]*&#[0-9]+;[^<>&]*</leader>"))
                {
                    // rewrite input string and try again.
                    // 07585nam a2200301 a 4500
                    String leader = extractLeader(marcRecordStr).replaceAll("&#[0-9]+;", "0");
                    marcRecordStr = marcRecordStr.replaceAll("<leader>[^<]*</leader>", leader);
                    tryAgain = true;
                }
                else
                {
                    me.printStackTrace();
                    //System.out.println("The bad record is: "+ marcRecordStr);
                    System.err.println("The bad record is: "+ marcRecordStr);
                }
            }
            catch (UnsupportedEncodingException e)
            {
                // e.printStackTrace();
                System.err.println(e.getMessage());
            }
        } while (tryAgain);
        return(null);

    }
        
 
    /**
     * Extract the leader from the marc record string
     * @param marcRecordStr marc record as a String
     * @return Leader leader string for the marc record
     */
    private String extractLeader(String marcRecordStr)
    {
        final String leadertag1 = "<leader>";
        final String leadertag2 = "</leader>";
        String leader = null;
        try {
            leader = marcRecordStr.substring(marcRecordStr.indexOf(leadertag1), marcRecordStr.indexOf(leadertag2)+leadertag2.length() );
        }
        catch (IndexOutOfBoundsException e)
        {}
        return leader;
    }

    public static void main(String args[])
    {
        String baseURLStr = "http://localhost:8983/solr";
        String query = null;
        String maxRows = "20000";
        String field = "marc_display";
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-v")) verbose = true;
            else if (args[i].equals("-vv")) { verbose = true; veryverbose = true; }
            else if (args[i].startsWith("http")) baseURLStr = args[i];
            else if (args[i].contains(":")) query = args[i];
            else field = args[i];
        }
        RemoteSolrSearcher searcher = new RemoteSolrSearcher(baseURLStr, query, field);
        searcher.handleAll();
        System.exit(0);
    }
}
