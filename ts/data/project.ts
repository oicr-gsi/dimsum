import { legendAction, TableDefinition } from "../component/table-builder";
import { makeNameDiv } from "../util/html-utils";
import { urls } from "../util/urls";

export interface ProjectSummary {
  name: string;
  totalTestCount: number;
  receiptPendingQcCount: number;
  receiptCompletedCount: number;
  extractionPendingCount: number;
  extractionPendingQcCount: number;
  extractionCompletedCount: number;
  libraryPrepPendingCount: number;
  libraryPrepPendingQcCount: number;
  libraryPrepCompletedCount: number;
  libraryQualPendingCount: number;
  libraryQualPendingQcCount: number;
  libraryQualCompletedCount: number;
  fullDepthSeqPendingCount: number;
  fullDepthSeqPendingQcCount: number;
  fullDepthSeqCompletedCount: number;
  informaticsPendingCount: number;
  informaticsCompletedCount: number;
  draftReportPendingCount: number;
  draftReportCompletedCount: number;
  finalReportPendingCount: number;
  finalReportCompletedCount: number;
}

export const projectDefinition: TableDefinition<ProjectSummary, void> = {
  queryUrl: urls.rest.projects,
  defaultSort: {
    columnTitle: "Name",
    descending: true,
    type: "text",
  },
  filters: [
    {
      title: "Name",
      key: "NAME",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.projectNames,
    },
  ],
  generateColumns(data) {
    return [
      {
        title: "Name",
        sortType: "text",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            makeNameDiv(
              projectSummary.name,
              urls.miso.project(projectSummary.name),
              urls.dimsum.project(projectSummary.name)
            )
          );
        },
      },
      {
        title: "Tests Expected",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(projectSummary.totalTestCount.toString())
          );
        },
      },
      {
        title: "Received",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.receiptCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Extracted",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.extractionCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Libraries Prepared",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.libraryPrepCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Libraries Qualified",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.libraryQualCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Sequenced",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.fullDepthSeqCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Informatics Reviewed",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.informaticsCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Report Drafted",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.draftReportCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Report Finalized",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.finalReportCompletedCount.toString()
            )
          );
        },
      },
    ];
  },
};
