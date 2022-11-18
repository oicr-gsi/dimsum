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
import { TableBuilder, TableDefinition } from "./component/table-builder";

const tableContainerId = "tableContainer";

// each pair consists of (1) the title of the table, and (2) its reload function upon selection
// (with its table definition taken as a param)
const tables = [
  new Pair("Cases", () => reload(caseDefinition)),
  new Pair("Receipts", () => reload(receiptDefinition)),
  new Pair("Extractions", () => reload(extractionDefinition)),
  new Pair("Library Preparations", () => reload(libraryPreparationDefinition)),
  new Pair("Library Qualifications", () =>
    reload(
      getLibraryQualificationsDefinition(urls.rest.libraryPreparations, true)
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
const tabBar = new TabBar(
  tables,
  "Cases",
  "tabBarContainer",
  tableContainerId // use the same container id across all tables (only one table will occupy it at a time)
);

tabBar.build();

// reload: destroy current table and build new table
function reload(definition: TableDefinition<any, any>) {
  tabBar.elements.forEach((element, idx) => {
    var tab = tabBar.tabs[idx];
    if (element.key === tabBar.current && tab.selected) {
      // destroy current table and construct new table
      tabBar.tableContainer.innerHTML = "";
      new TableBuilder(
        definition,
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

// function reload() {
//   tabBar.tables.forEach((tbl, idx) => {
//     var tab = tabBar.tabs[idx];
//     if (tbl.key.value === tabBar.current && tab.selected) {
//       // destroy current table and construct new table
//       tabBar.tableContainer.innerHTML = "";
//       new TableBuilder(
//         tbl.key.key,
//         tableContainerId,
//         getSearchParams(),
//         updateUrlParams
//       ).build();
//     } else {
//       tab.selected = false;
//     }
//     tab.styleButton();
//   });
// }
