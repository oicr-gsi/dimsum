import { legendAction, TableDefinition } from "../component/table-builder";
import { addNaText } from "../util/html-utils";
import { urls } from "../util/urls";
import { Project } from "./case";

export interface ProjectSummaryField {
  count: number;
  filterKey: string;
  filterValue: string;
}

export interface ProjectSummaryRow {
  title: string;
  receipt: ProjectSummaryField;
  extraction: ProjectSummaryField;
  libraryPreparation: ProjectSummaryField;
  libraryQualification: ProjectSummaryField;
  fullDepthSequencing: ProjectSummaryField;
  informaticsReview: ProjectSummaryField;
  draftReport: ProjectSummaryField;
  finalReport: ProjectSummaryField;
}

export function getProjectSummaryRowDefinition(
  queryUrl: string
): TableDefinition<ProjectSummaryRow, void> {
  return {
    queryUrl: queryUrl,
    disablePageControls: true,
    generateColumns(data) {
      return [
        {
          title: "Status",
          addParentContents(projectSummaryRow, fragment) {
            fragment.appendChild(
              document.createTextNode(projectSummaryRow.title)
            );
          },
        },
        {
          title: "Receipt",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(projectSummaryRow.receipt, fragment);
          },
          getCellHighlight(projectSummaryRow) {
            return !projectSummaryRow.receipt ? "na" : null;
          },
        },
        {
          title: "Extraction",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(projectSummaryRow.extraction, fragment);
          },
        },
        {
          title: "Library Preparation",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(projectSummaryRow.libraryPreparation, fragment);
          },
        },
        {
          title: "Library Qualification",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(projectSummaryRow.libraryQualification, fragment);
          },
        },
        {
          title: "Full-Depth Sequencing",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(projectSummaryRow.fullDepthSequencing, fragment);
          },
        },
        {
          title: "Informatics Review",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(projectSummaryRow.informaticsReview, fragment);
          },
          getCellHighlight(projectSummaryRow) {
            return !projectSummaryRow.informaticsReview ? "na" : null;
          },
        },
        {
          title: "Draft Report",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(projectSummaryRow.draftReport, fragment);
          },
          getCellHighlight(projectSummaryRow) {
            return !projectSummaryRow.draftReport ? "na" : null;
          },
        },
        {
          title: "Final Report",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(projectSummaryRow.finalReport, fragment);
          },
          getCellHighlight(projectSummaryRow) {
            return !projectSummaryRow.finalReport ? "na" : null;
          },
        },
      ];
    },
  };
}

function displayCount(
  projectSummaryField: ProjectSummaryField,
  fragment: DocumentFragment
) {
  if (!projectSummaryField) {
    addNaText(fragment);
    return;
  }
  fragment.appendChild(
    document.createTextNode(projectSummaryField.count.toString())
  );
}
