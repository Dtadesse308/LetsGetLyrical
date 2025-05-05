import RegisterService from '../services/RegisterService';

global.fetch = jest.fn();

describe("Test RegisterService", () => {
    afterEach(() => {
        jest.clearAllMocks(); // Reset mock after each test
    });
    test("test valid POST request to register ", async () => {
        const mockResponse = {username:"tommytrojan", password: "myPassword"};
        // Mock fetch response
        fetch.mockResolvedValueOnce({
            ok: true,
            json: jest.fn().mockResolvedValue(mockResponse),
        });

        const registerData = { username: "tommytrojan", password: "myPassword" };

        // Call login function
        const response = await RegisterService.register(registerData);
        const responseData = await response.json();

        // Verify fetch was called correctly
        expect(fetch).toHaveBeenCalledTimes(1);
        expect(fetch).toHaveBeenCalledWith("/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(registerData),
        });

        // Verify response data
        expect(responseData).toEqual(mockResponse);

    });

});