import { CellStatus, makeIcon, makeNameDiv } from "../util/html-utils";
import {
  ColumnDefinition,
  SortDefinition,
  TableDefinition,
} from "../util/table-builder";
import { urls } from "../util/urls";
import { qcStatuses } from "./qc-status";

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
  latestActivityDate?: string;
}

const defaultSort: SortDefinition = {
  columnTitle: "Latest Activity",
  descending: true,
  type: "date",
};

function qcStatusColumn(
  getQcs: (requisition: Requisition) => RequisitionQc[]
): ColumnDefinition<Requisition, void> {
  return {
    title: "QC Status",
    addParentContents(requisition, fragment) {
      const status = getRequisitionQcStatus(getQcs(requisition));
      const icon = makeIcon(status.icon);
      icon.title = status.label;
      fragment.appendChild(icon);
    },
    getCellHighlight(requisition) {
      const status = getRequisitionQcStatus(getQcs(requisition));
      return status.cellStatus || null;
    },
  };
}

const requisitionColumn: ColumnDefinition<Requisition, void> = {
  title: "Requisition",
  addParentContents(requisition, fragment) {
    fragment.appendChild(
      makeNameDiv(requisition.name, urls.miso.requisition(requisition.id))
    );
  },
  sortType: "text",
};

const latestActivityColumn: ColumnDefinition<Requisition, void> = {
  title: "Latest Activity",
  sortType: "date",
  addParentContents(requisition, fragment) {
    fragment.appendChild(
      document.createTextNode(requisition.latestActivityDate || "")
    );
  },
};

export const informaticsReviewDefinition: TableDefinition<Requisition, void> = {
  queryUrl: urls.rest.requisitions,
  defaultSort: defaultSort,
  columns: [
    qcStatusColumn((requisition) => requisition.informaticsReviews),
    requisitionColumn,
    {
      title: "(Metric Columns)",
      addParentContents(requisition, fragment) {
        addTodoIcon(fragment); // TODO
      },
      getCellHighlight: todoHighlight,
    },
    latestActivityColumn,
  ],
};

export const draftReportDefinition: TableDefinition<Requisition, void> = {
  queryUrl: urls.rest.requisitions,
  defaultSort: defaultSort,
  columns: [
    qcStatusColumn((requisition) => requisition.draftReports),
    requisitionColumn,
    latestActivityColumn,
  ],
};

export const finalReportDefinition: TableDefinition<Requisition, void> = {
  queryUrl: urls.rest.requisitions,
  defaultSort: defaultSort,
  columns: [
    qcStatusColumn((requisition) => requisition.finalReports),
    requisitionColumn,
    latestActivityColumn,
  ],
};

export function getRequisitionQcStatus(qcs: RequisitionQc[]) {
  // return status of latest QC in-case there are multiple
  if (!qcs.length) {
    return qcStatuses.qc;
  }
  const latest = getLatestRequisitionQc(qcs);
  return latest.qcPassed ? qcStatuses.passed : qcStatuses.failed;
}

export function getLatestRequisitionQc(qcs: RequisitionQc[]) {
  return qcs.reduce((previous, current) =>
    previous.qcDate > current.qcDate ? previous : current
  );
}

function addTodoIcon(fragment: DocumentFragment) {
  const icon = makeIcon("person-digging");
  icon.title = "We're working on it!";
  fragment.appendChild(icon);
}

function todoHighlight(): CellStatus {
  return "na";
}
