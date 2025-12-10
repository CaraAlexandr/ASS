import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';
import Dashboard from './components/Dashboard';
import PatternCategory from './components/PatternCategory';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/patterns/:category" element={<PatternCategory />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;

