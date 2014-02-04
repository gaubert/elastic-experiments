package org.elastic;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
 



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
 



import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
 
public class XPathTests {
    public static void main(String[] args) {
 
        try {
            FileInputStream file = new FileInputStream(new File("./etc/metadata/EO-EUM-DAT-METOP-AMSU-AHRPT.xml"));
                 
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
             
            DocumentBuilder builder =  builderFactory.newDocumentBuilder();
             
            Document xmlDocument = builder.parse(file);
 
            XPath xPath =  XPathFactory.newInstance().newXPath();
            
            String xpathFileID = "//*[local-name()='fileIdentifier']/*[local-name()='CharacterString']";
            
            JSONObject jsonObject=new JSONObject();
            
 
            // Get fileIDs
            System.out.println("*************************");
            //String expression = "/Employees/Employee[@emplid='3333']/email";
            System.out.println(xpathFileID);
            String fileID = xPath.compile(xpathFileID).evaluate(xmlDocument);
            System.out.println("result=" + fileID);
            
            if (fileID != null)
            {
            	jsonObject.put("fileIdentifier", fileID);
            }
            
            // Get abstract
            System.out.println("*************************");
            String expression = "//*[local-name()='abstract']/*[local-name()='CharacterString']";
            System.out.println(expression);
            String result = xPath.compile(expression).evaluate(xmlDocument);
            System.out.println("result=" + fileID);
            
            if (result != null)
            {
            	jsonObject.put("abstract", result);
            }
 
            // Get hierarchyLevelNames
            System.out.println("*************************");
            expression = "//*[local-name()='hierarchyLevelName']/*[local-name()='CharacterString']";
            System.out.println(expression);
            JSONArray list = new JSONArray();
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) 
            {
            	list.add(nodeList.item(i).getFirstChild().getNodeValue());
                //System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
            }
            
            if (list.size() > 0)
            {
            	jsonObject.put("hierarchyNames", list);
            }
            
            
            // Get Contact info           
            String deliveryPoint      = "//*[local-name()='address']//*[local-name()='deliveryPoint']/*[local-name()='CharacterString']";
            String city               = "//*[local-name()='address']//*[local-name()='city']/*[local-name()='CharacterString']";
            String administrativeArea = "//*[local-name()='address']//*[local-name()='administrativeArea']/*[local-name()='CharacterString']";
            String postalCode         = "//*[local-name()='address']//*[local-name()='postalCode']/*[local-name()='CharacterString']";
            String country            = "//*[local-name()='address']//*[local-name()='country']/*[local-name()='CharacterString']";
            String email              = "//*[local-name()='address']//*[local-name()='electronicMailAddress']/*[local-name()='CharacterString']";
            
            String addressString = "";
            String emailString   = "";
            
            result = xPath.compile(deliveryPoint).evaluate(xmlDocument);
            
            if (result != null)
            {
            	addressString += result.trim();
            }
            
            result = xPath.compile(postalCode).evaluate(xmlDocument);
            
            if (result != null)
            {
            	addressString += "\n" + result.trim();
            }
            
            result = xPath.compile(city).evaluate(xmlDocument);
            
            if (result != null)
            {
            	addressString += " " + result.trim();
            }
            result = xPath.compile(administrativeArea).evaluate(xmlDocument);
            
            if (result != null)
            {
            	addressString += "\n" + result.trim();
            }
                      
            result = xPath.compile(country).evaluate(xmlDocument);
            
            if (result != null)
            {
            	addressString += "\n" + result.trim();
            }
            
           
            System.out.println("address =" + addressString);
            
            result = xPath.compile(email).evaluate(xmlDocument);
            
            if (result != null)
            {
            	 emailString += result.trim();
            }
            
            System.out.println("email =" + emailString);
            HashMap<String,String> map = new HashMap<String, String>();
            map.put("address", addressString);
            map.put("email", emailString);
            
            jsonObject.put("contact", map);
         
            // add identification info   
            
            String abstractStr = "//*[local-name()='identificationInfo']//*[local-name()='abstract']/*[local-name()='CharacterString']";
            String statusStr   = "//*[local-name()='identificationInfo']//*[local-name()='status']/*[local-name()='MD_ProgressCode']/@codeListValue";
            String keywords    = "//*[local-name()='keyword']/*[local-name()='CharacterString']";
            
            HashMap<String,String> idMap = new HashMap<String, String>();
            
            result = xPath.compile(abstractStr).evaluate(xmlDocument);
            
            if (result != null)
            {
            	idMap.put("abstract", result.trim());
            }
            
            result = xPath.compile(statusStr).evaluate(xmlDocument);
            
            if (result != null)
            {
            	idMap.put("status", result.trim());
            }
            
            list = new JSONArray();
            nodeList = (NodeList) xPath.compile(keywords).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) 
            {
            	list.add(nodeList.item(i).getFirstChild().getNodeValue());
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
            }
            
            System.out.println("idMap =" + idMap);
            
            jsonObject.put("identificationInfo", idMap);
            
                  
            System.out.println("JSON Result Object: " + jsonObject.toJSONString());
            
            File f = new File("/tmp/metadata.json");
            FileUtils.writeStringToFile(new File("./sandbox/firstFile.json"), jsonObject.toJSONString());
 
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
}