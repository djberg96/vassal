package VASSAL.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class SavedGameUpdaterDialogTest {
  @Test
  void moduleVersionReadsCorrectedKey() {
    final Properties pieceInfo = new Properties();
    pieceInfo.setProperty("moduleVersion", "2.0");

    assertEquals("2.0", SavedGameUpdaterDialog.getModuleVersion(pieceInfo));
  }

  @Test
  void moduleVersionReadsLegacyTypoKey() {
    final Properties pieceInfo = new Properties();
    pieceInfo.setProperty("moduleVerion", "1.0");

    assertEquals("1.0", SavedGameUpdaterDialog.getModuleVersion(pieceInfo));
  }

  @Test
  void moduleVersionPrefersCorrectedKey() {
    final Properties pieceInfo = new Properties();
    pieceInfo.setProperty("moduleVersion", "2.0");
    pieceInfo.setProperty("moduleVerion", "1.0");

    assertEquals("2.0", SavedGameUpdaterDialog.getModuleVersion(pieceInfo));
  }
}
