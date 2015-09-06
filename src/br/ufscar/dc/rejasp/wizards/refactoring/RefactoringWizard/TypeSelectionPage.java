package br.ufscar.dc.rejasp.wizards.refactoring.RefactoringWizard;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;
import br.ufscar.dc.rejasp.views.TreeObject;

public class TypeSelectionPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private RefactoringWizard wizard;

	private Button btnAllTypes;
	private Button btnRefactoringTypes;
	private Combo cbRefactoring;
	private Tree tree;
	
	TypeInfo selectedTypeInfo;

	public TypeSelectionPage() {
		super("Type Selection Page");
		setTitle("Type Selection");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (RefactoringWizard)getWizard();

		// create the composite to hold the widgets
		Composite composite =  new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 2;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);
		
		GridData gd;
		btnAllTypes = new Button(composite, SWT.RADIO);
		btnAllTypes.setText("Show all classes and interfaces of project");
		gd = new GridData();
		gd.horizontalSpan = 2;
		btnAllTypes.setLayoutData(gd);

		btnRefactoringTypes = new Button(composite, SWT.RADIO);
		btnRefactoringTypes.setText("Show classes and interfaces which the following reorganization method is applied");
		gd = new GridData();
		gd.horizontalSpan = 2;
		btnAllTypes.setLayoutData(gd);

		cbRefactoring = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY );
		cbRefactoring.add("Extract Beginning");
		cbRefactoring.add("Extract End");
//		cbRefactoring.add("Before Call");
//		cbRefactoring.add("After Call");
//		cbRefactoring.add("Extract Conditional");
//		cbRefactoring.add("Pre Return");

		wizard.fillCells(composite, 2, 1);
		
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 7;
		tree = new Tree (composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE );
		tree.setHeaderVisible(true);
		TreeColumn tcName = new TreeColumn(tree, SWT.LEFT);
		tcName.setText("Name");
		tcName.setWidth(150);
		TreeColumn tcConstruction = new TreeColumn(tree, SWT.LEFT);
		tcConstruction.setText("Construction");
		tcConstruction.setWidth(150);
		TreeColumn tcLocale = new TreeColumn(tree, SWT.LEFT);
		tcLocale.setText("Locale");
		tcLocale.setWidth(150);
		TreeColumn tcModifiers = new TreeColumn(tree, SWT.LEFT);
		tcModifiers.setText("Modifiers");
		tcModifiers.setWidth(150);
		tree.setLayoutData(gd);

		setControl(composite);
		addListeners();
		
		onEnterPage();
	}

	private void addListeners() {
		btnAllTypes.addListener(SWT.MouseUp, this);
		btnRefactoringTypes.addListener(SWT.MouseUp, this);
		cbRefactoring.addListener(SWT.Selection, this);
		tree.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "Click Next to proceed", null);
	    if ( event.widget == btnAllTypes ) {
			buildTypeTree(0);
			cbRefactoring.setEnabled(false);
			status = new Status(IStatus.ERROR, "not_used", 0, "Select one class or interface and click Next", null);
	    }
	    else if ( event.widget == btnRefactoringTypes ) {
	    	cbRefactoring.setEnabled(true);
	    	cbRefactoring.deselectAll();
	    	status = new Status(IStatus.ERROR, "not_used", 0, "Select a reorganization method", null);
	    }
	    else if ( event.widget == cbRefactoring ) {
	    	switch(cbRefactoring.getSelectionIndex()) {
	    	case 0:
	    		buildTypeTree(TreeObject.REFACTOR_EXTRACT_BEGINNIG);
	    		break;
	    	case 1:
	    		buildTypeTree(TreeObject.REFACTOR_EXTRACT_END);
	    		break;
	    	case 2:
	    		buildTypeTree(TreeObject.REFACTOR_EXTRACT_BEFORE_CALL);
	    		break;
	    	case 3:
	    		buildTypeTree(TreeObject.REFACTOR_EXTRACT_AFTER_CALL);
	    		break;
	    	case 4:
	    		buildTypeTree(TreeObject.REFACTOR_EXTRACT_CONDICIONAL);
	    		break;
	    	case 5:
	    		buildTypeTree(TreeObject.REFACTOR_PRE_RETURN);
	    		break;
	    	}
	    	if(tree.getItemCount() == 0)
				status = new Status(IStatus.ERROR, "not_used", 0, "No classes or interfaces found, select another reorganization method", null);
	    	else
				status = new Status(IStatus.ERROR, "not_used", 0, "Select one class or interface and click Next", null);
	    }
	    else if ( tree.getSelectionCount() == 0 )
    		status = new Status(IStatus.ERROR, "not_used", 0, "Select one class or interface and click Next", null);
    	if ( event.item != null )
    		selectedTypeInfo = (TypeInfo)((TreeItem)event.item).getData();
		applyToStatusLine(status);
		wizard.getContainer().updateButtons();
	}
	
	private void buildTypeTree( int refactoring ) {
		ArrayList lstTypeInfo = wizard.getTypes();
		TypeInfo element;
		TreeItem item;
		tree.removeAll();
		// for each type
		for ( int i = 0; i < lstTypeInfo.size(); i++ ) {
			element = (TypeInfo)lstTypeInfo.get(i);
			if ( refactoring == 0 || (refactoring & element.getRefactoring()) != 0 ) {
				item = new TreeItem( tree, SWT.NONE );
				item.setText(new String[] { element.getName(), 
						element.isInterface()?"Interface":"Class",
						element.getLocale(), element.getModifiers()});
				item.setData(element);
			}
		}
		item = tree.getTopItem();
		if ( item != null )
			tree.showItem(item);
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
		else if ( tree.getSelectionCount() == 0 )
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
	 * It makes all the setting before go to the next page
	 */
	public IWizardPage getNextPage() {
    	int refactoring = 0;
    	if ( btnRefactoringTypes.getSelection() )
	    	switch(cbRefactoring.getSelectionIndex()) {
	    	case 0:
	    		refactoring = TreeObject.REFACTOR_EXTRACT_BEGINNIG;
	    		break;
	    	case 1:
	    		refactoring = TreeObject.REFACTOR_EXTRACT_END;
	    		break;
	    	case 2:
	    		refactoring = TreeObject.REFACTOR_EXTRACT_BEFORE_CALL;
	    		break;
	    	case 3:
	    		refactoring = TreeObject.REFACTOR_EXTRACT_AFTER_CALL;
	    		break;
	    	case 4:
	    		refactoring = TreeObject.REFACTOR_EXTRACT_CONDICIONAL;
	    		break;
	    	case 5:
	    		refactoring = TreeObject.REFACTOR_PRE_RETURN;
	    		break;
	    	}
    	wizard.setRefactoring(refactoring);
    	wizard.setSelectedType(selectedTypeInfo);
    	wizard.methodPage.onEnterPage();
    	return wizard.methodPage;
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		setDescription("Select a class or interface that will be the target of reorganization");
		btnAllTypes.setSelection(true);
		btnRefactoringTypes.setSelection(false);
		cbRefactoring.setEnabled(false);
		buildTypeTree(0);
	}
}