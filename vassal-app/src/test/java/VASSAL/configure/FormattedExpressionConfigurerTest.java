package VASSAL.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import VASSAL.i18n.Resources;

public class FormattedExpressionConfigurerTest {

  @Test
  public void constructorStoresInitialValue() {
    final FormattedExpressionConfigurer configurer =
      new FormattedExpressionConfigurer("key", "name", "$foo$");

    assertEquals("$foo$", configurer.getValueString());
  }

  @Test
  public void propertyExpressionConstructorStoresHint() {
    final PropertyExpressionConfigurer configurer =
      new PropertyExpressionConfigurer("key", "name", "value");

    assertEquals(Resources.getString("Editor.property_match_hint"), configurer.getHint());
  }
}
