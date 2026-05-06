package org.jkiss.dbeaver.sql.history;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.model.sql.SQLQuery;
import org.jkiss.dbeaver.model.sql.SQLQueryResult;
import org.jkiss.dbeaver.model.sql.SQLQueryType;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorListenerDefault;
import org.jkiss.dbeaver.ui.editors.sql.addins.SQLEditorAddIn;

import java.io.PrintWriter;

public class SQLHistoryAddIn implements SQLEditorAddIn {

    private SQLEditor editor;
    private final SQLEditorListenerDefault listener = new SQLEditorListenerDefault() {
        @Override
        public void onQueryResult(@NotNull DBPPreferenceStore ps, @NotNull SQLQueryResult r) {
            if (r.hasError()) return;
            SQLQuery q = r.getStatement();
            if (q == null) return;
            String sql = q.getOriginalText();
            if (sql == null || sql.isBlank()) return;
            sql = sql.trim();

            String schedulers = "", dsName = "";
            DBPDataSourceContainer c = editor.getDataSourceContainer();
            if (c != null) {
                dsName = c.getName();
                DBPConnectionConfiguration cfg = c.getConnectionConfiguration();
                if (cfg != null) {
                    String user = cfg.getUserName();
                    String db = cfg.getDatabaseName();
                    schedulers = (user != null ? user : "") + (db != null ? "(" + db + ")" : "");
                }
            }

            SQLHistoryManager.get().addEntry(new SQLHistoryEntry(
                sql, category(q.getType()), dsName, schedulers,
                System.currentTimeMillis(), r.getQueryTime(), rows(r)
            ));
        }
    };

    @Override
    public void init(@NotNull SQLEditor editor) {
        this.editor = editor;
        this.editor.addListener(listener);
    }

    @Override
    public void cleanup(@NotNull SQLEditor editor) {
        this.editor.removeListener(listener);
        this.editor = null;
    }

    @Override
    public PrintWriter getServerOutputConsumer() { return null; }

    private static String category(SQLQueryType t) {
        if (t == null) return "UNKNOWN";
        return switch (t) {
            case SELECT -> "DQL";
            case INSERT, UPDATE, DELETE, MERGE -> "DML";
            case DDL, USE -> "DDL";
            case COMMIT, ROLLBACK -> "TCL";
            case UNKNOWN -> "UNKNOWN";
        };
    }

    private static long rows(SQLQueryResult r) {
        long n = 0;
        for (SQLQueryResult.ExecuteResult e : r.getExecuteResults()) {
            if (e.getRowCount() != null) n += e.getRowCount();
            else if (e.getUpdateCount() != null) n += e.getUpdateCount();
        }
        return n;
    }
}
