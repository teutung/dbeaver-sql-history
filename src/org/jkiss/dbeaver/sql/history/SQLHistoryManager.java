package org.jkiss.dbeaver.sql.history;

import org.eclipse.core.runtime.FileLocator;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.Log;
import org.osgi.framework.FrameworkUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SQLHistoryManager {

    private static final Log log = Log.getLog(SQLHistoryManager.class);

    private static final String HTML_FILE = "SQL Historys.html";

    private static final String HTML_HEADER = """
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
          table { border-collapse: collapse; width: 100%; font-family: monospace; font-size: 12px; }
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
        """;
    private static final String HTML_FOOTER = "</table>\n</body>\n</html>\n";

    private static final Pattern HTML_ROW_PATTERN = Pattern.compile(
        "<tr><td[^>]*>(.*?)</td><td[^>]*>(.*?)</td><td>(.*?)</td><td class=\"sql-text\">(.*?)</td><td>(\\d+)</td><td>(-?\\d+)</td></tr>",
        Pattern.DOTALL
    );
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SQLHistoryManager instance;

    private final CopyOnWriteArrayList<SQLHistoryEntry> entries = new CopyOnWriteArrayList<>();
    private boolean loaded = false;

    public static synchronized SQLHistoryManager get() {
        if (instance == null) instance = new SQLHistoryManager();
        return instance;
    }

    private SQLHistoryManager() {}

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        loadFromHtmlFile();
    }

    private void loadFromHtmlFile() {
        Path file = getHistoryFile();
        if (!Files.exists(file)) return;
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            int tableStart = content.indexOf("<table>");
            int tableEnd = content.lastIndexOf("</table>");
            if (tableStart < 0 || tableEnd < 0) return;
            String tableContent = content.substring(tableStart + "<table>".length(), tableEnd);

            Matcher matcher = HTML_ROW_PATTERN.matcher(tableContent);
            List<SQLHistoryEntry> loaded = new ArrayList<>();
            while (matcher.find()) {
                String datasources = unescape(matcher.group(2));
                String category = unescape(matcher.group(3));
                String sqlText = unescape(matcher.group(4));
                long duration = Long.parseLong(matcher.group(5));
                long rowCount = Long.parseLong(matcher.group(6));
                long timestamp = parseTimestamp(matcher.group(1));

                String schedulers = "";
                String dsName = datasources;
                int atIdx = datasources.indexOf('@');
                if (atIdx > 0) {
                    schedulers = datasources.substring(0, atIdx);
                    dsName = datasources.substring(atIdx + 1);
                }
                loaded.add(new SQLHistoryEntry(sqlText, category, dsName, schedulers, timestamp, duration, rowCount));
            }
            for (int i = loaded.size() - 1; i >= 0; i--) {
                entries.add(0, loaded.get(i));
            }
        } catch (Exception e) {
            log.error("Failed to load history from HTML file", e);
        }
    }

    private static long parseTimestamp(String dateStr) {
        try { return DATE_FORMAT.parse(dateStr).getTime(); }
        catch (Exception e) { return System.currentTimeMillis(); }
    }

    // ---- Public API ----

    public void addEntry(@NotNull SQLHistoryEntry entry) {
        entries.add(0, entry);
        appendToHtmlFile(entry);
    }

    @NotNull
    public List<SQLHistoryEntry> getEntries() {
        ensureLoaded();
        return new ArrayList<>(entries);
    }

    @NotNull
    public List<SQLHistoryEntry> getEntries(@Nullable String categoryFilter) {
        if (categoryFilter == null || categoryFilter.isEmpty()) return getEntries();
        ensureLoaded();
        return entries.stream()
            .filter(e -> categoryFilter.equals(e.getQueryCategory()))
            .collect(Collectors.toList());
    }

    public void clearHistory() {
        entries.clear();
    }

    public static java.io.File getHtmlFile() {
        Path p = getHistoryFile();
        return p != null ? p.toFile() : null;
    }

    // ---- HTML persistence ----

    void appendToHtmlFile(@NotNull SQLHistoryEntry entry) {
        Path file = getHistoryFile();
        try {
            boolean exists = Files.exists(file);
            if (!exists) Files.createDirectories(file.getParent());
            String content;
            if (exists) {
                content = Files.readString(file, StandardCharsets.UTF_8);
                if (content.endsWith(HTML_FOOTER))
                    content = content.substring(0, content.length() - HTML_FOOTER.length());
            } else {
                content = HTML_HEADER;
            }
            content += formatRow(entry) + "\n" + HTML_FOOTER;
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to write SQL history", e);
        }
    }

    private static String formatRow(SQLHistoryEntry entry) {
        String schedulers = entry.getSchedulers();
        String dsName = entry.getDataSourceName();
        String datasources = schedulers.isEmpty() ? dsName : schedulers + "@" + dsName;
        return "<tr><td class=\"nowrap\">" + DATE_FORMAT.format(new Date(entry.getTimestamp()))
            + "</td><td class=\"max-col\">" + e(datasources)
            + "</td><td class=\"nowrap\">" + e(entry.getQueryCategory())
            + "</td><td class=\"sql-text\">" + e(entry.getSqlText())
            + "</td><td class=\"nowrap\">" + entry.getDuration()
            + "</td><td class=\"nowrap\">" + entry.getRowCount()
            + "</td></tr>";
    }

    private static String e(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String unescape(String text) {
        if (text == null) return "";
        return text.replace("&quot;", "\"").replace("&gt;", ">").replace("&lt;", "<").replace("&amp;", "&");
    }

    static Path getHistoryFile() {
        try {
            java.io.File bundleFile = FileLocator.getBundleFileLocation(
                FrameworkUtil.getBundle(SQLHistoryManager.class)
            ).orElseThrow(() -> new IOException("Cannot resolve bundle location"));
            return bundleFile.toPath().getParent().resolve(HTML_FILE).toAbsolutePath().normalize();
        } catch (Exception e) {
            log.error("Failed to resolve history file path, fallback to user home", e);
            return Path.of(System.getProperty("user.home"), HTML_FILE);
        }
    }
}
