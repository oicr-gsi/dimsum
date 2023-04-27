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
import {
  getSearchParams,
  updateUrlParams,
  replaceUrlParams,
  urls,
} from "./util/urls";
import { TabBar } from "./component/tab-bar-builder";
import { Pair } from "./util/pair";
import { TableBuilder, TableDefinition } from "./component/table-builder";
import { Dropdown, BasicDropdownOption } from "./component/dropdown";
import { makeIcon } from "./util/html-utils";

const tableContainerId = "tableContainer";
const tableContainer = document.getElementById(tableContainerId); // use same table container across all tables
if (tableContainer === null) {
  throw Error(`Container ID "${tableContainerId}" not found on page`);
}
const projectSummaryContainerId = "projectSummaryContainer";
const projectSummaryContainer = document.getElementById(
  projectSummaryContainerId
);
if (projectSummaryContainer === null) {
  throw Error(`Container ID "${projectSummaryContainerId}" not found on page`);
}

// creating dropdown for pre-defined date range filter
const today = new Date();

const dateOptions = [
  new BasicDropdownOption("Today", () => {
    handleDateDropdownFilter("AFTER_DATE", dateToString(today));
    handleDateDropdownFilter("BEFORE_DATE", dateToString(today));
  }),
  new BasicDropdownOption("Yesterday", () => {
    const date = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate() - 1
    );
    handleDateDropdownFilter("AFTER_DATE", dateToString(date));
    handleDateDropdownFilter("BEFORE_DATE", dateToString(date));
  }),
  new BasicDropdownOption("This Week", () => {
    const afterdate = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate() - 6
    );
    handleDateDropdownFilter("AFTER_DATE", dateToString(afterdate));
    handleDateDropdownFilter("BEFORE_DATE", dateToString(today));
  }),
  new BasicDropdownOption("Last Week", () => {
    const afterdate = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate() - 13
    );
    const beforedate = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate() - 7
    );
    handleDateDropdownFilter("AFTER_DATE", dateToString(afterdate));
    handleDateDropdownFilter("BEFORE_DATE", dateToString(beforedate));
  }),
  new BasicDropdownOption("This Month", () => {
    // first date of Month
    const afterdate = new Date(today.getFullYear(), today.getMonth(), 1);
    handleDateDropdownFilter("AFTER_DATE", dateToString(afterdate));
    handleDateDropdownFilter("BEFORE_DATE", dateToString(today));
  }),
  new BasicDropdownOption("Last Month", () => {
    const afterdate = new Date(today.getFullYear(), today.getMonth() - 1, 1);
    const beforedate = new Date(today.getFullYear(), today.getMonth(), 0);
    handleDateDropdownFilter("AFTER_DATE", dateToString(afterdate));
    handleDateDropdownFilter("BEFORE_DATE", dateToString(beforedate));
  }),
];

const dateDropdown = new Dropdown(dateOptions, false, undefined, "Date Range");

// Attach form submit event handler to each form element
document
  .getElementById("afterDateForm")
  ?.addEventListener("change", function () {
    const inputElement = document.getElementById(
      "afterDate"
    ) as HTMLInputElement;
    if (inputElement !== null) {
      handleDateFilter("AFTER_DATE", inputElement.value);
    }
  });

document
  .getElementById("beforeDateForm")
  ?.addEventListener("change", function () {
    const inputElement = document.getElementById(
      "beforeDate"
    ) as HTMLInputElement;
    if (inputElement !== null) {
      handleDateFilter("BEFORE_DATE", inputElement.value);
    }
  });

// create a top control container
const topControlsContainer = document.createElement("div");
topControlsContainer.className = "flex items-top space-x-2";

// create a data range container
const dateRangeContainer = document.createElement("div");
dateRangeContainer.append(dateDropdown.getContainerTag());
dateRangeContainer.classList.add("flex-auto", "items-center", "space-x-2");
topControlsContainer.appendChild(dateRangeContainer);

// append the project summary filter to the top project summary table's top control
document
  .getElementById("projectSummaryTableFilter")
  ?.appendChild(topControlsContainer);

let projectSummaryTable: TableBuilder<any, any>;
if (tableContainer.dataset.detailValue) {
  projectSummaryTable = new TableBuilder(
    getProjectSummaryRowDefinition(
      urls.rest.projects.summary(tableContainer.dataset.detailValue)
    ),
    "projectSummaryContainer",
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

// reloadSummary: destroy current table and build new project summary table
function reloadSummary() {
  projectSummaryContainer?.firstChild?.remove();
  if (tableContainer?.dataset.detailValue) {
    projectSummaryTable = new TableBuilder(
      getProjectSummaryRowDefinition(
        urls.rest.projects.summary(tableContainer.dataset.detailValue)
      ),
      "projectSummaryContainer",
      getSearchParams()
    ).build();
  }
}

function handleDateFilter(filterKey: string, filterValue: string) {
  // update URL to include query parameters
  replaceUrlParams(filterKey, filterValue);
  reloadSummary();
}

function handleDateDropdownFilter(filterKey: string, filterValue: string) {
  // update URL to include query parameters
  replaceUrlParams(filterKey, filterValue);
  if (filterKey == "BEFORE_DATE") {
    const beforeDateCOntainer = document.getElementById(
      "afterDate"
    ) as HTMLInputElement;
    beforeDateCOntainer.value = filterValue;
  } else {
    const afterDateContainer = document.getElementById(
      "beforeDate"
    ) as HTMLInputElement;
    afterDateContainer.value = filterValue;
  }
  reloadSummary();
}

function dateToString(date: Date) {
  return (
    date.getFullYear() +
    "-" +
    ("0" + (date.getMonth() + 1)).slice(-2) +
    "-" +
    ("0" + date.getDate()).slice(-2)
  );
}
