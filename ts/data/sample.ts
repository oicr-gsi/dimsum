import { CellStatus, makeIcon, makeNameDiv } from "../util/html-utils";
import {
  ColumnDefinition,
  SortDefinition,
  TableDefinition,
} from "../util/table-builder";
import { urls } from "../util/urls";
import { Donor, Qcable, Run } from "./case";
import { QcStatus, qcStatuses } from "./qc-status";

export interface Sample extends Qcable {
  id: string;
  name: string;
  requisitionId?: number;
  requisitionName?: string;
  tissueOrigin: string;
  tissueType: string;
  timepoint?: string;
  secondaryId?: string;
  groupId?: string;
  project: string;
  nucleicAcidType?: string;
  libraryDesignCode?: string;
  targetedSequencing?: string;
  createdDate: string;
  volume?: number;
  concentration?: number;
  run?: Run;
  donor: Donor;
  latestActivityDate: string;
}

const defaultSort: SortDefinition = {
  columnTitle: "Latest Activity",
  descending: true,
  type: "date",
};

function makeQcStatusColumn(
  includeRun: boolean
): ColumnDefinition<Sample, void> {
  const getStatus = includeRun ? getQcStatus : getSampleQcStatus;
  return {
    title: "QC Status",
    addParentContents(sample, fragment) {
      const status = getStatus(sample);
      const icon = makeIcon(status.icon);
      icon.title = status.label;
      fragment.appendChild(icon);
    },
    getCellHighlight(sample) {
      const status = getStatus(sample);
      return status.cellStatus || null;
    },
  };
}

function makeNameColumn(includeRun: boolean): ColumnDefinition<Sample, void> {
  return {
    title: "Name",
    addParentContents(sample, fragment) {
      fragment.appendChild(
        makeNameDiv(sample.name, urls.miso.sample(sample.id))
      );
      if (includeRun && sample.run) {
        const runName = sample.run.name;
        fragment.appendChild(
          makeNameDiv(runName, urls.miso.run(runName), urls.dimsum.run(runName))
        );
        // TODO: add Dashi icon link
      }
    },
    sortType: "text",
  };
}

const tissueAttributesColumn: ColumnDefinition<Sample, void> = {
  title: "Tissue Attributes",
  addParentContents(sample, fragment) {
    const tumourDetailDiv = document.createElement("div");
    tumourDetailDiv.appendChild(
      document.createTextNode(
        `${sample.tissueOrigin} ${sample.tissueType}` +
          (sample.timepoint ? " " + sample.timepoint : "")
      )
    );
    fragment.appendChild(tumourDetailDiv);
  },
};

const designColumn: ColumnDefinition<Sample, void> = {
  title: "Design",
  addParentContents(sample, fragment) {
    if (sample.libraryDesignCode) {
      fragment.append(document.createTextNode(sample.libraryDesignCode));
    }
  },
};

const sequencingAttributesColumn: ColumnDefinition<Sample, void> = {
  title: "Sequencing Attributes",
  addParentContents(sample, fragment) {
    if (sample.run) {
      const flowCellContainer = document.createElement("div");
      flowCellContainer.appendChild(
        document.createTextNode(
          `Flow cell: ${sample.run.containerModel || "unknown"}`
        )
      );
      fragment.appendChild(flowCellContainer);
      const parametersContainer = document.createElement("div");
      parametersContainer.appendChild(
        document.createTextNode(
          `Parameters: ${sample.run.sequencingParameters || "unknown"}`
        )
      );
      fragment.appendChild(parametersContainer);
    } else {
      fragment.appendChild(document.createTextNode("N/A"));
    }
  },
};

const latestActivityColumn: ColumnDefinition<Sample, void> = {
  title: "Latest Activity",
  sortType: "date",
  addParentContents(sample, fragment) {
    fragment.appendChild(document.createTextNode(sample.latestActivityDate));
  },
};

export const receiptDefinition: TableDefinition<Sample, void> = {
  queryUrl: urls.rest.receipts,
  defaultSort: defaultSort,
  columns: [
    makeQcStatusColumn(false),
    makeNameColumn(false),
    {
      title: "Requisition",
      addParentContents(sample, fragment) {
        if (sample.requisitionId && sample.requisitionName) {
          fragment.appendChild(
            makeNameDiv(
              sample.requisitionName,
              urls.miso.requisition(sample.requisitionId),
              urls.dimsum.requisition(sample.requisitionId)
            )
          );
        }
      },
    },
    {
      title: "Secondary ID",
      addParentContents(sample, fragment) {
        if (sample.secondaryId) {
          fragment.append(document.createTextNode(sample.secondaryId));
        }
      },
    },
    tissueAttributesColumn,
    {
      title: "(Metric Columns)",
      addParentContents(sample, fragment) {
        addTodoIcon(fragment); // TODO
      },
      getCellHighlight: todoHighlight,
    },
    latestActivityColumn,
  ],
};

