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

import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;

public class SelectionMethodsPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IntroductionMethodWizard wizard;
	
	private Button btnIndicationMethods;
	private Button btnAllMethods;
	private Button btnSelectAll;
	private Button btnDeselectAll;
	private Tree methodsTree;
	private ArrayList lstSelectedTypes;
	
	public SelectionMethodsPage() {
		super("Page of Selection Methods");
		setTitle("Selection of methods");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (IntroductionMethodWizard)getWizard();

		// create the composite to hold the widgets
		Composite composite =  new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 2;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);

		btnIndicationMethods = new Button(composite, SWT.RADIO);
		btnIndicationMethods.setText("Show only methods with indications");
		
		wizard.fillCells(composite, 1, 1);
		
		btnAllMethods = new Button(composite, SWT.RADIO);
		btnAllMethods.setText("Show all methods");

		wizard.fillCells(composite, 1, 1);
		
		wizard.fillCells(composite, 2, 1);
		
		GridData gd;
		
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 8;
		methodsTree = new Tree (composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK );
		methodsTree.setHeaderVisible(true);
		TreeColumn tcName = new TreeColumn(methodsTree, SWT.LEFT);
		tcName.setText("Name");
		tcName.setWidth(150);
		TreeColumn tcConstruction = new TreeColumn(methodsTree, SWT.LEFT);
		tcConstruction.setText("Construction");
		tcConstruction.setWidth(150);
		TreeColumn tcLocale = new TreeColumn(methodsTree, SWT.LEFT);
		tcLocale.setText("Locale / Return Type");
		tcLocale.setWidth(150);
		TreeColumn tcModifiers = new TreeColumn(methodsTree, SWT.LEFT);
		tcModifiers.setText("Modifiers");
		tcModifiers.setWidth(150);
		methodsTree.setLayoutData(gd);
		
		btnSelectAll = new Button(composite, SWT.CENTER);
		btnSelectAll.setText("Select &All");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		btnSelectAll.setLayoutData(gd);
		
		btnDeselectAll = new Button (composite, SWT.CENTER);
		btnDeselectAll.setText("&Deselect All");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		btnDeselectAll.setLayoutData(gd);

		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		btnIndicationMethods.addListener(SWT.MouseUp, this);
		btnAllMethods.addListener(SWT.MouseUp, this);
		btnSelectAll.addListener(SWT.MouseUp, this);
		btnDeselectAll.addListener(SWT.MouseUp, this);
		methodsTree.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);
	    TreeItem[] classItems, methodItems;
	    if ( event.widget == btnSelectAll ) {
	    	classItems = methodsTree.getItems();
	    	if ( classItems != null && classItems.length != 0 ) {
	    		for ( int i = 0; i < classItems.length; i++ ) {
	    			classItems[i].setChecked(true);
	    			methodItems = classItems[i].getItems();
	    			for ( int j = 0; j < methodItems.length; j++ )
	    				methodItems[j].setChecked(true);
	    		}
	    	}
	    	else 
	    		status = new Status(IStatus.ERROR, "not_used", 0, "Check at least one method", null);
	    }
	    else if ( event.widget == btnDeselectAll ) {
	    	classItems = methodsTree.getItems();
	    	if ( classItems != null && classItems.length != 0 ) {
	    		for ( int i = 0; i < classItems.length; i++ ) {
	    			classItems[i].setChecked(false);
	    			methodItems = classItems[i].getItems();
	    			for ( int j = 0; j < methodItems.length; j++ )
	    				methodItems[j].setChecked(false);
	    		}
	    	}
    		status = new Status(IStatus.ERROR, "not_used", 0, "Check at least one method", null);
	    } 
	    else if ( event.widget == btnAllMethods )
	    	buildTree(true);
	    else if ( event.widget == btnIndicationMethods )
	    	buildTree(false);
	    else if ( event.item  instanceof TreeItem ) {
	    	TreeItem currentItem = (TreeItem)event.item;
	    	// Is parent a root (tree)?
	    	if ( currentItem.getParentItem() == null ) {
	    		// A class was selected, (un)check all methods
	    		TreeItem[] items = currentItem.getItems();
	    		for ( int i = 0; i < items.length; i++ )
	    			items[i].setChecked(currentItem.getChecked());
	    	}
	    	else {
	    		// A method was (un)checked
	    		if ( currentItem.getChecked() )
	    			currentItem.getParentItem().setChecked(true);
	    		else {
	    			currentItem.getParentItem().setChecked(false);
		    		TreeItem[] items = currentItem.getParentItem().getItems();
		    		for ( int i = 0; i < items.length; i++ )
		    			if ( items[i].getChecked() ) {
			    			currentItem.getParentItem().setChecked(true);
			    			break;
		    			}
	    		}
	    	}
	    }
	    
	    if ( ! isChecked() )
    		status = new Status(IStatus.ERROR, "not_used", 0, "Check at least one method", null);
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
		ArrayList lstSelectedMethods = new ArrayList();
		TreeItem[] types = methodsTree.getItems();
		TreeItem[] methods;
		for ( int i = 0; i < types.length; i++ ) {
			methods = types[i].getItems();
			for ( int j = 0; j < methods.length; j++ )
				if ( methods[j].getChecked() )
					lstSelectedMethods.add(methods[j].getData());
		}
		wizard.setSelectedMethods(lstSelectedMethods);
		wizard.mainPage.onEnterPage();
		return wizard.mainPage;
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		setDescription("Select methods that will be introduced to aspects");
		btnIndicationMethods.setSelection(true);
		btnAllMethods.setSelection(false);
		boolean bAllMethods = true;
		if (wizard.selectionClassesPage.onlyIndicationTypes()) {
			btnIndicationMethods.setVisible(true);
			btnAllMethods.setVisible(true);
			btnIndicationMethods.setSelection(true);
			bAllMethods = false;
		}
		else {
			btnIndicationMethods.setVisible(false);
			btnAllMethods.setVisible(false);
		}
		lstSelectedTypes = wizard.getSelectedTypes();
		buildTree(bAllMethods);
	}
	
	private void buildTree( boolean bAllMethods) {
		TreeItem item, methodItem;
		methodsTree.removeAll();
		TypeInfo typeInfo;
		MethodInfo methodInfo;
		ArrayList lstMethods;
		String sReturnType;
		
		// For each class or interface
		for ( int i = 0; i < lstSelectedTypes.size(); i++ ) {
			typeInfo = (TypeInfo)lstSelectedTypes.get(i);
			item = new TreeItem( methodsTree, SWT.NONE );
			item.setText(new String[] { typeInfo.getName(), 
					typeInfo.isInterface()?"Interface":"Class", 
					typeInfo.getLocale(), typeInfo.getModifiers() });
			item.setData(typeInfo);
			lstMethods = typeInfo.getMethods();
			// for each method
			for ( int j = 0; j < lstMethods.size(); j++ ) {
				methodInfo = (MethodInfo)lstMethods.get(j);
				if ( bAllMethods || methodInfo.getIndication() ) {
					sReturnType = methodInfo.getReturnType();
					methodItem = new TreeItem( item, SWT.NONE );
					methodItem.setText(new String[] { methodInfo.getName(), 
							methodInfo.isConstructor()?"Constructor":"Method", 
							methodInfo.getReturnType(),methodInfo.getModifiers() });
					methodItem.setData(methodInfo);
				}
			}
			item.setExpanded(true);
		}
		if ( methodsTree.getItems().length > 0 ) {
			item = methodsTree.getItems()[0];
			methodsTree.showItem(item);
		}
	}
	
	public boolean isChecked() {
    	TreeItem[] items = methodsTree.getItems();
    	if ( items != null && items.length != 0 )
    		for ( int i = 0; i < items.length; i++ )
    			if ( items[i].getChecked())
    				return true;
    		return false;
	}
}
