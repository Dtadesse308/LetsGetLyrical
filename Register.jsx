import RegisterService from "../services/RegisterService";
import InputField from "../components/InputField";
import Button from "../components/Button";
import {useEffect, useState} from "react";
import { useNavigate } from 'react-router-dom';
import React from "react";


function Register() {
    const navigate = useNavigate();

    const [userData, setUserData] = useState({username: "", password: "", password2: ""});
    const [errorMessage, setErrorMessage] = useState("");
    const [showCancelPopup, setShowCancelPopup] = useState(false);
    const [showSuccessPopup, setShowSuccessPopup] = useState(false);

    useEffect(() => {
        document.title = "Register";
    }, []);

    const handleInputChange = (e) => {
        const {name, value} = e.target;
        setUserData((prev) => ({...prev, [name]: value}));
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        if (userData.password !== userData.password2) {
            setErrorMessage("Passwords don't match.");
            return;
        }

        const password = userData.password;
        const validPassword = /[A-Z]/.test(password) && /[a-z]/.test(password) && /\d/.test(password);

        if (!validPassword) {
            setErrorMessage("Password must contain at least one uppercase letter, one lowercase letter, and one number.");
            return;
        }

        const response = await RegisterService.register(userData);
        if (!response.ok) {
            setErrorMessage("Username already exists.");
            return;
        }

        setShowSuccessPopup(true);
        setTimeout(() => {
            navigate("/login");
        }, 2000);
    };

    return (
        <div
            className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-[#fdfcfb] via-[#e2d1c3] to-[#eac5ff] px-6">
            <h1 className="text-5xl font-semibold text-center mb-8 tracking-widest text-transparent bg-clip-text bg-[linear-gradient(45deg,_#e573b7,_#7b68ee)] cursor-pointer">
                Let’s Get Lyrical!
            </h1>

            <h2
                className="text-xl font-semibold text-center mb-8 tracking-widest text-transparent bg-clip-text bg-[linear-gradient(45deg,_#e573b7,_#7b68ee)] cursor-pointer"
            >
                Team 22
            </h2>

            <form onSubmit={handleRegister} className="w-full max-w-md flex flex-col gap-6 mt-5">
                <h2 className="text-xl text-center font-medium">Register</h2>

                <InputField
                    label="Username"
                    type="text"
                    name="username"
                    onChange={handleInputChange}
                    placeholder="Enter your username"
                />
                <InputField
                    label="Password"
                    type="password"
                    name="password"
                    onChange={handleInputChange}
                    placeholder="Enter your password"
                />
                <InputField
                    label="Re-enter Password"
                    type="password"
                    name="password2"
                    onChange={handleInputChange}
                    placeholder="Re-enter your password"
                />

                <p id="error" className="text-red-500 text-sm text-center -mt-2">
                    {errorMessage}
                </p>

                <div className="flex justify-between mt-2 gap-4">
                    <Button type="submit" variant="primary" className="w-full py-3 text-base"
                            aria-label="Create new account"
                    >
                        Register
                    </Button>
                    <button
                        type="button"
                        onClick={() => setShowCancelPopup(true)}
                        className="w-full py-3 text-base font-semibold rounded-full text-slate-600 border-2 border-slate-500 hover:bg-slate-600 hover:text-white transition duration-200"
                        aria-label="cancel"
                    >
                        Cancel
                    </button>
                </div>

                <div className="text-center mt-6">
                    <p className="text-base font-normal mb-1">Already have an account?</p>
                    <a href="/login" className="text-2xl font-semibold underline">
                        Log In
                    </a>
                </div>
            </form>

            {showCancelPopup && (
                <div
                    className="absolute top-0 left-0 w-full h-full bg-black bg-opacity-50 flex items-center justify-center">
                    <div
                        className="bg-[#e2d1c3] text-[#2e1a47] p-6 rounded-2xl shadow-2xl border border-[#d6bfae] max-w-sm text-center">
                        <p className="text-lg font-semibold mb-6">Are you sure you want to cancel registration?</p>
                        <div className="flex justify-center gap-4">
                            <button
                                onClick={() => navigate("/login")}
                                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-full"
                                aria-label="confirm cancel"
                            >
                                Confirm
                            </button>
                            <button
                                onClick={() => setShowCancelPopup(false)}
                                className="border border-slate-500 px-4 py-2 rounded-full text-slate-600 hover:bg-slate-600 hover:text-white transition duration-200 "
                                aria-label="dismiss cancel popup"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
            {showSuccessPopup && (
                <div
                    className="absolute top-0 left-0 w-full h-full bg-black bg-opacity-50 flex items-center justify-center">
                    <div
                        className="bg-[#e2d1c3] text-[#2e1a47] p-6 rounded-2xl shadow-2xl border border-[#d6bfae] max-w-sm text-center">
                        <p className="text-lg font-medium">Account created successfully!</p>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Register;