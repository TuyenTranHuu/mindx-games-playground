package com.mindx.puzzlegame.game;

import com.mindx.puzzlegame.common.ApiException;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BoardValidationServiceTest {
    private final BoardValidationService service = new BoardValidationService();

    @Test
    void acceptsBoardWithAllValuesFromZeroToEleven() {
        assertDoesNotThrow(() -> service.validateBoard(BoardValidationService.SOLVED_BOARD));
    }

    @Test
    void rejectsBoardWithDuplicateValue() {
        assertThrows(ApiException.class,
                () -> service.validateBoard(List.of(1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0)));
    }

    @Test
    void replaysValidMove() {
        List<Integer> result = service.replay(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 11),
                List.of("right"));
        assertEquals(BoardValidationService.SOLVED_BOARD, result);
    }

    @Test
    void rejectsMoveThatCrossesBoardEdge() {
        assertThrows(ApiException.class,
                () -> service.replay(BoardValidationService.SOLVED_BOARD, List.of("right")));
    }

    @Test
    void shuffledBoardIsValidSolvableAndNotAlreadySolved() {
        for (int i = 0; i < 20; i++) {
            List<Integer> board = service.shuffledBoard();
            assertDoesNotThrow(() -> service.validateBoard(board));
            assertFalse(service.isSolved(board));
        }
    }
}
