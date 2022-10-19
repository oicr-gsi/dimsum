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
import { TableBuilder } from "./component/table-builder";
import { getSearchParams, updateUrlParams, urls } from "./util/urls";
import { TabBar } from "./component/tab-bar-builder";

const tableContainerIds = [
  "casesTableContainer",
  "receiptsTableContainer",
  "extractionsTableContainer",
  "libraryPreparationsTableContainer",
  "libraryQualificationsTableContainer",
  "fullDepthSequencingsTableContainer",
  "informaticsReviewsTableContainer",
  "draftReportsTableContainer",
  "finalReportsTableContainer",
];
const tableTitles = [
  "Cases",
  "Receipts",
  "Extractions",
  "Library Preparations",
  "Library Qualifications",
  "Full Depth Sequencings",
  "Informatics Reviews",
  "Draft Reports",
  "Final Reports",
];

// tabbed interface defaults to the cases table
new TabBar(
  tableTitles,
  tableContainerIds,
  "casesTableContainer",
  "tabBarContainer"
).build();

new TableBuilder(
  caseDefinition,
  "casesTableContainer",
  getSearchParams(),
  updateUrlParams
).build();

new TableBuilder(receiptDefinition, "receiptsTableContainer").build();

new TableBuilder(extractionDefinition, "extractionsTableContainer").build();

new TableBuilder(
  libraryPreparationDefinition,
  "libraryPreparationsTableContainer"
).build();

new TableBuilder(
  getLibraryQualificationsDefinition(urls.rest.libraryQualifications, true),
  "libraryQualificationsTableContainer"
).build();

new TableBuilder(
  getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings, true),
  "fullDepthSequencingsTableContainer"
).build();

new TableBuilder(
  informaticsReviewDefinition,
  "informaticsReviewsTableContainer"
).build();

new TableBuilder(draftReportDefinition, "draftReportsTableContainer").build();

new TableBuilder(finalReportDefinition, "finalReportsTableContainer").build();
