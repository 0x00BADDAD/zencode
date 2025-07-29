const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require("webpack");
const ReactRefreshWebpackPlugin = require('@pmmmwh/react-refresh-webpack-plugin');

const isDevelopment = process.env.NODE_ENV !== 'production';


module.exports = {
  mode: isDevelopment ? 'development' : 'production',
  devtool: 'inline-source-map',

  devServer: {
      static: './dist',
      hot: true,
      port: 3000,
      proxy: [
          {
            context: ['/api'],
            target: 'http://localhost:8080',
          },
      ],
      historyApiFallback: true,
  },

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
              plugins: [isDevelopment && require.resolve('react-refresh/babel')].filter(Boolean),
            },
          },
        ],
        type: 'javascript/auto',
    }
  ],
  },
};
