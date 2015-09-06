package br.ufscar.dc.rejasp.model.ASTNodeInfo;

public class ParameterInfo extends VariableInfo {
	private MethodInfo methodParent;
	
	public ParameterInfo (String type, String name, MethodInfo methodInfo) {
		super(type, name);
		methodParent = methodInfo;
	}
	
	public MethodInfo getMethodInfo() {
		return methodParent;
	}
}
