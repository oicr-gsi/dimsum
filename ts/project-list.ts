import { TableBuilder } from "./component/table-builder";
import { projectDefinition } from "./data/project";
import { getSearchParams, updateUrlParams } from "./util/urls";

new TableBuilder(
  projectDefinition,
  "projectListTableContainer",
  getSearchParams(),
  updateUrlParams
).build();
