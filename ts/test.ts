import { Case, tableDefinition } from "./data/case";
import { TableBuilder } from "./util/table-builder";

const cases: Case[] = [
  {
    projects: [
      {
        name: "PROJ",
      },
    ],
    donor: {
      id: 1,
      name: "PROJ_0001",
      externalName: "extnl-01",
    },
    tests: [
      {
        name: "Normal WG",
      },
      {
        name: "Tumour WG",
      },
      {
        name: "Tumour WT",
      },
    ],
    latestActivityDate: "2022-06-23",
  },
];

let table = new TableBuilder(tableDefinition, "tableContainer");
table.build();
table.load(cases);
