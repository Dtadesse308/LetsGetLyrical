import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import '../styles/FriendSearch.css';


function FriendSearch() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("");
    const [loggedInUserid, setLoggedInUserId] = useState("");
    const [loggedInUserName, setLoggedInUserName] = useState("");
    //const [friend, setFriend] = useState([]);
    const [searchResults, setSearchResults] = useState([]);
    const [comparisonResults, setcomparisonResults] = useState([]);
    const [selectedFriends, setSelectedFriends] = useState([]);
    const [error, setError] = useState(null);
    const [compareError, setCompareError] = useState(null);

    const [loading, setLoading] = useState(false);
    const [showComparisonTable, setShowComparisonTable] = useState(false); // New state
    const [isDescending, setIsDescending] = useState(true);
    const [songInfoModalIsOpen, setSongInfoModalIsOpen] = useState(false);
    const [favoriteUsersModalIsOpen, setFavoriteUsersModalIsOpen] = useState(false);

    const [selectedSong, setSelectedSong] = useState(null);

    const openSongInfoModal = (song) => {
        setSelectedSong(song);
        setSongInfoModalIsOpen(true);
    };

    const closeSongInfoModal = () => {
        setSongInfoModalIsOpen(false);
        setSelectedSong(null);
    };

    const openFavoriteUserModal = (song) => {
        setSelectedSong(song);
        setFavoriteUsersModalIsOpen(true)
    };
    const closeFavoriteUserModal = () => {
        setSelectedSong(null);
        setFavoriteUsersModalIsOpen(false);
    };


    useEffect(() => {
        document.title = "Let's Get Lyrical";

        const id = sessionStorage.getItem("id");
        setLoggedInUserId((id));
        console.log("Logged in user ID:", id);
        console.log("set user id:", loggedInUserid);
        if (!id) {
            navigate("/login", { replace: true });

        }
        const storedUsername = sessionStorage.getItem("username");
         setLoggedInUserName(storedUsername);
       // setLoggedInUserId((id));
        console.log("Updated search results:", searchResults);

    }, [navigate, searchResults]);


    const handleSearch = async (e) => {
        e.preventDefault();
        if (!username.trim()) return;

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/friend/search?username=${encodeURIComponent(username)}`);
            if (!response.ok)

                throw new Error(`Error: ${response.status}`);

            const data = await response.json();
            setSearchResults([data]);

        } catch (err) {
            setError('no user found with name: ' + username);
            console.error('Search error:', err);
        } finally {
            setLoading(false);
        }
    };
    //Adds user to selected friend array
    const handleSelectFriend = (user) => {
       //if friend is not already selected
        if (!selectedFriends.some(f => f.id === user.id)) {
            setSelectedFriends([...selectedFriends, user]);
        }
    };
    const handleCompareFriends = async () => {
        console.log("Comparing friends:", selectedFriends);

        try {
           // const usernamesSelected = selectedFriends.map(friend => friend.id).join(',');
            //TODO must pass in the friend object, serilize it and pass it in
            const response = await fetch('/friend/compare', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ loggedInUserId: loggedInUserid, users: selectedFriends }),
            });
            console.log("set user id:", loggedInUserid);
            if (!response.ok) throw new Error(`Error: ${response.status}`);

            const data = await response.json();
            setcomparisonResults((data)); // Convert the object to an array of [key, value] pairs

        } catch (err) {
            setCompareError('unable to compare songs with ' + username);
            console.error('Search error:', err);
        } finally {
            setLoading(false);
            setShowComparisonTable(true);
        }
    };

    const changeSongsOrder = () => {
        setcomparisonResults([...comparisonResults].reverse());
        setIsDescending(!isDescending);
    };

    return (
        <div className = "friend-search">
            <Navbar/>
            <div className="container">
                <div className="left-panel">
                    <h1 className="search-title"></h1>
                    <div className="search-container">
                        <form onSubmit={handleSearch} className="search-form">
                            <input
                                type="text"
                                placeholder="Search for a friend"
                                onChange={(e) => setUsername(e.target.value)}
                                className="Friend-search-input"
                                aria-label="Type a friend's username to search for"
                            />
                            <button type="submit" className="search-button" aria-label="Search this friend">
                                Search
                            </button>
                        </form>
                        {loading && <div className="loading">Loading...</div>}
                        {error && <div className="error">{error}</div>}
                    </div>

                    <div className="results-container">

                        {searchResults.length > 0 && (
                            <ul className="search-results">
                                {searchResults.map((user) => (
                                    <li key={user.id} className="friend-item">
                                        <button className="friendButton" aria-label="Select friend" onClick={() => handleSelectFriend(user)}>
                                            {user.username}
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    <div className="selected-friends">
                        <h3 style={{ fontSize: '20px', textAlign:"center" }}>Selected Friends:</h3>
                        <ul >
                            {selectedFriends.map((friend) => (
                                <li className="selected-friends-list" aria-label="List of selected friends" key={friend.id}> - {friend.username}</li>
                            ))}
                        </ul>
                        {selectedFriends.length > 0 && (
                            <button onClick={handleCompareFriends} className="compare-button" aria-label="Compare friends">
                                Compare Friends
                            </button>
                        )}
                    </div>
                </div>

                <div className="right-panel">
                    {compareError && <div className="error">{compareError}</div>}
                    {showComparisonTable && (
                        <div className="comparison-table">

                            <button className="OrderButton" id = "OrderButton" onClick={changeSongsOrder} aria-label="Change order of songs">
                                {isDescending ? "descending order" : "ascending order"}
                            </button>

                            <table className="table">
                                <caption style={{ fontWeight: 'bold' }}>Comparison of the Songs</caption>
                                <tr>
                                    <th style={{ fontWeight: 'bold' }}>Song</th>
                                    <th style={{ fontWeight: 'bold' }}>Number of appearances</th>
                                </tr>
                                {comparisonResults.map((song, index) => (
                                    <tr key={index}>
                                        <td>
                                            <button aria-label={song.title} className="song-title-button"
                                                    id="song-title-button" onClick={() => openSongInfoModal(song)}>
                                                {song.title}
                                            </button>
                                        </td>
                                        {/*<td>{song.favoritedByUsers.length + 1}</td>*/}
                                        <td>
                                            <button aria-label={`Favorited by ${song.favoritedByUsers.length + 1} users`}
                                                    className="favorited-users-button" id="favorited-users-button"
                                                    onClick={() => openFavoriteUserModal(song)}>
                                                {song.favoritedByUsers.length + 1}
                                            </button>
                                        </td>
                                    </tr>
                                    ))}
                            </table>
                        </div>
                        )}

                    {selectedSong && songInfoModalIsOpen && (
                        <>
                            <div className="modal-overlay" onClick={closeSongInfoModal}></div>
                            <div className="song-modal" id="song-modal">
                                <h2 aria-label={selectedSong.title}>{selectedSong.title}</h2>
                                <p aria-label={selectedSong.artist}><strong>Artist:</strong> {selectedSong.artist}</p>
                                <p aria-label={selectedSong.year}><strong>Year:</strong> {selectedSong.year}</p>
                                <button aria-label="Close" onClick={closeSongInfoModal} className="close-modal-button">Close</button>
                            </div>
                        </>
                    )}

                    {selectedSong && favoriteUsersModalIsOpen && (
                    <>
                        <div className="modal-overlay" onClick={closeFavoriteUserModal}></div>
                        <div className="favorited-users-modal" id="favorited-users-modal">
                            <h2>{selectedSong.title}</h2>
                            <p aria-label="Favorited by:"><strong>Favorited by: </strong>
                                {loggedInUserName}, {selectedSong.favoritedByUsers.map(user => user.username).join(', ')}</p>
                            <button onClick={closeFavoriteUserModal} className="close-modal-button">Close</button>
                        </div>
                    </>

                    )}

                </div>
            </div>
        </div>
    );
}

export default FriendSearch;

