package org.elastic;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
 


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
 


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
            
            if (fileID != null)
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
            
 
            /*System.out.println("*************************");
            expression = "/Employees/Employee[@type='admin']/firstname";
            System.out.println(expression);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
            }
 
            System.out.println("*************************");
            expression = "/Employees/Employee[@emplid='2222']";
            System.out.println(expression);
            Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
            if(null != node) {
                nodeList = node.getChildNodes();
                for (int i = 0;null!=nodeList && i < nodeList.getLength(); i++) {
                    Node nod = nodeList.item(i);
                    if(nod.getNodeType() == Node.ELEMENT_NODE)
                        System.out.println(nodeList.item(i).getNodeName() + " : " + nod.getFirstChild().getNodeValue()); 
                }
            }
             
            System.out.println("*************************");
 
            expression = "/Employees/Employee[age>40]/firstname";
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            System.out.println(expression);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
            }
         
            System.out.println("*************************");
            expression = "/Employees/Employee[1]/firstname";
            System.out.println(expression);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
            }
            System.out.println("*************************");
            expression = "/Employees/Employee[position() <= 2]/firstname";
            System.out.println(expression);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
            }
 
            System.out.println("*************************");
            expression = "/Employees/Employee[last()]/firstname";
            System.out.println(expression);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
            }*/
            
            System.out.println("JSON Result Object: " + jsonObject.toJSONString());
 
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