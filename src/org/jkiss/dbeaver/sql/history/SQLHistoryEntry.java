package org.jkiss.dbeaver.sql.history;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;

public class SQLHistoryEntry {
    private final String sqlText;
    private final String queryCategory;
    private final String dataSourceName;
    private final String schedulers;
    private final long timestamp;
    private final long duration;
    private final long rowCount;

    public SQLHistoryEntry(
        @NotNull String sqlText,
        @NotNull String queryCategory,
        @Nullable String dataSourceName,
        @Nullable String schedulers,
        long timestamp, long duration, long rowCount
    ) {
        this.sqlText = sqlText;
        this.queryCategory = queryCategory;
        this.dataSourceName = dataSourceName != null ? dataSourceName : "";
        this.schedulers = schedulers != null ? schedulers : "";
        this.timestamp = timestamp;
        this.duration = duration;
        this.rowCount = rowCount;
    }

    @NotNull public String getSqlText() { return sqlText; }
    @NotNull public String getQueryCategory() { return queryCategory; }
    @NotNull public String getDataSourceName() { return dataSourceName; }
    @NotNull public String getSchedulers() { return schedulers; }
    public long getTimestamp() { return timestamp; }
    public long getDuration() { return duration; }
    public long getRowCount() { return rowCount; }
}
