package tool;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * データベース接続確認ツールのメインクラスです。
 * <p>
 * コマンドライン引数を解析し、各種リレーショナルデータベース（Oracle、MySQL、MariaDB、SQL Server、PostgreSQLなど）への
 * 接続テスト、SQLクエリ実行、および結果出力を実行します。
 * </p>
 */
public class ChkDbConn {

	/** 正常終了時の終了コード */
	public static final int EXIT_CODE_SUCCESS = 0;

	/** 引数エラーまたはヘルプ表示時の終了コード */
	public static final int EXIT_CODE_USAGE = 1;

	/** 接続・SQL実行エラー時の終了コード */
	public static final int EXIT_CODE_ERROR = 20;

	/** デフォルトの接続先ホスト名 */
	private static final String DEFAULT_HOST = "localhost";

	/** PostgreSQLのデフォルトデータベース名 */
	private static final String DEFAULT_PGSQL_DB = "postgres";

	/** Oracle用のデフォルトSQL文 */
	private static final String DEFAULT_SQL_ORACLE = "SELECT sysdate FROM dual";

	/** 標準的なデータベース用のデフォルトSQL文 */
	private static final String DEFAULT_SQL_STANDARD = "SELECT CURRENT_TIMESTAMP";

	/** Oracleのデフォルトポート番号 */
	private static final int DEFAULT_PORT_ORACLE = 1521;

	/** MySQLのデフォルトポート番号 */
	private static final int DEFAULT_PORT_MYSQL = 3306;

	/** MariaDBのデフォルトポート番号 */
	private static final int DEFAULT_PORT_MARIADB = 3306;

	/** SQL Serverのデフォルトポート番号 */
	private static final int DEFAULT_PORT_MSSQL = 1433;

	/** PostgreSQLのデフォルトポート番号 */
	private static final int DEFAULT_PORT_PGSQL = 5432;

	/** 冗長レベルの基本値 */
	private static final int DEFAULT_VERBOSE_LEVEL = 1;

	/**
	 * アプリケーションのメインメソッド（エントリポイント）です。
	 * <p>
	 * コマンドライン引数を受け取り、インスタンス化を行ってデータベース接続確認処理を実行します。
	 * </p>
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * // コマンドラインからの起動例:
	 * // java -jar chkdbconn.jar -t mysql -h localhost -u root -p pass -d mydb
	 * ChkDbConn.main(new String[]{"-t", "mysql", "-h", "localhost", "-u", "root", "-p", "pass", "-d", "mydb"});
	 * }</pre>
	 * </p>
	 *
	 * @param args コマンドライン引数の配列
	 */
	public static void main(final String[] args) {
		new ChkDbConn(args);
	}

	/**
	 * デフォルトコンストラクタです。
	 * <p>
	 * ユーティリティメソッド等の利用や個別インスタンス生成時に使用します。
	 * </p>
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ChkDbConn app = new ChkDbConn();
	 * int port = app.parseInt("3306", 1521);
	 * }</pre>
	 * </p>
	 */
	public ChkDbConn() {
	}

