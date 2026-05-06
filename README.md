# DBeaver SQL History Plugin

Automatically records successfully executed SQL statements from DBeaver SQL Editor, categorizes them by type, and persists to an HTML file.

## Features

- Records every successful SQL execution automatically
- Categorizes SQL as DQL / DML / DDL / TCL
- Dedicated SQL History view with category filtering (DQL/DML/DDL/DCL/TCL/All)
- Right-click context menu: Copy SQL / View Full SQL
- Double-click to view full SQL in dialog
- HTML audit log saved next to the plugin JAR (`SQL Historys.html`)
- Append-only audit log (never deletes historical data)
- SQL text preserves original formatting (line breaks, indentation)
- Columns: Time, Datasources, Category, SQL Text, Duration (ms), Rows

## Installation

### 1. Download the JAR

Download `dbeaver-sql-history.jar` from the [releases page](../../releases) or build it yourself.

### 2. Copy to DBeaver plugins directory

```
cp dbeaver-sql-history.jar /path/to/dbeaver/plugins/
```

Example paths:
- **Windows**: `C:\Program Files\DBeaver\plugins\`
- **macOS**: `/Applications/DBeaver.app/Contents/Eclipse/plugins/`
- **Linux**: `/opt/dbeaver/plugins/`

### 3. Register the plugin

Edit the file `configuration/org.eclipse.equinox.simpleconfigurator/bundles.info` in your DBeaver installation directory and add the following line at the end:

```
org.jkiss.dbeaver.sql.history,1.0.0.202605061703,plugins/dbeaver-sql-history.jar,4,false
```

### 4. Restart DBeaver

### 5. Open the view

**Window → Show View → Other → DBeaver → SQL History**

Or click the **SQL History** button in the SQL editor's right sidebar toolbar.

## Usage

| Action | How |
|--------|-----|
| Auto-record | SQL executed successfully in the SQL Editor is automatically recorded |
| View history | Open **SQL History** view via Window → Show View or toolbar button |
| Filter by type | Click **Filter: All** toolbar button to cycle through categories |
| Clear in-memory | Click **Clear History** toolbar button |
| Copy SQL | Right-click a row → **Copy SQL** |
| View full SQL | Right-click a row → **View Full SQL** (or double-click) |
| HTML audit log | All history is also saved to `plugins/SQL Historys.html` |

## Columns

| Column | Description |
|--------|-------------|
| Time | Execution timestamp (`yyyy-MM-dd HH:mm:ss`) |
| Datasources | `user(database)@datasource` format (e.g. `scott(FREEPDB1)@localhost-scott`) |
| Category | SQL type: DQL, DML, DDL, TCL, UNKNOWN |
| SQL Text | The SQL statement with original formatting preserved |
| Duration (ms) | Query execution time in milliseconds |
| Rows | Number of rows affected / returned |

## Build

Requires OpenJDK 21+ and the DBeaver SDK (parent POM + required plugins).

```bash
# From the dbeaver project root (after setting up dbeaver-common)
mvn install -pl plugins/org.jkiss.dbeaver.sql.history -am
```

The JAR will be in:
```
plugins/org.jkiss.dbeaver.sql.history/target/org.jkiss.dbeaver.sql.history-1.0.0-SNAPSHOT.jar
```

## Uninstall

1. Delete `plugins/dbeaver-sql-history.jar`
2. Remove the corresponding line from `configuration/org.eclipse.equinox.simpleconfigurator/bundles.info`

## License

Apache License 2.0
