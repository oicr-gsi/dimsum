import { TableDefinition } from "../util/table-builder";
import * as Rest from "../util/rest-api";
import { addLink } from "../util/html-utils";

export interface Project {
  name: string;
}

export interface Donor {
  id: number;
  name: string;
  externalName: string;
}

export interface Run {
  name: string;
}

export interface Sample {
  id: number;
  name: string;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  groupId: string;
  targetedSequencing: string;
  createdDate: string;
  run: Run;
  qcPassed: boolean;
  qcReason: string;
  qcUser: string;
  qcDate: string;
  dataReviewPassed: boolean;
  dataReviewUser: string;
  dataReviewDate: string;
  latestActivityDate: string;
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

export interface RequisitionQc {
  qcPassed: boolean;
  qcUser: string;
  qcDate: string;
}

export interface Requisition {
  id: number;
  name: string;
  informaticsReviews: RequisitionQc[];
  draftReports: RequisitionQc[];
  finalReports: RequisitionQc[];
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
  queryUrl: Rest.cases.query,
  getChildren: (parent) => parent.tests,
  columns: [
    {
      title: "Project",
      addParentContents(kase, fragment) {
        kase.projects.forEach((project) => {
          const div = document.createElement("div");
          addLink(div, project.name, "#");
          fragment.appendChild(div);
        });
      },
    },
    {
      title: "Donor",
      addParentContents(kase, fragment) {
        const nameDiv = document.createElement("div");
        addLink(nameDiv, kase.donor.name, "#");
        fragment.appendChild(nameDiv);
        const externalNameDiv = document.createElement("div");
        externalNameDiv.appendChild(
          document.createTextNode(kase.donor.externalName)
        );
        fragment.appendChild(externalNameDiv);
      },
    },
    {
      title: "Assay",
      addParentContents(kase, fragment) {
        fragment.appendChild(document.createTextNode(kase.assayName));
      },
    },
    {
      title: "First Receipt",
      addParentContents(kase, fragment) {
        fragment.appendChild(document.createTextNode(kase.earliestReceiptDate));
      },
    },
    {
      title: "Receipt/Inspection",
      addParentContents(kase, fragment) {
        addSampleIcons(kase.receipts, fragment);
      },
    },
    {
      title: "Test",
      child: true,
      addChildContents(test, kase, fragment) {
        fragment.appendChild(document.createTextNode(test.name));
      },
    },
    {
      title: "Extraction",
      child: true,
      addChildContents(test, kase, fragment) {
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
    },
    {
      title: "Library Preparation",
      child: true,
      addChildContents(test, kase, fragment) {
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
    },
    {
      title: "Library Qualification",
      child: true,
      addChildContents(test, kase, fragment) {
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
    },
    {
      title: "Full-Depth Sequencing",
      child: true,
      addChildContents(test, kase, fragment) {
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
    },
    {
      title: "Informatics Review",
      addParentContents(kase, fragment) {
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
    },
    {
      title: "Draft Report",
      addParentContents(kase, fragment) {
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
    },
    {
      title: "Final Report",
      addParentContents(kase, fragment) {
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
    },
    {
      title: "Latest Activity",
      addParentContents(kase, fragment) {
        fragment.appendChild(document.createTextNode(kase.latestActivityDate));
      },
    },
  ],
};

function samplePhaseComplete(samples: Sample[]) {
  // consider incomplete if any are pending QC or data review
  if (
    samples.some(
      (sample) => !sample.qcDate || (sample.run && !sample.dataReviewDate)
    )
  ) {
    return false;
  } else {
    // consider complete if at least one is passed QC
    return samples.some((sample) => sample.qcPassed);
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

function requisitionPhaseComplete(
  requisitions: Requisition[],
  getQcs: (requisition: Requisition) => RequisitionQc[]
) {
  return requisitions.every((requisition) => {
    const qcs = getQcs(requisition);
    return qcs.length && getLatestRequisitionQc(qcs).qcPassed;
  });
}

function addSpace(fragment: DocumentFragment) {
  fragment.appendChild(document.createTextNode(" "));
}

function addConstructionIcon(phase: string, fragment: DocumentFragment) {
  const icon = makeIcon(statuses.construction);
  icon.title = `Pending ${phase}`;
  fragment.appendChild(icon);
}

interface QcStatus {
  label: string;
  icon: string;
  qcComplete: boolean;
}

type QcStatusKey =
  | "construction"
  | "analysis"
  | "qc"
  | "dataReview"
  | "passed"
  | "failed"
  | "topUp";

const statuses: Record<QcStatusKey, QcStatus> = {
  construction: {
    label: "Pending work",
    icon: "road-barrier",
    qcComplete: false,
  },
  analysis: {
    label: "Pending analysis",
    icon: "hourglass",
    qcComplete: false,
  },
  qc: {
    label: "Pending QC",
    icon: "magnifying-glass",
    qcComplete: false,
  },
  dataReview: {
    label: "Pending data review",
    icon: "glasses",
    qcComplete: true,
  },
  passed: {
    label: "Passed",
    icon: "check",
    qcComplete: true,
  },
  failed: {
    label: "Failed",
    icon: "x",
    qcComplete: true,
  },
  topUp: {
    label: "Top-up required",
    icon: "fill-drip",
    qcComplete: true,
  },
};

function addSampleIcons(samples: Sample[], fragment: DocumentFragment) {
  samples.forEach((sample, i) => {
    const status = getSampleStatus(sample);
    const icon = makeIcon(status);
    icon.title =
      (sample.run ? sample.run.name + " - " : "") +
      `${sample.name}: ${status.label}`; // TODO: more detailed popup
    fragment.appendChild(icon);
    if (i < samples.length - 1) {
      fragment.appendChild(document.createTextNode(" "));
    }
  });
}

function makeIcon(status: QcStatus) {
  const icon = document.createElement("i");
  icon.className = `fa-solid fa-${status.icon}`;
  return icon;
}

function getSampleStatus(sample: Sample): QcStatus {
  const firstStatus = getFirstReviewStatus(sample);
  if (sample.run && firstStatus.qcComplete) {
    // run-libraries also have data review
    if (!sample.dataReviewDate) {
      return statuses.dataReview;
    } else if (sample.dataReviewPassed === false) {
      return statuses.failed;
    }
    // if data review is passed, first sign-off status is used
  }
  return firstStatus;
}

function getFirstReviewStatus(sample: Sample) {
  if (sample.qcPassed === false) {
    return statuses.failed;
  } else if (sample.qcPassed === true) {
    return statuses.passed;
  } else if (sample.qcReason === "Top-up Required") {
    return statuses.topUp;
  } else {
    return statuses.qc;
  }
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
      const status = getRequisitionQcStatus(qcs, previousComplete);
      addRequisitionIcon(requisition, status, fragment);
    } else if (previousComplete) {
      const status = statuses.qc;
      addRequisitionIcon(requisition, status, fragment);
    }
  });
}

function getRequisitionQcStatus(
  qcs: RequisitionQc[],
  previousComplete: boolean
) {
  // return status of latest QC in-case there are multiple
  if (!qcs.length) {
    return statuses.qc;
  }
  const latest = getLatestRequisitionQc(qcs);
  return latest.qcPassed ? statuses.passed : statuses.failed;
}

function getLatestRequisitionQc(qcs: RequisitionQc[]) {
  return qcs.reduce((previous, current) =>
    previous.qcDate > current.qcDate ? previous : current
  );
}

function addRequisitionIcon(
  requisition: Requisition,
  status: QcStatus,
  fragment: DocumentFragment
) {
  const icon = makeIcon(status);
  icon.title = `${requisition.name}: ${status.label}`; // TODO: more detailed popup
  fragment.appendChild(icon);
}
