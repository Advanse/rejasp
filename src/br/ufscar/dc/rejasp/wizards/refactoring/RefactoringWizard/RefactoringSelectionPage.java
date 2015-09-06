package br.ufscar.dc.rejasp.wizards.refactoring.RefactoringWizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;
import br.ufscar.dc.rejasp.views.TreeObject;

public class RefactoringSelectionPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private RefactoringWizard wizard;

	private List lstRefactoring;
	
	private Text txtDescription;
	
	public RefactoringSelectionPage() {
		super("Selection Page of Reorganization Method");
		setTitle("Selection of Reorganization");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (RefactoringWizard)getWizard();

		// create the composite to hold the widgets
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 1;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);
		
		new Label(composite, SWT.NONE).setText("Reorganization Available");

		lstRefactoring = new List(composite, SWT.SIMPLE);
		lstRefactoring.add("Extract Beginning");
		lstRefactoring.add("Extract End");
//		lstRefactoring.add("Extract Before Call");
//		lstRefactoring.add("Extract After Call");
//		lstRefactoring.add("Extract Conditional");
//		lstRefactoring.add("Pre Return");
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 6;
		lstRefactoring.setLayoutData(gd);

		wizard.fillCells(composite, 1, 1);
		
		new Label(composite, SWT.NONE).setText("Description");
		
		txtDescription = new Text(composite,  SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 3;
		txtDescription.setLayoutData(gd);
		
		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		lstRefactoring.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.ERROR, "not_used", 0, "Select a Reorganization Method", null);

	    if ( event.widget == lstRefactoring && lstRefactoring.getSelectionIndex() != -1 ) {
	    	if(lstRefactoring.getSelectionIndex() == 0)
	    		txtDescription.setText("The block of code is at the beginning of the body of the enclosing method.");
	    	else if(lstRefactoring.getSelectionIndex() == 1)
	    		txtDescription.setText("The block of code is always before another call.");
	    	else if(lstRefactoring.getSelectionIndex() == 2)
	    		txtDescription.setText("The block of code is always after another call.");
	    	else if(lstRefactoring.getSelectionIndex() == 3)
	    		txtDescription.setText("A conditional statement controls the execution of the block of code.");
	    	else if(lstRefactoring.getSelectionIndex() == 4)
	    		txtDescription.setText("The block of code is just before the return statement.");
		    status = new Status(IStatus.OK, "not_used", 0, "Click Next to Proceed", null);
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
		String sSelection = lstRefactoring.getItem(lstRefactoring.getSelectionIndex());
		int refactoring;
		if( sSelection.equals("Extract Beginning") )
			refactoring = TreeObject.REFACTOR_EXTRACT_BEGINNIG;
		else if( sSelection.equals("Extract End") )
			refactoring = TreeObject.REFACTOR_EXTRACT_END;
		else if( sSelection.equals("Extract Before Call") )
			refactoring = TreeObject.REFACTOR_EXTRACT_BEFORE_CALL;
		else if( sSelection.equals("Extract After Call") )
			refactoring = TreeObject.REFACTOR_EXTRACT_AFTER_CALL;
		else if( sSelection.equals("Extract Conditional") )
			refactoring = TreeObject.REFACTOR_EXTRACT_CONDICIONAL;
		else if( sSelection.equals("Pre Return") )
			refactoring = TreeObject.REFACTOR_PRE_RETURN;
		else refactoring = 0;
			
		wizard.setRefactoring(refactoring);
		wizard.aspectPage.onEnterPage();
		return wizard.aspectPage;
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		applyToStatusLine(new Status(IStatus.ERROR, "not_used", 0, "Select a reorganization method", null));
		// Choose available options for refactoring according to the selected method
		MethodInfo methodInfo = wizard.getSelectedMethod();
		lstRefactoring.removeAll();
		
		// Extract Beginning is always applied
		lstRefactoring.add("Extract Beginning");

		// Is Extract End applied?
		if( methodInfo.getReturnType().equals("void") || (methodInfo.getReturnType().length() == 0))
			lstRefactoring.add("Extract End");

//		lstRefactoring.add("Extract Before Call");
//		lstRefactoring.add("Extract After Call");
//		lstRefactoring.add("Extract Conditional");
//		lstRefactoring.add("Pre Return");
	}
}