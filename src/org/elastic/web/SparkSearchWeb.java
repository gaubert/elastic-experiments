package org.elastic.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.elastic.common.MimeUtil;
import org.elastic.common.SimpleRestClient;
import org.elastic.common.SimpleRestClient.WebResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * A simple example just showing some basic functionality
 */
public class SparkSearchWeb {

	// start elastic client once for all
	final static Client client = new TransportClient()
			.addTransportAddress(new InetSocketTransportAddress("localhost",
					9300));
	
	final static SimpleRestClient rClient = new SimpleRestClient();

	// Freemarker configuration object
	final static Configuration cfg = new Configuration();

	public static String queryElasticSearch(String searchTerms)
			throws Exception {
		// QueryBuilder qb = QueryBuilders.matchQuery("title", searchTerms);

		QueryBuilder qb = QueryBuilders.multiMatchQuery(searchTerms, "title",
				"abstract");
		
		System.out.println("Query:" + qb.toString());

		SearchResponse sr = client.prepareSearch("eumetsat-catalogue")
				.setQuery(qb).execute().actionGet();

		// Load template from source folder
		Template template = cfg.getTemplate("etc/web/search_results.ftl");

		// Build the data-model
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("total_hits", sr.getHits().totalHits());

		String id = null;
		float score = -1;
		String title = null;
		String abstractT = null;

		Map<String, Object> source = null;
		List<Map<String, String>> hits = new ArrayList<Map<String, String>>();
		Map<String, String> aHit = null;

		for (SearchHit hit : sr.getHits()) {
			aHit = new HashMap<String, String>();

			id = hit.getId();
			score = hit.getScore();

			source = hit.getSource();

			title = (String) ((Map) source.get("identificationInfo"))
					.get("title");
			abstractT = (String) ((Map) source.get("identificationInfo"))
					.get("abstract");

			aHit.put("id", id);
			aHit.put("score", Float.toString(score));
			aHit.put("title", title);
			aHit.put("abstract", abstractT);

			hits.add(aHit);
		}

		data.put("hits", hits);

		// Console output
		Writer out = new OutputStreamWriter(System.out);
		template.process(data, out);
		out.flush();

		// get in a String
		StringWriter results = new StringWriter();
		template.process(data, results);
		results.flush();

		return results.toString();
	}

	/**
	 * search using the Rest interface
	 * @param searchTerms
	 * @return
	 * @throws Exception
	 */
	public static String queryRestElasticSearch(String searchTerms)
			throws Exception {
		String result = null;

		URL url = new URL("http://localhost:9200/_search");
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> params  = new HashMap<String, String>();
		boolean debug = true;

		// Load template from source folder
		Template template = cfg.getTemplate("etc/web/search_results.ftl");

		List<Map<String, String>> resHits = new ArrayList<Map<String, String>>();
		Map<String, String>       resHit  = null;

		// template input
		Map<String, Object> data = new HashMap<String, Object>();

		String body = "{ \"query\" : { \"simple_query_string\" : { \"fields\" : [\"identificationInfo.title^10\", \"identificationInfo.abstract\"], \"query\" : \""
				+ searchTerms + "\" } }, \"highlight\" : { \"fields\" : { \"identificationInfo.title\": {}, \"identificationInfo.abstract\": {} } }  }";
		
		

		WebResponse response = rClient.doGetRequest(url, headers, params,
				body, debug);

		System.out.println("response = " + response);

		JSONParser parser = new JSONParser();

		JSONObject jsObj = (JSONObject) parser.parse(response.body);

		data.put("total_hits", ((Map) jsObj.get("hits")).get("total"));
		data.put("search_terms" , searchTerms);

		List<Map<String, Object>> hits = (List<Map<String, Object>>) ((Map) jsObj.get("hits")).get("hits");
		
		for  ( Map<String, Object> hit : hits) {
			resHit = new HashMap<String, String>();

			resHit.put("id", (String) hit.get("_id"));
			resHit.put("score", ((Double) hit.get("_score")).toString());

			resHit.put("abstract", ((String) (((Map) (((Map) hit.get("_source")).get("identificationInfo"))).get("abstract"))) );
			resHit.put("title", ((String) (((Map) (((Map) hit.get("_source")).get("identificationInfo"))).get("title"))) );

			resHits.add(resHit);
		}

		data.put("hits", resHits);

		// Console output
		Writer out = new OutputStreamWriter(System.out);
		template.process(data, out);
		out.flush();

		// get in a String
		StringWriter results = new StringWriter();
		template.process(data, results);
		results.flush();

		return results.toString();

	}

