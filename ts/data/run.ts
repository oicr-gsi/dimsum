import { TableDefinition } from "../component/table-builder";
import { makeNameDiv, addMisoIcon } from "../util/html-utils";
import { urls } from "../util/urls";
import { Run, Test } from "./case";
import { Tooltip } from "../component/tooltip";
import { siteConfig } from "../util/site-config";

export const runDefinition: TableDefinition<Run, void> = {
  queryUrl:
    urls.rest.runs.fullDepthSequencings.name &&
    urls.rest.runs.libraryQualifications.name,
  defaultSort: {
    columnTitle: "Completion Date",
    descending: true,
    type: "date",
  },
  filters: [
    {
      title: "Run",
      key: "RUN",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.runNames,
    },
    {
      title: "Project",
      key: "PROJECT",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.projectNames,
    },
    {
      title: "Pending",
      key: "PENDING",
      type: "dropdown",
      values: siteConfig.pendingStates,
    },
  ],
  generateColumns(data) {
    return [
      {
        title: "Run",
        addParentContents(run, fragment) {
          fragment.appendChild(
            makeNameDiv(
              run.name,
              urls.miso.run(run.name),
              urls.dimsum.run(run.name)
            )
          );
        },
      },
      {
        title: "Project",
        addParentContents(run, fragment) {
          fragment.appendChild(
            makeNameDiv(
              run.name,
              urls.miso.project(run.name),
              urls.dimsum.project(run.name)
            )
          );
        },
      },
      {
        title: "Completion Date",
        addParentContents(run, fragment) {
          if (run.completionDate) {
            fragment.appendChild(document.createTextNode(run.completionDate));
          }
        },
        sortType: "date",
      },
    ];
  },
};

/* FROM THE NOTIFICATION BRANCH
import { TableDefinition } from "../component/table-builder";
import { makeNameDiv } from "../util/html-utils";
import { urls } from "../util/urls";
import { Run } from "./case";

export const notificationDefinition: TableDefinition<Run, void> = {
  queryUrl: urls.rest.notifications,
  defaultSort: {
    columnTitle: "Completion Date",
    descending: true,
    type: "date",
  },
  generateColumns(data) {
    return [
      {
        title: "Name",
        addParentContents(run, fragment) {
          fragment.appendChild(
            makeNameDiv(
              run.name,
              urls.miso.run(run.name),
              urls.dimsum.run(run.name)
            )
          );
        },
        sortType: "text",
      },
      {
        title: "Completion Date",
        addParentContents(run, fragment) {
          if (run.completionDate) {
            fragment.appendChild(document.createTextNode(run.completionDate));
          }
        },
        sortType: "date",
      },
    ];
  },
};
*/
