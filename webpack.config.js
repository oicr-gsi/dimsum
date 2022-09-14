const path = require("path");
const webpack = require("webpack");

module.exports = {
  mode: "production",
  entry: {
    index: "./ts/index.ts",
    details: "./ts/details.ts",
    notifications: "./ts/notifications.ts",
    run: "./ts/run.ts",
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: "ts-loader",
        exclude: /node_modules/,
      },
    ],
  },
  resolve: {
    extensions: [".tsx", ".ts", ".js"],
  },
  output: {
    filename: "[name].js",
    path: path.resolve(__dirname, "target/classes/static/js/"),
    library: {
      name: "[name]",
      type: "var",
    },
    clean: true,
  },
  devtool: "eval-source-map",
};
