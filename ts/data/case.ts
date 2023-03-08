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
import { urls } from "../util/urls";
import { siteConfig } from "../util/site-config";
import { getQcStatus, Sample } from "./sample";
import { QcStatus, qcStatuses } from "./qc-status";
import {
  getLatestRequisitionQc,
  getRequisitionQcStatus,
  Requisition,
  RequisitionQc,
} from "./requisition";
import { Tooltip } from "../component/tooltip";
import { caseFilters, latestActivitySort } from "../component/table-components";

const dayMillis = 1000 * 60 * 60 * 24;

export interface Project {
  name: string;
  pipeline: string;
}

export interface Donor {
  id: string;
  name: string;
  externalName: string;
}

export interface Qcable {
  qcPassed?: boolean;
  qcReason?: string;
  qcUser?: string;
  qcDate?: string;
  dataReviewPassed?: boolean;
  dataReviewUser?: string;
  dataReviewDate?: string;
}

export interface Lane {
  laneNumber: number;
  percentOverQ30Read1?: number;
  percentOverQ30Read2?: number;
  clustersPf?: number;
  percentPfixRead1: number;
  percentPfixRead2: number;
}

export interface Run extends Qcable {
  id: number;
  name: string;
  containerModel?: string;
  joinedLanes: boolean;
  sequencingParameters?: string;
  readLength?: number;
  readLength2?: number;
  completionDate?: string;
  percentOverQ30?: number;
  clustersPf?: number;
  lanes: Lane[];
}

export interface Test {
  name: string;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  groupId: string;
  targetedSequencing: string;
  extractionSkipped: boolean;
  libraryPreparationSkipped: boolean;
  extractions: Sample[];
  libraryPreparations: Sample[];
  libraryQualifications: Sample[];
  fullDepthSequencings: Sample[];
  latestActivityDate: string;
}

export interface Case {
  id: string;
  projects: Project[];
  donor: Donor;
  assayId: number;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  receipts: Sample[];
  startDate: string;
  tests: Test[];
  requisition: Requisition;
  latestActivityDate: string;
}

