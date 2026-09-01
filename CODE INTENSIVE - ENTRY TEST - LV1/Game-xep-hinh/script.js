const API_BASE_URL = localStorage.getItem("puzzleApiUrl") || "https://mindx-games-playground.onrender.com/api";
const DEVICE_TOKEN_KEY = "puzzleDeviceToken";
const ACCESS_TOKEN_KEY = "puzzleAccessToken";

let board = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0];
let isPlaying = false;
let moveCount = 0;
let elapsedSeconds = 0;
let timerId = null;
let currentGameId = null;
let currentMoves = [];

const gameBoard = document.getElementById("game-board");
const startButton = document.getElementById("start-button");
const moveCountText = document.getElementById("move-count");
const winMessage = document.getElementById("win-message");
const timerText = document.getElementById("timer");
const historyBody = document.getElementById("history-body");
const playerName = document.getElementById("player-name");
const changeNameButton = document.getElementById("change-name-button");
const recoverPlayerButton = document.getElementById("recover-player-button");
const apiMessage = document.getElementById("api-message");
const recoveryCard = document.getElementById("recovery-card");
const recoveryCode = document.getElementById("recovery-code");
const copyRecoveryButton = document.getElementById("copy-recovery-button");

function renderBoard() {
    gameBoard.innerHTML = "";
    for (let i = 0; i < board.length; i++) {
        const tileValue = board[i];
        const tile = document.createElement("div");
        tile.classList.add("tile");
        if (tileValue === 0) {
            tile.classList.add("empty-tile");
        } else {
            tile.classList.add(`tile-${tileValue}`);
            tile.textContent = tileValue;
        }
        gameBoard.appendChild(tile);
    }
}

function changeBoardPosition(direction) {
    const emptyIndex = board.indexOf(0);
    const emptyRow = Math.floor(emptyIndex / 4);
    const emptyColumn = emptyIndex % 4;
    let targetIndex = emptyIndex;
    if (direction === "up" && emptyRow > 0) targetIndex = emptyIndex - 4;
    else if (direction === "down" && emptyRow < 2) targetIndex = emptyIndex + 4;
    else if (direction === "left" && emptyColumn > 0) targetIndex = emptyIndex - 1;
    else if (direction === "right" && emptyColumn < 3) targetIndex = emptyIndex + 1;
    if (targetIndex === emptyIndex) return false;
    board[emptyIndex] = board[targetIndex];
    board[targetIndex] = 0;
    return true;
}

function moveEmptyTile(direction) {
    if (!isPlaying || !changeBoardPosition(direction)) return;
    currentMoves.push(direction);
    moveCount++;
    moveCountText.textContent = moveCount;
    renderBoard();
    checkWin();
}

function isBoardSolved() {
    const solvedBoard = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0];
    return solvedBoard.every(function (value, index) {
        return board[index] === value;
    });
}

function formatTime(totalSeconds) {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

function stopTimer() {
    if (timerId !== null) {
        clearInterval(timerId);
        timerId = null;
    }
}

function startTimer() {
    stopTimer();
    timerId = setInterval(function () {
        elapsedSeconds++;
        timerText.textContent = formatTime(elapsedSeconds);
    }, 1000);
}

async function apiRequest(path, options = {}, canRetry = true) {
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
    if (accessToken) headers.Authorization = `Bearer ${accessToken}`;
    const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });

    if (response.status === 401 && canRetry && localStorage.getItem(DEVICE_TOKEN_KEY)) {
        await refreshAccessToken();
        return apiRequest(path, options, false);
    }

    if (!response.ok) {
        const error = await response.json().catch(function () { return {}; });
        throw new Error(error.message || "Không thể kết nối với máy chủ");
    }
    if (response.status === 204) return null;
    return response.json();
}

async function refreshAccessToken() {
    const deviceToken = localStorage.getItem(DEVICE_TOKEN_KEY);
    const response = await fetch(`${API_BASE_URL}/players/token`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ deviceToken })
    });
    if (!response.ok) {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        localStorage.removeItem(DEVICE_TOKEN_KEY);
        throw new Error("Phiên đăng nhập đã hết hạn");
    }
    const data = await response.json();
    localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
}

async function initializePlayer() {
    try {
        const deviceToken = localStorage.getItem(DEVICE_TOKEN_KEY);
        const data = await apiRequest("/players/anonymous", {
            method: "POST",
            body: JSON.stringify({ deviceToken })
        }, false);
        localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
        if (data.deviceToken) localStorage.setItem(DEVICE_TOKEN_KEY, data.deviceToken);
        playerName.textContent = data.nickname;
        startButton.disabled = false;
        changeNameButton.disabled = false;
        apiMessage.textContent = "";
        if (data.recoveryCode) {
            recoveryCode.textContent = data.recoveryCode;
            recoveryCard.hidden = false;
        }
        await loadHistory();
    } catch (error) {
        if (localStorage.getItem(DEVICE_TOKEN_KEY)) {
            localStorage.removeItem(DEVICE_TOKEN_KEY);
            localStorage.removeItem(ACCESS_TOKEN_KEY);
            return initializePlayer();
        }
        apiMessage.textContent = `${error.message}. Hãy kiểm tra Backend tại ${API_BASE_URL}.`;
    }
}

