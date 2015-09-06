package br.ufscar.dc.rejasp.model.ASTNodeInfo;

import java.util.ArrayList;

public class FieldInfo extends ModelInfo {
	private String sName;
	private String sType;
	private String sModifiers;
	private String sInitializer;
	private FileInfo fileParent;
	private TypeInfo typeParent;
	
	public FieldInfo() {
		super();
		fileParent = null;
		typeParent = null;
	}

	public FieldInfo(String sName, String sType, String sModifiers, String sInitializer) {
		super();
		this.sName = sName;
		this.sType = sType;
		this.sModifiers = sModifiers;
		this.sInitializer = sInitializer;
		fileParent = null;
		typeParent = null;
	}
	
	public String getName() {
		return sName;
	}
	
	public String getType() {
		return sType;
	}
	
	public String getModifiers() {
		return sModifiers;
	}
	
	public String getInitializer() {
		return sInitializer;
	}
	
	public FileInfo getFile() {
		return fileParent;
	}
	
	public TypeInfo getTypeParent() {
		return typeParent;
	}
	
	/**
	 * @return Returns a list of enclosured types.
	 */
	public ArrayList getEnclosuredTypes() {
		ArrayList lstTypes = new ArrayList();
		TypeInfo typeInfo = typeParent;
		while( typeInfo != null ) {
			lstTypes.add(typeInfo);
			typeInfo = typeInfo.getTypeParent();
		}
		// Inverting list
		ArrayList lstReturn = new ArrayList();
		for ( int i = lstTypes.size() - 1; i >= 0 ; i-- ) {
			lstReturn.add(lstTypes.get(i));
		}
		return lstReturn;
	}
	
	public void setFile( FileInfo fileParent ) {
		this.fileParent = fileParent;
	}
	
	public void setTypeParent( TypeInfo typeParent ) {
		this.typeParent = typeParent;
	}
}
