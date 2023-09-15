import { TableDefinition } from "../component/table-builder";
import { addNaText, makeNameDiv } from "../util/html-utils";
import { caseFilters } from "../component/table-components";
import { appendUrlParam, urls } from "../util/urls";

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
  analysisReview: ProjectSummaryField;
  releaseApproval: ProjectSummaryField;
  release: ProjectSummaryField;
}

export function getProjectSummaryRowDefinition(
  queryUrl: string
): TableDefinition<ProjectSummaryRow, void> {
  return {
    queryUrl: queryUrl,
    filters: [
      ...caseFilters,
      {
        title: "After Date",
        key: "AFTER_DATE",
        type: "text",
      },
      {
        title: "Before Date",
        key: "BEFORE_DATE",
        type: "text",
      },
    ],
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
            displayCount("Receipts", projectSummaryRow.receipt, fragment);
          },
          getCellHighlight(projectSummaryRow) {
            return !projectSummaryRow.receipt ? "na" : null;
          },
        },
        {
          title: "Extraction",
          addParentContents(projectSummaryRow, fragment) {
            displayCount("Extractions", projectSummaryRow.extraction, fragment);
          },
        },
        {
          title: "Library Preparation",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(
              "Library Preparations",
              projectSummaryRow.libraryPreparation,
              fragment
            );
          },
        },
        {
          title: "Library Qualification",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(
              "Library Qualifications",
              projectSummaryRow.libraryQualification,
              fragment
            );
          },
        },
        {
          title: "Full-Depth Sequencing",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(
              "Full-Depth Sequencings",
              projectSummaryRow.fullDepthSequencing,
              fragment
            );
          },
        },
        {
          title: "Analysis Review",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(
              "Analysis Reviews",
              projectSummaryRow.analysisReview,
              fragment
            );
          },
          getCellHighlight(projectSummaryRow) {
            return !projectSummaryRow.analysisReview ? "na" : null;
          },
        },
        {
          title: "Release Approval",
          addParentContents(projectSummaryRow, fragment) {
            displayCount(
              "Release Approvals",
              projectSummaryRow.releaseApproval,
              fragment
            );
          },
          getCellHighlight(projectSummaryRow) {
            return !projectSummaryRow.releaseApproval ? "na" : null;
          },
        },
        {
          title: "Release",
          addParentContents(projectSummaryRow, fragment) {
            displayCount("Releases", projectSummaryRow.release, fragment);
          },
          getCellHighlight(projectSummaryRow) {
            return !projectSummaryRow.release ? "na" : null;
          },
        },
      ];
    },
  };
}

const tableFilterKey = "TABLE";

function displayCount(
  tableFilterValue: string,
  projectSummaryField: ProjectSummaryField,
  fragment: DocumentFragment
) {
  if (!projectSummaryField) {
    addNaText(fragment);
    return;
  }
  const params = new URLSearchParams();
  params.append(tableFilterKey, tableFilterValue);
  params.append(projectSummaryField.filterKey, projectSummaryField.filterValue);
  fragment.appendChild(
    makeNameDiv(
      projectSummaryField.count.toString(),
      undefined,
      `?${params.toString()}`
    )
  );
}
