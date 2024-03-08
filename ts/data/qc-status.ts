import { CellStatus } from "../util/html-utils";

enum qcStatusKeyEnum {
  "construction",
  "sequencing",
  "analysis",
  "qc",
  "dataReview",
  "passed",
  "passedDifferentAssay",
  "failed",
  "topUp",
}

type QcStatusKey = keyof typeof qcStatusKeyEnum;

function isQcStatusKey(str: string): str is QcStatusKey {
  return str in qcStatusKeyEnum;
}

export interface QcStatus {
  label: string;
  icon: string;
  qcComplete: boolean;
  cellStatus: CellStatus | null;
  priority: number; // lower wins when combining multiple statuses (run-libraries)
}

export const qcStatuses: Record<QcStatusKey, QcStatus> = {
  construction: {
    label: "Pending work",
    icon: "road-barrier",
    qcComplete: false,
    cellStatus: "warning",
    priority: 0,
  },
  sequencing: {
    label: "Sequencing in progress",
    icon: "pen-to-square",
    qcComplete: false,
    cellStatus: "warning",
    priority: 2,
  },
  analysis: {
    label: "Pending analysis",
    icon: "hourglass-half",
    qcComplete: false,
    cellStatus: "warning",
    priority: 3,
  },
  qc: {
    label: "Pending QC",
    icon: "magnifying-glass",
    qcComplete: false,
    cellStatus: "warning",
    priority: 4,
  },
  dataReview: {
    label: "Pending data review",
    icon: "glasses",
    qcComplete: true,
    cellStatus: "warning",
    priority: 5,
  },
  passed: {
    label: "Approved",
    icon: "check",
    qcComplete: true,
    cellStatus: null,
    priority: 7,
  },
  passedDifferentAssay: {
    label: "Approved with different assay",
    icon: "circle-check",
    qcComplete: true,
    cellStatus: null,
    priority: 7,
  },
  failed: {
    label: "Failed",
    icon: "xmark",
    qcComplete: true,
    cellStatus: "error",
    priority: 1,
  },
  topUp: {
    label: "Top-up Required",
    icon: "fill-drip",
    qcComplete: true,
    cellStatus: "warning",
    priority: 6,
  },
};

export function getQcStatus(key?: string | null): QcStatus {
  if (key && isQcStatusKey(key)) {
    const goodKey: QcStatusKey = key;
    return qcStatuses[goodKey];
  } else {
    throw new Error(`Invalid QC status key: ${key}`);
  }
}
