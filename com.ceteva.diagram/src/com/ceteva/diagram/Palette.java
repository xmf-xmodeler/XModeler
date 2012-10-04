package com.ceteva.diagram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;

import com.ceteva.client.IconManager;

class Palette {

    static public void clearPalette(PaletteRoot root) {
        ListIterator li = root.getChildren().listIterator();
        while (li.hasNext()) {
            li.next();
            li.remove();
        }
        root.addAll(createCategories(root));
    }

    static PaletteRoot createPalette() {
        PaletteRoot logicPalette = new PaletteRoot();
        logicPalette.addAll(createCategories(logicPalette));
        return logicPalette;
    }

    static private List createCategories(PaletteRoot root) {
        List categories = new ArrayList();
        categories.add(createControlGroup(root));
        return categories;
    }

    static private PaletteContainer createControlGroup(PaletteRoot root) {
        PaletteGroup controlGroup = new PaletteGroup("Standard Tools");
        List entries = new ArrayList();
        ToolEntry tool = new SelectionToolEntry();
        entries.add(tool);
        root.setDefaultEntry(tool);
        tool = new MarqueeToolEntry();
        entries.add(tool);
        controlGroup.addAll(entries);
        return controlGroup;
    }

    static public void addDrawer(PaletteRoot root, String name) {
        PaletteDrawer drawer = new PaletteDrawer(name);
        root.add(drawer);
    }

    static public void addEntry(PaletteRoot root, String group, String label, String name, String icon, boolean connection) {
        PaletteEntry entry = addEntry(root, group, label, name, connection, icon);
        entry.setSmallIcon(IconManager.getImageDescriptor(DiagramPlugin.getDefault(), icon));
    }

    static public PaletteEntry addEntry(PaletteRoot root, String group, String label, String name, boolean connection, String icon) {
        Iterator it = root.getChildren().iterator();
        while (it.hasNext()) {
            PaletteEntry entry = (PaletteEntry) it.next();
            String identity = entry.getLabel();
            if (entry instanceof PaletteDrawer && identity.equals(group)) {
                PaletteDrawer drawer = (PaletteDrawer) entry;
                PaletteEntry newEntry;
                if (connection)
                    newEntry = new ConnectionCreationToolEntry(label, "", new ToolFactory(name,icon), null, null);
                else
                    newEntry = new CombinedTemplateCreationEntry(label, "", name, new ToolFactory(name,icon), null, null);
                // newEntry = new NodeCreationToolEntry(name,"",name,new ToolFactory(name),null,null);
                drawer.add(newEntry);
                return newEntry;
            }
        }
        return null;
    }

    static public void setNodeToolUnloadMode(PaletteRoot root, boolean mode) {
        Iterator it = root.getChildren().iterator();
        while (it.hasNext()) {
            PaletteEntry entry = (PaletteEntry) it.next();
            if (entry instanceof PaletteDrawer) {
                PaletteDrawer drawer = (PaletteDrawer) entry;
                Iterator tools = drawer.getChildren().iterator();
                while (tools.hasNext()) {
                    entry = (PaletteEntry) tools.next();
                    if (entry instanceof NodeCreationToolEntry) {
                        NodeCreationToolEntry nodeEntry = (NodeCreationToolEntry) entry;
                        nodeEntry.unloadWhenFinished(mode);
                    }
                }
            }
        }
    }
}