export const extractionDefinition: TableDefinition<Sample, void> = {
  queryUrl: urls.rest.extractions,
  defaultSort: defaultSort,
  columns: [
    makeQcStatusColumn(false),
    makeNameColumn(false),
    tissueAttributesColumn,
    {
      title: "Nucleic Acid Type",
      addParentContents(sample, fragment) {
        if (sample.nucleicAcidType) {
          fragment.append(document.createTextNode(sample.nucleicAcidType));
        }
      },
    },
    {
      title: "(Metric Columns)",
      addParentContents(sample, fragment) {
        addTodoIcon(fragment); // TODO
      },
      getCellHighlight: todoHighlight,
    },
    latestActivityColumn,
  ],
};

export const libraryPreparationDefinition: TableDefinition<Sample, void> = {
  queryUrl: urls.rest.libraryPreparations,
  defaultSort: defaultSort,
  columns: [
    makeQcStatusColumn(false),
    makeNameColumn(false),
    tissueAttributesColumn,
    designColumn,
    {
      title: "(Metric Columns)",
      addParentContents(sample, fragment) {
        addTodoIcon(fragment); // TODO
      },
      getCellHighlight: todoHighlight,
    },
    latestActivityColumn,
  ],
};

export function getLibraryQualificationsDefinition(
  queryUrl: string,
  includeSequencingAttributes: boolean
): TableDefinition<Sample, void> {
  const columns: ColumnDefinition<Sample, void>[] = [
    makeQcStatusColumn(includeSequencingAttributes),
    makeNameColumn(includeSequencingAttributes),
    tissueAttributesColumn,
    designColumn,
    {
      title: "(Metric Columns)",
      addParentContents(sample, fragment) {
        addTodoIcon(fragment); // TODO
      },
      getCellHighlight: todoHighlight,
    },
    latestActivityColumn,
  ];
  if (includeSequencingAttributes) {
    columns.splice(4, 0, sequencingAttributesColumn);
  }
  return {
    queryUrl: queryUrl,
    defaultSort: defaultSort,
    columns: columns,
  };
}

export function getFullDepthSequencingsDefinition(
  queryUrl: string,
  includeSequencingAttributes: boolean
): TableDefinition<Sample, void> {
  const columns: ColumnDefinition<Sample, void>[] = [
    makeQcStatusColumn(includeSequencingAttributes),
    makeNameColumn(includeSequencingAttributes),
    tissueAttributesColumn,
    designColumn,
    {
      title: "(Metric Columns)",
      addParentContents(sample, fragment) {
        addTodoIcon(fragment); // TODO
      },
      getCellHighlight: todoHighlight,
    },
    latestActivityColumn,
  ];
  if (includeSequencingAttributes) {
    columns.splice(4, 0, sequencingAttributesColumn);
  }
  return {
    queryUrl: queryUrl,
    defaultSort: defaultSort,
    columns: columns,
  };
}

export function getQcStatus(sample: Sample): QcStatus {
  const sampleStatus = getSampleQcStatus(sample);
  if (sample.run) {
    const runStatus = getRunQcStatus(sample.run);
    return runStatus.priority < sampleStatus.priority
      ? runStatus
      : sampleStatus;
  } else {
    return sampleStatus;
  }
}

export function getSampleQcStatus(sample: Sample): QcStatus {
  // TODO: run vs sample QC.. add "priority" to qcStatuses?
  const firstStatus = getFirstReviewStatus(sample);
  if (sample.run && firstStatus.qcComplete) {
    // run-libraries also have data review
    if (!sample.dataReviewDate) {
      return qcStatuses.dataReview;
    } else if (sample.dataReviewPassed === false) {
      return qcStatuses.failed;
    }
    // if data review is passed, first sign-off status is used
  }
  return firstStatus;
}

export function getRunQcStatus(run: Qcable): QcStatus {
  const firstStatus = getFirstReviewStatus(run);
  if (firstStatus.qcComplete) {
    if (!run.dataReviewDate) {
      return qcStatuses.dataReview;
    } else if (run.dataReviewPassed === false) {
      return qcStatuses.failed;
    }
    // if data review is passed, first sign-off status is used
  }
  return firstStatus;
}

function getFirstReviewStatus(qcable: Qcable) {
  if (qcable.qcPassed === false) {
    return qcStatuses.failed;
  } else if (qcable.qcPassed === true) {
    return qcStatuses.passed;
  } else if (qcable.qcReason === "Top-up Required") {
    return qcStatuses.topUp;
  } else {
    return qcStatuses.qc;
  }
}

function addTodoIcon(fragment: DocumentFragment) {
  const icon = makeIcon("person-digging");
  icon.title = "We're working on it!";
  fragment.appendChild(icon);
}

function todoHighlight(): CellStatus {
  return "na";
}
