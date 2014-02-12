package org.elastic.web;


import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elastic.common.SimpleRestClient;
import org.elastic.common.SimpleRestClient.WebResponse;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.search.SearchHit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.istack.internal.Builder;


public class SimpleRestClientTest {
    public static void main(String[] args) throws Exception 
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
       
       //String body = "{ \"query\" : { \"match_all\" : {} }}";
       
       //String body = "{ \"multi_match\" : { \"query\" : \"AVISO\", \"fields\" : [ \"title\", \"abstract\" ] } }";
       
       String body = "{ \"query\" : { \"simple_query_string\" : { \"fields\" : [\"identificationInfo.title^10\", \"identificationInfo.abstract\"], \"query\" : \"ATOVS\" } } }";
       
       WebResponse response1 = rClient.doGetRequest(url, headers, params, body, debug);
       
       System.out.println("response = " + response1);
       
       
       JSONParser parser = new JSONParser();
       
       JSONObject jsObj = (JSONObject) parser.parse(response1.body);
       
       List hits     = (List) ((Map) jsObj.get("hits")).get("hits");
       
       for (Object hit : hits ) 
       {
    	  Map lHit = (Map) hit; 
          String description = ((String) (((Map) (((Map) lHit.get("_source")).get("identificationInfo"))).get("abstract")));
          String title       = ((String) (((Map) (((Map) lHit.get("_source")).get("identificationInfo"))).get("title")));
        		  
          System.out.println("description =" + description); 
       }
       
    }
}