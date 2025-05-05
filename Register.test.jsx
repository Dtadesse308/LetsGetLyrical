import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { useNavigate } from 'react-router-dom';
import Register from '../pages/Register';
import RegisterService from '../services/RegisterService';

jest.mock('react-router-dom', () => ({
    useNavigate: jest.fn(),
}));

jest.mock('../services/RegisterService', () => ({
    register: jest.fn(),
}));

describe('Register Test', () => {
    const mockNavigate = jest.fn();
    beforeEach(() => {
        useNavigate.mockReturnValue(mockNavigate);
        jest.clearAllMocks();
        sessionStorage.clear();
    });

    test('test that Register component renders', () => {
        render(<Register />);
        expect(screen.getByRole('heading', { name: /Register/i })).toBeInTheDocument();
        expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Register/i })).toBeInTheDocument();
    });

    test('test that users can enter username and password for register', () => {
        render(<Register />);
        const usernameInput = screen.getByLabelText(/username/i);
        const passwordInput = screen.getByLabelText(/^password$/i);
        fireEvent.change(usernameInput, { target: { value: 'tommyTrojan' } });
        fireEvent.change(passwordInput, { target: { value: 'myPassword1' } });
        expect(usernameInput.value).toBe('tommyTrojan');
        expect(passwordInput.value).toBe('myPassword1');
    });

    test('displays error when passwords do not match', async () => {
        render(<Register />);
        const usernameInput = screen.getByLabelText(/username/i);
        const passwordInput = screen.getByLabelText(/^password$/i);
        const password2Input = screen.getByPlaceholderText(/re-enter your password/i);
        fireEvent.change(usernameInput, { target: { value: 'testUser' } });
        fireEvent.change(passwordInput, { target: { value: 'Password1' } });
        fireEvent.change(password2Input, { target: { value: 'Password2' } });

        const form = document.querySelector('form');
        fireEvent.submit(form);

        await waitFor(() => {
            expect(screen.getByText("Passwords don't match.")).toBeInTheDocument();
        });
    });

    test('shows error if password does not meet requirements', async () => {
        render(<Register />);
        fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'testUser' } });
        fireEvent.change(screen.getByLabelText(/^password$/i), { target: { value: 'alllowercase' } });
        fireEvent.change(screen.getByPlaceholderText(/re-enter your password/i), { target: { value: 'alllowercase' } });

        const form = document.querySelector('form');
        fireEvent.submit(form);

        await waitFor(() => {
            expect(screen.getByText(/password must contain at least one uppercase/i)).toBeInTheDocument();
        });
    });


    test('displays error when registration fails', async () => {
        const fakeResponse = {
            ok: false,
            json: async () => ({}),
        };
        RegisterService.register.mockResolvedValueOnce(fakeResponse);

        render(<Register />);
        const usernameInput = screen.getByLabelText(/username/i);
        const passwordInput = screen.getByLabelText(/^password$/i);
        const password2Input = screen.getByPlaceholderText(/re-enter your password/i);
        fireEvent.change(usernameInput, { target: { value: 'testUser' } });
        fireEvent.change(passwordInput, { target: { value: 'myPassword2' } });
        fireEvent.change(password2Input, { target: { value: 'myPassword2' } });

        const form = document.querySelector('form');
        fireEvent.submit(form);

        await waitFor(() => {
            expect(screen.getByText("Username already exists.")).toBeInTheDocument();
        });
    });

    test('shows success popup and redirects after delay on successful registration', async () => {
        jest.useFakeTimers();

        const fakeResponse = {
            ok: true,
            json: async () => ({ id: '1', username: 'testUser' }),
        };
        RegisterService.register.mockResolvedValueOnce(fakeResponse);
        useNavigate.mockReturnValue(mockNavigate);

        render(<Register />);
        fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'testUser' } });
        fireEvent.change(screen.getByLabelText(/^password$/i), { target: { value: 'MyPassword1' } });
        fireEvent.change(screen.getByPlaceholderText(/re-enter your password/i), { target: { value: 'MyPassword1' } });

        fireEvent.submit(document.querySelector('form'));

        await waitFor(() => {
            expect(screen.getByText(/account created successfully/i)).toBeInTheDocument();
        });

        // Fast-forward the timer to trigger redirect
        jest.runAllTimers();

        expect(mockNavigate).toHaveBeenCalledWith('/login');
    });


    test('shows cancel confirmation popup when clicking cancel', () => {
        render(<Register />);
        const cancelBtn = screen.getByRole('button', { name: /^cancel$/i });
        fireEvent.click(cancelBtn);
        expect(screen.getByText(/are you sure you want to cancel/i)).toBeInTheDocument();
    });

    test('clicking "Confirm" in cancel popup navigates to login', () => {
        render(<Register />);
        useNavigate.mockReturnValue(mockNavigate);

        // Click outer Cancel button to show the popup
        const openPopupBtn = screen.getByRole('button', { name: /^cancel$/i });
        fireEvent.click(openPopupBtn);

        // Click Confirm button inside the popup using aria-label
        const confirmBtn = screen.getByLabelText('confirm cancel');
        fireEvent.click(confirmBtn);

        expect(mockNavigate).toHaveBeenCalledWith('/login');
    });


    test('clicking "Cancel" in cancel popup closes the popup', () => {
        render(<Register />);

        // Click outer Cancel button to show the popup
        const openPopupBtn = screen.getByRole('button', { name: /^cancel$/i });
        fireEvent.click(openPopupBtn);

        expect(screen.getByText(/are you sure you want to cancel/i)).toBeInTheDocument();

        // Click the cancel button inside the popup to dismiss
        const dismissBtn = screen.getByLabelText('dismiss cancel popup');
        fireEvent.click(dismissBtn);

        expect(screen.queryByText(/are you sure you want to cancel/i)).not.toBeInTheDocument();
    });


    test('sets document title to "Register" on mount', () => {
        render(<Register />);
        expect(document.title).toBe("Register");
    });
});