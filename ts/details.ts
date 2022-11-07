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
import { urls } from "./util/urls";
import { TabBar } from "./component/tab-bar-builder";
import { Pair } from "./util/pair";

const tables = [
  new Pair(caseDefinition, "Cases"),
  new Pair(receiptDefinition, "Receipts"),
  new Pair(extractionDefinition, "Extractions"),
  new Pair(libraryPreparationDefinition, "Library Preparations"),
  new Pair(
    getLibraryQualificationsDefinition(urls.rest.libraryPreparations, true),
    "Library Qualifications"
  ),
  new Pair(
    getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings, true),
    "Full Depth Sequencings"
  ),
  new Pair(informaticsReviewDefinition, "Informatics Review"),
  new Pair(draftReportDefinition, "Draft Reports"),
  new Pair(finalReportDefinition, "Final Reports"),
];

// tabbed interface defaults to the cases table
new TabBar(
  tables,
  "Cases",
  "tabBarContainer",
  "tableContainer" // use the same container id across all tables (only one table will occupy it at a time)
).build();
