package tool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ClsDatabase の単体テストクラスです。
 */
public class ClsDatabaseTest {

	private Path tempDir;

	@Before
	public void setUp() throws IOException {
		// 注意事項に準拠した作業ディレクトリの作成
		tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "UnitTest", "chkdbconn", "ClsDatabase");
		if (!Files.exists(tempDir)) {
			Files.createDirectories(tempDir);
		}
	}

	@After
	public void tearDown() throws IOException {
		// 作業ディレクトリ内のクリーンアップ
		if (Files.exists(tempDir)) {
			Files.walk(tempDir)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		}
	}

	@Test
	public void testGettersAndSetters() {
		ClsDatabase db = new ClsDatabase();

		db.setDbDriver("org.h2.Driver");
		assertEquals("org.h2.Driver", db.getDbDriver());

		db.setDbUrl("jdbc:h2:mem:testdb");
		assertEquals("jdbc:h2:mem:testdb", db.getDbUrl());

		db.setHostName("127.0.0.1");
		assertEquals("127.0.0.1", db.getHostName());

		db.setDbType("mysql");
		assertEquals("mysql", db.getDbType());

		db.setDbName("sample_db");
		assertEquals("sample_db", db.getDbName());

		db.setUserId("admin");
		assertEquals("admin", db.getUserId());

		db.setPassword("secret");
		assertEquals("secret", db.getPassword());

		db.setSql("SELECT 1");
		assertEquals("SELECT 1", db.getSql());

		db.setDbCharset("UTF-8");
		assertEquals("UTF-8", db.getDbCharset());

		db.setEncoding("Shift_JIS");
		assertEquals("Shift_JIS", db.getEncoding());

		db.setPort(3306);
		assertEquals(3306, db.getPort());

		db.setVerbose(2);
		assertEquals(2, db.getVerbose());

		List<Integer> rawList = Arrays.asList(1, 2);
		db.setRawBytesList(rawList);
		assertEquals(rawList, db.getRawBytesList());

		List<Integer> convList = Arrays.asList(3, 4);
		db.setConvStrList(convList);
		assertEquals(convList, db.getConvStrList());
	}

	@Test
	public void testIsNullOrEmpty() {
		ClsDatabase db = new ClsDatabase();
		assertTrue(db.isNullOrEmpty(null));
		assertTrue(db.isNullOrEmpty(""));
		assertTrue(db.isNullOrEmpty("   "));
		assertFalse(db.isNullOrEmpty("text"));
		assertFalse(db.isNullOrEmpty(" text "));
	}

	@Test
	public void testConvertEncoding() {
		ClsDatabase db = new ClsDatabase();
		assertEquals("test", db.convertEncoding("test"));

		db.setEncoding("UTF-8");
		assertEquals("テスト文字列", db.convertEncoding("テスト文字列"));

		db.setEncoding("INVALID-CHARSET-NAME");
		assertEquals("fallback", db.convertEncoding("fallback"));
	}

	@Test
	public void testOpenAndExecSqlWithH2() {
		ClsDatabase db = new ClsDatabase();
		db.setDbDriver("org.h2.Driver");
		db.setDbUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
		db.setUserId("sa");
		db.setPassword("");

		// 接続
		boolean opened = db.open();
		assertTrue("H2 データベースに接続できること", opened);

		// SELECTクエリ実行（冗長出力、RAW取得列、変換列指定のテスト含む）
		db.setSql("SELECT 1 AS ID, 'Alice' AS NAME UNION ALL SELECT 2, 'Bob'");
		db.setVerbose(6);
		db.setRawBytesList(Arrays.asList(2));
		db.setConvStrList(Arrays.asList(1));
		db.setDbCharset("UTF-8");
		db.setEncoding("UTF-8");
		assertTrue("SQL実行と標準出力が正常に行われること", db.execSql());

		// 切断
		boolean closed = db.close();
		assertTrue("接続を切断できること", closed);
	}

	@Test
	public void testExecSqlWhenConnectionNull() {
		ClsDatabase db = new ClsDatabase();
		assertFalse("コネクションがnullの場合はfalseを返すこと", db.execSql());
	}

	@Test
	public void testTempDirectoryCreation() {
		assertTrue("作業ディレクトリが作成されていること", Files.exists(tempDir));
		assertTrue("カレントディレクトリより上位に影響を与えないこと", tempDir.startsWith(Paths.get(System.getProperty("java.io.tmpdir"))));
	}
}
