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

import br.ufscar.dc.rejasp.indication.model.IndicationClass;
import br.ufscar.dc.rejasp.indication.model.IndicationException;
import br.ufscar.dc.rejasp.indication.model.IndicationInterface;

/**
 * @author Daniel Kawakami
 * This is a page of indication wizard. It's in charge of show  
 * package data to user
 */
public class ViewPackagePage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IndicationWizard wizard;
	/**
	 * Name field of package
	 */
	private Text txtName;
	/**
	 * Description field of package
	 */
	private Text txtDescription;
	/**
	 * List of interfaces belong to the package
	 */
	private List lstInterfaces;
	/**
	 * List of classes belong to the package
	 */
	private List lstClasses;
	/**
	 * List of exceptions belong to the package
	 */
	private List lstExceptions;
	
	public ViewPackagePage() {
		super("View package data page");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (IndicationWizard)getWizard();

		// widges declarations
		Label lbName, lbDescription, lbInterface, lbClass, lbException;
		
		// create the composite to hold the widgets
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);
		int nIdent = 20;

	    // create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 5;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);

		// Line 1
		fillCells(composite, 1, 13);

		lbName = new Label(composite, SWT.NONE);
		lbName.setText("Name:");
		gd = new GridData();
		gd.horizontalIndent = nIdent;
		lbName.setLayoutData(gd);
		
		txtName = new Text(composite, SWT.BORDER | SWT.READ_ONLY );
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		txtName.setLayoutData(gd);

		fillCells(composite, 1, 13);
		
		// Line 2
		lbDescription = new Label(composite, SWT.NONE);
		lbDescription.setText("Description:");
		gd = new GridData();
		gd.horizontalIndent = nIdent;
		lbDescription.setLayoutData(gd);
		
		txtDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY  | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		gd.horizontalSpan = 2;
		txtDescription.setLayoutData(gd);
		
		// Line 3
		fillCells(composite, 1, 1);
		
		// Line 4
		lbInterface = new Label(composite, SWT.NONE);
		lbInterface.setText("Interfaces:");
		gd = new GridData();
		gd.horizontalIndent = nIdent;
		lbInterface.setLayoutData(gd);
		
		lstInterfaces = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		lstInterfaces.setItems(new String []{"","","","",""});
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 3;
		gd.horizontalSpan = 2;
		lstInterfaces.setLayoutData(gd);
		
		// Line 5 - 6
		fillCells(composite, 1, 2);
		
		// Line 7
		lbClass = new Label(composite, SWT.NONE);
		lbClass.setText("Classes:");
		gd = new GridData();
		gd.horizontalIndent = nIdent;
		lbClass.setLayoutData(gd);
		
		lstClasses = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		lstClasses.setItems(new String []{"","","","",""});
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 3;
		gd.horizontalSpan = 2;
		lstClasses.setLayoutData(gd);
		
		// Line 8 - 9
		fillCells(composite, 1, 2);

		// Line 10
		lbException = new Label(composite, SWT.NONE);
		lbException.setText("Exceptions:");
		gd = new GridData();
		gd.horizontalIndent = nIdent;
		lbException.setLayoutData(gd);
		
		lstExceptions = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		lstExceptions.setItems(new String []{"","","","",""});
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 3;
		gd.horizontalSpan = 2;
		lstExceptions.setLayoutData(gd);
		
		// Line 11 - 12
		fillCells(composite, 1, 2);
		
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
		setTitle("Package " + wizard.currentPackage.getName());
		setDescription("Click Back to return to package manage page");
		wizard.canFinish = false;
		
		// Insert data in interface
		txtName.setText(wizard.currentPackage.getName());
		txtDescription.setText(wizard.currentPackage.getDescription());
		
		// Fill list of interface 
		lstInterfaces.removeAll();
		ArrayList interfaces = wizard.currentPackage.getInterfaces();
		for ( int i = 0; i < interfaces.size(); i++ )
			lstInterfaces.add(((IndicationInterface)interfaces.get(i)).getName());
		
		// Fill list of classes 
		lstClasses.removeAll();
		ArrayList classes = wizard.currentPackage.getClasses();
		for ( int i = 0; i < classes.size(); i++ )
			lstClasses.add(((IndicationClass)classes.get(i)).getName());
		
		// Fill list of exceptions
		lstExceptions.removeAll();
		ArrayList exceptions = wizard.currentPackage.getExceptions();
		for ( int i = 0; i < exceptions.size(); i++ )
			lstExceptions.add(((IndicationException)exceptions.get(i)).getName());
	}

}
