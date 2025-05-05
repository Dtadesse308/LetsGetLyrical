const API_BASE_URL = "/login";

const LoginService = {

    async login(data) {
        return await fetch(API_BASE_URL, {
            method: "POST", headers: {
                "Content-Type": "application/json",
            }, body: JSON.stringify(data),
        });
    },
};

export default LoginService;