package br.ufscar.dc.rejasp.wizards.refactoring.RefactoringWizard;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import br.ufscar.dc.rejasp.model.Aspect;
import br.ufscar.dc.rejasp.model.Pointcut;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.FileInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.ParameterInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;
import br.ufscar.dc.rejasp.views.TreeObject;

public class PointcutPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private RefactoringWizard wizard;
	
	// Default Values
	private String sPointcutName;
	private String sCurrentObject;
	private ArrayList lstPointcut;
	private Aspect aspectParse;
	
	private Shell shell;
	private Display display;
	private Button btnDefault;
	private Button btnCustomize;
	private StyledText styledText;
	private Text txtName;
	private Text txtCurrentObject;
	private Tree treeArguments;
	
	public PointcutPage() {
		super("Pointcut Definition");
		setTitle("Pointcut Preview");
		setDescription("You can use default pointcut names or customize names");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (RefactoringWizard)getWizard();

		shell = parent.getShell();
		display = parent.getDisplay();
		// create the composite to hold the widgets
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 4;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);
		
		gd = new GridData();
		gd.horizontalSpan = 2;
		btnDefault = new Button(composite, SWT.RADIO);
		btnDefault.setText("Use default pointcut names");
		btnDefault.setLayoutData(gd);
		
		gd = new GridData();
		gd.horizontalSpan = 2;
		Label lbPointcutPreview = new Label(composite, SWT.NONE);
		lbPointcutPreview.setText("Pointcut Preview");
		lbPointcutPreview.setLayoutData(gd);
		
		gd = new GridData();
		gd.horizontalSpan = 2;
		btnCustomize = new Button(composite, SWT.RADIO);
		btnCustomize.setText("Customize pointcut names");
		btnCustomize.setLayoutData(gd);

		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 9;
		styledText = new StyledText(composite, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		styledText.setLayoutData(gd);
		
		Label lbName = new Label(composite, SWT.NONE);
		lbName.setText("Pointcut Name");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		lbName.setLayoutData(gd);
		
		txtName = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		txtName.setLayoutData(gd);
		
		Label lbObject = new Label(composite, SWT.NONE);
		lbObject.setText("Current Object Name");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		lbObject.setLayoutData(gd);
		
		txtCurrentObject = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		txtCurrentObject.setLayoutData(gd);

		wizard.fillCells(composite, 2, 1);
		
		Label lbArguments = new Label(composite, SWT.NONE);
		lbArguments.setText("Method Argument Names");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		lbArguments.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 4;
		treeArguments = new Tree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SIMPLE);
		treeArguments.setHeaderVisible(true);
		TreeColumn tcName = new TreeColumn(treeArguments, SWT.LEFT);
		tcName.setText("Name");
		tcName.setWidth(150);
		TreeColumn tcType = new TreeColumn(treeArguments, SWT.LEFT);
		tcType.setText("Type");
		tcType.setWidth(150);
		treeArguments.setLayoutData(gd);
		
		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		btnDefault.addListener(SWT.MouseUp, this);
		btnCustomize.addListener(SWT.MouseUp, this);
		txtName.addListener(SWT.KeyUp, this);
		txtCurrentObject.addListener(SWT.KeyUp, this);
		treeArguments.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "Click Next to proceed", null);
	    if( event.widget == btnDefault ) {
			txtName.setEnabled(false);
			txtCurrentObject.setEnabled(false);
			treeArguments.setEnabled(false);
	    }
	    else if( event.widget == btnCustomize ) {
			txtName.setEnabled(true);
			txtCurrentObject.setEnabled(true);
			treeArguments.setEnabled(true);
	    }
	    else if( event.widget == treeArguments && event.item != null ) {
	    	TreeItem item = (TreeItem)event.item;
			InputDialog dialog = new InputDialog(shell, "Argument Name", "Please, type a new Argument name", item.getText(0), null);
			do {
				dialog.open();
			}while(dialog.getValue().length() == 0);
			item.setText(0, dialog.getValue());
	    }

	    if( btnCustomize.getSelection() ) {
	    	if(txtName.getText().length() == 0)
	    		status = new Status(IStatus.ERROR, "not_used", 0, "Type a pointcut name", null);
	    	else if( txtName.getText().length() > 0 && pointcutExists(txtName.getText()))
	    		status = new Status(IStatus.ERROR, "not_used", 0, "Pointcut " + txtName.getText() + 
	    				" already exists", null);
	    	else if(txtCurrentObject.getText().length() == 0)
	    		status = new Status(IStatus.ERROR, "not_used", 0, "Type current object name", null);
	    }
	    generatePreview();
	    	
		applyToStatusLine(status);
		wizard.getContainer().updateButtons();
	}

	/**
	 * Applies the status to the status line of a dialog page.
	 */
	private void applyToStatusLine(IStatus status) {
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
	 * Could I procced to next page?
	 */
	public boolean canFlipToNextPage() {
		if (getErrorMessage() != null) {
			String sOldMessage = getErrorMessage();
			setErrorMessage(null);
			setMessage(sOldMessage, WizardPage.ERROR);
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
	 * It makes all the setting before go to the next page
	 */
	public IWizardPage getNextPage() {
		wizard.setPointcut(getPointcut());
		switch(wizard.getRefactoring()) {
			case TreeObject.REFACTOR_EXTRACT_BEGINNIG:
				wizard.visualizationEBPage.onEnterPage();
				return wizard.visualizationEBPage;
			case TreeObject.REFACTOR_EXTRACT_END:
				wizard.visualizationEEPage.onEnterPage();
				return wizard.visualizationEEPage;
			default:
				return null;
		}
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		// Putting default data
		getSourceInfo();
		
		txtName.setText(sPointcutName);
		txtCurrentObject.setText(sCurrentObject);
		ArrayList parameters = wizard.getSelectedMethod().getParameters();
		ParameterInfo parameterInfo;
		TreeItem item;
		treeArguments.removeAll();
		for( int i = 0; i < parameters.size(); i++ ) {
			parameterInfo = (ParameterInfo)parameters.get(i);
			item = new TreeItem(treeArguments, SWT.NONE);
			item.setText(new String[]{ parameterInfo.getName(), parameterInfo.getType() });
		}
		
		btnDefault.setSelection(true);
		btnCustomize.setSelection(false);
		
		txtName.setEnabled(false);
		txtCurrentObject.setEnabled(false);
		treeArguments.setEnabled(false);
		
		generatePreview();
	}
	
	private boolean pointcutExists(String sPcName) {
		for(int i = 0; i < lstPointcut.size(); i++)
			if(lstPointcut.get(i).equals(sPcName))
				return true;
		return false;
	}
	
	/**
	 * Find pointcut names in order to generate a new pointcut name 
	 */
	public void getSourceInfo() {
		/*
		 * Generate a Current Object Name
		 */
		IFile file;
		file = wizard.getAspectFile();
		String sAspectCode;
		// Getting aspect source code (in string)
		try {
			AJCompilationUnit unit =
				AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
			unit.requestOriginalContentMode();
			sAspectCode = unit.getBuffer().getContents();
		} catch (JavaModelException e) {
			System.err.println(e.getLocalizedMessage());
			return;
		}
		// Find comments in aspect
		aspectParse = new Aspect(sAspectCode);
		if( !aspectParse.parse() )
			System.err.println("Something wrong happened");
		String sType = wizard.getSelectedType().getName();
		String sMethodBody = getMethodBody();
		sCurrentObject = "";
		if(sType.charAt(0) >= 'A' && sType.charAt(0) <= 'Z') {
			sCurrentObject = sType.toLowerCase();
			if(sMethodBody.contains(sCurrentObject))
				sCurrentObject = "";
		}
		if(sCurrentObject.length() == 0 && sType.charAt(0) != '_') {
			sCurrentObject = '_' + sType;
			if(sMethodBody.contains(sCurrentObject))
				sCurrentObject = "";
		}
		if( sCurrentObject.length() == 0 ) {
			int i = 1;
			do {
				sCurrentObject = '_' + sType + i;
				i++;
			}while(sMethodBody.contains(sCurrentObject));
		}
		
		/*
		 * Generate a Pointcut Name
		 */
		lstPointcut = aspectParse.getPointcutNames();
		sPointcutName = "";
		for(int i = 1; sPointcutName.length() == 0; i++) {
			sPointcutName = "pc_";
			if ( i < 10 )
				sPointcutName += "0";
			sPointcutName += i;
			for(int j = 0; j < lstPointcut.size() && sPointcutName.length() > 0; j++)
				if(lstPointcut.get(j).equals(sPointcutName))
					sPointcutName = "";
		}
	}
	
	private String getMethodBody() {
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
			return null;
		}
		
		String buffer = new String();
		try	{
			for(int j = input.read(); j != -1; j = input.read()) {
	            buffer += (char) j;
	        }
			input.close();
	    } catch(IOException e) {
	        System.err.println("Could not read from file");
	        return null;
	    }

	    // Build the AST structure
	    ASTParser parser = ASTParser.newParser(AST.JLS3);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5
	    parser.setSource(buffer.toCharArray());
	    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		
	    // Getting all TypeDeclaration of selected method
	    TypeInfo typeInfo = methodInfo.getType();
	    ArrayList lstTypes = new ArrayList();
	    lstTypes.add(typeInfo.getName());
		while(typeInfo != null) {
			lstTypes.add(typeInfo.getName());
			typeInfo = typeInfo.getTypeParent();
		}
	    
	    // Getting all typeDeclaration of compilation unit
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
		MethodDeclaration methodDeclaration = null;
		for(int i = 0; i < methods.length; i++) 
			if ( methods[i].getName().getIdentifier().equals(methodInfo.getName()) && 
					methodInfo.equalsParameterList(methods[i].parameters())) {
				methodDeclaration = methods[i];
				break;
			}
		String sMethodCode = "";
		List statements = methodDeclaration.getBody().statements();
		for(int i = 0; i < statements.size(); i++)
			sMethodCode += ((Statement)statements.get(i)).toString();
		return sMethodCode;
	}
	
	private Pointcut getPointcut() {
		Pointcut pointcut;
		if(btnDefault.getSelection())
			pointcut = new Pointcut(sPointcutName, sCurrentObject, wizard.getSelectedMethod(), Pointcut.PC_NONE);
		else {
			pointcut = new Pointcut(txtName.getText(), txtCurrentObject.getText(), wizard.getSelectedMethod(), Pointcut.PC_NONE);
			ArrayList lstArguments = new ArrayList();
			TreeItem[] items = treeArguments.getItems();
			for( int i = 0; i < items.length; i++)
				lstArguments.add(new ParameterInfo( items[i].getText(1), 
						items[i].getText(0), wizard.getSelectedMethod() ));
			pointcut.customizeArguments(lstArguments);
		}
		switch(wizard.getRefactoring()) {
			case TreeObject.REFACTOR_EXTRACT_BEGINNIG:
			case TreeObject.REFACTOR_EXTRACT_END:
				pointcut.addPrimitivePC(Pointcut.PC_EXECUTION);
				pointcut.addPrimitivePC(Pointcut.PC_THIS);
				pointcut.addPrimitivePC(Pointcut.PC_ARGS);
				break;
		}
		return pointcut;
	}
	
	private void generatePreview() {
		String sNewAspect = null;
		Pointcut pointcut = getPointcut();
		switch(wizard.getRefactoring()) {
			case TreeObject.REFACTOR_EXTRACT_BEGINNIG:
				pointcut.addPrimitivePC(Pointcut.PC_EXECUTION);
				pointcut.addPrimitivePC(Pointcut.PC_THIS);
				pointcut.addPrimitivePC(Pointcut.PC_ARGS);
		}
		String sIdentation = aspectParse.getIdentation();
		pointcut.setIdentation(sIdentation);
		int nBeginRange = aspectParse.addPointcut(pointcut);
		sNewAspect = aspectParse.getCode().substring(0, nBeginRange) + '\n' + pointcut.toString() + 
					'\n' + aspectParse.getCode().substring(nBeginRange, aspectParse.getCode().length());
		nBeginRange += sIdentation.length();
		styledText.setText(sNewAspect);
		styledText.setStyleRanges(0, sNewAspect.length(), new int[]{}, new StyleRange[]{});
		styledText.setStyleRange(new StyleRange(nBeginRange, 
					pointcut.toString().length(), 
					display.getSystemColor(SWT.COLOR_BLACK), 
					display.getSystemColor(SWT.COLOR_YELLOW), SWT.BOLD));
	}
}
