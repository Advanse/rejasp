package br.ufscar.dc.rejasp.model.ASTNodeInfo;

import java.util.ArrayList;

public class TypeInfo extends ModelInfo {
	private String sName;
	private String sLocale;
	private String sModifiers;
	private boolean bInterface;
	/**
	 * It indicates the type has parents in its declaration by using
	 *  keywords: "extends" and/or "implements"
	 */
	private boolean bSubType;
	private TypeInfo typeParent;
	private FileInfo fileParent;
	private ArrayList lstTypeChildren;
	private ArrayList lstFields;
	private ArrayList lstMethods;
	
	public TypeInfo() {
		super();
		typeParent = null;
		fileParent = null;
		lstTypeChildren = new ArrayList();
		lstFields = new ArrayList();
		lstMethods = new ArrayList();
		bSubType = false;
	}
	
	public TypeInfo( String sName, String sModifiers, boolean bInterface ) {
		super();
		this.sName = sName;
		this.sLocale = "";
		this.sModifiers = sModifiers;
		this.bInterface = bInterface;
		typeParent = null;
		fileParent = null;
		lstTypeChildren = new ArrayList();
		lstFields = new ArrayList();
		lstMethods = new ArrayList();
		bSubType = false;
	}
	
	public String getName() {
		return sName;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLocale() {
		if ( sLocale.length() == 0 ) {
			if(typeParent != null)
				sLocale = "\\" +typeParent.getQualifiedName().replace('.', '\\');
			else
				sLocale = "";
			sLocale = "\\" + fileParent.getPackage().replace('.', '\\') + 
						sLocale;
		}
		return sLocale;
	}
	
	public String getModifiers() {
		return sModifiers;
	}
	
	public boolean isInterface() {
		return bInterface;
	}
	
	public boolean isSubType() {
		return bSubType;
	}
	
	public TypeInfo getTypeParent() {
		return typeParent;
	}
	
	public FileInfo getFile() {
		return fileParent;
	}

	public ArrayList getChildren() {
		return lstTypeChildren;
	}

	public ArrayList getFields() {
		return lstFields;
	}

	public ArrayList getMethods() {
		return lstMethods;
	}
	
	public String getQualifiedName() {
		ArrayList lstTypes = new ArrayList();
		TypeInfo parent = typeParent;
		while(parent != null) {
			lstTypes.add(parent.sName);
			parent = parent.typeParent;
		}
		String sQualifiedName = "";
		for(int i = lstTypes.size() - 1; i >= 0; i--)
			sQualifiedName += (String)lstTypes.get(i) + '.';
		sQualifiedName += sName;
		return sQualifiedName;
	}
	
	/**
	 * @return Returns true if there is at least one field with indication.
	 */
	public boolean indicationFieldExist() {
		for(int i = 0; i < lstFields.size(); i++)
			if(((FieldInfo)lstFields.get(i)).getIndication())
				return true;
		return false;
	}

	/**
	 * @return Returns true if there is at least one method with indication.
	 */
	public boolean indicationMethodExist() {
		for(int i = 0; i < lstMethods.size(); i++)
			if(((MethodInfo)lstMethods.get(i)).getIndication())
				return true;
		return false;
	}

	/**
	 * It is not intended this method not be used.
	 * @param fileParent
	 */
	public void setFile(FileInfo fileParent) {
		this.fileParent = fileParent;
		for(int i = 0; i < lstTypeChildren.size(); i++)
			((TypeInfo)lstTypeChildren.get(i)).setFile(fileParent);
		for(int i = 0; i < lstFields.size(); i++)
			((FieldInfo)lstFields.get(i)).setFile(fileParent);
		for(int i = 0; i < lstMethods.size(); i++)
			((MethodInfo)lstMethods.get(i)).setFile(fileParent);
	}
	
	public void addType( TypeInfo typeChild ) {
		lstTypeChildren.add(typeChild);
		typeChild.setTypeParent(this);
		//typeChild.setFile(this.getFile());
		if( this.getFile() != null )
			this.getFile().addType(typeChild);
	}
	
	public void setSubType(boolean bSubType ) {
		this.bSubType = bSubType;
	}
	
	/**
	 * Sets type.
	 * @param typeParent
	 */
	private void setTypeParent(TypeInfo typeParent) {
		this.typeParent = typeParent;
	}
	
	public void addField(FieldInfo fieldInfo) {
		lstFields.add(fieldInfo);
		fieldInfo.setTypeParent(this);
		fieldInfo.setFile(this.getFile());
	}
	
	public void addMethod(MethodInfo methodInfo) {
		lstMethods.add(methodInfo);
		methodInfo.setType(this);
		methodInfo.setFile(this.getFile());
	}
	
	public int getRefactoring() {
		int refactoring = 0;
		MethodInfo methodInfo;
		for(int i = 0; i < lstMethods.size(); i++) {
			methodInfo = (MethodInfo)lstMethods.get(i);
			refactoring |= methodInfo.getRefactoring();
		}
		return refactoring;
	}
}
