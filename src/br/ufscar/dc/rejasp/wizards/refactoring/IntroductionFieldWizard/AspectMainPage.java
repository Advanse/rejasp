package br.ufscar.dc.rejasp.wizards.refactoring.IntroductionFieldWizard;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

public class AspectMainPage extends WizardPage implements Listener {
	/**
	 * Reference to wizard
	 */
	private IntroductionFieldWizard wizard;

	Button btnCreateAspect;
	Button btnSelectAspect;
	ArrayList lstAspectFiles;
	List lstAspects;
	
	public AspectMainPage () {
		super("Aspect Main Page");
		setTitle("Selection of an Aspect");
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
		int ncol = 1;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);

		btnCreateAspect = new Button(composite, SWT.RADIO);
		btnCreateAspect.setText("Create a new Aspect that will contain extracted field");
		
		btnSelectAspect = new Button(composite, SWT.RADIO);
		btnSelectAspect.setText("Select an existing aspect where field will be inserted");
		
		wizard.fillCells(composite, 1, 1);
		
		new Label(composite, SWT.NONE).setText("Existing aspects");
		
		GridData gd;

		gd = new GridData( GridData.FILL_BOTH );
		lstAspects = new List(composite, SWT.SINGLE | SWT.V_SCROLL );
		lstAspects.setItems( new String[] {"","",""});
		lstAspects.setLayoutData(gd);
		
		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		btnCreateAspect.addListener(SWT.MouseUp, this);
		btnSelectAspect.addListener(SWT.MouseUp, this);
		lstAspects.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);
	    if ( btnCreateAspect.getSelection() ) {
	    	lstAspects.deselectAll();
	    	lstAspects.setEnabled(false);
	    	status = new Status(IStatus.OK, "not_used", 0, 
	    			"Click Next to create a new aspect", null);
	    }
	    else if ( btnSelectAspect.getSelection() ) {
	    	lstAspects.setEnabled(true);
	    	if ( lstAspects.getSelectionIndex() == -1)
		    	status = new Status(IStatus.ERROR, "not_used", 0, 
		    			"Choose an existing aspect", null);
	    	else
		    	status = new Status(IStatus.OK, "not_used", 0, 
		    			"Click Next to proceed", null);
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
		if ( btnCreateAspect.getSelection() ) {
			wizard.setAjdtAppearance();
			return wizard.newAspectPage;
		}
		int nIndex = lstAspects.getSelectionIndex(); 
		if ( nIndex == -1 )
			return null;
		
		IFile file = (IFile)lstAspectFiles.get(nIndex);
		wizard.setAspectFile(file);
		wizard.aspectVisualisation.onEnterPage();
		return wizard.aspectVisualisation;
	}
	
	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		lstAspects.removeAll();
		setDescription("Choose one of the options below");
		
		// Creating model
		IProject project = wizard.getProject();
		IResource[] resources = null;
		if ( ((IResource)project).exists() && project.isOpen() ) {
			try {
				resources = project.members();
			} catch (CoreException e) {
				System.err.println( e.getMessage() );
			}
			
		    // Model initialization
			lstAspectFiles = new ArrayList();

			// For each folder and file
			for ( int i = 0; i < resources.length; i++ ) {
				if ( ! resources[i].exists() )
					System.err.println("The resource" + resources[i].getName() + "doesn't exist!");
				else if ( resources[i] instanceof IFolder )
					searchAspectInFolder( (IFolder)resources[i] );
				else if ( resources[i] instanceof IFile && 
						 ((IFile)resources[i]).getFileExtension().equals("aj")&&
							((IFile)resources[i]).getName().length() > 3 ) {
					addAspect( (IFile)resources[i] );
				}
			}
			if(lstAspects.getItemCount() == 0) {
				btnCreateAspect.setSelection(true);
				btnSelectAspect.setSelection(false);
				lstAspects.setEnabled(false);
			}
			else {
				btnCreateAspect.setSelection(false);
				btnSelectAspect.setSelection(true);
				lstAspects.setEnabled(true);
			}
		}
	}
	
	public void searchAspectInFolder(IFolder folder) {
		IResource[] resources;
		try {
			resources= folder.members();
		} catch (CoreException e) {
			System.err.println(e.getStackTrace());
			return;
		}
		// For each folder and file
		for ( int i = 0; i < resources.length; i++ ) {
			if ( resources[i] instanceof IFolder )
				searchAspectInFolder( (IFolder)resources[i] );
			else if ( resources[i] instanceof IFile && 
					((IFile)resources[i]).getFileExtension().equals("aj") &&
					((IFile)resources[i]).getName().length() > 3)
				addAspect( (IFile)resources[i] );
		}
	}

	public void addAspect(IFile file) {
		String sAspectName = file.getName().substring(0, file.getName().lastIndexOf("."));
		lstAspects.add(sAspectName);
		lstAspectFiles.add(file);
	}
	
	public boolean isCreationAspectSelected() {
		return btnCreateAspect.getSelection();
	}
}
