import { TableDefinition } from "../component/table-builder";
import { makeNameDiv, addMisoIcon, addLink } from "../util/html-utils";
import { urls } from "../util/urls";
import { Case, Run, getElapsedMessage } from "./case";
import { Sample } from "./sample";
import { siteConfig } from "../util/site-config";

export interface RunAndLibraries {
  run: Run;
  libraryQualifications: Sample[];
  fullDepthSequencings: Sample[];
  projects: string[];
}

export const runDefinition: TableDefinition<Run, RunAndLibraries> = {
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
        // QUESTION: Project name is not present in the run json.
        // Is there another way to get the project name?
        title: "(SIMPLE) Project",
        addParentContents(run, fragment) {
          fragment.appendChild(document.createTextNode("PROJECT NAME HERE"));
        },
      },
      {
        title: "(SIMPLE) Start Date",
        addParentContents(run, fragment) {
          fragment.appendChild(document.createTextNode("START DATE HERE"));
        },
      },
      {
        title: "Project",
        child: true,
        addChildContents(runAndLibraries, run, fragment) {
          const projectNameDiv = document.createElement("div");
          fragment.appendChild(projectNameDiv);
          runAndLibraries.projects.forEach((project) => {
            const nameDiv = document.createElement("div");
            nameDiv.className = "flex flex-row space-x-2 items-center";
            addLink(nameDiv, project, urls.dimsum.project(project));
            addMisoIcon(nameDiv, urls.miso.project(project));
            projectNameDiv.appendChild(nameDiv);
          });
        },
      },

      // {
      //   title: "Test",
      //   child: true,
      //   addChildContents(test, kase, fragment) {
      //     const testNameDiv = document.createElement("div");
      //     testNameDiv.appendChild(document.createTextNode(test.name));
      //     fragment.appendChild(testNameDiv);

      //     if (test.groupId) {
      //       const groupIdDiv = document.createElement("div");
      //       groupIdDiv.appendChild(document.createTextNode(test.groupId));
      //       const tooltipInstance = Tooltip.getInstance();
      //       tooltipInstance.addTarget(
      //         groupIdDiv,
      //         document.createTextNode("Group ID")
      //       );
      //       fragment.appendChild(groupIdDiv);
      //     }
      //   },
      // },

      {
        title: "Start Date",
        sortType: "date",
        addParentContents(run, fragment) {
          const dateDiv = document.createElement("div");
          dateDiv.appendChild(document.createTextNode("START DATE (1)"));
          fragment.appendChild(dateDiv);

          const elapsedDiv = document.createElement("div");
          elapsedDiv.appendChild(
            // document.createTextNode(getElapsedMessage(kase))
            document.createTextNode("START DATE (2)")
          );
          fragment.appendChild(elapsedDiv);
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
