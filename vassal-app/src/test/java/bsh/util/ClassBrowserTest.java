package bsh.util;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ClassBrowserTest {
  @Test
  public void clearsClassTreeWhenClassIsNull() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      final ClassBrowser browser = new ClassBrowser();
      browser.tree = new JTree();

      browser.setClassTree(null);

      assertThat(browser.tree.getModel(), is(nullValue()));
    });
  }

  @Test
  public void buildsClassTreeFromRootToSelectedClass() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      final ClassBrowser browser = new ClassBrowser();
      browser.tree = new JTree();

      browser.setClassTree(Child.class);

      final DefaultMutableTreeNode root =
        (DefaultMutableTreeNode)browser.tree.getModel().getRoot();
      final DefaultMutableTreeNode parent =
        (DefaultMutableTreeNode)root.getChildAt(0);
      final DefaultMutableTreeNode child =
        (DefaultMutableTreeNode)parent.getChildAt(0);

      assertThat(root.toString(), is(Object.class.toString()));
      assertThat(parent.toString(), is(Parent.class.toString()));
      assertThat(child.toString(), is(Child.class.toString()));
      assertThat(browser.tree.isExpanded(new TreePath(parent.getPath())),
        is(true));
    });
  }

  private static class Parent {
  }

  private static class Child extends Parent {
  }
}