	public static void main(String[] args) {

		// setPort(5678); <- Uncomment this if you wan't spark to listen on a
		// port different than 4567.

		Spark.get(new Route("/test") {
			@Override
			public Object handle(Request request, Response response) {
				try {
					return FileUtils.readFileToString(new File(
							"etc/web/bootstrap_search.html"));
				} catch (IOException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					String str = errors.toString();
					// print in out
					System.out.println(str);
					halt(401, "Error while processing form. error = " + str);

				}

				return null;
			}
		});

		Spark.get(new Route("/search") {
			@Override
			public Object handle(Request request, Response response) {
				try {
					return FileUtils.readFileToString(new File(
							"etc/web/search_page.html"));
				} catch (IOException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					String str = errors.toString();
					// print in out
					System.out.println(str);
					halt(401, "Error while processing form. error = " + str);

				}

				return null;
			}
		});

		/*
		 * "hits" : { "total" : 4, "max_score" : 2.258254, "hits" : [ { "_index"
		 * : "eumetsat-catalogue", "_type" : "product", "_id" :
		 * "EO:EUM:DAT:METOP:ATOVSL2", "_score" : 2.258254, "_source" :
		 * {"identificationInfo":{"abstract":"The Advanced TIROS Operational
		 * Sounder (ATOVS), in combination with the Advanced Very High
		 * Resolution Radiometer (AVHRR), covers the visible, infrared and
		 * microwave spectral regions and thus has a wide range of applications:
		 * supplementing the retrieval of vertical temperature and humidity
		 * profiles, cloud and precipitation monitoring, sea ice and snow cover
		 * detection as well as surface temperature determination. ATOVS is
		 * composed of the Advanced Microwave Sounding Unit A, the Microwave
		 * Humidity Sounder (MHS) and the High Resolution Infrared Radiation
		 * Sounder (HIRS\/4).","title":"ATOVS Sounding Products -
		 * Metop","keywords":["Atmospheric
		 * conditions","Weather","Atmosphere","EUMETSAT Data
		 * Centre","GTS","EUMETCast
		 * -Europe","EUMETCast-Africa","EUMETCast-Americas
		 * ","EUMETCast"],"status":"
		 * Operational"},"hierarchyNames":["sat.Metop","
		 * theme.par.Atmosphere","SBA
		 * .Weather","dis.EUMETSATArchive","dis.GTS","dis
		 * .EUMETCast-Europe","dis.
		 * EUMETCast-Africa","dis.EUMETCast-Americas","dis
		 * .EUMETCast"],"contact":{"address":"EUMETSAT Allee 1\n64295
		 * Darmstadt\nHessen
		 * \nGermany","email":"ops@eumetsat.int"},"fileIdentifier
		 * ":"EO:EUM:DAT:METOP:ATOVSL2"} }, { "_index" : "eumetsat-catalogue",
		 * "_type" : "product", "_id" : "EO:EUM:DAT:NOAA:ATOVSL2", "_score" :
		 * 2.258254, "_source" : {"identificationInfo":{"abstract":"The Advanced
		 * TIROS Operational Sounder (ATOVS), in combination with the Advanced
		 * Very High Resolution Radiometer (AVHRR), covers the visible, infrared
		 * and microwave spectral regions and thus has a wide range of
		 * applications: supplementing the retrieval of vertical temperature and
		 * humidity profiles, cloud and precipitation monitoring, sea ice and
		 * snow cover detection as well as surface temperature determination.
		 * ATOVS is composed of the Advanced Microwave Sounding Unit A, the
		 * Microwave Humidity Sounder (MHS) and the High Resolution Infrared
		 * Radiation Sounder (HIRS\/4).","title":"ATOVS Sounding Products -
		 * NOAA","keywords":["Atmospheric
		 * conditions","Weather","Atmosphere","EUMETSAT Data
		 * Centre","GTS","EUMETCast
		 * -Europe","EUMETCast","EUMETCast-Africa","EUMETCast
		 * -Americas"],"status":"
		 * Operational"},"hierarchyNames":["theme.par.Atmosphere
		 * ","theme.par.GEONETCast
		 * ","SBA.Weather","dis.EUMETSATArchive","dis.GTS","
		 * dis.EUMETCast-Europe","
		 * sat.NOAA","dis.EUMETCast","dis.EUMETCast-Africa
		 * ","dis.EUMETCast-Americas"],"contact":{"address":"EUMETSAT Allee
		 * 1\n64295
		 * Darmstadt\nHessen\nGermany","email":"ops@eumetsat.int"},"fileIdentifier
		 * ":"EO:EUM:DAT:NOAA:ATOVSL2"} }, { "_index" : "eumetsat-catalogue",
		 * "_type" : "product", "_id" : "EO:EUM:DAT:MULT:EARS-ATOVS", "_score" :
		 * 2.1533632, "_source" : {"identificationInfo":{"abstract":
		 * "Sounder data is produced by a set of the instruments making up the Advanced TIROS Operational Vertical Sounder (ATOVS) and is used to obtain information about the vertical profile of temperature and humidity in the atmosphere. The radiation measurements from the ATOVS instruments can be assimilated directly into numerical models of the atmosphere. The EUMETSAT Advanced Retransmission Service (EARS) provides instrument data from the Metop and NOAA satellites collected via a network of Direct Readout stations."
		 * ,"title":"ATOVS Regional Data Service - Multimission","keywords":[
		 * "Atmospheric conditions"
		 * ,"Weather","GTS","EUMETCast-Europe","EUMETCast"
		 * ,"Level 1 Data","Atmosphere"
		 * ],"status":"Operational"},"hierarchyNames"
		 * :["sat.NOAA","MHS; AMSU-A; HIRS"
		 * ,"theme.par.Atmosphere","theme.par.GEONETCast"
		 * ,"SBA.Weather","dis.GTS"
		 * ,"dis.EUMETCast-Europe","theme.par.Level_1_Data"
		 * ,"dis.EUMETCast","sat.Metop"],"contact":{"address":
		 * "EUMETSAT Allee 1\n64295 Darmstadt\nHessen\nGermany"
		 * ,"email":"ops@eumetsat.int"
		 * },"fileIdentifier":"EO:EUM:DAT:MULT:EARS-ATOVS"} }, { "_index" :
		 * "eumetsat-catalogue", "_type" : "product", "_id" :
		 * "EO:EUM:SW:MULT:036", "_score" : 1.7940712, "_source" :
		 * {"identificationInfo":{"abstract":
		 * "As its main output, AAPP produces files of quality-controlled brightness temperature or radiance data for each instrument - either separately or mapped to a common field of view. If profiles of atmospheric variables are required the AAPP can be used in conjunction with an inversion package. AAPP radiance data are also used directly as input to variational data assimilation systems at several leading centres for Numerical Weather Prediction."
		 * ,"title":"ATOVS and AVHRR Pre-processing Package - Multimission",
		 * "keywords":["Atmospheric conditions","Weather","Climate","Internet",
		 * "SAF Archive & FTP"
		 * ],"status":""},"hierarchyNames":["sat.Metop","sat.NOAA"
		 * ,"SBA.Weather",
		 * "SBA.Climate","dis.SAFSoftware","theme.par.Land","dis.SAFArchiveFTP"
		 * ],"contact":{"address":
		 * "Met Office, Fitzroy Road\nEX1 3PB Exeter\n\nUnited Kingdom"
		 * ,"email":"nwpsaf@metoffice.gov.uk"
		 * },"fileIdentifier":"EO:EUM:SW:MULT:036"} } ] }
		 */
		Spark.get(new Route("/search/results") {
			@Override
			public Object handle(Request request, Response response) {
				try 
				{
					System.out.println("Request url " + request.raw().getRequestURL().toString());
					String searchTerms = request.queryParams("search-terms");
					// System.out.println(request.queryString());
					System.out.println("SearchTerms " + searchTerms);

					//String result = queryElasticSearch(searchTerms);
					String result = queryRestElasticSearch(searchTerms);

					return result;

				} catch (Exception e) {

					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					String str = errors.toString();
					// print in out
					System.out.println(str);
					halt(401, "Error while returning responses. error = " + str);

				}

				return null;
			}
		});

		Spark.get(new Route("/") {
			@Override
			public Object handle(Request request, Response response) {

				// redirect to search page
				response.redirect("/search");
				return null;
			}
		});

		// To serve static content. This should always be the last rule.
		// Add more mime types if necessary
		Spark.get(new Route("/*") {
			@Override
			public String handle(Request request, Response response) {
				// final File pub = new File("etc/static");
				final File file = new File("etc/static/" + request.pathInfo());

				if (!file.exists()) {
					System.out
							.println("File not found, returning 404: " + file);
					try
					{
					  halt(404, FileUtils.readFileToString(new File("etc/web/error_404.html")));
				    }
					catch (IOException e) 
				    {
					    StringWriter errors = new StringWriter();
					    e.printStackTrace(new PrintWriter(errors));
					    String str = errors.toString();
					    // print in out
					    System.out.println(str);
					    halt(401, "Error while processing form. error = " + str);

				    }
					return null;
				}

				String mime = MimeUtil.getMimeType(file.getName());

				System.out.println("Serving " + mime + ": " + file);
				response.raw().setContentType(mime + ";charset=utf-8");
				try {
					IOUtils.copy(new FileInputStream(file), response.raw()
							.getOutputStream());
				} catch (Exception e) {

					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					String str = errors.toString();
					// print in out
					System.out.println(str);
					halt(401, "Error while serving file " + file.getName()
							+ ". error = " + str);
				}
				halt(200);
				return null;
			}
		});

	}
}