export const caseDefinition: TableDefinition<Case, Test> = {
  queryUrl: urls.rest.cases,
  defaultSort: latestActivitySort,
  filters: caseFilters,
  getChildren: (parent) => parent.tests,
  getRowHighlight: (kase) => (kase.requisition.stopped ? "stopped" : null),
  staticActions: [legendAction],
  generateColumns: () => [
    {
      title: "Project",
      addParentContents(kase, fragment) {
        kase.projects.forEach((project) => {
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
      addParentContents(kase, fragment) {
        fragment.appendChild(
          makeNameDiv(
            kase.donor.name,
            urls.miso.sample(kase.donor.id),
            urls.dimsum.donor(kase.donor.name)
          )
        );
        fragment.appendChild(
          makeTextDivWithTooltip(kase.donor.externalName, "External Name")
        );

        const tumourDetailDiv = document.createElement("div");
        tumourDetailDiv.appendChild(
          document.createTextNode(
            `${kase.tissueOrigin} ${kase.tissueType}` +
              (kase.timepoint ? " " + kase.timepoint : "")
          )
        );
        const tooltipDiv = document.createElement("div");
        addTextDiv(`Tissue Origin: ${kase.tissueOrigin}`, tooltipDiv);
        addTextDiv(`Tissue Type: ${kase.tissueType}`, tooltipDiv);
        if (kase.timepoint) {
          addTextDiv(`Timepoint: ${kase.timepoint}`, tooltipDiv);
        }
        const tooltipInstance = Tooltip.getInstance();
        tooltipInstance.addTarget(tumourDetailDiv, tooltipDiv);
        fragment.appendChild(tumourDetailDiv);
      },
    },
    {
      title: "Assay",
      sortType: "text",
      addParentContents(kase, fragment) {
        const assayDiv = document.createElement("div");
        const assay = siteConfig.assaysById[kase.assayId];
        addLink(assayDiv, assay.name, urls.dimsum.case(kase.id));
        fragment.appendChild(assayDiv);

        const requisition = kase.requisition;
        if (kase.requisition.stopped) {
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
      title: "Start Date",
      sortType: "date",
      addParentContents(kase, fragment) {
        const dateDiv = document.createElement("div");
        dateDiv.appendChild(document.createTextNode(kase.startDate));
        fragment.appendChild(dateDiv);

        const elapsedDiv = document.createElement("div");
        elapsedDiv.appendChild(
          document.createTextNode(getElapsedMessage(kase))
        );
        fragment.appendChild(elapsedDiv);
      },
    },
    {
      title: "Receipt/Inspection",
      addParentContents(kase, fragment) {
        addSampleIcons(kase, kase.receipts, fragment);
        if (!kase.receipts.length) {
          addConstructionIcon("receipt", fragment);
        }
      },
      getCellHighlight(kase) {
        return getSamplePhaseHighlight(kase, kase.receipts);
      },
    },
    {
      title: "Test",
      child: true,
      addChildContents(test, kase, fragment) {
        const testNameDiv = document.createElement("div");
        testNameDiv.appendChild(document.createTextNode(test.name));
        fragment.appendChild(testNameDiv);

        if (test.groupId) {
          const groupIdDiv = document.createElement("div");
          groupIdDiv.appendChild(document.createTextNode(test.groupId));
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
      child: true,
      addChildContents(test, kase, fragment) {
        if (handleNaSamplePhase(kase, test.extractions, fragment)) {
          return;
        }
        if (!test.extractions.length && test.extractionSkipped) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(kase, test.extractions, fragment);
        if (
          samplePhaseComplete(kase.receipts) &&
          samplePhasePendingWork(test.extractions)
        ) {
          if (test.extractions.length) {
            addSpace(fragment);
          }
          addConstructionIcon("extraction", fragment);
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        if (!test.extractions.length && test.extractionSkipped) {
          return "na";
        }
        return getSamplePhaseHighlight(kase, test.extractions);
      },
    },
    {
      title: "Library Preparation",
      child: true,
      addChildContents(test, kase, fragment) {
        if (handleNaSamplePhase(kase, test.libraryPreparations, fragment)) {
          return;
        }
        if (
          !test.libraryPreparations.length &&
          test.libraryPreparationSkipped
        ) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(kase, test.libraryPreparations, fragment);
        if (
          samplePhaseComplete(test.extractions) &&
          samplePhasePendingWork(test.libraryPreparations)
        ) {
          if (test.libraryPreparations.length) {
            addSpace(fragment);
          }
          addConstructionIcon("library preparation", fragment);
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        if (
          !test.libraryPreparations.length &&
          test.libraryPreparationSkipped
        ) {
          return "na";
        }
        return getSamplePhaseHighlight(kase, test.libraryPreparations);
      },
    },
    {
      title: "Library Qualification",
      child: true,
      addChildContents(test, kase, fragment) {
        if (handleNaSamplePhase(kase, test.libraryQualifications, fragment)) {
          return;
        }
        addSampleIcons(kase, test.libraryQualifications, fragment);
        if (
          samplePhaseComplete(test.libraryPreparations) &&
          samplePhasePendingWork(test.libraryQualifications)
        ) {
          if (test.libraryQualifications.length) {
            addSpace(fragment);
          }
          addConstructionIcon("library qualification", fragment);
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        return getSamplePhaseHighlight(kase, test.libraryQualifications);
      },
    },
    {
      title: "Full-Depth Sequencing",
      child: true,
      addChildContents(test, kase, fragment) {
        if (handleNaSamplePhase(kase, test.fullDepthSequencings, fragment)) {
          return;
        }
        addSampleIcons(kase, test.fullDepthSequencings, fragment);
        if (
          samplePhaseComplete(test.libraryQualifications) &&
          samplePhasePendingWork(test.fullDepthSequencings)
        ) {
          if (test.fullDepthSequencings.length) {
            addSpace(fragment);
          }
          addConstructionIcon("full-depth sequencing", fragment);
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        return getSamplePhaseHighlight(kase, test.fullDepthSequencings);
      },
    },
    {
      title: "Informatics Review",
      addParentContents(kase, fragment) {
        if (
          handleNaRequisitionPhase(kase, (x) => x.informaticsReviews, fragment)
        ) {
          return;
        }
        const sequencingComplete = kase.tests.every((test) =>
          samplePhaseComplete(test.fullDepthSequencings)
        );
        addRequisitionIcons(
          kase.requisition,
          (requisition) => requisition.informaticsReviews,
          sequencingComplete,
          fragment
        );
      },
      getCellHighlight(kase) {
        return getRequisitionPhaseHighlight(
          kase,
          (requisition) => requisition.informaticsReviews
        );
      },
    },
    {
      title: "Draft Report",
      addParentContents(kase, fragment) {
        if (handleNaRequisitionPhase(kase, (x) => x.draftReports, fragment)) {
          return;
        }
        const informaticsComplete = requisitionPhaseComplete(
          kase.requisition,
          (requisition) => requisition.informaticsReviews
        );
        addRequisitionIcons(
          kase.requisition,
          (requisition) => requisition.draftReports,
          informaticsComplete,
          fragment
        );
      },
      getCellHighlight(kase) {
        return getRequisitionPhaseHighlight(
          kase,
          (requisition) => requisition.draftReports
        );
      },
    },
    {
      title: "Final Report",
      addParentContents(kase, fragment) {
        if (handleNaRequisitionPhase(kase, (x) => x.finalReports, fragment)) {
          return;
        }
        const draftComplete = requisitionPhaseComplete(
          kase.requisition,
          (requisition) => requisition.draftReports
        );
        addRequisitionIcons(
          kase.requisition,
          (requisition) => requisition.finalReports,
          draftComplete,
          fragment
        );
      },
      getCellHighlight(kase) {
        return getRequisitionPhaseHighlight(
          kase,
          (requisition) => requisition.finalReports
        );
      },
    },
    {
      title: "Latest Activity",
      sortType: "date",
      addParentContents(kase, fragment) {
        fragment.appendChild(document.createTextNode(kase.latestActivityDate));
      },
    },
  ],
};

function assertNotNull<Type>(object: Type | null) {
  if (object === null) {
    throw new Error("Unexpected null value");
  }
  return object;
}

function handleNaSamplePhase(
  kase: Case,
  samples: Sample[],
  fragment: DocumentFragment
) {
  if (kase.requisition.stopped && !samples.length) {
    addNaText(fragment);
    return true;
  } else {
    return false;
  }
}

function samplePhaseComplete(samples: Sample[]) {
  // consider incomplete if any are pending QC or data review
  // pending statuses besides "Top-up Required" are still considered pending QC
  if (
    samples.some(
      (sample) =>
        (sample.qcPassed === null &&
          sample.qcReason !== qcStatuses.topUp.label) ||
        (sample.run && !sample.dataReviewDate)
    )
  ) {
    return false;
  } else {
    // consider complete if at least one is passed QC, and data review if applicable
    return samples.some(
      (sample) => sample.qcPassed && (!sample.run || sample.dataReviewPassed)
    );
  }
}

function samplePhasePendingWork(samples: Sample[]) {
  // pending if there are no samples
  if (!samples.length) {
    return true;
  }
  // NOT pending if there are samples needing QC or data review
  if (
    samples.some(
      (sample) => !sample.qcDate || (sample.run && !sample.dataReviewDate)
    )
  ) {
    return false;
  }
  // NOT pending if there are samples with passed QC and data review (if applicable)
  return !samples.some(
    (sample) => sample.qcPassed && (!sample.run || sample.dataReviewPassed)
  );
}

function getSamplePhaseHighlight(kase: Case, samples: Sample[]) {
  if (samplePhaseComplete(samples)) {
    return null;
  } else if (kase.requisition.stopped) {
    return samples.length ? null : "na";
  } else {
    return "warning";
  }
}

function handleNaRequisitionPhase(
  kase: Case,
  getQcs: (requisition: Requisition) => RequisitionQc[],
  fragment: DocumentFragment
) {
  if (kase.requisition.stopped && !getQcs(kase.requisition).length) {
    addNaText(fragment);
    return true;
  } else {
    return false;
  }
}

function requisitionPhaseComplete(
  requisition: Requisition,
  getQcs: (requisition: Requisition) => RequisitionQc[]
): boolean {
  const qcs = getQcs(requisition);
  return !!(qcs.length && getLatestRequisitionQc(qcs).qcPassed);
}

function getRequisitionPhaseHighlight(
  kase: Case,
  getQcs: (requisition: Requisition) => RequisitionQc[]
) {
  if (requisitionPhaseComplete(kase.requisition, getQcs)) {
    return null;
  } else if (kase.requisition.stopped) {
    return getQcs(kase.requisition).length ? null : "na";
  } else {
    return "warning";
  }
}

function addSpace(fragment: DocumentFragment) {
  fragment.appendChild(document.createTextNode(" "));
}

function addConstructionIcon(phase: string, fragment: DocumentFragment) {
  const icon = makeIcon(qcStatuses.construction.icon);
  icon.title = `Pending ${phase}`;
  fragment.appendChild(icon);
}

function addSampleIcons(
  kase: Case,
  samples: Sample[],
  fragment: DocumentFragment
) {
  samples.forEach((sample, i) => {
    let status = getQcStatus(sample);
    if (status === qcStatuses.passed && sample.assayId !== kase.assayId) {
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

function makeSampleTooltip(sample: Sample) {
  const tooltipContainer = new DocumentFragment();
  const topContainer = document.createElement("div");
  topContainer.className = "flex flex-col space-y-1 text-black";
  const bottomContainer = document.createElement("div");
  bottomContainer.className =
    "grid grid-cols-2 grid-flow-row gap-y-1 font-inter font-medium font-14 text-black mt-3";
  // sample run links
  if (sample.run) {
    topContainer.appendChild(
      makeNameDiv(
        sample.sequencingLane
          ? sample.run.name + " (L" + sample.sequencingLane + ")"
          : sample.run.name,
        urls.miso.run(sample.run.name),
        urls.dimsum.run(sample.run.name)
      )
    );
  }
  // sample name links
  const sampleNameContainer = makeNameDiv(
    sample.name,
    urls.miso.sample(sample.id)
  );
  topContainer.appendChild(sampleNameContainer);

  // project links
  addTooltipRow(
    bottomContainer,
    "Project",
    sample.project,
    urls.miso.project(sample.project),
    urls.dimsum.project(sample.project)
  );
  // donor links
  addTooltipRow(
    bottomContainer,
    "Donor",
    sample.donor.name,
    urls.miso.sample(sample.donor.id),
    urls.dimsum.donor(sample.donor.name),
    sample.donor.externalName
  );
  // requisition links
  if (sample.requisitionId && sample.requisitionName) {
    addTooltipRow(
      bottomContainer,
      "Requisition",
      sample.requisitionName,
      urls.miso.requisition(sample.requisitionId),
      urls.dimsum.requisition(sample.requisitionId)
    );
  }
  if (sample.assayId) {
    const assay = siteConfig.assaysById[sample.assayId];
    addTooltipRow(bottomContainer, "Assay", assay.name);
  }
  tooltipContainer.appendChild(topContainer);
  tooltipContainer.appendChild(bottomContainer);
  return tooltipContainer;
}

function addRequisitionIcons(
  requisition: Requisition,
  getQcs: (requisition: Requisition) => RequisitionQc[],
  previousComplete: boolean,
  fragment: DocumentFragment
) {
  const qcs = getQcs(requisition);
  if (qcs.length) {
    const status = getRequisitionQcStatus(qcs);
    addRequisitionIcon(requisition, status, fragment);
  } else if (previousComplete) {
    const status = qcStatuses.qc;
    addRequisitionIcon(requisition, status, fragment);
  }
}

function addRequisitionIcon(
  requisition: Requisition,
  status: QcStatus,
  fragment: DocumentFragment
) {
  const tooltipIcon = makeIcon(status.icon);
  const tooltipInstance = Tooltip.getInstance();
  tooltipInstance.addTarget(tooltipIcon, makeRequisitionTooltip(requisition));
  fragment.appendChild(tooltipIcon);
}

function makeRequisitionTooltip(requisition: Requisition) {
  return makeNameDiv(
    requisition.name,
    urls.miso.requisition(requisition.id),
    urls.dimsum.requisition(requisition.id)
  );
}

function addTooltipRow(
  container: HTMLElement, // must be a two column grid
  label: string,
  name: string,
  misoUrl?: string,
  dimsumUrl?: string,
  additionalText?: string
) {
  const labelContainer = document.createElement("span");
  labelContainer.innerHTML = `${label}:`;
  const valueContainer = document.createElement("div");
  valueContainer.appendChild(makeNameDiv(name, misoUrl, dimsumUrl));
  if (additionalText) {
    const externalDonorNameContainer = document.createElement("span");
    externalDonorNameContainer.className = "block";
    externalDonorNameContainer.innerHTML = additionalText;
    valueContainer.appendChild(externalDonorNameContainer);
  }
  container.appendChild(labelContainer);
  container.appendChild(valueContainer);
}

function getElapsedMessage(kase: Case) {
  let endDate;
  let message;
  if (
    requisitionPhaseComplete(
      kase.requisition,
      (requisition) => requisition.finalReports
    )
  ) {
    endDate = new Date(
      kase.requisition.finalReports
        .map((qc) => qc.qcDate)
        .reduce((previous, current) =>
          previous > current ? previous : current
        )
    );
    message = "Completed in";
  } else {
    endDate = new Date();
    message = "Ongoing";
  }
  const startDate = new Date(kase.startDate);
  const milliDiff = endDate.getTime() - startDate.getTime();
  const dayDiff = Math.ceil(milliDiff / dayMillis);
  return `(${message} ${dayDiff} days)`;
}
