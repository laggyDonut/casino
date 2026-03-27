import React, { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const PokerGameTest = () => {
    // Default to '1' which likely exists in test data as user@example.com
    const [playerName, setPlayerName] = useState('1');
    const [gameId, setGameId] = useState(null);
    const [connected, setConnected] = useState(false);
    const [gameState, setGameState] = useState(null);
    const [logs, setLogs] = useState([]);
    const stompClientRef = useRef(null);

    const log = (msg) => {
        setLogs(prev => [...prev, `${new Date().toLocaleTimeString()} - ${msg}`]);
    };

    useEffect(() => {
        return () => {
             if (stompClientRef.current) {
                 stompClientRef.current.deactivate();
             }
        };
    }, []);

    // 1. REST: Join Game and get Table ID
    const joinGameRest = async () => {
        try {
            log(`Joining game as ${playerName}...`);
            const response = await fetch('http://localhost:8080/api/poker/join-random', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ playerId: playerName, playerName: playerName }) // Sending both just to be safe based on backend record
            });

            if (!response.ok) throw new Error('Failed to join game');

            const data = await response.json();
            log(`Joined successfully! Table ID: ${data.tableId}`);
            setGameId(data.tableId);

            // Auto connect to websocket after rest call
            connectWebSocket(data.tableId);
        } catch (error) {
            log(`Error joining game: ${error.message}`);
        }
    };

    // 2. WebSocket: Connect and Subscribe
    const connectWebSocket = (tableId) => {
        if (stompClientRef.current && stompClientRef.current.active) return;

        log('Connecting to WebSocket...');
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            onConnect: () => {
                log('WebSocket Connected!');
                setConnected(true);

                // Subscribe to table updates
                client.subscribe(`/topic/poker/${tableId}`, (message) => {
                    const body = JSON.parse(message.body);

                    // Check if I am a playing participant in this update
                    // Note: playerName is the ID string like "1" or "2"
                    const amIInGame = body.players && body.players.some(p => p.id == playerName);

                    if (!amIInGame) {
                        // Only apply public update if I'm not playing (observer mode)
                        // otherwise wait for personal update which contains cards
                        setGameState(body);
                    } else {
                        // log('Ignored public update (waiting for personal update with cards)');
                    }
                });

                // Subscribe to Personal updates (for own cards)
                client.subscribe(`/topic/poker/${tableId}/${playerName}`, (message) => {
                    const body = JSON.parse(message.body);
                    log('Received PERSONAL Game Update (with cards!)');
                    setGameState(body);
                });

                // Send initial join message via WebSocket to update engine
                // Using client instance directly to avoid race condition with ref
                client.publish({
                    destination: '/app/poker/join',
                    body: JSON.stringify({
                        gameId: tableId,
                        playerId: playerName,
                        actionType: 'CHECK', // Dummy action for join
                        amount: 0
                    })
                });
                log(`Sent JOIN to /app/poker/join`);
            },
            onStompError: (frame) => {
                log(`Broker reported error: ${frame.headers['message']}`);
                log(`Additional details: ${frame.body}`);
            },
            onWebSocketClose: () => {
                log('WebSocket connection closed');
                setConnected(false);
            }
        });

        client.activate();
        stompClientRef.current = client;
    };

    const sendWsMessage = (destination, payload) => {
        if (stompClientRef.current && stompClientRef.current.active) {
            stompClientRef.current.publish({
                destination: destination,
                body: JSON.stringify(payload)
            });
            log(`Sent to ${destination}: ${JSON.stringify(payload)}`);
        } else {
            log('WebSocket is not connected');
        }
    };

    // 3. Actions
    const sendAction = (actionType, amount = 0) => {
        if (!gameId) return;

        sendWsMessage('/app/poker/action', {
            gameId: gameId,
            playerId: playerName,
            actionType: actionType,
            amount: amount
        });
    };

    const renderCard = (card) => {
        if (!card) return null;
        const suitSymbols = { 'HEARTS': '♥', 'DIAMONDS': '♦', 'CLUBS': '♣', 'SPADES': '♠' };
        const color = (card.suit === 'HEARTS' || card.suit === 'DIAMONDS') ? 'red' : 'black';
        return (
            <span style={{
                color,
                border: '1px solid #ccc',
                padding: '2px 5px',
                borderRadius: '4px',
                margin: '0 2px',
                background: 'white',
                fontWeight: 'bold'
            }}>
                {card.rank}{suitSymbols[card.suit] || card.suit}
            </span>
        );
    };

    const isMyTurn = gameState && gameState.players[gameState.actionPosition]?.id == playerName;

    return (
        <div style={{ padding: '20px', fontFamily: 'Arial', background: '#333', minHeight: '100vh', color: 'white' }}>
            <h1 style={{color: '#fff'}}>Poker Table {gameId}</h1>

            <div style={{ marginBottom: '20px', background: '#444', padding: '10px', borderRadius: '8px' }}>
                <h3 style={{marginTop: 0}}>Configuration</h3>
                <label>Player Name (ID): </label>
                <input
                    value={playerName}
                    onChange={(e) => setPlayerName(e.target.value)}
                    disabled={connected}
                    style={{padding: '5px'}}
                />
                <button
                    onClick={joinGameRest}
                    disabled={connected}
                    style={{ marginLeft: '10px', padding: '5px 15px', cursor: connected ? 'default' : 'pointer' }}
                >
                    {connected ? 'Connected' : 'Find Table & Join'}
                </button>
            </div>

            {connected && gameState && (
                <div style={{ position: 'relative', height: '500px', background: '#250', borderRadius: '100px', border: '10px solid #630', margin: '20px auto', maxWidth: '800px' }}>

                    {/* Community Cards */}
                    <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', display: 'flex', gap: '5px' }}>
                        {gameState.board.map((card, i) => <div key={i}>{renderCard(card)}</div>)}
                        {gameState.board.length === 0 && <div style={{opacity: 0.5}}>Waiting for flop...</div>}
                    </div>

                    {/* Pot */}
                    <div style={{ position: 'absolute', top: '60%', left: '50%', transform: 'translate(-50%, -50%)', background: '#0008', padding: '5px 10px', borderRadius: '10px'}}>
                        Pot: {gameState.potTotal}
                    </div>

                    {/* Players */}
                    {gameState.players.map((player, index) => {
                        const angle = (index / gameState.players.length) * 2 * Math.PI;
                        const radius = 220; // Distance from center
                        const left = 50 + (radius * Math.cos(angle) / 400 * 50); // Normalized %
                        const top = 50 + (radius * Math.sin(angle) / 250 * 50);

                        const isTurn = index === gameState.actionPosition;
                        const isMe = player.id == playerName.toString();

                        return (
                            <div key={index} style={{
                                position: 'absolute',
                                left: `${left}%`,
                                top: `${top}%`,
                                transform: 'translate(-50%, -50%)',
                                background: isTurn ? '#ffeb3b' : (isMe ? '#b3e5fc' : '#fff'),
                                color: 'black',
                                padding: '10px',
                                borderRadius: '10px',
                                border: isTurn ? '3px solid orange' : '1px solid #999',
                                textAlign: 'center',
                                width: '120px',
                                boxShadow: '0 4px 8px rgba(0,0,0,0.3)'
                            }}>
                                <div style={{fontWeight: 'bold'}}>{player.name} {isMe ? '(YOU)' : ''}</div>
                                <div>Chips: {player.chips}</div>
                                <div>Bet: {player.currentBet}</div>
                                {player.status !== 'ACTIVE' && <div style={{color: 'red', fontSize: '0.8em'}}>{player.status}</div>}

                                {/* Hole Cards */}
                                <div style={{marginTop: '5px'}}>
                                    {(player.holeCards || player.cards) && (player.holeCards || player.cards).length > 0 ? (
                                        (player.holeCards || player.cards).map((c, i) => <span key={i}>{renderCard(c)}</span>)
                                    ) : (
                                        isMe ? <span>(No Cards)</span> : <span style={{fontSize: '20px'}}>🂠🂠</span>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {/* Controls */}
            {connected && (
                <div style={{
                    position: 'fixed',
                    bottom: '20px',
                    left: '50%',
                    transform: 'translate(-50%, 0)',
                    background: '#222',
                    padding: '20px',
                    borderRadius: '15px',
                    display: 'flex',
                    gap: '10px',
                    boxShadow: '0 -5px 20px rgba(0,0,0,0.5)',
                    opacity: isMyTurn ? 1 : 0.5,
                    pointerEvents: isMyTurn ? 'auto' : 'none'
                }}>
                    <button onClick={() => sendAction('FOLD')} style={{padding: '10px 20px', background: '#d32f2f', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>FOLD</button>
                    <button onClick={() => sendAction('CHECK')} style={{padding: '10px 20px', background: '#1976d2', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>CHECK</button>
                    <button onClick={() => sendAction('CALL')} style={{padding: '10px 20px', background: '#388e3c', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>CALL</button>
                    <button onClick={() => sendAction('RAISE', 50)} style={{padding: '10px 20px', background: '#fbc02d', color: 'black', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>RAISE 50</button>
                    <button onClick={() => sendAction('ALL_IN')} style={{padding: '10px 20px', background: '#7b1fa2', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>ALL IN</button>
                </div>
            )}
            {!isMyTurn && connected && <div style={{position: 'fixed', bottom: '100px', left: '50%', transform: 'translate(-50%,0)', background: 'rgba(0,0,0,0.7)', padding: '5px 10px', borderRadius: '5px'}}>Waiting for opponents...</div>}

            {/* Logs & Debug */}
            <div style={{ display: 'flex', gap: '20px', marginTop: '50px' }}>
                <div style={{ flex: 1, border: '1px solid #555', padding: '10px', background: '#222', borderRadius: '5px' }}>
                    <h3 style={{marginTop: 0}}>Game JSON (Debug)</h3>
                    <div style={{color: 'cyan', marginBottom: '5px'}}>
                        Turn: {gameState ? gameState.actionPosition : '-'} (PlayerID: {gameState && gameState.players[gameState.actionPosition] ? gameState.players[gameState.actionPosition].id : '?'})
                    </div>
                    <pre style={{ overflow: 'auto', maxHeight: '200px', fontSize: '10px', color: '#0f0' }}>
                        {gameState ? JSON.stringify(gameState, null, 2) : 'No game state...'}
                    </pre>
                </div>

                <div style={{ flex: 1, border: '1px solid #555', padding: '10px', background: '#222', borderRadius: '5px' }}>
                    <h3 style={{marginTop: 0}}>First Player Mock (Debug)</h3>
                    <pre style={{ overflow: 'auto', maxHeight: '200px', fontSize: '10px', color: 'orange' }}>
                        {gameState && gameState.players && gameState.players.length > 0
                            ? JSON.stringify(gameState.players[0], null, 2)
                            : 'No players...'}
                    </pre>
                </div>

                <div style={{ flex: 1, border: '1px solid #555', padding: '10px', background: '#222', borderRadius: '5px' }}>
                    <h3 style={{marginTop: 0}}>Logs</h3>
                    <div style={{ overflow: 'auto', maxHeight: '200px', fontSize: '10px', color: '#aaa' }}>
                        {logs.slice().reverse().map((l, i) => <div key={i}>{l}</div>)}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PokerGameTest;

