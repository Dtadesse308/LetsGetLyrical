import LoginService from '../services/LoginService';

global.fetch = jest.fn();

describe("Test LoginService", () => {
    afterEach(() => {
        jest.clearAllMocks(); // Reset mock after each test
    });

    test("valid POST request to login", async () => {
       const mockResponse = {username:"tommytrojan", password: "myPassword"};

        // Mock fetch response
        fetch.mockResolvedValueOnce({
            ok: true,
            json: jest.fn().mockResolvedValue(mockResponse),
        });


        const loginData = { username: "tommytrojan", password: "myPassword" };

        // Call login function
        const response = await LoginService.login(loginData);
        const responseData = await response.json();

        // Verify fetch was called correctly
        expect(fetch).toHaveBeenCalledTimes(1);
        expect(fetch).toHaveBeenCalledWith("/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(loginData),
        });

        // Verify response data
        expect(responseData).toEqual(mockResponse);

    });

});