import { CellStatus } from "../util/html-utils";

type QcStatusKey =
  | "construction"
  | "analysis"
  | "qc"
  | "dataReview"
  | "passed"
  | "failed"
  | "topUp";

export interface QcStatus {
  label: string;
  icon: string;
  qcComplete: boolean;
  cellStatus?: CellStatus;
}

export const qcStatuses: Record<QcStatusKey, QcStatus> = {
  construction: {
    label: "Pending work",
    icon: "road-barrier",
    qcComplete: false,
    cellStatus: "warning",
  },
  analysis: {
    label: "Pending analysis",
    icon: "hourglass",
    qcComplete: false,
    cellStatus: "warning",
  },
  qc: {
    label: "Pending QC",
    icon: "magnifying-glass",
    qcComplete: false,
    cellStatus: "warning",
  },
  dataReview: {
    label: "Pending data review",
    icon: "glasses",
    qcComplete: true,
    cellStatus: "warning",
  },
  passed: {
    label: "Passed",
    icon: "check",
    qcComplete: true,
  },
  failed: {
    label: "Failed",
    icon: "xmark",
    qcComplete: true,
    cellStatus: "error",
  },
  topUp: {
    label: "Top-up Required",
    icon: "fill-drip",
    qcComplete: true,
    cellStatus: "warning",
  },
};
