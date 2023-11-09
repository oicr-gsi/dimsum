const path = require("path");
const webpack = require("webpack");

module.exports = {
  mode: "production",
  entry: {
    index: "./ts/index.ts",
    details: "./ts/details.ts",
    projectDetails: "./ts/project-details.ts",
    notifications: "./ts/notifications.ts",
    runList: "./ts/run-list.ts",
    run: "./ts/run.ts",
    omissions: "./ts/omissions.ts",
    projectList: "./ts/project-list.ts",
    caseReport: "./ts/case-report.ts",
    error: "./ts/error.ts",
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
