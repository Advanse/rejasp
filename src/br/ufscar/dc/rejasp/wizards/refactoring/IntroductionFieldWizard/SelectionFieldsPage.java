package br.ufscar.dc.rejasp.wizards.refactoring.IntroductionFieldWizard;

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

import br.ufscar.dc.rejasp.model.ASTNodeInfo.FieldInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;

public class SelectionFieldsPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IntroductionFieldWizard wizard;
	
	private Button btnIndicationFields;
	private Button btnAllFields;
	private Button btnSelectAll;
	private Button btnDeselectAll;
	private Tree fieldsTree;
	private ArrayList lstSelectedTypes;
	
	public SelectionFieldsPage() {
		super("Page of Selection Fields");
		setTitle("Selection of fields");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (IntroductionFieldWizard)getWizard();

		// create the composite to hold the widgets
		Composite composite =  new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 2;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);

		btnIndicationFields = new Button(composite, SWT.RADIO);
		btnIndicationFields.setText("Show only fields with indications");
		
		wizard.fillCells(composite, 1, 1);
		
		btnAllFields = new Button(composite, SWT.RADIO);
		btnAllFields.setText("Show all fields");

		wizard.fillCells(composite, 1, 1);
		
		wizard.fillCells(composite, 2, 1);
		
		GridData gd;
		
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan = 8;
		fieldsTree = new Tree (composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK );
		fieldsTree.setHeaderVisible(true);
		TreeColumn tcName = new TreeColumn(fieldsTree, SWT.LEFT);
		tcName.setText("Name");
		tcName.setWidth(150);
		TreeColumn tcConstruction = new TreeColumn(fieldsTree, SWT.LEFT);
		tcConstruction.setText("Construction");
		tcConstruction.setWidth(150);
		TreeColumn tcLocale = new TreeColumn(fieldsTree, SWT.LEFT);
		tcLocale.setText("Locale / Type");
		tcLocale.setWidth(150);
		TreeColumn tcModifiers = new TreeColumn(fieldsTree, SWT.LEFT);
		tcModifiers.setText("Modifiers");
		tcModifiers.setWidth(150);
		fieldsTree.setLayoutData(gd);
		
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
		btnIndicationFields.addListener(SWT.MouseUp, this);
		btnAllFields.addListener(SWT.MouseUp, this);
		btnSelectAll.addListener(SWT.MouseUp, this);
		btnDeselectAll.addListener(SWT.MouseUp, this);
		fieldsTree.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);
	    TreeItem[] classItems, fieldItems;
	    if ( event.widget == btnSelectAll ) {
	    	classItems = fieldsTree.getItems();
	    	if ( classItems != null && classItems.length != 0 ) {
	    		for ( int i = 0; i < classItems.length; i++ ) {
	    			classItems[i].setChecked(true);
	    			fieldItems = classItems[i].getItems();
	    			for ( int j = 0; j < fieldItems.length; j++ )
	    				fieldItems[j].setChecked(true);
	    		}
	    	}
	    	else 
	    		status = new Status(IStatus.ERROR, "not_used", 0, "Check at least one field", null);
	    }
	    else if ( event.widget == btnDeselectAll ) {
	    	classItems = fieldsTree.getItems();
	    	if ( classItems != null && classItems.length != 0 ) {
	    		for ( int i = 0; i < classItems.length; i++ ) {
	    			classItems[i].setChecked(false);
	    			fieldItems = classItems[i].getItems();
	    			for ( int j = 0; j < fieldItems.length; j++ )
	    				fieldItems[j].setChecked(false);
	    		}
	    	}
    		status = new Status(IStatus.ERROR, "not_used", 0, "Check at least one field", null);
	    } 
	    else if ( event.widget == btnAllFields )
	    	buildTree(true);
	    else if ( event.widget == btnIndicationFields )
	    	buildTree(false);
	    else if ( event.item  instanceof TreeItem ) {
	    	TreeItem currentItem = (TreeItem)event.item;
	    	// Is parent a root (tree)?
	    	if ( currentItem.getParentItem() == null ) {
	    		// A class was selected, (un)check all fields
	    		TreeItem[] items = currentItem.getItems();
	    		for ( int i = 0; i < items.length; i++ )
	    			items[i].setChecked(currentItem.getChecked());
	    	}
	    	else {
	    		// A field was (un)checked
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
    		status = new Status(IStatus.ERROR, "not_used", 0, "Check at least one field", null);
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
		ArrayList lstSelectedFields = new ArrayList();
		TreeItem[] types = fieldsTree.getItems();
		TreeItem[] fields;
		for ( int i = 0; i < types.length; i++ ) {
			fields = types[i].getItems();
			for ( int j = 0; j < fields.length; j++ )
				if ( fields[j].getChecked() )
					lstSelectedFields.add(fields[j].getData());
		}
		wizard.setSelectedFields(lstSelectedFields);
		wizard.mainPage.onEnterPage();
		return wizard.mainPage; 
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		setDescription("Select fields that will be introduced to aspects");
		btnIndicationFields.setSelection(true);
		btnAllFields.setSelection(false);
		boolean bAllFields = true;
		if (wizard.selectionClassesPage.onlyIndicationTypes()) {
			btnIndicationFields.setVisible(true);
			btnAllFields.setVisible(true);
			btnIndicationFields.setSelection(true);
			bAllFields = false;
		}
		else {
			btnIndicationFields.setVisible(false);
			btnAllFields.setVisible(false);
		}
		lstSelectedTypes = wizard.getSelectedTypes();
		buildTree(bAllFields);
	}
	
	private void buildTree( boolean bAllFields) {
		TreeItem item, fieldItem;
		fieldsTree.removeAll();
		TypeInfo typeInfo;
		FieldInfo fieldInfo;
		ArrayList lstFields;
		
		// For each class or interface
		for ( int i = 0; i < lstSelectedTypes.size(); i++ ) {
			typeInfo = (TypeInfo)lstSelectedTypes.get(i);
			item = new TreeItem( fieldsTree, SWT.NONE );
			item.setText(new String[] { typeInfo.getName(), 
					typeInfo.isInterface()?"Interface":"Class", 
					typeInfo.getLocale(), typeInfo.getModifiers() });
			item.setData(typeInfo);
			lstFields = typeInfo.getFields();
			// for each field
			for ( int j = 0; j < lstFields.size(); j++ ) {
				fieldInfo = (FieldInfo)lstFields.get(j);
				if ( bAllFields || fieldInfo.getIndication() ) {
					fieldItem = new TreeItem( item, SWT.NONE );
					fieldItem.setText(new String[] { fieldInfo.getName(), "Field", fieldInfo.getType(), fieldInfo.getModifiers() });
					fieldItem.setData(fieldInfo);
				}
			}
			item.setExpanded(true);
		}
		if ( fieldsTree.getItems().length > 0 ) {
			item = fieldsTree.getItems()[0];
			fieldsTree.showItem(item);
		}
	}
	
	public boolean isChecked() {
    	TreeItem[] items = fieldsTree.getItems();
    	if ( items != null && items.length != 0 )
    		for ( int i = 0; i < items.length; i++ )
    			if ( items[i].getChecked())
    				return true;
    		return false;
	}
}
