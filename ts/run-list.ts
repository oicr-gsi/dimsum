import { TableBuilder } from "./component/table-builder";
import { runDefinition } from "./data/run";
import { getSearchParams, updateUrlParams } from "./util/urls";

new TableBuilder(
  runDefinition,
  "runListTableContainer",
  getSearchParams(),
  updateUrlParams
).build();
