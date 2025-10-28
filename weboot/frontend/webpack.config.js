const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require("webpack");
const ReactRefreshWebpackPlugin = require('@pmmmwh/react-refresh-webpack-plugin');
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

const isDevelopment = process.env.NODE_ENV !== 'production';


module.exports = {
  mode: isDevelopment ? 'development' : 'production',
  devtool: isDevelopment ? 'inline-source-map' : 'source-map',

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
            context: ['/ws1'],      // Match the WebSocket endpoint
            target: 'http://127.0.0.1:8080/',
            ws: true,               // Enable WebSocket proxying
            changeOrigin: true,     // Optional but often needed
          }
      ],
      historyApiFallback: true,
  }: {},

  entry: {
    index: './src/root.js',
  },

  resolve: {
      extensions: ['.js', '.jsx', '.ts', '.tsx'], // Add extensions your files might use
  },

  plugins: [isDevelopment && new ReactRefreshWebpackPlugin(),
    new MiniCssExtractPlugin({
      filename: "[name].css",
      chunkFilename: "[id].css",
    })
  ].filter(Boolean),

  output: {
    path: path.resolve(__dirname, 'dist'),
    clean: true,
    publicPath: '/',
      filename: 'js/[name].js',
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
     minimizer: [
        `...`,
        new CssMinimizerPlugin(),
     ]
  },

 module: {
    rules: [
      {
        test: /\.css$/i,
        use: [MiniCssExtractPlugin.loader, 'style-loader', 'css-loader'],
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
              plugins: [isDevelopment && require.resolve('react-refresh/babel')].filter(Boolean),
            },
          },
        ],
        type: 'javascript/auto',
    }
  ],
  },
};
