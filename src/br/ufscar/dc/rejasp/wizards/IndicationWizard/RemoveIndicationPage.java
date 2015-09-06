package br.ufscar.dc.rejasp.wizards.IndicationWizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Daniel Kawakami 
 * This is a page of indication wizard. It's in charge of prompt the user that 
 * indication was removed.
 */
public class RemoveIndicationPage extends WizardPage implements Listener {
	public RemoveIndicationPage() {
		super("Remove Indication Page");
		setTitle("Indication Removed");
		setDescription("The selected indication was removed. Click on Back button to return to indication management main page.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		// widges declarations

		// create the composite to hold the widgets
		Composite composite =  new Composite(parent, SWT.NULL);

	    // create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 3;
		gl.numColumns = ncol;
		composite.setLayout(gl);
		
		setControl(composite);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {}

	/**
	 * @see IWizardPage#canFlipToNextPage()
	 * Could I procced to next page?
	 */
	public boolean canFlipToNextPage() {
		return false;
	}

}
