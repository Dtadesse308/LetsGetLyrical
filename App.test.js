import React from 'react';
import {render, screen, act, fireEvent} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import App from './App';

jest.mock('./pages/Login', () => () => <div data-testid="login-page">Login Page</div>);
jest.mock('./pages/Register', () => () => <div data-testid="register-page">Register Page</div>);
jest.mock('./pages/ArtistSearch', () => () => <div data-testid="artist-page">Artist Search Page</div>);


// Mocks
jest.useFakeTimers();

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => {
    const actual = jest.requireActual("react-router-dom");
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

describe('App', () => {

    it('Login at "/login"', () => {
        render(
            <MemoryRouter initialEntries={['/login']}>
                <App />
            </MemoryRouter>
        );
        expect(screen.getByTestId('login-page')).toBeInTheDocument();
    });

    it('Register at "/register"', () => {
        render(
            <MemoryRouter initialEntries={['/register']}>
                <App />
            </MemoryRouter>
        );
        expect(screen.getByTestId('register-page')).toBeInTheDocument();
    });


});

describe('App inactivity logout', () => {

    beforeEach(() => {
        jest.clearAllTimers();
        jest.clearAllMocks();
        sessionStorage.clear();
    });

    const setup = () =>
        render(
            <MemoryRouter initialEntries={["/search"]}>
                <App />
            </MemoryRouter>
        );

    test("should log out after 60 seconds of inactivity", () => {
        setup();

        const spy = jest.spyOn(sessionStorage.__proto__, "clear");

        act(() => {
            jest.advanceTimersByTime(60000);
        });

        expect(spy).toHaveBeenCalled();
        expect(mockNavigate).toHaveBeenCalledWith("/login");
    });

    test("should reset logout timer on user activity", () => {
        setup();

        // Simulate user activity at 30 seconds
        act(() => {
            jest.advanceTimersByTime(30000);
        });
        // Simulate activity
        act(() => {
            fireEvent.mouseMove(document); // This should reset the timer
        });

        act(() => {
            jest.advanceTimersByTime(30000);
        });

        // Should not logout yet


        expect(mockNavigate).not.toHaveBeenCalled();

        // Now wait for another 60 seconds (inactivity)
        act(() => {
            jest.advanceTimersByTime(60000);
        });

        expect(mockNavigate).toHaveBeenCalledWith("/login");
    });

    test("should clean up event listeners on unmount", () => {
        const removeListenerSpy = jest.spyOn(document, "removeEventListener");

        const { unmount } = setup();
        unmount();

        expect(removeListenerSpy).toHaveBeenCalledWith("mousemove", expect.any(Function));
        expect(removeListenerSpy).toHaveBeenCalledWith("keypress", expect.any(Function));
        expect(removeListenerSpy).toHaveBeenCalledWith("click", expect.any(Function));
        expect(removeListenerSpy).toHaveBeenCalledWith("scroll", expect.any(Function));
    });



});
