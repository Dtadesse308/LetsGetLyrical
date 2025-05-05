import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Navbar from '../components/Navbar';
import React from 'react';

// Mock react-router-dom
jest.mock('react-router-dom', () => {
    const actual = jest.requireActual('react-router-dom');
    return {
        ...actual,
        useNavigate: jest.fn()
    };
});

describe('Navbar Component', () => {
    // Create a mock for sessionStorage
    const mockSessionStorage = (() => {
        let store = {};
        return {
            getItem: jest.fn(key => store[key] || null),
            setItem: jest.fn((key, value) => {
                store[key] = value.toString();
            }),
            clear: jest.fn(() => {
                store = {};
            })
        };
    })();

    // Replace the real sessionStorage with our mock
    Object.defineProperty(window, 'sessionStorage', {
        value: mockSessionStorage
    });

    // Mock navigate function
    const mockNavigate = jest.fn();

    beforeEach(() => {
        // Reset mocks before each test
        jest.clearAllMocks();

        // Setup default navigate mock
        require('react-router-dom').useNavigate.mockReturnValue(mockNavigate);
    });

    test('redirects to login when no username in sessionStorage', () => {
        // Ensure sessionStorage returns null for username
        mockSessionStorage.getItem.mockReturnValueOnce(null);

        render(
            <MemoryRouter>
                <Navbar />
            </MemoryRouter>
        );

        // Check if navigate was called with the correct arguments
        expect(mockNavigate).toHaveBeenCalledWith('/login', { replace: true });
    });

    test('displays Title when user is logged in', () => {
        // Set a username in the mock sessionStorage
        mockSessionStorage.getItem.mockReturnValueOnce('testUser');
        render(
            <MemoryRouter>
                <Navbar />
            </MemoryRouter>
        );

        // Check if the username is displayed
        expect(screen.getByText("Let's Get Lyrical")).toBeInTheDocument();
        expect(mockNavigate).not.toHaveBeenCalled();
    });

    test('contains links to search and favorites pages', () => {
        // Set a username in the mock sessionStorage
        mockSessionStorage.getItem.mockReturnValueOnce('testUser');

        render(
            <MemoryRouter>
                <Navbar />
            </MemoryRouter>
        );

        // Check if the links are rendered correctly
        const searchLink = screen.getByText('Artist Search');
        const favoritesLink = screen.getByText('Favorites');

        expect(searchLink).toBeInTheDocument();
        expect(searchLink.closest('a')).toHaveAttribute('href', '/search');

        expect(favoritesLink).toBeInTheDocument();
        expect(favoritesLink.closest('a')).toHaveAttribute('href', '/favorites');
    });

    test('logout button clears session and navigates to login', () => {
        // Set a username in the mock sessionStorage
        mockSessionStorage.getItem.mockReturnValueOnce('testUser');

        render(
            <MemoryRouter>
                <Navbar />
            </MemoryRouter>
        );

        // Find and click the logout button
        const logoutButton = screen.getByText('Logout');
        fireEvent.click(logoutButton);

        // Check if sessionStorage was cleared and navigation happened
        expect(mockSessionStorage.clear).toHaveBeenCalled();
        expect(mockNavigate).toHaveBeenCalledWith('/login');
    });


});