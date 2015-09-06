package br.ufscar.dc.rejasp.model.ASTNodeInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import br.ufscar.dc.rejasp.views.TreeObject;

/**
 * @author Daniel Kawakami
 * This class contain refactoring information about a method and each statement of first level of
 * this class.
 */
public class MethodRefactoringInfo {
	private class StatementInfo {
		private boolean bIndication;
		private ArrayList lstMethodInvocations;
		private Statement reference;
		private int refactoring;
		public StatementInfo(boolean indication, ArrayList lstMethodInvocations, Statement reference) {
			bIndication = indication;
			this.lstMethodInvocations = lstMethodInvocations;
			this.reference = reference;
			this.refactoring = 0;
		}
		public boolean isIndication() {
			return bIndication;
		}
		public ArrayList getMethodInvocations() {
			return lstMethodInvocations;
		}
		public Statement getReference() {
			return reference;
		}
		public int getRefactoring() {
			return refactoring; 
		}
		public void addRefactoring(int refactoring) {
			this.refactoring |= refactoring;
		}
	}
	private ArrayList lstStatements;
	/**
	 * Indicates all refactorings that could be done in the method.
	 */
	public MethodRefactoringInfo() {
		lstStatements = new ArrayList();
	}
	
	public void addStatement(boolean indication, Statement statement) {
		ArrayList lstMethodInvocations = getMethodInvocations(statement);
		StatementInfo stInfo = new StatementInfo(indication, lstMethodInvocations, statement);
		lstStatements.add(stInfo);
	}
	
	public void generateRefactoring() {
		if(lstStatements.size() <= 0)
			return;
		// Have Extract Beginnig refactoring found?
		int i = 0;
		StatementInfo stInfo;
		while(i < lstStatements.size() &&
			 (stInfo = (StatementInfo)lstStatements.get(i)).bIndication) {
			stInfo.addRefactoring(TreeObject.REFACTOR_EXTRACT_BEGINNIG);
			i++;
		}
		if( ((StatementInfo)lstStatements.get(lstStatements.size()-1)).getReference() instanceof ReturnStatement ) {
			// Have Before Return refactoring found?
			i = lstStatements.size() - 2;
			while( i >= 0 &&
				 (stInfo = (StatementInfo)lstStatements.get(i)).bIndication) {
				stInfo.addRefactoring(TreeObject.REFACTOR_PRE_RETURN);
				i--;
			}
		}
		else {
			// Have Extract End refactoring found?
			i = lstStatements.size() - 1;
			while( i >= 0 &&
				 (stInfo = (StatementInfo)lstStatements.get(i)).bIndication) {
				stInfo.addRefactoring(TreeObject.REFACTOR_EXTRACT_END);
				i--;
			}
		}
		/*
		// Before and After Call refactoring instructions:
		// Find all valid join points (a method only be called once in method body)
		ArrayList lstAllInvocations = new ArrayList();
		for(i = 0; i < lstStatements.size(); i++) {
			stInfo = (StatementInfo)lstStatements.get(i);
			lstAllInvocations.addAll(stInfo.getMethodInvocations());
		}
		i = 0;
		ArrayList lstDuplicatedInvocations = new ArrayList();
		while(i < lstAllInvocations.size()) {
			int j = i + 1;
			while(j < lstAllInvocations.size()) {
				if(isInvocationSame(((MethodInvocation)lstAllInvocations.get(i)),
									((MethodInvocation)lstAllInvocations.get(j)))) {
					lstDuplicatedInvocations
			}
		}
		*/
	}

	/**
	 * Returns an integer which bits indicate the types of refactoring applied to a statement
	 * @param statement
	 * @return
	 */
	public int getRefactoring(Statement statement) {
		StatementInfo stInfo;
		for(int i = 0; i < lstStatements.size(); i++) {
			stInfo = (StatementInfo)lstStatements.get(i);
			if(stInfo.getReference().equals(statement))
				return stInfo.getRefactoring();
		}
		return 0;
	}
	
	public int getAllMethodRefactoring() {
		int refactoring = 0;
		for(int i = 0; i < lstStatements.size(); i++)
			refactoring |= ((StatementInfo)lstStatements.get(i)).getRefactoring();
		return refactoring;
	}
	
