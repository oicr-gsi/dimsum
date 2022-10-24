import { legendAction, TableDefinition } from "../component/table-builder";
import {
  addLink,
  makeIcon,
  styleText,
  addMisoIcon,
  makeNameDiv,
  addNaText,
  addTextDiv,
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
  stopped: boolean;
  receipts: Sample[];
  startDate: string;
  tests: Test[];
  requisitions: Requisition[];
  latestActivityDate: string;
}

export const caseDefinition: TableDefinition<Case, Test> = {
  queryUrl: urls.rest.cases,
  defaultSort: {
    columnTitle: "Latest Activity",
    descending: true,
    type: "date",
  },
  filters: [
    {
      title: "Assay",
      key: "ASSAY",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.assayNames,
    },
    {
      title: "Donor",
      key: "DONOR",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.donorNames,
    },
    {
      title: "Pending",
      key: "PENDING",
      type: "dropdown",
      values: siteConfig.pendingStates,
    },
    {
      title: "Pipeline",
      key: "PIPELINE",
      type: "dropdown",
      values: siteConfig.pipelines,
    },
    {
      title: "Project",
      key: "PROJECT",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.projectNames,
    },
    {
      title: "Requisition",
      key: "REQUISITION",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.requisitionNames,
    },
  ],
  getChildren: (parent) => parent.tests,
  getRowHighlight: (kase) => (kase.stopped ? "stopped" : null),
  staticActions: [legendAction],
  generateColumns: () => [
    {
      title: "Project",
      addParentContents(kase, fragment) {
        kase.projects.forEach((project) => {
          const nameDiv = document.createElement("div");
          nameDiv.className = "flex flex-row space-x-2 items-center";
          addLink(nameDiv, project.name, urls.dimsum.project(project.name));
          addMisoIcon(nameDiv, urls.miso.project(project.name));
          fragment.appendChild(nameDiv);

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
        const nameDiv = document.createElement("div");
        nameDiv.className = "flex flex-row space-x-2 items-center";
        addLink(nameDiv, kase.donor.name, urls.dimsum.donor(kase.donor.name));
        addMisoIcon(nameDiv, urls.miso.sample(kase.donor.id));
        fragment.appendChild(nameDiv);

        const externalNameDiv = document.createElement("div");
        externalNameDiv.appendChild(
          document.createTextNode(kase.donor.externalName)
        );
        const tooltipInstance = Tooltip.getInstance();
        tooltipInstance.addTarget(
          externalNameDiv,
          document.createTextNode("External Name")
        );
        fragment.appendChild(externalNameDiv);

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
        if (kase.stopped) {
          const stoppedDiv = document.createElement("div");
          styleText(stoppedDiv, "error");
          stoppedDiv.appendChild(document.createTextNode("CASE STOPPED"));
          fragment.appendChild(stoppedDiv);
        }
        kase.requisitions.forEach((requisition) => {
          const requisitionDiv = document.createElement("div");
          addLink(
            requisitionDiv,
            requisition.name,
            urls.dimsum.requisition(requisition.id)
          );
          addMisoIcon(requisitionDiv, urls.miso.requisition(requisition.id));
          fragment.appendChild(requisitionDiv);
        });
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
        addSampleIcons(kase.receipts, fragment);
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
        addSampleIcons(test.extractions, fragment);
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
        addSampleIcons(test.libraryPreparations, fragment);
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
        addSampleIcons(test.libraryQualifications, fragment);
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
        addSampleIcons(test.fullDepthSequencings, fragment);
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
          kase.requisitions,
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
          kase.requisitions,
          (requisition) => requisition.informaticsReviews
        );
        addRequisitionIcons(
          kase.requisitions,
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
          kase.requisitions,
          (requisition) => requisition.draftReports
        );
        addRequisitionIcons(
          kase.requisitions,
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
  if (kase.stopped && !samples.length) {
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
  } else if (kase.stopped) {
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
  if (kase.stopped && !kase.requisitions.flatMap(getQcs).length) {
    addNaText(fragment);
    return true;
  } else {
    return false;
  }
}

function requisitionPhaseComplete(
  requisitions: Requisition[],
  getQcs: (requisition: Requisition) => RequisitionQc[]
) {
  return requisitions.every((requisition) => {
    const qcs = getQcs(requisition);
    return qcs.length && getLatestRequisitionQc(qcs).qcPassed;
  });
}

function getRequisitionPhaseHighlight(
  kase: Case,
  getQcs: (requisition: Requisition) => RequisitionQc[]
) {
  if (requisitionPhaseComplete(kase.requisitions, getQcs)) {
    return null;
  } else if (kase.stopped) {
    return kase.requisitions.flatMap(getQcs).length ? null : "na";
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

function addSampleIcons(samples: Sample[], fragment: DocumentFragment) {
  samples.forEach((sample, i) => {
    const status = getQcStatus(sample);
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
  tooltipContainer.appendChild(topContainer);
  tooltipContainer.appendChild(bottomContainer);
  return tooltipContainer;
}

function addRequisitionIcons(
  requisitions: Requisition[],
  getQcs: (requisition: Requisition) => RequisitionQc[],
  previousComplete: boolean,
  fragment: DocumentFragment
) {
  requisitions.forEach((requisition) => {
    const qcs = getQcs(requisition);
    if (qcs.length) {
      const status = getRequisitionQcStatus(qcs);
      addRequisitionIcon(requisition, status, fragment);
    } else if (previousComplete) {
      const status = qcStatuses.qc;
      addRequisitionIcon(requisition, status, fragment);
    }
  });
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
  misoUrl: string,
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
      kase.requisitions,
      (requisition) => requisition.finalReports
    )
  ) {
    endDate = new Date(
      kase.requisitions
        .flatMap((req) => req.finalReports)
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
