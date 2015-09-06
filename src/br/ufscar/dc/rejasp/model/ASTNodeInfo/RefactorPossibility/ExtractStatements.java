package br.ufscar.dc.rejasp.model.ASTNodeInfo.RefactorPossibility;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.FileInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;

public class ExtractStatements {
	private MethodInfo parent;
	private int statementCount;
	
	public ExtractStatements(MethodInfo parent, int statementCount) {
		this.parent = parent;
		this.statementCount = statementCount;
	}
	
	public int getCount() {
		return statementCount;
	}
	
	public MethodInfo getMethod() {
		return parent;
	}
	
	public FileInfo getFile() {
		return parent.getFile();
	}
	
	public TypeInfo getType() {
		return parent.getType();
	}
}
