package br.ufscar.dc.rejasp.indication.model;

import java.util.ArrayList;

/**
 * @author Daniel Kawakami
 * An indication can be described as a set of LibraryIndications and MatchTexts.
 * This structure represents the indication model part.
 */
public class Indication {
	/**
	 * Name of indication
	 */
	private String sName;
	/**
	 * Description of indication
	 */
	private String sDescription;
	/**
	 * Indicates if this indication must be considered in the indication detection
	 */
	private boolean bActive;
	/**
	 * Collection of packages that could be imported in source code and which types
	 * are indications.
	 */
	private ArrayList lstPackages;
	/**
	 * Collection of patterns like variable name and string literal that are 
	 * aspect indications. 
	 */
	private ArrayList lstMatches;
	
	public Indication ( String sName ) {
		this.sName = sName;
		bActive = false;
		lstPackages = new ArrayList();
		lstMatches = new ArrayList();
	}
	public String getName() {
		return sName;
	}
	public String getDescription() {
		return sDescription;
	}
	public boolean isActive() {
		return bActive;
	}
	public ArrayList getPackages() {
		return (ArrayList)lstPackages.clone();
	}
	public ArrayList getMatches() {
		return (ArrayList)lstMatches.clone();
	}
	public void setName(String sName) {
		this.sName = sName;
	}
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}
	public void setActive(boolean bActive) {
		this.bActive = bActive;
	}
	public void setPackages(ArrayList lstLibraries) {
		this.lstPackages = lstLibraries;
	}
	public void setMatches(ArrayList lstMatches) {
		this.lstMatches = lstMatches;
	}
	public void addMatch(MatchText match) {
		lstMatches.add(match);
	}
	
	/**
	 * Remove and return an object of MatchText at nIndex position or null if
	 * nIndex is out of range.
	 * @param nIndex
	 * @return
	 */
	public MatchText removeMatch(int nIndex) {
		if ( nIndex < 0 || nIndex >= lstMatches.size() )
			return null;
		return (MatchText)lstMatches.remove(nIndex);
	}
	
	/**
	 * Return an object of MatchText at nIndex position or null if
	 * nIndex is out of range.
	 * @param nIndex
	 * @return
	 */
	public MatchText getMatch(int nIndex) {
		if ( nIndex < 0 || nIndex >= lstMatches.size() )
			return null;
		return (MatchText)lstMatches.get(nIndex);
	}
	
	public int getMatchIndex(MatchText matchText) {
		for(int i = 0; i < lstMatches.size(); i++)
			if(matchText.equals(lstMatches.get(i)))
				return i;
		return -1;
	}
	
	public boolean updateMatch(MatchText matchText, int index) {
		if ( removeMatch(index) != null ) {
			lstMatches.add(index, matchText);
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 * Return a clone of this indication.
	 */
	public Object clone() {
		Indication clone = new Indication(this.sName);
		clone.bActive = this.bActive;
		clone.sDescription = this.sDescription;
		// Clone libraries
		clone.lstPackages = new ArrayList();
		for ( int i = 0; i < this.lstPackages.size(); i++ )
			clone.lstPackages.add(((IndicationPackage)this.lstPackages.get(i)).clone());
		
		clone.lstMatches = new ArrayList();
		// Clone matches
		for ( int i = 0; i < this.lstMatches.size(); i++ )
			clone.lstMatches.add(((MatchText)this.lstMatches.get(i)).clone());
		return clone;
	}
	
	/**
	 * Find a package by name (key).
	 * @param key package name
	 * @return package or null if no package was found
	 */
	public IndicationPackage findPackage(String key) {
		for ( int i = 0; i < lstPackages.size(); i++ )
			if ( key.equals(((IndicationPackage)lstPackages.get(i)).getName()) )
				return (IndicationPackage)lstPackages.get(i);
		return null;
	}

	/**
	 * Add an indication package with an unique name.
	 * @param element
	 * @return true if the indication package was added or false otherwise.
	 */
	public boolean addPackage(IndicationPackage element) {
		if ( findPackage(element.getName()) == null) {
			lstPackages.add(element);
			return true;
		}
		return false;
	}

	/**
	 * Update a package identified by key. 
	 * @param key name of element to be updated
	 * @param newElement element that contains all new data to be update
	 * @return true if the package was updated or false otherwise.
	 */
	public boolean updatePackage(String key, IndicationPackage newElement ) {
		IndicationPackage oldElement = findPackage(key);
		if ( oldElement == null )
			return false;
		IndicationPackage duplicatedElement = findPackage(newElement.getName());
		if ( duplicatedElement != null && duplicatedElement != oldElement )
			return false;
		oldElement.setName(newElement.getName());
		oldElement.setDescription(newElement.getDescription());
		return true;
	}

	/**
	 * Remove an package that matches to the argument key
	 * @param key package name
	 * @return removed package or null if no package was found
	 */
	public IndicationPackage removePackage(String key) {
		for ( int i = 0; i < lstPackages.size(); i++ )
			if ( key.equals(((IndicationPackage)lstPackages.get(i)).getName()) )
				return (IndicationPackage)lstPackages.remove(i);
		return null;
	}

	/**
	 * @return Returns a list of strings that contains all names of interfaces and classes.
	 */
	public ArrayList getTypes() {
		ArrayList lstTypes = new ArrayList();
		// for each package
		for( int i = 0; i < lstPackages.size(); i++ )
			lstTypes.addAll(((IndicationPackage)lstPackages.get(i)).getTypes());
		return lstTypes;
	}
	
	public boolean isType ( String sType ) {
		
		int nIdentifier = sType.lastIndexOf('.') + 1;
		if ( nIdentifier > 1 ) {
			String sPackage = sType.substring(0, nIdentifier - 1);
			IndicationPackage indicationPackage = findPackage(sPackage);
			if ( indicationPackage != null ) {
				return indicationPackage.isType(sType.substring(nIdentifier));
			}
		}
		return false;
	}
}
