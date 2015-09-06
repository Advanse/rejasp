package br.ufscar.dc.rejasp.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import br.ufscar.dc.rejasp.wizards.IndicationWizard.IndicationWizard;

/**
 * @author Dark Templar
 * This class calls wizards in order to manage (=insert, update and remove) indication. 
 */
public class ManageIndications implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	ISelection selection;

	/**
	 * The constructor.
	 */
	public ManageIndications() {
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		// Instantiates and initializes the wizard
		
		IndicationWizard wizard = new IndicationWizard();
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
