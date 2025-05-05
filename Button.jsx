import React from "react";

const variantStyles = {
    primary: "bg-primary text-white py-2 px-8 rounded-pill font-semibold hover:opacity-90 transition",
};

const Button = ({ children, onClick, type, className}) => {
    return (
        <button
            type={type}
            onClick={onClick}
            className={`bg-primary text-white py-2 px-8 rounded-pill font-semibold hover:opacity-90 transition ${className}`}
        >
            {children}
        </button>
    );
};

export default Button;
