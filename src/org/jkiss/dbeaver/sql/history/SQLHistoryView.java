package org.jkiss.dbeaver.sql.history;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIIcon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SQLHistoryView extends ViewPart {

    public static final String VIEW_ID = "org.jkiss.dbeaver.sql.history.SQLHistoryView";

    private static final String[] COLUMNS = {"Time", "Datasources", "Category", "SQL Text", "Duration (ms)", "Rows"};
    private static final int[] WIDTHS = {150, 200, 70, 350, 90, 65};
    private static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private TableViewer viewer;
    private String filter;

    @Override
    public void createPartControl(Composite parent) {
        Table table = new Table(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        for (int i = 0; i < COLUMNS.length; i++) {
            TableColumn tc = new TableColumn(table, SWT.LEFT);
            tc.setText(COLUMNS[i]); tc.setWidth(WIDTHS[i]); tc.setMoveable(true);
        }

        viewer = new TableViewer(table);
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new LP());
        viewer.addDoubleClickListener(e -> {
            if (e.getSelection() instanceof IStructuredSelection s && !s.isEmpty())
                showSql((SQLHistoryEntry) s.getFirstElement());
        });

        fillToolBar();
        fillContextMenu();
        refresh();
    }

    private void fillToolBar() {
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        Action fa = new Action("Filter: All") {
            @Override
            public void run() {
                String[] cats = {null, "DQL", "DML", "DDL", "DCL", "TCL", "UNKNOWN"};
                int idx = 0;
                for (int i = 0; i < cats.length; i++)
                    if (java.util.Objects.equals(cats[i], filter)) { idx = i; break; }
                idx = (idx + 1) % cats.length;
                filter = cats[idx];
                setText(filter == null ? "Filter: All" : "Filter: " + filter);
                refresh();
            }
        };
        fa.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
        mgr.add(fa);

        Action ca = new Action("Clear History") {
            @Override
            public void run() {
                MessageDialog d = new MessageDialog(getSite().getShell(), "Clear SQL History", null,
                    "Clear in-memory history? HTML file is preserved.", MessageDialog.CONFIRM,
                    new String[]{IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 1);
                if (d.open() == IDialogConstants.YES_ID) { SQLHistoryManager.get().clearHistory(); refresh(); }
            }
        };
        ca.setImageDescriptor(DBeaverIcons.getImageDescriptor(UIIcon.CLOSE));
        mgr.add(ca);
    }

    private void fillContextMenu() {
        MenuManager mm = new MenuManager();
        mm.setRemoveAllWhenShown(true);
        mm.addMenuListener(m -> {
            if (viewer.getSelection() instanceof IStructuredSelection s && !s.isEmpty()) {
                SQLHistoryEntry e = (SQLHistoryEntry) s.getFirstElement();
                m.add(new Action("Copy SQL") { public void run() { clip(e.getSqlText()); }});
                m.add(new Action("View Full SQL") { public void run() { showSql(e); }});
            }
        });
        viewer.getTable().setMenu(mm.createContextMenu(viewer.getTable()));
    }

    private void refresh() {
        viewer.setInput(filter == null ? SQLHistoryManager.get().getEntries() : SQLHistoryManager.get().getEntries(filter));
    }

    private void showSql(SQLHistoryEntry e) {
        new MessageDialog(getSite().getShell(), "SQL - " + e.getQueryCategory(), null,
            e.getSqlText(), MessageDialog.NONE, new String[]{IDialogConstants.CLOSE_LABEL}, 0).open();
    }

    private static void clip(String t) {
        Clipboard cb = new Clipboard(Display.getDefault());
        cb.setContents(new Object[]{t}, new Transfer[]{TextTransfer.getInstance()});
        cb.dispose();
    }

    @Override
    public void setFocus() { viewer.getTable().setFocus(); }

    private static String truncate(String t, int max) {
        if (t == null) return "";
        String s = t.replace('\n', ' ').replace('\r', ' ').trim();
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private static class LP extends LabelProvider implements ITableLabelProvider {
        @Override public org.eclipse.swt.graphics.Image getColumnImage(Object el, int col) { return null; }
        @Override
        public String getColumnText(Object el, int col) {
            if (!(el instanceof SQLHistoryEntry e)) return "";
            return switch (col) {
                case 0 -> FMT.format(new Date(e.getTimestamp()));
                case 1 -> {
                    String s = e.getSchedulers();
                    yield s.isEmpty() ? e.getDataSourceName() : s + "@" + e.getDataSourceName();
                }
                case 2 -> e.getQueryCategory();
                case 3 -> truncate(e.getSqlText(), 120);
                case 4 -> String.valueOf(e.getDuration());
                case 5 -> String.valueOf(e.getRowCount());
                default -> "";
            };
        }
    }
}
