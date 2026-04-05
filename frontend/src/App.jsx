import { useState } from 'react';
import PokerGameTest from './PokerGameTest';

function App() {
  const [currentView, setCurrentView] = useState('home');

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      <nav style={{ padding: '15px', background: '#222', color: 'white', display: 'flex', gap: '15px' }}>
        <button 
          onClick={() => setCurrentView('home')} 
          style={{ padding: '8px 16px', background: currentView === 'home' ? '#555' : '#333', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Startseite
        </button>
        <button 
          onClick={() => setCurrentView('poker')} 
          style={{ padding: '8px 16px', background: currentView === 'poker' ? '#555' : '#333', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Poker Game
        </button>
      </nav>
      
      <main style={{ padding: '20px' }}>
        {currentView === 'home' && (
          <div>
            <h1>Willkommen auf der Casino Startseite!</h1>
            <p>Dies ist der Platzhalter für die restlichen Casino-Bereiche. Bitte wähle im Menü "Poker Game", um zum Poker-Frontend zu gelangen.</p>
          </div>
        )}
        {currentView === 'poker' && <PokerGameTest />}
      </main>
    </div>
  )
}

export default App
