package br.ufscar.dc.rejasp.wizards.refactoring.RefactoringWizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Event;

public class VisualizationEBPage extends VisualizationPage {
	public VisualizationEBPage() {
		super();
	}
	
	public void handleEvent(Event event) {
		super.handleEvent(event);
	    Status status = new Status(IStatus.OK, "not_used", 0, "", null);

	    if(event.widget == spnStatements) {
			if ( spnStatements.getSelection() == 0 ) {
				status = new Status(IStatus.ERROR, "not_used", 0, "Select a number of statements to be extracted", null);
				wizard.setCanFinish(false);
			}
			else {
				boolean bPreview = previewJavaCode(0, spnStatements.getSelection() - 1);
				createRecomendation(0, spnStatements.getSelection() - 1);
				generateAspectCode(0, spnStatements.getSelection() - 1);
				wizard.setCanFinish(bPreview);
			}
	    }
		applyToStatusLine(status);
		wizard.getContainer().updateButtons();
	}
	
	public boolean saveJavaFile() {
		return saveJavaFile(0, spnStatements.getSelection() - 1);
	}
}
