import React, { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const PokerGameTest = () => {
    // Auto-generated numeric ID for the backend (hidden from user)
    const [playerName] = useState(() => Math.floor(Math.random() * 100000).toString());
    const [displayName, setDisplayName] = useState('');
    const [gameId, setGameId] = useState(null);
    const [connected, setConnected] = useState(false);
    const [gameState, setGameState] = useState(null);
    const [raiseAmount, setRaiseAmount] = useState(50);
    const [emotes, setEmotes] = useState({});
    const [splats, setSplats] = useState({});
    const [flyingEggs, setFlyingEggs] = useState([]);
    const [showEmoteMenu, setShowEmoteMenu] = useState(false);
    const [hasJoined, setHasJoined] = useState(false);
    const [winnerData, setWinnerData] = useState(null);
    const [turnTimer, setTurnTimer] = useState(null); // { playerId, deadlineMs, seconds }
    const [timeLeft, setTimeLeft] = useState(null); // seconds remaining
    const stompClientRef = useRef(null);

    const log = (msg) => {
        console.log(`[Poker] ${new Date().toLocaleTimeString()} - ${msg}`);
    };

    useEffect(() => {
        return () => {
             if (stompClientRef.current) {
                 stompClientRef.current.deactivate();
             }
        };
    }, []);

    // Countdown interval for turn timer
    useEffect(() => {
        if (!turnTimer) { setTimeLeft(null); return; }
        const tick = () => {
            const remaining = Math.max(0, Math.ceil((turnTimer.deadlineMs - Date.now()) / 1000));
            setTimeLeft(remaining);
            if (remaining <= 0) setTurnTimer(null);
        };
        tick();
        const interval = setInterval(tick, 200);
        return () => clearInterval(interval);
    }, [turnTimer]);

    // 1. REST: Join Game and get Table ID
    const joinGameRest = async () => {
        try {
            log(`Joining game as ${playerName}...`);
            const response = await fetch('http://localhost:8080/api/poker/join-random', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ playerId: playerName, playerName: displayName || 'Player_' + playerName }) 
            });

            if (!response.ok) throw new Error('Failed to join game');

            const data = await response.json();
            log(`Joined successfully! Table ID: ${data.tableId}`);
            setGameId(data.tableId);
            setGameState(null); // Clear old state
            setWinnerData(null); // Clear winner overlay
            setHasJoined(false);

            // Auto connect to websocket after rest call
            connectWebSocket(data.tableId);
        } catch (error) {
            log(`Error joining game: ${error.message}`);
        }
    };

    // 2. WebSocket: Connect and Subscribe
    const connectWebSocket = (tableId) => {
        if (stompClientRef.current) {
            log('Deactivating old WebSocket client...');
            stompClientRef.current.deactivate();
        }

        log('Connecting to WebSocket...');
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            onConnect: () => {
                log('WebSocket Connected!');
                setConnected(true);

                // Subscribe to table updates
                client.subscribe(`/topic/poker/${tableId}`, (message) => {
                    const body = JSON.parse(message.body);
                    const amIInGame = body?.players && body.players.some(p => p.id == playerName);

                    if (amIInGame) {
                        setHasJoined(true);
                    }
                    if (!amIInGame) {
                        setGameState(body);
                    }
                });

                // Subscribe to Personal updates (for own cards)
                client.subscribe(`/topic/poker/${tableId}/${playerName}`, (message) => {
                    const body = JSON.parse(message.body);
                    setHasJoined(true);
                    log('Received PERSONAL Game Update (with cards!)');
                    setGameState(body);
                });

                // Subscribe to Emotes
                client.subscribe(`/topic/poker/${tableId}/emotes`, (message) => {
                    const emoteDto = JSON.parse(message.body);
                    
                    if (emoteDto.emote === '🥚' && emoteDto.targetId) {
                        const flightId = Date.now() + Math.random();
                        setFlyingEggs(prev => [...prev, {
                            id: flightId,
                            from: emoteDto.playerId, // Sender's name
                            to: emoteDto.targetId   // Target's ID
                        }]);
                        
                        // Fly time is 600ms
                        setTimeout(() => {
                            setFlyingEggs(prev => prev.filter(e => e.id !== flightId));
                            
                            setSplats(prev => {
                                const currentTargetSplats = prev[emoteDto.targetId] || [];
                                // Limit to max 5 (keep the 4 newest, add the new one)
                                const keptSplats = currentTargetSplats.slice(-4);
                                return { ...prev, [emoteDto.targetId]: [...keptSplats, { id: flightId }] };
                            });
                            
                            setTimeout(() => {
                                setSplats(prev => {
                                    const newSplats = { ...prev };
                                    if (newSplats[emoteDto.targetId]) {
                                        newSplats[emoteDto.targetId] = newSplats[emoteDto.targetId].filter(s => s.id !== flightId);
                                    }
                                    return newSplats;
                                });
                            }, 5000);
                        }, 550);
                    } else {
                        setEmotes(prev => ({ ...prev, [emoteDto.playerId]: emoteDto.emote }));
                        
                        // Auto-hide emote after 3 seconds
                        setTimeout(() => {
                            setEmotes(prev => {
                                const newEmotes = {...prev};
                                if (newEmotes[emoteDto.playerId] === emoteDto.emote) {
                                    delete newEmotes[emoteDto.playerId];
                                }
                                return newEmotes;
                            });
                        }, 3000);
                    }
                });

                // Subscribe to Winners
                client.subscribe(`/topic/poker/${tableId}/winners`, (message) => {
                    const data = JSON.parse(message.body);
                    log('Received WINNER Update: ' + JSON.stringify(data));
                    setWinnerData(data);
                    
                    // Auto-hide the winner overlay just before the next hand begins (backend waits 5s)
                    setTimeout(() => {
                        setWinnerData(null);
                    }, 4800);
                });

                // Subscribe to Turn Timer
                client.subscribe(`/topic/poker/${tableId}/timer`, (message) => {
                    const data = JSON.parse(message.body);
                    log('Received TIMER: ' + data.seconds + 's for player ' + data.playerId);
                    setTurnTimer(data);
                });

                // Subscribe to Table Redirect (when current table is full)
                client.subscribe(`/topic/poker/${tableId}/${playerName}/redirect`, (message) => {
                    const data = JSON.parse(message.body);
                    log('Received TABLE REDIRECT to: ' + data.newTableId);
                    
                    // Deactivate old connection and reconnect to the new table
                    client.deactivate();
                    setGameId(data.newTableId);
                    setGameState(null);
                    setWinnerData(null);
                    setHasJoined(false);
                    
                    // Short delay to let old connection close cleanly
                    setTimeout(() => {
                        connectWebSocket(data.newTableId);
                    }, 300);
                });

                client.publish({
                    destination: '/app/poker/join',
                    body: JSON.stringify({
                        gameId: tableId,
                        playerId: playerName,
                        displayName: displayName || 'Player_' + playerName,
                        actionType: 'CHECK',
                        amount: 0
                    })
                });
                log(`Sent JOIN to /app/poker/join`);
            },
            onStompError: (frame) => {
                log(`Broker reported error: ${frame.headers['message']}`);
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

    const sendEmote = (emoji, targetPlayerId = null) => {
        if (!gameId) return;
        sendWsMessage('/app/poker/emote', {
            gameId: gameId,
            playerId: playerName,
            emote: emoji,
            targetId: targetPlayerId
        });
        if (!targetPlayerId) {
            setShowEmoteMenu(false);
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

    const renderCard = (card, large = false) => {
        if (!card) return null;
        const suitSymbols = { 'HEARTS': '♥', 'DIAMONDS': '♦', 'CLUBS': '♣', 'SPADES': '♠' };
        const isRed = card.suit === 'HEARTS' || card.suit === 'DIAMONDS';
        const color = isRed ? '#d32f2f' : '#212121';
        return (
            <div style={{
                color,
                background: 'linear-gradient(135deg, #fff 0%, #eee 100%)',
                border: '1px solid #ccc',
                padding: large ? '15px 20px' : '5px 10px',
                borderRadius: '8px',
                fontWeight: '900',
                fontSize: large ? '2rem' : '1.2rem',
                boxShadow: '2px 4px 8px rgba(0,0,0,0.4)',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
            }}>
                {card.rank}<span style={{fontSize: large ? '1.5rem' : '1rem', marginLeft: '2px'}}>{suitSymbols[card.suit] || card.suit}</span>
            </div>
        );
    };

    // --- Dynamic Computations ---
    const players = gameState?.players || [];
    const actionPosition = gameState?.actionPosition ?? -1;
    const isMyTurn = gameState && players[actionPosition] && players[actionPosition].id == playerName;
    const me = players.find(p => p.id == playerName);
    const currentHighestBet = players.reduce((max, p) => Math.max(max, p.currentBet || 0), 0) || 0;
    const myCurrentBet = me?.currentBet || 0;
    const toCall = Math.max(0, currentHighestBet - myCurrentBet);
    const potTotal = gameState?.potTotal || 0;
    
    // Minimum raise calculation (Usually at least big blind, or previous bet)
    // If someone already bet, min raise is usually matching the bet + increasing by at least that bet
    // If no one bet, it's the big blind (fallback assumed 20)
    const minTotalBet = currentHighestBet > 0 ? currentHighestBet * 2 : 20;

    // Enforce minimum in local state strictly immediately
    useEffect(() => {
        if (raiseAmount < minTotalBet) setRaiseAmount(minTotalBet);
    }, [minTotalBet, raiseAmount]);

    // Validation flags for Action buttons
    const canCheck = isMyTurn && toCall === 0;
    const canCall = isMyTurn && toCall > 0;
    const canRaise = isMyTurn && me && me.chips > toCall;
    const canAllIn = isMyTurn && me && me.chips > 0;

    const isBankrupt = hasJoined && connected && gameState && !players.some(p => String(p.id) === String(playerName));

    const btnStyle = (bg, enabled, color = 'white') => ({
        padding: '12px 24px', 
        background: enabled ? bg : '#444', 
        color: enabled ? color : '#777', 
        border: 'none', 
        borderRadius: '8px', 
        cursor: enabled ? 'pointer' : 'not-allowed',
        fontWeight: '900',
        fontSize: '1rem',
        textTransform: 'uppercase',
        boxShadow: enabled ? `0 4px 15px ${bg}88` : 'inset 0 2px 4px rgba(0,0,0,0.5)',
        transition: 'all 0.2s ease',
        opacity: enabled ? 1 : 0.6
    });

    const getPos = (identifier) => {
        if (!gameState || !players || players.length === 0) return { top: '50%', left: '50%', transform: 'translate(-50%, -50%)' };
        const index = players.findIndex(p => String(p?.id) === String(identifier) || p?.name === identifier);
        if (index === -1) return { top: '50%', left: '50%', transform: 'translate(-50%, -50%)' };
        
        const total = players.length;
        const meIdx = players.findIndex(p => String(p.id) === String(playerName));
        const shiftedIdx = meIdx !== -1 ? (index - meIdx + total) % total : index;
        const angle = (shiftedIdx / total) * 2 * Math.PI + Math.PI / 2;
        const radiusX = 400; // Oval horizontal scaling
        const radiusY = 240; // Oval vertical scaling
        const left = 50 + (radiusX * Math.cos(angle) / 1000 * 100); 
        const top = 50 + (radiusY * Math.sin(angle) / 600 * 100);
        return { top: `${top}%`, left: `${left}%`, transform: 'translate(-50%, -50%)' };
    };

    const EggFlight = ({ egg, getPos }) => {
        const [pos, setPos] = useState(getPos(egg.from));
        useEffect(() => {
            const timer = setTimeout(() => setPos(getPos(egg.to)), 50);
            return () => clearTimeout(timer);
        }, [egg.to, getPos]);
        return (
            <div style={{
                position: 'absolute', ...pos, zIndex: 999, pointerEvents: 'none',
                transition: 'all 0.55s cubic-bezier(0.2, 0.8, 0.2, 1)',
                filter: 'drop-shadow(0 15px 15px rgba(0,0,0,0.5))'
            }}>
                <div style={{ animation: 'spin 0.5s linear infinite', fontSize: '3.5rem' }}>🥚</div>
            </div>
        );
    };

    return (
        <div style={{ padding: '0', fontFamily: '"Inter", "Segoe UI", Tahoma, Geneva, Verdana, sans-serif', background: 'radial-gradient(circle at 50% 50%, #212121 0%, #000 100%)', minHeight: '100vh', color: 'white', position: 'relative', overflow: 'hidden' }}>
            
            {/* Header Toolbar */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '15px 30px', background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(10px)', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                <h1 style={{color: '#d4af37', textShadow: '0 2px 4px rgba(0,0,0,0.8)', margin: 0, fontSize: '1.8rem', letterSpacing: '1px'}}>PokerCasino VIP</h1>
                <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                    {!connected ? (
                        <>
                            <div style={{ display: 'flex', alignItems: 'center', background: 'rgba(255,255,255,0.1)', borderRadius: '6px', overflow: 'hidden', border: '1px solid #444' }}>
                                <span style={{padding: '8px 12px', background: '#333', color: '#aaa', fontSize: '0.9rem'}}>Username</span>
                                <input
                                    value={displayName}
                                    onChange={(e) => setDisplayName(e.target.value)}
                                    placeholder={'Player_' + playerName}
                                    style={{padding: '8px 15px', border: 'none', background: 'transparent', color: 'white', width: '120px', outline: 'none', fontWeight: 'bold'}}
                                />
                            </div>
                            <button
                                onClick={joinGameRest}
                                style={{ padding: '8px 20px', cursor: 'pointer', background: 'linear-gradient(to bottom, #43a047, #2e7d32)', color: 'white', border: 'none', borderRadius: '6px', fontWeight: 'bold', boxShadow: '0 4px 10px rgba(67, 160, 71, 0.4)' }}
                            >
                                JOIN TABLE
                            </button>
                        </>
                    ) : (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <span style={{ color: '#4caf50', display: 'flex', alignItems: 'center', gap: '5px' }}>
                                <div style={{width: '10px', height: '10px', background: '#4caf50', borderRadius: '50%', boxShadow: '0 0 10px #4caf50'}}></div>
                                Connected (Table {gameId})
                            </span>
                            <span style={{background: '#333', padding: '5px 15px', borderRadius: '20px', fontSize: '0.9rem'}}>{displayName || 'Player_' + playerName}</span>
                        </div>
                    )}
                </div>
            </div>

            {/* Bankrupt Overlay */}
            {isBankrupt && (
                <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.92)', zIndex: 1000, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', backdropFilter: 'blur(10px)' }}>
                    <h1 style={{color: '#f44336', fontSize: '4.5rem', marginBottom: '10px', textShadow: '0 5px 15px rgba(244, 67, 54, 0.4)'}}>B R O K E 💸</h1>
                    <p style={{fontSize: '1.4rem', color: '#ccc', marginBottom: '40px', maxWidth: '600px', textAlign: 'center'}}>
                        Du hast alle deine Chips verloren und wurdest vom Tisch verwiesen. Bitte lade dein Konto auf, um erneut teilzunehmen.
                    </p>
                    <div style={{display: 'flex', gap: '20px'}}>
                        <button onClick={() => window.location.reload()} style={{padding: '15px 30px', background: 'linear-gradient(to bottom, #43a047, #2e7d32)', color: 'white', border: 'none', borderRadius: '8px', fontSize: '1.2rem', cursor: 'pointer', fontWeight: 'bold', boxShadow: '0 8px 20px rgba(67, 160, 71, 0.4)'}}>
                            Mehr Einzahlen & Neu Laden
                        </button>
                        <button onClick={() => { setConnected(false); setGameId(null); setHasJoined(false); stompClientRef.current?.deactivate(); }} style={{padding: '15px 30px', background: 'linear-gradient(to bottom, #d32f2f, #b71c1c)', color: 'white', border: 'none', borderRadius: '8px', fontSize: '1.2rem', cursor: 'pointer', fontWeight: 'bold', boxShadow: '0 8px 20px rgba(211, 47, 47, 0.4)'}}>
                            Tisch Verlassen
                        </button>
                    </div>
                </div>
            )}

            {/* Winner Overlay */}
            {winnerData && (
                <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.85)', zIndex: 1100, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', backdropFilter: 'blur(5px)', animation: 'fadeIn 0.5s ease' }}>
                    
                    <div style={{ fontSize: '6rem', animation: 'bounce 2s infinite', marginBottom: '20px', filter: 'drop-shadow(0 0 20px #ffeb3b)' }}>🏆</div>
                    
                    <h1 style={{ color: '#ffb74d', fontSize: '3.5rem', margin: '0 0 10px 0', textShadow: '0 5px 15px rgba(255, 183, 77, 0.6)', textTransform: 'uppercase', letterSpacing: '4px' }}>
                        {winnerData.winnerNames && winnerData.winnerNames.length > 1 ? 'Split Pot!' : 'Winner!'}
                    </h1>
                    
                    <div style={{ background: 'linear-gradient(135deg, rgba(255,213,79,0.2) 0%, rgba(255,111,0,0.2) 100%)', border: '2px solid #ffb74d', borderRadius: '20px', padding: '30px 50px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '15px', boxShadow: '0 10px 30px rgba(0,0,0,0.8), inset 0 0 20px rgba(255,183,77,0.3)' }}>
                        
                        <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap', justifyContent: 'center' }}>
                            {winnerData.winnerNames?.map((name, idx) => (
                                <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                    <span style={{ color: '#fff', fontSize: '2rem', fontWeight: 'bold' }}>{name} {winnerData.winnerIds && winnerData.winnerIds[idx] == playerName ? '(YOU)' : ''}</span>
                                    {winnerData.payouts && winnerData.payouts[winnerData.winnerIds[idx]] && (
                                        <span style={{ color: '#81c784', fontSize: '1.5rem', fontWeight: '900', marginTop: '5px' }}>+ ${winnerData.payouts[winnerData.winnerIds[idx]]}</span>
                                    )}
                                </div>
                            ))}
                        </div>

                        <div style={{ marginTop: '10px', fontSize: '1.2rem', color: '#ccc', textTransform: 'uppercase', letterSpacing: '2px' }}>
                            Total Pot: <span style={{ color: '#ffeb3b', fontWeight: 'bold' }}>${winnerData.potTotal}</span>
                        </div>
                    </div>

                    <p style={{ color: '#aaa', fontSize: '1.1rem', marginTop: '30px', animation: 'pulse 1.5s infinite' }}>Next hand starting shortly...</p>
                </div>
            )}

            {/* Loading / Joining Overlay */}
            {connected && !gameState && !isBankrupt && (
                <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.85)', zIndex: 1000, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', backdropFilter: 'blur(10px)' }}>
                    <div style={{ fontSize: '5rem', animation: 'spin 2s linear infinite', marginBottom: '20px' }}>⏳</div>
                    <h2 style={{ color: '#fff', fontSize: '2.5rem', textTransform: 'uppercase', letterSpacing: '4px' }}>Joining Table...</h2>
                    <p style={{ color: '#aaa', fontSize: '1.2rem', maxWidth: '500px', textAlign: 'center', marginTop: '10px' }}>
                        Waiting for server sync...
                    </p>
                    <button 
                        onClick={() => { setConnected(false); setGameId(null); stompClientRef.current?.deactivate(); }}
                        style={{ marginTop: '30px', padding: '10px 25px', background: '#d32f2f', color: 'white', border: 'none', borderRadius: '8px', fontSize: '1.1rem', cursor: 'pointer', fontWeight: 'bold' }}
                    >
                        Cancel
                    </button>
                </div>
            )}

            {/* Main Poker Table Area */}
            {connected && gameState && !isBankrupt && (
                <div style={{ position: 'relative', height: '600px', background: 'radial-gradient(ellipse at center, #1b5e20 0%, #0a2e0e 100%)', borderRadius: '350px 350px 350px 350px', border: '35px solid #1a0f0a', boxShadow: '0 30px 60px rgba(0,0,0,0.9), inset 0 0 50px rgba(0,0,0,1), inset 0 0 10px rgba(255,255,255,0.1)', margin: '40px auto 140px auto', maxWidth: '1000px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                    
                    {/* Inner Table Lip (Leather Padding effect) */}
                    <div style={{position: 'absolute', top: '0', left: '0', right: '0', bottom: '0', borderRadius: '350px', border: '4px solid rgba(0,0,0,0.6)', pointerEvents: 'none', boxShadow: 'inset 0px 5px 15px rgba(255,255,255,0.05)'}}></div>

                    {/* Pot Information Plaque */}
                    {(players.length >= 2 || potTotal > 0) && (
                        <div style={{ background: 'linear-gradient(to bottom, #fbc02d, #f57f17)', padding: '8px 40px', borderRadius: '30px', border: '2px solid #fff9c4', color: '#3e2723', fontSize: '1.5rem', fontWeight: '900', display: 'flex', gap: '20px', textShadow: '0 1px 1px rgba(255,255,255,0.5)', boxShadow: '0 10px 20px rgba(0,0,0,0.6), inset 0 -3px 5px rgba(0,0,0,0.2)', marginBottom: '20px', zIndex: 15 }}>
                            <span style={{letterSpacing: '2px'}}>TOTAL POT</span>
                            <span>${potTotal}</span>
                        </div>
                    )}

                    {/* Community Cards Felt Outline */}
                    {(gameState.players?.length >= 2 || (gameState.board && gameState.board.length > 0)) && (
                        <div style={{ display: 'flex', gap: '15px', background: 'rgba(0,0,0,0.15)', padding: '25px', borderRadius: '20px', boxShadow: 'inset 0 0 30px rgba(0,0,0,0.8)', minHeight: '110px', alignItems: 'center', border: '2px dashed rgba(255,255,255,0.2)' }}>
                            {(!gameState.board || gameState.board.length === 0) ? <div style={{opacity: 0.6, fontStyle: 'italic', color: '#a5d6a7', letterSpacing: '1px', textTransform: 'uppercase', fontWeight: 'bold'}}>Waiting for Flop...</div> : ''}
                            {(gameState.board || []).map((card, i) => <div key={i}>{renderCard(card, true)}</div>)}
                        </div>
                    )}

                    {/* Waiting Lobby Centerpiece */}
                    {gameState?.players?.length < 2 && (
                        <div style={{ position: 'absolute', top: '45%', left: '50%', transform: 'translate(-50%, -50%)', textAlign: 'center', zIndex: 50, background: 'rgba(0,0,0,0.6)', padding: '40px 80px', borderRadius: '30px', border: '2px solid rgba(255,255,255,0.05)', backdropFilter: 'blur(10px)', boxShadow: '0 15px 35px rgba(0,0,0,0.8)' }}>
                            <div style={{ fontSize: '5rem', animation: 'spin 4s linear infinite', marginBottom: '15px', display: 'inline-block', filter: 'drop-shadow(0 0 15px rgba(255,183,77,0.5))' }}>🎰</div>
                            <h2 style={{ color: '#fff', fontSize: '2.5rem', margin: '0 0 10px 0', textShadow: '0 2px 10px rgba(0,0,0,0.9)', textTransform: 'uppercase', letterSpacing: '3px' }}>Waiting for Players</h2>
                            <p style={{ color: '#a5d6a7', fontSize: '1.2rem', letterSpacing: '4px', margin: 0, fontWeight: 'bold' }}>{(gameState?.players?.length || 0)} / 6 SEATED</p>
                        </div>
                    )}

                    {/* Players Orbit */}
                    {(gameState.players || []).map((player, index) => {
                        const total = gameState.players?.length || 1;
                        const meIdx = (gameState.players || []).findIndex(p => String(p?.id) === String(playerName));
                        const shiftedIdx = meIdx !== -1 ? (index - meIdx + total) % total : index;
                        const angle = (shiftedIdx / total) * 2 * Math.PI + Math.PI / 2;
                        const radiusX = 400; // Oval horizontal scaling
                        const radiusY = 240; // Oval vertical scaling
                        const left = 50 + (radiusX * Math.cos(angle) / 1000 * 100); 
                        const top = 50 + (radiusY * Math.sin(angle) / 600 * 100);

                        const isTurn = players && index === (gameState?.actionPosition ?? -1);
                        const isMe = player && String(player.id) === String(playerName);

                        return (
                            <div key={index} style={{
                                position: 'absolute',
                                left: `${left}%`,
                                top: `${top}%`,
                                transform: 'translate(-50%, -50%)',
                                background: isMe ? 'linear-gradient(135deg, rgba(20,40,80,0.95) 0%, rgba(30,60,120,0.95) 100%)' : 'rgba(20, 20, 20, 0.95)',
                                color: 'white',
                                padding: '15px',
                                borderRadius: '15px',
                                border: isTurn ? '3px solid #ffeb3b' : (isMe ? '2px solid #64b5f6' : '1px solid rgba(255,255,255,0.15)'),
                                textAlign: 'center',
                                width: '180px',
                                boxShadow: isTurn ? '0 0 40px rgba(255, 235, 59, 0.6), inset 0 0 15px rgba(255, 235, 59, 0.3)' : '0 15px 25px rgba(0,0,0,0.8)',
                                backdropFilter: 'blur(10px)',
                                zIndex: isTurn ? 20 : 10,
                                transition: 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)',
                                overflow: 'hidden'
                            }}>
                                {/* Turn Timer Bar */}
                                {isTurn && timeLeft != null && (
                                    <div style={{
                                        position: 'absolute', bottom: 0, left: 0, height: '4px',
                                        background: timeLeft > 5 ? '#4caf50' : timeLeft > 3 ? '#ff9800' : '#f44336',
                                        width: `${(timeLeft / 10) * 100}%`,
                                        transition: 'width 0.2s linear, background 0.3s',
                                        borderRadius: '0 2px 0 0'
                                    }} />
                                )}
                                <div style={{fontWeight: '900', fontSize: '1.1rem', marginBottom: '8px', color: isMe ? '#bbdefb' : '#fff', letterSpacing: '0.5px'}}>
                                    {player?.name || 'Unknown'} {isMe && <span style={{fontSize: '0.75rem', opacity: 0.9, background: 'linear-gradient(45deg, #1976d2, #42a5f5)', padding: '2px 6px', borderRadius: '6px', verticalAlign: 'middle', marginLeft: '5px', boxShadow: '0 2px 4px rgba(0,0,0,0.5)'}}>YOU</span>}
                                </div>
                                <div style={{fontSize: '0.9rem', color: '#bcaaa4', marginBottom: '2px', display: 'flex', justifyContent: 'space-between'}}>
                                    <span>Bank</span> <span style={{color: '#fff', fontWeight: 'bold'}}>${player?.chips ?? 0}</span>
                                </div>
                                
                                {/* Player Bet Stacks projected onto the table */}
                                {player?.currentBet > 0 && (
                                    <div style={{
                                        position: 'absolute',
                                        top: top < 50 ? 'calc(100% + 15px)' : '-45px',
                                        left: '50%',
                                        transform: 'translateX(-50%)',
                                        background: 'rgba(0,0,0,0.7)',
                                        border: '1px solid #ffb74d',
                                        borderRadius: '20px',
                                        padding: '4px 12px',
                                        fontSize: '1rem',
                                        fontWeight: 'bold',
                                        color: '#ffb74d',
                                        boxShadow: '0 5px 10px rgba(0,0,0,0.5)',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '5px',
                                        zIndex: 5
                                    }}>
                                        <span style={{fontSize: '1.2rem', filter: 'drop-shadow(1px 1px 0px #000)'}}>🪙</span> ${player?.currentBet}
                                    </div>
                                )}
                                
                                {player?.status !== 'ACTIVE' && (
                                    <div style={{
                                        color: '#ef5350', fontSize: '0.8rem', textTransform: 'uppercase', fontWeight: '900', 
                                        background: 'rgba(239, 83, 80, 0.15)', padding: '4px', borderRadius: '4px', border: '1px solid rgba(239,83,80,0.3)',
                                        marginBottom: '6px'
                                    }}>
                                        {player?.status}
                                    </div>
                                )}

                                {/* Hole Cards */}
                                <div style={{display: 'flex', justifyContent: 'center', gap: '8px', marginTop: '10px'}}>
                                    {!isMe && (player?.holeCards || player?.cards) && (player?.holeCards || player?.cards).length > 0 ? (
                                        (player?.holeCards || player?.cards).map((c, i) => <span key={i} style={{transform: i===0 ? 'rotate(-5deg)' : 'rotate(5deg)'}}>{renderCard(c, false)}</span>)
                                    ) : (
                                        !isMe ? <span style={{fontSize: '28px', color: '#555', display: 'flex', gap: '2px', textShadow: '0 2px 4px rgba(0,0,0,0.5)'}}>🂠🂠</span> : null
                                    )}
                                </div>
                                {/* Target Egg Throw Button */}
                                {!isMe && (
                                    <button 
                                        onClick={() => sendEmote('🥚', player?.id)}
                                        style={{
                                            position: 'absolute', top: '-15px', right: '-15px', background: 'white', border: '1px solid #ccc',
                                            borderRadius: '50%', padding: '5px', fontSize: '1.2rem', cursor: 'pointer', zIndex: 60,
                                            boxShadow: '0 2px 5px rgba(0,0,0,0.5)', transition: 'transform 0.1s'
                                        }}
                                        title="Throw an Egg!"
                                        onMouseOver={e => e.currentTarget.style.transform='scale(1.2)'} 
                                        onMouseOut={e => e.currentTarget.style.transform='scale(1)'}
                                    >
                                        🥚
                                    </button>
                                )}

                                {/* Emote Bubble */}
                                {emotes[player.id] && (
                                    <div style={{
                                        position: 'absolute', top: '-50px', left: '50%', transform: 'translateX(-50%)',
                                        fontSize: '3.5rem', zIndex: 100, filter: 'drop-shadow(0px 8px 15px rgba(0,0,0,0.8))',
                                        background: 'rgba(255,255,255,0.95)', padding: '5px 10px', borderRadius: '30px',
                                        border: '3px solid #ccc', animation: 'bounce 0.5s ease', pointerEvents: 'none'
                                    }}>
                                        {emotes[player.id]}
                                    </div>
                                )}

                                {/* Splats / Egg Hits */}
                                {splats[player.id] && splats[player.id].map(splat => (
                                    <div key={splat.id} style={{
                                        position: 'absolute', top: `${20 + (splat.id % 60)}%`, left: `${10 + (splat.id % 80)}%`, 
                                        transform: `translate(-50%, -50%) rotate(${(splat.id % 360)}deg)`,
                                        fontSize: '3.5rem', zIndex: 90, pointerEvents: 'none', filter: 'drop-shadow(0px 2px 4px rgba(0,0,0,0.5))',
                                        opacity: 0.85
                                    }}>
                                        🍳
                                    </div>
                                ))}
                            </div>
                        );
                    })}

                    {/* Render Flying Eggs inside the table container so coordinates match players */}
                    {connected && flyingEggs.map(egg => (
                        <EggFlight key={egg.id} egg={egg} getPos={getPos} />
                    ))}
                </div>
            )}

            {/* Organized Emotes Menu */}
            {connected && (
                <div style={{ position: 'fixed', bottom: '160px', left: '50px', zIndex: 90 }}>
                    <button 
                        onClick={() => setShowEmoteMenu(!showEmoteMenu)}
                        style={{
                            background: showEmoteMenu ? 'rgba(255,255,255,0.2)' : 'rgba(0,0,0,0.7)', border: '1px solid rgba(255,255,255,0.2)', borderRadius: '30px',
                            color: 'white', padding: '10px 20px', cursor: 'pointer', fontSize: '1.2rem', boxShadow: '0 5px 15px rgba(0,0,0,0.5)',
                            backdropFilter: 'blur(10px)', transition: 'background 0.2s'
                        }}
                    >
                        {showEmoteMenu ? '✖ Close Emotes' : '😀 Emotes'}
                    </button>

                    {showEmoteMenu && (
                        <div style={{
                            position: 'absolute', bottom: '50px', left: '0', background: 'rgba(0,0,0,0.8)', padding: '15px',
                            borderRadius: '15px', border: '1px solid rgba(255,255,255,0.1)', display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)',
                            gap: '10px', backdropFilter: 'blur(15px)', width: 'max-content', boxShadow: '0 10px 25px rgba(0,0,0,0.8)'
                        }}>
                            {['🤣','🤬','🥶','🥵','😭','💀','🍻','🥃','💸','📈','🤑','🤡','🔥','🎉','♠️','♥️','♦️','♣️','👀','🧠'].map(emoji => (
                                <button key={emoji} onClick={() => sendEmote(emoji)} style={{
                                    background: 'transparent', border: 'none', fontSize: '1.8rem', cursor: 'pointer', transition: 'transform 0.1s', padding: '5px'
                                }} onMouseOver={e => e.currentTarget.style.transform='scale(1.4)'} onMouseOut={e => e.currentTarget.style.transform='scale(1)'}>
                                    {emoji}
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            )}

            {/* Smart Action Controls */}
            {connected && (
                <div style={{
                    position: 'fixed',
                    bottom: '0',
                    left: '0',
                    width: '100%',
                    background: 'linear-gradient(to top, rgba(10,10,10,0.95) 0%, rgba(20,20,20,0.85) 100%)',
                    padding: '25px 0',
                    borderTop: '1px solid rgba(255,255,255,0.1)',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: '15px',
                    boxShadow: '0 -15px 40px rgba(0,0,0,0.6)',
                    backdropFilter: 'blur(15px)',
                    zIndex: 100,
                    transition: 'all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)',
                    transform: 'translateY(0)',
                    opacity: 1,
                    pointerEvents: 'auto'
                }}>
                    
                    {gameState?.players?.length < 2 ? (
                        <div style={{ textAlign: 'center', color: '#ffb74d', padding: '20px 0', fontSize: '1.8rem', fontWeight: '900', letterSpacing: '6px', textTransform: 'uppercase', textShadow: '0 0 15px rgba(255, 183, 77, 0.4)' }}>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite'}}>L</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '0.1s'}}>O</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '0.2s'}}>O</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '0.3s'}}>K</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '0.4s'}}>I</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '0.5s'}}>N</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '0.6s'}}>G</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '0.7s', marginLeft: '10px'}}>F</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '0.8s'}}>O</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '0.9s'}}>R</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.0s', marginLeft: '10px'}}>C</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.1s'}}>H</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.2s'}}>A</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.3s'}}>L</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.4s'}}>L</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.5s'}}>E</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.6s'}}>N</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.7s'}}>G</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.8s'}}>E</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '1.9s'}}>R</span>
                            <span style={{display: 'inline-block', animation: 'bounce 1.5s infinite', animationDelay: '2.0s'}}>S</span>
                        </div>
                    ) : (
                        <>
                            <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', maxWidth: '1400px', alignItems: 'center', padding: '0 20px', boxSizing: 'border-box' }}>
                                {/* Left Side: Player Info */}
                                <div style={{ flex: '1', display: 'flex', alignItems: 'center', gap: '20px' }}>
                                    {me && (
                                        <>
                                            <div style={{ display: 'flex', flexDirection: 'column', background: 'rgba(0,0,0,0.5)', padding: '8px 15px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.1)' }}>
                                                <span style={{ fontSize: '0.7rem', color: '#aaa', textTransform: 'uppercase' }}>Bank</span>
                                                <span style={{ fontSize: '1.4rem', color: '#4fc3f7', fontWeight: 'bold' }}>${me.chips || 0}</span>
                                            </div>
                                            <div style={{ display: 'flex', gap: '5px' }}>
                                                {(me.holeCards || me.cards) && (me.holeCards || me.cards).length > 0 ? (
                                                    (me.holeCards || me.cards).map((c, i) => <span key={i} style={{transform: i===0 ? 'rotate(-5deg)' : 'rotate(5deg)'}}>{renderCard(c, true)}</span>)
                                                ) : (
                                                    <span style={{color: '#555', fontStyle: 'italic', fontSize: '0.9rem'}}>(No Cards)</span>
                                                )}
                                            </div>
                                        </>
                                    )}
                                </div>

                                {/* Center: Status */}
                                <div style={{ flex: '1', textAlign: 'center', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                                    <div style={{color: '#ffb74d', fontWeight: 'bold', marginBottom: '8px', fontSize: '1.1rem'}}>
                                        {toCall > 0 ? `TO CALL: $${toCall}` : 'NO BET TO CALL'}
                                    </div>
                                    <div>
                                        {!me ? (
                                            <span style={{color: '#888', fontWeight: 'bold', letterSpacing: '2px'}}>Spectating</span>
                                        ) : me.status !== 'ACTIVE' && gameState?.phase !== 'PRE_GAME' && gameState?.phase !== 'HAND_ENDED' ? (
                                            <span style={{color: '#888', fontWeight: 'bold', letterSpacing: '2px'}}>Waiting for Next Hand</span>
                                        ) : isMyTurn ? (
                                            <span style={{color: '#ffeb3b', fontWeight: '900', textTransform: 'uppercase', letterSpacing: '4px', textShadow: '0 0 15px rgba(255, 235, 59, 0.6)', fontSize: '1.2rem'}}>Your Turn</span> 
                                        ) : (
                                            <span style={{color: '#888', fontWeight: 'bold', letterSpacing: '2px'}}>Waiting for Opponent</span>
                                        )}
                                    </div>
                                </div>

                                {/* Right Side: Actions */}
                                <div style={{ flex: '2', display: 'flex', gap: '8px', alignItems: 'stretch', justifyContent: 'flex-end', flexWrap: 'nowrap' }}>
                                    <button onClick={() => sendAction('FOLD')} disabled={!isMyTurn} style={{...btnStyle('#d32f2f', isMyTurn), padding: '10px 15px'}}>FOLD</button>
                                    <button onClick={() => sendAction('CHECK')} disabled={!canCheck} style={{...btnStyle('#1976d2', canCheck), padding: '10px 15px'}}>CHECK</button>
                                    <button onClick={() => sendAction('CALL')} disabled={!canCall} style={{...btnStyle('#388e3c', canCall), padding: '10px 15px'}}>CALL {toCall > 0 ? `$${toCall}` : ''}</button>
                                    <div style={{ display: 'flex', alignItems: 'stretch', background: 'rgba(0,0,0,0.4)', borderRadius: '8px', overflow: 'hidden', border: canRaise ? '1px solid #fbc02d' : '1px solid #555', opacity: canRaise ? 1 : 0.6 }}>
                                        <div style={{display: 'flex', flexDirection: 'column', justifyContent: 'center', padding: '0 8px', background: 'rgba(255,255,255,0.05)'}}>
                                            <span style={{fontSize: '0.6rem', color: '#aaa', textTransform: 'uppercase', fontWeight: 'bold'}}>Raise</span>
                                            <span style={{fontSize: '0.6rem', color: '#fbc02d'}}>Min: ${minTotalBet}</span>
                                        </div>
                                        <input type="number" value={raiseAmount} min={minTotalBet} onChange={(e) => setRaiseAmount(Number(e.target.value))} disabled={!canRaise} style={{ padding: '0', width: '60px', border: 'none', background: 'transparent', color: 'white', fontWeight: '900', fontSize: '1.2rem', textAlign: 'center', outline: 'none' }} />
                                        <button onClick={() => sendAction('RAISE', raiseAmount)} disabled={!canRaise} style={{...btnStyle('#fbc02d', canRaise, 'black'), padding: '10px 15px'}}>RAISE</button>
                                    </div>
                                    <button onClick={() => sendAction('ALL_IN')} disabled={!canAllIn} style={{...btnStyle('#7b1fa2', canAllIn), padding: '10px 15px'}}>ALL IN</button>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            )}
            
            <style>{`
                @keyframes bounce {
                    from { transform: translateY(0); }
                    to { transform: translateY(-10px); }
                }
                @keyframes spin {
                    from { transform: rotate(0deg) scale(1.5); }
                    to { transform: rotate(360deg) scale(1.5); }
                }
                @keyframes fadeIn {
                    from { opacity: 0; transform: scale(0.9); }
                    to { opacity: 1; transform: scale(1); }
                }
                @keyframes pulse {
                    0% { opacity: 0.5; }
                    50% { opacity: 1; }
                    100% { opacity: 0.5; }
                }
            `}</style>
        </div>
    );
};

export default PokerGameTest;
