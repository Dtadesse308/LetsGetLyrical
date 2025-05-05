import React, { useEffect, useState, useRef } from 'react';
import '../styles/ArtistSearch.css';
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";

/* global anychart */

export async function addToFavorites(songId, setTooltipError) {
    try {
        const response = await fetch(
            `/api/favorites?userID=${sessionStorage.getItem("id")}&songID=${songId}`,
            { method: "POST" }
        );
        if (!response.ok) {
            throw new Error(`Error: ${response.status}`);
        }
        const button = document.getElementById("favorite-button");
        button.innerText = "Added to Favorites";
        button.style.backgroundColor = "gray";
    } catch (e) {
        setTooltipError("Already in Favorites!");
    }
}

function ArtistSearch() {
    const navigate = useNavigate();

    const [artistQuery, setArtistQuery] = useState({ name: '', num: -1, mode: 'auto' });
    const [searchedQuery, setSearchedQuery] = useState({ name: '', num: -1, mode: 'auto' });
    const [cloudMode, setCloudMode] = useState('cloud');
    const [artists, setArtists] = useState([]);
    const [songs, setSongs] = useState([]);
    const [selectedSongs, setSelectedSongs] = useState([]);
    const [wordCloud, setWordCloud] = useState([]);
    const [cloudWords, setCloudWords] = useState([]);
    const [cumulativeWords, setCumulativeWords] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // modal and tooltip handlers
    const [modalData, setModalData] = useState(null);
    const [hoveredSong, setHoveredSong] = useState(null);
    const [clickedSong, setClickedSong] = useState(null);
    const [tooltipError, setTooltipError] = useState(null);
    const timeoutRef = useRef(null);


    useEffect(() => {
        document.title = "Let's Get Lyrical";
        const id = sessionStorage.getItem("id");
        if (!id) {
            navigate("/login", { replace: true });
        }
    }, [navigate]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setArtistQuery((prev) => ({ ...prev, [name]: value }));
    };

    const handleArtistSearch = async (e) => {
        e.preventDefault();
        if (!artistQuery.name.trim()) return;

        setLoading(true);
        setError(null);
        setArtists([]);
        setSongs([]);
        setSelectedSongs([]);

        setSearchedQuery({...searchedQuery, name: artistQuery.name.trim() });

        try {
            const response = await fetch(`/api/search/artists?name=${encodeURIComponent(artistQuery.name)}`);
            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }
            const data = await response.json();
            setArtists(data);
        } catch (err) {
            setError('Failed to fetch artists.');
            console.error('Artist search error:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async (artist) => {
        const mode = artistQuery.mode;

        setLoading(true);
        setError(null);
        setSelectedSongs([]);

        try {
            setSearchedQuery({ name: artist.name, num: artistQuery.num, mode: artistQuery.mode });
            if (mode === "auto") {
                const response = await fetch(`/api/search/songs?artistID=${artist.id}&artistName=${artist.name}&num=${artistQuery.num}`);
                if (!response.ok) {
                    throw new Error(`Error: ${response.status}`);
                }
                const data = await response.json();
                setSongs(data);
            } else {
                const response = await fetch(`/api/search/songs?artistID=${artist.id}&artistName=${artist.name}&num=${-1}`);
                if (!response.ok) {
                    throw new Error(`Error: ${response.status}`);
                }
                const data = await response.json();
                setSongs(data);
            }
        } catch (err) {
            setError('Failed to fetch songs for artist.');
            console.error('Artist songs error:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSongSelection = (song) => {
        setSelectedSongs((prev) => {
            const isSelected = prev.some(s => s.id === song.id);
            if (isSelected) {
                return prev.filter(s => s.id !== song.id);
            } else {
                return [...prev, song];
            }
        });
    };

    // Function to generate word cloud from top songs (auto mode)
    const handleGenerateFromTopSongs = async () => {
        // Use the first 'num' songs as specified in the artistQuery
        const topSongs = songs.slice(0, parseInt(artistQuery.num) || 10);

        setLoading(true);
        setError(null);
        try {
            const response = await fetch('/api/search/generate/custom', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(topSongs),
            });

            const data = await response.json();
            // Sort by occurrences
            data.sort((a, b) => {
                const totalA = a.songOccurrences.reduce((sum, occurrence) => sum + occurrence.count, 0);
                const totalB = b.songOccurrences.reduce((sum, occurrence) => sum + occurrence.count, 0);
                return totalB - totalA;
            });
            handleNewWordCloudGenerated(data);
        } catch (err) {
            setError('Failed to generate word cloud from top songs.');
            console.error('Word cloud error:', err);
        } finally {
            setLoading(false);
        }
    };

    // Function to add current selection to existing cloud
    const addToExistingCloud = (newWords) => {
        if (!newWords || newWords.length === 0) {
            setError('No new words to add.');
            return;
        }

        setCumulativeWords(prevWords => {
            // If this is the first set of words, just use them directly
            if (prevWords.length === 0) {
                return [...newWords];
            }

            // Combine previous words with new words
            const combinedWords = [...prevWords];

            newWords.forEach(newWord => {
                const existingWordIndex = combinedWords.findIndex(word => word.word === newWord.word);

                if (existingWordIndex >= 0) {
                    // Word exists, merge song occurrences
                    const existingWord = combinedWords[existingWordIndex];

                    newWord.songOccurrences.forEach(newOccurrence => {
                        const existingOccurrenceIndex = existingWord.songOccurrences.findIndex(
                            occ => occ.id === newOccurrence.id
                        );

                        if (existingOccurrenceIndex >= 0) {
                            // Song exists in both, add counts
                            existingWord.songOccurrences[existingOccurrenceIndex].count += newOccurrence.count;
                        } else {
                            // Song only in new words, add it
                            existingWord.songOccurrences.push(newOccurrence);
                        }
                    });
                } else {
                    // Word doesn't exist, add it
                    combinedWords.push(newWord);
                }
            });

            return combinedWords;
        });
    };

    // Function to generate word cloud from selected songs (manual mode)
    const handleGenerateFromSelected = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch('/api/search/generate/custom', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(selectedSongs),
            });
            const data = await response.json();
            // Sort by occurrences
            data.sort((a, b) => {
                const totalA = a.songOccurrences.reduce((sum, occurrence) => sum + occurrence.count, 0);
                const totalB = b.songOccurrences.reduce((sum, occurrence) => sum + occurrence.count, 0);
                return totalB - totalA;
            });
            handleNewWordCloudGenerated(data);
        } catch (err) {
            setError('Failed to generate word cloud from selected songs.');
            console.error('Word cloud error:', err);
        } finally {
            setLoading(false);
        }
    };

    // Function to handle adding current to existing
    const handleAddToExistingCloud = async (isTopSongs = false, songData = null) => {
        let songsToProcess = [];

        if (isTopSongs) {
            if (songs.length === 0) {
                setError('No songs available.');
                return;
            }
            songsToProcess = songs.slice(0, parseInt(artistQuery.num) || 10);
        } else {
            songsToProcess = selectedSongs;
        }

        if (cumulativeWords.length === 0 && wordCloud.length === 0) {
            const button = document.querySelector('.add-to-existing-button');
            if (button) {
                const tooltip = document.createElement('div');
                tooltip.innerText = 'Please generate a word cloud first';
                tooltip.className = 'tooltip-error';

                const rect = button.getBoundingClientRect();
                tooltip.style.left = `${rect.left + window.scrollX + rect.width / 2}px`;
                tooltip.style.top = `${rect.top + window.scrollY}px`;

                document.body.appendChild(tooltip);
                setTimeout(() => {
                    document.body.removeChild(tooltip);
                }, 2000);
            }
            return;
        }

        setLoading(true);
        setError(null);
        try {
            let data = songData;
            if (songData === null) {
                const response = await fetch('/api/search/generate/custom', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(songsToProcess),
                });

                data = await response.json();
            }

            // If this is the first addition and cumulativeWords is empty but wordCloud has data
            if (cumulativeWords.length === 0 && wordCloud.length > 0) setCumulativeWords(wordCloud);
            addToExistingCloud(data);

            // Sort and display the cumulative words
            const sortedData = [...data].sort((a, b) => {
                const totalA = a.songOccurrences.reduce((sum, occurrence) => sum + occurrence.count, 0);
                const totalB = b.songOccurrences.reduce((sum, occurrence) => sum + occurrence.count, 0);
                return totalB - totalA;
            });

            setWordCloud(sortedData);
        } catch (err) {
            setError('Failed to add to existing word cloud.');
            console.error('Word cloud error:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleNewWordCloudGenerated = (data) => {
        // Reset cumulative words when generating a brand new cloud
        setCumulativeWords([]);
        setWordCloud(data);
    };

    // Effect to update word cloud when cumulative words change
    useEffect(() => {
        if (cumulativeWords.length > 0) {
            const sortedWords = [...cumulativeWords].sort((a, b) => {
                const totalA = a.songOccurrences.reduce((sum, occurrence) => sum + occurrence.count, 0);
                const totalB = b.songOccurrences.reduce((sum, occurrence) => sum + occurrence.count, 0);
                return totalB - totalA;
            });

            setWordCloud(sortedWords);
        }
    }, [cumulativeWords]);

    const containerRef = useRef(null);

    // anychart library for wordCloud
   /* useEffect(() => {
        if (cloudWords.length > 0 && cloudMode === 'cloud') {
            const loadScript = (src) => {
                return new Promise((resolve, reject) => {
                    const script = document.createElement('script');
                    script.src = src;
                    script.async = true;
                    script.onload = () => resolve();
                    script.onerror = () => reject(new Error(`Failed to load script ${src}`));
                    document.head.appendChild(script);
                });
            };
            loadScript("https://cdn.anychart.com/releases/8.11.0/js/anychart-base.min.js")
                .then(() => loadScript("https://cdn.anychart.com/releases/8.11.0/js/anychart-tag-cloud.min.js"))
                .then(() => {
                    if (containerRef.current) {
                        containerRef.current.innerHTML = "";
                    }
                    const rawData = cloudWords.map(word => ({
                        x: word.word,
                        value: word.total,
                        occurrences: word.songOccurrences
                    }));
                    const dataSet = anychart.data.set(rawData); // global variable
                    const data = dataSet.mapAs({ x: 'x', value: 'value', occurrences: 'occurrences' });
                    const chart = anychart.tagCloud(data);

                    // event when you click on word
                    chart.listen("pointClick", function(e) {
                        const wordClicked = e.point.get("x");
                        const total = e.point.get("value");
                        const occurrences = e.point.get("occurrences");
                        const { clientX, clientY } = e.originalEvent;
                        setModalData({
                            word: wordClicked,
                            total,
                            occurrences,
                            x: clientX,
                            y: clientY
                        });
                    });

                    chart.tooltip().enabled(false);
                    chart.angles([0]);

                    if (containerRef.current) {
                        chart.container(containerRef.current);
                        chart.draw();

                        // give words data-testid for testing
                        const svg = containerRef.current.querySelector("svg");
                        if (svg) {
                            const texts = svg.querySelectorAll("text");
                            texts.forEach(textEl => {
                                textEl.setAttribute("data-testid", "word-item");
                            });
                        }
                    } else {
                        console.error("Container element not found");
                    }
                })
                .catch(err => console.error("Error loading scripts", err));
        }
    }, [cloudWords, cloudMode]); */
    useEffect(() => {
        if (cloudWords.length > 0 && cloudMode === 'cloud') {
            const loadScript = (src) => {
                return new Promise((resolve, reject) => {
                    const script = document.createElement('script');
                    script.src = src;
                    script.async = true;
                    script.onload = () => resolve();
                    script.onerror = () => reject(new Error(`Failed to load script ${src}`));
                    document.head.appendChild(script);
                });
            };

            loadScript("https://cdn.anychart.com/releases/8.11.0/js/anychart-base.min.js")
                .then(() => loadScript("https://cdn.anychart.com/releases/8.11.0/js/anychart-tag-cloud.min.js"))
                .then(() => {
                    if (containerRef.current) {
                        containerRef.current.innerHTML = "";
                    }

                    const rawData = cloudWords.map(word => ({
                        x: word.word,
                        value: word.total,
                        occurrences: word.songOccurrences
                    }));

                    const dataSet = anychart.data.set(rawData);
                    const data = dataSet.mapAs({ x: 'x', value: 'value', occurrences: 'occurrences' });
                    const chart = anychart.tagCloud(data);

                    // Make container focusable
                    if (containerRef.current) {
                        containerRef.current.setAttribute('tabindex', '0');
                        containerRef.current.setAttribute('role', 'application');
                        containerRef.current.setAttribute('aria-label', 'Word cloud visualization');
                    }

                    // Mouse click event
                    chart.listen("pointClick", function(e) {
                        const wordClicked = e.point.get("x");
                        const total = e.point.get("value");
                        const occurrences = e.point.get("occurrences");
                        const { clientX, clientY } = e.originalEvent;
                        setModalData({
                            word: wordClicked,
                            total,
                            occurrences,
                            x: clientX,
                            y: clientY
                        });
                    });

                    chart.tooltip().enabled(false);
                    chart.angles([0]);

                    if (containerRef.current) {
                        chart.container(containerRef.current);
                        chart.draw();

                        // keyboard accessibility
                        containerRef.current.addEventListener('keydown', function(e) {
                            if (e.key === 'Enter' || e.key === ' ') {
                                // find the curr word and simulate a click
                                const focusedElement = document.activeElement;
                                if (focusedElement && focusedElement.tagName.toLowerCase() === 'text') {
                                    const wordClicked = focusedElement.textContent;

                                    const wordData = cloudWords.find(item => item.word === wordClicked);

                                    if (wordData) {
                                        const rect = focusedElement.getBoundingClientRect();
                                        setModalData({
                                            word: wordClicked,
                                            total: wordData.total,
                                            occurrences: wordData.songOccurrences,
                                            x: rect.left + rect.width / 2,
                                            y: rect.top + rect.height / 2
                                        });
                                    }
                                }
                            }

                            // Arrow key nav between words
                            if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
                                e.preventDefault();
                                const svg = containerRef.current.querySelector("svg");
                                if (svg) {
                                    const texts = Array.from(svg.querySelectorAll("text"));
                                    if (texts.length === 0) return;

                                    let currentIndex = -1;
                                    const focusedElement = document.activeElement;

                                    if (focusedElement && focusedElement.tagName.toLowerCase() === 'text') {
                                        currentIndex = texts.indexOf(focusedElement);
                                    }

                                    let nextIndex;
                                    if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
                                        nextIndex = (currentIndex + 1) % texts.length;
                                    } else {
                                        nextIndex = (currentIndex - 1 + texts.length) % texts.length;
                                    }

                                    texts[nextIndex].setAttribute('tabindex', '0');
                                    texts[nextIndex].focus();
                                }
                            }
                        });

                        //wait for render
                        setTimeout(() => {
                            const svg = containerRef.current.querySelector("svg");
                            if (svg) {
                                const texts = svg.querySelectorAll("text");
                                texts.forEach(textEl => {
                                    textEl.setAttribute("data-testid", "word-item");
                                    textEl.setAttribute("tabindex", "0");
                                    textEl.setAttribute("role", "button");
                                    textEl.setAttribute("aria-label", `${textEl.textContent}, frequency: ${
                                        cloudWords.find(w => w.word === textEl.textContent)?.total || ''
                                    }`);
                                });

                                //make first word focusable to start
                                if (texts.length > 0) {
                                    texts[0].focus();
                                }
                            }
                        }, 500);
                    } else {
                        console.error("Container element not found");
                    }
                })
                .catch(err => console.error("Error loading scripts", err));
        }
    }, [cloudWords, cloudMode]);

    // generate random position and rotation
    useEffect(() => {
        if (wordCloud.length > 0) {
            // Top 100 words
            const cloudCopy = structuredClone(wordCloud.slice(0, 100));
            const wordsWithTotal = cloudCopy.map(word => {
                const total = word.songOccurrences.reduce((sum, occurrence) => sum + occurrence.count, 0);
                return { ...word, total };
            });
            setCloudWords(wordsWithTotal);
        }
    }, [wordCloud]);

    const addFavoriteWordCloud = async (generate = false) => {
        setLoading(true);
        setError(null);

        if (!generate && cumulativeWords.length === 0 && wordCloud.length === 0) {
            const button = document.getElementById('add-favorites-button');
            if (button) {
                const tooltip = document.createElement('div');
                tooltip.innerText = 'Please generate a word cloud first';
                tooltip.className = 'tooltip-error';

                const rect = button.getBoundingClientRect();
                tooltip.style.left = `${rect.left + window.scrollX + rect.width / 2}px`;
                tooltip.style.top = `${rect.top + window.scrollY}px`;

                document.body.appendChild(tooltip);
                setTimeout(() => {
                    document.body.removeChild(tooltip);
                }, 2000);
            }
            setLoading(false);
            return;
        }

        try {
            const response = await fetch(
                `/api/favorites?userID=${sessionStorage.getItem("id")}`,
                { method: "GET" }
            );
            if (!response.ok) throw new Error(response.status);
            const data = await response.json(); // favorite songs
            setSelectedSongs(data);

            const response2 = await fetch('/api/search/generate/custom', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data),
            });
            if (!response2.ok) throw new Error(response2.status);
            const cloudData = await response2.json(); // generated cloud

            if (generate) {
                cloudData.sort((a, b) => {
                    const sumA = a.songOccurrences.reduce((s, o) => s + o.count, 0);
                    const sumB = b.songOccurrences.reduce((s, o) => s + o.count, 0);
                    return sumB - sumA;
                });
                handleNewWordCloudGenerated(cloudData);
            }
            else await handleAddToExistingCloud(false, cloudData);
        } catch (err) {
            setError('Failed to generate word cloud from favorite songs.');
            console.error('Word cloud error:', err);
        } finally {
            setLoading(false);
        }
    }

    const closeModal = () => {
        setModalData(null);
    };

    const resetTooltip = () => {
        const button = document.getElementById("favorite-button");
        button.innerText = "Add to Favorites";
        button.style.backgroundColor = "#4caf50";
        setTooltipError(null);
    };

    const fixLyrics = (lyrics, word) => {
        let fixed = lyrics.trim();
        fixed = fixed.replace(/ {2,}/g, "\n");
        fixed = fixed.replace(/\n{3,}/g, "\n\n");

        if (!word) return fixed;
        const escaped = word.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
        const regex = new RegExp(`(?<!\\w)(${escaped})`, 'gi');
        return fixed.replace(regex, '<mark>$1</mark>');
    };

    // attempt to add song to favorites
    // const addToFavorites = async (id) => {
    //     try {
    //         const response = await fetch(`/favorites?userID=${sessionStorage.getItem("id")}&songID=${id}`, {
    //             method: 'POST'
    //         });
    //         if (!response.ok) {
    //             throw new Error(`Error: ${response.status}`);
    //         }
    //         const button = document.getElementById("favorite-button");
    //         button.innerText = "Added to Favorites";
    //         button.style.backgroundColor = "gray";
    //     } catch (e) {
    //         setTooltipError("Already in Favorites!");
    //     }
    // };
    const handleAddToFavorites = (id) =>
        addToFavorites(id, setTooltipError);

    return (
        <div className="artist-search">
            <Navbar />
            <div className="artist-search-container">
                <div className="artist-search-component">
                    <div className="search-container">
                        <form onSubmit={handleArtistSearch} className="search-form">
                            <input
                                type="text"
                                name="name"
                                placeholder="Search for an artist..."
                                onChange={handleInputChange}
                                className="search-input"
                                data-testid="artist-name-input"
                                required
                                aria-label="Artist name"
                            />
                            <input
                                type="number"
                                name="num"
                                placeholder="# of songs"
                                className="search-input"
                                onChange={handleInputChange}
                                data-testid="artist-num-input"
                                style={{maxWidth: 120}}
                                required
                                aria-label="Number of songs"
                            />
                            <button
                                type="submit"
                                className="search_button"
                                data-testid="search-button"

                                aria-label="Search for artist"
 
                            >
                                Search
                            </button>
                            <div className="search-mode" aria-label="Search mode selection">
                                <input
                                    type="radio"
                                    id="auto"
                                    name="mode"
                                    value="auto"
                                    onChange={handleInputChange}
                                    checked={artistQuery.mode === 'auto'}
                                    aria-label="Automatic mode"
                                />
                                <label htmlFor="auto">Auto</label>

                                <input
                                    type="radio"
                                    id="manual"
                                    name="mode"
                                    value="manual"
                                    onChange={handleInputChange}
                                    checked={artistQuery.mode === 'manual'}
                                    aria-label="Manual mode"
                                />
                                <label htmlFor="manual">Manual</label>
                            </div>
                        </form>
                    </div>

                    <div className="results-container">
                        {loading && <div className="loading">Loading...</div>}
                        {error && <div className="error">{error}</div>}

                        {!loading && !error && artists.length > 0 && songs.length === 0 && (
                            <div className="song-list" data-testid="artist-list">
                                <h2><strong>Artists matching &quot;{searchedQuery.name}&quot;</strong></h2>
                                <ul aria-label="Artist search results">
                                    {artists.map((artist) => (
                                        /*<li
                                            key={artist.id}
                                            className="song-item artist-item"
                                            data-testid="artist-item"
                                            onClick={() => handleSearch(artist)}
                                        >*/
                                        <li
                                            key={artist.id}
                                            className="song-item artist-item"
                                            data-testid="artist-item"
                                            onClick={() => handleSearch(artist)}
                                            tabIndex="0"
                                            onKeyDown={(e) => {
                                                if (e.key === 'Enter' || e.key === ' ') {
                                                    e.preventDefault();
                                                    handleSearch(artist);
                                                }
                                            }}
                                            aria-label={`${artist.name} - Click to view songs`}
                                            role="button"
                                        >
                                            <div className="artist-container">
                                                <div className="artist-image-container">
                                                    {artist.imageUrl ? (
                                                        <img
                                                            src={artist.imageUrl}
                                                            alt={`${artist.name}`}
                                                            className="artist-image"
                                                        />
                                                    ) : (
                                                        <div className="artist-image-placeholder">
                                                            {artist.name.charAt(0).toUpperCase()}
                                                        </div>
                                                    )}
                                                </div>
                                                <div className="song-title">{artist.name}</div>
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        {/* Auto mode (top songs) */}
                        {!loading && !error && songs.length > 0 && searchedQuery.mode === 'auto' && (
                            <div className="song-list" data-testid="song-list">
                                <h2><strong>Top {searchedQuery.num} Songs by {searchedQuery.name}</strong></h2>
                                <ul aria-label="Songs by artist">
                                    {songs.map((song) => (
                                        <li key={song.id} className="song-item" data-testid="song-item">
                                            <div className="song-title">{song.title}</div>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}
                        {!loading && !error && songs.length > 0 && searchedQuery.mode === 'auto' && (
                            <div className="buttons_container">
                                <button
                                    className="generate-button"
                                    onClick={handleGenerateFromTopSongs}

                                    aria-label="Generate word cloud from top songs"
 
                                >
                                    Generate Word Cloud
                                </button>
                                <button
                                    className="add-to-existing-button"
                                    onClick={() => handleAddToExistingCloud(true)}

                                    aria-label="Add top songs to existing word cloud"
 
                                >
                                    Add to Existing Cloud
                                </button>
                            </div>
                        )}

                        {/* Manual mode (selected songs) */}
                        {!loading && !error && songs.length > 0 && searchedQuery.mode === 'manual' && (
                            <div className="song-list" data-testid="song-list">
                                <h2><strong>Songs by {searchedQuery.name}</strong></h2>
                                <ul>
                                    {songs.map((song) => (
                                        /*<li
                                            key={song.id}
                                            className={`song-item ${selectedSongs.some(s => s.id === song.id) ? 'selected' : ''}`}
                                            data-testid="song-item"
                                            onClick={() => handleSongSelection(song)}
                                        >*/
                                        <li
                                            key={song.id}
                                            className={`song-item ${selectedSongs.some(s => s.id === song.id) ? 'selected' : ''}`}
                                            data-testid="song-item"
                                            onClick={() => handleSongSelection(song)}
                                            tabIndex="0"
                                            onKeyDown={(e) => {
                                                if (e.key === 'Enter' || e.key === ' ') {
                                                    e.preventDefault();
                                                    handleSongSelection(song);
                                                }
                                            }}
                                            aria-label={`${song.title}${selectedSongs.some(s => s.id === song.id) ? ' - Selected' : ' - Click to select'}`}
                                            role="button"
                                        >
                                            <div className="song-title">{song.title}</div>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}
                        {!loading && !error && songs.length > 0 && searchedQuery.mode === 'manual' && (
                            <div className="buttons_container">
                                <button
                                    className="generate-button"
                                    onClick={handleGenerateFromSelected}
                                    disabled={selectedSongs.length === 0}

                                    aria-label={`Generate word cloud from ${selectedSongs.length} selected songs`}
                                    aria-disabled={selectedSongs.length === 0}

 
                                >
                                    Generate Word Cloud ({selectedSongs.length} selected)
                                </button>
                                <button
                                    className="add-to-existing-button"
                                    onClick={() => handleAddToExistingCloud(false)}
                                    disabled={selectedSongs.length === 0}

                                    aria-label={`Add ${selectedSongs.length} selected songs to existing cloud`}
                                    aria-disabled={selectedSongs.length === 0}
 
                                >
                                    Add to Existing Cloud ({selectedSongs.length} selected)
                                </button>
                            </div>
                        )}

                        {!loading && !error && artists.length === 0 && songs.length === 0 && artistQuery.name && (
                            <div className="no-results">No artists found for &quot;{artistQuery.name}&quot;</div>
                        )}
                    </div>
                </div>

                {/* Word cloud container for button and cloud/table */}
                <div className="cloud-container">
                    {cloudWords.length > 0 && (
                        <div className="search-mode" style={{ margin: '15px auto 10px auto' }} aria-label="View mode selection">
                            <input
                                type="radio"
                                id="cloud"
                                value="cloud"
                                name="cloudMode"
                                checked={cloudMode === 'cloud'}
                                onChange={e => setCloudMode(e.target.value)}
                                aria-label="Cloud view for word cloud"
                            />
                            <label htmlFor="cloud">Cloud</label>

                            <input
                                type="radio"
                                id="tabular"
                                value="tabular"
                                name="cloudMode"
                                checked={cloudMode === 'tabular'}
                                onChange={e => setCloudMode(e.target.value)}
                                aria-label="Tabular view"
                            />
                            <label htmlFor="tabular">Tabular</label>
                        </div>
                    )}
                    {/* Word cloud */}
                    {cloudWords.length > 0 && cloudMode === 'cloud' && (
                        <div
                            ref={containerRef}
                            id="word"
                            data-testid="word-cloud"
                            style={{width: '100%', height: '550px', maxHeight: '550px', margin: 0, padding: 0}}
                        ></div>
                    )}
                    {/* Word table */}
                    {cloudWords.length > 0 && cloudMode === 'tabular' && (
                        <div className="word-table-container" style={{ height: '550px', maxHeight: '550px' }}>
                            <table data-testid="word-table" style={{ width: '100%', borderCollapse: 'collapse' }} aria-label="Word frequency table">
                                <thead>
                                <tr>
                                    <th style={{ textAlign: 'left', borderBottom: '1px solid #ccc', padding: '8px' }}>Word</th>
                                    <th style={{ textAlign: 'right', borderBottom: '1px solid #ccc', padding: '8px' }}>Total</th>
                                </tr>
                                </thead>
                                <tbody>
                                {cloudWords.map((w) => (
                                    <tr key={w.word}>
                                        <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{w.word}</td>
                                        <td style={{ padding: '8px', borderBottom: '1px solid #eee', textAlign: 'right'}}>{w.total}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                    {/* Favorite buttons */}
                    {!error && (
                        <div className="buttons_container">
                            <button
                                className="generate-button"
                                onClick={() => addFavoriteWordCloud(true)}
                                style={{ backgroundColor: "deeppink" }}

                                aria-label="Generate word cloud from favorite songs"
 
                            >
                                Generate Cloud from Favorites
                            </button>
                            <button
                                className="add-to-existing-button"
                                id="add-favorites-button"
                                onClick={() => addFavoriteWordCloud(false)}
                                style={{ backgroundColor: "purple" }}

                                aria-label="Add favorite songs to existing word cloud"
 
                            >
                                Add Favorites to Existing Cloud
                            </button>
                        </div>
                    )}
                </div>

                {/* Modal for clicking word in word cloud */}
                {modalData && (
                    <div
                        className="modal-overlay"
                        style={{
                            position: 'fixed',
                            top: 0,
                            left: 0,
                            right: 0,
                            bottom: 0,
                            backgroundColor: 'rgba(0,0,0,0.5)'
                        }}
                        onClick={closeModal}


 
                    >
                        <div
                            className="modal-content"
                            style={{
                                position: 'absolute',
                                left: modalData.x,
                                top: modalData.y,
                                background: '#fff',
                                padding: '1rem',
                                borderRadius: '4px',
                                transform: 'translate(-95%, -5%)'
                            }}
                            onClick={(e) => e.stopPropagation()}
                        >

                            <button style={{ position: 'absolute', right: 7, top: 0, padding: 10 }} onClick={closeModal} aria-label="Close word details">x</button>
 
                            <h2 style={{ textAlign: 'center', marginBottom: 3 }}><strong>{modalData.word}</strong></h2>
                            <p><strong>Total:</strong> {modalData.total}</p>
                            <div>
                                <strong>Occurrences:</strong>
                                <ul>
                                    {modalData.occurrences &&
                                        modalData.occurrences.map(song => (
                                            <li key={song.id}>
                                                <a
                                                    aria-label={`View lyrics for ${song.title} by ${song.artist}`}
                                                    role="button"
                                                    onMouseEnter={(e) => {
                                                        clearTimeout(timeoutRef.current);
                                                        const rect = e.target.getBoundingClientRect();
                                                        setHoveredSong({
                                                            song,
                                                            x: rect.left + rect.width / 2,
                                                            y: rect.top + rect.height / 2
                                                        });
                                                        resetTooltip();
                                                    }}
                                                    onMouseLeave={() => {
                                                        timeoutRef.current = setTimeout(() => {
                                                            setHoveredSong(null);
                                                            resetTooltip();
                                                        }, 200);
                                                    }}
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        setClickedSong(song);
                                                    }}
                                                    style={{ cursor: 'pointer', textDecoration: 'none' }}
                                                >
                                                    {song.title} ({song.artist})
                                                </a>
                                                : {song.count}

                                            </li>
                                        ))
                                    }
                                </ul>
                            </div>
                        </div>
                    </div>
                )}

                {/* Tooltip for hovering song in modal */}
                {hoveredSong && !clickedSong && (
                    <div
                        className="small-tooltip"
                        onMouseEnter={() => {
                            clearTimeout(timeoutRef.current);
                            resetTooltip();
                        }}
                        onMouseLeave={() => {
                            timeoutRef.current = setTimeout(() => {
                                setHoveredSong(null);
                                resetTooltip();
                            }, 200);
                        }}
                        style={{
                            position: 'fixed',
                            left: hoveredSong.x,
                            top: hoveredSong.y + 6,
                            background: '#fff',
                            border: '1px solid #ccc',
                            padding: '0.5rem',
                            borderRadius: '4px',
                            boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
                            zIndex: 1100
                        }}
                    >
                        <h4 style={{ margin: 0, marginBottom: 5, textAlign: 'center' }}>
                            <strong>{hoveredSong.song.title}</strong>
                        </h4>
                        <button
                            id="favorite-button"
                            className="generate-button"
                            style={{ display: 'block', margin: 'auto' }}

                            onClick={() => handleAddToFavorites(hoveredSong.song.id)} aria-label={`Add ${hoveredSong.song.title} to favorites`}>
 
                            Add to Favorites
                        </button>
                        {tooltipError && (<p style={{ margin: '0.25rem auto', fontSize: '0.9rem', color: 'red', textAlign: 'center' }}>{tooltipError}</p>)}
                    </div>
                )}

                {/* Modal for clicking song in modal */}
                {clickedSong && (
                    <div
                        className="modal-overlay"
                        style={{
                            position: 'fixed',
                            top: 0,
                            left: 0,
                            right: 0,
                            bottom: 0,
                            backgroundColor: 'rgba(0,0,0,0.5)',
                            zIndex: 1200
                        }}
                        onClick={() => setClickedSong(null)}

                        aria-modal="true"
                        aria-labelledby="song-title"
 
                    >
                        <div
                            className="modal-content"
                            style={{
                                position: 'absolute',
                                left: '50%',
                                top: '50%',
                                background: '#fff',
                                padding: '1rem',
                                borderRadius: '4px',
                                transform: 'translate(-50%, -50%)',
                                maxHeight: '80%',
                                overflowY: 'auto'
                            }}
                            onClick={(e) => e.stopPropagation()}
                        >
                            <button
                                style={{ position: 'absolute', right: 7, top: 7, padding: 5 }}
                                onClick={() => setClickedSong(null)}
                                aria-label="Close song details"
                            >
                                x
                            </button>
                            <h1 style={{ textAlign: 'center', marginBottom: 10, fontSize: 20 }}>
                                {clickedSong.title}
                            </h1>
                            <p><strong>Artist:</strong> {clickedSong.artist}</p>
                            <p><strong>Year:</strong> {clickedSong.year || 'N/A'}</p>
                            <div>
                                <strong>Lyrics (with "{modalData.word}" highlighted):</strong>
                                <div
                                    style={{ whiteSpace: 'pre-wrap' }}
                                    dangerouslySetInnerHTML={{
                                        __html: fixLyrics(clickedSong.lyrics, modalData.word)
                                    }}
                                />
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default ArtistSearch;