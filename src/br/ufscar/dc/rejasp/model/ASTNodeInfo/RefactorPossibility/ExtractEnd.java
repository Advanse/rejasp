package br.ufscar.dc.rejasp.model.ASTNodeInfo.RefactorPossibility;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;

public class ExtractEnd extends ExtractStatements {
	public ExtractEnd(MethodInfo parent, int statementCount) {
		super( parent, statementCount);
	}
}
