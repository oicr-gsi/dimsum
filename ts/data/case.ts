import { TableDefinition } from "../util/table-builder";
import * as Rest from "../util/rest-api";

export interface Project {
  name: string;
}

export interface Donor {
  id: number;
  name: string;
  externalName: string;
}

export interface Test {
  name: string;
}

export interface Case {
  projects: Project[];
  donor: Donor;
  tests: Test[];
  latestActivityDate: string;
}

export const caseDefinition: TableDefinition<Case, Test> = {
  queryUrl: Rest.cases.query,
  getChildren: (parent) => parent.tests,
  columns: [
    {
      title: "Project",
      addParentContents(kase, fragment) {
        kase.projects.forEach((project) => {
          const div = document.createElement("div");
          div.appendChild(document.createTextNode(project.name));
          fragment.appendChild(div);
        });
      },
    },
    {
      title: "Donor",
      addParentContents(kase, fragment) {
        const nameDiv = document.createElement("div");
        nameDiv.appendChild(document.createTextNode(kase.donor.name));
        fragment.appendChild(nameDiv);
        const externalNameDiv = document.createElement("div");
        externalNameDiv.appendChild(
          document.createTextNode(kase.donor.externalName)
        );
        fragment.appendChild(externalNameDiv);
      },
    },
    {
      title: "Test",
      child: true,
      addChildContents(test, fragment) {
        fragment.appendChild(document.createTextNode(test.name));
      },
    },
    {
      title: "Last Activity",
      addParentContents(kase, fragment) {
        fragment.appendChild(document.createTextNode(kase.latestActivityDate));
      },
    },
  ],
};
