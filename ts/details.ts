import { caseDefinition } from "./data/case";
import {
  draftReportDefinition,
  finalReportDefinition,
  informaticsReviewDefinition,
} from "./data/requisition";
import {
  extractionDefinition,
  fullDepthSequencingDefinition,
  libraryPreparationDefinition,
  libraryQualificationsDefinition,
  receiptDefinition,
} from "./data/sample";
import { TableBuilder } from "./util/table-builder";

new TableBuilder(caseDefinition, "casesTableContainer").build();
new TableBuilder(receiptDefinition, "receiptsTableContainer").build();
new TableBuilder(extractionDefinition, "extractionsTableContainer").build();
new TableBuilder(
  libraryPreparationDefinition,
  "libraryPreparationsTableContainer"
).build();
new TableBuilder(
  libraryQualificationsDefinition,
  "libraryQualificationsTableContainer"
).build();
new TableBuilder(
  fullDepthSequencingDefinition,
  "fullDepthSequencingsTableContainer"
).build();
new TableBuilder(
  informaticsReviewDefinition,
  "informaticsReviewsTableContainer"
).build();
new TableBuilder(draftReportDefinition, "draftReportsTableContainer").build();
new TableBuilder(finalReportDefinition, "finalReportsTableContainer").build();
