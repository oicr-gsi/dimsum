import { legendAction, TableDefinition } from "../component/table-builder";
import { makeNameDiv } from "../util/html-utils";
import { urls } from "../util/urls";
import { ProjectSummary } from "./case";

export const projectDefinition: TableDefinition<ProjectSummary, void> = {
  queryUrl: urls.rest.projects,
  defaultSort: {
    columnTitle: "Name",
    descending: true,
    type: "text",
  },
  staticActions: [legendAction],
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
        title: "Receipt",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.receiptCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Extraction",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.extractionCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Library Preparation",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.libraryPrepCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Library Qualification",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.libraryQualCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Full Depth Sequencing",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.fullDepthSeqCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Informatics Review",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.informaticsCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Draft Report",
        addParentContents(projectSummary, fragment) {
          fragment.appendChild(
            document.createTextNode(
              projectSummary.draftReportCompletedCount.toString()
            )
          );
        },
      },
      {
        title: "Final Report",
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
