# DBeaver SQL History Plugin

Automatically records every successfully executed SQL statement from DBeaver SQL Editor, appends to a TXT file (O(1) I/O, never slows down), and opens in your default browser on demand.

## Features

- Records every successful SQL execution automatically — zero configuration
- Categorizes SQL as DQL / DML / DDL / TCL / UNKNOWN
- Append-only storage (`SQL Historys.txt`, never deletes historical data)
- **O(1) append** — writing never slows down regardless of history size
- View in browser: toolbar button reads TXT → generates HTML → opens default browser
- Columns: Time, Datasources, Category, SQL Text, Duration (ms), Rows

## Installation

### 1. Copy JAR to DBeaver plugins directory

```
cp dbeaver-sql-history.jar /path/to/dbeaver/plugins/
```

Example paths:
- **Windows**: `C:\Program Files\DBeaver\plugins\`
- **macOS**: `/Applications/DBeaver.app/Contents/Eclipse/plugins/`
- **Linux**: `/opt/dbeaver/plugins/`

### 2. Register the plugin

Edit `configuration/org.eclipse.equinox.simpleconfigurator/bundles.info` in your DBeaver installation directory and add this line at the end:

```
org.jkiss.dbeaver.sql.history,1.0.0.202605061703,plugins/dbeaver-sql-history.jar,4,false
```

### 3. Restart DBeaver

## Usage

| Action | How |
|--------|-----|
| Auto-record | Any SQL executed successfully in the SQL Editor is automatically recorded |
| View history | Click the **SQL History** button in the SQL editor's right sidebar toolbar |
| Browser opens | A temporary `SQL Historys.html` is generated from `SQL Historys.txt` and opened in your default browser |

The TXT and HTML files are located next to the plugin JAR (same directory as `dbeaver-sql-history.jar`).

## File Structure

| File | Description |
|------|-------------|
| `plugins/SQL Historys.txt` | Persistent append-only storage. Each line is an HTML `<tr>` row. Never deleted. |
| `plugins/SQL Historys.html` | Temporary file, generated on demand when you click the toolbar button. Overwritten each time. |

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
# From the dbeaver project root
mvn clean package -pl plugins/org.jkiss.dbeaver.sql.history -am -DenforcePlatform=false -DskipTests
```

## Uninstall

1. Delete `plugins/dbeaver-sql-history.jar`
2. Remove the corresponding line from `configuration/org.eclipse.equinox.simpleconfigurator/bundles.info`

## License

Apache License 2.0
