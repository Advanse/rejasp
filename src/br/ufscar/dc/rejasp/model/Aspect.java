package br.ufscar.dc.rejasp.model;

import java.util.ArrayList;

public class Aspect {
	private String sCode;
	//private String sName;
	private ArrayList lstComments;
	//private String sIdentity;
	private ArrayList lstImports;
	
	public Aspect(String sCode) {
		this.sCode = sCode;
		lstComments = new ArrayList();
		lstImports = new ArrayList();
	}
	
	public ArrayList getImports() {
		return lstImports;
	}
	
	public String getCode() {
		return sCode;
	}
	
	/**
	 * @return Returns a list of comments. Type element
	 * is CommentInterval.
	 */
	public ArrayList getComments() {
		return lstComments;
	}
	
	public boolean importExists(String sImport) {
		String sPackage;
		for(int i = 0; i < lstImports.size(); i++)
			if (sImport.equals(lstImports.get(i)))
				return true;
			else if(((String)lstImports.get(i)).endsWith(".*")) {
				sPackage = (String)lstImports.get(i);
				sPackage = sPackage.substring(0, sPackage.length() - 2);
				if(sImport.substring(0, sImport.lastIndexOf('.')).equals(sPackage))
					return true;
			}
		return false;
	}
	
	public boolean parse() {
		boolean bReturn = true;
		if ( ! registryComments() )
			bReturn = false;;
		
		if ( ! registryImports() )
			bReturn = false;
		
		return bReturn;
	}
	
	/**
	 * Adds a import declaration on source code
	 * @param sImport string with import declaration without ';'
	 * @return -1 if fails or the index of import declaration in the string
	 * code.
	 */
	public int addImport(String sImport) {
		if(importExists(sImport))
			return -1;
		int nStartImport;
		int nIndex = findWord(0, "package");
		if (nIndex == -1)
			nStartImport = 0;
		else
			nStartImport = sCode.indexOf(';', nIndex) + 1;
		
		int nEndImport = findWord(nStartImport, "{");
		String sImportIdentation;
		
		nIndex = findWord(nStartImport, "import");
		if(nIndex > nEndImport)
			return -1;
		// Is there any import declaration
		if ( nIndex == -1 ) {
			sImportIdentation = "";
			nIndex = goToLineEndPosition(nStartImport) + 1;
			while(IsInsideComment(nIndex))
				nIndex = goToLineStartPosition(nIndex + 1 );
		}
		// there is no import declaration
		else {
			sImportIdentation = getIdentation(nIndex);
			if ( sImportIdentation == null )
				return -1;
			nIndex = findWord(nStartImport, "import");
			int nIndexWord;
			while( (nIndexWord = findWord(nIndex + 6, "import")) != -1)
				nIndex = nIndexWord;
			
			nIndex = goToLineEndPosition(nIndex) + 1;
			while(IsInsideComment(nIndex))
				nIndex = goToLineStartPosition(nIndex - 1);
		}
		
		// Adding import(s) in string that contains code
		sCode = sCode.substring(0, nIndex) + "import " + sImportIdentation + sImport + ";\n" + 
				sCode.substring(nIndex, sCode.length());
		offsetComments( nIndex, sImportIdentation.length() + sImport.length() + 9 );
		return nIndex + sImportIdentation.length();
	}
	
	/**
	 * Add method in the end of aspect code
	 * @param sMethod
	 * @return Returns index when the method starts
	 */
	public int addMethod(String sMethod) {
		// Find out the end of the aspect
		int nEndAspect = sCode.length();
		while (nEndAspect != -1 &&
				IsInsideComment( nEndAspect = sCode.substring(0, nEndAspect).lastIndexOf('}')))
			nEndAspect--;
		// nEndAspect is above '}' character
		int nIndex = goToLineStartPosition(nEndAspect);
		if(IsInsideComment(nIndex))
			nIndex = skipComment(nIndex);
		// Insert the method in this point
		String sIdentation = getIdentation();
		sCode = sCode.substring(0, nIndex) + '\n' + sIdentation + sMethod + 
				'\n' + sCode.substring(nIndex, sCode.length());
		offsetComments( nIndex, sIdentation.length() + sMethod.length() + 1 );
		return nIndex + sIdentation.length() + 1;
	}
	
