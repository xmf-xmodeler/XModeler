package com.ceteva.diagram;

import java.util.Vector;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import XOS.Message;
import XOS.Value;

import com.ceteva.diagram.action.CtrlPressedAction;
import com.ceteva.diagram.action.CtrlReleasedAction;
import com.ceteva.diagram.action.PrintAction;
import com.ceteva.diagram.dnd.DropTargetListener;
import com.ceteva.diagram.editPart.ConnectionLayerManager;
import com.ceteva.diagram.editPart.GraphicalPartFactory;
import com.ceteva.diagram.editPart.RootEditPart;
import com.ceteva.diagram.model.AbstractDiagram;
import com.ceteva.diagram.model.PaletteTool;
import com.ceteva.diagram.model.PaletteToolGroup;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.menus.MenuBuilder;

public class Diagram extends GraphicalEditorWithFlyoutPalette implements IPropertyChangeListener, IPartListener2 {

    public class OverviewOutlinePage extends Page implements IContentOutlinePage {

        private Canvas       overview;

        private RootEditPart rootEditPart;

        private Thumbnail    thumbnail;
        
        private LightweightSystem lws;
        
        public OverviewOutlinePage(RootEditPart rootEditPart) {
            super();
            this.rootEditPart = rootEditPart;
        }

        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        }

        public void createControl(Composite parent) {
            overview = new Canvas(parent, SWT.NONE);
            lws = new LightweightSystem(overview);
            thumbnail = new ScrollableThumbnail((Viewport) rootEditPart.getFigure());
            thumbnail.setBorder(new MarginBorder(3));
            thumbnail.setSource(rootEditPart.getLayer(LayerConstants.PRINTABLE_LAYERS));
            lws.setContents(thumbnail);
        }
        
        public void changeEditPart(RootEditPart rootEditPart) {
            thumbnail = new ScrollableThumbnail((Viewport)rootEditPart.getFigure());
            thumbnail.setSource(rootEditPart.getLayer(LayerConstants.PRINTABLE_LAYERS));
            lws.setContents(thumbnail);
            //rootEditPart.getViewer().addDropTargetListener(new DropTargetListener(rootEditPart.getViewer(),TextTransfer.getInstance()));
        }

        public void dispose() {
            if (null != thumbnail)
                thumbnail.deactivate();
            super.dispose();
        }

        public void disable() {
            thumbnail.getUpdateManager().removeUpdateListener(thumbnail);
        }

        public void enable() {
            thumbnail.getUpdateManager().addUpdateListener(thumbnail);
        }

        public Control getControl() {
            return overview;
        }

