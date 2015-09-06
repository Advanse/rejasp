package br.ufscar.dc.rejasp.wizards.IndicationWizard;

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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import br.ufscar.dc.rejasp.indication.model.IndicationPackage;

/**
 * @author Daniel Kawakami
 * This is a page of indication wizard. It's in charge of create, update or view
 * the name and description of a package.
 */
public class PackageHeadPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;
	/**
	 * Name field of package
	 */
	private Text txtName;
	/**
	 * Description field of description
	 */
	private Text txtDescription;
	
	public PackageHeadPage() {
		super("Package Head Information");
		setTitle("Package name");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (IndicationWizard)getWizard();

		// create the composite to hold the widgets
		int nIdentation = 10;
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);

	    // create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);
		
		Label lbName = new Label(composite, SWT.NONE);
		lbName.setText("Name:");
		txtName = new Text(composite, SWT.BORDER );
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = nIdentation;
		txtName.setLayoutData(gd);
		
		new Label(composite, SWT.NONE);
		
		Label lbDescription = new Label(composite, SWT.NONE);
		lbDescription.setText("Description:");
		txtDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		gd = new GridData ( GridData.FILL_BOTH );
		gd.horizontalIndent = nIdentation;
		txtDescription.setLayoutData(gd);

		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		txtName.addListener(SWT.KeyUp, this);
		txtDescription.addListener(SWT.KeyUp, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
		IndicationPackage libIndication = wizard.currentPackage;
		
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);
		if ( txtName.getText().length() == 0 )
	    	status = new Status(IStatus.ERROR, "not_used", 0, 
	    			"Enter a package name or sumary", null);
    	else if( txtDescription.getText().length() == 0 )
    		status = new Status(IStatus.WARNING, "not_used", 0, 
    				"It's recommended enter an indication description", null);
    	else
    		status = new Status(IStatus.OK, "not_used", 0, 
    				"Click Next to proceed", null);
	    if(wizard.currentIndication.findPackage(txtName.getText()) != null) {
	    	if ( wizard.newPackage || 
	    	   (( libIndication != null) && ! txtName.getText().equals(libIndication.getName())))
	    		status = new Status(IStatus.ERROR, "not_used", 0, 
	    				"The indication " + txtName.getText() + 
	    				" already exists. Choose another name or sumary", null);
	    }
	    // Update interface
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
		if (txtName.getText().length() == 0)
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
	 * It makes all the setting before go to the next page
	 */
	public IWizardPage getNextPage() {
		if (txtName.getText().length() == 0)
			return null;
		// Insert data from view to model
		wizard.currentPackage.setName(txtName.getText());
		wizard.currentPackage.setDescription(txtDescription.getText());
		// Next Page
		InterfaceManagementPage interfacePage = wizard.interfacePage;
		interfacePage.onEnterPage();
		return interfacePage;
	}

	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		if ( wizard.newPackage )
			setDescription("Enter a package name");
		else
			setDescription("You may update name and description. Click Next to proceed");

		txtName.setText(wizard.currentPackage.getName());
		txtDescription.setText(wizard.currentPackage.getDescription());

		applyToStatusLine(new Status(IStatus.OK, "not_used", 0, "", null));
		wizard.getContainer().updateButtons();
	}

}
