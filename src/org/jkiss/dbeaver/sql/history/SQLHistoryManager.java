package org.jkiss.dbeaver.sql.history;

import org.eclipse.core.runtime.FileLocator;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.Log;
import org.osgi.framework.FrameworkUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class SQLHistoryManager {

    private static final Log log = Log.getLog(SQLHistoryManager.class);

    private static final String TXT_FILE = "SQL Historys.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SQLHistoryManager instance;

    public static synchronized SQLHistoryManager get() {
        if (instance == null) instance = new SQLHistoryManager();
        return instance;
    }

    private SQLHistoryManager() {}

    public void addEntry(@NotNull SQLHistoryEntry entry) {
        Path file = getHistoryFile();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, formatRow(entry) + "\n", StandardCharsets.UTF_8,
                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            log.error("Failed to write SQL history", e);
        }
    }

    @NotNull
    public List<String> readAllRows() {
        Path file = getHistoryFile();
        if (!Files.exists(file)) return Collections.emptyList();
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read SQL history", e);
            return Collections.emptyList();
        }
    }

    static String formatRow(SQLHistoryEntry entry) {
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

    static Path getHistoryFile() {
        try {
            java.io.File bundleFile = FileLocator.getBundleFileLocation(
                FrameworkUtil.getBundle(SQLHistoryManager.class)
            ).orElseThrow(() -> new IOException("Cannot resolve bundle location"));
            return bundleFile.toPath().getParent().resolve(TXT_FILE).toAbsolutePath().normalize();
        } catch (Exception e) {
            log.error("Failed to resolve history file path, fallback to user home", e);
            return Path.of(System.getProperty("user.home"), TXT_FILE);
        }
    }
}
