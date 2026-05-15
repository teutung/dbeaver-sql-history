package org.jkiss.dbeaver.sql.history;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.program.Program;
import org.jkiss.dbeaver.Log;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OpenHistoryHandler extends AbstractHandler {

    private static final Log log = Log.getLog(OpenHistoryHandler.class);

    private static final String HTML_FILE = "SQL Historys.html";

    private static final String HTML_TEMPLATE = """
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
          table { border-collapse: collapse; width: 100%%; font-family: monospace; font-size: 12px; }
          th, td { border: 1px solid #ccc; padding: 4px 8px; text-align: left; vertical-align: top; }
          th { background: #f0f0f0; position: sticky; top: 0; white-space: nowrap; }
          tr:nth-child(even) { background: #fafafa; }
          td.sql-text { white-space: pre-wrap; word-break: break-all; max-width: 600px; }
          td.nowrap { white-space: nowrap; }
          td.max-col { white-space: nowrap; max-width: 180px; overflow: hidden; text-overflow: ellipsis; }
        </style>
        </head>
        <body>
        <table>
        <tr><th>Time</th><th>Datasources</th><th>Category</th><th>SQL Text</th><th>Duration (ms)</th><th>Rows</th></tr>
        %s
        </table>
        </body>
        </html>
        """;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            List<String> rows = SQLHistoryManager.get().readAllRows();
            String body = rows.isEmpty() ? "" : String.join("\n", rows);
            String html = String.format(HTML_TEMPLATE, body);
            Path txtFile = SQLHistoryManager.getHistoryFile();
            Path htmlFile = txtFile.getParent().resolve(HTML_FILE).toAbsolutePath().normalize();
            Files.writeString(htmlFile, html, StandardCharsets.UTF_8);
            Program.launch(htmlFile.toString());
        } catch (Exception e) {
            log.error("Failed to open SQL History in browser", e);
        }
        return null;
    }
}
