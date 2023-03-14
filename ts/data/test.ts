import { legendAction, TableDefinition } from "../component/table-builder";
import {
  addLink,
  makeIcon,
  styleText,
  addMisoIcon,
  makeNameDiv,
  addNaText,
  addTextDiv,
  makeTextDivWithTooltip,
} from "../util/html-utils";
import { siteConfig } from "../util/site-config";
import { urls } from "../util/urls";
import {
  Test,
  Donor,
  Project,
  makeSampleTooltip,
  samplePhaseComplete,
  addConstructionIcon,
  addSpace,
  assertNotNull,
  samplePhasePendingWork,
} from "./case";
import { Assay } from "./assay";
import { getQcStatus, Sample } from "./sample";
import { QcStatus, qcStatuses } from "./qc-status";
import { Requisition } from "./requisition";
import { Tooltip } from "../component/tooltip";
import { caseFilters, latestActivitySort } from "../component/table-components";

export interface TestTableView {
  test: Test;
  caseId: string;
  requisition: Requisition;
  donor: Donor;
  projects: Project[];
  assay: Assay;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  receipts: Sample[];
}

export const testDefinition: TableDefinition<TestTableView, void> = {
  queryUrl: urls.rest.tests,
  defaultSort: {
    columnTitle: "Test",
    descending: true,
    type: "text",
  },
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: () => [
    {
      title: "Project",
      addParentContents(testTableView, fragment) {
        testTableView.projects.forEach((project) => {
          fragment.appendChild(
            makeNameDiv(
              project.name,
              urls.miso.project(project.name),
              urls.dimsum.project(project.name)
            )
          );

          const pipelineDiv = document.createElement("div");
          pipelineDiv.appendChild(document.createTextNode(project.pipeline));
          fragment.appendChild(pipelineDiv);
        });
      },
    },
    {
      title: "Donor",
      sortType: "text",
      addParentContents(testTableView, fragment) {
        fragment.appendChild(
          makeNameDiv(
            testTableView.donor.name,
            urls.miso.sample(testTableView.donor.id),
            urls.dimsum.donor(testTableView.donor.name)
          )
        );
        fragment.appendChild(
          makeTextDivWithTooltip(
            testTableView.donor.externalName,
            "External Name"
          )
        );

        const tumourDetailDiv = document.createElement("div");
        tumourDetailDiv.appendChild(
          document.createTextNode(
            `${testTableView.tissueOrigin} ${testTableView.tissueType}` +
              (testTableView.timepoint ? " " + testTableView.timepoint : "")
          )
        );
        const tooltipDiv = document.createElement("div");
        addTextDiv(`Tissue Origin: ${testTableView.tissueOrigin}`, tooltipDiv);
        addTextDiv(`Tissue Type: ${testTableView.tissueType}`, tooltipDiv);
        if (testTableView.timepoint) {
          addTextDiv(`Timepoint: ${testTableView.timepoint}`, tooltipDiv);
        }
        const tooltipInstance = Tooltip.getInstance();
        tooltipInstance.addTarget(tumourDetailDiv, tooltipDiv);
        fragment.appendChild(tumourDetailDiv);
      },
    },
    {
      title: "Assay",
      sortType: "text",
      addParentContents(testTableView, fragment) {
        const assayDiv = document.createElement("div");
        const assay = siteConfig.assaysById[testTableView.assay.id];
        addLink(assayDiv, assay.name, urls.dimsum.case(testTableView.caseId));
        fragment.appendChild(assayDiv);

        const requisition = testTableView.requisition;
        if (requisition.stopped) {
          const stoppedDiv = makeTextDivWithTooltip(
            "CASE STOPPED",
            `Stop reason: ${requisition.stopReason || "Unspecified"}`
          );
          styleText(stoppedDiv, "error");
          fragment.appendChild(stoppedDiv);
        }
        const requisitionDiv = document.createElement("div");
        addLink(
          requisitionDiv,
          requisition.name,
          urls.dimsum.requisition(requisition.id)
        );
        addMisoIcon(requisitionDiv, urls.miso.requisition(requisition.id));
        fragment.appendChild(requisitionDiv);
      },
    },
    {
      title: "Test",
      sortType: "text",
      addParentContents(testTableView, fragment) {
        const testNameDiv = document.createElement("div");
        testNameDiv.appendChild(makeNameDiv(testTableView.test.name));
        fragment.appendChild(testNameDiv);

        if (testTableView.test.groupId) {
          const groupIdDiv = document.createElement("div");
          groupIdDiv.appendChild(
            document.createTextNode(testTableView.test.groupId)
          );
          const tooltipInstance = Tooltip.getInstance();
          tooltipInstance.addTarget(
            groupIdDiv,
            document.createTextNode("Group ID")
          );
          fragment.appendChild(groupIdDiv);
        }
      },
    },
    {
      title: "Extraction",
      addParentContents(testTableView, fragment) {
        if (
          handleNaSamplePhase(
            testTableView,
            testTableView.test.extractions,
            fragment
          )
        ) {
          return;
        }
        if (
          !testTableView.test.extractions.length &&
          testTableView.test.extractionSkipped
        ) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(testTableView, testTableView.test.extractions, fragment);
        if (
          samplePhaseComplete(testTableView.receipts) &&
          samplePhasePendingWork(testTableView.test.extractions)
        ) {
          if (testTableView.test.extractions.length) {
            addSpace(fragment);
          }
          addConstructionIcon("extraction", fragment);
        }
      },
      getCellHighlight(testTableView) {
        testTableView.test = assertNotNull(testTableView.test);
        if (
          !testTableView.test.extractions.length &&
          testTableView.test.extractionSkipped
        ) {
          return "na";
        }
        return getSamplePhaseHighlight(
          testTableView,
          testTableView.test.extractions
        );
      },
    },
    {
      title: "Library Preparation",
      addParentContents(testTableView, fragment) {
        if (
          handleNaSamplePhase(
            testTableView,
            testTableView.test.libraryPreparations,
            fragment
          )
        ) {
          return;
        }
        if (
          !testTableView.test.libraryPreparations.length &&
          testTableView.test.libraryPreparationSkipped
        ) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(
          testTableView,
          testTableView.test.libraryPreparations,
          fragment
        );
        if (
          samplePhaseComplete(testTableView.test.extractions) &&
          samplePhasePendingWork(testTableView.test.libraryPreparations)
        ) {
          if (testTableView.test.libraryPreparations.length) {
            addSpace(fragment);
          }
          addConstructionIcon("library preparation", fragment);
        }
      },
      getCellHighlight(testTableView) {
        testTableView.test = assertNotNull(testTableView.test);
        if (
          !testTableView.test.libraryPreparations.length &&
          testTableView.test.libraryPreparationSkipped
        ) {
          return "na";
        }
        return getSamplePhaseHighlight(
          testTableView,
          testTableView.test.libraryPreparations
        );
      },
    },
    {
      title: "Library Qualification",
      addParentContents(testTableView, fragment) {
        if (
          handleNaSamplePhase(
            testTableView,
            testTableView.test.libraryQualifications,
            fragment
          )
        ) {
          return;
        }
        addSampleIcons(
          testTableView,
          testTableView.test.libraryQualifications,
          fragment
        );
        if (
          samplePhaseComplete(testTableView.test.libraryPreparations) &&
          samplePhasePendingWork(testTableView.test.libraryQualifications)
        ) {
          if (testTableView.test.libraryQualifications.length) {
            addSpace(fragment);
          }
          addConstructionIcon("library qualification", fragment);
        }
      },
      getCellHighlight(testTableView) {
        testTableView.test = assertNotNull(testTableView.test);
        return getSamplePhaseHighlight(
          testTableView,
          testTableView.test.libraryQualifications
        );
      },
    },
    {
      title: "Full-Depth Sequencing",
      addParentContents(testTableView, fragment) {
        if (
          handleNaSamplePhase(
            testTableView,
            testTableView.test.fullDepthSequencings,
            fragment
          )
        ) {
          return;
        }
        addSampleIcons(
          testTableView,
          testTableView.test.fullDepthSequencings,
          fragment
        );
        if (
          samplePhaseComplete(testTableView.test.libraryQualifications) &&
          samplePhasePendingWork(testTableView.test.fullDepthSequencings)
        ) {
          if (testTableView.test.fullDepthSequencings.length) {
            addSpace(fragment);
          }
          addConstructionIcon("full-depth sequencing", fragment);
        }
      },
      getCellHighlight(testTableView) {
        testTableView.test = assertNotNull(testTableView.test);
        return getSamplePhaseHighlight(
          testTableView,
          testTableView.test.fullDepthSequencings
        );
      },
    },
  ],
};

function handleNaSamplePhase(
  testTableView: TestTableView,
  samples: Sample[],
  fragment: DocumentFragment
) {
  if (testTableView.requisition.stopped && !samples.length) {
    addNaText(fragment);
    return true;
  } else {
    return false;
  }
}

function addSampleIcons(
  testTableView: TestTableView,
  samples: Sample[],
  fragment: DocumentFragment
) {
  samples.forEach((sample, i) => {
    let status = getQcStatus(sample);
    if (
      status === qcStatuses.passed &&
      sample.assayId !== testTableView.assay.id
    ) {
      status = qcStatuses.passedDifferentAssay;
    }
    const icon = makeIcon(status.icon);
    const tooltipInstance = Tooltip.getInstance();
    tooltipInstance.addTarget(icon, makeSampleTooltip(sample));
    fragment.appendChild(icon);
    if (i < samples.length - 1) {
      fragment.appendChild(document.createTextNode(" "));
    }
  });
}

function getSamplePhaseHighlight(
  testTableView: TestTableView,
  samples: Sample[]
) {
  if (samplePhaseComplete(samples)) {
    return null;
  } else if (testTableView.requisition.stopped) {
    return samples.length ? null : "na";
  } else {
    return "warning";
  }
}
