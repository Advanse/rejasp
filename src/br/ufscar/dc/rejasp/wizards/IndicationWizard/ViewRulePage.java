package br.ufscar.dc.rejasp.wizards.IndicationWizard;

import java.util.ArrayList;

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

import br.ufscar.dc.rejasp.indication.model.MatchText;

public class ViewRulePage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;
	/**
	 * Label of rule
	 */
	private Text txtRule;
	/**
	 * List of words of the rule
	 */
	private List lstWords;
	
	public ViewRulePage() {
		super("Viewing rule page");
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
		int ncol = 1;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);

		// Line 1 - 2
		txtRule = new Text(composite, SWT.READ_ONLY | SWT.WRAP );
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		txtRule.setLayoutData(gd);

		// Line 3 - Empty space
		fillCells(composite, 1, 1);
		
		// Line 4
		new Label(composite, SWT.NONE).setText("Words used by the rule");

		// Line 5
		lstWords = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		lstWords.setItems(new String []{"","","","",""});
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 3;
		lstWords.setLayoutData(gd);
		setControl(composite);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {}
	
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
	 * @see IWizardPage#canFlipToNextPage()
	 * Could I procced to next page?
	 */
	public boolean canFlipToNextPage() {
		return false;
	}

	public IWizardPage getNextPage() {
		return null;
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		setTitle("Viewing rule");
		setDescription("Click Back to return to management of rules page");
		wizard.canFinish = false;
		
		// Insert data in interface
		String sRule = "The rule is applied to a ";
		if ( wizard.currentRule.getTarget().equals(MatchText.STRING_LITERAL))
			sRule += "string literal that ";
		else
			sRule += "variable name that ";
		if ( wizard.currentRule.getRule().equals(MatchText.CONTAINS) )
			sRule += "contains ";
		else if ( wizard.currentRule.getRule().equals(MatchText.STARTS_WITH) )
			sRule += "starts with ";
		else
			sRule += "ends with ";
		sRule += " one of the words of the list below. Uppercase letters ";
		if ( ! wizard.currentRule.isCaseSensity() )
			sRule += "doesn't ";
		sRule += "differ from lowercase letters";
		txtRule.setText(sRule);
		
		// Fill list of words 
		lstWords.removeAll();
		ArrayList words = wizard.currentRule.getWords();
		for ( int i = 0; i < words.size(); i++ )
			lstWords.add((String)words.get(i));
	}
}
