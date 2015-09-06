package br.ufscar.dc.rejasp.model.ASTNodeInfo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class MethodInfo extends ModelInfo{
	private String sName;
	private String sReturnType;
	private String sModifiers;
	private ArrayList lstParameters;
	private boolean bConstructor;
	private FileInfo fileParent;
	private TypeInfo typeParent;
	private ArrayList lstIndicationTypes;
	private MethodRefactoringInfo refactoringInfo;
	
	public MethodInfo() {
		super();
		typeParent = null;
		fileParent = null;
		lstParameters = new ArrayList();
	}

	public MethodInfo(String sName, String sReturnType, String sModifiers) {
		super();
		lstParameters = new ArrayList();
		this.sName = sName;
		this.sReturnType = sReturnType;
		this.sModifiers = sModifiers;
		typeParent = null;
		fileParent = null;
	}
	
	public String getName() {
		return sName;
	}

	/**
	 * @return Returns a string that represents method type. If empty
	 * string is returned, this method is a constructor.
	 */
	public String getReturnType() {
		return sReturnType;
	}

	public String getModifiers() {
		return sModifiers;
	}

	public ArrayList getParameters() {
		return lstParameters;
	}

	public FileInfo getFile() {
		if(fileParent == null)
			fileParent = typeParent.getFile();
		return fileParent;
	}

	public TypeInfo getType() {
		return typeParent;
	}
	
	public ArrayList getIndicationTypes() {
		return lstIndicationTypes;
	}
	
	public MethodRefactoringInfo getRefactoringInfo() {
		return refactoringInfo;
	}
	
	public boolean isConstructor() {
		return bConstructor;
	}

	public void setFile(FileInfo file) {
		this.fileParent = file;
	}
	
	public void setType(TypeInfo type) {
		this.typeParent = type;
	}
	
	public void setIndicationTypes(ArrayList lstIndicationTypes) {
		this.lstIndicationTypes = lstIndicationTypes;
	}
	
	public void setConstructor(boolean bConstructor) {
		this.bConstructor = bConstructor;
	}
	
	public void setRefactoringInfo(MethodRefactoringInfo refactoringInfo) {
		this.refactoringInfo = refactoringInfo;
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

	public int getRefactoring() {
		if (refactoringInfo == null)
			return 0;
		return refactoringInfo.getAllMethodRefactoring();
	}
	
	public void setParameters(List parameters) {
		SingleVariableDeclaration parameter;
		ParameterInfo parameterInfo;
		for ( int i = 0; i < parameters.size(); i++ ) {
			parameter = (SingleVariableDeclaration)parameters.get(i);
			parameterInfo = new ParameterInfo(parameter.getType().toString(), parameter.getName().getIdentifier(), this);
			lstParameters.add(parameterInfo);
		}
	}

	public void setParameters(ArrayList parameters) {
		lstParameters = parameters;
	}
	
	public boolean equalsParameterList(List parameters) {
		SingleVariableDeclaration parameter;
		ParameterInfo parameterInfo;
		if(parameters.size() != lstParameters.size())
			return false;
		for ( int i = 0; i < parameters.size(); i++ ) {
			parameter = (SingleVariableDeclaration)parameters.get(i);
			parameterInfo = (ParameterInfo)lstParameters.get(i);
			if( !(parameter.getType().toString().equals(parameterInfo.getType()) && 
				parameter.getName().toString().equals(parameterInfo.getName())) )
				return false;
		}
		return true;
	}
	
	public String getSignature() {
		String sSignature = sModifiers + " " + sReturnType + " " + sName + "(";
		for(int i = 0; i < lstParameters.size(); i++ )
			sSignature += ((ParameterInfo)lstParameters.get(i)).getType() + ",";
		if( lstParameters.size() > 0 )
			sSignature.substring(0, sSignature.length() - 1);
		sSignature += ")";
		return sSignature;
	}

	public String getSignatureForPointcutExpression() {
		String sSignature = sModifiers + " " + sReturnType + " " + 
							typeParent.getName() + ".";
		if(bConstructor)
			sSignature += "new" + "(";
		else
			sSignature += sName + "(";

		for(int i = 0; i < lstParameters.size(); i++ )
			if ( i == 0 )
				sSignature += ((ParameterInfo)lstParameters.get(i)).getType();
			else
				sSignature += ", " + ((ParameterInfo)lstParameters.get(i)).getType();
		sSignature += ")";
		return sSignature;
	}
}
