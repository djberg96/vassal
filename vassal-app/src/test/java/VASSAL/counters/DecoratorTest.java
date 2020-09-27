/*
 * Copyright 2020 Vassal Development Team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.counters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.tools.icon.IconFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.swing.ImageIcon;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Base class for all Decorator Tests. Provides
 */
public class DecoratorTest {

  /**
   * Run the serialization tests on the supplied Decorator.
   *
   *
   * @param test Test Description
   * @param referenceTrait The reference Trait to be tested. The Reference trait does not need to have a BasicPiece as an Inner piece,
   *                       but one will be added if it does not.
   */
  public void serializeTest(String test, Decorator referenceTrait) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

    // Create a static mock for IconFactory and return the Calculator icon when asked. Allows Editors with Beanshell configurers to initialise.
    try (MockedStatic<IconFactory> staticIf = Mockito.mockStatic(IconFactory.class)) {
      staticIf.when(() -> IconFactory.getIcon("calculator", 12)).thenReturn(new ImageIcon());

      // Can't test without a properly constructed BasicPiece as the inner
      if (referenceTrait.getInner() == null) {
        referenceTrait.setInner(createBasicPiece());
      }

      // Test we can reconstruct a Piece by passing its type through its Constructor
      constructorTest(test, referenceTrait);

      // Test we can reconstruct a Piece using the BasicCommandEncoder
      commandEncoderTest(test, referenceTrait);

      // Test the serialization used in the internal editor matches the standard serialization
      editorTest(test, referenceTrait);
    }
  }

  /**
   * Create and return a standard BasiCPiece trait with enough internals set up to
   * be used for simple tests
   *
   * @return Generated BasicPiece
   */
  public BasicPiece createBasicPiece() {
    final BasicPiece bp = new BasicPiece();
    bp.setProperty(Properties.PIECE_ID, "1");
    return bp;
  }

  /**
   * Test that a trait's internal editor encodes the type and state in the same way
   * that the trait started with. Checks for serialization issues in the trait editors.
   *
   * @param test A descriptive name for this test or test sequence
   * @param referenceTrait The trait to be tested
   */
  public void editorTest(String test, Decorator referenceTrait) {

    // Save the original Type and State in case the Editor changes them
    final String originalType = referenceTrait.myGetType();
    final String originalState = referenceTrait.myGetState();

    // Create an instance of the Editor
    final PieceEditor editor = referenceTrait.getEditor();

    // Confirm that the Type and State encoded by the editor is the same as the original trait
    assertThat("Trait Edit Test (State): " + test, editor.getState(), is(equalTo(originalState))); // NON-NLS
    assertThat("Trait Edit Test (Type): " + test, editor.getType(), is(equalTo(originalType))); // NON-NLS
  }

  /**
   * Test that the BasicCommandEncoder can faithfully reproduce this trait
   *
   * @param test Test description
   * @param referenceTrait Reference Trait to be tested
   */
  public void commandEncoderTest(String test, Decorator referenceTrait) {
    final BasicCommandEncoder ce = new BasicCommandEncoder();

    // Create a new trait using the standard Command Encoder and the type from the reference trait
    Decorator testTrait = ce.createDecorator(referenceTrait.myGetType(), new BasicPiece());

    // Inject any State
    testTrait.mySetState(referenceTrait.myGetState());

    // Test constructed trait is equivalent to the reference trait
    assertThat("Command Encoder Test: " + test, referenceTrait.testEquals(testTrait), is(true)); // NON-NLS
  }

  /**
   * Test that a constructed Decorator with the type injected via its 2-arg constructor equals the reference trait
   *
   * @param test Test Description
   * @param referenceTrait Reference trait for comparison
   */
  public void constructorTest(String test, Decorator referenceTrait) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
    // Build a new trait using the Type from the reference trait
    Constructor<? extends Decorator>  constructor = referenceTrait.getClass().getConstructor(String.class, GamePiece.class);
    Decorator constructedTrait = constructor.newInstance(referenceTrait.myGetType(), createBasicPiece());

    // Inject the state
    constructedTrait.mySetState(referenceTrait.myGetState());

    // Test constructed trait is equivalent to the reference trait
    assertThat("Constructor Test: " + test, referenceTrait.testEquals(constructedTrait), is(true)); // NON-NLS
  }
}
