let myPlayerId = null;
let currentRoomId = null;
let stompClient = null;
let playerNumber = null; // 1 or 2
let isMyTurn = false;
let activeBoardId = null;

const lobbyScreen = document.getElementById('lobby-screen');
const gameScreen = document.getElementById('game-screen');
const roomDisplay = document.getElementById('room-display');
const statusDisplay = document.getElementById('status-display');



window.addEventListener('load', () => {
    const savedSession = localStorage.getItem('mancalaSession');
    const wasInGame = sessionStorage.getItem('wasInGame');

    if (savedSession && wasInGame) {
        const session = JSON.parse(savedSession);
        reconnectToGame(session.roomId, session.playerId, session.playerNumber);
    } else if (savedSession && !wasInGame) {
        const session = JSON.parse(savedSession);
        const shouldReconnect = confirm(`You have an active game in Room ${session.roomId}. Rejoin?`);
        if (shouldReconnect) {
            reconnectToGame(session.roomId, session.playerId, session.playerNumber);
        } else {
            localStorage.removeItem('mancalaSession');
        }
    }
});

async function reconnectToGame(roomId, playerId, pNum) {
    try {
        const response = await fetch('/api/rooms/' + roomId);
        if (!response.ok) {
            localStorage.removeItem('mancalaSession');
            return;
        }
        const room = await response.json();
        // Pass 'true' because this is an actual reconnect!
        joinWebSocket(roomId, playerId, pNum, room.game, true);
    } catch (e) {
        console.error('Reconnection failed:', e);
        localStorage.removeItem('mancalaSession');
    }
}



document.getElementById('btn-create').addEventListener('click', async (event) => {
    event.preventDefault();

    const playerName = document.getElementById('player-name').value;
    if (!playerName) return alert("Please enter a name!");

    const btn = document.getElementById('btn-create');
    btn.disabled = true;
    btn.innerText = "Creating...";

    try {
        const response = await fetch('/api/rooms', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ playerName: playerName })
        });

        const room = await response.json();
        const fetchedPlayerId = room.game.player1.id;

        // Pass 'false' because this is a brand new room connection
        joinWebSocket(room.roomId, fetchedPlayerId, 1, room.game, false);

    } catch (error) {
        alert("Failed to connect to the server.");
    } finally {
        btn.disabled = false;
        btn.innerText = "Create Room";
    }
});

document.getElementById('btn-join').addEventListener('click', async (event) => {
    event.preventDefault();

    const playerName = document.getElementById('player-name').value;
    const roomId = document.getElementById('room-id-input').value;
    if (!playerName || !roomId) return alert("Please enter a name and room ID!");

    const btn = document.getElementById('btn-join');
    btn.disabled = true;
    btn.innerText = "Joining...";

    try {
        const response = await fetch('/api/rooms/join', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ roomId: roomId, playerName: playerName })
        });

        if (!response.ok) {
            const errorMessage = await response.text();
            alert("Cannot join: " + errorMessage);
            return;
        }

        const room = await response.json();
        const fetchedPlayerId = room.game.player2.id;

        // Pass 'false' because this is a brand new join connection
        joinWebSocket(room.roomId, fetchedPlayerId, 2, room.game, false);

    } catch (error) {
        alert("Failed to connect to the server.");
    } finally {
        btn.disabled = false;
        btn.innerText = "Join Room";
    }
});


