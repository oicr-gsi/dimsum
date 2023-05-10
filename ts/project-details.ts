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

const tableContainerId = "tableContainer";
const tableContainer = getRequiredElementById(tableContainerId); // use same table container across all tables
const afterDatePicker = getRequiredElementById("afterDate") as HTMLInputElement;
const beforeDatePicker = getRequiredElementById(
  "beforeDate"
) as HTMLInputElement;
const afterDateKey = "AFTER_DATE";
const beforeDateKey = "BEFORE_DATE";

function getRequiredElementById(id: string): HTMLElement {
  const element = document.getElementById(id);
  if (!element) {
    throw Error(`Required element ${id} not found`);
  }
  return element;
}

// creating dropdown for pre-defined date range filter
const today = new Date();

const dateOptions = [
  new BasicDropdownOption("Any Time", () => {
    // delete all query parameter with date filter key
    getSearchParams().forEach((x) => {
      if (x.key === afterDateKey || x.key === beforeDateKey) {
        updateUrlParams(x.key, x.value, false);
      }
      afterDatePicker.value = "";
      beforeDatePicker.value = "";
    });
    projectSummaryTable.replaceFilters([
      new Pair(afterDateKey, ""),
      new Pair(beforeDateKey, ""),
    ]);
  }),
  new BasicDropdownOption("Today", () => {
    handleDateDropdownFilter(dateToString(today), dateToString(today));
  }),
  new BasicDropdownOption("Yesterday", () => {
    const date = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate() - 1
    );
    handleDateDropdownFilter(dateToString(date), dateToString(date));
  }),
  new BasicDropdownOption("This Week", () => {
    const afterdate = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate() - 6
    );
    handleDateDropdownFilter(dateToString(afterdate), dateToString(today));
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
    handleDateDropdownFilter(dateToString(afterdate), dateToString(beforedate));
  }),
  new BasicDropdownOption("This Month", () => {
    // first date of Month
    const afterdate = new Date(today.getFullYear(), today.getMonth(), 1);
    handleDateDropdownFilter(dateToString(afterdate), dateToString(today));
  }),
  new BasicDropdownOption("Last Month", () => {
    const afterdate = new Date(today.getFullYear(), today.getMonth() - 1, 1);
    const beforedate = new Date(today.getFullYear(), today.getMonth(), 0);
    handleDateDropdownFilter(dateToString(afterdate), dateToString(beforedate));
  }),
];

const dateDropdown = new Dropdown(dateOptions, false, undefined, "Date Range");
// set values from URL params in date pickers
getSearchParams().forEach((x) => {
  if (x.key === afterDateKey) {
    afterDatePicker.value = x.value;
  } else if (x.key === beforeDateKey) {
    beforeDatePicker.value = x.value;
  }
});

// Attach event handlers to date pickers
afterDatePicker.addEventListener("change", function () {
  handleDateFilter(afterDateKey, afterDatePicker.value);
});

beforeDatePicker.addEventListener("change", function () {
  handleDateFilter(beforeDateKey, beforeDatePicker.value);
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
getRequiredElementById("projectSummaryDateControls").appendChild(
  topControlsContainer
);

if (!tableContainer.dataset.detailValue) {
  throw Error("Summary table missing data: detail value");
}
const projectSummaryTable = new TableBuilder(
  getProjectSummaryRowDefinition(
    urls.rest.projects.summary(tableContainer.dataset.detailValue)
  ),
  "projectSummaryContainer",
  getSearchParams()
).build();

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
    handleTabbedTableFilters
  ).build();
}
function handleTabbedTableFilters(key: string, value: string, add: boolean) {
  projectSummaryTable.applyFilter(key, value, add);
  updateUrlParams(key, value, add);
}

function handleDateFilter(filterKey: string, filterValue: string) {
  // update URL to include query parameters
  replaceUrlParams(filterKey, filterValue);
  projectSummaryTable.replaceFilters([new Pair(filterKey, filterValue)]);
}

function handleDateDropdownFilter(afterDate: string, beforeDate: string) {
  afterDatePicker.value = afterDate;
  beforeDatePicker.value = beforeDate;
  replaceUrlParams(afterDateKey, afterDate);
  replaceUrlParams(beforeDateKey, beforeDate);
  projectSummaryTable.replaceFilters([
    new Pair(afterDateKey, afterDate),
    new Pair(beforeDateKey, beforeDate),
  ]);
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
