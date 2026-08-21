package tool;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * データベース接続およびSQLクエリ実行を管理するクラスです。
 * <p>
 * 各種DBMS（Oracle、MySQL、MariaDB、SQL Server、PostgreSQL）への接続確立、
 * SQL文の実行、結果セットの標準出力、文字コード変換およびRAWバイト取得などの制御を行います。
 * </p>
 */
public class ClsDatabase {

	/** Oracle用JDBCドライバークラス名 */
	public static final String DRIVER_ORACLE = "oracle.jdbc.OracleDriver";

	/** MySQL用JDBCドライバークラス名 */
	public static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";

	/** MariaDB用JDBCドライバークラス名 */
	public static final String DRIVER_MARIADB = "org.mariadb.jdbc.Driver";

	/** SQL Server用JDBCドライバークラス名 */
	public static final String DRIVER_MSSQL = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	/** PostgreSQL用JDBCドライバークラス名 */
	public static final String DRIVER_PGSQL = "org.postgresql.Driver";

	/** 取得値のUnicodeコードポイント出力を行う冗長レベルの閾値 */
	private static final int VERBOSE_UNICODE_VALUE_THRESHOLD = 2;

	/** 変換後文字列のUnicodeコードポイント出力を行う冗長レベルの閾値 */
	private static final int VERBOSE_UNICODE_OUTPUT_THRESHOLD = 5;

	/** 出力時の区切り線 */
	private static final String SEPARATOR_LINE = "--------------------------------------------------";

	/** フィールド変数: データベースコネクション */
	private Connection connection = null;

	/** フィールド変数: JDBCドライバー名 */
	private String dbDriver = null;

	/** フィールド変数: JDBC接続文字列 */
	private String dbUrl = null;

	/** フィールド変数: 接続先ホスト名またはIPアドレス */
	private String hostName = null;

	/** フィールド変数: データベース種別 (oracle, mysql, mariadb, mssql, pgsql) */
	private String dbType = null;

	/** フィールド変数: データベース名またはSID/サービス名 */
	private String dbName = null;

	/** フィールド変数: 接続ユーザー名 */
	private String userId = null;

	/** フィールド変数: 接続パスワード */
	private String password = null;

	/** フィールド変数: 実行対象のSQL文 */
	private String sql = "";

	/** フィールド変数: データベース文字コード */
	private String dbCharset = null;

	/** フィールド変数: 出力エンコーディング */
	private String encoding = null;

	/** フィールド変数: ポート番号 */
	private int port = 0;

	/** フィールド変数: 冗長出力レベル */
	private int verbose = 0;

	/** フィールド変数: RAWバイト取得対象の列番号リスト (0指定時は全列) */
	private List<Integer> rawBytesList = new ArrayList<>();

	/** フィールド変数: 文字列文字コード変換対象の列番号リスト (0指定時は全列) */
	private List<Integer> convStrList = new ArrayList<>();

	/**
	 * データベース管理クラスの新規インスタンスを生成するデフォルトコンストラクタです。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * }</pre>
	 * </p>
	 */
	public ClsDatabase() {
	}

	/**
	 * 設定されているJDBCドライバー名を取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbDriver("com.mysql.cj.jdbc.Driver");
	 * String driver = db.getDbDriver(); // "com.mysql.cj.jdbc.Driver"
	 * }</pre>
	 * </p>
	 *
	 * @return JDBCドライバー名（未設定時はnull）
	 */
	public String getDbDriver() {
		return dbDriver;
	}

	/**
	 * 設定されているJDBC接続文字列を取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbUrl("jdbc:mysql://localhost:3306/mydb");
	 * String url = db.getDbUrl();
	 * }</pre>
	 * </p>
	 *
	 * @return JDBC接続文字列（未設定時はnull）
	 */
	public String getDbUrl() {
		return dbUrl;
	}

