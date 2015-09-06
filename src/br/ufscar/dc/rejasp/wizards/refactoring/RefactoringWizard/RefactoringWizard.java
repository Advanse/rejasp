package br.ufscar.dc.rejasp.wizards.refactoring.RefactoringWizard;

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

import br.ufscar.dc.rejasp.model.Pointcut;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.FileInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;
import br.ufscar.dc.rejasp.views.IndicationTree;
import br.ufscar.dc.rejasp.views.TreeObject;

public class RefactoringWizard  extends Wizard implements INewWizard {
	// wizard pages
	TypeSelectionPage typePage;
	MethodSelectionPage methodPage;
	RefactoringSelectionPage refactoringPage;
	AspectMainPage aspectPage;
	NewAspectPage newAspectPage;
	PointcutPage pointcutPage;
	VisualizationEBPage visualizationEBPage;
	VisualizationEEPage visualizationEEPage;
	
	private boolean bCanFinish;
	private ArrayList lstTypes;
	private ArrayList lstMethods;
	private TypeInfo typeInfo;
	private MethodInfo methodInfo;
	private IFile aspectFile;
	private int refactoring;
	private Pointcut pointcut;
	
	// workbench selection when the wizard was started
	protected IStructuredSelection selection;
	
	// the workbench instance
	protected IWorkbench workbench;

	private IProject project;

	public RefactoringWizard() {
		super();
		setWindowTitle("Reorganization");
		bCanFinish = false;
		refactoring = 0;
	}
	
	public void addPages() {
		typePage = new TypeSelectionPage();
		addPage(typePage);
		methodPage = new MethodSelectionPage();
		addPage(methodPage);
		refactoringPage = new RefactoringSelectionPage();
		addPage(refactoringPage);
		aspectPage = new AspectMainPage();
		addPage(aspectPage);
		newAspectPage = new NewAspectPage();
		addPage(newAspectPage);
		pointcutPage = new PointcutPage();
		addPage(pointcutPage);
		visualizationEBPage = new VisualizationEBPage();
		addPage(visualizationEBPage);
		visualizationEEPage = new VisualizationEEPage();
		addPage(visualizationEEPage);
	}
	
	/**
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		this.setWindowTitle("Reorganization");
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
		if( (refactoring & TreeObject.REFACTOR_EXTRACT_BEGINNIG) != 0)	{
			if ( ! visualizationEBPage.saveJavaFile() )
				return false;
			if ( ! visualizationEBPage.saveAjFile() )
				return false;
		}
		else if((refactoring & TreeObject.REFACTOR_EXTRACT_END) != 0) {
			if ( ! visualizationEEPage.saveJavaFile() )
				return false;
			if ( ! visualizationEEPage.saveAjFile() )
				return false;
		}
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
	
	public void setRefactoring(int refactoring) {
		this.refactoring = refactoring;
	}
	
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}
	
	public Pointcut getPointcut() {
		return pointcut;
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
	 * Only call this method after setting a selected class / interface by
	 * setSelectedType.  
	 * @return Returns all methods filtered by class selection.
	 */
	public ArrayList getMethods() {
		return lstMethods;
	}
	
	public TypeInfo getSelectedType() {
		return typeInfo;
	}

	public MethodInfo getSelectedMethod() {
		return methodInfo;
	}

	public IWorkbench getWorkbench() {
		return workbench;
	}
	
	public int getRefactoring() {
		return refactoring;
	}
	
	public void setSelectedType(TypeInfo typeInfo) {
		this.typeInfo = typeInfo;
		lstMethods.clear();
		lstMethods.addAll(typeInfo.getMethods());
	}

	public void setSelectedMethod(MethodInfo methodInfo) {
		this.methodInfo = methodInfo;
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
}
