import { legendAction, TableDefinition } from "../component/table-builder";
import { makeIcon, makeNameDiv } from "../util/html-utils";
import { urls } from "../util/urls";
import { Run } from "./case";
import { MetricCategory } from "./assay";
import { getRunQcStatus } from "./sample";

interface Notification {
  run: Run;
  metricCategory: MetricCategory;
  pendingAnalysisCount: number;
  pendingQcCount: number;
  pendingDataReviewCount: number;
}

export const notificationDefinition: TableDefinition<Notification, void> = {
  queryUrl: urls.rest.notifications,
  defaultSort: {
    columnTitle: "Completion Date",
    descending: true,
    type: "date",
  },
  staticActions: [legendAction],
  generateColumns(data) {
    return [
      {
        title: "Run",
        addParentContents(notification, fragment) {
          const runName = notification.run.name;
          fragment.appendChild(
            makeNameDiv(
              runName,
              urls.miso.run(runName),
              urls.dimsum.run(runName)
            )
          );
        },
        sortType: "text",
      },
      {
        title: "Completion Date",
        addParentContents(notification, fragment) {
          const completionDate = notification.run.completionDate;
          if (completionDate) {
            fragment.appendChild(document.createTextNode(completionDate));
          }
        },
        sortType: "date",
      },
      {
        title: "QC Gate",
        addParentContents(notification, fragment) {
          switch (notification.metricCategory) {
            case "LIBRARY_QUALIFICATION":
              addText(fragment, "Library Qualification");
              break;
            case "FULL_DEPTH_SEQUENCING":
              addText(fragment, "Full-Depth Sequencing");
              break;
            default:
              addText(fragment, "unknown/error");
          }
        },
      },
      {
        title: "Run QC",
        addParentContents(notification, fragment) {
          const status = getRunQcStatus(notification.run);
          const icon = makeIcon(status.icon);
          icon.title = status.label;
          fragment.appendChild(icon);
        },
        getCellHighlight(notification) {
          const status = getRunQcStatus(notification.run);
          return status.cellStatus || null;
        },
      },
      {
        title: "Libraries Pending Analysis",
        addParentContents(notification, fragment) {
          addText(fragment, notification.pendingAnalysisCount.toString());
        },
      },
      {
        title: "Libraries Pending QC",
        addParentContents(notification, fragment) {
          addText(fragment, notification.pendingQcCount.toString());
        },
        getCellHighlight(notification) {
          return notification.pendingQcCount ? "warning" : null;
        },
      },
      {
        title: "Libraries Pending Data Review",
        addParentContents(notification, fragment) {
          addText(fragment, notification.pendingDataReviewCount.toString());
        },
        getCellHighlight(notification) {
          return notification.pendingDataReviewCount ? "warning" : null;
        },
      },
    ];
  },
};

function addText(element: Node, text: string) {
  element.appendChild(document.createTextNode(text));
}