	/**
	 * 設定されている接続先ホスト名を取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setHostName("localhost");
	 * String host = db.getHostName(); // "localhost"
	 * }</pre>
	 * </p>
	 *
	 * @return ホスト名またはIPアドレス（未設定時はnull）
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * 設定されているデータベース種別を取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbType("mysql");
	 * String type = db.getDbType(); // "mysql"
	 * }</pre>
	 * </p>
	 *
	 * @return データベース種別 (oracle, mysql, mariadb, mssql, pgsql など)
	 */
	public String getDbType() {
		return dbType;
	}

	/**
	 * 設定されているデータベース名またはSID/サービス名を取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbName("sample_db");
	 * String dbName = db.getDbName(); // "sample_db"
	 * }</pre>
	 * </p>
	 *
	 * @return データベース名またはSID/サービス名（未設定時はnull）
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * 設定されている接続ユーザー名を取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setUserId("root");
	 * String user = db.getUserId(); // "root"
	 * }</pre>
	 * </p>
	 *
	 * @return ユーザー名（未設定時はnull）
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * 設定されている接続パスワードを取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setPassword("secret");
	 * String pass = db.getPassword(); // "secret"
	 * }</pre>
	 * </p>
	 *
	 * @return パスワード（未設定時はnull）
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 設定されている実行対象SQL文を取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setSql("SELECT * FROM users");
	 * String sql = db.getSql(); // "SELECT * FROM users"
	 * }</pre>
	 * </p>
	 *
	 * @return SQL文文字列
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * 設定されているデータベース文字コードを取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbCharset("x-eucJP-Open");
	 * String charset = db.getDbCharset(); // "x-eucJP-Open"
	 * }</pre>
	 * </p>
	 *
	 * @return データベース文字コード名（未設定時はnull）
	 */
	public String getDbCharset() {
		return dbCharset;
	}

	/**
	 * 設定されている出力エンコーディングを取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setEncoding("UTF-8");
	 * String enc = db.getEncoding(); // "UTF-8"
	 * }</pre>
	 * </p>
	 *
	 * @return 出力エンコーディング名（未設定時はnull）
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * 設定されているポート番号を取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setPort(3306);
	 * int port = db.getPort(); // 3306
	 * }</pre>
	 * </p>
	 *
	 * @return ポート番号
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 設定されている冗長出力レベルを取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setVerbose(2);
	 * int level = db.getVerbose(); // 2
	 * }</pre>
	 * </p>
	 *
	 * @return 冗長出力レベル
	 */
	public int getVerbose() {
		return verbose;
	}

	/**
	 * RAWバイト取得対象の列番号リストを取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * List<Integer> list = db.getRawBytesList();
	 * list.add(1);
	 * }</pre>
	 * </p>
	 *
	 * @return RAWバイト取得対象の列番号リスト（1始まりの列インデックス、0は全列指定）
	 */
	public List<Integer> getRawBytesList() {
		return rawBytesList;
	}

	/**
	 * 文字コード変換対象の列番号リストを取得します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * List<Integer> list = db.getConvStrList();
	 * list.add(2);
	 * }</pre>
	 * </p>
	 *
	 * @return 文字コード変換対象の列番号リスト（1始まりの列インデックス、0は全列指定）
	 */
	public List<Integer> getConvStrList() {
		return convStrList;
	}

	/**
	 * JDBCドライバー名を設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbDriver("com.mysql.cj.jdbc.Driver");
	 * }</pre>
	 * </p>
	 *
	 * @param dbDriver JDBCドライバークラス名（例: {@code "oracle.jdbc.OracleDriver"}）
	 */
	public void setDbDriver(final String dbDriver) {
		this.dbDriver = dbDriver;
	}

	/**
	 * JDBC接続文字列を設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbUrl("jdbc:mysql://localhost:3306/mydb");
	 * }</pre>
	 * </p>
	 *
	 * @param dbUrl 接続文字列 (JDBC URL)
	 */
	public void setDbUrl(final String dbUrl) {
		this.dbUrl = dbUrl;
	}

	/**
	 * 接続先ホスト名またはIPアドレスを設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setHostName("192.168.1.100");
	 * }</pre>
	 * </p>
	 *
	 * @param hostName ホスト名またはIPアドレス文字列
	 */
	public void setHostName(final String hostName) {
		this.hostName = hostName;
	}

