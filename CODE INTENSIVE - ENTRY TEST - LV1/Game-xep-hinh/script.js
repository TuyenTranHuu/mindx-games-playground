// Mảng board lưu trạng thái của 12 ô trên bàn cờ.
// Số 0 đại diện cho ô đen.
let board = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0];

// Các biến lưu trạng thái hiện tại của lượt chơi.
let isPlaying = false;
let moveCount = 0;
let elapsedSeconds = 0;
let timerId = null;
let gameHistory = [];

// Tìm vùng bàn cờ trong HTML để JavaScript có thể tạo các ô bên trong.
const gameBoard = document.getElementById("game-board");
const startButton = document.getElementById("start-button");
const moveCountText = document.getElementById("move-count");
const winMessage = document.getElementById("win-message");
const timerText = document.getElementById("timer");
const historyBody = document.getElementById("history-body");

// Hàm này đọc mảng board và hiển thị các ô lên giao diện.
function renderBoard() {
    // Xóa giao diện cũ trước khi vẽ lại để các ô không bị lặp.
    gameBoard.innerHTML = "";

    // Duyệt lần lượt tất cả phần tử trong mảng board.
    for (let i = 0; i < board.length; i++) {
        const tileValue = board[i];
        const tile = document.createElement("div");

        tile.classList.add("tile");

        if (tileValue === 0) {
            // Nếu giá trị là 0, tạo ô đen và không hiển thị số 0.
            tile.classList.add("empty-tile");
        } else {
            // Mỗi số được thêm class riêng để nhận đúng màu trong CSS.
            tile.classList.add(`tile-${tileValue}`);
            tile.textContent = tileValue;
        }

        gameBoard.appendChild(tile);
    }
}

// Thay đổi mảng board theo một hướng và trả về kết quả có di chuyển được không.
function changeBoardPosition(direction) {
    const emptyIndex = board.indexOf(0);
    const emptyRow = Math.floor(emptyIndex / 4);
    const emptyColumn = emptyIndex % 4;
    let targetIndex = emptyIndex;

    if (direction === "up" && emptyRow > 0) {
        targetIndex = emptyIndex - 4;
    } else if (direction === "down" && emptyRow < 2) {
        targetIndex = emptyIndex + 4;
    } else if (direction === "left" && emptyColumn > 0) {
        targetIndex = emptyIndex - 1;
    } else if (direction === "right" && emptyColumn < 3) {
        targetIndex = emptyIndex + 1;
    }

    // Nếu targetIndex không đổi thì ô đen đang ở biên và không thể đi tiếp.
    if (targetIndex === emptyIndex) {
        return false;
    }

    // Đổi chỗ ô đen và ô nằm ở hướng di chuyển.
    board[emptyIndex] = board[targetIndex];
    board[targetIndex] = 0;

    return true;
}

// Di chuyển ô đen khi người chơi nhấn phím.
function moveEmptyTile(direction) {
    if (!isPlaying) {
        return;
    }

    const hasMoved = changeBoardPosition(direction);

    if (!hasMoved) {
        return;
    }

    moveCount++;
    moveCountText.textContent = moveCount;
    renderBoard();
    checkWin();
}

// Kiểm tra bàn cờ có đang ở đúng thứ tự chiến thắng hay không.
function isBoardSolved() {
    const solvedBoard = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0];

    for (let i = 0; i < solvedBoard.length; i++) {
        if (board[i] !== solvedBoard[i]) {
            return false;
        }
    }

    return true;
}

// Trộn bằng 100 bước hợp lệ nên bàn cờ sau khi trộn luôn có thể giải được.
function shuffleBoard() {
    const solvedBoard = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0];

    do {
        // Mỗi lần thử trộn đều bắt đầu từ trạng thái chiến thắng.
        board = [...solvedBoard];

        for (let shuffleCount = 0; shuffleCount < 100; shuffleCount++) {
            const emptyIndex = board.indexOf(0);
            const emptyRow = Math.floor(emptyIndex / 4);
            const emptyColumn = emptyIndex % 4;
            const validDirections = [];

            if (emptyRow > 0) {
                validDirections.push("up");
            }

            if (emptyRow < 2) {
                validDirections.push("down");
            }

            if (emptyColumn > 0) {
                validDirections.push("left");
            }

            if (emptyColumn < 3) {
                validDirections.push("right");
            }

            const randomIndex = Math.floor(Math.random() * validDirections.length);
            const randomDirection = validDirections[randomIndex];
            changeBoardPosition(randomDirection);
        }
    } while (isBoardSolved());
}

// Chuyển tổng số giây thành định dạng phút:giây, ví dụ 65 thành 01:05.
function formatTime(totalSeconds) {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    const formattedMinutes = String(minutes).padStart(2, "0");
    const formattedSeconds = String(seconds).padStart(2, "0");

    return `${formattedMinutes}:${formattedSeconds}`;
}

function stopTimer() {
    if (timerId !== null) {
        clearInterval(timerId);
        timerId = null;
    }
}

function startTimer() {
    // Luôn dừng interval cũ trước khi tạo interval mới.
    stopTimer();

    timerId = setInterval(function () {
        elapsedSeconds++;
        timerText.textContent = formatTime(elapsedSeconds);
    }, 1000);
}

function renderHistory() {
    historyBody.innerHTML = "";

    for (let i = 0; i < gameHistory.length; i++) {
        const historyItem = gameHistory[i];
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${i + 1}</td>
            <td>${historyItem.moves}</td>
            <td>${historyItem.time}</td>
        `;

        historyBody.appendChild(row);
    }
}

function addHistory() {
    const newHistoryItem = {
        moves: moveCount,
        time: formatTime(elapsedSeconds)
    };

    gameHistory.push(newHistoryItem);
    renderHistory();
}

function checkWin() {
    if (!isPlaying || !isBoardSolved()) {
        return;
    }

    isPlaying = false;
    stopTimer();
    winMessage.hidden = false;
    startButton.textContent = "Chơi lại";
    addHistory();
}

function startGame() {
    stopTimer();
    moveCount = 0;
    elapsedSeconds = 0;
    moveCountText.textContent = moveCount;
    timerText.textContent = formatTime(elapsedSeconds);
    winMessage.hidden = true;

    shuffleBoard();
    renderBoard();

    isPlaying = true;
    startButton.textContent = "Kết thúc";
    startTimer();
}

function endGame() {
    isPlaying = false;
    stopTimer();
    startButton.textContent = "Bắt đầu";
}

// Đổi tên phím người dùng nhấn thành bốn hướng mà game sử dụng.
function getDirectionFromKey(key) {
    const pressedKey = key.toLowerCase();

    if (pressedKey === "w" || pressedKey === "arrowup") {
        return "up";
    } else if (pressedKey === "s" || pressedKey === "arrowdown") {
        return "down";
    } else if (pressedKey === "a" || pressedKey === "arrowleft") {
        return "left";
    } else if (pressedKey === "d" || pressedKey === "arrowright") {
        return "right";
    }

    return null;
}

startButton.addEventListener("click", function () {
    if (isPlaying) {
        endGame();
    } else {
        startGame();
    }
});

document.addEventListener("keydown", function (event) {
    const direction = getDirectionFromKey(event.key);

    if (direction === null) {
        return;
    }

    // Ngăn trình duyệt cuộn trang khi người chơi dùng phím mũi tên.
    event.preventDefault();
    moveEmptyTile(direction);
});

// Vẽ bàn cờ ngay khi trang web vừa được mở.
renderBoard();
