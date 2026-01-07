import { CellStatus } from "../util/html-utils";

enum qcStatusKeyEnum {
  "construction",
  "sequencing",
  "analysis",
  "qc",
  "transfer",
  "dataReview",
  "passed",
  "passedDifferentAssay",
  "staged",
  "failed",
  "topUp",
  "na",
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
  showExternal: boolean;
}

export const qcStatuses: Record<QcStatusKey, QcStatus> = {
  construction: {
    label: "Pending work",
    icon: "road-barrier",
    qcComplete: false,
    cellStatus: "warning",
    priority: 0,
    showExternal: true,
  },
  sequencing: {
    label: "Sequencing in progress",
    icon: "pen-to-square",
    qcComplete: false,
    cellStatus: "warning",
    priority: 2,
    showExternal: false,
  },
  analysis: {
    label: "Pending analysis",
    icon: "hourglass-half",
    qcComplete: false,
    cellStatus: "warning",
    priority: 3,
    showExternal: false,
  },
  qc: {
    label: "Pending QC",
    icon: "magnifying-glass",
    qcComplete: false,
    cellStatus: "warning",
    priority: 4,
    showExternal: false,
  },
  transfer: {
    label: "Pending transfer",
    icon: "right-left",
    qcComplete: true,
    cellStatus: "warning",
    priority: 0,
    showExternal: false,
  },
  dataReview: {
    label: "Pending data review",
    icon: "glasses",
    qcComplete: true,
    cellStatus: "warning",
    priority: 5,
    showExternal: false,
  },
  passed: {
    label: "Approved",
    icon: "check",
    qcComplete: true,
    cellStatus: null,
    priority: 7,
    showExternal: true,
  },
  passedDifferentAssay: {
    label: "Approved with different assay",
    icon: "circle-check",
    qcComplete: true,
    cellStatus: null,
    priority: 7,
    showExternal: false,
  },
  staged: {
    label: "Staged",
    icon: "hard-drive",
    qcComplete: true,
    cellStatus: null,
    priority: 7,
    showExternal: true,
  },
  failed: {
    label: "Failed",
    icon: "xmark",
    qcComplete: true,
    cellStatus: "error",
    priority: 1,
    showExternal: true,
  },
  topUp: {
    label: "Top-up Required",
    icon: "fill-drip",
    qcComplete: true,
    cellStatus: "warning",
    priority: 6,
    showExternal: true,
  },
  na: {
    label: "Not Applicable",
    icon: "ban",
    qcComplete: true,
    cellStatus: null,
    priority: 8,
    showExternal: true,
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
