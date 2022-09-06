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
