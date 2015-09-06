package br.ufscar.dc.rejasp.model;

import java.util.ArrayList;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.VariableInfo;
import br.ufscar.dc.rejasp.util.SimpleRange;

public class Advice {
	public static final int AD_NONE = 0;
	public static final int AD_BEFORE = 1;
	public static final int AD_AFTER = 1 << 1;
	public static final int AD_AROUND = 1 << 2;
	public static final int AD_RETURN = 1 << 3;
	
	private int nType;
	private Pointcut pointcut;
	private String sBody;
	/**
	 * A list of elements (type: SimpleRange) that indicate the begin
	 * and length of statements of the body.
	 */
	private ArrayList lstStatementLocation;
	
	public Advice(int nType, Pointcut pointcut, String sBody) {
		this.nType = nType;
		this.pointcut = pointcut;
		this.sBody = sBody;
	}

	public void setStatementLocation(ArrayList lstStatementLocation) {
		this.lstStatementLocation = lstStatementLocation;
	}
	
	public String toString(){
		String sIdentation = pointcut.getIdentation();
		// Creating a list of formals
		ArrayList lstFormals = new ArrayList();
		if( (pointcut.getPrimitivePC() & Pointcut.PC_THIS) != 0 )
			lstFormals.add(new VariableInfo(pointcut.getMethodInfo().getType().getName(), pointcut.getTypeName()));
		if ((pointcut.getPrimitivePC() & Pointcut.PC_ARGS) != 0)
			if( pointcut.getCustomArguments() != null )
				lstFormals.addAll(pointcut.getCustomArguments());
			else
				lstFormals.addAll(pointcut.getMethodInfo().getParameters());

		String sCode = "\r\n" + sIdentation;
		if( (nType & AD_BEFORE) != 0 ) {
			sCode += "before ( ";
		}
		else if( (nType & AD_AFTER) != 0 ) {
			sCode += "after ( ";
		}

		VariableInfo variableInfo;
		for( int i = 0; i < lstFormals.size(); i++ ) {
			variableInfo = (VariableInfo)lstFormals.get(i);
			if(i == 0)
				sCode += variableInfo.toString();
			else
				sCode += ", " + variableInfo.toString();
		}
		sCode += "): " + pointcut.getName() + "(";
		
		for( int i = 0; i < lstFormals.size(); i++ ) {
			variableInfo = (VariableInfo)lstFormals.get(i);
			if(i == 0)
				sCode += variableInfo.getName();
			else
				sCode += ", " + variableInfo.getName();
		}
		
		sCode += ") {\r\n";
		sCode += getBodyWithIdentation();
		sCode += sIdentation + "}\r\n";
		return sCode;
	}
	
	private String getBodyWithIdentation() {
		String sIdentation = pointcut.getIdentation();
		String sNewBody = "";
		// For each statement
		SimpleRange simpleRange;
		for( int i = 0; i < lstStatementLocation.size(); i++) {
			simpleRange = (SimpleRange)lstStatementLocation.get(i);
			sNewBody += sIdentation + sIdentation + 
				sBody.substring(simpleRange.start, simpleRange.start + simpleRange.length) + "\r\n";
		}
		return sNewBody;
	}
}
