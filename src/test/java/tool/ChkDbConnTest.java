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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ChkDbConn の単体テストクラスです。
 */
public class ChkDbConnTest {

	private Path tempDir;

	@Before
	public void setUp() throws IOException {
		// 注意事項に準拠した作業ディレクトリの作成
		tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "UnitTest", "chkdbconn", "ChkDbConn");
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
	public void testParseInt() {
		ChkDbConn app = createDummyApp();
		assertEquals(123, app.parseInt("123", 0));
		assertEquals(456, app.parseInt("  456  ", 0));
		assertEquals(999, app.parseInt("invalid", 999));
		assertEquals(999, app.parseInt("", 999));
		assertEquals(999, app.parseInt(null, 999));
	}

	@Test
	public void testIsNullOrEmpty() {
		ChkDbConn app = createDummyApp();
		assertTrue(app.isNullOrEmpty(null));
		assertTrue(app.isNullOrEmpty(""));
		assertTrue(app.isNullOrEmpty("   "));
		assertFalse(app.isNullOrEmpty("value"));
		assertFalse(app.isNullOrEmpty(" value "));
	}

	@Test
	public void testIsEquals() {
		ChkDbConn app = createDummyApp();
		assertTrue(app.isEquals("abc", "abc"));
		assertTrue(app.isEquals("abc", "ABC"));
		assertTrue(app.isEquals("  abc  ", "ABC"));
		assertTrue(app.isEquals(null, ""));
		assertTrue(app.isEquals("", null));
		assertTrue(app.isEquals(null, null));
		assertFalse(app.isEquals("abc", "def"));
	}

	@Test
	public void testJoinIntList() {
		ChkDbConn app = createDummyApp();
		assertEquals("", app.joinIntList(null, ","));
		assertEquals("", app.joinIntList(Collections.emptyList(), ","));

		List<Integer> list1 = Collections.singletonList(10);
		assertEquals("10", app.joinIntList(list1, ","));

		List<Integer> list2 = Arrays.asList(1, 2, 3);
		assertEquals("1,2,3", app.joinIntList(list2, ","));
		assertEquals("1:2:3", app.joinIntList(list2, ":"));
	}

	@Test
	public void testTempDirectoryCreation() {
		assertTrue("作業ディレクトリが作成されていること", Files.exists(tempDir));
		assertTrue("カレントディレクトリより上位に影響を与えないこと", tempDir.startsWith(Paths.get(System.getProperty("java.io.tmpdir"))));
	}

	/**
	 * テスト用ヘルパーメソッド：コンストラクタがSystem.exitを呼ばないインスタンス生成をシミュレート
	 *
	 * @return ダミーのChkDbConnインスタンス
	 */
	private ChkDbConn createDummyApp() {
		return new ChkDbConn();
	}
}
