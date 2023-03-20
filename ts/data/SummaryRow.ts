import { legendAction, TableDefinition } from "../component/table-builder";
import { makeNameDiv } from "../util/html-utils";
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
    generateColumns(date) {
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
            fragment.appendChild(
              document.createTextNode(
                projectSummaryRow.receipt.count.toString()
              )
            );
          },
        },
        {
          title: "Extraction",
          addParentContents(projectSummaryRow, fragment) {
            fragment.appendChild(
              document.createTextNode(
                projectSummaryRow.extraction.count.toString()
              )
            );
          },
        },
        {
          title: "Library Preparation",
          addParentContents(projectSummaryRow, fragment) {
            fragment.appendChild(
              document.createTextNode(
                projectSummaryRow.libraryPreparation.count.toString()
              )
            );
          },
        },
        {
          title: "Library Qualification",
          addParentContents(projectSummaryRow, fragment) {
            fragment.appendChild(
              document.createTextNode(
                projectSummaryRow.libraryQualification.count.toString()
              )
            );
          },
        },
        {
          title: "Full-Depth Sequencing",
          addParentContents(projectSummaryRow, fragment) {
            fragment.appendChild(
              document.createTextNode(
                projectSummaryRow.fullDepthSequencing.count.toString()
              )
            );
          },
        },
        {
          title: "Informatics Review",
          addParentContents(projectSummaryRow, fragment) {
            fragment.appendChild(
              document.createTextNode(
                projectSummaryRow.informaticsReview.count.toString()
              )
            );
          },
        },
        {
          title: "Draft Report",
          addParentContents(projectSummaryRow, fragment) {
            fragment.appendChild(
              document.createTextNode(
                projectSummaryRow.draftReport.count.toString()
              )
            );
          },
        },
        {
          title: "Final Report",
          addParentContents(projectSummaryRow, fragment) {
            fragment.appendChild(
              document.createTextNode(
                projectSummaryRow.finalReport.count.toString()
              )
            );
          },
        },
      ];
    },
    projectSummary: true,
  };
}
