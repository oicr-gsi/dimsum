import {
  getFullDepthSequencingsDefinition,
  getLibraryQualificationsDefinition,
  Sample,
} from "./data/sample";
import { TableBuilder, TableDefinition } from "./util/table-builder";
import { urls } from "./util/urls";

const misoRunLink = document.getElementById("misoRunLink");
if (!misoRunLink) {
  throw new Error("MISO run link element missing");
}
const runName = misoRunLink.getAttribute("data-run-name");
if (!runName) {
  throw new Error("Missing run name data attribute");
}
misoRunLink.setAttribute("href", urls.miso.run(runName));

function makeTable(
  containerId: string,
  definition: TableDefinition<Sample, void>
) {
  const container = document.getElementById(containerId);
  if (container) {
    new TableBuilder(definition, containerId).build();
  }
}

makeTable(
  "libraryQualificationsTableContainer",
  getLibraryQualificationsDefinition(
    urls.rest.runs.libraryQualifications(runName)
  )
);

makeTable(
  "fullDepthSequencingsTableContainer",
  getFullDepthSequencingsDefinition(
    urls.rest.runs.fullDepthSequencings(runName)
  )
);
