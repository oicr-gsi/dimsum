import { TableDefinition } from "../util/table-builder";
import { addLink, makeIcon, styleText, addMisoIcon } from "../util/html-utils";
import { urls } from "../util/urls";
import { siteConfig } from "../util/site-config";
import { getSampleQcStatus, Sample } from "./sample";
import { QcStatus, qcStatuses } from "./qc-status";
import {
  getLatestRequisitionQc,
  getRequisitionQcStatus,
  Requisition,
  RequisitionQc,
} from "./requisition";

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

export interface Run {
  name: string;
}

export interface Test {
  name: string;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  groupId: string;
  targetedSequencing: string;
  extractions: Sample[];
  libraryPreparations: Sample[];
  libraryQualifications: Sample[];
  fullDepthSequencings: Sample[];
  latestActivityDate: string;
}

export interface Case {
  projects: Project[];
  donor: Donor;
  assayName: string;
  assayDescription: string;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  stopped: boolean;
  receipts: Sample[];
  earliestReceiptDate: string;
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
    },
    {
      title: "Donor",
      key: "DONOR",
      type: "text",
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
    },
    {
      title: "Requisition",
      key: "REQUISITION",
      type: "text",
    },
  ],
  getChildren: (parent) => parent.tests,
  getRowHighlight: (kase) => (kase.stopped ? "stopped" : null),
  columns: [
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
        fragment.appendChild(externalNameDiv);

        const tumourDetailDiv = document.createElement("div");
        tumourDetailDiv.appendChild(
          document.createTextNode(
            `${kase.tissueOrigin} ${kase.tissueType}` +
              (kase.timepoint ? " " + kase.timepoint : "")
          )
        );
        fragment.appendChild(tumourDetailDiv);
      },
    },
    {
      title: "Assay",
      sortType: "text",
      addParentContents(kase, fragment) {
        const assayDiv = document.createElement("div");
        addLink(assayDiv, kase.assayName, "#");
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
      title: "First Receipt",
      sortType: "date",
      addParentContents(kase, fragment) {
        const dateDiv = document.createElement("div");
        dateDiv.appendChild(document.createTextNode(kase.earliestReceiptDate));
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

function addNaText(fragment: DocumentFragment) {
  fragment.appendChild(document.createTextNode("N/A"));
}

function addSampleIcons(samples: Sample[], fragment: DocumentFragment) {
  samples.forEach((sample, i) => {
    const status = getSampleQcStatus(sample);
    const icon = makeIcon(status.icon);
    icon.title =
      (sample.run ? sample.run.name + " - " : "") +
      `${sample.name}: ${status.label}`; // TODO: more detailed popup
    fragment.appendChild(icon);
    if (i < samples.length - 1) {
      fragment.appendChild(document.createTextNode(" "));
    }
  });
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
  const icon = makeIcon(status.icon);
  icon.title = `${requisition.name}: ${status.label}`; // TODO: more detailed popup
  fragment.appendChild(icon);
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
  const startDate = new Date(kase.earliestReceiptDate);
  const milliDiff = endDate.getTime() - startDate.getTime();
  const dayDiff = Math.ceil(milliDiff / dayMillis);
  return `(${message} ${dayDiff} days)`;
}
