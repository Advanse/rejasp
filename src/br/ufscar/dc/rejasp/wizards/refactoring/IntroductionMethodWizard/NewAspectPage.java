package br.ufscar.dc.rejasp.wizards.refactoring.IntroductionMethodWizard;

import org.eclipse.ajdt.internal.ui.wizards.NewAspectWizardPage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

public class NewAspectPage extends NewAspectWizardPage {
	/**
	 * Reference to wizard
	 */
	private IntroductionMethodWizard wizard;

	public void createControl(Composite parent) {
		super.createControl(parent);
		wizard = (IntroductionMethodWizard)getWizard();
	}
	
	public boolean canFlipToNextPage() {
		if ( getErrorMessage() == null )
			return true;
		return false;
	}
	
	public IWizardPage getNextPage() {
		NullProgressMonitor monitor = new NullProgressMonitor();
		try {
			createType(monitor);
		} catch (InterruptedException e) {
			System.err.println(e.getStackTrace());
		} catch (CoreException e) {
			System.err.println(e.getStackTrace());
		}
		wizard.setAspectFile((IFile)this.getCreatedType().getResource());
		wizard.setOriginalAppearance();
		wizard.aspectVisualisation.onEnterPage();
		return wizard.aspectVisualisation;
	}
}