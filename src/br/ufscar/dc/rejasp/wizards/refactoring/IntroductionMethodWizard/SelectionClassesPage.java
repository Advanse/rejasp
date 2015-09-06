package br.ufscar.dc.rejasp.wizards.refactoring.IntroductionMethodWizard;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;

public class SelectionClassesPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IntroductionMethodWizard wizard;
	private Button btnIndicationTypes;
	private Button btnAllTypes;
	private Tree tree;
	private Button btnSelectAll;
	private Button btnDeselectAll;
	
	private ArrayList lstTypeInfo;

	public SelectionClassesPage() {
		super ("Page of Selection of Classes / Interfaces");
		setTitle("Selection of Classes and Interfaces");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (IntroductionMethodWizard)getWizard();

		// Creating visual presentation
		Composite composite =  new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 2;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);
		
		btnIndicationTypes = new Button(composite, SWT.RADIO);
		btnIndicationTypes.setText("Show only classes and interfaces that contains methods with indication");
		
		wizard.fillCells(composite, 1, 1);
		
		btnAllTypes = new Button(composite, SWT.RADIO);
		btnAllTypes.setText("Show all classes and interfaces of project");
		
		wizard.fillCells(composite, 1, 1);
		
		wizard.fillCells(composite, 2, 1);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 8;
		tree = new Tree (composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK );
		tree.setHeaderVisible(true);
		TreeColumn tcConstruction = new TreeColumn(tree, SWT.LEFT);
		tcConstruction.setText("Name");
		tcConstruction.setWidth(150);
		TreeColumn tcName = new TreeColumn(tree, SWT.LEFT);
		tcName.setText("Construction");
		tcName.setWidth(150);
		TreeColumn tcLocale = new TreeColumn(tree, SWT.LEFT);
		tcLocale.setText("Locale");
		tcLocale.setWidth(150);
		TreeColumn tcModifiers = new TreeColumn(tree, SWT.LEFT);
		tcModifiers.setText("Modifiers");
		tcModifiers.setWidth(150);
		tree.setLayoutData(gd);

		btnSelectAll = new Button(composite, SWT.CENTER );
		btnSelectAll.setText("Select &All");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		btnSelectAll.setLayoutData(gd);
		
		btnDeselectAll = new Button (composite, SWT.CENTER );
		btnDeselectAll.setText("&Deselect All");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		btnDeselectAll.setLayoutData(gd);

		setControl(composite);
		addListeners();
		
		onEnterPage();
	}
	
	private void addListeners() {
		btnIndicationTypes.addListener(SWT.MouseUp, this);
		btnAllTypes.addListener(SWT.MouseUp, this);
		btnSelectAll.addListener(SWT.MouseUp, this);
		btnDeselectAll.addListener(SWT.MouseUp, this);
		tree.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "Click Next to proceed", null);
	    TreeItem[] items;
	    if ( event.widget == btnSelectAll ) {
	    	items = tree.getItems();
	    	if ( items != null && items.length != 0 ) {
	    		for ( int i = 0; i < items.length; i++ )
	    			items[i].setChecked(true);
	    	}
	    	else 
	    		status = new Status(IStatus.ERROR, "not_used", 0, "Check at least one class or interface", null);
	    }
	    else if ( event.widget == btnDeselectAll ){
	    	items = tree.getItems();
	    	if ( items != null && items.length != 0 ) {
	    		for ( int i = 0; i < items.length; i++ )
	    			items[i].setChecked(false);
	    	}
    		status = new Status(IStatus.ERROR, "not_used", 0, "Check at least one class or interface", null);
	    }
	    else if ( event.widget == btnAllTypes ) {
			buildTypeTree(false);
	    }
	    else if ( event.widget == btnIndicationTypes ) {
	    	buildTypeTree(true);
	    }
	    if ( ! isChecked() )
    		status = new Status(IStatus.ERROR, "not_used", 0, "Check at least one class or interface", null);
		applyToStatusLine(status);
		wizard.getContainer().updateButtons();
	}
	
	private void buildTypeTree( boolean bOnlyIndication ) {
		TypeInfo element;
		TreeItem item;
		tree.removeAll();
		// for each type
		for ( int i = 0; i < lstTypeInfo.size(); i++ ) {
			element = (TypeInfo)lstTypeInfo.get(i);
			if ( ! bOnlyIndication || ( element.getIndication() && element.indicationMethodExist() )) {
				item = new TreeItem( tree, SWT.NONE );
				item.setText(new String[] { element.getName(), 
						element.isInterface()?"Interface":"Class",
						element.getLocale(), element.getModifiers()});
				item.setData(element);
			}
		}
		item = tree.getTopItem();
		if ( item != null )
			tree.showItem(item);
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
		if (getMessageType() == WizardPage.ERROR) {
			String sOldMessage = getErrorMessage();
			setErrorMessage(sOldMessage);
			setMessage(sOldMessage, WizardPage.ERROR);
			return false;
		}
		else if ( ! isChecked() )
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
	 * It makes all the setting before go to the next page
	 */
	public IWizardPage getNextPage() {
    	TreeItem[] items = tree.getItems();
    	ArrayList lstSelectedTypes = new ArrayList();
    	if ( items != null && items.length != 0 ) {
    		for ( int i = 0; i < items.length; i++ )
    			if ( items[i].getChecked())
    				lstSelectedTypes.add( items[i].getData() );
    	}
		wizard.setSelectedTypes(lstSelectedTypes);
		wizard.selectionMethodsPage.onEnterPage();
		return wizard.selectionMethodsPage; 
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		setDescription("Select classes and interfaces that contain methods to be moved to aspects");
		btnIndicationTypes.setSelection(true);
		btnAllTypes.setSelection(false);
		lstTypeInfo = wizard.getTypes();
		buildTypeTree(true);
	}
	
	public boolean isChecked() {
    	TreeItem[] items = tree.getItems();
    	if ( items != null && items.length != 0 )
    		for ( int i = 0; i < items.length; i++ )
    			if ( items[i].getChecked())
    				return true;
    		return false;
	}
	
	/**
	 * @return Returns true if only classes or interfaces with indication was selected.
	 */
	public boolean onlyIndicationTypes() {
		return btnIndicationTypes.getSelection();
	}
}