        public ISelection getSelection() {
            return StructuredSelection.EMPTY;
        }

        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        }

        public void setActive(boolean active) {
            thumbnail.setEnabled(active);
        }

        public void setFocus() {
            if (getControl() != null)
                getControl().setFocus();
        }

        public void setSelection(ISelection selection) {
        }

        public Object getAdapter(Class type) {
            if (type == ZoomManager.class)
                return ((ScalableFreeformRootEditPart) getGraphicalViewer().getRootEditPart()).getZoomManager();
            return null;
        }
    }

    public static boolean             antialias        = true;
    com.ceteva.diagram.model.Diagram diagram;
    PaletteRoot                       root;
    OverviewOutlinePage               overviewOutlinePage;
    KeyHandler                        sharedKeyHandler;
    ZoomManager						  zoomManager;
    DropTargetListener				  dropTargetListener;

    public Diagram() {
        setEditDomain(new DefaultEditDomain(this));
        registerAsListener();
    }
    
    public void activate(boolean activate) {
    	GraphicalViewer viewer = getGraphicalViewer();
    	EditPart ep = (EditPart)viewer.getRootEditPart();
    	if(activate)
    	  ep.activate();
    	else
    	  ep.deactivate();
    }

    public void delete() {
        DiagramPlugin diagramManager = DiagramPlugin.getDefault();
        IWorkbenchPage page = diagramManager.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        page.closeEditor(this, false);
    }

    public com.ceteva.diagram.model.Diagram getRootModel() {
        return diagram;
    }

    public void setName(String name) {
        setPartName(name);
    }
    
    public ConnectionLayerManager getConnectionManager() {
    	return diagram.getConnectionManager();
    }
    
    public AbstractDiagram getDisplayedDiagram() {
    	return diagram.getDisplayedDiagram();
    }

    public String getName() {
        return getTitle();
    }

    public String getTitleToolTip() {
        return getTitle();
    }

    public String getIdentity() {
        return getDisplayedDiagram().getIdentity();
    }

    public Value processCall(Message message) {
        return null;
    }
    
    public void clearToolPalette() {
    	Palette.clearPalette(getPaletteRoot());
        this.createPalettePage();
        this.createPaletteViewerProvider();
    }
    
    public void newToolGroup(String name) {
        Palette.addDrawer(getPaletteRoot(), name);
    }
    
    public void newTool(String parent,String label,String identity,boolean connection,String icon) {
        Palette.addEntry(getPaletteRoot(), parent, label, identity, icon, connection);
    }
    
    public void buildPalette() {
    	Vector toolGroups = diagram.getToolGroups();
    	for(int i=0;i<toolGroups.size();i++) {
    		PaletteToolGroup ptg = (PaletteToolGroup)toolGroups.elementAt(i);
    		newToolGroup(ptg.name);
    		for(int z=0;z<ptg.tools.size();z++) {
    		  PaletteTool pt = (PaletteTool)ptg.tools.elementAt(z);
    		  newTool(ptg.name,pt.name,pt.identity,pt.connection,pt.icon);
    		}
    	}
    }

    public void setImage(Image icon) {
        setTitleImage(icon);
    }

    public void init(IEditorSite iSite, IEditorInput iInput) throws PartInitException {
        super.init(iSite, iInput);
        setSite(iSite);
        setInput(iInput);

        if (iInput instanceof DiagramEditorInput) {
            DiagramEditorInput input = (DiagramEditorInput) iInput;
            diagram = input.getModel();
        	diagram.setOwner(this);
        	setName(diagram.getName());
        }
    }

    protected void initializeGraphicalViewer() {
        getGraphicalViewer().setContents(diagram.getDisplayedDiagram());
        dropTargetListener = new DropTargetListener(getGraphicalViewer(),TextTransfer.getInstance());
        getGraphicalViewer().addDropTargetListener(dropTargetListener);
    }

    public void setDroppable() {
    	setDroppable(true);
    }
    
    public void setDroppable(boolean enabled) {
    	dropTargetListener.setEnabled(enabled);
    }    
    
    public boolean isSaveAsAllowed() {
        return false;
    }

    public void doSave(IProgressMonitor iMonitor) {
    }

    public void doSaveAs() {
    }

    public void gotoMarker(IMarker iMarker) {
    }

    public boolean isDirty() {
        return false;
    }

    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        GraphicalViewer viewer = getGraphicalViewer();
        RootEditPart editPartRoot = new RootEditPart();
        viewer.setRootEditPart(editPartRoot);
        viewer.addSelectionChangedListener(new DiagramMenuBuilder(viewer, getSite()));
        zoomManager = editPartRoot.getZoomManager();
        viewer.setEditPartFactory(new GraphicalPartFactory());
        viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer).setParent(getCommonKeyHandler()));
        ((FigureCanvas) viewer.getControl()).setScrollBarVisibility(FigureCanvas.ALWAYS);
        getConnectionManager().setPrintableLayers(editPartRoot.getPrintableLayers());
    }
    
    public void setViewerModel(AbstractDiagram newDiagram) {
		GraphicalViewer viewer = getGraphicalViewer();
		diagram.setDisplayedDiagram(newDiagram);
		RootEditPart rootep = (RootEditPart) viewer.getRootEditPart();

		// deactivate the current edit parts hierarchy

		rootep.deactivate();
		rootep.setParent(null);

		// create a new editpart hierarchy at the appropriate zoom level

		viewer.getEditPartRegistry().clear();
		rootep = new RootEditPart();
		viewer.setRootEditPart(rootep);
		viewer.addSelectionChangedListener(new DiagramMenuBuilder(viewer, getSite()));
		ConnectionLayerManager clm = getConnectionManager();
		clm.reset();
		clm.setPrintableLayers(rootep.getPrintableLayers());
		viewer.setContents(newDiagram);
		overviewOutlinePage.changeEditPart(rootep);

		// update the zoom manager

		zoomManager = rootep.getZoomManager();
				
		IWorkbenchPage page = DiagramPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
		  page.showView("org.eclipse.ui.views.ContentOutline");
		} catch (PartInitException e) {
		  e.printStackTrace();
		}
		page.activate(this);
	}

    public void createActions() {
        ActionRegistry registry = getActionRegistry();
        PrintAction print = new PrintAction(this);
        getSelectionActions().add(print.getId());
        registry.registerAction(print);
    } 

    protected PaletteRoot getPaletteRoot() {
        if (root == null)
            root = Palette.createPalette();
        return root;
    }
    
    public com.ceteva.diagram.model.Diagram getDiagramModel() {
        return diagram;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IContentOutlinePage.class)
            return getOverviewOutlinePage();
        if (adapter == ZoomManager.class)
        	return getZoomManager();
        return super.getAdapter(adapter);
    }

    protected OverviewOutlinePage getOverviewOutlinePage() {
        if (null == overviewOutlinePage && null != getGraphicalViewer()) {
            RootEditPart rootep = (RootEditPart)getGraphicalViewer().getRootEditPart();
            if (rootep instanceof ScalableFreeformRootEditPart)
                overviewOutlinePage = new OverviewOutlinePage(rootep);
        }
        return overviewOutlinePage;
    }
    
    public ZoomManager getZoomManager() {
    	return zoomManager;
    }

    public void propertyChange(PropertyChangeEvent event) {
        diagram.preferenceChange();
        try{
        RootEditPart rootEditPart = (RootEditPart)getGraphicalViewer().getRootEditPart();
        rootEditPart.preferenceUpdate();
        }catch(NullPointerException npe){
        	System.err.println("Move to ne propery cange system in eclipse 4.0!");
        }
    }

    protected KeyHandler getCommonKeyHandler() {
        if (sharedKeyHandler == null) {
            sharedKeyHandler = new KeyHandler();
            sharedKeyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0), getActionRegistry().getAction(ActionFactory.DELETE.getId()));
            sharedKeyHandler.put(KeyStroke.getPressed(SWT.CTRL, 0), new CtrlPressedAction());
            sharedKeyHandler.put(KeyStroke.getReleased(SWT.CTRL, SWT.CTRL), new CtrlReleasedAction());
            sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0), getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));        }
        return sharedKeyHandler;
    }

    public void dispose() {
    	diagram.close();
        removeListener();
        diagram.dispose();
        MenuBuilder.dispose(getSite());
        getConnectionManager().resetPrintableLayers();
        super.dispose();
    }

    public void registerAsListener() {
    	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      	page.addPartListener(this);
        IPreferenceStore preference = DiagramPlugin.getDefault().getPreferenceStore();
        preference.addPropertyChangeListener(this);
    }

    public void removeListener() {
        getSite().getPage().removePartListener(this);
        IPreferenceStore preference = DiagramPlugin.getDefault().getPreferenceStore();
        preference.removePropertyChangeListener(this);
    }

    public void partActivated(IWorkbenchPartReference ref) {
        if (ref.getPart(false).equals(this) && diagram.handler != null)
          raiseFocusGained();
    }

    public void partBroughtToTop(IWorkbenchPartReference ref) {
    }

    public void partClosed(IWorkbenchPartReference ref) {
    }

    public void partDeactivated(IWorkbenchPartReference ref) {
    }

    public void partHidden(IWorkbenchPartReference ref) {
    }

    public void partInputChanged(IWorkbenchPartReference ref) {
    }

    public void partOpened(IWorkbenchPartReference ref) {
    }

    public void partVisible(IWorkbenchPartReference ref) {
    }
    
    public void raiseFocusGained() {
    	Message m = diagram.handler.newMessage("focusGained", 1);
        Value v1 = new Value(getIdentity());
        m.args[0] = v1;
        diagram.handler.raiseEvent(m);
    }

    protected FlyoutPreferences getPalettePreferences() {
        return new FlyoutPreferences() {
            public int getDockLocation() {
                return DiagramPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.DOCK_LOCATION);
            }

            public int getPaletteState() {
                return DiagramPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.PALETTE_STATE);
            }

            public int getPaletteWidth() {
                return DiagramPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.PALETTE_WIDTH);
            }

            public void setDockLocation(int location) {
                DiagramPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.DOCK_LOCATION, location);
            }

            public void setPaletteState(int state) {
                DiagramPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.PALETTE_STATE, state);
            }

            public void setPaletteWidth(int width) {
                DiagramPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.PALETTE_WIDTH, width);
            }
        };
    }

    protected CustomPalettePage createPalettePage() {
        return new CustomPalettePage(getPaletteViewerProvider()) {
            public void init(IPageSite pageSite) {
                super.init(pageSite);
                IAction copy = getActionRegistry().getAction(ActionFactory.COPY.getId());
                pageSite.getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copy);
            }
        };
    }

    protected PaletteViewerProvider createPaletteViewerProvider() {
        return new PaletteViewerProvider(getEditDomain()) {
            protected void configurePaletteViewer(PaletteViewer viewer) {
                super.configurePaletteViewer(viewer);
            }

            protected void hookPaletteViewer(PaletteViewer viewer) {
                super.hookPaletteViewer(viewer);
            }
        };
    }

}