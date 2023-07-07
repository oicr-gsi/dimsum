import { legendAction, TableDefinition } from "../component/table-builder";
import { makeNameDiv } from "../util/html-utils";
import { urls } from "../util/urls";
import { Run } from "./case";

export const runDefinition: TableDefinition<Run, void> = {
  queryUrl: urls.rest.runs.list,
  defaultSort: {
    columnTitle: "Completion Date",
    descending: true,
    type: "date",
  },
  staticActions: [legendAction],
  filters: [
    {
      title: "Name",
      key: "NAME",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.runNames,
    },
  ],
  generateColumns(data) {
    return [
      {
        title: "Name",
        addParentContents(run, fragment) {
          fragment.appendChild(
            makeNameDiv(
              run.name,
              urls.miso.run(run.name),
              urls.dimsum.run(run.name),
              run.name
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