	/**
	 * Skips all blank space, new line and tab.
	 * @param fromIndex
	 * @return Returns -1 when fromIndex is greater than
	 * source code length or when there isn't character.  
	 */
	private int nextCharacter( int fromIndex ) {
		while ( fromIndex < sCode.length() && (sCode.charAt(fromIndex) == ' ' ||
				sCode.charAt(fromIndex) == '\n' || sCode.charAt(fromIndex) == '\r' ||
				sCode.charAt(fromIndex) == '\t') )
			fromIndex++;
		if ( fromIndex < sCode.length() )
			return fromIndex;
		else
			return -1;
	}
	
	private boolean registryComments() {
		int nIndex = 0, nStart, nLength;
		while(nIndex < sCode.length() - 1)
			if ( sCode.charAt(nIndex) == '/' && sCode.charAt(nIndex + 1) == '/') {
				nStart = nIndex;
				nIndex += 2;
				while(nIndex < sCode.length() - 1 && sCode.charAt(nIndex) != '\n')
					nIndex++;
				if(sCode.charAt(nIndex) != '\n') {
					System.err.println("New line expected");
					return false;
				}
				nLength = nIndex - nStart + 1;
				lstComments.add(new CommentInterval(nStart, nLength, true));
			}
			else if ( sCode.charAt(nIndex) == '/' && sCode.charAt(nIndex + 1) == '*') {
				nStart = nIndex;
				nIndex += 2;
				while(nIndex < sCode.length() - 1 && (sCode.charAt(nIndex) != '*' || 
						sCode.charAt(nIndex + 1) != '/'))
					nIndex++;
				if(nIndex >= sCode.length()) {
					System.err.println("Multi line comment termination expected");
					return false;
				}
				nIndex++;
				nLength = nIndex - nStart + 1;
				lstComments.add(new CommentInterval(nStart, nLength, false));
			}
			else
				nIndex++;
		return true;
	}
	
	private boolean registryImports () {
		int nStartImport;
		int nIndex = findWord(0, "package");
		int nAux;
		if (nIndex == -1)
			nStartImport = 0;
		else
			nStartImport = sCode.indexOf(';', nIndex);
		
		int nEndImport = findWord(nStartImport, "{");
		nIndex = findWord(nStartImport, "import");
		while ( nIndex != -1 && nIndex < nEndImport ) {
			nIndex += 6; 
			nIndex = nextCharacter(nIndex);
			if (nIndex != -1) {
				nAux = findWord(nIndex, ";");
				if ( nAux != -1 ) {
					lstImports.add(getTextWithoutComment(nIndex, nAux - 1));
					nIndex = findWord(nAux + 1, "import");
				}
				else return false;
			}
		}
		return true;
	}
	
	private boolean IsInsideComment( int nIndex ) {
		CommentInterval interval;
		for(int i = 0; i < lstComments.size(); i++) {
			interval = (CommentInterval)lstComments.get(i);
			if (interval.contains(nIndex))
				return true;
		}
		return false;
	}
	
	/**
	 * It finds an index of the first ocurrence of a word, skipping all comments.
	 * @param nFrom an initial index where a search will start
	 * @param sWord word to be searched
	 * @return index of the first ocurrence of a word, skipping all comments 
	 * or -1 if none
	 */
	private int findWord ( int nFrom, String sWord ) {
		int nIndex = sCode.indexOf(sWord, nFrom);
		if (nIndex == -1)
			return -1;
		else {
			while ( nIndex != -1 && IsInsideComment(nIndex) ) {
				nIndex += sWord.length();
				nIndex = sCode.indexOf(sWord, nIndex);
			}
			return nIndex;
		}
	}
	
	private String getTextWithoutComment( int nFrom, int nEnd ) {
		String sResult = "";
		while ( nFrom <= nEnd) {
			if ( ! IsInsideComment(nFrom) )
				sResult += sCode.charAt(nFrom);
			nFrom++;
		}
		return sResult;
	}
	
	/**
	 * @param index Index of the first character after identation
	 * @return Identation Strin or null if identation was not found
	 */
	private String getIdentation(int index) {
		//int nTab = 0, nSpace = 0;
		index--;
		String sIdentation = "";
		while( index >= 0 && ( sCode.charAt(index) == ' ' ||
				sCode.charAt(index) == '\t'))
			sIdentation += sCode.charAt(index--); 
		if(index < 0 || sCode.charAt(index) != '\n')
			return null;
		return sIdentation;
	}
	
