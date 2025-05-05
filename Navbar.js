import React from "react";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useEffect} from "react";
import "../styles/Navbar.css"; // Import CSS for styling

function Navbar() {
    const navigate = useNavigate();

    useEffect(() => {
        const storedUsername = sessionStorage.getItem("username");
        if (!storedUsername) {
            navigate("/login", { replace: true }); // Redirect if not logged in
        }
    }, [navigate]);

    const handleLogout = () => {
        sessionStorage.clear();
        navigate("/login");
    };

    return (


        <div className="navbar">
            <nav className="navbar">
                <h1 className="logo">Let&apos;s Get Lyrical <strong>22</strong></h1>
                <ul className="nav-links">
                    <li><Link to="/search" className="nav-link">Artist Search</Link></li>
                    <li><Link to="/friendSearch" className="nav-link">Friend Search</Link></li>
                    <li><Link to="/favorites" className="nav-link">Favorites</Link></li>
                    <li>
                        <button className="Logout_button" onClick={handleLogout}>Logout</button>
                    </li>
                </ul>
            </nav>
        </div>

    );
}

export default Navbar;
