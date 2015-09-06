package br.ufscar.dc.rejasp.file.xml;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import br.ufscar.dc.rejasp.indication.model.Indication;
import br.ufscar.dc.rejasp.indication.model.IndicationClass;
import br.ufscar.dc.rejasp.indication.model.IndicationException;
import br.ufscar.dc.rejasp.indication.model.IndicationInterface;
import br.ufscar.dc.rejasp.indication.model.IndicationPackage;
import br.ufscar.dc.rejasp.indication.model.MatchText;

import javax.xml.parsers.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class IndicationXml {
	private String sXmlFilePath;
	private ArrayList lstIndications;
	
	public IndicationXml(String sXmlFilePath, ArrayList lstIndications) {
		this.sXmlFilePath = sXmlFilePath;
		this.lstIndications = lstIndications;
	}
	
	public ArrayList getIndications() {
		return lstIndications;
	}

	public boolean saveIndicationsToXml() {
		ArrayList lstPackages, lstInterfaces, lstClasses, lstExceptions, lstRules, lstWords;
		String sTarget, sRule;
		Indication indicationModel;
		IndicationPackage packageModel;
		IndicationInterface interfaceModel;
		IndicationClass classModel;
		IndicationException exceptionModel;
		MatchText ruleModel;

		Element indication, name, description, packages, _package, interfaces, 
		_interface, classes, _class, exceptions, exception, rules, rule, 
		target, matchRule, caseSensity, words, word;
		try {

			// Find the implementation
			DocumentBuilderFactory factory 
			= DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();

			// Create the document
			Document doc = impl.createDocument(null, 
					"Indications", null);

			Element root = doc.getDocumentElement();

			for (int i = 0; i < lstIndications.size(); i++) {
				// Setting indication node
				indicationModel = (Indication)lstIndications.get(i);
				indication = doc.createElement("indication");
				indication.setAttribute("index", String.valueOf(i));
				/*// Setting activation state
				indication.setAttribute("active", 
						indicationModel.isActive()?"true":"false");*/
				// Setting name
				name = doc.createElement("name");
				name.appendChild(doc.createTextNode(indicationModel.getName()));
				indication.appendChild(name);
				// Setting description
				description = doc.createElement("description");
				description.appendChild(doc.createTextNode(indicationModel.getDescription()));
				indication.appendChild(description);

				// Packages
				packages = doc.createElement("Packages");
				lstPackages = indicationModel.getPackages();
				// For each package
				for ( int j = 0; j < lstPackages.size();j++ ) {
					packageModel = (IndicationPackage)lstPackages.get(j);
					_package = doc.createElement("package");
					_package.setAttribute("index", String.valueOf(j));
					// Setting package name
					name = doc.createElement("name");
					name.appendChild(doc.createTextNode(packageModel.getName()));
					_package.appendChild(name);
					// Setting package description
					description = doc.createElement("description");
					description.appendChild(doc.createTextNode(packageModel.getDescription()));
					_package.appendChild(description);
					// Setting package interfaces
					interfaces = doc.createElement("Interfaces");
					lstInterfaces = packageModel.getInterfaces();
					for( int k = 0; k < lstInterfaces.size(); k++ ) {
						interfaceModel = (IndicationInterface)lstInterfaces.get(k);
						_interface = doc.createElement("interface");
						_interface.setAttribute("index", String.valueOf(k));
						// Setting interface name
						name = doc.createElement("name");
						name.appendChild(doc.createTextNode(interfaceModel.getName()));
						_interface.appendChild(name);
						// Setting interface description
						description = doc.createElement("description");
						description.appendChild(doc.createTextNode(interfaceModel.getDescription()));
						_interface.appendChild(description);
						interfaces.appendChild(_interface);
					}
					_package.appendChild(interfaces);

					// Setting package classes
					classes = doc.createElement("Classes");
					lstClasses = packageModel.getClasses();
					for( int k = 0; k < lstClasses.size(); k++ ) {
						classModel = (IndicationClass)lstClasses.get(k);
						_class = doc.createElement("class");
						_class.setAttribute("index", String.valueOf(k));
						// Setting class name
						name = doc.createElement("name");
						name.appendChild(doc.createTextNode(classModel.getName()));
						_class.appendChild(name);
						// Setting class description
						description = doc.createElement("description");
						description.appendChild(doc.createTextNode(classModel.getDescription()));
						_class.appendChild(description);
						classes.appendChild(_class);
					}
					_package.appendChild(classes);

					// Setting package exceptions
					exceptions = doc.createElement("Exceptions");
					lstExceptions = packageModel.getExceptions();
					for( int k = 0; k < lstExceptions.size(); k++ ) {
						exceptionModel = (IndicationException)lstExceptions.get(k);
						exception = doc.createElement("exception");
						exception.setAttribute("index", String.valueOf(k));
						// Setting exception name
						name = doc.createElement("name");
						name.appendChild(doc.createTextNode(exceptionModel.getName()));
						exception.appendChild(name);
						// Setting exception description
						description = doc.createElement("description");
						description.appendChild(doc.createTextNode(exceptionModel.getDescription()));
						exception.appendChild(description);
						exceptions.appendChild(exception);
					}
					_package.appendChild(exceptions);
					packages.appendChild(_package);	
				}
				indication.appendChild(packages);

				//Rules
				rules = doc.createElement("Rules");
				lstRules = indicationModel.getMatches();
				for ( int j = 0; j < lstRules.size(); j++ ) {
					ruleModel = (MatchText)lstRules.get(j);
					rule = doc.createElement("rule");
					rule.setAttribute("index", String.valueOf(j));
					// Set rule target
					target = doc.createElement("target");
					if ( ruleModel.getTarget().equals(MatchText.STRING_LITERAL) )
						sTarget = "string literal";
					else
						sTarget = "variable name";
					target.appendChild(doc.createTextNode(sTarget));
					rule.appendChild(target);
					// Set matching rule
					matchRule = doc.createElement("matchingRule");
					if(ruleModel.getRule().equals(MatchText.CONTAINS))
						sRule = "contains";
					else if (ruleModel.getRule().equals(MatchText.STARTS_WITH))
						sRule = "starts with";
					else
						sRule = "ends with";
					matchRule.appendChild(doc.createTextNode(sRule));
					rule.appendChild(matchRule);
					// Set case sensity flag
					caseSensity = doc.createElement("caseSensity");
					if ( ruleModel.isCaseSensity() )
						caseSensity.appendChild(doc.createTextNode("true"));
					else
						caseSensity.appendChild(doc.createTextNode("false"));
					rule.appendChild(caseSensity);

					// For each word in rule
					words = doc.createElement("Words");
					lstWords = ruleModel.getWords();
					for ( int k = 0; k < lstWords.size(); k++ ) {
						word = doc.createElement("word");
						word.setAttribute("index", String.valueOf(k));
						word.appendChild(doc.createTextNode((String)lstWords.get(k)));
						words.appendChild(word);
					}
					rule.appendChild(words);
					rules.appendChild(rule);
				}
				indication.appendChild(rules);
				root.appendChild(indication);
			}
			// Serialize the document
			OutputFormat format = new OutputFormat(doc);
			format.setLineWidth(65);
			format.setIndenting(true);
			format.setIndent(2);
			format.setEncoding("ISO-8859-1");
			FileOutputStream  outputStream;
			outputStream = new FileOutputStream( sXmlFilePath );
			XMLSerializer serializer = new XMLSerializer(outputStream, format);
			serializer.serialize(doc);
		}
		catch (FactoryConfigurationError e) { 
			System.out.println("Could not locate a JAXP factory class"); 
		}
		catch (ParserConfigurationException e) { 
			System.out.println(
					"Could not locate a JAXP DocumentBuilder class"
			); 
		}
		catch (DOMException e) {
			System.err.println(e); 
		}
		catch (IOException e) {
			System.err.println(e); 
		}
		return true;
	}
	
	public boolean loadIndicationsFromXml() {
		Indication indicationModel;
		IndicationPackage packageModel;
		IndicationInterface interfaceModel;
		IndicationClass classModel;
		IndicationException exceptionModel;
		MatchText ruleModel;

		String sName, sDescription, sTarget, sRule, sCase;
		lstIndications = new ArrayList();

        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
           DocumentBuilder builder = factory.newDocumentBuilder();
           doc = builder.parse( this.sXmlFilePath );
           
        } catch (SAXException sxe) {
           // Error generated during parsing)
           Exception  x = sxe;
           if (sxe.getException() != null)
               x = sxe.getException();
           x.printStackTrace();

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
            return false;
        } catch (IOException ioe) {
           // I/O error
           ioe.printStackTrace();
           return false;
        }
        Node domIndication, node, nodeAttribute, domPackage, domRule = null;
        NodeList domIndications = doc.getElementsByTagName("indication");

		// For each indication
		for ( int i = 0; i < domIndications.getLength(); i++ ) {
			domIndication = (Node)domIndications.item(i);
			
			// Read name and description of the indication
			node = domIndication.getFirstChild();
			sName = getNodeValueFromSibling("name", node);
			if(sName == null) {
				System.err.println("Name node of indication not found.");
				return false;
			}
			sDescription = getNodeValueFromSibling("description", node);
			if(sDescription == null) {
				System.err.println("Description node of indication " + sName + " not found.");
				return false;
			}
			
			/*
			// Read active attribute of indication
			if(domIndication.getAttributes() == null) {
				System.err.println("Atribute active of indication " + sName + " wasn't found in file.");
				return false;
			}
			if((nodeAttribute = domIndication.getAttributes().item(0)) == null) {
				System.err.println("Atribute active of indication " + sName + " wasn't found in file.");
				return false;
			}
			if(! nodeAttribute.getNodeName().equals("active")) {
				System.err.println("Atribute active of indication " + sName + " wasn't found in file.");
				return false;
			}*/
			indicationModel = new Indication(sName);
			indicationModel.setDescription(sDescription);

			/*if(nodeAttribute.getNodeValue().equals("true"))
				indicationModel.setActive(true);
			else
				indicationModel.setActive(false);*/

			// Find packages node
			node = getNodeFromSibling("Packages", node);
			if(node == null) {
				System.err.println("Packages node of indication " + sName + " wasn't found.");
				return false;
			}
			
			// For each package
			domPackage = node.getFirstChild();
			while(domPackage != null && (domPackage = getNodeFromSibling("package", domPackage)) != null) {
				sName = getNodeValueFromParent("name", domPackage);
				if(sName == null) {
					System.err.println("Package name wasn't found.");
					return false;
				}
				sDescription = getNodeValueFromParent("description", domPackage);
				packageModel = new IndicationPackage(sName, sDescription);
				
				node = getNodeFromParent("Interfaces", domPackage);
				if(node != null)
					node = getNodeFromParent("interface", node);
				// For each Interface
				while( node != null ) {
					sName = getNodeValueFromParent("name", node);
					if(sName == null) {
						System.err.println("Interface name wasn't found.");
						return false;
					}
					sDescription = getNodeValueFromParent("description", node);
					if(sDescription == null) {
						System.err.println("Interface description wasn't found.");
						return false;
					}
					interfaceModel = new IndicationInterface(sName, sDescription);	
					packageModel.addInterface(interfaceModel);

					// Next sibling node
					node = getNodeFromSibling("interface", node.getNextSibling());
				}
				
				node = getNodeFromParent("Classes", domPackage);
				if(node != null)
					node = getNodeFromParent("class", node);
				// For each class
				while( node != null ) {
					sName = getNodeValueFromParent("name", node);
					if(sName == null) {
						System.err.println("Class name wasn't found.");
						return false;
					}
					sDescription = getNodeValueFromParent("description", node);
					if(sDescription == null) {
						System.err.println("Class description wasn't found.");
						return false;
					}
					classModel = new IndicationClass(sName, sDescription);	
					packageModel.addClass(classModel);

					// Next sibling node
					node = getNodeFromSibling("class", node.getNextSibling());
				}
				
				node = getNodeFromParent("Exceptions", domPackage);
				if(node != null)
					node = getNodeFromParent("exception", node);
				// For each exception
				while( node != null ) {
					sName = getNodeValueFromParent("name", node);
					if(sName == null) {
						System.err.println("Exception name wasn't found.");
						return false;
					}
					sDescription = getNodeValueFromParent("description", node);
					if(sDescription == null) {
						System.err.println("Exception description wasn't found.");
						return false;
					}
					exceptionModel = new IndicationException(sName, sDescription);	
					packageModel.addException(exceptionModel);

					// Next sibling node
					node = getNodeFromSibling("exception", node.getNextSibling());
				}
				indicationModel.addPackage(packageModel);
				
				// Go to next package
				domPackage = domPackage.getNextSibling();
			}
			
			// Accessing "Rules" node
			node = getNodeFromParent("Rules", domIndication);
			if(node != null)
				domRule = getNodeFromParent("rule", node);
			
			// For each rule
			while(domRule != null) {
				// Getting Target
				sTarget = getNodeValueFromParent("target", domRule);
				if(sTarget == null) {
					System.err.println("Target wasn't found for " + indicationModel.getName() + "indication");
					return false;
				}
				
				// Getting rule
				sRule = getNodeValueFromParent("matchingRule", domRule);
				if(sRule == null) {
					System.err.println("Rule wasn't found for " + indicationModel.getName() + "indication");
					return false;
				}
				if (sRule.equals("contains"))
					sRule = MatchText.CONTAINS;
				else if(sRule.equals("starts with"))
					sRule = MatchText.STARTS_WITH;
				else
					sRule = MatchText.ENDS_WITH;

				// Getting Case Sensity
				sCase = getNodeValueFromParent("caseSensity", domRule);
				if(sCase == null) {
					System.err.println("Case Sensity wasn't found for " + indicationModel.getName() + "indication");
					return false;
				}
				ruleModel = new MatchText(sTarget, sRule, sCase.equals("true"));
				
				// Getting words
				node = getNodeFromParent("Words", domRule);
				if(node == null) {
					System.err.println("Words node wasn't found for " + indicationModel.getName() + "indication");
					return false;
				}
				node = getNodeFromParent("word", node);
				if(node == null) {
					System.err.println("No word node was found for " + indicationModel.getName() + "indication");
					return false;
				}
				
				// For each word
				while(node != null) {
					if(node.getFirstChild() == null || node.getFirstChild().getNodeValue() == null) {
						System.err.println("No word node was found for " + indicationModel.getName() + "indication");
						return false;
					}
					ruleModel.addWord(node.getFirstChild().getNodeValue());
					
					// Go to next word
					node = getNodeFromSibling("word", node.getNextSibling());
				}
				
				indicationModel.addMatch(ruleModel);
				
				// Go to next rule
				domRule = getNodeFromSibling("rule", domRule.getNextSibling());
			}
			lstIndications.add(indicationModel);
		}
		return true;
	}
			
	/**
	 * It gets a node value from a parent node which name is described by name parameter.
	 * @param name Name of node to be found
	 * @param parent Parent node that contains a node to be found
	 * @return A string value of a node or null otherwise
	 */
	private String getNodeValueFromParent(String name, Node parent) {
		if(parent == null) {
			//System.err.println("Parent is null!");
			return null;
		}
		return getNodeValueFromSibling(name, parent.getFirstChild());
	}
	
	/**
	 * It gets a node value from a sibling node which name is described by name parameter.
	 * @param name Name of node to be found
	 * @param sibling Sibling node of the node to be found
	 * @return A string value of a node or null otherwise
	 */
	private String getNodeValueFromSibling(String name, Node sibling) {
		if(sibling == null) {
			//System.err.println("Sibling is null!");
			return null;
		}
		while((!sibling.getNodeName().equals(name)) && sibling.getNextSibling() != null)
			sibling = sibling.getNextSibling();
		
		if(!sibling.getNodeName().equals(name)) {
			System.err.println(name + " node wasn't found in file.");
			return null;
		}
		if(sibling.getFirstChild() == null) {
			System.err.println(name + " node value wasn't found in file.");
			return null;
		}
		if(sibling.getFirstChild().getNodeValue() == null) {
			System.err.println(name + " node value wasn't found in file.");
			return null;
		}
		return sibling.getFirstChild().getNodeValue();
	}
	
	/**
	 * It gets a node from a parent node which name is described by name parameter.
	 * @param name Name of node to be found
	 * @param parent Parent node of the node to be found
	 * @return Node which name is the same of name parameter or null otherwise 
	 */
	private Node getNodeFromParent(String name, Node parent) {
		if(parent == null) {
			//System.err.println("Parent is null!");
			return null;
		}
		return getNodeFromSibling(name, parent.getFirstChild());
	}
	
	/**
	 * It gets a node from a sibling node which name is described by name parameter.
	 * @param name Name of node to be found
	 * @param sibling Sibling node of the node to be found
	 * @return Node which name is the same of name parameter or null otherwise 
	 */
	private Node getNodeFromSibling(String name, Node sibling) {
		if(sibling == null) {
			//System.err.println("Sibling is null!");
			return null;
		}
		Node node = sibling;
		while((!node.getNodeName().equals(name)) && node.getNextSibling() != null)
			node = node.getNextSibling();
		
		if(!node.getNodeName().equals(name)) {
			//System.err.println(name + " node wasn't found in file.");
			return null;
		}
		return node;
	}
}
