package br.ufscar.dc.rejasp.model.ASTNodeInfo;

public class VariableInfo {
	private String sName;
	private String sType;
	
	public VariableInfo(String type, String name) {
		sName = name;
		sType = type;
	}
	
	public String getName() {
		return sName;
	}
	
	public String getType() {
		return sType;
	}
	
	public String toString() {
		return sType + " " + sName; 
	}
}
