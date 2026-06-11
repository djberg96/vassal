package VASSAL.counters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import VASSAL.tools.NamedKeyStroke;

import org.junit.jupiter.api.Test;

public class AttachmentSerializeTest extends SerializeTest<Attachment> {
  @Test
  public void serialize() throws Exception {
    final Attachment attachment = new Attachment();
    attachment.attachName = "Squad";
    attachment.desc = "linked units";
    attachment.attachCommandName = "Attach";
    attachment.attachKey = NamedKeyStroke.of("ctrl A");
    attachment.clearAllCommandName = "Clear all";
    attachment.clearAllKey = NamedKeyStroke.of("ctrl C");
    attachment.propertiesFilter.setExpression("{Status == \"Ready\"}");
    attachment.restrictRange = true;
    attachment.range = 4;
    attachment.fixedRange = false;
    attachment.rangeProperty = "CommandRange";
    attachment.selectFromDeckExpression = "2";
    attachment.clearMatchingCommandName = "Clear matching";
    attachment.clearMatchingKey = NamedKeyStroke.of("ctrl M");
    attachment.clearMatchingFilter.setExpression("{Side == \"Blue\"}");
    attachment.onAttach = Attachment.ON_ATTACH_ATTACH_ALL;
    attachment.onDetach = Attachment.ON_DETACH_REMOVE;
    attachment.beforeAttach = Attachment.BEFORE_ATTACH_NOTHING;
    attachment.allowSelfAttach = true;
    attachment.autoAttach = false;

    serializeTest(Attachment.class, attachment);
  }

  @Override
  void assertSame(Attachment a1, Attachment a2) {
    assertEquals(a1.attachName, a2.attachName);
    assertEquals(a1.desc, a2.desc);
    assertEquals(a1.attachCommandName, a2.attachCommandName);
    assertEquals(a1.attachKey, a2.attachKey);
    assertEquals(a1.clearAllCommandName, a2.clearAllCommandName);
    assertEquals(a1.clearAllKey, a2.clearAllKey);
    assertEquals(a1.propertiesFilter.getExpression(), a2.propertiesFilter.getExpression());
    assertEquals(a1.restrictRange, a2.restrictRange);
    assertEquals(a1.range, a2.range);
    assertEquals(a1.fixedRange, a2.fixedRange);
    assertEquals(a1.rangeProperty, a2.rangeProperty);
    assertEquals(a1.selectFromDeckExpression, a2.selectFromDeckExpression);
    assertEquals(a1.clearMatchingCommandName, a2.clearMatchingCommandName);
    assertEquals(a1.clearMatchingKey, a2.clearMatchingKey);
    assertEquals(a1.clearMatchingFilter.getExpression(), a2.clearMatchingFilter.getExpression());
    assertEquals(a1.onAttach, a2.onAttach);
    assertEquals(a1.onDetach, a2.onDetach);
    assertEquals(a1.beforeAttach, a2.beforeAttach);
    assertEquals(a1.allowSelfAttach, a2.allowSelfAttach);
    assertEquals(a1.autoAttach, a2.autoAttach);
  }
}