function joinWebSocket(roomId, playerId, pNum, initialGameState, isReconnecting = false) {
    myPlayerId = playerId;
    currentRoomId = roomId;
    playerNumber = pNum;

    localStorage.setItem('mancalaSession', JSON.stringify({
        roomId: roomId,
        playerId: playerId,
        playerNumber: pNum
    }));
    sessionStorage.setItem('wasInGame', 'true');

    const socket = new SockJS('/mancala-ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Hides noisy STOMP console logs

    stompClient.connect({}, () => {
        lobbyScreen.classList.remove('active');
        gameScreen.classList.add('active');

        setActiveBoard();
        roomDisplay.innerText = "Room: " + roomId;
        highlightMyPits();

        stompClient.subscribe('/topic/room/' + roomId, (message) => {
            const gameData = JSON.parse(message.body);
            updateBoard(gameData);
        });


        stompClient.subscribe('/topic/room/' + roomId + '/events', (message) => {
            const eventData = JSON.parse(message.body);

            console.log("RECEIVED EVENT FROM SERVER:", eventData);
            // Jackson will serialize the MoveMadeEvent automatically!
            if (eventData.captureOccurred === true) {
                showActionNotification("💥 CAPTURE!", "capture");
            }
            if (eventData.freeTurnGranted === true) {
                showActionNotification("🔄 FREE TURN!", "free-turn");
            }
        });


        if (isReconnecting) {
            stompClient.send('/app/game.reconnect', {}, JSON.stringify({
                roomId: currentRoomId,
                playerId: myPlayerId
            }));
        }

        updateBoard(initialGameState);
    });
}

function setActiveBoard() {
    const board1 = document.getElementById('board-player1');
    const board2 = document.getElementById('board-player2');
    const allBoards = Array.from(document.querySelectorAll('.board-wrapper'));

    if (!board1 || !board2) {
        const fallbackBoard = board1 || board2 || allBoards[0] || null;
        if (!fallbackBoard) {
            console.error('Board containers not found: expected #board-player1 and #board-player2');
            activeBoardId = null;
            return;
        }

        allBoards.forEach(board => {
            board.style.display = board === fallbackBoard ? 'block' : 'none';
        });

        activeBoardId = fallbackBoard.id || null;
        console.warn('Using fallback board because expected board ids are missing in DOM.');
        return;
    }

    if (playerNumber === 1) {
        board1.style.display = 'block';
        board2.style.display = 'none';
        activeBoardId = 'board-player1';
    } else {
        board1.style.display = 'none';
        board2.style.display = 'block';
        activeBoardId = 'board-player2';
    }
}



document.addEventListener('click', (event) => {
    const pitElement = event.target.closest('.pit');
    if (!pitElement || !isMyTurn) return;

    const boardWrapper = pitElement.closest('.board-wrapper');
    if (!boardWrapper || boardWrapper.id !== activeBoardId) return;

    const pitIndex = parseInt(pitElement.dataset.pitIndex, 10);

    // Prevent players from clicking the opponent's side
    if (playerNumber === 1 && (pitIndex < 0 || pitIndex > 5)) return;
    if (playerNumber === 2 && (pitIndex < 7 || pitIndex > 12)) return;

    stompClient.send('/app/game.move', {}, JSON.stringify({
        roomId: currentRoomId,
        playerId: myPlayerId,
        pitIndex: pitIndex
    }));
});



function highlightMyPits() {
    const activeBoard = document.getElementById(activeBoardId);
    if (!activeBoard) return;

    const myStoreIndex = playerNumber === 1 ? 6 : 13;
    const opponentStoreIndex = playerNumber === 1 ? 13 : 6;

    activeBoard.querySelectorAll('.pit').forEach(p => {
        p.classList.remove('my-pit', 'opponent-pit');
        const idx = parseInt(p.dataset.pitIndex, 10);
        const isMine = playerNumber === 1 ? (idx >= 0 && idx <= 5) : (idx >= 7 && idx <= 12);
        p.classList.add(isMine ? 'my-pit' : 'opponent-pit');
    });

    activeBoard.querySelectorAll('.store').forEach(store => {
        store.classList.remove('my-store', 'opponent-store');
        const idx = parseInt(store.dataset.pitIndex, 10);
        if (idx === myStoreIndex) {
            store.classList.add('my-store');
        } else if (idx === opponentStoreIndex) {
            store.classList.add('opponent-store');
        }
    });
}



function updateBoard(gameData) {
    const pits = gameData.board.pits;
    for (let i = 0; i < 14; i++) {
        document.querySelectorAll(`[data-pit-index="${i}"]`).forEach(el => {
            el.innerText = pits[i].stones;
        });
    }

    isMyTurn = (gameData.gameStatus === 'PLAYER_1_TURN' && playerNumber === 1) ||
        (gameData.gameStatus === 'PLAYER_2_TURN' && playerNumber === 2);

    updatePitStates();


    if (gameData.gameStatus === 'WAITING_FOR_PLAYER_2') {
        statusDisplay.innerText = "Waiting for an opponent to join...";
    } else if (gameData.gameStatus === 'PLAYER_1_TURN') {
        statusDisplay.innerText = playerNumber === 1 ? "Your Turn! 🎯" : "Opponent's Turn...";
    } else if (gameData.gameStatus === 'PLAYER_2_TURN') {
        statusDisplay.innerText = playerNumber === 2 ? "Your Turn! 🎯" : "Opponent's Turn...";
    } else if (gameData.gameStatus === 'MATCH_SUSPENDED') {
        statusDisplay.innerText = "Opponent lost connection! Pausing for 30 seconds...";
        disableAllPits();
    } else if (gameData.gameStatus === 'GAME_OVER' || gameData.gameStatus === 'FORFEIT' || gameData.gameStatus === 'PLAYER_DISCONNECTED') {
        if (gameData.winner === 'DRAW') {
            statusDisplay.innerText = "Game Over - It's a Draw!";
        } else if (gameData.winner === myPlayerId) {
            statusDisplay.innerText = "Game Over - You WIN! 🎉";
        } else {
            statusDisplay.innerText = "Game Over - You Lose 😢";
        }
        disableAllPits();
        localStorage.removeItem('mancalaSession');
        sessionStorage.removeItem('wasInGame');
    }
}

function updatePitStates() {
    const activeBoard = document.getElementById(activeBoardId);
    if (!activeBoard) return;

    activeBoard.querySelectorAll('.my-pit').forEach(pit => {
        if (isMyTurn) {
            pit.classList.add('active');
            pit.classList.remove('disabled');
        } else {
            pit.classList.remove('active');
            pit.classList.add('disabled');
        }
    });
}

function disableAllPits() {
    const activeBoard = document.getElementById(activeBoardId);
    if (!activeBoard) return;

    activeBoard.querySelectorAll('.pit').forEach(pit => {
        pit.classList.add('disabled');
        pit.classList.remove('active');
    });
}


function showActionNotification(message, typeClass) {
        const container = document.getElementById('notification-container');
        if (!container) return;

        // Create the banner
        const banner = document.createElement('div');
        banner.classList.add('action-notification');
        if (typeClass) banner.classList.add(typeClass);
        banner.innerText = message;

        // Add to the screen
        container.appendChild(banner);

        // Clean it up after the CSS animation finishes (3 seconds)
        setTimeout(() => {
            if (banner.parentElement) {
                banner.remove();
            }
        }, 3000);
}
