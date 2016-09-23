package org.solrmarc.solr;

import java.io.IOException;
import java.util.*;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.solrmarc.tools.SolrUtils;

/**
 * An implementation of SolrProxy utilizing a SolrServer from SolrJ.
 * @author Bob Haschart, with modifications by Naomi Dushay
 */
public class SolrServerProxy implements SolrProxy
{
    SolrClient solrJSolrClient;

    public SolrServerProxy(HttpSolrClient solrJSolrClient) {
        this.solrJSolrClient = solrJSolrClient;
    }


    /**
     * given a map of field names and values, create a Document and add it to
     *  the index
     * @param fldNames2ValsMap keys are Solr field names, values are String or Collection objects containing values for Solr field
     * @return a string representation of the document when verbose is true or addDocToIndex is false
     */
    public String addDoc(Map<String, Object> fldNames2ValsMap, boolean verbose, boolean addDocToIndex) throws IOException
    {
        SolrInputDocument inputDoc = SolrUtils.createSolrInputDoc(fldNames2ValsMap);
        if (addDocToIndex)
        {
            try
            {
                solrJSolrClient.add(inputDoc);
            }
            catch (SolrServerException e)
            {
                throw(new SolrRuntimeException("SolrServerException", e));
            }
        }

        if (verbose || !addDocToIndex)
            return inputDoc.toString().replaceAll("> ", "> \n");
        else
            return(null);
    }

    /**
     * close the solrCore
     */
    public void close()
    {
        // do nothing
    }

    public SolrClient getSolrServer()
    {
        return(solrJSolrClient);
    }

    /**
     * commit changes to the index
     */
    public void commit(boolean optimize) throws IOException
    {
        try
        {
            if (optimize)
                solrJSolrClient.optimize();
            else
                solrJSolrClient.commit();
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrServerException", e));
        }
    }

    /**
     * delete doc from the index
     * @param id the unique identifier of the document to be deleted
     */
    public void delete(String id, boolean fromCommitted, boolean fromPending) throws IOException
    {
        try
        {
            solrJSolrClient.deleteById(id);
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrServerException", e));
        }
    }

    /**
     * delete all docs from the index
     * Warning: be very sure you want to call this
     */
    public void deleteAllDocs() throws IOException
    {
        try
        {
            solrJSolrClient.deleteByQuery("*:*");
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrServerException", e));
        }
    }

    /**
     * return true if exception is a SolrException
     */
    public boolean isSolrException(Exception e)
    {
        if (e.getCause() instanceof SolrServerException)
            return(true);
        return false;
    }

}
