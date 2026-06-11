package VASSAL.command;

import VASSAL.build.GameModule;
import VASSAL.counters.GamePiece;
import VASSAL.property.PersistentPropertyContainer;

/**
 * This Command sets a Persistent Property in a PersistentPropertyContainer.
 * Currently only BasicPiece and Decorator implement PersistentPropertyContainer.
 * The Undo Command is a SetPropertyCommand to set the value back to the original value.
 */
public final class SetPersistentPropertyCommand extends Command {
  public static final String COMMAND_PREFIX = "SPP\t"; // NON-NLS
  private final Object key;
  private final Object oldValue;
  private final Object newValue;
  private final String id;


  public SetPersistentPropertyCommand(String id, Object key, Object oldValue, Object newValue) {
    this.id = id;
    this.key = key;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  protected void executeCommand() {
    final GamePiece target = GameModule.getGameModule().getGameState().getPieceForId(id);
    if (target != null) {
      // Not all GamePieces will have persistent Properties
      if (target instanceof PersistentPropertyContainer) {
        ((PersistentPropertyContainer) target).setPersistentProperty(getKey(), getNewValue());
      }
    }
  }

  @Override
  protected Command myUndoCommand() {
    return new SetPersistentPropertyCommand(id, key, newValue, oldValue);
  }

  @Override
  public String getDetails() {
    return "id=" + id + ",key=" + key + ",old=" + oldValue + ",new=" + newValue; // NON-NLS
  }

  public Object getKey() {
    return key;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  public String getId() {
    return id;
  }
}