	private int goToLineStartPosition(int index) {
		while(index > 0 && sCode.charAt(index - 1) != '\n')
			index--;
		if(sCode.charAt(index - 1) == '\n')
			return index;
		else
			return -1;
	}
	
	private int goToLineEndPosition(int index) {
		while(index < sCode.length() && sCode.charAt(index) != '\n')
			index++;
		if(sCode.charAt(index) == '\n')
			return index;
		else
			return -1;
	}
	
	/**
	 * Shift the comments begining a position.
	 * @param fromIndex Index of the begining position.
	 * @param length Number of positions to be shifted.
	 */
	public void offsetComments(int fromIndex, int length) {
		// find comments that will be affected
		int i;
		CommentInterval interval;
		for( i = 0; i < lstComments.size(); i++ ) {
			interval = (CommentInterval)lstComments.get(i);
			if(interval.getStart() >= fromIndex ) {
				for( int j = i; j < lstComments.size(); j++ ) {
					interval = (CommentInterval)lstComments.get(j);
					interval.setStart(interval.getStart() + length);
				}
				break;
			}
		}
		if( i > 0 && ((CommentInterval)lstComments.get(i - 1)).contains(fromIndex)) {
			interval = (CommentInterval)lstComments.get(i - 1);
			interval.setLength(interval.getLength() + length);
		}
	}
	
	/**
	 * Get a string identation of a member of aspect (field, method, pointcut, advice)
	 * @return
	 */
	public String getIdentation() {
		int nBeginAspect = getBeginAspect();
		int nBeginBlock = findWord(0, "{");
		
		// Find identation of first element:
		int nCurrentIndex = nBeginBlock + 1;
		nCurrentIndex = nextCharacter(nCurrentIndex);
		while(IsInsideComment(nCurrentIndex)) {
			nCurrentIndex = skipComment(nCurrentIndex);
			nCurrentIndex = nextCharacter(nCurrentIndex);
		}
		
		String sIdentation;
		int nSpaceCount = 0, nTabCount = 0;
		boolean bEmptyAspect = false;
		int nRightLimit;
		if ( sCode.charAt(nCurrentIndex) == '}' ) { // if aspect is empty
			bEmptyAspect = true;
			// get aspect identation
			nRightLimit = nBeginAspect;
			nCurrentIndex = goToLineStartPosition(nRightLimit);
		}
		else {
			nRightLimit = nCurrentIndex;
			nCurrentIndex = goToLineStartPosition(nCurrentIndex);
		}
		
		// Count tabs and newlines
		do {
			nCurrentIndex = skipComment(nCurrentIndex);
			while ( nCurrentIndex < nRightLimit && sCode.charAt(nCurrentIndex) == ' ' ){
				nSpaceCount++;
				nCurrentIndex++;
			}
			while ( nCurrentIndex < nRightLimit && sCode.charAt(nCurrentIndex) == '\t' ){
				nTabCount++;
				nCurrentIndex++;
			}
		}while ( nCurrentIndex < nRightLimit && 
				( sCode.charAt(nCurrentIndex) == '/' ||
				  sCode.charAt(nCurrentIndex) == ' ' ||
				  sCode.charAt(nCurrentIndex) == '\t' ));
		sIdentation = "";
		for ( int i = 0; i < nSpaceCount; i++ ) {
			sIdentation += " ";
		}
		for ( int i = 0; i < nTabCount; i++ ) {
			sIdentation += '\t';
		}
		if(bEmptyAspect)
			// Identation ITD (intertype declaration) is double of aspect identation
			sIdentation += sIdentation;
		
		if(sIdentation.length() == 0)
			sIdentation = "\t";
		return sIdentation;
	}
	
	/**
	 * Get the next closest index that points to a position without comment. 
	 * @param index
	 * @return
	 */
	private int skipComment(int index) {
		CommentInterval interval;
		for(int i = 0; i < lstComments.size(); i++) {
			interval = (CommentInterval)lstComments.get(i);
			if (interval.contains(index)) {
				index = interval.getStart() + interval.getLength();
				break;
			}
		}
		return index;
	}
	
