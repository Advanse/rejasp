package br.ufscar.dc.rejasp.views;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import br.ufscar.dc.rejasp.plugin.Plugin;


class ViewLabelProvider extends LabelProvider {

	private Map imageCache = new HashMap(20);
	
	public String getText(Object obj) {
		return obj.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 * Each element in the model of indication tree has a type. This type is used 
	 * associate each element type to a different image.
	 */
	public Image getImage(Object obj) {
		String imageKey = null;			
		if (obj instanceof TreeParent) {
			TreeParent parent = (TreeParent)obj;
			
			if (parent.getType().compareTo(IndicationTree.PROJECT) == 0) {
				imageKey = org.eclipse.ui.ISharedImages.IMG_OBJ_PROJECT;
				return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			}
			//else if(parent.getType().compareTo(IndicationTree.OBJECT) == 0) {
			//	imageKey = org.eclipse.ui.ISharedImages.IMG_OBJ_ELEMENT;
			//	return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			//}
			//else if (parent.getType().compareTo(IndicationTree.ASPECT) == 0) {
			//	imageKey = org.eclipse.ui.ISharedImages.IMG_OBJ_ELEMENT;
			//	return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			//}
			else if (parent.getType().compareTo(IndicationTree.PACKAGE) == 0)
				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE;
			else if (parent.getType().compareTo(IndicationTree.FILE) == 0)
				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CUNIT;
			else if (parent.getType().compareTo(IndicationTree.CLASS) == 0)
				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CLASS;
			else if (parent.getType().compareTo(IndicationTree.METHOD_PRIVATE) == 0)
				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PRIVATE;
			else if (parent.getType().compareTo(IndicationTree.METHOD_PUBLIC) == 0)
				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PUBLIC;
			else if (parent.getType().compareTo(IndicationTree.METHOD_PROTECTED) == 0)
				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PROTECTED;
			else if (parent.getType().compareTo(IndicationTree.ASPECT_FOLDER) == 0) {
				imageKey = org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER;
				return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			}
			//else if (parent.getType().compareTo(IndicationTree.ASPECT_FILE) == 0) {
			//	imageKey = org.eclipse.ui.ISharedImages.IMG_OBJ_FILE;
			//	return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			//}
			else if (parent.getType().compareTo(IndicationTree.ASPECT_MODULE) == 0) {
				imageKey = org.eclipse.ui.ISharedImages.IMG_OBJ_ELEMENT;
				return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			}
			else if (parent.getType().compareTo(IndicationTree.ASPECT_ADVICE) == 0) {
				imageKey = org.eclipse.ui.ISharedImages.IMG_OBJ_ELEMENT;
				return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			}
			else {
				return getCustomImage(parent.getType());
			}
		}
		else if ( obj instanceof TreeObject ) {
			if (((TreeObject)obj).getType().compareTo(IndicationTree.EMPTY_PACKAGE) == 0)
				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_EMPTY_PACKAGE;
			else {
				//imageKey = org.eclipse.ui.ISharedImages.IMG_OBJS_ERROR_TSK;
				//return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
				return getCustomImage(IndicationTree.ASPECT_INDICATION);
			}
		}
		return JavaUI.getSharedImages().getImage(imageKey);
	}
	
	public Image getCustomImage(String key) {
		ImageDescriptor descriptor = null;
		if( key.equals(IndicationTree.ASPECT) )
			descriptor = Plugin.getImageDescriptor("aspect_part.gif");
		else if(key.equals(IndicationTree.ASPECT_FILE))
			descriptor = Plugin.getImageDescriptor("aspect_file.gif");
		else if(key.equals(IndicationTree.OBJECT))
			descriptor = Plugin.getImageDescriptor("object.gif");
		else if(key.equals(IndicationTree.ASPECT_INDICATION))
			descriptor = Plugin.getImageDescriptor("aspect_indication.gif");
		
		//obtain the cached image corresponding to the descriptor
		Image image = (Image)imageCache.get(descriptor);
		if (image == null) {
			image = descriptor.createImage();
			imageCache.put(descriptor, image);
		}
		return image;
	}

	public void dispose() {
		for (Iterator i = imageCache.values().iterator(); i.hasNext();) {
			((Image) i.next()).dispose();
		}
		imageCache.clear();
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}
}
