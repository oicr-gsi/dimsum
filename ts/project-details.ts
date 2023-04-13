import { caseDefinition } from "./data/case";
import { testDefinition } from "./data/test";
import {
  draftReportDefinition,
  finalReportDefinition,
  informaticsReviewDefinition,
} from "./data/requisition";
import {
  extractionDefinition,
  getFullDepthSequencingsDefinition,
  getLibraryQualificationsDefinition,
  libraryPreparationDefinition,
  receiptDefinition,
} from "./data/sample";
import { getProjectSummaryRowDefinition } from "./data/project-summary-row";
import { getSearchParams, updateUrlParams, urls } from "./util/urls";
import { TabBar } from "./component/tab-bar-builder";
import { Pair } from "./util/pair";
import { TableBuilder, TableDefinition } from "./component/table-builder";

const tableContainerId = "tableContainer";
const tableContainer = document.getElementById(tableContainerId); // use same table container across all tables
if (tableContainer === null) {
  throw Error(`Container ID "${tableContainerId}" not found on page`);
}

let projectSummaryTable: TableBuilder<any, any>;
if (tableContainer.dataset.detailValue) {
  projectSummaryTable = new TableBuilder(
    getProjectSummaryRowDefinition(
      urls.rest.projects.summary(tableContainer.dataset.detailValue)
    ),
    "projectSummaryTableContainer",
    getSearchParams()
  ).build();
}

// each pair consists of (1) the title of the table, and (2) its reload function upon selection
// (with its table definition taken as a param)
const tables = [
  new Pair("Cases", () => reload(caseDefinition)),
  new Pair("Tests", () => reload(testDefinition)),
  new Pair("Receipts", () => reload(receiptDefinition)),
  new Pair("Extractions", () => reload(extractionDefinition)),
  new Pair("Library Preparations", () => reload(libraryPreparationDefinition)),
  new Pair("Library Qualifications", () =>
    reload(
      getLibraryQualificationsDefinition(urls.rest.libraryQualifications, true)
    )
  ),
  new Pair("Full-Depth Sequencings", () =>
    reload(
      getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings, true)
    )
  ),
  new Pair("Informatics Reviews", () => reload(informaticsReviewDefinition)),
  new Pair("Draft Reports", () => reload(draftReportDefinition)),
  new Pair("Final Reports", () => reload(finalReportDefinition)),
];

// tabbed interface defaults to the cases table
const tableUrlQuery =
  getSearchParams().find((query) => query.key === "TABLE")?.value || "Cases";
const tabBar = new TabBar(tables, tableUrlQuery, "tabBarContainer");
tabBar.build();

// reload: destroy current table and build new table
function reload(definition: TableDefinition<any, any>) {
  if (tableContainer) tableContainer.innerHTML = "";
  new TableBuilder(
    definition,
    tableContainerId,
    getSearchParams(),
    handleFilters
  ).build();
}

function handleFilters(key: string, value: string, add?: boolean) {
  projectSummaryTable.applyFilters(key, value, add);
  updateUrlParams(key, value, add);
}
