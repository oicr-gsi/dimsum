import { TableDefinition } from "../component/table-builder";
import { makeNameDiv, addMisoIcon, addTextDiv } from "../util/html-utils";
import { urls } from "../util/urls";
import { Run } from "./case";
import { siteConfig } from "../util/site-config";

export const runDefinition: TableDefinition<Run, void> = {
  queryUrl: urls.rest.notifications,
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
        sortType: "date",
      },
      {
        // QUESTION: Project name is not present in the run json.
        // Is there another way to get the project name?
        title: "Project",
        addParentContents(run, fragment) {
          fragment.appendChild(document.createTextNode("PROJECT NAME HERE"));
        },
        sortType: "date",
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
