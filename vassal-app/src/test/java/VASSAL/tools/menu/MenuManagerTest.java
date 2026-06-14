package VASSAL.tools.menu;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MenuManagerTest {
  private TestMenuManager menuManager;

  @BeforeEach
  public void setUp() {
    MenuManager.instance = null;
    menuManager = new TestMenuManager();
  }

  @AfterEach
  public void tearDown() {
    MenuManager.instance = null;
  }

  @Test
  public void removeFromSectionKeepsEarlierMarkersWhenSectionBecomesEmpty() {
    final MenuProxy parent = new MenuProxy("parent");
    final MenuMarker previousMarker = new MenuMarker();
    final MenuMarker start = menuManager.addMarker("section.start");
    final MenuMarker end = menuManager.addMarker("section.end");
    final MenuItemProxy item = new MenuItemProxy();
    final SeparatorProxy separator = new SeparatorProxy();
    final MenuItemProxy followingItem = new MenuItemProxy();

    parent.add(previousMarker);
    parent.add(start);
    parent.add(item);
    parent.add(end);
    parent.add(separator);
    parent.add(followingItem);

    menuManager.removeFromSection("section", item);

    assertArrayEquals(
      new ChildProxy<?>[] {previousMarker, start, end, followingItem},
      parent.getChildren()
    );
  }

  private static class TestMenuManager extends MenuManager {
    @Override
    public JMenuBar getMenuBarFor(JFrame fc) {
      return new JMenuBar();
    }

    @Override
    public MenuBarProxy getMenuBarProxyFor(JFrame fc) {
      return new MenuBarProxy();
    }
  }
}
