package br.ufscar.dc.rejasp.model.ASTNodeInfo.RefactorPossibility;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;

public class ExtractBeginning extends ExtractStatements {
	public ExtractBeginning(MethodInfo parent, int statementCount) {
		super(parent, statementCount);
	}
}
