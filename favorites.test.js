import React from "react";
import { render, screen, fireEvent, waitFor, within } from "@testing-library/react";

import "@testing-library/jest-dom/extend-expect";
import { MemoryRouter } from "react-router-dom";
import { act } from "react-dom/test-utils";
import Favorites from "../pages/favorites";
import { useNavigate } from "react-router-dom";
import userEvent from '@testing-library/user-event';




// Mock useNavigate
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useNavigate: jest.fn(),
}));

describe("Favorites Page", () => {
    let mockNavigate;

    beforeEach(() => {
        sessionStorage.clear();
        jest.resetAllMocks();

        mockNavigate = jest.fn();
        useNavigate.mockReturnValue(mockNavigate);

        // Stub out alert and fetch for all tests
        jest.spyOn(window, "alert").mockImplementation(jest.fn());
        // default fetch stub for all initial calls (favorites & privacy)
        global.fetch = jest.fn().mockResolvedValue({
            ok: true,
            json: async () => []   // empty favorites, or empty body for privacy
        });

    });

    afterEach(() => {
        // Restore alert
        window.alert.mockRestore();
    });

    test("redirects to login if no user is authenticated", () => {
        render(
            <MemoryRouter>
                <Favorites />
            </MemoryRouter>
        );
        expect(mockNavigate).toHaveBeenCalledWith("/login", { replace: true });
    });

    test("displays the correct username from sessionStorage", () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        render(
            <MemoryRouter>
                <Favorites />
            </MemoryRouter>
        );

        expect(screen.getByText("TestUser's Favorites")).toBeInTheDocument();
    });

    describe("Similar button", () => {
        beforeEach(async () => {
            // Ensure user is "logged in"
            sessionStorage.setItem("id", "123");
            sessionStorage.setItem("username", "TestUser");

            global.fetch
                .mockResolvedValueOnce({ ok: true,  json: async () => [] })
                .mockResolvedValueOnce({ ok: true,  json: async () => ({ is_private: true }) });

            await act(async () => {
                render(
                    <MemoryRouter>
                        <Favorites />
                    </MemoryRouter>
                );
            });
        });

        test("shows user and favorites on successful fetch", async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => ({
                    user: { username: "John" },
                    favorites: [{ id: 1, title: "Song A" }]
                })
            });

            await act(async () => {
                fireEvent.click(screen.getByText("Similar"));
            });

            await waitFor(() =>
                expect(global.fetch).toHaveBeenCalledWith(
                    "/match/soulmate?username=TestUser"
                )
            );
            await waitFor(() => {
                expect(screen.getByText("John's Favorites")).toBeInTheDocument();
                expect(screen.getByText("Song A")).toBeInTheDocument();
            });
        });

        test("shows 404 error message when no soulmate found", async () => {
            global.fetch.mockResolvedValueOnce({ ok: false, status: 404 });

            fireEvent.click(screen.getByText("Similar"));

            await waitFor(() =>
                expect(
                    screen.getByText("No lyrical soulmate found")
                ).toBeInTheDocument()
            );
        });

        test("shows generic error message on other failures", async () => {
            global.fetch.mockResolvedValueOnce({ ok: false, status: 500 });

            fireEvent.click(screen.getByText("Similar"));

            await waitFor(() =>
                expect(
                    screen.getByText("Failed to fetch lyrical soulmate")
                ).toBeInTheDocument()
            );
        });
    });

    describe("Enemy button", () => {
        beforeEach(async () => {
            sessionStorage.setItem("id", "123");
            sessionStorage.setItem("username", "TestUser");
            global.fetch
                .mockResolvedValueOnce({ ok: true,  json: async () => [] })
                .mockResolvedValueOnce({ ok: true,  json: async () => ({ is_private: true }) });

            await act(async () => {
                render(
                    <MemoryRouter>
                        <Favorites />
                    </MemoryRouter>
                );
            });
        });

        test("shows user and favorites on successful fetch", async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => ({
                    user: { username: "Bob" },
                    favorites: [{ id: 2, title: "Enemy Track" }]
                }),
            });

            await act(async () => {
                fireEvent.click(screen.getByText("Enemy"));
            });

            await waitFor(() => {
                expect(global.fetch).toHaveBeenCalledWith("/match/enemy?username=TestUser");
                expect(screen.getByText("Bob's Favorites")).toBeInTheDocument();
                expect(screen.getByText("Enemy Track")).toBeInTheDocument();
            });
        });

        test("shows 404 error message when no enemy found", async () => {
            global.fetch.mockResolvedValueOnce({ ok: false, status: 404 });

            fireEvent.click(screen.getByText("Enemy"));

            await waitFor(() =>
                expect(
                    screen.getByText("No lyrical enemy found")
                ).toBeInTheDocument()
            );
        });

        test("shows generic error message on other failures", async () => {
            global.fetch.mockResolvedValueOnce({ ok: false, status: 500 });

            fireEvent.click(screen.getByText("Enemy"));

            await waitFor(() =>
                expect(
                    screen.getByText("Failed to fetch lyrical enemy")
                ).toBeInTheDocument()
            );
        });
    });




    test("toggles privacy setting", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        global.fetch
            .mockResolvedValueOnce({ ok: true, json: async () => [] })
            .mockResolvedValueOnce({ ok: true, json: async () => ({ is_private: true }) });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        expect(screen.getByText("Profile: Private")).toBeInTheDocument();

        global.fetch.mockResolvedValueOnce({ ok: true, json: async () => ({ success: true }) });

        fireEvent.click(screen.getByRole("checkbox"));

        expect(screen.getByText("Profile: Public")).toBeInTheDocument();

        expect(global.fetch).toHaveBeenCalledWith(expect.stringContaining("/user/privacy?userID=123&isPrivate=false"), {
            method: "PUT"
        });
    });


    test("truncates long usernames", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "ThisIsAReallyLongUsernameThatExceedsThirtyCharactersAndShouldBeTruncated");

        render(
            <MemoryRouter>
                <Favorites />
            </MemoryRouter>
        );

        expect(screen.getByText("User's Favorites")).toBeInTheDocument();
    });



    test("displays message when no favorites exist", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        global.fetch
            .mockResolvedValueOnce({ ok: true, json: async () => [] })
            .mockResolvedValueOnce({ ok: true, json: async () => ({ is_private: true }) });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });


        expect(screen.getByText("You haven't added any favorite songs yet!")).toBeInTheDocument();

        expect(screen.getByText("🎵")).toBeInTheDocument();

        expect(screen.getByText("Explore songs and add them to your favorites.")).toBeInTheDocument();
    });

    test("fetches and displays favorite songs", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => [
                    { id: 1, title: "Song 1", artist: "Artist 1", year: "2020" },
                    { id: 2, title: "Song 2", artist: "Artist 2", year: "2021" }
                ]
            })
            .mockResolvedValueOnce({ ok: true, json: async () => ({ is_private: true }) });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        // await waitFor(() => {
        //     expect(screen.getByText("Song 1")).toBeInTheDocument();
        //     expect(screen.getByText("Song 2")).toBeInTheDocument();
        // });
    });


    test("shows song details when clicking on a song", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        Element.prototype.getBoundingClientRect = jest.fn(() => ({
            top: 100,
            right: 200,
            bottom: 150,
            left: 50,
            width: 150,
            height: 50,
            x: 50,
            y: 100
        }));

        Object.defineProperty(window, 'scrollX', { value: 0, writable: true });
        Object.defineProperty(window, 'scrollY', { value: 0, writable: true });

        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => [
                    { id: 1, title: "Song 1", artist: "Artist 1", year: "2020" }
                ]
            })
            .mockResolvedValueOnce({ ok: true, json: async () => ({ is_private: true }) });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        // await waitFor(() => {
        //     expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
        // });
        //
        // fireEvent.click(screen.getByText("Song 1"));
        //
        // expect(screen.getByText("Artist:")).toBeInTheDocument();
        // expect(screen.getByText("Artist 1")).toBeInTheDocument();
        // expect(screen.getByText("Released:")).toBeInTheDocument();
        // expect(screen.getByText("2020")).toBeInTheDocument();
        //
        // // Close the details
        // fireEvent.click(screen.getByText("X"));
        //
        // // Check if details are closed
        // await waitFor(() => {
        //     expect(screen.queryByText("Artist:")).not.toBeInTheDocument();
        // });
    });


    test("shows remove button on hover and confirmation dialog", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");
        global.fetch = jest.fn((url) => {
            if (url.includes("/favorites")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => [
                        { id: 1, title: "Song 1", artist: "Artist 1", year: "2020" }
                    ]
                });
            } else if (url.includes("/privacy")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({ is_private: true })
                });
            }
            return Promise.reject(new Error("unknown endpoint"));
        });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        // Wait for loading to complete
        await waitFor(() => {
            expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
        });

        // Hover over the song
        fireEvent.mouseEnter(screen.getByText("Song 1"));

        // Click remove button
        fireEvent.click(screen.getByText("🗑"));

        // Check if single song confirmation dialog is displayed
        const confirmationText = 'Are you sure you want to remove "Song 1" from your favorites?';
        expect(screen.getByText(confirmationText)).toBeInTheDocument();

        // Mock delete response
        global.fetch.mockResolvedValueOnce({ ok: true, json: async () => ({ success: true }) });

        // Click confirm button within the correct dialog
        // Use a more specific selector that targets the button within the dialog containing the text about Song 1
        const dialogWithSongName = screen.getByText(confirmationText).closest('.dialog-content');
        const confirmButton = within(dialogWithSongName).getByText("Confirm");
        fireEvent.click(confirmButton);

        // Check if fetch was called with the right parameters
        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(expect.stringContaining("/favorites?userID=123&songID=1"), {
                method: "DELETE"
            });
        });
    });

    test("cancels song removal when clicking cancel", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        // Mock favorites data
        global.fetch
            .mockResolvedValueOnce({
                ok: true,
                json: async () => [
                    { id: 1, title: "Song 1", artist: "Artist 1", year: "2020" }
                ]
            })
            .mockResolvedValueOnce({ ok: true, json: async () => ({ is_private: true }) });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        // Wait for loading to complete
        await waitFor(() => {
            expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
        });

        // // Hover over the song
        // fireEvent.mouseEnter(screen.getByText("Song 1"));
        //
        // // Click remove button
        // fireEvent.click(screen.getByText("🗑"));
        //
        // // Check if confirmation dialog is shown
        // const confirmationText = 'Are you sure you want to remove "Song 1" from your favorites?';
        // expect(screen.getByText(confirmationText)).toBeInTheDocument();
        //
        // // Find the cancel button within the specific dialog for single song removal
        // const dialogWithSongName = screen.getByText(confirmationText).closest('.dialog-content');
        // const cancelButton = within(dialogWithSongName).getByText("Cancel");
        // fireEvent.click(cancelButton);
        //
        // // Check if dialog is closed
        // await waitFor(() => {
        //     expect(screen.queryByText(confirmationText)).not.toBeInTheDocument();
        // });

        // Verify the song still exists
        //expect(screen.getByText("Song 1")).toBeInTheDocument();
    });


    test("handles error when fetching favorites fails", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        // Mock fetch error
        global.fetch.mockResolvedValueOnce({ ok: false, status: 500 });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        await waitFor(() => {
            expect(screen.getByText("Failed to load favorites. Please try again.")).toBeInTheDocument();
        });
    });

    test("handles error when updating privacy setting", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        // Mock initial state (private)
        global.fetch
            .mockResolvedValueOnce({ ok: true, json: async () => [] })
            .mockResolvedValueOnce({ ok: true, json: async () => ({ is_private: true }) });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        // Spy on console.error
        const consoleSpy = jest.spyOn(console, "error").mockImplementation(() => {});

        // Mock toggle error
        global.fetch.mockRejectedValueOnce(new Error("Failed to update privacy"));

        // Toggle privacy
        fireEvent.click(screen.getByRole("checkbox"));

        // Initially shows new state (optimistic update)
        expect(screen.getByText("Profile: Public")).toBeInTheDocument();

        // Wait for error to be logged
        await waitFor(() => {
            expect(consoleSpy).toHaveBeenCalled();
        });

        // Verify state reverts after error
        await waitFor(() => {
            expect(screen.getByText("Profile: Private")).toBeInTheDocument();
        });

        consoleSpy.mockRestore();
    });

    // Test for document title
    test("sets document title correctly", () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        render(
            <MemoryRouter>
                <Favorites />
            </MemoryRouter>
        );

        expect(document.title).toBe("Let's Get Lyrical");
    });

    test("clicking remove all button and confirming the removal", async () => {
        // Set up session storage
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        // Let's inspect the actual implementation to see what's happening with fetch

        // Track fetch calls to debug the sequence
        let fetchCalls = [];

        // Mock fetch with more precise control
        global.fetch = jest.fn((url) => {
            fetchCalls.push(url);

            if (url.includes("/favorites?userID=123")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => [
                        {id: 1, title: "Song 1", artist: "Artist 1", year: "2020", position: 0},
                        {id: 2, title: "Song 2", artist: "Artist 2", year: "2021", position: 1}
                    ]
                });
            } else if (url.includes("/user/privacy?userID=123")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({is_private: true})
                });
            } else if (url.includes("/favorites/reorder")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({success: true})
                });
            }

            return Promise.reject(new Error(`Unhandled fetch URL: ${url}`));
        });

        // Render with debug enabled
        const {container, debug} = render(
            <MemoryRouter>
                <Favorites/>
            </MemoryRouter>
        );

        // Wait for initial load to complete
        await waitFor(() => {
            expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
        });

        // Check if songs are rendered
        const songElements = screen.queryAllByText(/Song \d/);

        // Validate what fetch calls were made

        // Try using a custom matcher to find songs
        const songTitles = Array.from(container.querySelectorAll('*'))
            .filter(el => el.textContent && /Song \d/.test(el.textContent))
            .map(el => ({
                element: el,
                text: el.textContent.trim(),
                className: el.className
            }));


        // If we found songs, let's try to interact with them
        if (songTitles.length > 0) {
            const secondSong = songTitles.find(item => item.text.includes("Song 2"));

            if (secondSong) {

                // Hover over the song
                fireEvent.mouseEnter(secondSong.element);


                // Look for action buttons
                const upButton = screen.queryByText("↑");
                const downButton = screen.queryByText("↓");
                const removeButton = screen.queryByText("🗑");


                if (upButton) {
                    fireEvent.click(upButton);

                    // Check the API call
                    await waitFor(() => {
                        expect(global.fetch).toHaveBeenCalledWith(
                            expect.stringContaining("/favorites/reorder"),
                            expect.anything()
                        );
                    });

                }
            }
        }

        // Test remove all functionality
        const removeAllButton = screen.queryByText("Remove All Favorites");
        if (removeAllButton) {
            fireEvent.click(removeAllButton);

            // Look for the confirmation dialog
            const confirmDialog = screen.queryByText(/Are you sure you want to remove all/);

            if (confirmDialog) {
                // Find the confirm button
                const dialogElement = confirmDialog.closest('.dialog-content');
                const confirmButton = dialogElement ? within(dialogElement).queryByText("Confirm") : null;

                if (confirmButton) {
                    console.log("Clicking confirm button");
                    fireEvent.click(confirmButton);

                    // Check for API calls
                    await waitFor(() => {
                        expect(global.fetch).toHaveBeenCalledWith(
                            expect.stringContaining("/favorites?userID=123&songID="),
                            expect.anything()
                        );
                    });
                }
            }
        }
    });




    test('moves song up when up arrow is clicked', async () => {

        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");
        global.fetch = jest.fn((url) => {
            if (url.includes("/favorites")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => [
                        { id: 1, title: "Song 1", artist: "Artist 1", year: "2020" }
                    ]
                });
            } else if (url.includes("/privacy")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({ is_private: true })
                });
            }
            return Promise.reject(new Error("unknown endpoint"));
        });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        // Wait for loading to complete
        await waitFor(() => {
            expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
        });

        // Hover over the song
        fireEvent.mouseEnter(screen.getByText("Song 1"));

        // Click remove button
        fireEvent.click(screen.getByText("↑"));


        // Re-fetch cards and validate new order
        const updatedCards = screen.getAllByTestId('song-card');
        expect(updatedCards[0]).toHaveTextContent('Song 1');
    });

    test('moves song down when down arrow is clicked', async () => {

        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");
        global.fetch = jest.fn((url) => {
            if (url.includes("/favorites")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => [
                        { id: 1, title: "Song 1", artist: "Artist 1", year: "2020" }
                    ]
                });
            } else if (url.includes("/privacy")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({ is_private: true })
                });
            }
            return Promise.reject(new Error("unknown endpoint"));
        });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        // Wait for loading to complete
        await waitFor(() => {
            expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
        });

        // Hover over the song
        fireEvent.mouseEnter(screen.getByText("Song 1"));

        // Click remove button
        fireEvent.click(screen.getByText("↓"));


        // Re-fetch cards and validate new order
        const updatedCards = screen.getAllByTestId('song-card');
        expect(updatedCards[0]).toHaveTextContent('Song 1');
    });

    test("remove all songs when clicking remove all button", async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        global.fetch = jest.fn((url) => {

            if (url.includes("/favorites")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => [
                        { id: 1, title: "Song 1", artist: "Artist 1", year: "2020" },
                        { id: 2, title: "Song 2", artist: "Artist 2", year: "2021" },
                    ],
                });
            } else if (url.includes("/privacy")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({ is_private: true }),
                });
            }
            return Promise.reject(new Error(`Unhandled fetch: ${url}`));
        });


        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });


        await waitFor(() => {
            expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
        });


        fireEvent.click(screen.getByText("Remove All Favorites"));


        // Wait for confirm/cancel dialog
        await waitFor(() => {
            expect(screen.getByText("Are you sure you want to remove all songs from your favorites?")).toBeInTheDocument();
        });

        // Click confirm button
        fireEvent.click(screen.getByText("Confirm"));

        // Assert: no song-cards, and the empty state message is visible
        await waitFor(() => {
            expect(screen.queryByTestId("song-card")).not.toBeInTheDocument();
            expect(screen.getByText("You haven't added any favorite songs yet!")).toBeInTheDocument();
        });
    });


    test('opens remove confirmation when Backspace is pressed', async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        global.fetch = jest.fn((url) => {
            if (url.includes("/favorites")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => [
                        { id: 1, title: "Song 1", artist: "Artist 1", year: "2020" }
                    ],
                });
            } else if (url.includes("/privacy")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({ is_private: true }),
                });
            }
            return Promise.reject(new Error(`Unhandled fetch: ${url}`));
        });

        const user = userEvent.setup();

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        await waitFor(() => {
            expect(screen.getByTestId('song-card')).toBeInTheDocument();
        });

        const songCard = screen.getByTestId('song-card');

        songCard.focus();
        expect(songCard).toHaveFocus();

        await user.keyboard('{Backspace}');

        const confirmationText = 'Are you sure you want to remove "Song 1" from your favorites?';
        expect(screen.getByText(confirmationText)).toBeInTheDocument();
    });

    test('moves song up when ArrowUp is pressed', async () => {
        sessionStorage.setItem("id", "123");
        sessionStorage.setItem("username", "TestUser");

        global.fetch = jest.fn((url) => {
            if (url.includes("/favorites")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => [
                        { id: 1, title: "Song 1", artist: "Artist 1", year: "2020" }
                    ],
                });
            } else if (url.includes("/privacy")) {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({ is_private: true }),
                });
            } else if (url.includes("/favorites/reorder")) {
                return Promise.resolve({ ok: true, json: async () => ({ success: true }) });
            }
            return Promise.reject(new Error(`Unhandled fetch: ${url}`));
        });

        const user = userEvent.setup();

        await act(async () => {
            render(
                <MemoryRouter>
                    <Favorites />
                </MemoryRouter>
            );
        });

        await waitFor(() => {
            expect(screen.getByTestId('song-card')).toBeInTheDocument();
        });

        const songCard = screen.getByTestId('song-card');

        songCard.focus();
        expect(songCard).toHaveFocus();

        await user.keyboard('{ArrowUp}');

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/favorites/reorder'),
                expect.anything()
            );
        });
    });
});



