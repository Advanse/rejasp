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

import br.ufscar.dc.rejasp.indication.model.Indication;

/**
 * @author Daniel Kawakami
 * This is a page of indication wizard. It's in charge of create, update or view
 * that name and description of an indication.
 */
public class IndicationHeadPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;
	/**
	 * Reference to the current indication
	 */
	private Indication indication;
	/**
	 * Field to enter the indication name
	 */
	private Text txtName;
	/**
	 * Field top enter the indication description
	 */
	private Text txtDescription;

	public IndicationHeadPage() {
		super("Page of Name and description of a indication");
		setTitle("Indication name");
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
		
		// Name fields
		Label lbName = new Label(composite, SWT.NONE);
		lbName.setText("Name:");
		txtName = new Text(composite, SWT.BORDER );
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = nIdentation;
		txtName.setLayoutData(gd);
		
		// Empty space
		new Label(composite, SWT.NONE);
		
		// Description fields
		Label lbDescription = new Label(composite, SWT.NONE);
		lbDescription.setText("Description:");
		txtDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		gd = new GridData ( GridData.FILL_BOTH );
		gd.horizontalIndent = nIdentation;
		txtDescription.setLayoutData(gd);
		
		setControl(composite);
		addListeners();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);
	    // Is name field empty? 
	    if(txtName.getText().length() == 0)
	    	status = new Status(IStatus.ERROR, "not_used", 0, 
	    			"Enter an indication name or sumary", null);
	    // Is description empty?
    	else if( txtDescription.getText().length() == 0 )
    		status = new Status(IStatus.WARNING, "not_used", 0, 
    				"It's recommended enter an indication description", null);
	    // Everything is alright (I guess)
    	else
    		status = new Status(IStatus.OK, "not_used", 0, 
    				"Click Next to proceed", null);
	    // Is the indication name duplicated?
	    if(wizard.findIndication(txtName.getText()) != null) {
	    	if ( wizard.newIndication || 
	    	   ((indication != null) && ! txtName.getText().equals(indication.getName())))
	    		status = new Status(IStatus.ERROR, "not_used", 0, 
	    				"The indication " + txtName.getText() + 
	    				" already exists. Choose another name or sumary", null);
	    }
	    // Update interface
		applyToStatusLine(status);
		wizard.getContainer().updateButtons();
		wizard.canFinish = false;
	}

	private void addListeners() {
		txtName.addListener(SWT.KeyUp, this);
		txtDescription.addListener(SWT.KeyUp, this);
	}

	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		this.indication = wizard.currentIndication;

		// Disable Finish
		wizard.canFinish = false;
		
		if ( ! wizard.newIndication ) {
			txtName.setText(indication.getName());
			txtDescription.setText(indication.getDescription());			
			setDescription("You may update name and description. Click Next to proceed");
		}
		else {
			txtName.setText("");
			txtDescription.setText("");
			setDescription("Enter a indication name");
		}
		applyToStatusLine(new Status(IStatus.OK, "not_used", 0, "", null));
		wizard.getContainer().updateButtons();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
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
	 * @see org.eclipse.jface.wizard.IWizardPage#getPreviousPage()
	 * Choose the page that wil be shpwed when Back button is clicked
	 */
	public IWizardPage getPreviousPage() {
		// Enable Finish
		wizard.canFinish = true;
		return wizard.indicationsPage;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
	 * It makes all the setting before go to the next page
	 */
	public IWizardPage getNextPage() {
		indication.setName(txtName.getText());
		indication.setDescription(txtDescription.getText());
		if(wizard.newIndication)
			wizard.mainPage.setChanged(true);
		else
			wizard.mainPage.setChanged(false);
		wizard.mainPage.onEnterPage();
		return wizard.mainPage;
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
}