	/**
	 * データベース種別を設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbType("pgsql");
	 * }</pre>
	 * </p>
	 *
	 * @param dbType データベース種別名 (oracle, oracle_i, mysql, mariadb, mssql, pgsql)
	 */
	public void setDbType(final String dbType) {
		this.dbType = dbType;
	}

	/**
	 * データベース名またはSID/サービス名を設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbName("sampledb");
	 * }</pre>
	 * </p>
	 *
	 * @param dbName データベース名またはSID/サービス名
	 */
	public void setDbName(final String dbName) {
		this.dbName = dbName;
	}

	/**
	 * 接続ユーザー名を設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setUserId("admin");
	 * }</pre>
	 * </p>
	 *
	 * @param userId 接続ユーザーID
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	/**
	 * 接続パスワードを設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setPassword("p@ssword123");
	 * }</pre>
	 * </p>
	 *
	 * @param password 接続パスワード
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * 実行対象のSQL文を設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setSql("SELECT * FROM employees WHERE department = 'IT'");
	 * }</pre>
	 * </p>
	 *
	 * @param sql 実行するSQL文
	 */
	public void setSql(final String sql) {
		this.sql = sql;
	}

	/**
	 * データベース文字コードを設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbCharset("MS932");
	 * }</pre>
	 * </p>
	 *
	 * @param dbCharset データベース文字コード名（例: "x-eucJP-Open", "MS932"）
	 */
	public void setDbCharset(final String dbCharset) {
		this.dbCharset = dbCharset;
	}

	/**
	 * 出力エンコーディングを設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setEncoding("UTF-8");
	 * }</pre>
	 * </p>
	 *
	 * @param encoding 出力エンコーディング名（例: "UTF-8", "Windows-31J"）
	 */
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	/**
	 * ポート番号を設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setPort(5432);
	 * }</pre>
	 * </p>
	 *
	 * @param port 接続先ポート番号
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	/**
	 * 冗長出力レベルを設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setVerbose(3);
	 * }</pre>
	 * </p>
	 *
	 * @param verbose 冗長レベル数値（2超過でUnicode値出力など）
	 */
	public void setVerbose(final int verbose) {
		this.verbose = verbose;
	}

	/**
	 * RAWバイト取得対象の列番号リストを設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * List<Integer> list = Arrays.asList(1, 2);
	 * db.setRawBytesList(list);
	 * }</pre>
	 * </p>
	 *
	 * @param rawBytesList RAWバイト取得対象の列番号リスト（1始まり、0は全列指定）
	 */
	public void setRawBytesList(final List<Integer> rawBytesList) {
		this.rawBytesList = rawBytesList;
	}

	/**
	 * 文字コード変換対象の列番号リストを設定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * List<Integer> list = Arrays.asList(1, 2);
	 * db.setConvStrList(list);
	 * }</pre>
	 * </p>
	 *
	 * @param convStrList 文字コード変換対象の列番号リスト（1始まり、0は全列指定）
	 */
	public void setConvStrList(final List<Integer> convStrList) {
		this.convStrList = convStrList;
	}

