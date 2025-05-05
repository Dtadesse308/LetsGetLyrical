import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { useNavigate } from 'react-router-dom';
import Login from '../pages/Login';
import LoginService from '../services/LoginService';
import React from "react";

jest.mock('react-router-dom', () => ({
    useNavigate: jest.fn(),
}));

jest.mock('../services/LoginService', () => ({
    login: jest.fn(),
}));

describe('Login Test', () => {
    const mockNavigate = jest.fn();
    beforeEach(() => {
        useNavigate.mockReturnValue(mockNavigate);
        jest.clearAllMocks();
        sessionStorage.clear();
    });

    test('test that Login component renders', () => {
        render(<Login />);
        expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /log in/i })).toBeInTheDocument();
    });

    test('test that users can enter username and password', () => {
        render(<Login />);
        const usernameInput = screen.getByLabelText(/username/i);
        const passwordInput = screen.getByLabelText(/password/i);
        fireEvent.change(usernameInput, { target: { value: 'testUser' } });
        fireEvent.change(passwordInput, { target: { value: 'testPassword' } });
        expect(usernameInput.value).toBe('testUser');
        expect(passwordInput.value).toBe('testPassword');
    });

    test('test successful login flow', async () => {
        const fakeResponse = {
            ok: true,
            json: async () => ({ id: '123', username: 'testUser' }),
        };
        LoginService.login.mockResolvedValueOnce(fakeResponse);

        render(<Login />);

        const usernameInput = screen.getByLabelText(/username/i);
        const passwordInput = screen.getByLabelText(/password/i);
        fireEvent.change(usernameInput, { target: { value: 'testUser' } });
        fireEvent.change(passwordInput, { target: { value: 'testPassword' } });

        const loginButton = screen.getByRole('button', { name: /log in/i });
        fireEvent.click(loginButton);

        await waitFor(() => {
            expect(LoginService.login).toHaveBeenCalled();
            expect(sessionStorage.getItem('id')).toBe('123');
            expect(sessionStorage.getItem('username')).toBe('testUser');
            expect(mockNavigate).toHaveBeenCalledWith('/search');
        });
    });

    test('test error message display with message property', async () => {
        const fakeResponse = {
            ok: false,
            json: async () => ({ message: 'Invalid credentials' }),
        };
        LoginService.login.mockResolvedValueOnce(fakeResponse);

        render(<Login />);

        fireEvent.click(screen.getByRole('button', { name: /log in/i }));

        await waitFor(() => {
            expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
        });
    });

    test('test error message display with error_description property', async () => {
        const fakeResponse = {
            ok: false,
            json: async () => ({ error_description: 'Account is locked' }),
        };
        LoginService.login.mockResolvedValueOnce(fakeResponse);

        render(<Login />);

        fireEvent.click(screen.getByRole('button', { name: /log in/i }));

        await waitFor(() => {
            expect(screen.getByText('Account is locked')).toBeInTheDocument();
        });
    });

    test('test generic error message when no specific message provided', async () => {
        const fakeResponse = {
            ok: false,
            json: async () => ({}),
        };
        LoginService.login.mockResolvedValueOnce(fakeResponse);

        render(<Login />);

        fireEvent.click(screen.getByRole('button', { name: /log in/i }));

        await waitFor(() => {
            expect(screen.getByText('Login failed. Please check your credentials.')).toBeInTheDocument();
        });
    });

    test('test error handling for network failures', async () => {
        LoginService.login.mockRejectedValueOnce(new Error('Network error'));

        render(<Login />);

        fireEvent.click(screen.getByRole('button', { name: /log in/i }));

        await waitFor(() => {
            expect(screen.getByText('Something went wrong. Please try again later.')).toBeInTheDocument();
        });
    });

    test('test document title and redirect if already logged in', () => {
        sessionStorage.setItem('id', '123');
        render(<Login />);

        expect(document.title).toBe('Login');
        expect(mockNavigate).toHaveBeenCalledWith('/search', { replace: true });
    });
});