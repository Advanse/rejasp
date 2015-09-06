package br.ufscar.dc.rejasp.wizards.refactoring.RefactoringWizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Event;

public class VisualizationEEPage extends VisualizationPage {
	public VisualizationEEPage() {
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
				// The range of statements is represented by the index of the first statement
				// in the range and the index of the last statement.
				// First statement = last index - (number of statements to be extracted - 1),
				// so first statement = (number statement - 1) - (spinner selection - 1) =
				// = number of statement -1 - spinner selection + 1 = number of statement - spinner selection
				// And last statement = number of statement - 1
				int nStatements = methodDeclaration.getBody().statements().size();
				boolean bPreview = previewJavaCode(nStatements - spnStatements.getSelection(), nStatements - 1);
				createRecomendation(nStatements - spnStatements.getSelection(), nStatements - 1);
				generateAspectCode(nStatements - spnStatements.getSelection(), nStatements - 1);
				wizard.setCanFinish(bPreview);
			}
	    }
		applyToStatusLine(status);
		wizard.getContainer().updateButtons();
	}
	
	public boolean saveJavaFile() {
		return saveJavaFile(methodDeclaration.getBody().statements().size() - spnStatements.getSelection(), 
				methodDeclaration.getBody().statements().size() - 1);
	}
}