	public int ExtractBeginningSize() {
		int size = 0;
		int i = 0;
		while( i < lstStatements.size() && 
				((((StatementInfo)lstStatements.get(i)).getRefactoring()& 
				TreeObject.REFACTOR_EXTRACT_BEGINNIG)  != 0)) {
			i++;
			size++;
		}
		return size;
	}
	
	public int ExtractEndSize() {
		int size = 0;
		int i = lstStatements.size() - 1;
		while( i >= 0 && 
				((((StatementInfo)lstStatements.get(i)).getRefactoring()& 
				TreeObject.REFACTOR_EXTRACT_END)  != 0)) {
			i--;
			size++;
		}
		return size;
	}

	private ArrayList getMethodInvocations(Statement statement) {
		ArrayList lstExpressions = new ArrayList();
		/*
		 * AssertStatement:
		 *     assert Expression [ : Expression ] ;
		 */
		if ( statement instanceof AssertStatement ) {
			AssertStatement assertStatement = (AssertStatement)statement;
			lstExpressions.add(assertStatement.getExpression());
			lstExpressions.add(assertStatement.getMessage());
		}
		/*
		 * ConstructorInvocation:
		 *       [ < Type { , Type } > ]
		 *       		      this ( [ Expression { , Expression } ] ) ;
		 */
		else if ( statement instanceof ConstructorInvocation ) {
			ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
			List arguments = constructorInvocation.arguments();
			for ( int i = 0; i < arguments.size(); i++ )
				lstExpressions.add(arguments.get(i));
		}
		/*
		 *  ExpressionStatement:
		 *      StatementExpression ;
		 */
		else if ( statement instanceof ExpressionStatement ) {
			ExpressionStatement expressionStatement = (ExpressionStatement)statement;
			lstExpressions.add(expressionStatement.getExpression());
		}
		/*
		 * IfStatement:
		 *     if ( Expression ) Statement [ else Statement]
		 */
		else if ( statement instanceof IfStatement ) {
			IfStatement ifStatement = (IfStatement)statement;
			lstExpressions.add(ifStatement.getExpression());
		}

		/*
		 *  SuperConstructorInvocation:
		 *       [ Expression . ]
		 *                [ < Type { , Type } > ]
		 *                super ( [ Expression { , Expression } ] ) ;
		 */
		else if ( statement instanceof SuperConstructorInvocation ) {
			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;
			lstExpressions.add(superConstructorInvocation.getExpression());
			List arguments = superConstructorInvocation.arguments();
			for ( int i = 0; i < arguments.size(); i++ )
				lstExpressions.add(arguments.get(i));
		}
		/*
		 * SwitchStatement:
		 * 		switch ( Expression )
		 *  	{ { SwitchCase | Statement } } }
		 */
		else if ( statement instanceof SwitchStatement ) {
			SwitchStatement switchStatement = (SwitchStatement)statement;
			lstExpressions.add(switchStatement.getExpression());
		}
		/*
		 * SynchronizedStatement:
		 *     synchronized ( Expression ) Block
		 */
		else if ( statement instanceof SynchronizedStatement ) {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
			lstExpressions.add(synchronizedStatement.getExpression());
		}
		/*
		 * ThrowStatement:
		 *     throw Expression ;
		 */
		else if ( statement instanceof ThrowStatement ) {
			ThrowStatement throwStatement = (ThrowStatement)statement;
			lstExpressions.add(throwStatement.getExpression());
		}
		/*
		 * VariableDeclarationStatement:
		 *    { ExtendedModifier } Type VariableDeclarationFragment
		 *            { , VariableDeclarationFragment } ;
		 *            
		 *  VariableDeclarationFragment:
		 *      Identifier { [] } [ = Expression ]
		 */
		else if ( statement instanceof VariableDeclarationStatement ) {
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
			List fragments = variableDeclarationStatement.fragments();
				for (int i = 0; i < fragments.size(); i++) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment)fragments.get(i);
					if(fragment.getInitializer() != null)
						lstExpressions.add(fragment.getInitializer());
				}
		}
		Iterator it = lstExpressions.iterator();
		Expression expression;
		while(it.hasNext()) {
			expression = (Expression)it.next();
			if(! (expression instanceof MethodInvocation) )
				it.remove();
		}
		return lstExpressions;
	}
}
