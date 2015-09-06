package br.ufscar.dc.rejasp.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import br.ufscar.dc.rejasp.views.IndicationTree;
import br.ufscar.dc.rejasp.wizards.refactoring.RefactoringWizard.RefactoringWizard;

public class RefactoringAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	ISelection selection;

	/**
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		// Get current project
		IViewPart viewPart = window.getActivePage().findView("br.ufscar.dc.rejasp.views.IndicationTree");
		if (viewPart == null) {
			MessageDialog.openInformation(window.getShell(), 
					"Warning", "Please, close the next page and open indication tree view before managing indications.");
			return;
		}
		IProject project = ((IndicationTree)viewPart).getCurrentProject();
		if(project == null)
			MessageDialog.openInformation(window.getShell(), 
					"Warning", "No project was found at indication tree. Please, select a project.");

		// Instantiates and initializes the wizard
		RefactoringWizard wizard = new RefactoringWizard();
		wizard.setProject(project);
		if ((selection instanceof IStructuredSelection) || (selection == null))
			wizard.init(window.getWorkbench(), (IStructuredSelection)selection);
		else
			wizard.init(window.getWorkbench(), null);
			
		// Instantiates the wizard container with the wizard and opens it
		WizardDialog dialog = new WizardDialog( window.getShell(), wizard);
		dialog.create();
		dialog.open();
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
