const path = require('path');

module.exports = {
  entry: './src/main/frontend/src',
  output: {
    path: path.resolve(__dirname, 'src/main/webapp/js'),
    filename: 'folder-auth-bundle.js',
  },
  // enable source maps for debugging webpack output
  devtool: 'source-map',
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        exclude: /node_modules/,
        use: {
          loader: 'ts-loader',
        },
      },
      {
        test: /\.js$/,
        enforce: 'pre',
        loader: 'source-map-loader',
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
      {
        test: /\.(png|jpe?g|gif|svg|eot|ttf|woff|woff2)$/i,
        loader: 'url-loader',
        options: {
          limit: 8192,
        },
      },
    ]
  },
  resolve: {
    extensions: ['.js', '.jsx','.ts', '.tsx'],
  }
};