	/**
	 * データベースへの接続を開きます。
	 * <p>
	 * 設定されたDB種別やURLに基づき、適切なドライバーをロードして接続を確立します。
	 * </p>
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setDbType("mysql");
	 * db.setHostName("localhost");
	 * db.setPort(3306);
	 * db.setDbName("mydb");
	 * db.setUserId("root");
	 * db.setPassword("password");
	 * boolean isConnected = db.open();
	 * if (isConnected) {
	 *     System.out.println("接続成功");
	 * }
	 * }</pre>
	 * </p>
	 *
	 * @return 接続に成功した場合はtrue、失敗した場合はfalse
	 */
	public boolean open() {
		boolean success = true;
		switch (dbType != null ? dbType : "") {
			case "oracle":
			case "oracle_s":
			case "oracles":
				if (isNullOrEmpty(dbDriver)) {
					dbDriver = DRIVER_ORACLE;
				}
				// サービス名指定での接続
				if (isNullOrEmpty(dbUrl)) {
					dbUrl = "jdbc:oracle:thin:@" + hostName + ":" + port + "/" + dbName;
				}
				break;
			case "oracle_i":
			case "oraclei":
				if (isNullOrEmpty(dbDriver)) {
					dbDriver = DRIVER_ORACLE;
				}
				// インスタンス指定での接続
				if (isNullOrEmpty(dbUrl)) {
					dbUrl = "jdbc:oracle:thin:@" + hostName + ":" + port + ":" + dbName;
				}
				break;
			case "mysql":
				if (isNullOrEmpty(dbDriver)) {
					dbDriver = DRIVER_MYSQL;
				}
				if (isNullOrEmpty(dbUrl)) {
					dbUrl = "jdbc:mysql://" + hostName + ":" + port + "/" + dbName;
				}
				break;
			case "mariadb":
				if (isNullOrEmpty(dbDriver)) {
					dbDriver = DRIVER_MARIADB;
				}
				if (isNullOrEmpty(dbUrl)) {
					dbUrl = "jdbc:mariadb://" + hostName + ":" + port + "/" + dbName;
				}
				break;
			case "mssql":
				if (isNullOrEmpty(dbDriver)) {
					dbDriver = DRIVER_MSSQL;
				}
				if (isNullOrEmpty(dbUrl)) {
					dbUrl = "jdbc:sqlserver://" + hostName + ":" + port;
					if (!isNullOrEmpty(dbName)) {
						dbUrl = dbUrl + ";database=" + dbName;
					}
				}
				break;
			case "pgsql":
			case "postgresql":
				if (isNullOrEmpty(dbDriver)) {
					dbDriver = DRIVER_PGSQL;
				}
				if (isNullOrEmpty(dbUrl)) {
					dbUrl = "jdbc:postgresql://" + hostName + ":" + port + "/" + dbName;
				}
				break;
			default:
				break;
		}

		if (!isNullOrEmpty(dbDriver)) {
			try {
				System.out.println("[INFO ][ClsDatabase.open()] Class.forName(" + dbDriver + ")");
				Class.forName(dbDriver);
			} catch (ClassNotFoundException e) {
				success = false;
				e.printStackTrace();
			}
		}

		if (success) {
			Properties properties = new Properties();
			if (userId != null) {
				properties.put("user", userId);
			}
			if (password != null) {
				properties.put("password", password);
			}
			try {
				System.out.println("[INFO ][ClsDatabase.open()] DriverManager.getConnection(" + dbUrl + ")");
				connection = DriverManager.getConnection(dbUrl, properties);
			} catch (SQLException e) {
				success = false;
				e.printStackTrace();
			}
		}
		return success;
	}