	/**
	 * Get a list of pointcut names.
	 * @return
	 */
	public ArrayList getPointcutNames() {
		ArrayList lstPointcut = new ArrayList();
		int nIndex = 0;
		String sName;
		while( (nIndex = findWord(nIndex, "pointcut ")) != -1 ) {
			nIndex += 9;
			nIndex = nextCharacter(nIndex);
			sName = "";
			while(nIndex < sCode.length() && sCode.charAt(nIndex) != ' ' && sCode.charAt(nIndex) != '(') {
				nIndex = skipComment(nIndex);
				sName += sCode.charAt(nIndex);
				nIndex++;
			}
			if( nIndex == sCode.length() )
				System.err.println("Wrong aspect definition");
			else
				lstPointcut.add(sName);
		}
		return lstPointcut;
	}
	
	/**
	 * Add pointcut in the end of aspect code. It's look like addMethod, but
	 * it doesn't change the internal code of this class.
	 * @param pointcut Pointcut object.
	 * @return Returns index when the pointcut starts.
	 */
	public int addPointcut(Pointcut pointcut) {
		// Find out the end of the aspect
		int nEndAspect = sCode.length();
		while (nEndAspect != -1 &&
				IsInsideComment( nEndAspect = sCode.substring(0, nEndAspect).lastIndexOf('}')))
			nEndAspect--;
		// nEndAspect is above '}' character
		int nIndex = goToLineStartPosition(nEndAspect);
		if(IsInsideComment(nIndex))
			nIndex = skipComment(nIndex);
		// Insert the method in this point
		return nIndex;
	}
	
	
	/**
	 * This method was created in order to avoid problems when word aspect was used in
	 * package or import statements.
	 * @return An index that represents the aspect beginning, after "aspect <aspect_name> {"
	 */
	private int getBeginAspect() {
		int nBeginBlock = findWord(0, "{");
		if (nBeginBlock == -1) {
			System.err.println("Block beginning was expected");
			return -1;
		}
		
		// Getting begin and end of package statement
		int nBeginPackage = findWord(0, "package"), nEndPackage;
		if( nBeginPackage == -1 )
			// Source code is in default package 
			nEndPackage = 0;
		else {
			nEndPackage = findWord(nBeginPackage, ";");
			if (nEndPackage == -1) {
				System.err.println("End of package was expected");
				return -1;
			}
		}
		
		// Getting begin and end of the first and last import statement
		int nBeginImport = findWord(nEndPackage, "import");
		// Couldn´t be word aspect, because this word can be used in import statement
		int nEndImport = 0;
		
		if(nBeginImport == -1 || nBeginImport > nBeginBlock) {	
			// There is no import statement
			nBeginImport = -1;
			nEndImport = 0;
		}
		else {
			// There is imports
			int nIndex = nBeginImport;
			while (nIndex != -1) {
				// Now, this index will be used to store the last valid import  
				nEndImport = nIndex;
				
				nIndex = findWord(nIndex, ";");
				if(nIndex == -1) {
					System.err.println("Unexpected end of import statement");
					return -1;
				}
				nIndex = findWord(nIndex, "import");
			}
			
			// Updating last import index
			nEndImport = findWord(nEndImport, ";");
			if(nEndImport == -1) {
				System.err.println("Unexpected end of import statement in last import statement");
				return -1;
			}
		}

		// Getting the aspect begging
		int nBeginAspect = 0;
		
		// Existe package statement?
		if(nBeginPackage != -1)
			nBeginAspect = nEndPackage;
		
		// Existe import statement?
		if(nBeginImport != -1) {
			// O final de pacote não é maior que o começo do import 
			if(nBeginAspect > nBeginImport) {
				// Se for, algo está errado
				System.err.println("End index for package is greater than beginning index for imports");
				return -1;
			}
			nBeginAspect = nEndImport;
		}
		
		// Final de pacote e/ou final de imports não é maior que o início de bloco de aspecto
		if(nBeginAspect > nBeginBlock) {
			// Se for, algo está errado
			System.err.println("End index for package/imports is greater than beginning index for aspect block");
			return -1;
		}
		
		// Entre final de package/import e início de bloco, a palavra reservada aspect deve existir
		int nIndex = findWord(nBeginAspect, "aspect");
		if(nIndex == -1) {
			System.err.println("Word \"aspect\" was expected");
			return -1;
		}
		if(nIndex > nBeginBlock) {
			System.err.println("Word \"aspect\" was expected before aspect block");
			return -1;
		}
		return nIndex;
	}
}
