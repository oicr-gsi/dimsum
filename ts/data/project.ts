import { legendAction, TableDefinition } from "../component/table-builder";
import { makeNameDiv } from "../util/html-utils";
import { postDownload } from "../util/requests";
import { urls } from "../util/urls";
import { siteConfig } from "../util/site-config";

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
  analysisReviewPendingCount: number;
  analysisReviewCompletedCount: number;
  releaseApprovalPendingCount: number;
  releaseApprovalCompletedCount: number;
  releasePendingCount: number;
  releaseCompletedCount: number;
}

export const projectDefinition: TableDefinition<ProjectSummary, void> = {
  queryUrl: urls.rest.projects.list,
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
    {
      title: "Pipeline",
      key: "PIPELINE",
      type: "dropdown",
      values: siteConfig.pipelines,
    },
  ],
  bulkActions: [
    {
      title: "Download",
      handler: (items) => {
        const params: any = {};
        if (items && items.length) {
          params.projects = items.map((project) => project.name).join(",");
        }
        // May be expanded in the future to handle different reports, but for now there is only one
        postDownload(urls.rest.downloads.reports("tgl-tracking-sheet"), params);
      },
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
            displayFilteredProject(
              projectSummary.receiptCompletedCount,
              projectSummary.name,
              "Receipt"
            )
          );
        },
      },
      {
        title: "Extracted",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            displayFilteredProject(
              projectSummary.extractionCompletedCount,
              projectSummary.name,
              "Extraction"
            )
          );
        },
      },
      {
        title: "Libraries Prepared",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            displayFilteredProject(
              projectSummary.libraryPrepCompletedCount,
              projectSummary.name,
              "Library Preparation"
            )
          );
        },
      },
      {
        title: "Libraries Qualified",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            displayFilteredProject(
              projectSummary.libraryQualCompletedCount,
              projectSummary.name,
              "Library Qualification"
            )
          );
        },
      },
      {
        title: "Sequenced",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            displayFilteredProject(
              projectSummary.fullDepthSeqCompletedCount,
              projectSummary.name,
              "Full-Depth Sequencing"
            )
          );
        },
      },
      {
        title: "Analysis Reviewed",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            displayFilteredProject(
              projectSummary.analysisReviewCompletedCount,
              projectSummary.name,
              "Analysis Review"
            )
          );
        },
      },
      {
        title: "Release Approved",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            displayFilteredProject(
              projectSummary.releaseApprovalCompletedCount,
              projectSummary.name,
              "Release Approval"
            )
          );
        },
      },
      {
        title: "Released",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            displayFilteredProject(
              projectSummary.releaseCompletedCount,
              projectSummary.name,
              "Release"
            )
          );
        },
      },
    ];
  },
};

const completedFilterKey = "COMPLETED";
const tableFilterKey = "TABLE";

function displayFilteredProject(
  count: number,
  projectName: string,
  filterValue: string
) {
  const params = new URLSearchParams();
  const tableFilterValue = filterValue + "s";
  params.append(tableFilterKey, tableFilterValue);
  params.append(completedFilterKey, filterValue);
  return makeNameDiv(
    count.toString(),
    undefined,
    urls.dimsum.project(projectName) + `?${params.toString()}`
  );
}
