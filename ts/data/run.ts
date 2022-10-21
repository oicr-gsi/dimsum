import { TableDefinition } from "../component/table-builder";
import { makeNameDiv } from "../util/html-utils";
import { urls } from "../util/urls";
import { Run } from "./case";

export const runDefinition: TableDefinition<Run, void> = {
  queryUrl: urls.rest.runList,
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
  ],
  generateColumns() {
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
      },
      {
        title: "Completion Date",
        addParentContents(run, fragment) {
          if (run.completionDate) {
            fragment.appendChild(document.createTextNode(run.completionDate));
          }
        },
      },
    ];
  },
};
