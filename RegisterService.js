const API_BASE_URL = "/register";

const RegisterService = {

    async register(data) {
        return await fetch(API_BASE_URL, {
            method: "POST", headers: {
                "Content-Type": "application/json",
            }, body: JSON.stringify(data),
        });
    },
};

export default RegisterService;