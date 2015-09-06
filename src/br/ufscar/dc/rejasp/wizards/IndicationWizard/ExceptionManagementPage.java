package br.ufscar.dc.rejasp.wizards.IndicationWizard;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import br.ufscar.dc.rejasp.indication.model.IndicationException;

/**
 * @author Daniel Kawakami
 * This is a page of indication wizard. It's in charge of create, update or view
 * the exceptions of a package.
 */
public class ExceptionManagementPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;
	/**
	 * Name field of the exception
	 */
	private Text txtName;
	/**
	 * Description field of the exception
	 */
	private Text txtDescription;
	/**
	 * List of registered exceptions of the package
	 */
	private List lstExceptions;
	/**
	 * Radio button used to clean fields and prepare to create a new exception
	 */
	private Button btnNew;
	/**
	 * Radio button used to create or update an exception
	 */
	private Button btnInsert;
	/**
	 * Radio button used to remove an existing exception
	 */
	private Button btnRemove;
	
	/**
	 * Internal flag to know if the package have been added
	 */
	
	// Constants used for the control of this page
	private final String sAdd = "&Add>>"; 
	private final String sUpdate = "&Update>>"; 
	
	public ExceptionManagementPage() {
		super("Exception Management Page");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (IndicationWizard)getWizard();
		// create the composite to hold the widgets
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);

	    // create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 5;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		gl.horizontalSpacing = 15;
		composite.setLayout(gl);
		
		// Line 1
		Label lbName = new Label(composite, SWT.NONE);
		lbName.setText("Name");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		lbName.setLayoutData(gd);
		
		Label lbEmpty = new Label(composite, SWT.NONE);
		lbEmpty.setText("");
		gd = new GridData();
		gd.verticalSpan = 4;
		lbEmpty.setLayoutData(gd);
		
		Label lbexception = new Label(composite, SWT.NONE);
		lbexception.setText("Available Exceptions");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		lbexception.setLayoutData(gd);
		
		// Line 2
		txtName = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalSpan = 2;
		gd.horizontalSpan = 2;
		txtName.setLayoutData(gd);
		
		lstExceptions = new List (composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL | 
						  GridData.FILL_VERTICAL);
		gd.verticalSpan = 9;
		gd.horizontalSpan = 2;
		lstExceptions.setItems(new String []{"","","","","","","","",""});
		lstExceptions.setLayoutData( gd );
		
		// Line 4
		Label lbDescription = new Label(composite, SWT.NONE);
		lbDescription.setText("Description");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		lbDescription.setLayoutData(gd);
		
		// Line 5 - 6
		txtDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL | 
						  GridData.FILL_VERTICAL);
		gd.verticalSpan = 5;
		gd.horizontalSpan = 2;
		txtDescription.setLayoutData(gd);

		createBlankSpace(composite);
		
		btnNew = new Button(composite, SWT.PUSH );
		btnNew.setText("&New");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL );
		btnNew.setLayoutData(gd);

		// Line 7
		btnInsert = new Button(composite, SWT.PUSH );
		btnInsert.setText(sAdd);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL );
		btnInsert.setLayoutData(gd);
		
		// Line 8
		btnRemove = new Button(composite, SWT.PUSH );
		btnRemove.setText("<<&Remove");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL );
		btnRemove.setLayoutData(gd);
		
		setControl(composite);
		addListeners();
	}

	/**
	 * Creates an empty space
	 * @param composite reference to composite component
	 */
	private void createBlankSpace(Composite composite) {
		Label lbEmpty = new Label(composite, SWT.NONE );
		lbEmpty.setText("");
		lbEmpty.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL ));
	}
	
	private void addListeners() {
		txtName.addListener(SWT.KeyUp, this);
		txtDescription.addListener(SWT.KeyUp, this);
		lstExceptions.addListener(SWT.Selection, this);
		btnNew.addListener(SWT.MouseUp, this);
		btnInsert.addListener(SWT.MouseUp, this);
		btnRemove.addListener(SWT.MouseUp, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);
	    // Was an element of list selected?
		if( event.widget == lstExceptions ) {
			btnInsert.setText(sUpdate);
			IndicationException IndicationException = findException();
			if ( IndicationException == null ) {
				System.err.println("Can't find exception.");
				return;
			}
			txtName.setText(IndicationException.getName());
			txtDescription.setText(IndicationException.getDescription());
			btnInsert.setEnabled(true);
			btnRemove.setEnabled(true);
    		status = new Status(IStatus.OK, "not_used", 0, 
    				"Change name and description. Later, click Update to commit the changes." + 
    				" Click Remove if you wish remove exception " + 
    				lstExceptions.getItem(lstExceptions.getSelectionIndex()), null);
		}
		// Was new button clicked?
		else if( event.widget == btnNew ) {
			resetFields();
		}
		// Was insert button clicked?
		else if( event.widget == btnInsert) {
			if ( btnInsert.getText().equals(sAdd) ) {
				// Adding in model
				IndicationException element = new IndicationException(txtName.getText(), txtDescription.getText()); 
				if (! wizard.currentPackage.addException(element))
					System.err.println("Exception not added to package.");
				
				// Adding in exception
				lstExceptions.add(txtName.getText());
				MessageDialog.openInformation(wizard.workbench.getActiveWorkbenchWindow().getShell(), 
						"Exception was added", "Exception " + txtName.getText() + " was added successfully.");
				resetFields();
			}
			else if ( btnInsert.getText().equals(sUpdate) ) {
				int nIndex = lstExceptions.getSelectionIndex();
				String sException = lstExceptions.getItem(nIndex);
				// Updating in model
				if ( ! wizard.currentPackage.updateException(sException, txtName.getText(), txtDescription.getText()) ) {
					System.err.println("Can't find exception.");
					return;
				}
				// Updating in list
				lstExceptions.remove(nIndex);
				lstExceptions.add(txtName.getText(), nIndex);
				MessageDialog.openInformation(wizard.workbench.getActiveWorkbenchWindow().getShell(), 
						"Exception was updated", "Exception " + txtName.getText() + " was updated successfully.");
				resetFields();
			}
		}
		// Was remove button clicked?
		else if ( event.widget == btnRemove ) {
			// Removing in model
			IndicationException IndicationException = removeException();
			if ( IndicationException == null ) {
				System.err.println("Can't find exception.");
				return;
			}
			// Removing in list
			lstExceptions.remove(lstExceptions.getSelectionIndex());
			txtName.setText(IndicationException.getName());
			txtDescription.setText(IndicationException.getDescription());
			MessageDialog.openInformation(wizard.workbench.getActiveWorkbenchWindow().getShell(), 
					"Exception was removed", "Exception " + txtName.getText() + " was removed successfully.");
			lstExceptions.deselectAll();
			btnInsert.setText(sAdd);
			btnInsert.setEnabled(true);
			btnRemove.setEnabled(false);
		}
		// Did any text field change?
		else if ( event.widget == txtName || event.widget == txtDescription ) {
	    	btnInsert.setEnabled(true);
		    if ( txtName.getText().length() == 0 ) {
		    	status = new Status(IStatus.ERROR, "not_used", 0, 
		    			"Enter exception name", null);
		    	btnInsert.setEnabled(false);
		    }
		    else if ( txtDescription.getText().length() == 0 )
	    		status = new Status(IStatus.WARNING, "not_used", 0, 
	    				"It's recommended enter a description of the exception", null);
		    else if ( btnInsert.getText().equals(sAdd) ) {
	    		status = new Status(IStatus.OK, "not_used", 0, 
	    				"Click Add to include the exception " + txtName.getText() + 
	    				" in the list", null);
		    }
		    else if ( btnInsert.getText().equals(sUpdate) )
	    		status = new Status(IStatus.OK, "not_used", 0, 
	    				"Click Update to change the exception " + 
	    				lstExceptions.getItem(lstExceptions.getSelectionIndex()) + 
	    				" in the list", null);
		    IndicationException obj = wizard.currentPackage.findException(txtName.getText());
		    if ( obj != null )
		    	if ( btnInsert.getText().equals(sAdd) || btnInsert.getText().equals(sUpdate) && 
		    		 !txtName.getText().equals(lstExceptions.getItem(lstExceptions.getSelectionIndex())) ) {
		    		status = new Status(IStatus.ERROR, "not_used", 0, 
		    				"The exception " + txtName.getText() + 
		    				" already exists. Choose another name to the exception", null);
			    	btnInsert.setEnabled(false);
		    	}
		}
		// Update exception
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
			setErrorMessage(null);
			setMessage(message, WizardPage.ERROR);
		break;		
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
	 * It makes all the setting before go to the next page
	 */
	public IWizardPage getNextPage() {
		// Adding package into indication
		if(wizard.newPackage)
			wizard.currentIndication.addPackage(wizard.currentPackage);

		PackageMainPage mainPage = wizard.mainPage;
		mainPage.setChanged(true);
		mainPage.onEnterPage();
		return mainPage;
	}

	/**
	 * Before show this page, the exception can be set.
	 */
	public void onEnterPage() {
		String sMessage = "Insert, update or remove exceptions from package " + wizard.currentPackage.getName();
		setTitle(sMessage);
		wizard.canFinish = false;
		setMessage(sMessage);

		// Reset Interface
		resetFields();

		// Insert data in exception
		lstExceptions.removeAll();
		ArrayList exceptions = wizard.currentPackage.getExceptions();
		for(int i = 0; i < exceptions.size(); i++)
			lstExceptions.add(((IndicationException)exceptions.get(i)).getName());
	}
	
	/**
	 * Find the exception in model that was selected in the list of exceptions
	 * @return desired exception or null if no exception was found 
	 */
	public IndicationException findException() {
		if( lstExceptions.getSelectionIndex() == -1 )
			return null;
		return wizard.currentPackage.findException(lstExceptions.getItem(lstExceptions.getSelectionIndex()));
	}

	/**
	 * Remove the exception from the model that was selected in the list of indications. 
	 * @return the removed exception
	 */
	public IndicationException removeException() {
		if( lstExceptions.getSelectionIndex() == -1 )
			return null;
		String sException = lstExceptions.getItem(lstExceptions.getSelectionIndex());
		return wizard.currentPackage.removeException(sException);
	}
	
	/**
	 * Reset and clear some fields of exception 
	 */
	public void resetFields() {
		setDescription("Enter fields and click Add if you want add an exception or select a exception");
		txtName.setText("");
		txtDescription.setText("");
		lstExceptions.deselectAll();
		btnInsert.setText(sAdd);
		btnInsert.setEnabled(false);
		btnRemove.setEnabled(false);
	}
}