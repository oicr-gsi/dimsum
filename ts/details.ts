import { caseDefinition } from "./data/case";
import { testDefinition } from "./data/test";
import {
  releaseApprovalDefinition,
  releaseDefinition,
  analysisReviewDefinition,
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
import { makeCopyButton } from "./util/html-utils";

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
  new Pair("Analysis Reviews", () => reload(analysisReviewDefinition)),
  new Pair("Release Approvals", () => reload(releaseApprovalDefinition)),
  new Pair("Releases", () => reload(releaseDefinition)),
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

switch (tableContainer.getAttribute("data-detail-type")) {
  case "REQUISITION_ID": {
    const titleMisoLink = document.getElementById("titleMisoLink");
    if (titleMisoLink === null) {
      throw Error("title link not found on page");
    }
    const requisitionName = tableContainer.getAttribute("data-detail-name");
    if (requisitionName === null) {
      throw Error("requisition name data attribute missing");
    }
    const copyButton = makeCopyButton(requisitionName);
    titleMisoLink.parentNode?.insertBefore(copyButton, titleMisoLink);
  }
  case "CASE_ID": {
    // add QC Report button
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
}
