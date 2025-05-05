import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import FriendSearch from './FriendSearch';

describe('FriendSearch', () => {
    test('renders search input and button', () => {
        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        expect(screen.getByPlaceholderText('Search for a friend')).toBeInTheDocument();
        expect(screen.getByText('Search')).toBeInTheDocument();
    });

    test('displays loading indicator when searching', async () => {
        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    test('displays error message when no user is found', async () => {
        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: 'nonexistentuser' } });
        fireEvent.click(screen.getByText('Search'));

        expect(await screen.findByText('no user found with name: nonexistentuser')).toBeInTheDocument();
    });

    test('displays search results', async () => {
        const mockFriend = { id: 1, username: 'testuser' };
        fetch.mockImplementation(() =>
            Promise.resolve({
                ok: true,
                json: () => Promise.resolve(mockFriend),
            })
        );

        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText(/Search for a friend/i), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        expect(await screen.findByText('testuser', { selector: 'button' })).toBeInTheDocument();
    });

    test('adds friend to selected friends list', async () => {
        sessionStorage.setItem('username', 'loggedInUser');
        const mockFriend = { id: 1, username: 'testuser' };
        fetch.mockImplementation(() =>
            Promise.resolve({
                ok: true,
                json: () => Promise.resolve(mockFriend),
            })
        );

        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText(/Search for a friend/i), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        // click on the friend from the search results to add to seected friend
        const searchResult = await screen.findByText('testuser', { selector: 'button' });
        fireEvent.click(searchResult);

        expect(screen.getByText('Selected Friends:')).toBeInTheDocument();
        expect(screen.getByText('- testuser', { selector: 'li' })).toBeInTheDocument();
    });


    test('username is empty or all whitespaces', () => {
        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: '   ' } });
        fireEvent.click(screen.getByText('Search'));

        //nothing happens
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
        expect(screen.getByPlaceholderText('Search for a friend')).toBeInTheDocument();


    });

    test('search fetch call fails', async () => {
        fetch.mockImplementation(() =>
            Promise.resolve({
                ok: false,
                status: 404,
            })
        );

        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        //error message apears
        expect(await screen.findByText('no user found with name: testuser')).toBeInTheDocument();
    });

    test('select a friend that is already in the selected friends list', async () => {
        const mockFriend = { id: 1, username: 'testuser' };
        fetch.mockImplementation(() =>
            Promise.resolve({
                ok: true,
                json: () => Promise.resolve(mockFriend),
            })
        );

        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        //search for testuser
        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        //add test user to selected friends 2x
        const searchResult = await screen.findByText('testuser', { selector: 'button' });
        fireEvent.click(searchResult);
        fireEvent.click(searchResult);

        //should only see one
        const selectedFriends = screen.getAllByText('- testuser', { selector: 'li' });
        expect(selectedFriends.length).toBe(1);
    });

    test('compare friends favorite songs successfully', async () => {
        const mockFriend = { id: 1, username: 'testuser' };
        const mockComparisonResults = [
            { id: 1, title: 'Song1', artist: 'Artist1', favoritedByUsers: [{ username: 'user1' }, { username: 'user2' }] },
            { id: 2, title: 'Song2', artist: 'Artist2', favoritedByUsers: [{ username: 'user3' }] }
        ];
        fetch.mockImplementation((url) => {
            if (url.includes('/friend/search')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockFriend),
                });
            } else if (url.includes('/friend/compare')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockComparisonResults),
                });
            }
        });

        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        const searchResult = await screen.findByText('testuser', { selector: 'button' });
        fireEvent.click(searchResult);

        fireEvent.click(screen.getByText('Compare Friends'));

        expect(await screen.findByText('Comparison of the Songs')).toBeInTheDocument();
        expect(screen.getByText('Song1')).toBeInTheDocument();
        expect(screen.getByText('3')).toBeInTheDocument(); // 2 favoritedByUsers + 1 logged-in user
        expect(screen.getByText('Song2')).toBeInTheDocument();
        expect(screen.getByText('2')).toBeInTheDocument(); // 1 favoritedByUser + 1 logged-in user
    });

    test('compare friends fail api call', async () => {
        const mockFriend = { id: 1, username: 'testuser' };
        fetch.mockImplementation((url) => {
            if (url.includes('/friend/search')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockFriend),
                });
            } else if (url.includes('/friend/compare')) {
                return Promise.resolve({
                    ok: false,
                    status: 404,
                });
            }
        });

        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        const searchResult = await screen.findByText('testuser', { selector: 'button' });
        fireEvent.click(searchResult);

        fireEvent.click(screen.getByText('Compare Friends'));

        expect(await screen.findByText('unable to compare songs with testuser')).toBeInTheDocument();
    });

    test('reverse the order of the comparison results', async () => {
        const mockFriend = { id: 1, username: 'testuser' };
        const mockComparisonResults = [
            { id: 1, title: 'Song1', artist: 'Artist1', favoritedByUsers: [{ username: 'user1' }, { username: 'user2' }] },
            { id: 2, title: 'Song2', artist: 'Artist2', favoritedByUsers: [{ username: 'user3' }] }
        ];
        fetch.mockImplementation((url) => {
            if (url.includes('/friend/search')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockFriend),
                });
            } else if (url.includes('/friend/compare')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockComparisonResults),
                });
            }
        });

        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        const searchResult = await screen.findByText('testuser', { selector: 'button' });
        fireEvent.click(searchResult);

        fireEvent.click(screen.getByText('Compare Friends', { selector: 'button' }));

        expect(await screen.findByText('descending order')).toBeInTheDocument();
        fireEvent.click(screen.getByText('descending order'));
        expect(await screen.findByText('ascending order')).toBeInTheDocument();

        fireEvent.click(screen.getByText('ascending order'));
        expect(await screen.findByText('descending order')).toBeInTheDocument();
    });

    test('displays song info modal when song title button is clicked', async () => {
        const mockFriend = { id: 1, username: 'user2' };
        const mockComparisonResults = [
            { id: 1, title: 'Song1', artist: 'Artist1', favoritedByUsers: [{ username: 'user1' }, { username: 'user2' }] }
        ];

        fetch.mockImplementation((url) => {
            if (url.includes('/friend/search')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockFriend),
                });
            } else if (url.includes('/friend/compare')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockComparisonResults),
                });
            }
        });

        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        const searchResult = await screen.findByText('user2', { selector: 'button' });
        fireEvent.click(searchResult);

        fireEvent.click(screen.getByText('Compare Friends'));

        const songTitleButton = await screen.findByText('Song1', { selector: 'button' });
        fireEvent.click(songTitleButton);

        // Use a custom matcher to find the artist information
        expect(await screen.findByText((content, element) => {
            return element?.textContent === 'Artist: Artist1';
        })).toBeInTheDocument();

        fireEvent.click(screen.getByText('Close', { selector: 'button' }));
        expect(screen.queryByText((content, element) => {
            return element?.textContent === 'Artist: Artist1';
        })).not.toBeInTheDocument();
    });

    test('displays favorited users modal when favorited users button is clicked', async () => {
        const mockFriend = { id: 1, username: 'testuser' };
        const mockComparisonResults = [
            { id: 1, title: 'Song1', artist: 'Artist1', favoritedByUsers: [{ username: 'user1' }, { username: 'user2' }] }
        ];
        sessionStorage.setItem('username', 'loggedInUser');
        fetch.mockImplementation((url) => {
            if (url.includes('/friend/search')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockFriend),
                });
            } else if (url.includes('/friend/compare')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockComparisonResults),
                });
            }
        });

        render(
            <MemoryRouter>
                <FriendSearch />
            </MemoryRouter>
        );

        fireEvent.change(screen.getByPlaceholderText('Search for a friend'), { target: { value: 'testuser' } });
        fireEvent.click(screen.getByText('Search'));

        const searchResult = await screen.findByText('testuser', { selector: 'button' });
        fireEvent.click(searchResult);

        fireEvent.click(screen.getByText('Compare Friends'));

        const favoritedUsersButton = await screen.findByText('3', { selector: 'button' });
        fireEvent.click(favoritedUsersButton);

        expect(await screen.findByText((content, element) => {
            return element?.textContent === 'Favorited by: loggedInUser, user1, user2';
        })).toBeInTheDocument();
        fireEvent.click(screen.getByText('Close', { selector: 'button' }));

        expect(screen.queryByText((content, element) => {
            return element?.textContent === 'Favorited by: loggedInUser, user1, user2';
        })).not.toBeInTheDocument();
    });
});

