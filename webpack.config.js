const path = require('path');
const webpack = require('webpack');

module.exports = {
  mode: 'production',
  entry: {
    common: './ts/common.ts',
    index: './ts/index.ts'
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
  },
  output: {
    filename: '[name].js',
    path: path.resolve(__dirname, 'target/classes/static/js/'),
    library: {
      name: '[name]',
      type: 'var'
    },
    clean: true
  },
  plugins: [
    new webpack.ProvidePlugin({
      $: 'jquery/src/jquery',
      jQuery: 'jquery/src/jquery'
    })
  ]
};