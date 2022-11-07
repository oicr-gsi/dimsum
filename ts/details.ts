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
import { Pair } from "./util/pair";

class TableInformation<ParentType> {
  definition: ParentType;
  containerId: string;
  title: string;
  constructor(definition: ParentType, containerId: string, title: string) {
    this.definition = definition;
    this.containerId = containerId;
    this.title = title;
  }
}

const tables = [
  new TableInformation(caseDefinition, "casesTableContainer", "Cases"),
  new TableInformation(receiptDefinition, "receiptsTableContainer", "Receipts"),
];

// array of table builders we will use to destroy and create new tables
const tableBuilders = [
  new Pair(
    new TableBuilder(
      caseDefinition,
      "casesTableContainer",
      getSearchParams(),
      updateUrlParams
    ),
    "Cases"
  ),

  new Pair(
    new TableBuilder(
      receiptDefinition,
      "receiptsTableContainer",
      getSearchParams(),
      updateUrlParams
    ),
    "Receipts"
  ),

  new Pair(
    new TableBuilder(
      extractionDefinition,
      "extractionsTableContainer",
      getSearchParams(),
      updateUrlParams
    ),
    "Extractions"
  ),

  new Pair(
    new TableBuilder(
      libraryPreparationDefinition,
      "libraryPreparationsTableContainer",
      getSearchParams(),
      updateUrlParams
    ),
    "Library Preparations"
  ),

  new Pair(
    new TableBuilder(
      getLibraryQualificationsDefinition(urls.rest.libraryQualifications, true),
      "libraryQualificationsTableContainer",
      getSearchParams(),
      updateUrlParams
    ),
    "Library Qualifications"
  ),

  new Pair(
    new TableBuilder(
      getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings, true),
      "fullDepthSequencingsTableContainer",
      getSearchParams(),
      updateUrlParams
    ),
    "Full Depth Sequencings"
  ),

  new Pair(
    new TableBuilder(
      informaticsReviewDefinition,
      "informaticsReviewsTableContainer",
      getSearchParams(),
      updateUrlParams
    ),
    "Informatics Review"
  ),

  new Pair(
    new TableBuilder(
      draftReportDefinition,
      "draftReportsTableContainer",
      getSearchParams(),
      updateUrlParams
    ),
    "Draft Reports"
  ),

  new Pair(
    new TableBuilder(
      finalReportDefinition,
      "finalReportsTableContainer",
      getSearchParams(),
      updateUrlParams
    ),
    "Final Reports"
  ),
];

// tabbed interface defaults to the cases table
new TabBar(tableBuilders, "Cases", "tabBarContainer").build();

/*
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
import { Pair } from "./util/pair";

// array of table builders we will use to destroy and create new tables
const tableBuilders = [
  new TableBuilder(
    caseDefinition,
    "casesTableContainer",
    getSearchParams(),
    updateUrlParams
  ),
  new TableBuilder(
    receiptDefinition,
    "receiptsTableContainer",
    getSearchParams(),
    updateUrlParams
  ),
  new TableBuilder(
    extractionDefinition,
    "extractionsTableContainer",
    getSearchParams(),
    updateUrlParams
  ),
  new TableBuilder(
    libraryPreparationDefinition,
    "libraryPreparationsTableContainer",
    getSearchParams(),
    updateUrlParams
  ),
  new TableBuilder(
    getLibraryQualificationsDefinition(urls.rest.libraryQualifications, true),
    "libraryQualificationsTableContainer",
    getSearchParams(),
    updateUrlParams
  ),
  new TableBuilder(
    getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings, true),
    "fullDepthSequencingsTableContainer",
    getSearchParams(),
    updateUrlParams
  ),
  new TableBuilder(
    informaticsReviewDefinition,
    "informaticsReviewsTableContainer",
    getSearchParams(),
    updateUrlParams
  ),
  new TableBuilder(
    draftReportDefinition,
    "draftReportsTableContainer",
    getSearchParams(),
    updateUrlParams
  ),
  new TableBuilder(
    finalReportDefinition,
    "finalReportsTableContainer",
    getSearchParams(),
    updateUrlParams
  ),
];

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
  tableBuilders,
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

new TableBuilder(
  receiptDefinition,
  "receiptsTableContainer",
  getSearchParams(),
  updateUrlParams
).build();

new TableBuilder(
  extractionDefinition,
  "extractionsTableContainer",
  getSearchParams(),
  updateUrlParams
).build();

new TableBuilder(
  libraryPreparationDefinition,
  "libraryPreparationsTableContainer",
  getSearchParams(),
  updateUrlParams
).build();

new TableBuilder(
  getLibraryQualificationsDefinition(urls.rest.libraryQualifications, true),
  "libraryQualificationsTableContainer",
  getSearchParams(),
  updateUrlParams
).build();

new TableBuilder(
  getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings, true),
  "fullDepthSequencingsTableContainer",
  getSearchParams(),
  updateUrlParams
).build();

new TableBuilder(
  informaticsReviewDefinition,
  "informaticsReviewsTableContainer",
  getSearchParams(),
  updateUrlParams
).build();

new TableBuilder(
  draftReportDefinition,
  "draftReportsTableContainer",
  getSearchParams(),
  updateUrlParams
).build();

new TableBuilder(
  finalReportDefinition,
  "finalReportsTableContainer",
  getSearchParams(),
  updateUrlParams
).build();
*/
