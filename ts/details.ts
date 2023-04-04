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
import { getSearchParams, updateUrlParams, urls } from "./util/urls";
import { TabBar } from "./component/tab-bar-builder";
import { Pair } from "./util/pair";
import { TableBuilder, TableDefinition } from "./component/table-builder";

const tableContainerId = "tableContainer";
const tableContainer = document.getElementById(tableContainerId); // use same table container across all tables
if (tableContainer === null) {
  throw Error(`Container ID "${tableContainerId}" not found on page`);
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
  new Pair("Full Depth Sequencings", () =>
    reload(
      getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings, true)
    )
  ),
  new Pair("Informatics Review", () => reload(informaticsReviewDefinition)),
  new Pair("Draft Reports", () => reload(draftReportDefinition)),
  new Pair("Final Reports", () => reload(finalReportDefinition)),
];

// tabbed interface defaults to the cases table
const tabBar = new TabBar(tables, "Cases", "tabBarContainer");
tabBar.build();

// reload: destroy current table and build new table
function reload(definition: TableDefinition<any, any>) {
  if (tableContainer) tableContainer.innerHTML = "";
  new TableBuilder(
    definition,
    tableContainerId,
    getSearchParams(),
    updateUrlParams
  ).build();
}

// add QC Report button if it's a case page
if (tableContainer.getAttribute("data-detail-type") === "CASE_ID") {
  const caseId = tableContainer.getAttribute("data-detail-value");
  if (!caseId) {
    throw new Error("Missing case ID value");
  }
  const actionContainer = document.getElementById("pageActionsContainer"); // use same table container across all tables
  if (actionContainer === null) {
    throw Error(`Container ID "${actionContainer}" not found on page`);
  }
  const button = document.createElement("button");
  button.classList.add(
    "bg-green-200",
    "rounded-md",
    "hover:ring-2",
    "ring-offset-1",
    "ring-green-200",
    "text-white",
    "font-inter",
    "font-medium",
    "text-12",
    "px-2",
    "py-1"
  );
  button.innerText = "QC Report";
  button.onclick = (event) =>
    (window.location.href = urls.dimsum.caseQcReport(caseId));
  actionContainer.appendChild(button);
}
