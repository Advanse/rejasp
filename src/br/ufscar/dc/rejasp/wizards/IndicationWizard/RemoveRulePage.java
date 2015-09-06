package br.ufscar.dc.rejasp.wizards.IndicationWizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class RemoveRulePage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;

	public RemoveRulePage() {
		super("Remove Rule Page");
		setTitle("Rule removed");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (IndicationWizard)getWizard();
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
	public void handleEvent(Event event) {
	}
	
	/**
	 * @see IWizardPage#canFlipToNextPage()
	 * Could I procced to next page?
	 */
	public boolean canFlipToNextPage() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getPreviousPage()
	 * Choose the page that wil be showed when Back button is clicked
	 */
	public IWizardPage getPreviousPage() {
		wizard.ruleMainPage.onEnterPage();
		return wizard.ruleMainPage;
	}
	
	/**
	 * Before show this page, the interface can be set.
	 * @param sPackage
	 */
	public void onEnterPage( String sRule ) {
		setDescription("The rule <" + sRule + "> was removed. Click on Back button to return to Management of Rules page");
	}
}
