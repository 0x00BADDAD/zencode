module.exports = {
  presets: [
    '@babel/preset-env', // For modern JavaScript features
    ['@babel/preset-react', { runtime: 'automatic' }] // For React JSX
  ],
  plugins: [
    // ... other plugins ...

    // Add react-refresh/babel plugin *only in development*
    // This is crucial for Fast Refresh to work correctly.
    // Ensure it's before any other transform that would alter JSX.
    process.env.NODE_ENV === 'development' && require('react-refresh/babel')
  ].filter(Boolean) // .filter(Boolean) removes 'false' entries for production
};
