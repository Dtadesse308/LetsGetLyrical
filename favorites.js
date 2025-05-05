import React from "react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import '../styles/Favorites.css';
import Navbar from "../components/Navbar";

function Favorites() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("");

    const [favorites, setFavorites] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    const [error, setError] = useState(null);

    const [showSongDetails, setShowSongDetails] = useState(false);
    const [selectedSong, setSelectedSong] = useState(null);
    const [dialogStyle, setDialogStyle] = useState({});

    const [showRemoveConfirm, setShowRemoveConfirm] = useState(false);
    const [songToRemove, setSongToRemove] = useState(null);
    const [hoveredSongId, setHoveredSongId] = useState(null);

    const [isPrivate, setIsPrivate] = useState(true);
    const [matchResult, setMatchResult] = useState(null); // null | { type, username, favorites }

    const [showRemoveAllConfirm, setShowRemoveAllConfirm] = useState(false);
    const [refreshKey, setRefreshKey] = useState(0);

    const [focusedSongId, setFocusedSongId] = useState(null);

    const [showMutualPopup, setShowMutualPopup] = useState(false);
    const [mutualType, setMutualType] = useState(""); // "soulmate" or "enemy"

    useEffect(() => {
        document.title = "Let's Get Lyrical";

        const id = sessionStorage.getItem("id");

        const storedUsername = sessionStorage.getItem("username");

        if (!id) {
            navigate("/login", { replace: true });
        } else {
            if (storedUsername && storedUsername.length > 30) {
                setUsername("User"); //when username is long change user's name to 'User'
            } else {
                setUsername(storedUsername);
            }
            fetchFavoriteSongs(id);
        }

        const fetchPrivacySetting = async () => {
            try {
                const userId = sessionStorage.getItem("id");
                if (userId) {
                    const response = await fetch(`/user/privacy?userID=${userId}`);

                    if (!response.ok) {
                        throw new Error('Failed to fetch privacy setting');
                    }

                    const data = await response.json();
                    setIsPrivate(data.is_private);
                }
            } catch (error) {
                console.error('Error fetching privacy setting:', error);
            }
        };

        fetchPrivacySetting();

    }, [navigate]);

    useEffect(() => {
        // this will run when refreshKey changes or when favorites change
        const userId = sessionStorage.getItem("id");
        if (userId) {
            fetchFavoriteSongs(userId);
        }
    }, [refreshKey]);

    const fetchFavoriteSongs = async (user_id) => {
        setIsLoading(true);
        setError(null);

        try {
            //const response = await fetch(`/favorites?userID=${user_id}`);

            const timestamp = new Date().getTime();
            const response = await fetch(`/api/favorites?userID=${user_id}&t=${timestamp}`);


            if (!response.ok) {
                throw new Error('Failed to fetch favorites');
            }

            const data = await response.json();
            console.log("Fetched favorites:", data);
            console.log("Position values:", data.map(song => ({
                id: song.id,
                title: song.title,
                position: song.position
            })));

            const sortedFavorites = [...data].sort((a, b) => {
                const posA = a.position !== undefined ? a.position : 0;
                const posB = b.position !== undefined ? b.position : 0;

                return posB - posA;
            });

            console.log("Sorted favorites:", sortedFavorites);
            //setFavorites(sortedFavorites);
            setFavorites([...sortedFavorites]);
        }
        catch (error) {
            setError('Failed to load favorites. Please try again.');
            console.error('Error fetching favorites:', error);
        } finally {
            setIsLoading(false);
        }
    }


    const handleSongClick = (song, event) => {
        const rect = event.currentTarget.getBoundingClientRect();

        const dialogPositionStyle = {
            position: 'fixed',
            top: `${rect.top + window.scrollY}px`,
            left: `${rect.right + window.scrollX + 10}px`,
            transform: 'none',
            zIndex: 1000
        };

        setDialogStyle(dialogPositionStyle);
        setSelectedSong(song);
        setShowSongDetails(true);
    };

    const removeSong = async (songId) => {
        try {
            const userId = sessionStorage.getItem("id");

            const response = await fetch(`/api/favorites?userID=${userId}&songID=${songId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error("Failed to remove song");
            }
            setFavorites(favorites.filter(song => song.id !== songId));
        } catch (error) {
            console.error("Error removing song:", error);
        }
    };

    const togglePrivacy = async () => {
        try {
            const userId = sessionStorage.getItem("id");

            setIsPrivate(!isPrivate);

            await fetch(`/user/privacy?userID=${userId}&isPrivate=${!isPrivate}`, {
                method: 'PUT'
            });
        } catch (error) {
            console.error('Error updating privacy setting:', error);
            setIsPrivate(isPrivate);
        }
    };

// Inside your Favorites component:

    const handleSimilarClick = async () => {
        setError(null);
        try {
            const response = await fetch(
                `/match/soulmate?username=${username}`
            );
            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error("No lyrical soulmate found");
                }
                throw new Error("Failed to fetch lyrical soulmate");
            }
            const data = await response.json();
            if (data.mutual) {
                setMutualType("soulmate");
                setShowMutualPopup(true);
                setTimeout(() => setShowMutualPopup(false), 4000); // Auto-dismiss after 4s
            }

            setMatchResult({
                type: "similar",
                username: data.user.username,
                favorites: data.favorites
            });

        } catch (err) {
            console.error(err);
            setError(err.message);
        }
    };

    const handleEnemyClick = async () => {
        setError(null);
        try {
            const response = await fetch(
                `/match/enemy?username=${username}`
            );
            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error("No lyrical enemy found");
                }
                throw new Error("Failed to fetch lyrical enemy");
            }
            const data = await response.json();
            if (data.mutual) {
                setMutualType("enemy");
                setShowMutualPopup(true);
                setTimeout(() => setShowMutualPopup(false), 4000);
            }

            setMatchResult({
                type: "enemy",
                username: data.user.username,
                favorites: data.favorites
            });

        } catch (err) {
            console.error(err);
            setError(err.message);
        }
    };

    const removeAllSongs = async () => {
        try {
            const userId = sessionStorage.getItem("id");

            // copy of user's current favorites
            const songsToRemove = [...favorites];

            setFavorites([]); //clear UI

            let failureOccurred = false;

            for (const song of songsToRemove) {
                try {
                    const response = await fetch(`/api/favorites?userID=${userId}&songID=${song.id}`, {
                        method: 'DELETE'
                    });

                    if (!response.ok) {
                        failureOccurred = true;
                    }
                } catch (err) {
                    failureOccurred = true;
                }
            }

            if (failureOccurred) {
                await fetchFavoriteSongs(userId);
                setError("Some songs could not be removed. Please try again.");
            }

        } catch (error) {
            console.error("Error removing all songs:", error);
            setIsLoading(false);
            setError("Failed to remove all songs. Please try again.");

            const userId = sessionStorage.getItem("id");
            await fetchFavoriteSongs(userId);
        }
    };


    const moveSongUp = async (songId, e) => {
        e.stopPropagation();

        try {
            const userId = sessionStorage.getItem("id");

            console.log(`Sending reorder request: userID=${userId}, songID=${songId}, direction=up`);

            const response = await fetch(`/api/favorites/reorder?userID=${userId}&songID=${songId}&direction=up`, {
                method: 'PUT'
            });

            if (!response.ok) {
                throw new Error('Failed to reorder song');
            }

            // refresh the favorites list
            await fetchFavoriteSongs(userId);
            setRefreshKey(prevKey => prevKey + 1); //force re-render
        } catch (error) {
            console.error('Error moving song up:', error);
            setError('Failed to reorder song. Please try again.');
        }
    };

    const moveSongDown = async (songId, e) => {
        e.stopPropagation();

        try {
            const userId = sessionStorage.getItem("id");

            console.log(`Sending reorder request: userID=${userId}, songID=${songId}, direction=down`);

            const response = await fetch(`/api/favorites/reorder?userID=${userId}&songID=${songId}&direction=down`, {
                method: 'PUT'
            });

            if (!response.ok) {
                throw new Error('Failed to reorder song');
            }

            // refresh the favorites list
            await fetchFavoriteSongs(userId);
            setRefreshKey(prevKey => prevKey + 1); //force re-render
        } catch (error) {
            console.error('Error moving song down:', error);
            setError('Failed to reorder song. Please try again.');
        }
    };


    return (
        <div className="favorites">
            <Navbar/>
            <div className="favorites-container">
                <div className="header-section">
                    {matchResult ? (
                        <div className={`match-banner ${matchResult.type}`}>
                            {matchResult.type === "similar" ? "Similar Friend" : "Lyrical Enemy"}
                        </div>
                    ) : (
                        <>
                            <h1 className="favorites-title">{username}'s Favorites</h1>
                            <div className="privacy-toggle-container">
                                <span className="privacy-label">Profile: {isPrivate ? 'Private' : 'Public'}</span>
                                <label className="switch">
                                    <input
                                        type="checkbox"
                                        checked={isPrivate}
                                        onChange={togglePrivacy}
                                        aria-label="Toggle profile privacy"
                                    />
                                    <span className="slider round"></span>
                                </label>
                            </div>
                        </>
                    )}
                </div>

                <div className="content-layout">
                    <div className="favorites-content">
                        {!matchResult && favorites.length > 0 && (
                            <button
                                data-testid="remove-all-button"
                                className="remove-all-button"
                                onClick={() => setShowRemoveAllConfirm(true)}
                                aria-label="Remove all favorite songs"
                            >
                                Remove All Favorites
                            </button>
                        )}
                        {isLoading ? (
                            // <div className="loading">Loading...</div>
                            <div className="loading-spinner">
                                <div className="spinner"></div>
                                <p>Loading your favorites...</p>
                            </div>
                        ) : error ? (
                            // <div className="error">{error}</div>
                            <div className="error-message">
                                <span className="error-icon">⚠</span>
                                <p>{error}</p>
                            </div>
                        ) : (
                            <div className="favorites-list">
                                {matchResult ? (
                                    <>
                                        <h2 className="favorites-title">{matchResult.username}'s Favorites</h2>
                                        {matchResult.favorites.length === 0 ? (
                                            <div className="no-favorites">This user has no favorites yet.</div>
                                        ) : (
                                            matchResult.favorites.map(song => (
                                                <div key={song.id} className="song-card passive">
                                                    {song.title}
                                                </div>
                                            ))
                                        )}
                                    </>
                                ) : favorites.length === 0 ? (
                                    // <div className="no-favorites">No favorite songs yet!</div>
                                    <div className="no-favorites">
                                        <div className="empty-state-icon">🎵</div>
                                        <p>You haven't added any favorite songs yet!</p>
                                        <p className="empty-state-subtext">Explore songs and add them to your
                                            favorites.</p>
                                    </div>
                                ) : (
                                    favorites.map(song => (
                                        <div
                                            data-testid="song-card"
                                            key={`${song.id}-position-${song.position}`} // Add position to the key
                                            className={`song-card ${hoveredSongId === song.id ? 'hovered' : ''}`}
                                            onClick={(e) => handleSongClick(song, e)}
                                            onMouseEnter={() => setHoveredSongId(song.id)}
                                            onMouseLeave={() => setHoveredSongId(null)}

                                            onFocus={() => setFocusedSongId(song.id)}
                                            onBlur={() => setFocusedSongId(null)}
                                            tabIndex="0" // Make the card focusable with keyboard
                                            onKeyDown={(e) => {
                                                // Handle keyboard events for song card
                                                if (e.key === 'Enter' || e.key === ' ') {
                                                    // Space or Enter opens song details
                                                    handleSongClick(song, e);
                                                } else if (e.key === 'Delete' || e.key === 'Backspace') {
                                                    // Delete or Backspace shows removal confirmation
                                                    e.preventDefault(); // Prevent page navigation
                                                    setSongToRemove(song);
                                                    setShowRemoveConfirm(true);
                                                } else if (e.key === 'ArrowUp') {
                                                    // Up arrow moves song up
                                                    e.preventDefault(); // Prevent page scroll
                                                    moveSongUp(song.id, e);
                                                } else if (e.key === 'ArrowDown') {
                                                    // Down arrow moves song down
                                                    e.preventDefault(); // Prevent page scroll
                                                    moveSongDown(song.id, e);
                                                }
                                            }}
                                            aria-label={`Song: ${song.title}${song.artist ? `, Artist: ${song.artist}` : ''}${song.year ? `, Year: ${song.year}` : ''}`}
                                        >
                                            <div className="song-card-title">{song.title}</div>
                                            {(hoveredSongId === song.id || focusedSongId === song.id) && (
                                                <div className="song-actions">
                                                    <button
                                                        data-testid={`up-button-${song.id}`}
                                                        className="up-icon"
                                                        onClick={(e) => moveSongUp(song.id, e)}
                                                        tabIndex="0"
                                                        aria-label={`Move ${song.title} up`}

                                                    >
                                                        ↑
                                                    </button>
                                                    <button
                                                        className="down-icon"
                                                        onClick={(e) => moveSongDown(song.id, e)}
                                                        tabIndex="0"
                                                        aria-label={`Move ${song.title} down`}
                                                    >
                                                        ↓
                                                    </button>
                                                    <button
                                                        className="remove-icon"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            setSongToRemove(song);
                                                            setShowRemoveConfirm(true);
                                                        }}
                                                        tabIndex="0"
                                                        aria-label={`Remove ${song.title} from favorites`}
                                                    >
                                                        🗑
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    ))
                                )}
                            </div>
                        )}
                    </div>

                    <div className="buttons-container">
                        <button className="similar-button" onClick={handleSimilarClick}
                                aria-label="Find similar music tastes"
                        >
                            Similar
                        </button>
                        <button className="enemy-button" onClick={handleEnemyClick}
                                aria-label="Find opposite music tastes"
                        >
                            Enemy
                        </button>
                    </div>
                </div>

                {showSongDetails && selectedSong && (
                    <div className="song-details-dialog" style={dialogStyle}>
                        <div className="dialog-content">
                            <button className="close-button" onClick={() => setShowSongDetails(false)}
                                    aria-label="Close song details"
                            >
                                X
                            </button>
                            <div className="song-info">
                                <div className="info-row"><span>Artist:</span> {selectedSong.artist}</div>
                                <div className="info-row"><span>Released:</span> {selectedSong.year}</div>
                            </div>
                        </div>
                    </div>
                )}

                {showRemoveConfirm && songToRemove && (
                    <div className="confirmation-dialog">
                        <div className="dialog-content"
                             role="dialog"
                             aria-labelledby="dialog-title"
                             aria-modal="true"
                        >
                            <p>Are you sure you want to remove "{songToRemove.title}" from your favorites?</p>
                            <div className="dialog-buttons">
                                <button
                                    className="confirm-button"
                                    onClick={() => {
                                        removeSong(songToRemove.id);
                                        setShowRemoveConfirm(false);
                                    }}
                                    autoFocus
                                    aria-label={`Confirm removal of ${songToRemove.title}`}
                                >
                                    Confirm
                                </button>
                                <button className="cancel-button" onClick={() => setShowRemoveConfirm(false)}
                                        aria-label="Cancel removal"
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {showRemoveAllConfirm && (
                    <div className="confirmation-dialog">
                        <div className="dialog-content">
                            <p>Are you sure you want to remove all songs from your favorites?</p>
                            <div className="dialog-buttons">
                                <button
                                    className="confirm-button"
                                    onClick={() => {
                                        removeAllSongs();
                                        setShowRemoveAllConfirm(false);
                                    }}
                                    aria-label="Confirm removal of all songs"
                                >
                                    Confirm
                                </button>
                                <button
                                    className="cancel-button"
                                    onClick={() => setShowRemoveAllConfirm(false)}
                                    aria-label="Cancel removal of all songs"
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
            {showMutualPopup && (
                <div className={`mutual-popup ${mutualType}`}>
                    {mutualType === "enemy" ? (
                        <>
                            <span role="img" aria-label="disaster">💥</span> Oh no! You have a mutual enemy!!
                        </>
                    ) : (
                        <>
                            <span role="img" aria-label="sparkle">✨</span> Congrats! You have a mutual soulmate!!”
                        </>
                    )}
                </div>
            )}
        </div>
    );
}

export default Favorites;
