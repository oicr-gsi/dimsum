import { getQcStatus } from "./data/qc-status";
import {
  getFullDepthSequencingsDefinition,
  getLibraryQualificationsDefinition,
  Sample,
} from "./data/sample";
import { makeIcon, shadeElement } from "./util/html-utils";
import { TableBuilder, TableDefinition } from "./component/table-builder";
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

const runQcCell = document.getElementById("runStatus");
if (!runQcCell) {
  throw new Error("Run QC cell missing");
}
const statusString = runQcCell.getAttribute("data-run-status");
const status = getQcStatus(statusString);
const icon = makeIcon(status.icon);
icon.title = status.label;
runQcCell.appendChild(icon);
shadeElement(runQcCell, status.cellStatus);

makeTable(
  "libraryQualificationsTableContainer",
  getLibraryQualificationsDefinition(
    urls.rest.runs.libraryQualifications(runName),
    false
  )
);

makeTable(
  "fullDepthSequencingsTableContainer",
  getFullDepthSequencingsDefinition(
    urls.rest.runs.fullDepthSequencings(runName),
    false
  )
);
