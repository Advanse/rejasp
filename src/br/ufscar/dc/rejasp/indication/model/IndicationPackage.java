package br.ufscar.dc.rejasp.indication.model;

import java.util.ArrayList;

/**
 * @author Daniel Kawakami
 * An indication can be described by some types (class / interface / exceptions).
 * These types could be imported from other packages. So these packages and types 
 * are used as libraries. This class storage the types of a package so that these 
 * types are indications. 
 */
public class IndicationPackage {
	/**
	 * Package name.
	 */
	private String sName;
	/**
	 * Package Description
	 */
	private String sDescription;
	/**
	 * A list od registered interfaces of IndicationInterface
	 */
	private ArrayList lstInterfaces;
	/**
	 * A list of registered classes of IndicationClass
	 */
	private ArrayList lstClasses;
	/**
	 * A list of registered exceptions of IndicationException
	 */
	private ArrayList lstExceptions;
	
	public IndicationPackage (String sPackage, String sDescription) {
		this.sName = sPackage;
		this.sDescription = sDescription;
		lstInterfaces = new ArrayList();
		lstClasses = new ArrayList();
		lstExceptions = new ArrayList();
	}
	public String getName() {
		return sName;
	}
	public String getDescription() {
		return sDescription;
	}
	public ArrayList getInterfaces() {
		return (ArrayList)lstInterfaces.clone();
	}
	public ArrayList getClasses() {
		return (ArrayList)lstClasses.clone();
	}
	public ArrayList getExceptions() {
		return (ArrayList)lstExceptions.clone();
	}
	public void setName(String sPackage) {
		this.sName = sPackage;
	}
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}
	public void setInterfaces(ArrayList lstInterfaces) {
		this.lstInterfaces = lstInterfaces;
	}
	public void setClasses(ArrayList lstClasses) {
		this.lstClasses = lstClasses;
	}
	public void setExceptions(ArrayList lstExceptions) {
		this.lstExceptions = lstExceptions;
	}
	
	/**
	 * Return an interface that matches a name or null otherwise.
	 * @param key
	 * @return
	 */
	public IndicationInterface findInterface(String key) {
		for ( int i = 0; i < lstInterfaces.size(); i++ )
			if ( ((IndicationInterface)lstInterfaces.get(i)).getName().equals(key) )
				return (IndicationInterface)lstInterfaces.get(i);
		return null;
	}
	
	/**
	 * Return a class that matches a name or null otherwise.
	 * @param key
	 * @return
	 */
	public IndicationClass findClass(String key) {
		for ( int i = 0; i < lstClasses.size(); i++ )
			if ( ((IndicationClass)lstClasses.get(i)).getName().equals(key) )
				return (IndicationClass)lstClasses.get(i);
		return null;
	}
	
	/**
	 * Return an exception  that matches a name or null otherwise.
	 * @param key
	 * @return
	 */
	public IndicationException findException(String key) {
		for ( int i = 0; i < lstExceptions.size(); i++ )
			if ( ((IndicationException)lstExceptions.get(i)).getName().equals(key) )
				return (IndicationException)lstExceptions.get(i);
		return null;
	}

	/**
	 * Add an indication interface with an unique name. 
	 * @param element
	 * @return true if the indication interface was added or false otherwise.
	 */
	public boolean addInterface(IndicationInterface element) {
		if ( findInterface(element.getName()) == null) {
			lstInterfaces.add(element);
			return true;
		}
		return false;
	}
	
	/**
	 * Add an indication class with an unique name.
	 * @param element
	 * @return true if the indication class was added or false otherwise. 
	 */
	public boolean addClass(IndicationClass element) {
		if ( findClass(element.getName()) == null) {
			lstClasses.add(element);
			return true;
		}
		return false;
	}

	/**
	 * Add an indication exception with an unique name.
	 * @param element
	 * @return true if the indication exception was added or false otherwise. 
	 */
	public boolean addException(IndicationException element) {
		if ( findException(element.getName()) == null) {
			lstExceptions.add(element);
			return true;
		}
		return false;
	}
	
	/**
	 * Update an interface identified by key. 
	 * @param key name of element to be updated
	 * @param newName name to be updated
	 * @param newDescription description to be updated
	 * @return true if the interface was updated or false otherwise.
	 */
	public boolean updateInterface(String key, String newName, String newDescription ) {
		IndicationInterface oldElement = findInterface(key);
		if ( oldElement == null )
			return false;
		IndicationInterface duplicatedElement = findInterface(newName);
		if ( duplicatedElement != null && duplicatedElement != oldElement )
			return false;
		oldElement.setName(newName);
		oldElement.setDescription(newDescription);
		return true;
	}

	/**
	 * Update a class identified by key. 
	 * @param key name of element to be updated
	 * @param newName name to be updated
	 * @param newDescription description to be updated
	 * @return true if the class was updated or false otherwise.
	 */
	public boolean updateClass(String key,  String newName, String newDescription ) {
		IndicationClass oldElement = findClass(key);
		if ( oldElement == null )
			return false;
		IndicationClass duplicatedElement = findClass(newName);
		if ( duplicatedElement != null && duplicatedElement != oldElement )
			return false;
		oldElement.setName(newName);
		oldElement.setDescription(newDescription);
		return true;
	}
	
	/**
	 * Update an exception identified by key. 
	 * @param key name of element to be updated
	 * @param newName name to be updated
	 * @param newDescription description to be updated
	 * @return true if the exception was updated or false otherwise.
	 */
	public boolean updateException(String key,  String newName, String newDescription ) {
		IndicationException oldElement = findException(key);
		if ( oldElement == null )
			return false;
		IndicationException duplicatedElement = findException(newName);
		if ( duplicatedElement != null && duplicatedElement != oldElement )
			return false;
		oldElement.setName(newName);
		oldElement.setDescription(newDescription);
		return true;
	}

	/**
	 * Remove an interface.
	 * @param key key of interface that must be removed
	 * @return removed interface or null if no interface was removed
	 */
	public IndicationInterface removeInterface(String key) {
		for ( int i = 0; i < lstInterfaces.size(); i++ )
			if ( ((IndicationInterface)lstInterfaces.get(i)).getName().equals(key) )
				return (IndicationInterface)lstInterfaces.remove(i);
		return null;
	}

	/**
	 * Remove a class.
	 * @param key key of class that must be removed
	 * @return removed class or null if no class was removed
	 */
	public IndicationClass removeClass(String key) {
		for ( int i = 0; i < lstClasses.size(); i++ )
			if ( ((IndicationClass)lstClasses.get(i)).getName().equals(key) )
				return (IndicationClass)lstClasses.remove(i);
		return null;
	}

	/**
	 * Remove an exception.
	 * @param key key of exception that must be removed
	 * @return removed exception or null if no exception was removed
	 */
	public IndicationException removeException(String key) {
		for ( int i = 0; i < lstExceptions.size(); i++ )
			if ( ((IndicationException)lstExceptions.get(i)).getName().equals(key) )
				return (IndicationException)lstExceptions.remove(i);
		return null;
	}

	/**
	 * Verify if a type belong to a package. This type can be an interface, class or
	 * exception.
	 * @param sType type described as a string
	 * @return true if the type belong to the package or false, otherwise.
	 */
	public boolean isType (String sType) {
		if ( sType.equalsIgnoreCase("*") )
			return true;
		for ( int i = 0; i < lstInterfaces.size(); i++  )
			if ( ((IndicationInterface)lstInterfaces.get(i)).getName().compareTo(sType) == 0 )
				return true;
		for ( int i = 0; i < lstClasses.size(); i++  )
			if ( ((IndicationClass)lstClasses.get(i)).getName().compareTo(sType) == 0 )
				return true;
		for ( int i = 0; i < lstExceptions.size(); i++  )
			if ( ((IndicationException)lstExceptions.get(i)).getName().compareTo(sType) == 0 )
				return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 * Return a clone of this object.  
	 */
	public Object clone() {
		IndicationPackage clone = new IndicationPackage(this.sName, this.sDescription);
		
		// Clone list of interfaces
		clone.lstInterfaces = new ArrayList();
		for (int i = 0; i < this.lstInterfaces.size(); i++)
			clone.lstInterfaces.add(((IndicationInterface)this.lstInterfaces.get(i)).clone());
		
		// Clone list of classes
		clone.lstClasses = new ArrayList();
		for (int i = 0; i < this.lstClasses.size(); i++)
			clone.lstClasses.add(((IndicationClass)this.lstClasses.get(i)).clone());
		
		// Clone list of exceptions
		clone.lstExceptions = new ArrayList();
		for (int i = 0; i < this.lstExceptions.size(); i++)
			clone.lstExceptions.add(((IndicationException)this.lstExceptions.get(i)).clone());
		return clone;
	}
	
	/**
	 * @return Returns a list of strings that contains all names of interfaces and classes.
	 */
	public ArrayList getTypes() {
		ArrayList lstTypes = new ArrayList();
		IndicationClass classObj;
		IndicationInterface interfaceObj;
		// For each interface
		for ( int i = 0; i < lstInterfaces.size(); i++ ) {
			interfaceObj = (IndicationInterface)lstInterfaces.get(i);
			lstTypes.add(interfaceObj.getName());
		}
		// For each class
		for ( int i = 0; i < lstClasses.size(); i++ ) {
			classObj = (IndicationClass)lstClasses.get(i);
			lstTypes.add(classObj.getName());
		}
		return lstTypes;
	}
}
