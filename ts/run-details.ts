import { getQcStatus } from "./data/qc-status";
import {
  getFullDepthSequencingsDefinition,
  getLibraryQualificationsDefinition,
  Sample,
} from "./data/sample";
import {
  addLink,
  addTextDiv,
  getRequiredDataAttribute,
  getRequiredElementById,
  makeCopyButton,
  makeIcon,
  shadeElement,
} from "./util/html-utils";
import { TableBuilder, TableDefinition } from "./component/table-builder";
import { urls } from "./util/urls";
import { Tooltip } from "./component/tooltip";
import { getOmissionsDefinition } from "./data/run";
import { showAlertDialog } from "./component/dialog";

const misoRunLink = getRequiredElementById("misoRunLink");
const runName = getRequiredDataAttribute(misoRunLink, "data-run-name");
const copyButton = makeCopyButton(runName);
misoRunLink.parentNode?.insertBefore(copyButton, misoRunLink);
misoRunLink.setAttribute("href", urls.miso.run(runName));

const dashiRunLink = getRequiredElementById("dashiRunLink");
const libraryDesignsString = dashiRunLink.getAttribute("data-library-designs");
const libraryDesigns = libraryDesignsString
  ? libraryDesignsString.split(",")
  : [];
makeDashiRunLinksTooltip(dashiRunLink, runName, libraryDesigns);

function makeTable(
  containerId: string,
  definition: TableDefinition<Sample, void>
) {
  const container = document.getElementById(containerId);
  if (container) {
    new TableBuilder(definition, containerId).build();
  }
}

function makeOmissionsTable(runName: string) {
  const containerId = "omissionsTableContainer";
  const container = document.getElementById(containerId);
  if (container) {
    addOmissionsHelp();
    const definition = getOmissionsDefinition(
      urls.rest.runs.omissions(runName)
    );
    new TableBuilder(definition, containerId).build();
  }
}

function addOmissionsHelp() {
  document.getElementById("omissionsInfo")?.addEventListener("click", () => {
    showAlertDialog(
      "Omissions",
      "The libraries listed here were included in the run, but are not a part of any case."
    );
  });
}

const runQcCell = getRequiredElementById("runStatus");
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

makeOmissionsTable(runName);

function makeDashiRunLinksTooltip(
  element: HTMLElement,
  runName: string,
  libraryDesigns: string[]
) {
  const tooltipInstance = Tooltip.getInstance();
  tooltipInstance.addTarget(element, (fragment) => {
    let linksAdded = false;
    if (libraryDesigns.includes("TS") || libraryDesigns.includes("EX")) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Single-Lane Targeted Sequencing",
        urls.dashi.run.singleLaneTar(runName)
      );
    }
    if (libraryDesigns.includes("WT")) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Single-Lane RNA-seq",
        urls.dashi.run.singleLaneRna(runName)
      );
    }
    if (
      libraryDesigns.includes("WG") ||
      libraryDesigns.includes("SW") ||
      libraryDesigns.includes("PG")
    ) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Single-Lane WGS",
        urls.dashi.run.singleLaneWgs(runName)
      );
    }
    if (libraryDesigns.includes("CM")) {
      linksAdded = true;
      addLinkDiv(
        fragment,
        "Single-Lane cfMeDIP",
        urls.dashi.run.singleLaneCfMeDip(runName)
      );
    }
    if (!linksAdded) {
      addTextDiv("No Dashi-compatible libraries", fragment);
    }
  });
}

function addLinkDiv(container: Node, text: string, url: string) {
  const div = document.createElement("div");
  addLink(div, text, url, true);
  container.appendChild(div);
}
