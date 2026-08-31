package com.mindx.puzzlegame.game;

import com.mindx.puzzlegame.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BoardValidationService {
    public static final List<Integer> SOLVED_BOARD = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0);
    private static final Set<String> DIRECTIONS = Set.of("up", "down", "left", "right");
    private final SecureRandom random = new SecureRandom();

    public void validateBoard(List<Integer> board) {
        if (board == null || board.size() != 12 || new HashSet<>(board).size() != 12) {
            throw invalid("Bàn cờ phải có đúng 12 giá trị khác nhau");
        }
        for (int value = 0; value <= 11; value++) {
            if (!board.contains(value)) {
                throw invalid("Bàn cờ phải chứa đủ các số từ 0 đến 11");
            }
        }
    }

    public List<Integer> replay(List<Integer> initialBoard, List<String> moves) {
        validateBoard(initialBoard);
        if (moves == null) throw invalid("Danh sách bước đi không được để trống");
        List<Integer> result = new ArrayList<>(initialBoard);
        for (String rawDirection : moves) {
            if (rawDirection == null) throw invalid("Bước đi không hợp lệ");
            String direction = rawDirection.toLowerCase();
            if (!DIRECTIONS.contains(direction) || !move(result, direction)) {
                throw invalid("Chuỗi bước đi chứa thao tác không hợp lệ");
            }
        }
        return result;
    }

    public boolean isSolved(List<Integer> board) {
        return SOLVED_BOARD.equals(board);
    }

    public List<Integer> shuffledBoard() {
        List<Integer> board;
        do {
            board = new ArrayList<>(SOLVED_BOARD);
            for (int i = 0; i < 100; i++) {
                List<String> valid = validDirections(board);
                move(board, valid.get(random.nextInt(valid.size())));
            }
        } while (isSolved(board));
        return board;
    }

    boolean move(List<Integer> board, String direction) {
        int emptyIndex = board.indexOf(0);
        int row = emptyIndex / 4;
        int column = emptyIndex % 4;
        int target = switch (direction) {
            case "up" -> row > 0 ? emptyIndex - 4 : emptyIndex;
            case "down" -> row < 2 ? emptyIndex + 4 : emptyIndex;
            case "left" -> column > 0 ? emptyIndex - 1 : emptyIndex;
            case "right" -> column < 3 ? emptyIndex + 1 : emptyIndex;
            default -> emptyIndex;
        };
        if (target == emptyIndex) return false;
        board.set(emptyIndex, board.get(target));
        board.set(target, 0);
        return true;
    }

    private List<String> validDirections(List<Integer> board) {
        int emptyIndex = board.indexOf(0);
        int row = emptyIndex / 4;
        int column = emptyIndex % 4;
        List<String> result = new ArrayList<>();
        if (row > 0) result.add("up");
        if (row < 2) result.add("down");
        if (column > 0) result.add("left");
        if (column < 3) result.add("right");
        return result;
    }

    private ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