	/**
	 * 引数付きコンストラクタです。
	 * <p>
	 * コマンドライン引数を解析し、データベースへの接続・SQL実行・切断の一連の処理を実行します。
	 * 処理終了時に適切な終了コードでシステムを終了（{@link System#exit(int)}）します。
	 * </p>
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * String[] args = {"-t", "pgsql", "-h", "localhost", "-u", "postgres", "-p", "pass", "-d", "postgres"};
	 * ChkDbConn app = new ChkDbConn(args);
	 * }</pre>
	 * </p>
	 *
	 * @param args コマンドライン引数の配列
	 */
	public ChkDbConn(final String[] args) {
		boolean isOk = true;
		boolean isValidArgs = true;
		int exitCode = EXIT_CODE_SUCCESS;
		String message = "";
		ClsDatabase db = new ClsDatabase();

		// GET ARGS
		for (int i = 0; i < args.length; ++i) {
			switch (args[i]) {
				case "-h":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setHostName(args[++i]);
					} else {
						isValidArgs = false;
					}
					break;
				case "-v":
					db.setVerbose(DEFAULT_VERBOSE_LEVEL);
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						try {
							db.setVerbose(Integer.parseInt(args[++i]));
						} catch (NumberFormatException ignored) {
							// ignore
						}
					}
					break;
				case "-port":
				case "-P":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						try {
							db.setPort(Integer.parseInt(args[++i]));
						} catch (NumberFormatException e) {
							System.err.println("EXCEPTION : " + e);
							isValidArgs = false;
						}
					}
					break;
				case "-t":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setDbType(args[++i]);
					}
					break;
				case "-d":
				case "-sid":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setDbName(args[++i]);
					}
					break;
				case "-u":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setUserId(args[++i]);
					}
					break;
				case "-p":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setPassword(args[++i]);
					}
					break;
				case "-sql":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setSql(args[++i]);
					}
					break;
				case "-driver":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setDbDriver(args[++i]);
					}
					break;
				case "-url":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setDbUrl(args[++i]);
					}
					break;
				case "-dbchar":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setDbCharset(args[++i]);
					}
					break;
				case "-enc":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						db.setEncoding(args[++i]);
					}
					break;
				case "-help":
				case "--help":
				case "/q":
					isValidArgs = false;
					break;
				case "-getbytes":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						String[] elements = args[++i].split(",");
						Arrays.stream(elements)
							.map(String::trim)
							.filter(s -> !s.isEmpty())
							.forEach(elem -> {
								try {
									int val = Integer.parseInt(elem);
									if (!db.getRawBytesList().contains(0) && !db.getRawBytesList().contains(val)) {
										db.getRawBytesList().add(val);
									}
								} catch (NumberFormatException ignored) {
									// ignore
								}
							});
					}
					break;
				case "-getstrconv":
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						String[] elements = args[++i].split(",");
						Arrays.stream(elements)
							.map(String::trim)
							.filter(s -> !s.isEmpty())
							.forEach(elem -> {
								try {
									int val = Integer.parseInt(elem);
									if (!db.getConvStrList().contains(0) && !db.getConvStrList().contains(val)) {
										db.getConvStrList().add(val);
									}
								} catch (NumberFormatException ignored) {
									// ignore
								}
							});
					}
					break;
				default:
					break;
			}
		}

		// CHECK ARGS
		if (isNullOrEmpty(db.getHostName())) {
			db.setHostName(DEFAULT_HOST);
		}
		if (isNullOrEmpty(db.getUserId())) {
			isValidArgs = false;
		}
		if (isNullOrEmpty(db.getPassword())) {
			db.setPassword("");
		}
		if (!isNullOrEmpty(db.getDbType())) {
			db.setDbType(db.getDbType().toLowerCase());
		}
		if (isNullOrEmpty(db.getDbType())) {
			db.setDbType("");
		}
		if (isEquals(db.getEncoding(), "NO") || isEquals(db.getEncoding(), "FALSE") || isEquals(db.getEncoding(), "OFF") || isEquals(db.getEncoding(), "NONE")) {
			db.setEncoding("");
		}

		switch (db.getDbType()) {
			case "oracle_i":
			case "oraclei":
				db.setDbType("oracle_i");
				break;
			case "oracle_s":
			case "oracles":
			case "oracle":
				db.setDbType("oracle");
				break;
			case "mysql":
				db.setDbType("mysql");
				break;
			case "mariadb":
				db.setDbType("mariadb");
				break;
			case "sqlserver":
			case "mssql":
				db.setDbType("mssql");
				break;
			case "pg":
			case "pgsql":
			case "postgresql":
				db.setDbType("pgsql");
				break;
			default:
				break;
		}

		switch (db.getDbType()) {
			case "oracle_i":
			case "oracle":
				if (db.getPort() == 0) {
					db.setPort(DEFAULT_PORT_ORACLE);
				}
				if (isNullOrEmpty(db.getDbName()) && isNullOrEmpty(db.getDbUrl())) {
					isValidArgs = false;
				}
				if (isNullOrEmpty(db.getSql())) {
					db.setSql(DEFAULT_SQL_ORACLE);
				}
				break;
			case "mysql":
				if (db.getPort() == 0) {
					db.setPort(DEFAULT_PORT_MYSQL);
				}
				if (isNullOrEmpty(db.getSql())) {
					db.setSql(DEFAULT_SQL_STANDARD);
				}
				break;
			case "mariadb":
				if (db.getPort() == 0) {
					db.setPort(DEFAULT_PORT_MARIADB);
				}
				if (isNullOrEmpty(db.getSql())) {
					db.setSql(DEFAULT_SQL_STANDARD);
				}
				break;
			case "mssql":
				if (db.getPort() == 0) {
					db.setPort(DEFAULT_PORT_MSSQL);
				}
				if (isNullOrEmpty(db.getSql())) {
					db.setSql(DEFAULT_SQL_STANDARD);
				}
				break;
			case "pgsql":
				if (db.getPort() == 0) {
					db.setPort(DEFAULT_PORT_PGSQL);
				}
				if (isNullOrEmpty(db.getDbName())) {
					db.setDbName(DEFAULT_PGSQL_DB);
				}
				if (isNullOrEmpty(db.getSql())) {
					db.setSql(DEFAULT_SQL_STANDARD);
				}
				break;
			case "":
				if (isNullOrEmpty(db.getSql())) {
					db.setSql(DEFAULT_SQL_STANDARD);
				}
				if (isNullOrEmpty(db.getDbDriver()) || isNullOrEmpty(db.getDbUrl())) {
					isValidArgs = false;
				}
				break;
			default:
				break;
		}

		// Usage
		if (!isValidArgs) {
			System.out.println("Usage java -jar chkdbconn.jar [OPTION]");
			System.out.println("");
			System.out.println("[OPTION]");
			System.out.println("  -v level         : " + db.getVerbose());
			System.out.println("  -h HostName      : " + db.getHostName());
			System.out.println("  -P|-port Port    : " + db.getPort());
			System.out.println("  -t DBMS TYPE     : oracle|mssql|mariadb|mysql|pgsql => " + db.getDbType());
			System.out.println("  -d DBNAME OR SID : " + db.getDbName());
			System.out.println("  -u Username      : " + db.getUserId());
			System.out.println("  -p Password      : " + db.getPassword());
			System.out.println("  -sql Sql         : " + db.getSql());
			System.out.println("  -driver driver   : " + db.getDbDriver());
			System.out.println("  -url conn str    : " + db.getDbUrl());
			System.out.println("  -dbchar charset  : ex. x-eucJP-Open => " + db.getDbCharset());
			System.out.println("  -enc encoding    : ex. UTF-8 => " + db.getEncoding());
			System.out.println("  -getbytes csv    : ex. 0 or 1,2,3 => " + joinIntList(db.getRawBytesList(), ","));
			System.out.println("  -getstrconv csv  : ex. 0 or 1,2,3 => " + joinIntList(db.getConvStrList(), ","));
			System.out.println("");
			System.exit(EXIT_CODE_USAGE);
		}

		System.out.println("");
		if (!isNullOrEmpty(db.getDbDriver())) {
			System.out.println("Driver     : " + db.getDbDriver());
		}
		if (!isNullOrEmpty(db.getDbUrl())) {
			System.out.println("Url        : " + db.getDbUrl());
		} else {
			System.out.println("HostName   : " + db.getHostName());
			System.out.println("Port       : " + db.getPort());
			System.out.println("DBType     : " + db.getDbType());
			System.out.println("DBName/SID : " + db.getDbName());
		}
		System.out.println("UserName   : " + db.getUserId());
		System.out.println("Password   : " + db.getPassword());
		System.out.println("Sql        : " + db.getSql());
		if (!db.getRawBytesList().isEmpty() || !db.getConvStrList().isEmpty()) {
			System.out.println("DBCharset  : " + db.getDbCharset());
			System.out.println("Encoding   : " + db.getEncoding());
		}
		System.out.println("");

		try {
			message = "CONNECT";
			System.out.println("[INFO ][ChkDbConn()] " + message + " : START");
			isOk = db.open();
			if (isOk) {
				System.out.println("[INFO ][ChkDbConn()] " + message + " : SUCCESS");
			} else {
				exitCode = EXIT_CODE_ERROR;
				System.out.println("[ERROR][ChkDbConn()] " + message + " : FAILED");
			}
			if (isOk) {
				message = "EXEC SQL";
				System.out.println("[INFO ][ChkDbConn()] " + message + " : START");
				isOk = db.execSql();
				if (isOk) {
					System.out.println("[INFO ][ChkDbConn()] " + message + " : SUCCESS");
				} else {
					exitCode = EXIT_CODE_ERROR;
					System.out.println("[ERROR][ChkDbConn()] " + message + " : FAILED");
				}
			}
			message = "DISCONNECT";
			System.out.println("[INFO ][ChkDbConn()] " + message + " : START");
			isOk = db.close();
			if (isOk) {
				System.out.println("[INFO ][ChkDbConn()] " + message + " : SUCCESS");
			} else {
				exitCode = EXIT_CODE_ERROR;
				System.out.println("[ERROR][ChkDbConn()] " + message + " : FAILED");
			}
		} catch (Exception e) {
			System.err.println("EXCEPTION : " + e);
			exitCode = EXIT_CODE_ERROR;
		}
		System.out.println("");
		System.out.println("EXIT => " + exitCode);
		System.exit(exitCode);
	}

	/**
	 * 文字列を整数値に変換します。変換に失敗した場合やnullの場合は指定されたデフォルト値を返却します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ChkDbConn app = new ChkDbConn();
	 * int port = app.parseInt("3306", 1521);        // 3306 が返る
	 * int fallback = app.parseInt("invalid", 1521); // 1521 が返る
	 * int nullVal = app.parseInt(null, 1521);       // 1521 が返る
	 * }</pre>
	 * </p>
	 *
	 * @param value 変換対象の文字列
	 * @param defaultValue 変換失敗時またはnull時に返却するデフォルト値
	 * @return 変換後の整数値、または変換失敗時のdefaultValue
	 */
	public int parseInt(final String value, final int defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException ex) {
			return defaultValue;
		}
	}

	/**
	 * 整数リストの要素を指定された区切り文字（デリミタ）で結合した文字列を返却します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ChkDbConn app = new ChkDbConn();
	 * List<Integer> list = Arrays.asList(1, 2, 3);
	 * String joined = app.joinIntList(list, ","); // "1,2,3"
	 * String empty = app.joinIntList(null, ",");  // ""
	 * }</pre>
	 * </p>
	 *
	 * @param list 結合対象の整数リスト
	 * @param delimiter 区切り文字列（nullの場合は空文字として連結）
	 * @return 結合された文字列（リストがnullまたは空の場合は空文字）
	 */
	public String joinIntList(final List<Integer> list, final String delimiter) {
		if (list == null || list.isEmpty()) {
			return "";
		}
		return list.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(delimiter != null ? delimiter : ""));
	}

	/**
	 * 文字列がnullまたは空文字（空白のみを含む場合も含む）であるかを判定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ChkDbConn app = new ChkDbConn();
	 * boolean b1 = app.isNullOrEmpty(null);    // true
	 * boolean b2 = app.isNullOrEmpty("   ");   // true
	 * boolean b3 = app.isNullOrEmpty("hello"); // false
	 * }</pre>
	 * </p>
	 *
	 * @param str 判定対象の文字列
	 * @return nullまたは空文字（空白含む）の場合はtrue、それ以外はfalse
	 */
	boolean isNullOrEmpty(final String str) {
		return str == null || str.trim().isEmpty();
	}

	/**
	 * 2つの文字列が等しいかを判定します（null安全、空白トリムおよび大文字・小文字を区別しない比較）。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ChkDbConn app = new ChkDbConn();
	 * boolean b1 = app.isEquals("UTF-8", "utf-8"); // true
	 * boolean b2 = app.isEquals(" yes ", "YES");   // true
	 * boolean b3 = app.isEquals(null, "");         // true
	 * boolean b4 = app.isEquals("abc", "def");     // false
	 * }</pre>
	 * </p>
	 *
	 * @param str1 比較対象の文字列1
	 * @param str2 比較対象の文字列2
	 * @return 両者が等しい場合はtrue、それ以外はfalse
	 */
	boolean isEquals(final String str1, final String str2) {
		String s1 = (str1 == null) ? "" : str1.trim();
		String s2 = (str2 == null) ? "" : str2.trim();
		return s1.equalsIgnoreCase(s2);
	}
}
