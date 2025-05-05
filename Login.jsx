import LoginService from "../services/LoginService";
import InputField from "../components/InputField";
import Button from "../components/Button";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import React from "react";

function Login() {
    const navigate = useNavigate();

    const [userData, setUserData] = useState({ username: "", password: "" });
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        document.title = "Login";
        const id = sessionStorage.getItem("id");
        if (id) {
            navigate("/search", { replace: true });
        }
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setUserData((prev) => ({ ...prev, [name]: value }));
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await LoginService.login(userData);

            if (!response.ok) {
                const data = await response.json();

                if (data.message) {
                    setErrorMessage(data.message);
                } else if (data.error_description) {
                    setErrorMessage(data.error_description);
                } else {
                    setErrorMessage("Login failed. Please check your credentials.");
                }
                return;
            }

            const data = await response.json();
            const { id, username } = data;
            sessionStorage.setItem("id", id);
            sessionStorage.setItem("username", username);
            console.log(id, username);
            navigate("/search");

        } catch (error) {
            setErrorMessage("Something went wrong. Please try again later.");
            console.error("Login error:", error);
        }
    };

    return (
        <div
            className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-[#fdfcfb] via-[#e2d1c3] to-[#eac5ff] px-6">
            <h1
                className="text-5xl font-semibold text-center mb-8 tracking-widest text-transparent bg-clip-text bg-[linear-gradient(45deg,_#e573b7,_#7b68ee)] cursor-pointer"
            >
                Let’s Get Lyrical!
            </h1>
            <h2
                className="text-xl font-semibold text-center mb-8 tracking-widest text-transparent bg-clip-text bg-[linear-gradient(45deg,_#e573b7,_#7b68ee)] cursor-pointer"
            >
                Team 22
            </h2>

            <form onSubmit={handleLogin} className="w-full max-w-md flex flex-col gap-6 mt-5" aria-labelledby="form-title">
                <h2 className="text-xl text-center font-medium">Log In</h2>

                <InputField
                    label="Username"
                    type="text"
                    name="username"
                    onChange={handleInputChange}
                    placeholder="Enter your username"
                    aria-required="true"
                />
                <InputField
                    label="Password"
                    type="password"
                    name="password"
                    onChange={handleInputChange}
                    placeholder="Enter your password"
                    aria-required="true"
                />

                {errorMessage && (
                    <p id="error" className="text-red-500 text-sm text-center -mt-2" aria-live="assertive">
                        {errorMessage}
                    </p>
                )}

                <div className="flex justify-center mt-2">
                    <Button type="submit" variant="primary" className="w-full max-w-xs py-3 text-base" aria-label="Log in">
                        Log In
                    </Button>
                </div>

                <div className="text-center mt-6">
                    <p className="text-base font-normal mb-1">New User?</p>
                    <a href="/register" className="text-2xl font-semibold underline" aria-label="Create a new account">
                        Create an Account
                    </a>
                </div>
            </form>
        </div>
    );
}

export default Login;
