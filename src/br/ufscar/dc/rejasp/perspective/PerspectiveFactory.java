package br.ufscar.dc.rejasp.perspective;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory  implements IPerspectiveFactory{
	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
		defineLayout(layout);
	}
	
	public void defineActions(IPageLayout layout) {
		// Add "new wizards".
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		// Add "show views".
		layout.addShowViewShortcut("br.ufscar.dc.rejasp.views.IndicationTree");
		layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
	}
	
	public void defineLayout(IPageLayout layout) {
		// Editors are placed for free.
		String editorArea = layout.getEditorArea();
		// Place navigator and outline to left of editor area.
		IFolderLayout left =
			layout.createFolder("left", IPageLayout.LEFT, (float) 0.26, editorArea);
		left.addView(JavaUI.ID_PACKAGES);
		left.addView("br.ufscar.dc.rejasp.views.IndicationTree");
	}
}
