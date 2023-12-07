import { legendAction, TableDefinition } from "../component/table-builder";
import {
  addLink,
  makeIcon,
  styleText,
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
import { AssayTargets } from "./assay";

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
  qcNote?: string;
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
  extractionDaysSpent: number;
  libraryPreparationDaysSpent: number;
  libraryQualificationDaysSpent: number;
  fullDepthSequencingDaysSpent: number;
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
  startDate?: string;
  tests: Test[];
  requisition: Requisition;
  latestActivityDate?: string;
  receiptDaysSpent: number;
  analysisReviewDaysSpent: number;
  releaseApprovalDaysSpent: number;
  releaseDaysSpent: number;
  caseDaysSpent: number;
  pauseDays: number;
}

export const caseDefinition: TableDefinition<Case, Test> = {
  queryUrl: urls.rest.cases,
  defaultSort: {
    columnTitle: "Urgency",
    type: "number",
    descending: true,
  },
  nonColumnSorting: [
    {
      columnTitle: "Urgency",
      type: "number",
    },
  ],
  filters: caseFilters,
  getChildren: (parent) => parent.tests,
  getRowHighlight: (kase) => {
    if (kase.requisition.stopped) {
      return "stopped";
    } else if (kase.requisition.paused) {
      return "paused";
    }
    return null;
  },
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
        const nameDiv = makeNameDiv(
          kase.donor.name,
          urls.miso.sample(kase.donor.id),
          urls.dimsum.donor(kase.donor.name),
          kase.donor.name
        );
        fragment.appendChild(nameDiv);

        const externalNameDiv = makeTextDivWithTooltip(
          kase.donor.externalName,
          "External Name",
          true
        );
        fragment.appendChild(externalNameDiv);

        const tumourDetailDiv = document.createElement("div");
        tumourDetailDiv.appendChild(
          document.createTextNode(
            `${kase.tissueOrigin} ${kase.tissueType}` +
              (kase.timepoint ? " " + kase.timepoint : "")
          )
        );
        const addContents = (fragment: DocumentFragment) => {
          addTextDiv(`Tissue Origin: ${kase.tissueOrigin}`, fragment);
          addTextDiv(`Tissue Type: ${kase.tissueType}`, fragment);
          if (kase.timepoint) {
            addTextDiv(`Timepoint: ${kase.timepoint}`, fragment);
          }
        };
        const tooltipInstance = Tooltip.getInstance();
        tooltipInstance.addTarget(tumourDetailDiv, addContents);
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
        if (kase.requisition.paused) {
          const pausedDiv = makeTextDivWithTooltip(
            "CASE PAUSED",
            `Pause reason: ${requisition.pauseReason || "Unspecified"}`
          );
          styleText(pausedDiv, "error");
          fragment.appendChild(pausedDiv);
        }
        fragment.appendChild(
          makeNameDiv(
            requisition.name,
            urls.miso.requisition(requisition.id),
            urls.dimsum.requisition(requisition.id),
            requisition.name
          )
        );
      },
    },
    {
      title: "Start Date",
      sortType: "date",
      addParentContents(kase, fragment) {
        if (!kase.startDate) {
          return;
        }
        const dateDiv = document.createElement("div");
        dateDiv.appendChild(document.createTextNode(kase.startDate));
        fragment.appendChild(dateDiv);

        const elapsedDiv = document.createElement("div");
        if (!kase.requisition.stopped) {
          elapsedDiv.appendChild(
            document.createTextNode(getElapsedMessage(kase))
          );
          fragment.appendChild(elapsedDiv);

          if (!caseComplete(kase)) {
            const targets = getTargets(kase);
            if (caseOverdue(kase, targets)) {
              const overdueDiv = makeTextDivWithTooltip(
                "OVERDUE",
                `Case target: ${targets.caseDays} days`
              );
              styleText(overdueDiv, "bold");
              fragment.appendChild(overdueDiv);
            } else {
              const overdueStep = getOverdueStep(kase, targets);
              if (overdueStep) {
                const behindDiv = makeTextDivWithTooltip(
                  "BEHIND SCHEDULE",
                  `${overdueStep.stepLabel} target: ${overdueStep.targetDays} days`
                );
                styleText(behindDiv, "error");
                fragment.appendChild(behindDiv);
              }
            }
          }
        }
      },
      getCellHighlight(kase) {
        if (kase.requisition.stopped || caseComplete(kase)) {
          // Never show overdue/behind warning/error for completed or stopped cases
          return null;
        }
        const targets = getTargets(kase);
        if (caseOverdue(kase, targets)) {
          return "error";
        } else if (getOverdueStep(kase, targets)) {
          return "warning";
        }
        return null;
      },
    },
    {
      title: "Receipt/Inspection",
      addParentContents(kase, fragment) {
        addSampleIcons(kase.assayId, kase.receipts, fragment);
        if (samplePhasePendingWorkOrQc(kase.receipts)) {
          if (samplePhasePendingWork(kase.receipts)) {
            addConstructionIcon("receipt", fragment);
          }
          const targets = getTargets(kase);
          addTurnAroundTimeInfo(
            kase.caseDaysSpent,
            targets.receiptDays,
            fragment
          );
        }
      },
      getCellHighlight(kase) {
        return getSamplePhaseHighlight(kase.requisition, kase.receipts);
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
          const addContents = (fragment: DocumentFragment) =>
            fragment.appendChild(document.createTextNode("Group ID"));
          tooltipInstance.addTarget(groupIdDiv, addContents);
          fragment.appendChild(groupIdDiv);
        }
      },
    },
    {
      title: "Extraction",
      child: true,
      addChildContents(test, kase, fragment) {
        if (handleNaSamplePhase(kase.requisition, test.extractions, fragment)) {
          return;
        }
        if (!test.extractions.length && test.extractionSkipped) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(kase.assayId, test.extractions, fragment);
        if (samplePhaseComplete(kase.receipts)) {
          if (samplePhasePendingWorkOrQc(test.extractions)) {
            if (samplePhasePendingWork(test.extractions)) {
              if (test.extractions.length) {
                addSpace(fragment);
              }
              addConstructionIcon("extraction", fragment);
            }
            const targets = getTargets(kase);
            addTurnAroundTimeInfo(
              kase.caseDaysSpent,
              targets.extractionDays,
              fragment
            );
          }
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        if (!test.extractions.length && test.extractionSkipped) {
          return "na";
        }
        return getSamplePhaseHighlight(kase.requisition, test.extractions);
      },
    },
    {
      title: "Library Preparation",
      child: true,
      addChildContents(test, kase, fragment) {
        if (
          handleNaSamplePhase(
            kase.requisition,
            test.libraryPreparations,
            fragment
          )
        ) {
          return;
        }
        if (
          !test.libraryPreparations.length &&
          test.libraryPreparationSkipped
        ) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(kase.assayId, test.libraryPreparations, fragment);
        if (samplePhaseComplete(test.extractions)) {
          if (samplePhasePendingWorkOrQc(test.libraryPreparations)) {
            if (samplePhasePendingWork(test.libraryPreparations)) {
              if (test.libraryPreparations.length) {
                addSpace(fragment);
              }
              addConstructionIcon("library preparation", fragment);
            }
            const targets = getTargets(kase);
            addTurnAroundTimeInfo(
              kase.caseDaysSpent,
              targets.libraryPreparationDays,
              fragment
            );
          }
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
        return getSamplePhaseHighlight(
          kase.requisition,
          test.libraryPreparations
        );
      },
    },
    {
      title: "Library Qualification",
      child: true,
      addChildContents(test, kase, fragment) {
        if (
          handleNaSamplePhase(
            kase.requisition,
            test.libraryQualifications,
            fragment
          )
        ) {
          return;
        }
        addSampleIcons(kase.assayId, test.libraryQualifications, fragment);
        if (samplePhaseComplete(test.libraryPreparations)) {
          if (samplePhasePendingWorkOrQc(test.libraryQualifications)) {
            if (samplePhasePendingWork(test.libraryQualifications)) {
              if (test.libraryQualifications.length) {
                addSpace(fragment);
              }
              addConstructionIcon("library qualification", fragment);
            }
            const targets = getTargets(kase);
            addTurnAroundTimeInfo(
              kase.caseDaysSpent,
              targets.libraryQualificationDays,
              fragment
            );
          }
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        return getSamplePhaseHighlight(
          kase.requisition,
          test.libraryQualifications
        );
      },
    },
    {
      title: "Full-Depth Sequencing",
      child: true,
      addChildContents(test, kase, fragment) {
        if (
          handleNaSamplePhase(
            kase.requisition,
            test.fullDepthSequencings,
            fragment
          )
        ) {
          return;
        }
        addSampleIcons(kase.assayId, test.fullDepthSequencings, fragment);
        if (samplePhaseComplete(test.libraryQualifications)) {
          if (samplePhasePendingWorkOrQc(test.fullDepthSequencings)) {
            if (samplePhasePendingWork(test.fullDepthSequencings)) {
              if (test.fullDepthSequencings.length) {
                addSpace(fragment);
              }
              addConstructionIcon("full-depth sequencing", fragment);
            }
            const targets = getTargets(kase);
            addTurnAroundTimeInfo(
              kase.caseDaysSpent,
              targets.fullDepthSequencingDays,
              fragment
            );
          }
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        return getSamplePhaseHighlight(
          kase.requisition,
          test.fullDepthSequencings
        );
      },
    },
    {
      title: "Analysis Review",
      addParentContents(kase, fragment) {
        if (
          handleNaRequisitionPhase(kase, (x) => x.analysisReviews, fragment)
        ) {
          return;
        }
        const sequencingComplete = kase.tests.every((test) =>
          samplePhaseComplete(test.fullDepthSequencings)
        );
        const targets = getTargets(kase);
        addRequisitionIcons(
          kase,
          (requisition) => requisition.analysisReviews,
          sequencingComplete,
          targets.analysisReviewDays,
          fragment
        );
      },
      getCellHighlight(kase) {
        return getRequisitionPhaseHighlight(
          kase,
          kase.requisition.analysisReviews
        );
      },
    },
    {
      title: "Release Approval",
      addParentContents(kase, fragment) {
        if (
          handleNaRequisitionPhase(kase, (x) => x.releaseApprovals, fragment)
        ) {
          return;
        }
        const analysisReviewComplete = requisitionPhaseComplete(
          kase.requisition.analysisReviews
        );
        const targets = getTargets(kase);
        addRequisitionIcons(
          kase,
          (requisition) => requisition.releaseApprovals,
          analysisReviewComplete,
          targets.releaseApprovalDays,
          fragment
        );
      },
      getCellHighlight(kase) {
        return getRequisitionPhaseHighlight(
          kase,
          kase.requisition.releaseApprovals
        );
      },
    },
    {
      title: "Release",
      addParentContents(kase, fragment) {
        if (handleNaRequisitionPhase(kase, (x) => x.releases, fragment)) {
          return;
        }
        const draftComplete = requisitionPhaseComplete(
          kase.requisition.releaseApprovals
        );
        const targets = getTargets(kase);
        addRequisitionIcons(
          kase,
          (requisition) => requisition.releases,
          draftComplete,
          targets.releaseDays,
          fragment
        );
      },
      getCellHighlight(kase) {
        return getRequisitionPhaseHighlight(kase, kase.requisition.releases);
      },
    },
    {
      title: "Latest Activity",
      sortType: "date",
      addParentContents(kase, fragment) {
        if (!kase.latestActivityDate) {
          return;
        }
        fragment.appendChild(document.createTextNode(kase.latestActivityDate));
      },
    },
  ],
};

export function assertNotNull<Type>(object: Type | null) {
  if (object === null) {
    throw new Error("Unexpected null value");
  }
  return object;
}

export function handleNaSamplePhase(
  requisition: Requisition,
  samples: Sample[],
  fragment: DocumentFragment
) {
  if (requisition.stopped && !samples.length) {
    addNaText(fragment);
    return true;
  } else if (requisition.paused) {
    return false;
  } else {
    return false;
  }
}

export function samplePhaseComplete(samples: Sample[]) {
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

export function samplePhasePendingWork(samples: Sample[]) {
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

function samplePhasePendingWorkOrQc(samples: Sample[]) {
  // pending if there are no samples
  if (!samples.length) {
    return true;
  }
  // pending if any sample needs QC or data review
  if (
    samples.some(
      (sample) => !sample.qcDate || (sample.run && !sample.dataReviewDate)
    )
  ) {
    return true;
  }
  // pending if there are no samples with passed QC and data review (if applicable)
  return !samples.some(
    (sample) => sample.qcPassed && (!sample.run || sample.dataReviewPassed)
  );
}

export function getSamplePhaseHighlight(
  requisition: Requisition,
  samples: Sample[]
) {
  if (samplePhaseComplete(samples)) {
    return null;
  } else if (requisition.stopped) {
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
  } else if (kase.requisition.paused) {
    return false;
  } else {
    return false;
  }
}

function requisitionPhaseComplete(qcs: RequisitionQc[]): boolean {
  return !!(qcs.length && getLatestRequisitionQc(qcs).qcPassed);
}

function getRequisitionPhaseHighlight(kase: Case, qcs: RequisitionQc[]) {
  if (requisitionPhaseComplete(qcs)) {
    return null;
  } else if (kase.requisition.stopped) {
    return qcs.length ? null : "na";
  } else {
    return "warning";
  }
}

function caseComplete(kase: Case) {
  return requisitionPhaseComplete(kase.requisition.releases);
}

export function addSpace(fragment: DocumentFragment) {
  fragment.appendChild(document.createTextNode(" "));
}

export function addConstructionIcon(phase: string, fragment: DocumentFragment) {
  const icon = makeIcon(qcStatuses.construction.icon);
  icon.title = `Pending ${phase}`;
  fragment.appendChild(icon);
}

export function addSampleIcons(
  assayId: number,
  samples: Sample[],
  fragment: DocumentFragment
) {
  samples.forEach((sample, i) => {
    let status = getQcStatus(sample);
    if (status === qcStatuses.passed && sample.assayId !== assayId) {
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

export function makeSampleTooltip(sample: Sample) {
  return (fragment: DocumentFragment) => {
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
          urls.dimsum.run(sample.run.name),
          sample.run.name
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
        urls.dimsum.requisition(sample.requisitionId),
        undefined,
        sample.requisitionName
      );
    }
    if (sample.assayId) {
      const assay = siteConfig.assaysById[sample.assayId];
      addTooltipRow(bottomContainer, "Assay", assay.name);
    }
    fragment.appendChild(topContainer);
    fragment.appendChild(bottomContainer);
  };
}

function addRequisitionIcons(
  kase: Case,
  getQcs: (requisition: Requisition) => RequisitionQc[],
  previousComplete: boolean,
  targetDays: number | null,
  fragment: DocumentFragment
) {
  const qcs = getQcs(kase.requisition);
  if (qcs.length) {
    const status = getRequisitionQcStatus(qcs);
    addRequisitionIcon(kase.requisition, status, fragment);
  } else if (previousComplete) {
    const status = qcStatuses.qc;
    addRequisitionIcon(kase.requisition, status, fragment);
  }
  if (previousComplete && !requisitionPhaseComplete(qcs)) {
    addTurnAroundTimeInfo(kase.caseDaysSpent, targetDays, fragment);
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
  return (fragment: DocumentFragment) => {
    fragment.appendChild(
      makeNameDiv(
        requisition.name,
        urls.miso.requisition(requisition.id),
        urls.dimsum.requisition(requisition.id),
        requisition.name
      )
    );
  };
}

function addTooltipRow(
  container: HTMLElement, // must be a two column grid
  label: string,
  name: string,
  misoUrl?: string,
  dimsumUrl?: string,
  additionalText?: string,
  copyText?: string
) {
  const labelContainer = document.createElement("span");
  labelContainer.innerHTML = `${label}:`;
  const valueContainer = document.createElement("div");
  valueContainer.appendChild(makeNameDiv(name, misoUrl, dimsumUrl, copyText));
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
  const complete = caseComplete(kase);
  return `(${complete ? "Completed in" : "Ongoing"} ${
    kase.caseDaysSpent
  } days)`;
}

function caseOverdue(kase: Case, targets: AssayTargets) {
  return targets.caseDays && kase.caseDaysSpent > targets.caseDays;
}

interface OverdueStep {
  stepLabel: string;
  targetDays: number | null;
}

function getOverdueStep(kase: Case, targets: AssayTargets): OverdueStep | null {
  // Check later steps first to emphasize if we are multiple steps overdue
  if (
    requisitionPhaseBehind(
      kase.requisition.releases,
      kase.caseDaysSpent,
      targets.releaseDays
    )
  ) {
    return {
      stepLabel: "Release",
      targetDays: targets.releaseDays,
    };
  } else if (
    requisitionPhaseBehind(
      kase.requisition.releaseApprovals,
      kase.caseDaysSpent,
      targets.releaseApprovalDays
    )
  ) {
    return {
      stepLabel: "Release approval",
      targetDays: targets.releaseApprovalDays,
    };
  } else if (
    requisitionPhaseBehind(
      kase.requisition.analysisReviews,
      kase.caseDaysSpent,
      targets.analysisReviewDays
    )
  ) {
    return {
      stepLabel: "Analysis review",
      targetDays: targets.analysisReviewDays,
    };
  } else if (
    kase.tests.some((test) =>
      samplePhaseBehind(
        test.fullDepthSequencings,
        kase.caseDaysSpent,
        targets.fullDepthSequencingDays
      )
    )
  ) {
    return {
      stepLabel: "Full-depth sequencing",
      targetDays: targets.fullDepthSequencingDays,
    };
  } else if (
    kase.tests.some((test) =>
      samplePhaseBehind(
        test.libraryQualifications,
        kase.caseDaysSpent,
        targets.libraryQualificationDays
      )
    )
  ) {
    return {
      stepLabel: "Library qualification",
      targetDays: targets.libraryQualificationDays,
    };
  } else if (
    kase.tests.some((test) =>
      samplePhaseBehind(
        test.libraryPreparations,
        kase.caseDaysSpent,
        targets.libraryPreparationDays
      )
    )
  ) {
    return {
      stepLabel: "Library preparation",
      targetDays: targets.libraryPreparationDays,
    };
  } else if (
    kase.tests.some((test) =>
      samplePhaseBehind(
        test.extractions,
        kase.caseDaysSpent,
        targets.extractionDays
      )
    )
  ) {
    return {
      stepLabel: "Extraction",
      targetDays: targets.extractionDays,
    };
  } else if (
    samplePhaseBehind(kase.receipts, kase.caseDaysSpent, targets.receiptDays)
  ) {
    return {
      stepLabel: "Receipt",
      targetDays: targets.receiptDays,
    };
  }
  return null;
}

function samplePhaseBehind(
  samples: Sample[],
  caseDaysSpent: number,
  stepTarget: number | null
) {
  return (
    stepTarget && !samplePhaseComplete(samples) && caseDaysSpent > stepTarget
  );
}

function requisitionPhaseBehind(
  qcs: RequisitionQc[],
  caseDaysSpent: number,
  stepTarget: number | null
) {
  return (
    stepTarget && !requisitionPhaseComplete(qcs) && caseDaysSpent > stepTarget
  );
}

function addTurnAroundTimeInfo(
  daysSpent: number,
  target: number | null,
  fragment: DocumentFragment
) {
  if (!target) {
    return;
  }
  if (daysSpent > target) {
    const tatDiv = makeTextDivWithTooltip("OVERDUE", `Target: ${target} days`);
    styleText(tatDiv, "error");
    fragment.appendChild(tatDiv);
  } else {
    const daysRemaining = target - daysSpent;
    const tatDiv = makeTextDivWithTooltip(
      `${daysRemaining}d remain`,
      `Target: ${target} days`
    );
    fragment.appendChild(tatDiv);
  }
}

function getTargets(kase: Case) {
  return siteConfig.assaysById[kase.assayId].targets;
}
