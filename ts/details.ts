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
import { urls } from "./util/urls";

// // parse params
// // prior to loading the filter controls, fetch search params
// const params = new URL(document.location.href).searchParams;
// params.forEach((value, key) => {
//   const reload = () => this.reload();
//   this.acceptedFilters.push(
//     new AcceptedFilter(key, key.toUpperCase(), value, reload)
//   );
// });

new TableBuilder(caseDefinition, "casesTableContainer").build();
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
