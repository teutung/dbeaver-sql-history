package org.jkiss.dbeaver.sql.history;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.jkiss.dbeaver.Log;

public class OpenHistoryHandler extends AbstractHandler {

    private static final Log log = Log.getLog(OpenHistoryHandler.class);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (page != null) {
                page.showView(SQLHistoryView.VIEW_ID);
            }
        } catch (PartInitException e) {
            log.error("Failed to open SQL History view", e);
        }
        return null;
    }
}
