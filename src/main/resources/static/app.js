// --- 1. GAME STATE VARIABLES ---
let myPlayerId = null;
let currentRoomId = null;
let stompClient = null;
let playerNumber = null; // 1 or 2
let isMyTurn = false;

// --- 2. HTML ELEMENTS ---
const lobbyScreen = document.getElementById('lobby-screen');
const gameScreen = document.getElementById('game-screen');
const roomDisplay = document.getElementById('room-display');
const statusDisplay = document.getElementById('status-display');

// --- 2.5 CHECK FOR SAVED SESSION ON PAGE LOAD ---
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
        joinWebSocket(roomId, playerId, pNum, room.game);
    } catch (e) {
        console.error('Reconnection failed:', e);
        localStorage.removeItem('mancalaSession');
    }
}

// --- 3. REST API CALLS (Lobby Phase) ---

document.getElementById('btn-create').addEventListener('click', async (event) => {
    event.preventDefault(); // Stop page refresh!

    const playerName = document.getElementById('player-name').value;
    if (!playerName) return alert("Please enter a name!");

    const btn = document.getElementById('btn-create');
    btn.disabled = true;
    btn.innerText = "Creating...";

    try {
        const response = await fetch('/api/rooms/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ playerName: playerName })
        });

        const room = await response.json();

        // Grab ID directly from the game object
        const fetchedPlayerId = room.game.player1.id;
        joinWebSocket(room.roomId, fetchedPlayerId, 1, room.game);

    } catch (error) {
        alert("Failed to connect to the server.");
    } finally {
        btn.disabled = false;
        btn.innerText = "Create Room";
    }
});

document.getElementById('btn-join').addEventListener('click', async (event) => {
    event.preventDefault(); // Stop page refresh!

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

        // Read the exact error from Spring Boot
        if (!response.ok) {
            const errorMessage = await response.text();
            alert("Cannot join: " + errorMessage);
            return;
        }

        const room = await response.json();

        // Grab ID directly from the game object
        const fetchedPlayerId = room.game.player2.id;
        joinWebSocket(room.roomId, fetchedPlayerId, 2, room.game);

    } catch (error) {
        alert("Failed to connect to the server.");
    } finally {
        btn.disabled = false;
        btn.innerText = "Join Room";
    }
});

// --- 4. WEBSOCKETS (Gameplay Phase) ---

function joinWebSocket(roomId, playerId, pNum, initialGameState) {
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
    stompClient.debug = null;

    stompClient.connect({}, () => {
        lobbyScreen.classList.remove('active');
        gameScreen.classList.add('active');
        roomDisplay.innerText = "Room: " + roomId;
        highlightMyPits();

        stompClient.subscribe('/topic/room/' + roomId, (message) => {
            const gameData = JSON.parse(message.body);
            updateBoard(gameData);
        });

        stompClient.send('/app/game.reconnect', {}, JSON.stringify({
            roomId: currentRoomId,
            playerId: myPlayerId
        }));

        updateBoard(initialGameState);
    });
}

// --- 5. MAKING A MOVE ---

document.querySelectorAll('.pit').forEach(pitElement => {
    pitElement.addEventListener('click', (event) => {
        if (!isMyTurn) return;

        const pitIndex = parseInt(event.target.id.split('-')[1]);

        if (playerNumber === 1 && (pitIndex < 0 || pitIndex > 5)) return;
        if (playerNumber === 2 && (pitIndex < 7 || pitIndex > 12)) return;

        const command = {
            roomId: currentRoomId,
            playerId: myPlayerId,
            pitIndex: pitIndex
        };
        stompClient.send('/app/game.move', {}, JSON.stringify(command));
    });
});

// --- 6. HIGHLIGHT MY PITS ---

function highlightMyPits() {
    if (playerNumber === 1) {
        document.querySelectorAll('.player1-row .pit').forEach(p => p.classList.add('my-pit'));
        document.querySelectorAll('.player2-row .pit').forEach(p => p.classList.add('opponent-pit'));
    } else {
        document.querySelectorAll('.player2-row .pit').forEach(p => p.classList.add('my-pit'));
        document.querySelectorAll('.player1-row .pit').forEach(p => p.classList.add('opponent-pit'));
    }
}

// --- 7. UPDATING THE SCREEN ---

function updateBoard(gameData) {
    const pits = gameData.board.pits;
    for (let i = 0; i < 14; i++) {
        document.getElementById('pit-' + i).innerText = pits[i].stones;
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
    document.querySelectorAll('.my-pit').forEach(pit => {
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
    document.querySelectorAll('.pit').forEach(pit => {
        pit.classList.add('disabled');
        pit.classList.remove('active');
    });
}