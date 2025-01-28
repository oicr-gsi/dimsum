import { legendAction, TableDefinition } from "../component/table-builder";
import { addLink, makeIcon, makeNameDiv } from "../util/html-utils";
import { urls } from "../util/urls";
import { Run } from "./case";
import { getQcStatusWithDataReview } from "./sample";
import { siteConfig } from "../util/site-config";

interface Notification {
  run: Run;
  pendingAnalysisCount: number;
  pendingQcCount: number;
  pendingDataReviewCount: number;
  issueKey: string;
}

export const notificationDefinition: TableDefinition<Notification, void> = {
  queryUrl: urls.rest.notifications,
  getDefaultSort: () => {
    return {
      columnTitle: "Completion Date",
      descending: true,
      type: "date",
    };
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
              urls.dimsum.run(runName),
              runName
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
        title: "Run QC",
        addParentContents(notification, fragment) {
          const status = getQcStatusWithDataReview(notification.run);
          const icon = makeIcon(status.icon);
          icon.title = status.label;
          fragment.appendChild(icon);
        },
        getCellHighlight(notification) {
          const status = getQcStatusWithDataReview(notification.run);
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
      {
        title: "Issue",
        addParentContents(notification, fragment) {
          if (notification.issueKey) {
            addLink(
              fragment,
              notification.issueKey,
              urls.jira.issue(notification.issueKey),
              true
            );
          } else if (siteConfig.jiraUrl) {
            addText(fragment, "Error");
          } else {
            addText(fragment, "N/A");
          }
        },
        getCellHighlight(notification) {
          if (siteConfig.jiraUrl) {
            if (!notification.issueKey) {
              return "error";
            }
          } else {
            return "na";
          }
          return null;
        },
      },
    ];
  },
};

function addText(element: Node, text: string) {
  element.appendChild(document.createTextNode(text));
}
