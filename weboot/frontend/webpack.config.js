const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require("webpack");
const ReactRefreshWebpackPlugin = require('@pmmmwh/react-refresh-webpack-plugin');

const isDevelopment = process.env.NODE_ENV === 'development';


module.exports = {
  devtool: isDevelopment ? 'inline-source-map' : 'source-map',
  stats: 'errors-only',
  devServer: isDevelopment ? {
      static: './dist',
      hot: true,
      host: '127.0.0.1',
      port: 3000,
      proxy: [
          {
            context: ['/api'],
            target: 'http://127.0.0.1:8080/',
          },
          {
            context: ['/ws1'], // Match the WebSocket endpoint
            target: 'http://127.0.0.1:8080/',
            ws: true,               // Enable WebSocket proxying
            changeOrigin: true,     // Optional but often needed
          }
      ],
      historyApiFallback: true,
  }: {},

  entry: {
    index: './src/root.js',
    //print: './src/print.js',
  },

    resolve: {
        extensions: ['.js', '.jsx', '.ts', '.tsx'], // Add extensions your files might use
    },

  plugins: [isDevelopment && new ReactRefreshWebpackPlugin()].filter(Boolean),
  output: {
    path: path.resolve(__dirname, 'dist'),
    clean: true,
    publicPath: '/',
      filename: isDevelopment ? 'js/[name].js' : 'js/[name].[contenthash].js',
  },

 optimization: {
   moduleIds: 'deterministic',
   runtimeChunk: 'single',
   splitChunks: {
    cacheGroups: {
      vendor: {
        test: /[\\/]node_modules[\\/]/,
        name: 'vendors',
        chunks: 'all',
      },
    },
  },
  },

 module: {
    rules: [
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
     {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: 'asset/resource',
      },
    {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: 'asset/resource',
    },
    {
        test: /\.[jt]sx?$/,
        exclude: /node_modules/,
        use: [
          {
            loader: require.resolve('babel-loader'),
            options: {
              cacheDirectory: true,
              configFile: path.join(__dirname, './babel.config.js'),
              plugins: [isDevelopment && [require.resolve('react-refresh/babel'), { skipEnvCheck: true }]].filter(Boolean),
            },
          },
        ],
        type: 'javascript/auto',
    }
  ],
  },
};
