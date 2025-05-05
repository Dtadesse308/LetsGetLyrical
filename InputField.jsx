import React from "react";
import PropTypes from "prop-types";

function InputField({ label, type, name, onChange, placeholder }) {
    return (
        <div className="flex flex-col">
            <label
                className="mb-1 text-sm font-medium text-gray-800"
                htmlFor={name}
            >
                {label}
            </label>
            <input
                type={type}
                id={name}
                name={name}
                onChange={onChange}
                className="w-full px-4 py-2 border border-gray-400 rounded-md bg-transparent placeholder-gray-500 text-gray-900 focus:outline-none focus:border-gray-700 transition"
                placeholder={placeholder}
                required
            />
        </div>
    );
}

InputField.propTypes = {
    label: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    placeholder: PropTypes.string,
};

export default InputField;
