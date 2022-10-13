import { caseDefinition } from "./data/case";
import { TableBuilder } from "./component/table-builder";
import { getSearchParams, updateUrlParams } from "./util/urls";

new TableBuilder(
  caseDefinition,
  "casesTableContainer",
  getSearchParams(),
  () => updateUrlParams
).build();
