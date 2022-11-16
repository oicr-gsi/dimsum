import { caseDefinition } from "./data/case";
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
import { TableBuilder } from "./component/table-builder";

const tableContainerId = "tableContainer";

// each pair consists of (1) the information required to build the table (definition
// and title), and (2) its reload function upon selection
const tables = [
  new Pair(new Pair(caseDefinition, "Cases"), reload),
  new Pair(new Pair(receiptDefinition, "Receipts"), reload),
  new Pair(new Pair(extractionDefinition, "Extractions"), reload),
  new Pair(
    new Pair(libraryPreparationDefinition, "Library Preparations"),
    reload
  ),
  new Pair(
    new Pair(
      getLibraryQualificationsDefinition(urls.rest.libraryPreparations, true),
      "Library Qualifications"
    ),
    reload
  ),
  new Pair(
    new Pair(
      getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings, true),
      "Full Depth Sequencings"
    ),
    reload
  ),
  new Pair(new Pair(informaticsReviewDefinition, "Informatics Review"), reload),
  new Pair(new Pair(draftReportDefinition, "Draft Reports"), reload),
  new Pair(new Pair(finalReportDefinition, "Final Reports"), reload),
];

// tabbed interface defaults to the cases table
const tabBar = new TabBar(
  tables,
  "Cases",
  "tabBarContainer",
  tableContainerId // use the same container id across all tables (only one table will occupy it at a time)
);

tabBar.build();

// reload
function reload() {
  tabBar.tables.forEach((tbl, idx) => {
    var tab = tabBar.tabs[idx];
    if (tbl.key.value === tabBar.current && tab.selected) {
      // destroy current table and construct new table
      tabBar.tableContainer.innerHTML = "";
      new TableBuilder(
        tbl.key.key,
        tableContainerId,
        getSearchParams(),
        updateUrlParams
      ).build();
    } else {
      tab.selected = false;
    }
    tab.styleButton();
  });
}
