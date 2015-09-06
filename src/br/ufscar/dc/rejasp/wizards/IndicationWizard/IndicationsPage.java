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

import br.ufscar.dc.rejasp.indication.model.Indication;
import br.ufscar.dc.rejasp.indication.model.IndicationPackage;


/**
 * @author Daniel Kawakami
 * This is a page of indication wizard. It's in charge of create, update or view
 * indications.
 */
public class IndicationsPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;
	/**
	 * Radio button to creating indication 
	 */
	private Button createIndication;
	/**
	 * Radio button for updating indication
	 */
	private Button updateIndication;
	/**
	 * Radio button for removing indication
	 */
	private Button removeIndication;
	/**
	 * List component for showing existing indication
	 */
	private List lstIndications;
	/**
	 * Name field used to enter an indication name 
	 */
	private Text txtName;
	/**
	 * Description field used to enter indication description
	 */
	private Text txtDescription;
	/**
	 * List component used to view existing packages in a indication
	 */
	private List packages;
	
	/**
	 * Is one or more indications changed?
	 */
	private boolean bChanged;

	public IndicationsPage() {
		super("Main Page of all indications");
		setTitle("Manage Indications");
		setDescription("Please, select one option below and press Next button or Finish button to exit.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (IndicationWizard)getWizard();
		ArrayList indications = wizard.lstIndications;

		// create the composite to hold the widgets
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);

	    // create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 3;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);
		
		// Line 1
		fillCells(composite, 1, 1);
		new Label (composite, SWT.NONE).setText("Indication Name");
		fillCells(composite, 1, 1);
		
		// Line 2
		createIndication = new Button(composite, SWT.RADIO);
		createIndication.setText("Define an indication");
		createIndication.setSelection(true);
		
		txtName = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		txtName.setLayoutData(gd);
		txtName.setEnabled(false);
		
		// Line 3
		updateIndication = new Button(composite, SWT.RADIO);
		updateIndication.setText("Update an existing indication");
		updateIndication.setSelection(false);

		new Label (composite, SWT.NONE).setText("Description");

		fillCells(composite, 1, 1);
		
		// Line 4
		removeIndication = new Button(composite, SWT.RADIO);
		removeIndication.setText("Remove an existing indication");
		removeIndication.setSelection(false);

		txtDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gd.verticalSpan = 3;
		gd.horizontalSpan = 2;
		txtDescription.setLayoutData(gd);
		txtDescription.setEnabled(false);

		// Line 5
		fillCells(composite, 1, 1);
		
		// Line 6
		new Label (composite, SWT.NONE).setText("Available Indications");

		// Line 7
		lstIndications = new List (composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL );
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.verticalSpan = 4;
		
		lstIndications.getVerticalBar().setEnabled(true);
		lstIndications.getVerticalBar().setVisible(true);

		for( int i = 0; i < indications.size(); i++ )
			lstIndications.add(((Indication)indications.get(i)).getName());
		
		lstIndications.setLayoutData( gd );

		new Label(composite, SWT.NONE).setText("Associated Packages");
		
		fillCells(composite, 1, 1);

		// Line 8
		packages = new List (composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL );
		gd.verticalSpan = 3;
		gd.horizontalSpan = 2;
		packages.setItems(new String [] {});
		packages.setLayoutData( gd );
		
	    // set the composite as the control for this page
		setControl(composite);
		addListeners();
	}
	
	private void addListeners() {
		createIndication.addListener(SWT.FocusIn, this);
		updateIndication.addListener(SWT.FocusIn, this);
		removeIndication.addListener(SWT.FocusIn, this);
		lstIndications.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);
	    // Was indication creation selected?
		if ( event.widget == createIndication ) {
			clearIndicationField();
			setDescription("Click Next to start to create a new indication");
		}
		// Was indication updating selected?
		else if ( event.widget == updateIndication ) {
			if ( lstIndications.getSelectionCount() == 0 ) {
	            status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Select an indication you want to update", null);        
			}
			else {
				setDescription("Click Next to update " + 
						lstIndications.getItem(lstIndications.getSelectionIndex()));
			}
		}
		// Was indication removing selected?
		else if ( event.widget == removeIndication ) {
			if ( lstIndications.getSelectionCount() == 0 ) {
	            status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Select an indication you want to remove", null);        
			}
			else {
				setDescription("Click Next to remove " + 
						lstIndications.getItem(lstIndications.getSelectionIndex()));
			}
		}
		// Have an element in the list been selected?
		else if ( event.widget == lstIndications ) {
			// Find the indication on model
			Indication indication = wizard.findIndication(lstIndications.getItem(lstIndications.getSelectionIndex()));
			if ( indication == null )
	            status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Can't find indication in model. Restart wizard.", null);
			else {
				txtName.setText(indication.getName());
				txtName.setEnabled(true);
				txtDescription.setText(indication.getDescription());
				txtDescription.setEnabled(true);
				packages.removeAll();
				ArrayList lstLibraries = indication.getPackages();
				IndicationPackage library;
				for ( int i = 0; i < lstLibraries.size(); i++ ) {
					library = (IndicationPackage)lstLibraries.get(i);
					packages.add(library.getName());
				}
				if ( updateIndication.getSelection() )
					setDescription("Click Next to update " + 
							lstIndications.getItem(lstIndications.getSelectionIndex()));
				else if ( removeIndication.getSelection() )
					setDescription("Click Next to remove " + 
							lstIndications.getItem(lstIndications.getSelectionIndex()));
			}
		}
		// Updating interface
		applyToStatusLine(status);
		wizard.getContainer().updateButtons();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
	 * It makes all the setting before go to the next page
	 */
	public IWizardPage getNextPage() {
		if ( removeIndication.getSelection() ) {
			String sSelectedIndication = lstIndications.getItem(lstIndications.getSelectionIndex());
			if(MessageDialog.openQuestion(wizard.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Remove Indication", "Do you really want to remove " + 
					 sSelectedIndication + "?")) {
				RemoveIndicationPage removePage = wizard.removePage;
				
				// Something wrong happened
				if ( wizard.removeIndication(sSelectedIndication) == null ) {
		            Status status = new Status(IStatus.ERROR, "not_used", 0, 
			                "Can't find and remove indication in model. Restart wizard.", null);
		    		applyToStatusLine(status);
		    		wizard.getContainer().updateButtons();
		    		return null;
				}
				
				// Updating list of indications in interface
				lstIndications.remove(lstIndications.getSelectionIndex());
				// Clear some fields
				clearIndicationField();				
				return removePage;
			}
			else return null;
		}
		IndicationHeadPage headPage = ((IndicationWizard)getWizard()).headPage;

		if (createIndication.getSelection()) {
			wizard.newIndication = true;
			wizard.currentIndication = new Indication("");
			headPage.onEnterPage();
		}
		else {
			Indication indication = wizard.findIndication(lstIndications.getItem(lstIndications.getSelectionIndex()));
			// Something wrong happened 
			if (indication == null) {
	            Status status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Can't find and remove indication in model. Restart wizard.", null);
	    		applyToStatusLine(status);
	    		wizard.getContainer().updateButtons();
	    		return null;
			}
			wizard.currentIndication = indication;
			wizard.newIndication = false;
			headPage.onEnterPage();
		}
		return headPage;
	}
	
	/**
	 * Create an empty space interface 
	 * @param composite reference to composite component
	 * @param nHorizontalSpan number of horizontal cells that the empty space will fill 
	 * @param nVerticalSpan number of vertical cells that the empty space will fill
	 */
	private void fillCells(Composite composite, int nHorizontalSpan, int nVerticalSpan) {
		Label lbEmpty = new Label(composite, SWT.NONE );
		lbEmpty.setText("");
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = nHorizontalSpan;
		gd.verticalSpan = nVerticalSpan;
		lbEmpty.setLayoutData(gd);
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
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		wizard.canFinish = true;
		createIndication.setSelection(true);
		updateIndication.setSelection(false);
		removeIndication.setSelection(false);
		clearIndicationField();
		lstIndications.removeAll();

		ArrayList indications = wizard.lstIndications;
		for( int i = 0; i < indications.size(); i++ )
			lstIndications.add(((Indication)indications.get(i)).getName());
	}
	
	/**
	 * Reset some fields of the interface
	 */
	public void clearIndicationField() {
		lstIndications.deselectAll();
		txtName.setText("");
		txtName.setEnabled(false);
		txtDescription.setText("");
		txtDescription.setEnabled(false);
		packages.removeAll();
	}
	
	public void setChanged(boolean bChanged) {
		this.bChanged = bChanged;
	}
	
	public boolean isChanged() {
		return bChanged;
	}
}

