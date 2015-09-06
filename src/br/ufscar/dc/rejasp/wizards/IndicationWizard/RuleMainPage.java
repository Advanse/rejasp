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

import br.ufscar.dc.rejasp.indication.model.MatchText;

/**
 * @author Daniel Kawakami
 * This is a page of indication wizard. It's in charge of create, update or view
 * only one match text defined by user.
 */
public class RuleMainPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;
	/**
	 * Radio Button of creating a new match
	 */
	private Button createMatcher;
	/**
	 * Radio Button of updating an existing match
	 */
	private Button updateMatcher;
	/**
	 * Radio Button of removing an existing match
	 */
	private Button removeMatcher;
	/**
	 * Radio Button of viewing an existing match
	 */
	private Button viewMatcher;
	/**
	 * Radio Button of concluding indication management
	 */
	private Button concludeIndication;
	/**
	 * List that shows the existing matchers of an indication
	 */
	private List lstMatchers;
	
	/**
	 * Is the indication changed?
	 */
//	private boolean bChanged;

	public RuleMainPage() {
		super("Management of only one indication");
//		bChanged = false;
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
		new Label(composite, SWT.NONE).setText("Available Rules");
		
		// Second Line
		createMatcher = new Button(composite, SWT.RADIO);
		createMatcher.setText("Create a new rule to the match indication");

		lstMatchers = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 6;
		lstMatchers.setItems(new String []{"","","","","","","","",""});
		lstMatchers.setLayoutData( gd );

		// Third Line
		updateMatcher = new Button(composite, SWT.RADIO);
		updateMatcher.setText("Update an existing rule");

		// Forth Line
		removeMatcher = new Button(composite, SWT.RADIO);
		removeMatcher.setText("Remove an existing rule");

		// Fifth Line
		viewMatcher = new Button(composite, SWT.RADIO);
		viewMatcher.setText("View a rule");
		
		// Sixth Line
		concludeIndication = new Button(composite, SWT.RADIO);
		concludeIndication.setText("Conclude indication management");

		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		createMatcher.addListener(SWT.FocusIn, this);
		updateMatcher.addListener(SWT.FocusIn, this);
		removeMatcher.addListener(SWT.FocusIn, this);
		viewMatcher.addListener(SWT.FocusIn, this);
		concludeIndication.addListener(SWT.FocusIn, this);
		lstMatchers.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);
		if ( event.widget == createMatcher ) {
			lstMatchers.deselectAll();
			setDescription("Click Next to start to create a new rule to " + wizard.currentIndication.getName());
		}
		else if ( event.widget == updateMatcher ) {
			if ( lstMatchers.getSelectionCount() == 0 ) {
	            status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Select a rule you want to update", null);        
			}
			else {
				setDescription("Click Next to update " + 
						lstMatchers.getItem(lstMatchers.getSelectionIndex()));
			}
		}
		else if ( event.widget == removeMatcher ) {
			if ( lstMatchers.getSelectionCount() == 0 ) {
	            status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Select a rule you want to remove", null);        
			}
			else {
				setDescription("Click Next to remove " + 
						lstMatchers.getItem(lstMatchers.getSelectionIndex()));
			}
		}
		else if (event.widget == viewMatcher) {
			if ( lstMatchers.getSelectionCount() == 0 ) {
	            status = new Status(IStatus.ERROR, "not_used", 0, 
		                "Select a rule you want to view", null);        
			}
			else {
				setDescription("Click Next to view " + 
						lstMatchers.getItem(lstMatchers.getSelectionIndex()));
			}
		}
		else if ( event.widget == concludeIndication ) {
			setDescription("Click Next to end management of " + wizard.currentIndication.getName());
		}
		else if ( event.widget == lstMatchers ) {
				if ( updateMatcher.getSelection() )
					setDescription("Click Next to update " + 
							lstMatchers.getItem(lstMatchers.getSelectionIndex()));
				else if ( removeMatcher.getSelection() )
					setDescription("Click Next to remove " + 
							lstMatchers.getItem(lstMatchers.getSelectionIndex()));
				else if ( viewMatcher.getSelection() )
					setDescription("Click Next to view " + 
							lstMatchers.getItem(lstMatchers.getSelectionIndex()));
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
		// Remove match option was chosen
		if ( removeMatcher.getSelection() ) {
			String sSelectedMatcher = lstMatchers.getItem(lstMatchers.getSelectionIndex());
			if(MessageDialog.openQuestion(wizard.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Remove Matcher", "Do you really want to remove " + 
					 sSelectedMatcher + "?")) {
				MatchText removedMatcher = wizard.currentIndication.removeMatch(lstMatchers.getSelectionIndex());
				// Something wrong happened
				if ( removedMatcher == null ) {
		            Status status = new Status(IStatus.ERROR, "not_used", 0, 
			                "Can't find and remove match in model. Restart wizard.", null);
		    		applyToStatusLine(status);
		    		wizard.getContainer().updateButtons();
		    		return null;
				}

				RemoveRulePage removeRulePage = wizard.removeRulePage;
				removeRulePage.onEnterPage(sSelectedMatcher);
				
				// Updating list of indications in interface
				lstMatchers.remove(lstMatchers.getSelectionIndex());
				return removeRulePage;
			}
			else return null;
			
		}
		// View match option was chosen
		else if ( viewMatcher.getSelection() ) {
			wizard.currentRule = wizard.currentIndication.getMatch(lstMatchers.getSelectionIndex());
			ViewRulePage viewPage = wizard.viewRulePage;
			viewPage.onEnterPage();
			return viewPage;
		}
		// Conclude match option was chosen
		else if ( concludeIndication.getSelection() ) {
			if (wizard.newIndication)
				wizard.lstIndications.add(wizard.currentIndication);
			IndicationsPage indicationsPage = wizard.indicationsPage;
			indicationsPage.setChanged(true);
			indicationsPage.onEnterPage();
			return indicationsPage;
		}
		// Create match option was chosen
		else if ( createMatcher.getSelection() ){
			wizard.newRule = true;
			wizard.currentRule = new MatchText("", "", false);
			CreationRulePage creationRulePage = wizard.creationRulePage;
			creationRulePage.onEnterPage();
			return creationRulePage;
		}
		// Update match option was chosen
		else {	
			wizard.newRule = false;
			wizard.currentRule = wizard.currentIndication.getMatch(lstMatchers.getSelectionIndex());
			if ( wizard.currentRule == null )
				return null;
			CreationRulePage creationRulePage = wizard.creationRulePage;
			creationRulePage.onEnterPage();
			return creationRulePage;
		}
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		setTitle("Management of Rules");
		setDescription("Choose one option below and click Next.");
		wizard.canFinish = false;
		
		// Reset radios
		createMatcher.setSelection(true);
		updateMatcher.setSelection(false);
		removeMatcher.setSelection(false);
		viewMatcher.setSelection(false);
		concludeIndication.setSelection(false);
		
		// Updating list
		ArrayList rules = wizard.currentIndication.getMatches();
		lstMatchers.removeAll();
		String sItem;
		MatchText rule;
		for (int i = 0; i < rules.size(); i++){
			rule = (MatchText)rules.get(i);
			if ( rule.getTarget() == MatchText.STRING_LITERAL )
				sItem = "string literal that ";
			else
				sItem = "variable name that ";
			if ( rule.getRule() == MatchText.CONTAINS )
				sItem += "contains "; 
			else if ( rule.getRule() == MatchText.STARTS_WITH )
				sItem += "starts with "; 
			if ( rule.getRule() == MatchText.ENDS_WITH )
				sItem += "ends with ";
			if ( rule.getWords().size() == 1 )
				sItem += (String)rule.getWords().get(0);
			else if ( rule.getWords().size() == 2 )
				sItem += (String)rule.getWords().get(0) + " and " +
					(String)rule.getWords().get(1);
			else if ( rule.getWords().size() == 3 )
				sItem += (String)rule.getWords().get(0) + ", " +
					(String)rule.getWords().get(1) + " and " +
					(String)rule.getWords().get(2);
			else for ( int j = 0; j < 3; j++ ) {
				sItem += (String)rule.getWords().get(j);
				if ( j < 2 )
					sItem += ", ";
				else
					sItem += ", ...";
			}
			if(rule.isCaseSensity())
				sItem += " (case sensitive)";
			lstMatchers.add(sItem);
		}
	}
}
