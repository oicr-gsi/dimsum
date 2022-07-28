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
import { TableBuilder } from "./util/table-builder";
import { urls } from "./util/urls";

new TableBuilder(caseDefinition, "casesTableContainer").build();
new TableBuilder(receiptDefinition, "receiptsTableContainer").build();
new TableBuilder(extractionDefinition, "extractionsTableContainer").build();
new TableBuilder(
  libraryPreparationDefinition,
  "libraryPreparationsTableContainer"
).build();
new TableBuilder(
  getLibraryQualificationsDefinition(urls.rest.libraryQualifications),
  "libraryQualificationsTableContainer"
).build();
new TableBuilder(
  getFullDepthSequencingsDefinition(urls.rest.fullDepthSequencings),
  "fullDepthSequencingsTableContainer"
).build();
new TableBuilder(
  informaticsReviewDefinition,
  "informaticsReviewsTableContainer"
).build();
new TableBuilder(draftReportDefinition, "draftReportsTableContainer").build();
new TableBuilder(finalReportDefinition, "finalReportsTableContainer").build();
