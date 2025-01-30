import {
  analysisReviewDefinition,
  caseDefinition,
  releaseApprovalDefinition,
  releaseDefinition,
} from "./data/case";
import { testDefinition } from "./data/test";
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
import {
  addLink,
  addTextDiv,
  getRequiredDataAttribute,
  getRequiredElementById,
} from "./util/html-utils";
import { Tooltip } from "./component/tooltip";
import { getOmittedRunSamplesDefinition } from "./data/omitted-run-sample";
import { internalUser } from "./util/site-config";

const tableContainerId = "tableContainer";
const tableContainer = getRequiredElementById(tableContainerId); // use same table container across all tables
const afterDatePicker = getRequiredElementById("afterDate") as HTMLInputElement;
const beforeDatePicker = getRequiredElementById(
  "beforeDate"
) as HTMLInputElement;
const afterDateKey = "AFTER_DATE";
const beforeDateKey = "BEFORE_DATE";

const projectHeading = getRequiredElementById("projectHeading");
const projectName = getRequiredDataAttribute(
  projectHeading,
  "data-project-name"
);
if (internalUser) {
  // Add Dashi links
  const dashiLink = getRequiredElementById("dashiProjectLink");
  const libraryDesignsString = dashiLink.getAttribute("data-library-designs");
  const libraryDesigns = libraryDesignsString
    ? libraryDesignsString.split(",")
    : [];
  makeDashiProjectLinksTooltip(dashiLink, projectName, libraryDesigns);
}

// creating dropdown for pre-defined date range filter
const today = new Date();

let omissionsHeading: HTMLElement | null = null;
let omissionsTableContainer: HTMLElement | null = null;

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
  new Pair("Library Qualifications", () => {
    reload(
      getLibraryQualificationsDefinition(urls.rest.libraryQualifications, true)
    );
    addOmissionsTable(
      urls.rest.projects.libraryQualificationOmissions(projectName)
    );
  }),
  new Pair("Full-Depth Sequencings", () => {
    reload(
      getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings, true)
    );
    addOmissionsTable(urls.rest.projects.fullDepthOmissions(projectName));
  }),
  new Pair("Analysis Reviews", () => reload(analysisReviewDefinition)),
  new Pair("Release Approvals", () => reload(releaseApprovalDefinition)),
  new Pair("Releases", () => reload(releaseDefinition)),
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

  omissionsHeading?.remove();
  omissionsTableContainer?.remove();
}

function handleTabbedTableFilters(key: string, value: string, add: boolean) {
  projectSummaryTable.applyFilter(key, value, add);
  updateUrlParams(key, value, add);
}

function addOmissionsTable(queryUrl: string) {
  omissionsHeading = document.createElement("h2");
  omissionsHeading.innerText = "Omissions";
  omissionsHeading.className =
    "text-green-200 font-sarabun font-light font text-24 mt-8";
  omissionsTableContainer = document.createElement("div");
  omissionsTableContainer.id = "omissionsTableContainer";

  tableContainer.after(omissionsHeading, omissionsTableContainer);
  new TableBuilder(
    getOmittedRunSamplesDefinition(queryUrl, true),
    omissionsTableContainer.id
  ).build();
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

function makeDashiProjectLinksTooltip(
  element: HTMLElement,
  projectName: string,
  libraryDesigns: string[]
) {
  const tooltipInstance = Tooltip.getInstance();
  tooltipInstance.addTarget(element, (fragment) => {
    let linksAdded = false;
    if (libraryDesigns.includes("TS") || libraryDesigns.includes("EX")) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Call Ready Targeted Sequencing",
        urls.dashi.project.callReadyTar(projectName)
      );
    }
    if (libraryDesigns.includes("WT")) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Call Ready RNA-seq",
        urls.dashi.project.callReadyRna(projectName)
      );
    }
    if (
      libraryDesigns.includes("WG") ||
      libraryDesigns.includes("SW") ||
      libraryDesigns.includes("PG")
    ) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Call Ready WGS",
        urls.dashi.project.callReadyWgs(projectName)
      );
    }
    if (libraryDesigns.includes("TS") || libraryDesigns.includes("EX")) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Single-Lane Targeted Sequencing",
        urls.dashi.project.singleLaneTar(projectName)
      );
    }
    if (libraryDesigns.includes("WT")) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Single-Lane RNA-seq",
        urls.dashi.project.singleLaneRna(projectName)
      );
    }
    if (
      libraryDesigns.includes("WG") ||
      libraryDesigns.includes("SW") ||
      libraryDesigns.includes("PG")
    ) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Single-Lane WGS",
        urls.dashi.project.singleLaneWgs(projectName)
      );
    }
    if (libraryDesigns.includes("CM")) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Single-Lane cfMeDIP",
        urls.dashi.project.singleLaneCfMeDip(projectName)
      );
    }
    if (!linksAdded) {
      addTextDiv("No Dashi-compatible libraries", fragment);
    }
  });
}

function addLinkDiv(container: Node, text: string, url: string) {
  const div = document.createElement("div");
  addLink(div, text, url, true);
  container.appendChild(div);
}
