package VASSAL.build.module.map.boardPicker;

import VASSAL.build.module.map.BoardPicker;

import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BoardSlotTest {
  private static final Dimension DEFAULT_SLOT_SIZE = new Dimension(350, 125);
  private static final double SLOT_SCALE = 0.2;

  private static class TestBoardPicker extends BoardPicker {
    private final List<BoardSlot> slots = new ArrayList<>();
    private final Board selectableBoard;

    private TestBoardPicker(Board selectableBoard) {
      this.selectableBoard = selectableBoard;
    }

    private void addSlot(BoardSlot slot) {
      slots.add(slot);
    }

    @Override
    public Dimension getDefaultSlotSize() {
      return DEFAULT_SLOT_SIZE;
    }

    @Override
    public double getSlotScale() {
      return SLOT_SCALE;
    }

    @Override
    public String[] getAllowableLocalizedBoardNames() {
      return selectableBoard == null ?
        new String[0] :
        new String[] {selectableBoard.getLocalizedName()};
    }

    @Override
    public Board getLocalizedBoard(String localizedBoardName) {
      return selectableBoard;
    }

    @Override
    public List<Board> getBoardsFromControls() {
      return slots.stream()
        .map(BoardSlot::getBoard)
        .filter(board -> board != null)
        .toList();
    }

    @Override
    public BoardSlot getSlot(int i) {
      return i >= 0 && i < slots.size() ? slots.get(i) : null;
    }

    @Override
    public void repaint() {
    }
  }

  @Test
  void emptyFirstSlotUsesDefaultSize() {
    final TestBoardPicker picker = new TestBoardPicker(null);
    final BoardSlot slot = new BoardSlot(picker);
    picker.addSlot(slot);

    assertNull(slot.getBoard());
    assertEquals(DEFAULT_SLOT_SIZE.width, slot.getIconWidth());
    assertEquals(DEFAULT_SLOT_SIZE.height, slot.getIconHeight());
  }

  @Test
  void selectedBoardUsesScaledBoardBounds() {
    final Board board = board("board", 500, 300);
    final TestBoardPicker picker = new TestBoardPicker(board);
    final BoardSlot slot = new BoardSlot(picker);
    picker.addSlot(slot);

    assertEquals(board, slot.getBoard());
    assertEquals(100, slot.getIconWidth());
    assertEquals(60, slot.getIconHeight());
  }

  @Test
  void emptyLaterSlotUsesFirstSlotSize() {
    final Board board = board("board", 500, 300);
    final TestBoardPicker picker = new TestBoardPicker(null);
    final BoardSlot firstSlot = new BoardSlot(picker);
    picker.addSlot(firstSlot);
    firstSlot.setBoard(board);

    final BoardSlot secondSlot = new BoardSlot(picker);
    picker.addSlot(secondSlot);

    assertNull(secondSlot.getBoard());
    assertEquals(firstSlot.getIconWidth(), secondSlot.getIconWidth());
    assertEquals(firstSlot.getIconHeight(), secondSlot.getIconHeight());
  }

  private static Board board(String name, int width, int height) {
    final Board board = new Board();
    board.setAttribute(Board.NAME, name);
    board.setAttribute(Board.WIDTH, width);
    board.setAttribute(Board.HEIGHT, height);
    return board;
  }
}
