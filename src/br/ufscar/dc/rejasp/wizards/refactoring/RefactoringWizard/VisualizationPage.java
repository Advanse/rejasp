package br.ufscar.dc.rejasp.wizards.refactoring.RefactoringWizard;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IViewPart;

import br.ufscar.dc.rejasp.indication.detection.VariableTable;
import br.ufscar.dc.rejasp.model.Advice;
import br.ufscar.dc.rejasp.model.Aspect;
import br.ufscar.dc.rejasp.model.CommentInterval;
import br.ufscar.dc.rejasp.model.Pointcut;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.FieldInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.FileInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.ParameterInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.VariableInfo;
import br.ufscar.dc.rejasp.util.SimpleRange;
import br.ufscar.dc.rejasp.views.IndicationTree;
import br.ufscar.dc.rejasp.views.TreeObject;

public class VisualizationPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	protected RefactoringWizard wizard;

	/**
	 * Used for generating recommendations 
	 */
	private ArrayList lstExternalFields;
	private ArrayList lstPrivateFields;
	/**
	 * List of SimpleRange. It is used to know where type object will be inserted
	 * in the extracted piece of code. 
	 */
	private ArrayList lstTypeSimpleRange;
	private ArrayList lstArgumentSimpleRange;
	/**
	 * It stores the argument name in method and relates it with
	 * the new name for argument in aspect.
	 */
	private HashMap argumentMap;
	private VariableTable variables;
	private Pointcut pointcut;
	private Advice advice;
	private Aspect aspectParse;
	/**
	 * JDT Types 
	 */
	protected MethodDeclaration methodDeclaration;
	/**
	 * Fixed ranges
	 */
	private ArrayList lstStyleRange;

	/*
	 * Storage java code as a string 
	 */
	private String sJavaCode;
	
	protected Spinner spnStatements;
	protected StyledText javaText;
	protected StyledText aspectText;
	private StyledText recomendationText;
	private Label lbAspect;
	protected Display display;
	
	public VisualizationPage() {
		super("Aspect Visualisation");
		setTitle("Preview of Aspect Reorganization");
		setDescription("If the preview of aspect is correct, confirm changes clicking Finish");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (RefactoringWizard)getWizard();
		variables = new VariableTable();
		lstExternalFields = new ArrayList();
		lstPrivateFields = new ArrayList();
		lstTypeSimpleRange = new ArrayList();
		lstArgumentSimpleRange = new ArrayList();
		argumentMap = new HashMap(); 

		// create the composite to hold the widgets
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);
		display = composite.getDisplay();

		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 4;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);

		Label numberSt = new Label(composite, SWT.NONE);
		numberSt.setText("Statements to be extracted");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 1;
		//gd.horizontalIndent = 10;
		numberSt.setLayoutData(gd);
		
		spnStatements = new Spinner(composite, SWT.NONE);
		spnStatements.setMinimum(0);
		spnStatements.setIncrement(1);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 1;
		spnStatements.setLayoutData(gd);

		Label lbRecomendation = new Label(composite, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		lbRecomendation.setText("Reorganization recomendation");
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER);
		gd.horizontalSpan = 2;
		lbRecomendation.setLayoutData(gd);
		
		javaText = new StyledText(composite, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 8;
		javaText.setLayoutData(gd);
		
		recomendationText = new StyledText(composite, SWT.READ_ONLY | SWT.H_SCROLL | SWT.WRAP | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 5;
		recomendationText.setLayoutData(gd);
		recomendationText.setText("\r\n\r\n\r\n");

		lbAspect = new Label(composite, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		lbAspect.setText("Aspect Selected in previous screen");
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER);
		gd.horizontalSpan = 2;
		lbAspect.setLayoutData(gd);

		aspectText = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		aspectText.setLayoutData(gd);

		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		spnStatements.addListener(SWT.Selection, this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    if(event.widget == spnStatements) {
			// Remove all ranges
			javaText.setStyleRanges(0, javaText.getText().length(), new int[]{}, new StyleRange[]{});
			for(int i = 0; i < lstStyleRange.size(); i++)
				javaText.setStyleRange((StyleRange)lstStyleRange.get(i));
		}
	}

	/**
	 * Applies the status to the status line of a dialog page.
	 */
	protected void applyToStatusLine(IStatus status) {
		String message= status.getMessage();
		if (message.length() == 0) message= null;
		switch (status.getSeverity()) {
		case IStatus.OK:
			setErrorMessage(null);
			setMessage(message);
			break;
		case IStatus.WARNING:
			setErrorMessage(null);
			setMessage(message, WizardPage.WARNING);
			break;				
		case IStatus.INFO:
			setErrorMessage(null);
			setMessage(message, WizardPage.INFORMATION);
			break;			
		default:
			setErrorMessage(message);
			setMessage(message, WizardPage.ERROR);
		break;		
		}
	}

	/**
	 * @see IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getPreviousPage()
	 */
	public IWizardPage getPreviousPage() {
		wizard.pointcutPage.onEnterPage();
		return wizard.pointcutPage;
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		pointcut = wizard.getPointcut();
		recomendationText.setText("");
		generateJavaStyledText();
		generateAspectStyledText();
		populateVariableTable();

	    spnStatements.setMaximum(methodDeclaration.getBody().statements().size());
	    spnStatements.setSelection(0);
	}
	
	/**
	 * It shows java code with indication marks.
	 */
	private void generateJavaStyledText() {
		// Insert original method
		MethodInfo methodInfo = wizard.getSelectedMethod();
		FileInfo fileInfo = methodInfo.getFile();
		// Getting file name
		String sPath = fileInfo.getPath();
		int nIndex = sPath.lastIndexOf('\\');
		sPath = sPath.substring(nIndex + 1, sPath.length());
		sPath = '/' + fileInfo.getPackage().replace('.', '/') + '/' + sPath;
		IFile file = (IFile)wizard.getProject().findMember(sPath);
		
		InputStream input = null;
		try {
			input = file.getContents();
		} catch ( CoreException e ) {
			System.err.println( e.getMessage() );
			return;
		}
		
		sJavaCode = new String();
		try	{
			for(int j = input.read(); j != -1; j = input.read()) {
				sJavaCode += (char) j;
	        }
			input.close();
	    } catch(IOException e) {
	        System.err.println("Could not read from file");
	        return;
	    }
	    
	    lstStyleRange = new ArrayList();
	    // Build the AST structure
	    ASTParser parser = ASTParser.newParser(AST.JLS3);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5
	    parser.setSource(sJavaCode.toCharArray());
	    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		
	    // Getting all TypeDeclaration of selected method
	    TypeInfo typeInfo = methodInfo.getType();
	    ArrayList lstTypes = new ArrayList();
	    lstTypes.add(typeInfo.getName());
		while(typeInfo != null) {
			lstTypes.add(typeInfo.getName());
			typeInfo = typeInfo.getTypeParent();
		}
	    
	    // Getting all enclosured typeDeclaration of compilation unit
		List lstUnitTypes = unit.types();
		Object[] types = lstUnitTypes.toArray();
		TypeDeclaration typeDeclaration = null;
		
		// Find the type of selected method
		for( int i = lstTypes.size() - 1; i >= 0; i--  ) {
			for ( int j = 0; j < types.length; j++ )
				if( lstTypes.get(i).equals(((TypeDeclaration)types[j]).getName().getIdentifier()) ) {
					typeDeclaration = (TypeDeclaration)types[j];
					types = ((TypeDeclaration)types[j]).getTypes();
					break;
				}
		}
		// Find selected method
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		for(int i = 0; i < methods.length; i++) 
			if ( methods[i].getName().getIdentifier().equals(methodInfo.getName()) && 
					methodInfo.equalsParameterList(methods[i].parameters())) {
				methodDeclaration = methods[i];
				break;
			}
		
	    // Getting ranges for comments in original tree
	    List lstComments = unit.getCommentList();
	    Comment comment;
	    for( int j = 0 ; j < lstComments.size(); j++ ) {
    		comment = (Comment)lstComments.get(j);
    		if( comment.getStartPosition() > methodDeclaration.getStartPosition() &&
    			comment.getStartPosition() < methodDeclaration.getStartPosition() + methodDeclaration.getLength()) {
		    	if( comment instanceof LineComment )
		    		lstStyleRange.add(new StyleRange(comment.getStartPosition() - methodDeclaration.getStartPosition(),
		    				comment.getLength(), display.getSystemColor(SWT.COLOR_DARK_GREEN), 
		    				display.getSystemColor(SWT.COLOR_WHITE)));
		    	else if( comment instanceof BlockComment )
		    		lstStyleRange.add(new StyleRange(comment.getStartPosition() - methodDeclaration.getStartPosition(),
		    				comment.getLength(), display.getSystemColor(SWT.COLOR_BLUE), 
		    				display.getSystemColor(SWT.COLOR_WHITE)));
    		}
	    }
	    // Marking indications in method
		try {
			IMarker[] markers = null;
			markers = file.findMarkers(IMarker.TASK, true, IResource.DEPTH_INFINITE);
			for( int i = 0; i < markers.length; i++ )
				if(markers[i].getAttribute(IMarker.CHAR_START, -1) != -1 &&
					markers[i].getAttribute(IMarker.CHAR_START, -1) > methodDeclaration.getStartPosition() &&
					markers[i].getAttribute(IMarker.CHAR_START, -1) < methodDeclaration.getStartPosition() + methodDeclaration.getLength()	) {
					lstStyleRange.add(new StyleRange(
								markers[i].getAttribute(IMarker.CHAR_START, -1) - methodDeclaration.getStartPosition(),
								markers[i].getAttribute(IMarker.CHAR_END, -1) - markers[i].getAttribute(IMarker.CHAR_START, -1), 
								display.getSystemColor(SWT.COLOR_BLACK), display.getSystemColor(SWT.COLOR_YELLOW), SWT.BOLD));
				}
		} catch (CoreException e) {
			System.err.println( e.getMessage() );
		}
	    javaText.setText(sJavaCode.substring(methodDeclaration.getStartPosition(), 
				methodDeclaration.getStartPosition() + methodDeclaration.getLength()));
	    for(int i = 0; i < lstStyleRange.size(); i++)
	    	javaText.setStyleRange((StyleRange)lstStyleRange.get(i));
	}
	
	/**
	 * It shows aspect code with comment marks.
	 */
	private void generateAspectStyledText() {
		IFile file;
		String sSourceCode;
		file = wizard.getAspectFile();
		// Getting aspect source code (in string)
		try {
			AJCompilationUnit unit =
				AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
			unit.requestOriginalContentMode();
			sSourceCode = unit.getBuffer().getContents();
		} catch (JavaModelException e) {
			System.err.println(e.getLocalizedMessage());
			return;
		}
		
		// Find comments in aspect
		aspectParse = new Aspect(sSourceCode);
		if( !aspectParse.parse() )
			System.err.println("Something wrong happened");

		// updating source code string
		aspectText.setText(sSourceCode);
		// Marking all commented element
		ArrayList lstComments = aspectParse.getComments();
		CommentInterval interval;
		for( int i = 0; i < lstComments.size(); i++ ) {
			interval = (CommentInterval)lstComments.get(i);
			if(interval.isLineComment())
				aspectText.setStyleRange(new StyleRange(interval.getStart(),interval.getLength(),
						display.getSystemColor(SWT.COLOR_DARK_GREEN), 
						display.getSystemColor(SWT.COLOR_WHITE)));
			else
				aspectText.setStyleRange(new StyleRange(interval.getStart(),interval.getLength(),
						display.getSystemColor(SWT.COLOR_BLUE), 
						display.getSystemColor(SWT.COLOR_WHITE)));
		}
	}
	
	/**
	 * It inserts data in variable table (object variable) for
	 * fields, parameters and external fields.
	 */
	private void populateVariableTable() {
		variables.removeAll();
		MethodInfo methodInfo = wizard.getSelectedMethod();
		// Method parameters level
		variables.addLevel();
		ArrayList list = methodInfo.getParameters();
		for(int i = 0; i < list.size(); i++)
			variables.addElement((ParameterInfo)list.get(i));
		// Atributes of current type
		variables.addLevel();
		TypeInfo typeInfo = methodInfo.getType();
		list = typeInfo.getFields();
		for(int i = 0; i < list.size(); i++)
			variables.addElement((FieldInfo)list.get(i));
		// Atributes of external type
		while((typeInfo = typeInfo.getTypeParent()) != null ) {
			variables.addLevel();
			list = typeInfo.getFields();
			for(int i = 0; i < list.size(); i++)
				variables.addElement((FieldInfo)list.get(i));
		}
		variables.invertLevels();
	}
	
	/**
	 * It shows added markers in java code that indicate piece of code that would be 
	 * extracted by using extract beginning refatoring.
	 * @return
	 */
	protected boolean previewJavaCode( int nBegin, int nEnd ) {
		Statement statement;
		StyleRange styleRange, newRange;
		int nMethodStart;
		// For each statement
		for(int i = nBegin; i <= nEnd; i++) {
			statement = (Statement)methodDeclaration.getBody().statements().get(i);
			nMethodStart = statement.getStartPosition() - methodDeclaration.getStartPosition();
			// For each character in the statement, mark with or without indication
			for(int j = nMethodStart; j < nMethodStart + statement.getLength(); j++) {
				styleRange = javaText.getStyleRangeAtOffset(j);
				if ( styleRange != null && styleRange.background == display.getSystemColor(SWT.COLOR_YELLOW ))
					newRange = new StyleRange(j, 1, display.getSystemColor(SWT.COLOR_RED), 
							display.getSystemColor(SWT.COLOR_YELLOW), SWT.BOLD);
				else
					newRange = new StyleRange(j, 1, display.getSystemColor(SWT.COLOR_RED), 
							display.getSystemColor(SWT.COLOR_WHITE), SWT.BOLD);
				newRange.strikeout = true;
				javaText.setStyleRange(newRange);
			}
		}
		// Positing styled text selection on the first statement to be extracted
		statement = (Statement)methodDeclaration.getBody().statements().get(nBegin);
		javaText.setSelection(statement.getStartPosition() - methodDeclaration.getStartPosition());
		javaText.showSelection();

		return true;
	}

	/**
	 * Create a recomendation when a set of statements are selected.
	 * @param nBegin the first statement index (based zero index)
	 * @param nEnd the last statement index (based zero index)
	 */
	protected void createRecomendation( int nBegin, int nEnd ) {
		// Reseting lists that will be used to generate recomendations
		lstExternalFields.clear();
		lstPrivateFields.clear();
		lstTypeSimpleRange.clear();
		lstArgumentSimpleRange.clear();
		// Creating a temporary level for local variable storage
		variables.addLevel();
		for( int i = nBegin; i <= nEnd; i++ ) {
			analyseStatement((Statement)methodDeclaration.getBody().statements().get(i));
		}
		// Find local variables that is not recommeded to be extracted
		ArrayList lstNotRecommedLV = null;
		ArrayList lstExtractedLocalVariables = null;
		if(/*spnStatements.getSelection() + 1 < */methodDeclaration.getBody().statements().size() > nEnd) {
			lstExtractedLocalVariables = findIndentifier(variables.getElementsAtLevel(variables.getLevelCount()), 
					nBegin, nEnd);
			if(lstExtractedLocalVariables.size() != 0) {
				lstNotRecommedLV = new ArrayList();
				if( nBegin > 0 )
					lstNotRecommedLV.addAll(findIndentifier(variables.getElementsAtLevel(variables.getLevelCount()), 
							0, nBegin - 1));
				if(nEnd < methodDeclaration.getBody().statements().size() - 1)
					lstNotRecommedLV.addAll(findIndentifier(variables.getElementsAtLevel(variables.getLevelCount()), 
							nEnd + 1, methodDeclaration.getBody().statements().size() - 1));
				extractDuplicatedElements(lstNotRecommedLV);
			}
			// A local variable that exists in code to be extracted and remaining code
			// it's not a recommended local variable to extract
			if ( lstExtractedLocalVariables.size() == 0 || lstNotRecommedLV.size() == 0 ) {
				// Al local variable are in the part to extracted or in the remaining part. So no problem detected
				if(lstNotRecommedLV != null)
					lstNotRecommedLV.clear();
			}
			else {
				int i = 0;
				VariableInfo variableInfo1 = null, variableInfo2 = null;
				// For each local variable out of statement to be extracted
				while ( i < lstNotRecommedLV.size() - 1 ) {
					variableInfo1 = (VariableInfo)lstNotRecommedLV.get(i);
					// For each local variable contained in a statement to be extracted
					for( int j = 0; j < lstExtractedLocalVariables.size(); j++ ) {
						variableInfo2 = (VariableInfo)lstExtractedLocalVariables.get(j);
						// If the variable exists at both lists, it's a not recommeded variable
						if( variableInfo1.getName().equals(variableInfo2.getName()) &&
							variableInfo1.getType().equals(variableInfo2.getType()) ) {
							// Not recommeded variable is taken out of this list in order to
							// not being compared again
							lstExtractedLocalVariables.remove(j);
							variableInfo2 = null;
							break;
						}
					}
					// Element found in both list, so it's not recommended to extract
					if( variableInfo2 == null )
						i++;
					else
						// The current element in this list could be extracted
						lstNotRecommedLV.remove(i);
				}
			}
		}
		
		variables.removeLevel();
		// Extract duplicated VariableInfo
		extractDuplicatedElements(lstExternalFields);
		
		// Extract duplicated FieldInfo
		extractDuplicatedElements(lstPrivateFields);

		String sRecomendation = "";
		// Recommendation for fields belongs to external classes / interfaces 
		if( lstExternalFields.size() == 1 )
			sRecomendation += "The field " + ((FieldInfo)lstExternalFields.get(0)).getName() + " is external and not visible in aspects. Eliminate dependencies from the enclosing class and turn the inner class into a standalone class.\r\n\r\n";
		else if(lstExternalFields.size() > 1) {
			sRecomendation += "External Fields not visible in aspects: " + 
							((FieldInfo)lstExternalFields.get(0)).getName();
			int i = 1;
			while ( i < lstExternalFields.size() - 1 ) {
				sRecomendation += ", " + ((FieldInfo)lstExternalFields.get(0)).getName();
				i++;
			}
			sRecomendation += " and " + ((FieldInfo)lstExternalFields.get(i)).getName();
			sRecomendation += "Eliminate dependencies from the enclosing class and turn the inner class into a standalone class.\r\n\r\n";
		}
		// Recomendation for not visible field
		if( lstPrivateFields.size() == 1 )
			sRecomendation += "The field " + ((FieldInfo)lstPrivateFields.get(0)).getName() + " is not visible in aspects. You can change visibility to public, use get method for this field (if available), or introduce field to aspect.\r\n\r\n";
		else if(lstPrivateFields.size() > 1) {
			sRecomendation += "Fields not visible in aspects: " + 
							((FieldInfo)lstPrivateFields.get(0)).getName();
			int i = 1;
			while ( i < lstPrivateFields.size() - 1 ) {
				sRecomendation += ", " + ((FieldInfo)lstPrivateFields.get(i)).getName();
				i++;
			}
			sRecomendation += " and " + ((FieldInfo)lstPrivateFields.get(i)).getName();
			sRecomendation += ". You can change visibility to public, use get method for these fields (if available), or introduce these fields to aspect.\r\n\r\n";
		}
		// Recomendation for local variable
		if( lstNotRecommedLV != null )
			if( lstNotRecommedLV.size() == 1)
				sRecomendation += "The local variable " + ((VariableInfo)lstNotRecommedLV.get(0)).getName() + 
					" is used in the piece of code not extracted.\r\n\r\n";
			else if ( lstNotRecommedLV.size() > 1 ) {
				VariableInfo localVariable = (VariableInfo)lstNotRecommedLV.get(0);
				sRecomendation += "The local variables: " + localVariable.getName(); 
				for( int i = 1; i < lstNotRecommedLV.size(); i++ ) {
					localVariable = (VariableInfo)lstNotRecommedLV.get(i);
					sRecomendation += ", " + localVariable.getName();
				}
				sRecomendation += " are used in the piece of code not extracted.\r\n\r\n";
			}
		recomendationText.setText(sRecomendation);
	}
	
	/**
	 * It removes duplicated elements in a list.
	 * @param list
	 */
	private void extractDuplicatedElements(ArrayList list) {
		// Extract duplicated VariableInfo
		int i = 0, j;
		while ( i < list.size() - 1 ) {
			j = i + 1;
			while ( j < list.size() ) {
				if( list.get(i).equals(list.get(j)) )
					list.remove(j);
				else
					j++;
			}
			i++;
		}
	}
	
	/**
	 * Find Identifiers that match with an element in lstVariables in a range of statements 
	 * @param lstVariable list of variables that be used to match identifier
	 * @param nBeginStatement an index number of the first statement 
	 * @param nEndStatement an index number of the last statement
	 * @return a list of elements matched
	 */
	private ArrayList findIndentifier(ArrayList lstVariable, int nBeginStatement, int nEndStatement) {
		ArrayList lstIdentifiersFound = new ArrayList();
		for( int i = nBeginStatement; i <= nEndStatement; i++ )
			findIdentifiers((Statement)methodDeclaration.getBody().statements().get(i), lstVariable, lstIdentifiersFound);
		// Removing duplicate elements
		extractDuplicatedElements(lstIdentifiersFound);
		return lstIdentifiersFound;
	}
	
	/**
	 * Find identifiers in a statement
	 * @param statement Statement that could contain identifiers 
	 * @param lstVariables List of Variables that we desire to look for in the statement
	 * @param lstIdentifiers List the identifiers related to Variables of lstVariables in the statement
	 */
	private void findIdentifiers(Statement statement, ArrayList lstVariables, ArrayList lstIdentifiers) {
		/*
		 * AssertStatement:
		 *     assert Expression [ : Expression ] ;
		 */
		if ( statement instanceof AssertStatement ) {
			AssertStatement assertStatement = (AssertStatement)statement;
			findIdentifiersInExpression(assertStatement.getExpression(), lstVariables, lstIdentifiers);
			findIdentifiersInExpression(assertStatement.getMessage(), lstVariables, lstIdentifiers);
		}
		/*
		 * BreakStatement:
		 *     break [ Identifier ] ;
		 */
		else if ( statement instanceof BreakStatement ) {
			BreakStatement breakStatement = (BreakStatement)statement;
			findIdentifiersInExpression(breakStatement.getLabel(), lstVariables, lstIdentifiers);
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
				findIdentifiersInExpression((Expression)arguments.get(i), lstVariables, lstIdentifiers);
		}
		/*
		 * ContinueStatement:
		 *     continue [ Identifier ] ;
		 */
		else if ( statement instanceof ContinueStatement ) {
			ContinueStatement continueStatement = (ContinueStatement)statement;
			findIdentifiersInExpression(continueStatement.getLabel(), lstVariables, lstIdentifiers);
		}
		/*
		 * DoStatement:
		 *     do Statement while ( Expression ) ;
		 */
		else if ( statement instanceof DoStatement ) {
			DoStatement doStatement = (DoStatement)statement;
			findIdentifiersInExpression(doStatement.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * EnhancedForStatement:
		 *     for ( FormalParameter : Expression )
		 *      			Statement
		 *      
		 * The FormalParameter is represented by a SingleVariableDeclaration  (without an initializer).
		 * 
		 * SingleVariableDeclaration:
		 *     { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
		 */
		else if ( statement instanceof EnhancedForStatement ) {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
			SingleVariableDeclaration formalParameter = enhancedForStatement.getParameter();
			// Is the formal parameter a indication?
			findIdentifiersInExpression(formalParameter.getName(), lstVariables, lstIdentifiers);
			// Is the expression of EnhancedForStatement a indication?
			findIdentifiersInExpression(enhancedForStatement.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 *  ExpressionStatement:
		 *      StatementExpression ;
		 */
		else if ( statement instanceof ExpressionStatement ) {
			ExpressionStatement expressionStatement = (ExpressionStatement)statement;
			findIdentifiersInExpression(expressionStatement.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * ForStatement:
		 *     for (
		 *      			[ ForInit ];
		 *       			[ Expression ] ;
		 *       			[ ForUpdate ] )
		 *       			Statement
		 * ForInit:
		 *  		Expression { , Expression }
		 * ForUpdate:
		 *  		Expression { , Expression }
		 */
		else if ( statement instanceof ForStatement ) {
			ForStatement forStatement = (ForStatement)statement;
			// For each initializer
			List expressions = forStatement.initializers();
			for ( int i = 0; i < expressions.size(); i++ )
				findIdentifiersInExpression((Expression)expressions.get(i), lstVariables, lstIdentifiers);
			findIdentifiersInExpression(forStatement.getExpression(), lstVariables, lstIdentifiers);
			// For each updater
			expressions = forStatement.updaters();
			for ( int i = 0; i < expressions.size(); i++ )
				findIdentifiersInExpression((Expression)expressions.get(i), lstVariables, lstIdentifiers);
		}
		/*
		 * IfStatement:
		 *     if ( Expression ) Statement [ else Statement]
		 */
		else if ( statement instanceof IfStatement ) {
			IfStatement ifStatement = (IfStatement)statement;
			findIdentifiersInExpression(ifStatement.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * LabeledStatement:
		 *     Identifier : Statement
		 */
		else if ( statement instanceof LabeledStatement ) {
			LabeledStatement labeledStatement = (LabeledStatement)statement;
			findIdentifiersInExpression(labeledStatement.getLabel(), lstVariables, lstIdentifiers);
		}
		/*
		 * ReturnStatement:
		 *     return [ Expression ] ;
		 */
		else if ( statement instanceof ReturnStatement ) {
			ReturnStatement returnStatement = (ReturnStatement)statement;
			findIdentifiersInExpression(returnStatement.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 *  SuperConstructorInvocation:
		 *       [ Expression . ]
		 *                [ < Type { , Type } > ]
		 *                super ( [ Expression { , Expression } ] ) ;
		 */
		else if ( statement instanceof SuperConstructorInvocation ) {
			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;
			findIdentifiersInExpression(superConstructorInvocation.getExpression(), lstVariables, lstIdentifiers);
			List arguments = superConstructorInvocation.arguments();
			for ( int i = 0; i < arguments.size(); i++ ) 
				findIdentifiersInExpression((Expression)arguments.get(i), lstVariables, lstIdentifiers);
		}
		/*
		 * SwitchCase:
		 * 		case Expression  :
		 * 		default :
		 */
		else if ( statement instanceof SwitchCase ) {
			SwitchCase switchCase = (SwitchCase)statement;
			findIdentifiersInExpression(switchCase.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * SwitchStatement:
		 * 		switch ( Expression )
		 *  	{ { SwitchCase | Statement } } }
		 */
		else if ( statement instanceof SwitchStatement ) {
			SwitchStatement switchStatement = (SwitchStatement)statement;
			findIdentifiersInExpression(switchStatement.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * SynchronizedStatement:
		 *     synchronized ( Expression ) Block
		 */
		else if ( statement instanceof SynchronizedStatement ) {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
			findIdentifiersInExpression(synchronizedStatement.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * ThrowStatement:
		 *     throw Expression ;
		 */
		else if ( statement instanceof ThrowStatement ) {
			ThrowStatement throwStatement = (ThrowStatement)statement;
			findIdentifiersInExpression(throwStatement.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * TryStatement:
		 *      try Block 
		 *               { CatchClause }
		 *               [ finally Block ]
		 */
		else if ( statement instanceof TryStatement ) {
			TryStatement tryStatement = (TryStatement)statement;
			List catchs = tryStatement.catchClauses();
			for ( int i = 0; i < catchs.size(); i++ ) {
				findIdentifiersInExpression((((CatchClause)catchs.get(i)).getException().getName()), lstVariables, lstIdentifiers);
				findIdentifiersInExpression((((CatchClause)catchs.get(i)).getException().getInitializer()), lstVariables, lstIdentifiers);
			}
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
			VariableDeclarationFragment fragment;
			for (int i = 0; i < fragments.size(); i++) {
				fragment = (VariableDeclarationFragment)fragments.get(i);
				findIdentifiersInExpression(fragment.getInitializer(), lstVariables, lstIdentifiers);
			}
		}
		/*
		 * WhileStatement:
		 *     while ( Expression ) Statement
		 */
		else if ( statement instanceof WhileStatement ) {
			WhileStatement whileStatement = (WhileStatement)statement;
			findIdentifiersInExpression(whileStatement.getExpression(), lstVariables, lstIdentifiers);
		}

		// Does statement have other statements? 
		List statements = getStatements( statement );
		if ( statements != null ) {
			// Look at each statement
			for ( int i = 0; i < statements.size(); i++ )
				findIdentifiers((Statement)statements.get(i), lstVariables, lstIdentifiers);
		}
	}
	
	/**
	 * It finds for indetifiers in an expression.
	 * @param expression Expression where identifiers must be searched.
	 * @param lstVariables List of variables that we use to match identifiers
	 * @param lstIdentifiers  List the identifiers related to Variables of lstVariables in the statement
	 */
	private void findIdentifiersInExpression(Expression expression, ArrayList lstVariables, ArrayList lstIdentifiers) {
		/*
		 * ArrayAccess:
		 *     Expression [ Expression ]
		 */
		if  ( expression instanceof ArrayAccess ) {
			ArrayAccess arrayAccess = (ArrayAccess)expression; 
			findIdentifiersInExpression (arrayAccess.getArray(), lstVariables, lstIdentifiers);
			findIdentifiersInExpression(arrayAccess.getIndex(), lstVariables, lstIdentifiers);
		}
		/*
		 * ArrayCreation:
		 *     new PrimitiveType [ Expression ] { [ Expression ] } { [ ] }
		 *     new TypeName [ < Type { , Type } > ]
		 *         [ Expression ] { [ Expression ] } { [ ] }
		 *     new PrimitiveType [ ] { [ ] } ArrayInitializer
		 *     new TypeName [ < Type { , Type } > ]
		 *         [ ] { [ ] } ArrayInitializer
		 */
		else if  ( expression instanceof ArrayCreation ) {
			ArrayCreation arrayCreation = (ArrayCreation)expression;
			for ( int i = 0; i < arrayCreation.dimensions().size(); i++ )
				findIdentifiersInExpression( (Expression)arrayCreation.dimensions().get(i), lstVariables, lstIdentifiers); 
			findIdentifiersInExpression( arrayCreation.getInitializer(), lstVariables, lstIdentifiers); 
		}
		/*
		 *  ArrayInitializer:
		 *   		{ [ Expression { , Expression} [ , ]] }
		 */
		else if  ( expression instanceof ArrayInitializer ) {
			ArrayInitializer arrayInitializer = (ArrayInitializer)expression; 
			for ( int i = 0; i < arrayInitializer.expressions().size(); i++ )
				findIdentifiersInExpression( (Expression) arrayInitializer.expressions().get(i), lstVariables, lstIdentifiers);
		}
		/*
		 * Assignment:
		 *     Expression AssignmentOperator Expression
		 */
		else if  ( expression instanceof Assignment ) {
			Assignment assignment = (Assignment)expression; 
			findIdentifiersInExpression(assignment.getLeftHandSide(), lstVariables, lstIdentifiers);
			findIdentifiersInExpression(assignment.getRightHandSide(), lstVariables, lstIdentifiers);
		}
		/*
		 * CastExpression:
		 *     ( Type ) Expression
		 */
		else if  ( expression instanceof CastExpression ) {
			CastExpression castExpression = (CastExpression)expression;
			findIdentifiersInExpression(castExpression.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * ClassInstanceCreation:
		 *         [ Expression . ]
		 *           new [ < Type { , Type } > ]
		 *           Type ( [ Expression { , Expression } ] )
		 *           [ AnonymousClassDeclaration ]
		 */
		else if  ( expression instanceof ClassInstanceCreation ) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation)expression; 
			findIdentifiersInExpression(classInstanceCreation.getExpression(), lstVariables, lstIdentifiers);
			for ( int i = 0; i < classInstanceCreation.arguments().size(); i++ )
				findIdentifiersInExpression((Expression)classInstanceCreation.arguments().get(i), lstVariables, lstIdentifiers);
		}
		/*
		 * ConditionalExpression:
		 *     Expression ? Expression : Expression
		 */
		else if  ( expression instanceof ConditionalExpression ) {
			ConditionalExpression conditionalExpression = (ConditionalExpression)expression; 
			findIdentifiersInExpression(conditionalExpression.getExpression(), lstVariables, lstIdentifiers);
			findIdentifiersInExpression(conditionalExpression.getThenExpression(), lstVariables, lstIdentifiers);
			findIdentifiersInExpression(conditionalExpression.getElseExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * FieldAccess:
		 *  		Expression . Identifier
		 */
		else if  ( expression instanceof FieldAccess ) {
			FieldAccess fieldAccess = (FieldAccess)expression;
			findIdentifiersInExpression(fieldAccess.getName(), lstVariables, lstIdentifiers);
			findIdentifiersInExpression(fieldAccess.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * InfixExpression:
		 *     Expression InfixOperator Expression { InfixOperator Expression } 
		 */
		else if  ( expression instanceof InfixExpression ) {
			InfixExpression infixExpression = (InfixExpression)expression; 
			findIdentifiersInExpression(infixExpression.getLeftOperand(), lstVariables, lstIdentifiers);
			findIdentifiersInExpression(infixExpression.getRightOperand(), lstVariables, lstIdentifiers);
			for ( int i = 0; i < infixExpression.extendedOperands().size(); i++ )
				findIdentifiersInExpression( (Expression)infixExpression.extendedOperands().get(i), lstVariables, lstIdentifiers);
		}
		/*
		 * InstanceofExpression:
		 *     Expression instanceof Type
		 */
		else if  ( expression instanceof InstanceofExpression ) {
			InstanceofExpression instanceofExpression = (InstanceofExpression)expression;
			findIdentifiersInExpression( instanceofExpression.getLeftOperand(), lstVariables, lstIdentifiers);
		}
		/*
		 * MethodInvocation:
		 *      [ Expression . ]
		 *               [ < Type { , Type } > ]
		 *               Identifier ( [ Expression { , Expression } ] )
		 */
		else if  ( expression instanceof MethodInvocation ) {
			MethodInvocation methodInvocation = (MethodInvocation)expression;
			findIdentifiersInExpression ( methodInvocation.getExpression(), lstVariables, lstIdentifiers);
			if ( methodInvocation.arguments() != null )
				for ( int i = 0; i < methodInvocation.arguments().size(); i++ )
					findIdentifiersInExpression((Expression)methodInvocation.arguments().get(i), lstVariables, lstIdentifiers);
		}
		/*
		 * Name:
		 *      SimpleName
		 *      QualifiedName
		 */
		else if  ( expression instanceof SimpleName ) {
			SimpleName simpleName = (SimpleName)expression;
			VariableInfo variableInfo;
			for( int i = 0; i < lstVariables.size(); i++ ) {
				variableInfo = (VariableInfo)lstVariables.get(i);
				if(variableInfo.getName().equals(simpleName.getIdentifier())) {
					lstIdentifiers.add(variableInfo);
					break;
				}
			}
		}
		else if  ( expression instanceof QualifiedName ) {
			QualifiedName qualifiedName = (QualifiedName)expression;
			findIdentifiersInExpression(qualifiedName.getQualifier(), lstVariables, lstIdentifiers);
			findIdentifiersInExpression(qualifiedName.getName(), lstVariables, lstIdentifiers);
		}
		/*
		 * ParenthesizedExpression:
		 *      ( Expression )
		 */
		else if  ( expression instanceof ParenthesizedExpression ) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression)expression;
			findIdentifiersInExpression( parenthesizedExpression.getExpression(), lstVariables, lstIdentifiers);
		}
		/*
		 * PostfixExpression:
		 *     Expression PostfixOperator
		 */
		else if  ( expression instanceof PostfixExpression ) {
			PostfixExpression postfixExpression = (PostfixExpression)expression;
			findIdentifiersInExpression ( postfixExpression.getOperand(), lstVariables, lstIdentifiers);
		}
		/*
		 * SuperFieldAccess:
		 *      [ ClassName . ] super . Identifier
		 */
		else if  ( expression instanceof SuperFieldAccess ) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess)expression;
			findIdentifiersInExpression( superFieldAccess.getQualifier(), lstVariables, lstIdentifiers);
			analyseIdentifier( superFieldAccess.getName() );
		}
		/*
		 * SuperMethodInvocation:
		 *      [ ClassName . ] super .
		 *               [ < Type { , Type } > ]
		 *               Identifier ( [ Expression { , Expression } ] )
		 */
		else if  ( expression instanceof SuperMethodInvocation ) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)expression;
			if ( superMethodInvocation.typeArguments() != null && 
				 superMethodInvocation.typeArguments().size() == 0 )
				findIdentifiersInExpression( superMethodInvocation.getName(),lstVariables, lstIdentifiers);
			findIdentifiersInExpression ( superMethodInvocation.getQualifier(), lstVariables, lstIdentifiers);
			if ( superMethodInvocation.arguments() != null )
				for ( int i = 0; i < superMethodInvocation.arguments().size(); i++ )
					findIdentifiersInExpression((Expression)superMethodInvocation.arguments().get(i), lstVariables, lstIdentifiers);
		}
		/*
		 * VariableDeclarationExpression:
		 *     { ExtendedModifier } Type VariableDeclarationFragment
		 *              { , VariableDeclarationFragment } 
		 */
		else if  ( expression instanceof VariableDeclarationExpression ) {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)expression;
			VariableDeclarationFragment fragment = null;
			// For each fragment
			for ( int i = 0; i < variableDeclarationExpression.fragments().size(); i++ ){
				/*
				 *  VariableDeclarationFragment:
				 *      Identifier { [] } [ = Expression ]
				 */
				fragment = (VariableDeclarationFragment)variableDeclarationExpression.fragments().get(i);
				variables.addElement(new VariableInfo(variableDeclarationExpression.getType().toString(),
						fragment.getName().getIdentifier()));
				findIdentifiersInExpression( fragment.getInitializer(), lstVariables, lstIdentifiers);
			}
		}
	}
	
	/**
	 * Analise a statement in order to get information to generate recommendations.
	 * @param statement
	 * @return
	 */
	private boolean analyseStatement(Statement statement) {
		/*
		 * AssertStatement:
		 *     assert Expression [ : Expression ] ;
		 */
		if ( statement instanceof AssertStatement ) {
			AssertStatement assertStatement = (AssertStatement)statement;
			analyseExpression(assertStatement.getExpression());
			analyseExpression(assertStatement.getMessage());
		}
		/*
		 * BreakStatement:
		 *     break [ Identifier ] ;
		 */
		else if ( statement instanceof BreakStatement ) {
			BreakStatement breakStatement = (BreakStatement)statement;
			analyseIdentifier(breakStatement.getLabel());
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
				analyseExpression((Expression)arguments.get(i));
		}
		/*
		 * ContinueStatement:
		 *     continue [ Identifier ] ;
		 */
		else if ( statement instanceof ContinueStatement ) {
			ContinueStatement continueStatement = (ContinueStatement)statement;
			analyseIdentifier(continueStatement.getLabel());
		}
		/*
		 * DoStatement:
		 *     do Statement while ( Expression ) ;
		 */
		else if ( statement instanceof DoStatement ) {
			DoStatement doStatement = (DoStatement)statement;
			analyseExpression(doStatement.getExpression());
		}
		/*
		 * EnhancedForStatement:
		 *     for ( FormalParameter : Expression )
		 *      			Statement
		 *      
		 * The FormalParameter is represented by a SingleVariableDeclaration  (without an initializer).
		 * 
		 * SingleVariableDeclaration:
		 *     { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
		 */
		else if ( statement instanceof EnhancedForStatement ) {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
			SingleVariableDeclaration formalParameter = enhancedForStatement.getParameter();
			// Is the formal parameter a indication?
			analyseIdentifier( formalParameter.getName() );
			// Is the expression of EnhancedForStatement a indication?
			analyseExpression(enhancedForStatement.getExpression());
		}
		/*
		 *  ExpressionStatement:
		 *      StatementExpression ;
		 */
		else if ( statement instanceof ExpressionStatement ) {
			ExpressionStatement expressionStatement = (ExpressionStatement)statement;
			analyseExpression(expressionStatement.getExpression());
		}
		/*
		 * ForStatement:
		 *     for (
		 *      			[ ForInit ];
		 *       			[ Expression ] ;
		 *       			[ ForUpdate ] )
		 *       			Statement
		 * ForInit:
		 *  		Expression { , Expression }
		 * ForUpdate:
		 *  		Expression { , Expression }
		 */
		else if ( statement instanceof ForStatement ) {
			ForStatement forStatement = (ForStatement)statement;
			// For each initializer
			List expressions = forStatement.initializers();
			for ( int i = 0; i < expressions.size(); i++ )
				analyseExpression((Expression)expressions.get(i) );
			analyseExpression(forStatement.getExpression());
			// For each updater
			expressions = forStatement.updaters();
			for ( int i = 0; i < expressions.size(); i++ )
				analyseExpression((Expression)expressions.get(i) );
		}
		/*
		 * IfStatement:
		 *     if ( Expression ) Statement [ else Statement]
		 */
		else if ( statement instanceof IfStatement ) {
			IfStatement ifStatement = (IfStatement)statement;
			analyseExpression(ifStatement.getExpression());
		}
		/*
		 * LabeledStatement:
		 *     Identifier : Statement
		 */
		else if ( statement instanceof LabeledStatement ) {
			LabeledStatement labeledStatement = (LabeledStatement)statement;
			analyseIdentifier(labeledStatement.getLabel());
		}
		/*
		 * ReturnStatement:
		 *     return [ Expression ] ;
		 */
		else if ( statement instanceof ReturnStatement ) {
			ReturnStatement returnStatement = (ReturnStatement)statement;
			analyseExpression(returnStatement.getExpression());
		}
		/*
		 *  SuperConstructorInvocation:
		 *       [ Expression . ]
		 *                [ < Type { , Type } > ]
		 *                super ( [ Expression { , Expression } ] ) ;
		 */
		else if ( statement instanceof SuperConstructorInvocation ) {
			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;
			analyseExpression(superConstructorInvocation.getExpression() );
			List arguments = superConstructorInvocation.arguments();
			for ( int i = 0; i < arguments.size(); i++ ) 
				analyseExpression((Expression)arguments.get(i) );
		}
		/*
		 * SwitchCase:
		 * 		case Expression  :
		 * 		default :
		 */
		else if ( statement instanceof SwitchCase ) {
			SwitchCase switchCase = (SwitchCase)statement;
			analyseExpression(switchCase.getExpression());
		}
		/*
		 * SwitchStatement:
		 * 		switch ( Expression )
		 *  	{ { SwitchCase | Statement } } }
		 */
		else if ( statement instanceof SwitchStatement ) {
			SwitchStatement switchStatement = (SwitchStatement)statement;
			analyseExpression(switchStatement.getExpression());
		}
		/*
		 * SynchronizedStatement:
		 *     synchronized ( Expression ) Block
		 */
		else if ( statement instanceof SynchronizedStatement ) {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
			analyseExpression(synchronizedStatement.getExpression());
		}
		/*
		 * ThrowStatement:
		 *     throw Expression ;
		 */
		else if ( statement instanceof ThrowStatement ) {
			ThrowStatement throwStatement = (ThrowStatement)statement;
			analyseExpression(throwStatement.getExpression());
		}
		/*
		 * TryStatement:
		 *      try Block 
		 *               { CatchClause }
		 *               [ finally Block ]
		 */
		else if ( statement instanceof TryStatement ) {
			TryStatement tryStatement = (TryStatement)statement;
			List catchs = tryStatement.catchClauses();
			for ( int i = 0; i < catchs.size(); i++ ) {
				analyseIdentifier( (((CatchClause)catchs.get(i)).getException().getName()) );
				analyseExpression( (((CatchClause)catchs.get(i)).getException().getInitializer()) );
			}
		}
		/*
		 * TypeDeclarationStatement:
		 *     TypeDeclaration
		 *     EnumDeclaration
		 */
		//else if ( statement instanceof TypeDeclarationStatement ) {
		//	TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement)statement;
		//}
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
			VariableDeclarationFragment fragment;
			for (int i = 0; i < fragments.size(); i++) {
				fragment = (VariableDeclarationFragment)fragments.get(i);
				analyseExpression(fragment.getInitializer());
				variables.addElement(new VariableInfo(variableDeclarationStatement.getType().toString(),
						fragment.getName().getIdentifier()));
				//analyseIdentifier(fragment.getName());
			}
		}
		/*
		 * WhileStatement:
		 *     while ( Expression ) Statement
		 */
		else if ( statement instanceof WhileStatement ) {
			WhileStatement whileStatement = (WhileStatement)statement;
			analyseExpression(whileStatement.getExpression());
		}

		// Does statement have other statements? 
		List statements = getStatements( statement );
		if ( statements != null ) {
			// Look at each statement
			for ( int i = 0; i < statements.size(); i++ )
				analyseStatement((Statement)statements.get(i));
		}
		return true;
	} 
	
	/**
	 * Analise an expression in order to get information to generate recommendations.
	 * @param expression
	 */
	private void analyseExpression(Expression expression) {
		/*
		 * ArrayAccess:
		 *     Expression [ Expression ]
		 */
		if  ( expression instanceof ArrayAccess ) {
			ArrayAccess arrayAccess = (ArrayAccess)expression; 
			analyseExpression (arrayAccess.getArray());
			analyseExpression(arrayAccess.getIndex());
		}
		/*
		 * ArrayCreation:
		 *     new PrimitiveType [ Expression ] { [ Expression ] } { [ ] }
		 *     new TypeName [ < Type { , Type } > ]
		 *         [ Expression ] { [ Expression ] } { [ ] }
		 *     new PrimitiveType [ ] { [ ] } ArrayInitializer
		 *     new TypeName [ < Type { , Type } > ]
		 *         [ ] { [ ] } ArrayInitializer
		 */
		else if  ( expression instanceof ArrayCreation ) {
			ArrayCreation arrayCreation = (ArrayCreation)expression;
			for ( int i = 0; i < arrayCreation.dimensions().size(); i++ )
				analyseExpression( (Expression)arrayCreation.dimensions().get(i)); 
			analyseExpression( arrayCreation.getInitializer()); 
		}
		/*
		 *  ArrayInitializer:
		 *   		{ [ Expression { , Expression} [ , ]] }
		 */
		else if  ( expression instanceof ArrayInitializer ) {
			ArrayInitializer arrayInitializer = (ArrayInitializer)expression; 
			for ( int i = 0; i < arrayInitializer.expressions().size(); i++ )
				analyseExpression( (Expression) arrayInitializer.expressions().get(i));
		}
		/*
		 * Assignment:
		 *     Expression AssignmentOperator Expression
		 */
		else if  ( expression instanceof Assignment ) {
			Assignment assignment = (Assignment)expression; 
			analyseExpression(assignment.getLeftHandSide());
			analyseExpression(assignment.getRightHandSide());
		}
		/*
		 * CastExpression:
		 *     ( Type ) Expression
		 */
		else if  ( expression instanceof CastExpression ) {
			CastExpression castExpression = (CastExpression)expression;
			analyseExpression( castExpression.getExpression() );
		}
		/*
		 * ClassInstanceCreation:
		 *         [ Expression . ]
		 *           new [ < Type { , Type } > ]
		 *           Type ( [ Expression { , Expression } ] )
		 *           [ AnonymousClassDeclaration ]
		 */
		else if  ( expression instanceof ClassInstanceCreation ) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation)expression; 
			analyseExpression(classInstanceCreation.getExpression());
			for ( int i = 0; i < classInstanceCreation.arguments().size(); i++ )
				analyseExpression((Expression)classInstanceCreation.arguments().get(i));
		}
		/*
		 * ConditionalExpression:
		 *     Expression ? Expression : Expression
		 */
		else if  ( expression instanceof ConditionalExpression ) {
			ConditionalExpression conditionalExpression = (ConditionalExpression)expression; 
			analyseExpression(conditionalExpression.getExpression());
			analyseExpression(conditionalExpression.getThenExpression());
			analyseExpression(conditionalExpression.getElseExpression());
		}
		/*
		 * FieldAccess:
		 *  		Expression . Identifier
		 */
		else if  ( expression instanceof FieldAccess ) {
			FieldAccess fieldAccess = (FieldAccess)expression;
			if ( fieldAccess.getExpression() == null || fieldAccess.getExpression() instanceof ThisExpression ) {
				ArrayList lstVariables = variables.getElementsAtLevel(variables.getLevelCount() - 2);
				FieldInfo fieldInfo = null;
				SimpleName name = fieldAccess.getName();
				for(int i = 0; i < lstVariables.size(); i++) {
					fieldInfo = (FieldInfo)lstVariables.get(i);
					if(fieldInfo.getName().equals(name.getIdentifier())) {
						if( fieldAccess.getExpression() == null )
							lstTypeSimpleRange.add(new SimpleRange(name.getStartPosition(), 0));
						else if(fieldAccess.getExpression() instanceof ThisExpression)
							lstTypeSimpleRange.add(new SimpleRange(fieldAccess.getExpression().getStartPosition(),
									fieldAccess.getExpression().getLength()));
						if(fieldInfo.getModifiers().contains("private") || fieldInfo.getModifiers().contains("protected"))
							lstPrivateFields.add(fieldInfo);
						return;
					}
				}
			}
			else {
				analyseIdentifier( fieldAccess.getName() );
				analyseExpression(fieldAccess.getExpression());
			}
		}
		/*
		 * InfixExpression:
		 *     Expression InfixOperator Expression { InfixOperator Expression } 
		 */
		else if  ( expression instanceof InfixExpression ) {
			InfixExpression infixExpression = (InfixExpression)expression; 
			analyseExpression(infixExpression.getLeftOperand());
			analyseExpression(infixExpression.getRightOperand());
			for ( int i = 0; i < infixExpression.extendedOperands().size(); i++ )
				analyseExpression( (Expression)infixExpression.extendedOperands().get(i) );
		}
		/*
		 * InstanceofExpression:
		 *     Expression instanceof Type
		 */
		else if  ( expression instanceof InstanceofExpression ) {
			InstanceofExpression instanceofExpression = (InstanceofExpression)expression;
			analyseExpression( instanceofExpression.getLeftOperand() );
		}
		/*
		 * MethodInvocation:
		 *      [ Expression . ]
		 *               [ < Type { , Type } > ]
		 *               Identifier ( [ Expression { , Expression } ] )
		 */
		else if  ( expression instanceof MethodInvocation ) {
			MethodInvocation methodInvocation = (MethodInvocation)expression;
			analyseExpression ( methodInvocation.getExpression() );
			if ( methodInvocation.arguments() != null )
				for ( int i = 0; i < methodInvocation.arguments().size(); i++ )
					analyseExpression((Expression)methodInvocation.arguments().get(i));
			
			// If MethodInvocaction belongs to the current type or to an external type 
			if( methodInvocation.getExpression() == null || ( methodInvocation.getExpression() != null && 
					methodInvocation.getExpression() instanceof ThisExpression)) {
				// Find the type this method belong
				TypeInfo typeInfo = wizard.getSelectedMethod().getType();
				ArrayList lstMethods;
				MethodInfo methodInfo = null;
				ArrayList lstParameterInfo;
				List parameters;
				// Find for MethodInvocation
				boolean bMethodFound = false;
				// for current type and each external type
				while ( typeInfo != null && ! bMethodFound) {
					lstMethods = typeInfo.getMethods();
					// for each method, search
					for(int i = 0; i < lstMethods.size(); i++) {
						methodInfo = (MethodInfo)lstMethods.get(i);
						// Does it have the same name, the same type and number of parameters? 
						if ( methodInfo.getName().equals(methodInvocation.getName().getIdentifier()) &&
							 methodInfo.getType().equals(methodInvocation.resolveMethodBinding().getReturnType().getName())	&&
							 methodInfo.getParameters().size() == methodInvocation.typeArguments().size() ) {
							// Compare the type of each parameter
							lstParameterInfo = methodInfo.getParameters();
							parameters = methodInvocation.typeArguments();
							// For each parameter
							bMethodFound = true;
							for ( int j = 0; j < lstParameterInfo.size(); j++ )
								if( !((ParameterInfo)lstParameterInfo.get(j)).getType().equals(
									 ((Type)parameters.get(j)).toString())) {
									bMethodFound = false;
									break;
								}
						}
					}
 					typeInfo = typeInfo.getTypeParent();
				}
				if ( ! bMethodFound ) {
					typeInfo = wizard.getSelectedMethod().getType();
					if(typeInfo.isSubType()) {
						System.out.println("Method " + methodInvocation.getName().getIdentifier() +
							" not found. It might be a method inherited from a super type.");
					}
					else {
						while ((typeInfo = typeInfo.getTypeParent()) != null && ! typeInfo.isSubType());
						if(typeInfo != null && typeInfo.isSubType())
							System.out.println("Method " + methodInvocation.getName().getIdentifier() +
								" not found. It might be a method inherited from a super type of one of external types.");
						else {
							System.err.println("Method " + methodInvocation.getName().getIdentifier() +
								" not internally registered.");
							return;
						}
					}
					if(methodInvocation.getExpression() == null)
						lstTypeSimpleRange.add(new SimpleRange(methodInvocation.getStartPosition(), 0));
					else
						lstTypeSimpleRange.add(new SimpleRange(methodInvocation.getStartPosition(), 
								methodInvocation.getExpression().getLength()));
					return;
				}
				else {
					if(methodInvocation.getExpression() == null)
						lstTypeSimpleRange.add(new SimpleRange(methodInvocation.getStartPosition(), 0));
					else
						lstTypeSimpleRange.add(new SimpleRange(methodInvocation.getStartPosition(), 
								methodInvocation.getExpression().getLength()));
				}
				// Receive the type of method found
				typeInfo = methodInfo.getType();
				
				// Is the method belong to an external type?
				if(typeInfo != wizard.getSelectedMethod().getType()) {
					recomendationText.setText(recomendationText.getText() + 
							"Method \"" + methodInfo.getSignature() + 
							"\" belongs to an external " + (typeInfo.isInterface()?"interface":"class") + 
							" (" + typeInfo.getQualifiedName() + "). You should reorder statements or apply an OO refactoring");
				}
			}
			// If method invocation belong to an argument of method or a field of type 
			else {
				analyseExpression(methodInvocation.getExpression());
			}
		}
		/*
		 * Name:
		 *      SimpleName
		 *      QualifiedName
		 */
		else if  ( expression instanceof SimpleName ) {
			SimpleName simpleName = (SimpleName)expression;
			analyseIdentifier(simpleName);
		}
		else if  ( expression instanceof QualifiedName ) {
			QualifiedName qualifiedName = (QualifiedName)expression;
			analyseExpression(qualifiedName.getName());
		}
		/*
		 * ParenthesizedExpression:
		 *      ( Expression )
		 */
		else if  ( expression instanceof ParenthesizedExpression ) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression)expression;
			analyseExpression( parenthesizedExpression.getExpression() );
		}
		/*
		 * PostfixExpression:
		 *     Expression PostfixOperator
		 */
		else if  ( expression instanceof PostfixExpression ) {
			PostfixExpression postfixExpression = (PostfixExpression)expression;
			analyseExpression ( postfixExpression.getOperand() );
		}
		/*
		 * SuperFieldAccess:
		 *      [ ClassName . ] super . Identifier
		 */
		else if  ( expression instanceof SuperFieldAccess ) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess)expression;
			analyseExpression( superFieldAccess.getQualifier() );
			analyseIdentifier( superFieldAccess.getName() );
		}
		/*
		 * SuperMethodInvocation:
		 *      [ ClassName . ] super .
		 *               [ < Type { , Type } > ]
		 *               Identifier ( [ Expression { , Expression } ] )
		 */
		else if  ( expression instanceof SuperMethodInvocation ) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)expression;
			if ( superMethodInvocation.typeArguments() != null && 
				 superMethodInvocation.typeArguments().size() == 0 )
				analyseIdentifier( superMethodInvocation.getName());
			analyseExpression ( superMethodInvocation.getQualifier() );
			if ( superMethodInvocation.arguments() != null )
				for ( int i = 0; i < superMethodInvocation.arguments().size(); i++ )
					analyseExpression((Expression)superMethodInvocation.arguments().get(i) );
		}
		/*
		 * VariableDeclarationExpression:
		 *     { ExtendedModifier } Type VariableDeclarationFragment
		 *              { , VariableDeclarationFragment } 
		 */
		else if  ( expression instanceof VariableDeclarationExpression ) {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)expression;
			VariableDeclarationFragment fragment = null;
			// For each fragment
			for ( int i = 0; i < variableDeclarationExpression.fragments().size(); i++ ){
				/*
				 *  VariableDeclarationFragment:
				 *      Identifier { [] } [ = Expression ]
				 */
				fragment = (VariableDeclarationFragment)variableDeclarationExpression.fragments().get(i);
				variables.addElement(new VariableInfo(variableDeclarationExpression.getType().toString(),
						fragment.getName().getIdentifier()));
				analyseExpression( fragment.getInitializer() );
			}
		}
	}
	
	/**
	 * Analise an identifier in order to get information to generate recommendations.
	 * @param name
	 */
	private void analyseIdentifier(SimpleName name) {
		// Is it a local variable? If yes, exit
		ArrayList lstVariables = variables.getElementsAtLevel(variables.getLevelCount());
		VariableInfo variableInfo;
		for(int i = 0; i < lstVariables.size(); i++) {
			variableInfo = (VariableInfo)lstVariables.get(i);
			if(variableInfo.getName().equals(name.getIdentifier())) {
				return;
			}
		}
		// Is it method parameter ? If yes, warn Pointcut structure that 
		// "arg" must be used. Rename identifier
			lstVariables = variables.getElementsAtLevel(variables.getLevelCount() - 1);
			for(int i = 0; i < lstVariables.size(); i++) {
				variableInfo = (VariableInfo)lstVariables.get(i);
				if(variableInfo.getName().equals(name.getIdentifier())) {
						lstArgumentSimpleRange.add(new SimpleRange(name.getStartPosition(), name.getLength()));
						return;
					}
			}
		// Is it an public field of current type ? If yes, add type name given by user.
		// If it is not public field, add to the list of not visible fields 
		FieldInfo fieldInfo;
		lstVariables = variables.getElementsAtLevel(variables.getLevelCount() - 2);
		for(int i = 0; i < lstVariables.size(); i++) {
			fieldInfo = (FieldInfo)lstVariables.get(i);
			if(fieldInfo.getName().equals(name.getIdentifier())) {
				lstTypeSimpleRange.add(new SimpleRange(name.getStartPosition(), 0));
				if(fieldInfo.getModifiers().contains("private") || fieldInfo.getModifiers().contains("protected"))
					lstPrivateFields.add(fieldInfo);
				return;
			}
		}
		// Atributes of external type
		for(int i = variables.getLevelCount() - 3; i > 0; i--) {
			lstVariables = variables.getElementsAtLevel(i);
			for(int j = 0; j < lstVariables.size(); j++) {
				fieldInfo = (FieldInfo)lstVariables.get(j);
				if(fieldInfo.getName().equals(name.getIdentifier())) {
					lstExternalFields.add(fieldInfo);
					break;
				}
			}
		}
	}
	
	/**
	 * Get a list of statements from a statement.
	 * @param statement
	 * @return a list of statements.
	 */
	private List getStatements ( Statement statement ) {
		// Does the statement have block?
		Block block = null;
		Statement st = null;
		List statements = null;
		if ( statement instanceof Block )
			block = (Block)statement;
		else if ( statement instanceof DoStatement )
			st = ((DoStatement)statement).getBody();
		else if ( statement instanceof EnhancedForStatement )
			st = ((EnhancedForStatement)statement).getBody();
		else if ( statement instanceof ForStatement )
			st = ((ForStatement)statement).getBody();
		else if ( statement instanceof LabeledStatement )
			st = ((LabeledStatement)statement).getBody();
		else if ( statement instanceof SwitchStatement )
			statements = ((SwitchStatement)statement).statements();
		else if ( statement instanceof SynchronizedStatement )
			block = ((SynchronizedStatement)statement).getBody();
		else if ( statement instanceof WhileStatement )
			st = ((WhileStatement)statement).getBody();
		else if ( statement instanceof IfStatement ) {
			statements = new ArrayList();
			statements.add(((IfStatement)statement).getThenStatement());
			// Is there a else part?
			if ( ((IfStatement)statement).getElseStatement() != null )
				statements.add(((IfStatement)statement).getElseStatement());
		}
		else if ( statement instanceof TryStatement ) {
			statements = new ArrayList();
			statements.add(((TryStatement)statement).getBody());
			List catchClauses = ((TryStatement)statement).catchClauses();
			for ( int i = 0; i < catchClauses.size(); i++ ) 
				statements.add(((CatchClause)catchClauses.get(i)).getBody());
			if ( ((TryStatement)statement).getFinally() != null )
				statements.add(((TryStatement)statement).getFinally());
		}
		
		if ( block != null )
			statements = block.statements();
		else if ( st != null ) {
			statements = new ArrayList();
			statements.add( st );
		}
		return statements;
	}
	
	/**
	 * It shows the aspect code with a generated pointcut and advice.
	 * @param nFirstStatement index of first statement (zero based index)
	 * @param nLastStatements index of last statement (zero based index)
	 */
	protected void generateAspectCode( int nFirstStatement, int nLastStatement ) {
		// It takes out duplicated elements from list of type range
		SimpleRange range_index, range_j;
		int index = 0;
		while (index < lstTypeSimpleRange.size() - 1) {
			range_index = (SimpleRange)lstTypeSimpleRange.get(index);
			int j = index + 1;
			while( j < lstTypeSimpleRange.size() ) {
				range_j = (SimpleRange)lstTypeSimpleRange.get(j);
				if( range_index.start == range_j.start) {
					if(range_index.length != range_j.length)
						System.err.println("Type Range comparation: same starts and different lengths");
					lstTypeSimpleRange.remove(j);
				}
				j++;
			}
			index++;
		}
		
		List statements = methodDeclaration.getBody().statements();
		Statement statement = (Statement)statements.get(nFirstStatement);
		
		// Getting a piece of code that is the statements to be extracted.
		// This piece of code will be changed by inserting objects exposured in pointcuts
		// Therefore, the start index of each statement must be updated according the code being modified
		// offset variable is used to update these start indexes.
		int nBegin = statement.getStartPosition();
		statement = (Statement)statements.get(nLastStatement);
		int nEnd = statement.getStartPosition() + statement.getLength();
		String sNewCode = sJavaCode.substring(nBegin, nEnd);
		int offset = nBegin;
		
		// Getting the original begin and end of each statement:
		ArrayList lstStatementLocation = new ArrayList();
		for( int i = nFirstStatement; i <= nLastStatement && i < statements.size(); i++ ) {
			statement = (Statement)statements.get(i);
			lstStatementLocation.add( new SimpleRange (statement.getStartPosition() - offset, statement.getLength()) );
		}

		// Inserting type name and argument
		String sTypeName = pointcut.getTypeName();
		SimpleRange typeRange, argRange;

		argumentMap.clear();
		// Have default arguments been selected?
		if(pointcut.getCustomArguments() != null) {
			// Relate each method argument name with the new argument name
			List lstMethodParameters = methodDeclaration.parameters();
			for( int i = 0; i < lstMethodParameters.size(); i++ ) {
				SingleVariableDeclaration parameter = (SingleVariableDeclaration)lstMethodParameters.get(i);
				ParameterInfo parameterInfo = (ParameterInfo)pointcut.getCustomArguments().get(i);
				argumentMap.put(parameter.getName().getIdentifier(), parameterInfo.getName());
			}
		}
			
		// i -> type index
		// j -> argument index
		String sOldArgument, sNewArgument;
		int i = 0, j = 0;
		
		// Custom argument names will replace the original argument names
		if(pointcut.getCustomArguments() != null) {
			// There are type and argument objects to be processed
			while ( i < lstTypeSimpleRange.size() && j < lstArgumentSimpleRange.size() ) {
				typeRange = (SimpleRange)lstTypeSimpleRange.get(i);
				argRange = (SimpleRange)lstArgumentSimpleRange.get(j);
				if( typeRange.start < argRange.start ) {
					// Type Range happens first and will be handle first too
					sNewCode = sNewCode.substring(0, typeRange.start - offset) + sTypeName + '.' +
						sNewCode.substring( typeRange.start + typeRange.length - offset + (typeRange.length == 0?0:1), sNewCode.length() );
					offsetStatementRange(lstStatementLocation, typeRange.start - offset, sTypeName.length() - typeRange.length + (typeRange.length == 0?1:0));
					offset -= (sTypeName.length() - typeRange.length + (typeRange.length == 0?1:0));
					i++;
				}
				else {
					// Argument Range happens first and will be handle first too
					sOldArgument = sNewCode.substring(argRange.start - offset, 
							argRange.start  + argRange.length - offset);
					sNewArgument = (String)argumentMap.get(sOldArgument);
					if( sNewArgument == null )
						System.err.println("Argument not found: " + sOldArgument);
					else {
						sNewCode = sNewCode.substring(0, argRange.start - offset) + sNewArgument + 
							sNewCode.substring( argRange.start + argRange.length - offset, sNewCode.length() );
						offsetStatementRange(lstStatementLocation, argRange.start - offset, sNewArgument.length() - sOldArgument.length());
						offset -= sNewArgument.length() - sOldArgument.length();
					}
					j++;
				}
			}

			// There is only argument object to be processed
			while ( j < lstArgumentSimpleRange.size() ) {
				argRange = (SimpleRange)lstArgumentSimpleRange.get(j);

				sOldArgument = sNewCode.substring(argRange.start - offset, 
						argRange.start  + argRange.length - offset);
				sNewArgument = (String)argumentMap.get(sOldArgument);
				if( sNewArgument == null )
					System.err.println("Argument not found: " + sOldArgument);
				else {
					sNewCode = sNewCode.substring(0, argRange.start - offset) + sNewArgument + 
						sNewCode.substring( argRange.start + argRange.length - offset, sNewCode.length() );
					offsetStatementRange(lstStatementLocation, argRange.start - offset, sNewArgument.length() - sOldArgument.length());
					offset -= sNewArgument.length() - sOldArgument.length();
				}
				j++;
			}
		}
		
		// There is only type object to be processed
		while ( i < lstTypeSimpleRange.size()) {
			typeRange = (SimpleRange)lstTypeSimpleRange.get(i);
			sNewCode = sNewCode.substring(0, typeRange.start - offset) + sTypeName + '.' +
				sNewCode.substring( typeRange.start + typeRange.length - offset + (typeRange.length == 0?0:1), sNewCode.length() );
			offsetStatementRange(lstStatementLocation, typeRange.start - offset, sTypeName.length() - typeRange.length + (typeRange.length == 0?1:0));
			offset -= (sTypeName.length() - typeRange.length + (typeRange.length == 0?1:0));
			i++;
		}
		
		// If it doesn't need exposure arguments
		if( lstArgumentSimpleRange.size() == 0 )
			pointcut.removePrimitivePC(Pointcut.PC_ARGS);
		else
			pointcut.addPrimitivePC(Pointcut.PC_ARGS);
		// If it doesn't need exposure current object
		if(lstTypeSimpleRange.size() == 0)
			pointcut.removePrimitivePC(Pointcut.PC_THIS);
		else
			pointcut.addPrimitivePC(Pointcut.PC_THIS);
		switch(wizard.getRefactoring()) {
			case TreeObject.REFACTOR_EXTRACT_BEGINNIG:
				advice = new Advice(Advice.AD_BEFORE, pointcut, sNewCode);
				break;
			case TreeObject.REFACTOR_EXTRACT_END:
				advice = new Advice(Advice.AD_AFTER, pointcut, sNewCode);
				break;
		}
		advice.setStatementLocation(lstStatementLocation);
		previewAspect(sNewCode);
	}
	
	private void previewAspect(String sExtractedCode){
		// Creating pointcut
		String sIdentation = aspectParse.getIdentation();
		pointcut.setIdentation(sIdentation);
		int nBeginRange = aspectParse.addPointcut(pointcut);
		String sPointcutCode = pointcut.toString();
		String adviceName = advice.toString();
	
		String sNewAspect = aspectParse.getCode().substring(0, nBeginRange) + "\r\n" + 
					sPointcutCode +  "\r\n" + adviceName + "\r\n" + 
					aspectParse.getCode().substring(nBeginRange, aspectParse.getCode().length());
		nBeginRange += sIdentation.length();
		aspectText.setText(sNewAspect);
		aspectText.setStyleRanges(0, sNewAspect.length(), new int[]{}, new StyleRange[]{});
		aspectText.setStyleRange(new StyleRange(nBeginRange, 
					sPointcutCode.length(), 
					display.getSystemColor(SWT.COLOR_BLACK), 
					display.getSystemColor(SWT.COLOR_YELLOW), SWT.BOLD));
		aspectText.setStyleRange(new StyleRange(nBeginRange + sPointcutCode.length() + sIdentation.length() + 2, 
				adviceName.length(), 
				display.getSystemColor(SWT.COLOR_BLACK), 
				display.getSystemColor(SWT.COLOR_YELLOW), SWT.BOLD));
		// Scroll to the end of text
		int nEnd = sNewAspect.length() - 1;
		while(sNewAspect.charAt(nEnd) == '\n' || sNewAspect.charAt(nEnd) == '\r')
			nEnd--;
		aspectText.setSelection(nEnd);
		aspectText.showSelection();
	}
	
	/**
	 * Offset a list of statement range beginning from a specified location (nLocation), and it can update the
	 * length of an interval that contain the location; the subsequent statements have begin index updated.
	 * @param lstStatementRange
	 * @param nLocation
	 * @param nOffset
	 */
	private void offsetStatementRange(ArrayList lstStatementRange, int nLocation, int nOffset) {
		SimpleRange simpleRange;
		for( int i = 0; i < lstStatementRange.size(); i ++) {
			simpleRange = (SimpleRange)lstStatementRange.get(i);
			// statement range found
			if( simpleRange.start <= nLocation && simpleRange.start + simpleRange.length >= nLocation ) {
				simpleRange.length += nOffset;
				for ( int j = i + 1; j < lstStatementRange.size(); j++ ) {
					simpleRange = (SimpleRange)lstStatementRange.get(j);
					simpleRange.start += nOffset;
				}
				break;
			}
			else if ( simpleRange.start > nLocation ) {
				System.err.println("Can't find statement range");
				return;
			}
		}
	}

	public boolean saveAjFile() {
		IViewPart viewPart = wizard.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("br.ufscar.dc.rejasp.views.IndicationTree");
		IFile file;
		file = wizard.getAspectFile();
		// Sync resource 
		if (saveInFile(file.getFullPath().toOSString(), aspectText.getText())) {
			try {
				file.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				((IndicationTree)viewPart).constructAspectFile(file);
			} catch (CoreException e) {
				System.err.println(e.getLocalizedMessage());
			}
		}
		else
			return false;
		return true;
	}

	protected boolean saveJavaFile( int nBegin, int nEnd ) {
		// Insert original method
		FileInfo fileInfo = wizard.getSelectedMethod().getFile();
		// Getting file name
		String sPath = fileInfo.getPath();
		int nIndex = sPath.lastIndexOf('\\');
		sPath = sPath.substring(nIndex + 1, sPath.length());
		sPath = '/' + fileInfo.getPackage().replace('.', '/') + '/' + sPath;
		IFile file = (IFile)wizard.getProject().findMember(sPath);
		
		InputStream input = null;
		try {
			input = file.getContents();
		} catch ( CoreException e ) {
			System.err.println( e.getMessage() );
			return false;
		}
		
		sJavaCode = new String();
		try	{
			for(int j = input.read(); j != -1; j = input.read()) {
				sJavaCode += (char) j;
	        }
			input.close();
	    } catch(IOException e) {
	        System.err.println("Could not read from file");
	        return false;
	    }
	    
	    ArrayList lstEnclosuredTypes = new ArrayList();
	    TypeInfo typeInfo = wizard.getSelectedMethod().getType();
	    do {
	    	lstEnclosuredTypes.add(0, typeInfo);
	    	typeInfo = typeInfo.getTypeParent();
	    }while( typeInfo != null );
	    
	    // Build the AST structure
	    Document document = new Document(sJavaCode);
	    ASTParser parser = ASTParser.newParser(AST.JLS3);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5
	    parser.setSource(document.get().toCharArray());
	    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.recordModifications();

	    // Find method which statements must be extracted
		// Find the type (interface or class)
		List lstTypes = unit.types();
		TypeDeclaration[] types = new TypeDeclaration[lstTypes.size()];
		TypeDeclaration typeDeclaration = null;
		for ( int i = 0; i < lstTypes.size(); i++ )
			types[i] = (TypeDeclaration)lstTypes.get(i);
		// For each enclosured type
		for ( int i = 0 ; i < lstEnclosuredTypes.size(); i++ ) {
			typeInfo = (TypeInfo)lstEnclosuredTypes.get(i);
			// Find the current enclosured type
			for( int j = 0; j < types.length; j++ )
				if ( typeInfo.getName().equals(types[j].getName().getIdentifier()) ) {
					if(i == lstEnclosuredTypes.size() - 1)
						typeDeclaration = types[j];
					types = types[j].getTypes();
					break;
				}
		}
		// Find method
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		MethodDeclaration method = null;
		for(int i = 0; i < methods.length; i++)
			if(methods[i].getName().getIdentifier().equals(wizard.getSelectedMethod().getName())) {
				method = methods[i];
				break;
			}
		
		// Removing statements from method
		List statements = method.getBody().statements();
		for (int i = nBegin; i <= nEnd; i++)
			statements.remove(nBegin);
		
		String sSourceOOCode;
		try {
			TextEdit edits = unit.rewrite(document, null);
			edits.apply(document);
			sSourceOOCode = document.get();
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalStateException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		saveInFile(fileInfo.getPath(), sSourceOOCode);
		
		// Sync resource 
		IViewPart viewPart = wizard.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("br.ufscar.dc.rejasp.views.IndicationTree");
		if (viewPart == null) {
			System.err.println("Couldn't find indication view");
			return false;
		}
		try {
			file.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			((IndicationTree)viewPart).updateFileNode(fileInfo.getPath());
			ArrayList lstFileInfo = ((IndicationTree)viewPart).getFileInfo();
			for( int j = 0; j < lstFileInfo.size(); j++)
				if(fileInfo.equals(lstFileInfo.get(j))) {
					lstFileInfo.remove(j);
					break;
				}
		} catch (CoreException e) {
			System.err.println(e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	private boolean saveInFile( String sFullPath, String sContent) {
		String sPath = wizard.getProject().getWorkspace().getRoot().getLocation() + sFullPath;
		FileOutputStream  outputStream = null;
		try { 
			outputStream = new FileOutputStream( sPath );
		} catch ( IOException ex ) {
			System.err.println(ex.getLocalizedMessage());
			return false;
		}
		PrintWriter printWriter = new PrintWriter(outputStream);
		printWriter.print(sContent);
		printWriter.flush();
		try {
			outputStream.flush();
			outputStream.close();
		} catch ( IOException ex ) {
			System.err.println(ex.getLocalizedMessage());
			return false;
		}
		return true;
	}
}

