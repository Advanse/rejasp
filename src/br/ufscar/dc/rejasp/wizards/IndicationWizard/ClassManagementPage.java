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

import br.ufscar.dc.rejasp.indication.model.IndicationClass;

/**
 * @author Daniel Kawakami
 * This is a page of indication wizard. It's in charge of create, update or view
 * the classes of a package.
 */
public class ClassManagementPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;
	/**
	 * Name field of the class
	 */
	private Text txtName;
	/**
	 * Description field of the class
	 */
	private Text txtDescription;
	/**
	 * List of registered classes of the package
	 */
	private List lstClasses;
	/**
	 * Radio button used to clean fields and prepare to create a new class
	 */
	private Button btnNew;
	/**
	 * Radio button used to create or update an class
	 */
	private Button btnInsert;
	/**
	 * Radio button used to remove an existing class
	 */
	private Button btnRemove;
	
	// Constants used for the control of this page
	private final String sAdd = "&Add>>"; 
	private final String sUpdate = "&Update>>"; 
	
	public ClassManagementPage() {
		super("Class Management Page");
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
		
		Label lbclass = new Label(composite, SWT.NONE);
		lbclass.setText("Available Classes");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		lbclass.setLayoutData(gd);
		
		// Line 2
		txtName = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalSpan = 2;
		gd.horizontalSpan = 2;
		txtName.setLayoutData(gd);
		
		lstClasses = new List (composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL | 
						  GridData.FILL_VERTICAL);
		gd.verticalSpan = 9;
		gd.horizontalSpan = 2;
		lstClasses.setItems(new String []{"","","","","","","","",""});
		lstClasses.setLayoutData( gd );
		
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
		lstClasses.addListener(SWT.Selection, this);
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
		if( event.widget == lstClasses ) {
			btnInsert.setText(sUpdate);
			IndicationClass IndicationClass = findClass();
			if ( IndicationClass == null ) {
				System.err.println("Can't find class.");
				return;
			}
			txtName.setText(IndicationClass.getName());
			txtDescription.setText(IndicationClass.getDescription());
			btnInsert.setEnabled(true);
			btnRemove.setEnabled(true);
    		status = new Status(IStatus.OK, "not_used", 0, 
    				"Change name and description. Later, click Update to commit the changes." + 
    				" Click Remove if you wish remove class " + 
    				lstClasses.getItem(lstClasses.getSelectionIndex()), null);
		}
		// Was new button clicked?
		else if( event.widget == btnNew ) {
			resetFields();
		}
		// Was insert button clicked?
		else if( event.widget == btnInsert) {
			if ( btnInsert.getText().equals(sAdd) ) {
				// Adding in model
				IndicationClass element = new IndicationClass(txtName.getText(), txtDescription.getText()); 
				if (! wizard.currentPackage.addClass(element))
					System.err.println("Class not added to package.");
				
				// Adding in class
				lstClasses.add(txtName.getText());
				MessageDialog.openInformation(wizard.workbench.getActiveWorkbenchWindow().getShell(), 
						"class was added", "class " + txtName.getText() + " was added successfully.");
				resetFields();
			}
			else if ( btnInsert.getText().equals(sUpdate) ) {
				int nIndex = lstClasses.getSelectionIndex();
				String sClass = lstClasses.getItem(nIndex);
				// Updating in model
				if ( ! wizard.currentPackage.updateClass(sClass, txtName.getText(), txtDescription.getText()) ) {
					System.err.println("Can't find class.");
					return;
				}
				// Updating in list
				lstClasses.remove(nIndex);
				lstClasses.add(txtName.getText(), nIndex);
				MessageDialog.openInformation(wizard.workbench.getActiveWorkbenchWindow().getShell(), 
						"Class was updated", "Class " + txtName.getText() + " was updated successfully.");
				resetFields();
			}
		}
		// Was remove button clicked?
		else if ( event.widget == btnRemove ) {
			// Removing in model
			IndicationClass IndicationClass = removeClass();
			if ( IndicationClass == null ) {
				System.err.println("Can't find class.");
				return;
			}
			// Removing in list
			lstClasses.remove(lstClasses.getSelectionIndex());
			txtName.setText(IndicationClass.getName());
			txtDescription.setText(IndicationClass.getDescription());
			MessageDialog.openInformation(wizard.workbench.getActiveWorkbenchWindow().getShell(), 
					"Class was removed", "Class " + txtName.getText() + " was removed successfully.");
			lstClasses.deselectAll();
			btnInsert.setText(sAdd);
			btnInsert.setEnabled(true);
			btnRemove.setEnabled(false);
		}
		// Did any text field change?
		else if ( event.widget == txtName || event.widget == txtDescription ) {
	    	btnInsert.setEnabled(true);
		    if ( txtName.getText().length() == 0 ) {
		    	status = new Status(IStatus.ERROR, "not_used", 0, 
		    			"Enter class name", null);
		    	btnInsert.setEnabled(false);
		    }
		    else if ( txtDescription.getText().length() == 0 )
	    		status = new Status(IStatus.WARNING, "not_used", 0, 
	    				"It's recommended enter a description of the class", null);
		    else if ( btnInsert.getText().equals(sAdd) ) {
	    		status = new Status(IStatus.OK, "not_used", 0, 
	    				"Click Add to include the class " + txtName.getText() + 
	    				" in the list", null);
		    }
		    else if ( btnInsert.getText().equals(sUpdate) )
	    		status = new Status(IStatus.OK, "not_used", 0, 
	    				"Click Update to change the class " + 
	    				lstClasses.getItem(lstClasses.getSelectionIndex()) + 
	    				" in the list", null);
		    IndicationClass obj = (txtName.getText().length() == 0)?
		    		null:wizard.currentPackage.findClass(txtName.getText());
		    if ( obj != null )
		    	if ( btnInsert.getText().equals(sAdd) || btnInsert.getText().equals(sUpdate) && 
		    		 !txtName.getText().equals(lstClasses.getItem(lstClasses.getSelectionIndex())) ) {
		    		status = new Status(IStatus.ERROR, "not_used", 0, 
		    				"The class " + txtName.getText() + 
		    				" already exists. Choose another name to the class", null);
			    	btnInsert.setEnabled(false);
		    	}
		}
		// Update class
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
		ExceptionManagementPage exceptionPackage = wizard.exceptionPage;
		exceptionPackage.onEnterPage();
		return exceptionPackage;
	}

	/**
	 * Before show this page, the class can be set.
	 */
	public void onEnterPage() {
		String sMessage = "Insert, update or remove classes from package " + wizard.currentPackage.getName();
		setTitle(sMessage);
		wizard.canFinish = false;
		setMessage(sMessage);

		// Reset Interface
		resetFields();

		// Insert data in class
		lstClasses.removeAll();
		ArrayList classes = wizard.currentPackage.getClasses();
		for(int i = 0; i < classes.size(); i++)
			lstClasses.add(((IndicationClass)classes.get(i)).getName());
	}
	
	/**
	 * Find the class in model that was selected in the list of classes
	 * @return desired class or null if no class was found 
	 */
	public IndicationClass findClass() {
		if( lstClasses.getSelectionIndex() == -1 )
			return null;
		return wizard.currentPackage.findClass(lstClasses.getItem(lstClasses.getSelectionIndex()));
	}

	/**
	 * Remove the class from the model that was selected in the list of indications. 
	 * @return the removed class
	 */
	public IndicationClass removeClass() {
		if( lstClasses.getSelectionIndex() == -1 )
			return null;
		String sClass = lstClasses.getItem(lstClasses.getSelectionIndex());
		return wizard.currentPackage.removeClass(sClass);
	}
	
	/**
	 * Reset and clear some fields of class 
	 */
	public void resetFields() {
		setDescription("Enter fields and click Add if you want add an class or select a class");
		txtName.setText("");
		txtDescription.setText("");
		lstClasses.deselectAll();
		btnInsert.setText(sAdd);
		btnInsert.setEnabled(false);
		btnRemove.setEnabled(false);
	}
}