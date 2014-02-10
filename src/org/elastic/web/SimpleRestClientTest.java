package org.elastic.web;


import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.elastic.common.SimpleRestClient;
import org.elastic.common.SimpleRestClient.Response;
import org.elasticsearch.common.collect.ImmutableMap;
import org.json.simple.JSONObject;

import com.sun.istack.internal.Builder;


public class SimpleRestClientTest {
    public static void main(String[] args) throws IOException 
    {
       URL url = new URL("http://localhost:9200/_search");
       HashMap<String, String> headers = new HashMap<String, String>();
       HashMap<String, String> params  = new HashMap<String, String>();
       boolean debug = true;
       
       //params.put("q", "title:ATOVS");
       
       SimpleRestClient rClient = new SimpleRestClient();
       
       //Response response = rClient.doGetRequest(url, headers, params, true);
       
       //System.out.println("Response = " + response);
            
       //jsonObject.put("query", new JSONObject().put("term", new JSONObject().put("title", "ATOVS")));
       
       
       //String body = "{ \"query\" : {\"term\" : { \"title\" : \"ATOVS\" } } }";
       
       //String body = jsonObject.toJSONString();
       
       String body = "{ \"query\" : { \"match_all\" : {} }}";
       
       //String body = "{ \"multi_match\" : { \"query\" : \"AVISO\", \"fields\" : [ \"title\", \"abstract\" ] } }";
       
       Response response1 = rClient.doGetRequest(url, headers, params, body, debug);
       
    }
}