	/**
	 * データベースとのアクティブな接続を閉じます。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * // 接続処理...
	 * db.open();
	 * // 処理後...
	 * boolean isClosed = db.close();
	 * }</pre>
	 * </p>
	 *
	 * @return 切断に成功した場合はtrue、失敗した場合はfalse
	 */
	public boolean close() {
		boolean success = true;
		try {
			if (connection != null && !connection.isClosed()) {
				System.out.println("[INFO ][ClsDatabase.close()] connection.close()");
				connection.close();
			}
		} catch (SQLException e) {
			success = false;
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * 設定されたSQL文を実行し、結果を標準出力にCSV形式で表示します。
	 * <p>
	 * 必要に応じて列ごとの文字コード変換やRAWバイト取得、Unicodeコードポイントの出力も行います。
	 * </p>
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * // 接続設定およびopen()実行...
	 * db.open();
	 * db.setSql("SELECT id, name FROM users");
	 * boolean isOk = db.execSql();
	 * db.close();
	 * }</pre>
	 * </p>
	 *
	 * @return SQLの実行および結果の出力に成功した場合はtrue、失敗した場合はfalse
	 */
	public boolean execSql() {
		if (connection == null) {
			System.err.println("[ERROR][ClsDatabase.execSql()] Connection is null.");
			return false;
		}

		System.out.println("[INFO ][ClsDatabase.execSql()] " + sql);
		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			if (resultSet != null) {
				System.out.println(SEPARATOR_LINE);
				// カラム数の取得
				ResultSetMetaData metaData = resultSet.getMetaData();
				int columnCount = metaData.getColumnCount();

				// ヘッダー行の表示 (String.joinの活用)
				List<String> headerList = new ArrayList<>(columnCount);
				for (int j = 1; j <= columnCount; j++) {
					headerList.add(metaData.getColumnName(j));
				}
				String header = String.join(",", headerList);
				if (!header.trim().isEmpty()) {
					System.out.println(header);
				}

				// レコードの表示
				while (resultSet.next()) {
					List<String> rowValues = new ArrayList<>(columnCount);
					for (int j = 1; j <= columnCount; j++) {
						String value = "";
						String output = "";
						try {
							// バイト配列の取得
							if (rawBytesList.contains(0) || rawBytesList.contains(j)) {
								byte[] rawBytes = resultSet.getBytes(j);
								if (rawBytes != null) {
									value = (dbCharset != null) ? new String(rawBytes, dbCharset) : new String(rawBytes);
									output = convertEncoding(value);
								}
							// 文字列の取得
							} else {
								value = resultSet.getString(j);
								if (value != null) {
									if (convStrList.contains(0) || convStrList.contains(j)) {
										byte[] bytes = (dbCharset != null) ? value.getBytes(dbCharset) : value.getBytes();
										output = convertEncoding((dbCharset != null) ? new String(bytes, dbCharset) : new String(bytes));
									} else {
										output = convertEncoding(value);
									}
								}
							}
						} catch (Exception e) {
							System.err.printf("EXCEPTION : [j = %d] %s\n", j, e.getMessage());
						}

						if (VERBOSE_UNICODE_VALUE_THRESHOLD < verbose && value != null) {
							// 文字列のUnicodeコードポイントを1文字ずつ16進数で出力する
							System.out.println();
							System.out.print("Unicodeコードポイント(V): ");
							for (int i = 0; i < value.length(); i++) {
								System.out.printf("U+%04X ", (int) value.charAt(i));
							}
							System.out.println();
						}
						if (VERBOSE_UNICODE_OUTPUT_THRESHOLD < verbose && output != null) {
							// 出力文字列のUnicodeコードポイントを1文字ずつ16進数で出力する
							System.out.print("Unicodeコードポイント(O): ");
							for (int i = 0; i < output.length(); i++) {
								System.out.printf("U+%04X ", (int) output.charAt(i));
							}
							System.out.println();
						}
						rowValues.add(output != null ? output : "");
					}
					System.out.println(String.join(",", rowValues));
				}
				System.out.println(SEPARATOR_LINE);
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 文字列がnullまたは空文字（空白のみを含む場合も含む）であるかを判定します。
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * boolean b1 = db.isNullOrEmpty(null);    // true
	 * boolean b2 = db.isNullOrEmpty("   ");   // true
	 * boolean b3 = db.isNullOrEmpty("abc");   // false
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
	 * 文字列の文字コードを指定されたエンコーディングに変換します。
	 * <p>
	 * {@code encoding} プロパティが未設定または文字列がnullの場合はそのまま返却します。
	 * </p>
	 *
	 * <p>使用例:
	 * <pre>{@code
	 * ClsDatabase db = new ClsDatabase();
	 * db.setEncoding("UTF-8");
	 * String converted = db.convertEncoding("テキスト");
	 * }</pre>
	 * </p>
	 *
	 * @param str 変換元の文字列
	 * @return 変換後の文字列（変換エラー時やエンコーディング未指定時は元の文字列）
	 */
	String convertEncoding(final String str) {
		if (isNullOrEmpty(encoding) || str == null) {
			return str;
		}
		try {
			if (Charset.isSupported(encoding)) {
				return new String(str.getBytes(encoding), Charset.forName(encoding));
			}
			return new String(str.getBytes(encoding), encoding);
		} catch (Exception e) {
			// ignore
			return str;
		}
	}
}
