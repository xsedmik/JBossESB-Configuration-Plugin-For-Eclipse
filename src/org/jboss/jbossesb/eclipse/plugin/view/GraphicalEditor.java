package org.jboss.jbossesb.eclipse.plugin.view;

import java.io.File;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.jboss.jbossesb.eclipse.plugin.controller.XMLElementManipulator;
import org.jboss.jbossesb.eclipse.plugin.controller.XMLException;
import org.jboss.jbossesb.eclipse.plugin.controller.XMLManipulator;
import org.jboss.jbossesb.eclipse.plugin.controller.XMLManipulatorImpl;
import org.jboss.jbossesb.eclipse.plugin.model.XMLDocument;
import org.jboss.jbossesb.eclipse.plugin.model.XMLElement;
import org.jboss.jbossesb.eclipse.plugin.view.factory.EditorPartFactory;
import org.jboss.jbossesb.eclipse.plugin.view.part.CommonObjectPart;

/**
 * Basic class of the graphical editor.
 * 
 * @author Tomas Sedmik, tomas.sedmik@gmail.com
 * @since 2012-11-24
 */
public class GraphicalEditor extends GraphicalEditorWithFlyoutPalette {

	private XMLManipulator manipulator;
	private XMLDocument document;
	
	private boolean isCorrupt = false;
	private static Logger log = Logger.getLogger(CommonObjectPart.class.getName());

	public GraphicalEditor() {
		super();
		setEditDomain(new DefaultEditDomain(this)); 
	}

	@Override
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
		try {
			getGraphicalViewer().setContents(document);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Problem during graphic initialization!", e);
		}
	}

	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		if (!isCorrupt) {
			getGraphicalViewer().setEditPartFactory(new EditorPartFactory());
		}
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		return new GraphicalEditorPalette();
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		try {
			manipulator.saveConfiguration(document);
		} catch (XMLException e) {
			log.log(Level.SEVERE, "Problem during saving occured!", e);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		IFile ifile = (IFile) input.getAdapter(IFile.class);
		String path = ifile.getRawLocation().toOSString();
		manipulator = new XMLManipulatorImpl(new File(path));
		try {
			document = manipulator.loadConfiguration();
		} catch (XMLException e) {
			MessageDialog.openError(new Shell(), "Error", "The File is corrupted (it isn't well formed JBoss ESB configuration file.)");
			log.log(Level.INFO, "Loaded file is corrupt (isn't a well formed JBoss ESB Configuration file)!");
		}
		setInitialPosition(document);
	}
	
	/**
	  * Fire a property change and call super implementation.
	  */
	@Override public void commandStackChanged(EventObject event) {
		// FIXME need to save only if some data changed
		firePropertyChange(PROP_DIRTY);
		super.commandStackChanged(event);
	}
	
	/**
	 * Method sets initial position in the editor.
	 * 
	 * @param doc Object model of the JBoss ESB configuration file.
	 */
	private void setInitialPosition(XMLDocument doc) {
		
		int start = 40;
		if (doc.getProviders() != null) {
			for (XMLElement elem : doc.getProviders()) {
				int height = 28 + XMLElementManipulator.getBuses(elem).size() * 28;
				Rectangle layout = new Rectangle(2, start, 300, height);
				start = start + height + 8;
				elem.setRectangle(layout);
			}
		}
		
		
		start = 40;
		if (doc.getServices() != null) {
			for (XMLElement elem : doc.getServices()) {
				//TODO set correct height (how many actions) 
				int height = 50;
				Rectangle layout = new Rectangle(400, start, 200, height);
				start = start + height + 15;
				elem.setRectangle(layout);
			}
		}
	}
}
