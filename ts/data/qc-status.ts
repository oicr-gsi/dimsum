import { CellStatus } from "../util/html-utils";

enum qcStatusKeyEnum {
  "construction",
  "analysis",
  "qc",
  "dataReview",
  "passed",
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
  cellStatus?: CellStatus;
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
  analysis: {
    label: "Pending analysis",
    icon: "hourglass",
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
    label: "Passed",
    icon: "check",
    qcComplete: true,
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