async function loadHistory() {
    const data = await apiRequest("/games/history?page=0&size=20");
    historyBody.innerHTML = "";
    data.items.forEach(function (item, index) {
        const row = document.createElement("tr");
        const numberCell = document.createElement("td");
        const moveCell = document.createElement("td");
        const timeCell = document.createElement("td");
        const statusCell = document.createElement("td");
        numberCell.textContent = index + 1;
        moveCell.textContent = item.moveCount;
        timeCell.textContent = formatTime(item.elapsedSeconds);
        statusCell.textContent = item.status;
        row.append(numberCell, moveCell, timeCell, statusCell);
        historyBody.appendChild(row);
    });
}

async function startGame() {
    startButton.disabled = true;
    apiMessage.textContent = "Đang tạo lượt chơi...";
    try {
        const game = await apiRequest("/games", { method: "POST" });
        currentGameId = game.id;
        board = game.board;
        currentMoves = [];
        moveCount = 0;
        elapsedSeconds = 0;
        moveCountText.textContent = "0";
        timerText.textContent = "00:00";
        winMessage.hidden = true;
        renderBoard();
        isPlaying = true;
        startButton.textContent = "Kết thúc";
        startTimer();
        apiMessage.textContent = "";
    } catch (error) {
        apiMessage.textContent = error.message;
    } finally {
        startButton.disabled = false;
    }
}

async function finishGame(result) {
    if (!currentGameId) return;
    isPlaying = false;
    stopTimer();
    startButton.disabled = true;
    try {
        await apiRequest(`/games/${currentGameId}/finish`, {
            method: "POST",
            body: JSON.stringify({ result, moves: currentMoves })
        });
        if (result === "won") {
            winMessage.hidden = false;
            startButton.textContent = "Chơi lại";
        } else {
            startButton.textContent = "Bắt đầu";
        }
        currentGameId = null;
        await loadHistory();
        apiMessage.textContent = "";
    } catch (error) {
        apiMessage.textContent = error.message;
        startButton.textContent = "Bắt đầu";
    } finally {
        startButton.disabled = false;
    }
}

function checkWin() {
    if (isPlaying && isBoardSolved()) finishGame("won");
}

function getDirectionFromKey(key) {
    const pressedKey = key.toLowerCase();
    if (pressedKey === "w" || pressedKey === "arrowup") return "up";
    if (pressedKey === "s" || pressedKey === "arrowdown") return "down";
    if (pressedKey === "a" || pressedKey === "arrowleft") return "left";
    if (pressedKey === "d" || pressedKey === "arrowright") return "right";
    return null;
}

startButton.addEventListener("click", function () {
    if (isPlaying) finishGame("ended");
    else startGame();
});

changeNameButton.addEventListener("click", async function () {
    const nickname = window.prompt("Nhập biệt danh mới (3–30 ký tự):", playerName.textContent);
    if (nickname === null) return;
    try {
        const data = await apiRequest("/players/me", {
            method: "PATCH",
            body: JSON.stringify({ nickname })
        });
        playerName.textContent = data.nickname;
        apiMessage.textContent = "Đã cập nhật biệt danh.";
    } catch (error) {
        apiMessage.textContent = error.message;
    }
});

recoverPlayerButton.addEventListener("click", async function () {
    const code = window.prompt("Nhập mã khôi phục của bạn:");
    if (code === null || code.trim() === "") return;
    try {
        const data = await apiRequest("/players/recover", {
            method: "POST",
            body: JSON.stringify({ recoveryCode: code.trim() })
        }, false);
        localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
        localStorage.setItem(DEVICE_TOKEN_KEY, data.deviceToken);
        playerName.textContent = data.nickname;
        recoveryCard.hidden = true;
        startButton.disabled = false;
        changeNameButton.disabled = false;
        apiMessage.textContent = "Đã khôi phục hồ sơ thành công.";
        await loadHistory();
    } catch (error) {
        apiMessage.textContent = error.message;
    }
});

copyRecoveryButton.addEventListener("click", async function () {
    await navigator.clipboard.writeText(recoveryCode.textContent);
    copyRecoveryButton.textContent = "Đã sao chép";
});

document.addEventListener("keydown", function (event) {
    const direction = getDirectionFromKey(event.key);
    if (direction === null) return;
    event.preventDefault();
    moveEmptyTile(direction);
});

renderBoard();
initializePlayer();
