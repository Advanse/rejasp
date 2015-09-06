package br.ufscar.dc.rejasp.indication.model;

import java.util.ArrayList;

/**
 * @author Daniel Kawakami
 * This class describes a pattern of indications for variable names or string content.
 * It storage a set of words that could be found in the begining, middle or ending of 
 * a variable name or string content.
 */
public class MatchText {
	public static final String VARIABLE_NAME = "rejasp.indication.variableName";
	public static final String STRING_LITERAL = "rejasp.indication.stringLiteral";
	public static final String CONTAINS = "rejasp.indication.contains";
	public static final String STARTS_WITH = "rejasp.indication.startsWith";
	public static final String ENDS_WITH = "rejasp.indication.endsWith";

	/**
	 * Target in which the text will match 
	 */
	private String sTarget;
	/**
	 * Describe the rule of match (considering similarities in begin, end or middle)
	 */
	private String sRule;
	/**
	 * It indicates if the match will be case sensity
	 */
	private boolean bCaseSensity;
	/**
	 * A collection of texts that must be match
	 */
	private ArrayList lstWords;
	
	public MatchText ( String sTarget, String sRule, boolean bCaseSensity ) {
		this.sTarget = sTarget;
		this.sRule = sRule;
		this.bCaseSensity = bCaseSensity;
		lstWords = new ArrayList();
	}
	public String getTarget() {
		return sTarget;
	}
	public String getRule() {
		return sRule;
	}
	public ArrayList getWords () {
		return (ArrayList)lstWords.clone();
	}
	public void setTarget(String sTarget) {
		this.sTarget = sTarget;
	}
	public void setRule(String sRule) {
		this.sRule = sRule;
	}
	public void setCaseSensity(boolean bCaseSensity) {
		this.bCaseSensity = bCaseSensity;
	}

	public boolean isCaseSensity () {
		return bCaseSensity;
	}
	public boolean addWord ( String sWord ) {
		if ( findWordIndex(sWord) == -1 ) {
			lstWords.add (sWord);
			return true;
		}
		else
			return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 * Return a clone of this object. 
	 */
	public Object clone() {
		MatchText clone = new MatchText(this.sTarget, this.sRule, this.bCaseSensity );
		clone.lstWords = new ArrayList();
		for (int i = 0; i < this.lstWords.size(); i++)
			clone.lstWords.add(new String((String)this.lstWords.get(i)));
		return clone;
	}
	
	public int findWordIndex(String word) {
		for ( int i = 0; i < lstWords.size(); i++ )
			if ( word.equals((String)lstWords.get(i)) )
				return i;
		return -1;
	}
	
	public boolean updateWord(String oldWord, String newWord) {
		int nIndex = findWordIndex(oldWord);
		if ( nIndex == -1 )
			return false;
		lstWords.remove(nIndex);
		lstWords.add(nIndex, newWord);
		return true;
	}
	
	public boolean removeWord(String word) {
		int nIndex = findWordIndex(word);
		if ( nIndex == -1 )
			return false;
		lstWords.remove(nIndex);
		return true;
	}
}
