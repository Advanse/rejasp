package br.ufscar.dc.rejasp.wizards.refactoring.IntroductionMethodWizard;

import java.util.ArrayList;

import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import br.ufscar.dc.rejasp.indication.model.Indication;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.FileInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;
import br.ufscar.dc.rejasp.views.IndicationTree;
public class IntroductionMethodWizard extends Wizard implements INewWizard
{
	// wizard pages
	SelectionClassesPage selectionClassesPage;
	AspectMainPage mainPage;
	SelectionMethodsPage selectionMethodsPage;
	NewAspectPage newAspectPage;
	AspectVisualisationPage aspectVisualisation;
	
	private boolean bCanFinish;
	private ArrayList lstTypes;
	private ArrayList lstMethods;
	private ArrayList lstSelectedTypes;
	private ArrayList lstSelectedMethods;
	
	private IFile aspectFile;
	
	// workbench selection when the wizard was started
	protected IStructuredSelection selection;
	
	// the workbench instance
	protected IWorkbench workbench;

	private IProject project;
	/**
	 * Constructor for IntroductionMethodWizard.
	 */
	public IntroductionMethodWizard() {
		super();
		setWindowTitle("Introduction of methods to aspects");
		bCanFinish = false;
		lstSelectedMethods = null;
	}
	
	public void addPages() {
		if( lstSelectedMethods == null ) {
			selectionClassesPage = new SelectionClassesPage();
			addPage(selectionClassesPage);
			selectionMethodsPage = new SelectionMethodsPage();
			addPage(selectionMethodsPage);
		}
		mainPage = new AspectMainPage();
		addPage (mainPage);
		newAspectPage = new NewAspectPage();
		/*
		 * Função comentada pode ser útil no futuro 
		 */
		//newAspectPage.init(getSelection());
		addPage(newAspectPage);
		aspectVisualisation = new AspectVisualisationPage();
		addPage(aspectVisualisation);
	}

	/**
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		this.setWindowTitle("Introduction of methods");
		IViewPart viewPart = workbench.getActiveWorkbenchWindow().getActivePage().findView("br.ufscar.dc.rejasp.views.IndicationTree");
		if (viewPart == null) {
			MessageDialog.openInformation(workbench.getActiveWorkbenchWindow().getShell(), 
					"Warning", "Please, close the next page and open indication tree view before managing indications.");
			lstTypes = new ArrayList();
		}
		else {
			// Setting lists
			lstMethods = new ArrayList();
			ArrayList lstFileInfo = (ArrayList)((IndicationTree)viewPart).getFileInfo().clone();
			lstTypes = new ArrayList();
			// for each file
			for( int i = 0; i < lstFileInfo.size(); i++ )
				lstTypes.addAll( ((FileInfo)lstFileInfo.get(i)).getTypes() );
		}
	}

	public boolean canFinish() {
		return bCanFinish;
	}
	
	public boolean performFinish() {
		if ( ! bCanFinish )
			return false;
		if ( ! aspectVisualisation.saveJavaFiles() )
			return false;
		if ( ! aspectVisualisation.saveAjFile() )
			return false;
		return true;
	}
	
	/**
	 * Create an empty space interface 
	 * @param composite reference to composite component
	 * @param nHorizontalSpan number of horizontal cells that the empty space will fill 
	 * @param nVerticalSpan number of vertical cells that the empty space will fill
	 */
	public void fillCells(Composite composite, int nHorizontalSpan, int nVerticalSpan) {
		Label lbEmpty = new Label(composite, SWT.NONE );
		lbEmpty.setText("");
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = nHorizontalSpan;
		gd.verticalSpan = nVerticalSpan;
		lbEmpty.setLayoutData(gd);
	}

	public void setAjdtAppearance() {
		setDefaultPageImageDescriptor(AspectJImages.W_NEW_ASPECT
				.getImageDescriptor());
		setDialogSettings(AspectJUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(UIMessages.NewAspectCreationWizard_title);
		getContainer().updateTitleBar();
		getContainer().updateMessage();
		getContainer().updateWindowTitle();
		getContainer().updateButtons();
	}
	
	public void setOriginalAppearance() {
		setDefaultPageImageDescriptor(null);
		setDialogSettings(null);
		setWindowTitle("Introduction of methods to aspects");
		getContainer().updateTitleBar();
	}
	
	public void setProject(IProject project) {
		this.project = project;
	}
	
	public IProject getProject() {
		return project;
	}
	
	/**
	 * @return Returns a list of all types.
	 */
	public ArrayList getTypes() {
		return lstTypes;
	}
	
	/**
	 * Only call this method after setting list of selected classes by
	 * setSelectedTypes.  
	 * @return Returns all methods filtered by class selection.
	 */
	public ArrayList getMethods() {
		return lstMethods;
	}
	
	public ArrayList getSelectedTypes() {
		return lstSelectedTypes;
	}

	public ArrayList getSelectedMethods() {
		return lstSelectedMethods;
	}
	
	public IWorkbench getWorkbench() {
		return workbench;
	}
	
	public void setSelectedTypes(ArrayList lstSelectedTypes) {
		this.lstSelectedTypes = lstSelectedTypes;
		// Find all methods belong to selected types
		lstMethods.clear();
		for( int i = 0; i < lstSelectedTypes.size(); i++ )
			lstMethods.addAll(((TypeInfo)lstSelectedTypes.get(i)).getMethods());
	}

	public void setSelectedMethods(ArrayList lstSelectedMethods) {
		this.lstSelectedMethods = lstSelectedMethods;
	}
	
	public void setCanFinish (boolean bCanFinish) {
		this.bCanFinish = bCanFinish;
	}
	
	public IJavaElement getCreatedElement() {
		return newAspectPage.getCreatedType();
	}

	public void setAspectFile(IFile aspectFile) {
		this.aspectFile = aspectFile; 
	}

	public IFile getAspectFile() {
		return aspectFile;
	}
	
	public Indication getActiveIndication () {
		// Looking for the first indication active
		IViewPart viewPart = workbench.getActiveWorkbenchWindow().getActivePage().findView("br.ufscar.dc.rejasp.views.IndicationTree");
		if (viewPart == null) {
			System.err.println("Indication Tree not accessible");
			return null;
		}
		ArrayList lstIndications = (ArrayList)((IndicationTree)viewPart).getIndications();
		for ( int i = 0; i < lstIndications.size(); i++ )
			if ( ((Indication)lstIndications.get(i)).isActive() ) 
				return (Indication)lstIndications.get(i);
		return null;
	}
	
	public void OnEnterMainPage() {
		mainPage.onEnterPage();
	}
}
