package br.ufscar.dc.rejasp.wizards.refactoring.RefactoringWizard;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.FileInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;

public class MethodSelectionPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private RefactoringWizard wizard;
	private Tree methodsTree;
	private Display display;
	private StyledText styledText;
	private MethodInfo selectedMethodInfo;

	public MethodSelectionPage() {
		super("Method Selection Page");
		setTitle("Method Selection");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (RefactoringWizard)getWizard();

		// create the composite to hold the widgets
		Composite composite =  new Composite(parent, SWT.NULL);
		display = composite.getDisplay();

		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 2;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);

		new Label(composite, SWT.NONE).setText("Constructors and methods available");
		wizard.fillCells(composite, 1, 1);
		
		GridData gd;
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 4;
		methodsTree = new Tree (composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SIMPLE );
		methodsTree.setHeaderVisible(true);
		TreeColumn tcName = new TreeColumn(methodsTree, SWT.LEFT);
		tcName.setText("Name");
		tcName.setWidth(150);
		TreeColumn tcConstruction = new TreeColumn(methodsTree, SWT.LEFT);
		tcConstruction.setText("Construction");
		tcConstruction.setWidth(150);
		TreeColumn tcLocale = new TreeColumn(methodsTree, SWT.LEFT);
		tcLocale.setText("Locale / Return Type");
		tcLocale.setWidth(150);
		TreeColumn tcModifiers = new TreeColumn(methodsTree, SWT.LEFT);
		tcModifiers.setText("Modifiers");
		tcModifiers.setWidth(150);
		methodsTree.setLayoutData(gd);

		wizard.fillCells(composite, 2, 1);

		new Label(composite, SWT.NONE).setText("Original Source");
		wizard.fillCells(composite, 1, 1);

		styledText = new StyledText(composite, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 4;
		styledText.setLayoutData(gd);
		
		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		methodsTree.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "Click Next to proceed", null);

//		applyToStatusLine(status);
		if ( event.widget == methodsTree ) {
			if (methodsTree.getSelectionCount() == 0)
				status = new Status(IStatus.ERROR, "not_used", 0, "Select a method", null);
		}
		if ( event.item != null ) {
			selectedMethodInfo = (MethodInfo)((TreeItem)event.item).getData();
			viewMethod();
		}
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
		wizard.setSelectedMethod(selectedMethodInfo);
		if(wizard.getRefactoring() == 0) {
			wizard.refactoringPage.onEnterPage();
			return wizard.refactoringPage;
		}
		else {
			wizard.aspectPage.onEnterPage();
			return wizard.aspectPage;
		}
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		setDescription("Select a method to reorganize");
		styledText.setText("");
		
		// Build the method tree
		methodsTree.removeAll();
		MethodInfo methodInfo;
		TreeItem methodItem;
		ArrayList lstMethods = wizard.getSelectedType().getMethods();
		for ( int i = 0; i < lstMethods.size(); i++ ) {
			methodInfo = (MethodInfo)lstMethods.get(i);
			if ( wizard.getRefactoring() == 0 || (methodInfo.getRefactoring() & wizard.getRefactoring()) != 0 ) {
				methodItem = new TreeItem( methodsTree, SWT.NONE );
				methodItem.setText(new String[] { methodInfo.getName(), 
						methodInfo.isConstructor()?"Constructor":"Method", 
								methodInfo.getReturnType(),methodInfo.getModifiers() });
				methodItem.setData(methodInfo);
			}
		}
		applyToStatusLine(new Status(IStatus.ERROR, "not_used", 0, "Select a method", null));
	}
	
	public boolean viewMethod() {
		FileInfo fileInfo = selectedMethodInfo.getFile();
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
		
		String buffer = new String();
		try	{
			for(int j = input.read(); j != -1; j = input.read()) {
	            buffer += (char) j;
	        }
			input.close();
	    } catch(IOException e) {
	        System.err.println("Could not read from file");
	        return false;
	    }
	    
	    ArrayList lstStyleRange = new ArrayList();
	    
	    // Build the AST structure
	    ASTParser parser = ASTParser.newParser(AST.JLS3);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5
	    parser.setSource(buffer.toCharArray());
	    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		
	    // Getting all TypeDeclaration of selected method
	    TypeInfo typeInfo = selectedMethodInfo.getType();
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
			if ( methods[i].getName().getIdentifier().equals(selectedMethodInfo.getName()) && 
					selectedMethodInfo.equalsParameterList(methods[i].parameters())) {
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
					markers[i].getAttribute(IMarker.CHAR_START, -1) < methodDeclaration.getStartPosition() + methodDeclaration.getLength()	)
						lstStyleRange.add(new StyleRange(
								markers[i].getAttribute(IMarker.CHAR_START, -1) - methodDeclaration.getStartPosition(),
								markers[i].getAttribute(IMarker.CHAR_END, -1) - markers[i].getAttribute(IMarker.CHAR_START, -1), 
								display.getSystemColor(SWT.COLOR_BLACK), display.getSystemColor(SWT.COLOR_YELLOW)));			
		} catch (CoreException e) {
			System.err.println( e.getMessage() );
		}
	    styledText.setText(buffer.substring(methodDeclaration.getStartPosition(), 
				methodDeclaration.getStartPosition() + methodDeclaration.getLength()));
	    for(int i = 0; i < lstStyleRange.size(); i++)
	    	styledText.setStyleRange((StyleRange)lstStyleRange.get(i));
		return true;
	}
}
