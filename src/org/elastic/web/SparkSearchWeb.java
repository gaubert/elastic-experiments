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
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.simple.JSONArray;
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
	
	//elements per page (currently only a static constant, to be externalized)
	final static int ELEM_PER_PAGE = 10;

	/**
	 * return the pagination information
	 * @param total
	 * @param from_element
	 * @return 
	 */
	private static Map<String, Object> computePaginationParams(int total, int from_element)
	{
		Map<String, Object> pagination = new HashMap<String, Object>();
		
		// nb_pages = integer div + 1 if total mod elem_per_page > 0
		int nb_pages = (total / ELEM_PER_PAGE) + ( ((total % ELEM_PER_PAGE) == 0) ? 0 : 1);
		
		pagination.put("nb_pages" , nb_pages);
		pagination.put("current_page", (from_element/ELEM_PER_PAGE));
		System.out.println("Current page = " + (from_element/ELEM_PER_PAGE));
		pagination.put("elem_per_page", ELEM_PER_PAGE);
		
		return pagination;
	}
	
	/**
	 * get the product description from elastic search index
	 * @param id Id of the product
	 * @return
	 * @throws Exception
	 */
	private static Map<?,?> getProductDescriptionFromElSearch(String id)
			throws Exception {
		
		//create the url with the id passed in argument
		URL url = new URL("http://localhost:9200/eumetsat-catalogue/product/"+ id);
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> params  = new HashMap<String, String>();
		String body = null;
		boolean debug = true;
		
		WebResponse response = rClient.doGetRequest(url, headers, params,
				body, debug);
		
		System.out.println("response = " + response);
		
		//if response.status

		// template input
		Map<String, Object> data = new HashMap<String, Object>();
		
		JSONParser parser = new JSONParser();

		JSONObject jsObj = (JSONObject) parser.parse(response.body);
		
		Map<?,?> identificationInfo = ((Map<?,?>) (((Map<?,?>) jsObj.get("_source")).get("identificationInfo")));
		
		data.put("id", id);
		data.put("title",    identificationInfo.get("title"));
		data.put("abstract", identificationInfo.get("abstract"));
		
		return data;
	}

	/**
	 * search using the Rest interface
	 * @param searchTerms
	 * @param from offset of the first element to return
	 * @param size maximum nb of elements to return
	 * @return
	 * @throws Exception
	 */
	private static Map<?,?> searchQueryElasticSearch(String searchTerms, int from, int size)
			throws Exception {
		
		URL url = new URL("http://localhost:9200/_search");
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> params  = new HashMap<String, String>();
		boolean debug = true;

		List<Map<String, String>> resHits = new ArrayList<Map<String, String>>();
		Map<String, String>       resHit  = null;

		// template input
		Map<String, Object> data = new HashMap<String, Object>();

		String body = "{ \"from\" : "+ from + ", \"size\" : " + size + "," + 
		              "\"query\" : { \"simple_query_string\" : { \"fields\" : [\"identificationInfo.title^10\", \"identificationInfo.abstract\"], \"query\" : \""
				+ searchTerms + "\" } }," + 
		              "  \"highlight\" : { \"fields\" : { \"identificationInfo.title\": {}, \"identificationInfo.abstract\": {} } } , " +
				      " \"facets\" : {\"tags\": { \"terms\" : { \"field\" : \"hierarchyNames\" } } } }";
		
		System.out.println("elastic-search request: " + body);
		
		WebResponse response = rClient.doGetRequest(url, headers, params,
				body, debug);

		System.out.println("response = " + response);

		JSONParser parser = new JSONParser();

		JSONObject jsObj = (JSONObject) parser.parse(response.body);

		data.put("total_hits", ((Map<?, ?>) jsObj.get("hits")).get("total"));
		
		// compute the pagination information to create the pagination bar
		Map<String, Object> pagination = computePaginationParams(((Long) (data.get("total_hits"))).intValue(), from);
		data.put("pagination", pagination);
		
		data.put("search_terms" , searchTerms);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> hits = (List<Map<String, Object>>) ((Map<?,?>) jsObj.get("hits")).get("hits");
		
		//to get the highlight
		Map<?,?> highlight = null;
		for  ( Map<String, Object> hit : hits) {
			resHit = new HashMap<String, String>();

			resHit.put("id", (String) hit.get("_id"));
			resHit.put("score", ((Double) hit.get("_score")).toString());

			// can have or not title or abstract
			// strategy. If it doesn't have an abstract or a title match then take it from the _source
			highlight= (Map<?, ?>) hit.get("highlight");
			
			if (highlight.containsKey("identification.title"))
			{
				resHit.put("title", (String) ((JSONArray) highlight.get("identificationInfo.title")).get(0) );
			}
			else
			{
				resHit.put("title", ((String) (((Map<?, ?>) (((Map<?, ?>) hit.get("_source")).get("identificationInfo"))).get("title"))) );
			}
			
			if (highlight.containsKey("identification.abstract"))
			{
				resHit.put("abstract", (String) ((JSONArray) highlight.get("identificationInfo.abstract")).get(0) );
			}
			else
			{
				resHit.put("abstract", ((String) (((Map<?, ?>) (((Map<?, ?>) hit.get("_source")).get("identificationInfo"))).get("abstract"))) );
			}
			
			resHits.add(resHit);
		}

		data.put("hits", resHits);

		return data;

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
					// Load template from source folder
					Template template = cfg.getTemplate("etc/web/search_page.ftl");
					
					// template input
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("elem_per_page", ELEM_PER_PAGE);
					
					// get in a String
					StringWriter results = new StringWriter();
					template.process(data, results);
					results.flush();

					return results.toString();
					
				} catch (Exception e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					String str = errors.toString();
					// print in out
					System.out.println(str);
					halt(401, "Error while accessing page search_page. error = " + str);

				}

				return null;
			}
		});

		/**
		 * show search results and paginate them
		 */
		Spark.get(new Route("/search/results") {
			@Override
			public Object handle(Request request, Response response) {
				try 
				{
					System.out.println("Request url " + request.raw().getRequestURL().toString());
					String searchTerms = request.queryParams("search-terms");
					
					int from = -1;
					int size = -1;
					
					try 
					{	
						from = Integer.parseInt(request.queryParams("from"));
					} catch (Exception e) {
						System.out.println("from parameter = " + from + ". It cannot be converted to int. default to -1.");
						e.printStackTrace(System.out);
					}
					
					try 
					{
						size        = Integer.parseInt(request.queryParams("size"));
				    } catch (Exception e) {
				       System.out.println("size parameter = " + size + ". It cannot be converted to int. default to -1.");
					   e.printStackTrace(System.out);
				    }		
						
					// System.out.println(request.queryString());
					System.out.println("SearchTerms " + searchTerms);
					System.out.println("From " + from);
					System.out.println("Size " + size);
					
					//template parameter map
					Map<?,?> data = null;
					// Load template from source foldqueryRestElasticSearcher
					Template template = cfg.getTemplate("etc/web/search_results.ftl");
					
					data = searchQueryElasticSearch(searchTerms, from, size);

					// Console output
					Writer out = new OutputStreamWriter(System.out);
					template.process(data, out);
					out.flush();

					// get in a String
					StringWriter results = new StringWriter();
					template.process(data, results);
					results.flush();
					
					return results.toString();

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
		
		/**
		 * add product page detail
		 */
		Spark.get(new Route("/product_description") {
			@Override
			public Object handle(Request request, Response response) {

				
				System.out.println("Request url " + request.raw().getRequestURL().toString());
				String id = request.queryParams("id");
				
				// Load template from source foldqueryRestElasticSearcher
				try 
				{
					//template parameter map
					Map<?,?> data = null;
					
					Template template = cfg.getTemplate("etc/web/product_description.ftl");
					
					//get product description info and return them as the input for the template
					data = getProductDescriptionFromElSearch(id);
					
					// Console output
					Writer out = new OutputStreamWriter(System.out);
					template.process(data, out);
					out.flush();

					// get in a String
					StringWriter results = new StringWriter();
					template.process(data, results);
					results.flush();
					
					return results.toString();

					
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

		/**
		 * default page => redirect by default to the search start page
		 */
		Spark.get(new Route("/") {
			@Override
			public Object handle(Request request, Response response) {

				// redirect to search page
				response.redirect("/search");
				return null;
			}
		});

		/**
		 * To serve static content. This should always be the last rule.
		// Add more mime types if necessary
		 */
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