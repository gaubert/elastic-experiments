package org.elastic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.elastic.common.FileSystem;
import org.elastic.common.JSONWriter;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ISO2JSON 
{
	public static void createInfoToIndex(String aSourceDirPath, String aDestDirPath)
	{

		try {
			String expression         = null;
			String result             = null;
			FileInputStream fileInput = null;

			
			for (File file : FileSystem.listDirectory(aSourceDirPath)) 
			{
		
			// FileInputStream file = new FileInputStream(new
			// File("./etc/metadata/EO-EUM-DAT-METOP-AMSU-AHRPT.xml"));
			fileInput = new FileInputStream(file);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();

			DocumentBuilder builder = builderFactory.newDocumentBuilder();

			Document xmlDocument = builder.parse(fileInput);

			XPath xPath = XPathFactory.newInstance().newXPath();

			String xpathFileID = "//*[local-name()='fileIdentifier']/*[local-name()='CharacterString']";

			JSONObject jsonObject = new JSONObject();

			// Get fileIDs
			System.out.println("*************************");
			// String expression = "/Employees/Employee[@emplid='3333']/email";
			System.out.println(xpathFileID);
			String fileID = xPath.compile(xpathFileID).evaluate(xmlDocument);
			System.out.println("result=" + fileID);

			if (fileID != null) {
				jsonObject.put("fileIdentifier", fileID);
			}

			// Get abstract
			/*
			 * System.out.println("*************************"); expression =
			 * "//*[local-name()='abstract']/*[local-name()='CharacterString']";
			 * System.out.println(expression); result =
			 * xPath.compile(expression).evaluate(xmlDocument);
			 * System.out.println("result=" + fileID);
			 * 
			 * if (result != null) { jsonObject.put("abstract", result); }
			 */

			// Get hierarchyLevelNames
			System.out.println("*************************");
			expression = "//*[local-name()='hierarchyLevelName']/*[local-name()='CharacterString']";
			System.out.println(expression);
			JSONArray list = new JSONArray();
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
					xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				list.add(nodeList.item(i).getFirstChild().getNodeValue());
				// System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
			}

			if (list.size() > 0) {
				jsonObject.put("hierarchyNames", list);
			}

			// Get Contact info
			String deliveryPoint = "//*[local-name()='address']//*[local-name()='deliveryPoint']/*[local-name()='CharacterString']";
			String city = "//*[local-name()='address']//*[local-name()='city']/*[local-name()='CharacterString']";
			String administrativeArea = "//*[local-name()='address']//*[local-name()='administrativeArea']/*[local-name()='CharacterString']";
			String postalCode = "//*[local-name()='address']//*[local-name()='postalCode']/*[local-name()='CharacterString']";
			String country = "//*[local-name()='address']//*[local-name()='country']/*[local-name()='CharacterString']";
			String email = "//*[local-name()='address']//*[local-name()='electronicMailAddress']/*[local-name()='CharacterString']";

			String addressString = "";
			String emailString = "";

			result = xPath.compile(deliveryPoint).evaluate(xmlDocument);

			if (result != null) {
				addressString += result.trim();
			}

			result = xPath.compile(postalCode).evaluate(xmlDocument);

			if (result != null) {
				addressString += "\n" + result.trim();
			}

			result = xPath.compile(city).evaluate(xmlDocument);

			if (result != null) {
				addressString += " " + result.trim();
			}
			result = xPath.compile(administrativeArea).evaluate(xmlDocument);

			if (result != null) {
				addressString += "\n" + result.trim();
			}

			result = xPath.compile(country).evaluate(xmlDocument);

			if (result != null) {
				addressString += "\n" + result.trim();
			}

			System.out.println("address =" + addressString);

			result = xPath.compile(email).evaluate(xmlDocument);

			if (result != null) {
				emailString += result.trim();
			}

			System.out.println("email =" + emailString);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("address", addressString);
			map.put("email", emailString);

			jsonObject.put("contact", map);

			// add identification info

			String abstractStr = "//*[local-name()='identificationInfo']//*[local-name()='abstract']/*[local-name()='CharacterString']";
			String titleStr = "//*[local-name()='identificationInfo']//*[local-name()='title']/*[local-name()='CharacterString']";
			String statusStr = "//*[local-name()='identificationInfo']//*[local-name()='status']/*[local-name()='MD_ProgressCode']/@codeListValue";
			String keywords = "//*[local-name()='keyword']/*[local-name()='CharacterString']";

			HashMap<String, Object> idMap = new HashMap<String, Object>();

			result = xPath.compile(titleStr).evaluate(xmlDocument);

			if (result != null) {
				idMap.put("title", result.trim());
			}

			result = xPath.compile(abstractStr).evaluate(xmlDocument);

			if (result != null) {
				idMap.put("abstract", result.trim());
			}

			result = xPath.compile(statusStr).evaluate(xmlDocument);

			if (result != null) {
				idMap.put("status", result.trim());
			}

			list = new JSONArray();
			nodeList = (NodeList) xPath.compile(keywords).evaluate(xmlDocument,
					XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				list.add(nodeList.item(i).getFirstChild().getNodeValue());
				System.out.println(nodeList.item(i).getFirstChild()
						.getNodeValue());
			}

			if (list.size() > 0) {
				idMap.put("keywords", list);
			}

			System.out.println("idMap =" + idMap);

			jsonObject.put("identificationInfo", idMap);

			// to pretty print
			Writer writer = new JSONWriter(); // this is the new writter that
												// adds indentation.
			jsonObject.writeJSONString(writer);

			System.out.println("JSON Result Object: " + writer.toString());

			String fName = aDestDirPath + "/" + FilenameUtils.getBaseName(file.getName()) + ".json";
			
			
			FileUtils.writeStringToFile(new File(fName), jsonObject.toJSONString());
			
			}

			System.out.println("*************************");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	public static void indexDirContent(String aSrcDir)
	{
		Client client     = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        String jsonStr    = null;
        JSONParser parser = new JSONParser();
        JSONObject jsObj  = null;
		
        try
        {
        
        int cpt = 0;
		for (File file : FileSystem.listDirectory(aSrcDir, new SuffixFileFilter(".json"))) 
		{
			jsonStr = FileUtils.readFileToString(file);
			jsObj = (JSONObject) parser.parse(jsonStr);
			
			IndexResponse response = client.prepareIndex("eumetsat-catalogue", "product", (String) jsObj.get("fileIdentifier"))
			        .setSource(jsObj.toJSONString())
			        .execute()
			        .actionGet();
			
			cpt ++;
			
			System.out.println("repsonse = " + response.getId() + " [] version = " + response.getVersion());
		}
		
		System.out.println("Indexed " + cpt + " files.");

        }
        catch (Exception e) {
			e.printStackTrace();
		}
		
        // on shutdown
        client.close();
	}
	
	public static void main(String[] args) 
	{
		String srcDir  = "etc/metadata";
		String destDir = "/tmp/json-results"; 
	 
		createInfoToIndex(srcDir, destDir);
		
		indexDirContent(destDir);
	}
	
}

