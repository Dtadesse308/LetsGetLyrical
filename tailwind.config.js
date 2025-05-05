module.exports = {
    content: ["./src/**/*.{html,js,jsx}"],
    theme: {
        extend: {
            colors: {
                primary: '#4CAF50', // Green login button
            },
            borderRadius: {
                pill: '9999px', // For rounded buttons
            },
            fontFamily: {
                header: ['"Poppins"', 'sans-serif'], // if we add font
            },
        },
    },
    plugins: [],
};
