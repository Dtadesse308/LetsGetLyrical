import React, { useEffect } from "react";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ArtistSearch from "./pages/ArtistSearch";
import Favorites from "./pages/favorites";
import FriendSearch from "./pages/FriendSearch";

function App() {
    const navigate = useNavigate();

    useEffect(() => {
        let timer;

        const logout = () => {
            console.log("Logging out due to inactivity...");
            sessionStorage.clear();
            navigate("/login");
        };

        const resetTimer = () => {
            clearTimeout(timer);
            timer = setTimeout(logout, 60000); // 60 seconds
        };

        const events = ["mousemove", "keypress", "click", "scroll"];
        events.forEach(event => document.addEventListener(event, resetTimer));
        resetTimer();

        return () => {
            clearTimeout(timer);
            events.forEach(event => document.removeEventListener(event, resetTimer));
        };
    }, [navigate]);

    return (
        <div>
            <Routes>
                <Route path="/search" element={<ArtistSearch />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/friendSearch" element={<FriendSearch />} />
                <Route path="/favorites" element={<Favorites />} />
                <Route path="/" element={<Navigate to="/login" />} />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </div>
    );
}

export default App;
