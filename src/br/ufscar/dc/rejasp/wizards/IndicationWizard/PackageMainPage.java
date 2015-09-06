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

import br.ufscar.dc.rejasp.indication.model.Indication;
import br.ufscar.dc.rejasp.indication.model.IndicationPackage;

/**
 * @author Daniel Kawakami
 * This is a page of indication wizard. It's in charge of create, update or view
 * only one indication
 */
public class PackageMainPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;
	/**
	 * Reference to current indication
	 */
	private Indication indication;
	/**
	 * Radio Button of creating a new package
	 */
	private Button createPackage;
	/**
	 * Radio Button of updating an existing package
	 */
	private Button updatePackage;
	/**
	 * Radio Button of removing an existing package
	 */
	private Button removePackage;
	/**
	 * Radio Button of viewing an existing package
	 */
	private Button viewPackage;
	/**
	 * Radio Button of concluding indication management
	 */
	private Button concludePackagePage;
	/**
	 * List that shows the existing packages of an indication
	 */
	private List lstPackages;
	
	/**
	 * Is the indication changed?
	 */
	private boolean bChanged;

	public PackageMainPage() {
		super("Management of only one indication");
		bChanged = false;
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
		int ncol = 2;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);
		
		// First Line
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE).setText("Available Packages");
		
		// Second Line
		createPackage = new Button(composite, SWT.RADIO);
		createPackage.setText("Create and bind a new package to the indication");
		createPackage.setSelection(true);

		lstPackages = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL | 
				  GridData.FILL_VERTICAL);
		gd.verticalSpan = 6;
		lstPackages.setItems(new String []{"","","","","","","","",""});
		lstPackages.setLayoutData( gd );

		// Third Line
		updatePackage = new Button(composite, SWT.RADIO);
		updatePackage.setText("Update an existing package");
		updatePackage.setSelection(false);

		// Forth Line
		removePackage = new Button(composite, SWT.RADIO);
		removePackage.setText("Remove all data related to a package");
		removePackage.setSelection(false);

		// Fifth Line
		viewPackage = new Button(composite, SWT.RADIO);
		viewPackage.setText("View package data");
		viewPackage.setSelection(false);
		
		// Sixth Line
		concludePackagePage = new Button(composite, SWT.RADIO);
		concludePackagePage.setText("Conclude package management");
		concludePackagePage.setSelection(false);

		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		createPackage.addListener(SWT.FocusIn, this);
		updatePackage.addListener(SWT.FocusIn, this);
		removePackage.addListener(SWT.FocusIn, this);
		viewPackage.addListener(SWT.FocusIn, this);
		concludePackagePage.addListener(SWT.FocusIn, this);
		lstPackages.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);
		if ( event.widget == createPackage ) {
			lstPackages.deselectAll();
			setDescription("Click Next to start to create a new package to " + indication.getName());
		}
		else if ( event.widget == updatePackage ) {
			if ( lstPackages.getSelectionCount() == 0 ) {
	            status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Select a package you want to update", null);        
			}
			else {
				setDescription("Click Next to update " + 
						lstPackages.getItem(lstPackages.getSelectionIndex()));
			}
		}
		else if ( event.widget == removePackage ) {
			if ( lstPackages.getSelectionCount() == 0 ) {
	            status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Select a package you want to remove", null);        
			}
			else {
				setDescription("Click Next to remove " + 
						lstPackages.getItem(lstPackages.getSelectionIndex()));
			}
		}
		else if (event.widget == viewPackage) {
			if ( lstPackages.getSelectionCount() == 0 ) {
	            status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Select a package you want to view", null);        
			}
			else {
				setDescription("Click Next to view " + 
						lstPackages.getItem(lstPackages.getSelectionIndex()));
			}
		}
		else if ( event.widget == concludePackagePage ) {
			setDescription("Click Next to end management of packages");
		}
		else if ( event.widget == lstPackages ) {
				if ( updatePackage.getSelection() )
					setDescription("Click Next to update " + 
							lstPackages.getItem(lstPackages.getSelectionIndex()));
				else if ( removePackage.getSelection() )
					setDescription("Click Next to remove " + 
							lstPackages.getItem(lstPackages.getSelectionIndex()));
				else if ( viewPackage.getSelection() )
					setDescription("Click Next to view " + 
							lstPackages.getItem(lstPackages.getSelectionIndex()));
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
		// Remove package option was chosen
		if ( removePackage.getSelection() ) {
			String sSelectedPackage = lstPackages.getItem(lstPackages.getSelectionIndex());
			if(MessageDialog.openQuestion(wizard.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Remove Package", "Do you really want to remove " + 
					 sSelectedPackage + "?")) {
				IndicationPackage removedPackage = wizard.currentIndication.removePackage(sSelectedPackage);
				// Something wrong happened
				if ( removedPackage == null ) {
		            Status status = new Status(IStatus.ERROR, "not_used", 0, 
			                "Can't find and remove package in model. Restart wizard.", null);
		    		applyToStatusLine(status);
		    		wizard.getContainer().updateButtons();
		    		return null;
				}

				RemovePackagePage removePackagePage = wizard.removePackagePage;
				removePackagePage.onEnterPage(removedPackage.getName());
				
				// Updating list of indications in interface
				lstPackages.remove(lstPackages.getSelectionIndex());
				return removePackagePage;
			}
			else return null;
			
		}
		// View package option was chosen
		else if ( viewPackage.getSelection() ) {
			wizard.currentPackage = wizard.currentIndication.findPackage(lstPackages.getItem(lstPackages.getSelectionIndex()));
			ViewPackagePage viewPage = wizard.viewPage;
			viewPage.onEnterPage();
			return viewPage;
		}
		// Conclude package option was chosen
		else if ( concludePackagePage.getSelection() ) {
//			if (wizard.newIndication)
//				wizard.lstIndications.add(indication);
//			IndicationsPage indicationsPage = wizard.indicationsPage;
//			indicationsPage.setChanged(true);
//			indicationsPage.onEnterPage();
//			return indicationsPage;
			RuleMainPage ruleMainPage = wizard.ruleMainPage;
			ruleMainPage.onEnterPage();
			return ruleMainPage;
		}
		// Create package option was chosen
		else if ( createPackage.getSelection() ){
			wizard.newPackage = true;
			wizard.currentPackage = new IndicationPackage("", "");
			PackageHeadPage headPage = wizard.packageHeadPage;
			headPage.onEnterPage();
			return headPage;
		}
		// Update package option was chosen
		else {	
			wizard.newPackage = false;
			wizard.currentPackage = wizard.currentIndication.findPackage(lstPackages.getItem(lstPackages.getSelectionIndex()));
			PackageHeadPage headPage = wizard.packageHeadPage;
			headPage.onEnterPage();
			return headPage;
		}
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		this.indication = wizard.currentIndication;
		setTitle("Management of " + indication.getName());
		setDescription("Choose one option below and click Next.");
		wizard.canFinish = false;
		ArrayList packages = indication.getPackages();
		
		// Reset radios
		createPackage.setSelection(true);
		updatePackage.setSelection(false);
		removePackage.setSelection(false);
		viewPackage.setSelection(false);
		concludePackagePage.setSelection(false);
		
		// Updating list
		lstPackages.removeAll();
		for (int i = 0; i < packages.size(); i++)
			lstPackages.add(((IndicationPackage)packages.get(i)).getName());
	}
	
	public void setChanged(boolean bChanged) {
		this.bChanged = bChanged;
	}

	public boolean getChanged() {
		return bChanged;
	}
}
