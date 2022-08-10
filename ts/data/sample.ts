import {
  addMisoIcon,
  CellStatus,
  makeIcon,
  makeNameDiv,
} from "../util/html-utils";
import {
  ColumnDefinition,
  SortDefinition,
  TableDefinition,
} from "../util/table-builder";
import { urls } from "../util/urls";
import { Donor, Run } from "./case";
import { QcStatus, qcStatuses } from "./qc-status";

export interface Sample {
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
  qcPassed?: boolean;
  qcReason: string;
  qcUser: string;
  qcDate: string;
  dataReviewPassed: boolean;
  dataReviewUser: string;
  dataReviewDate: string;
  latestActivityDate: string;
}

const defaultSort: SortDefinition = {
  columnTitle: "Latest Activity",
  descending: true,
  type: "date",
};

const qcStatusColumn: ColumnDefinition<Sample, void> = {
  title: "QC Status",
  addParentContents(sample, fragment) {
    const status = getSampleQcStatus(sample);
    const icon = makeIcon(status.icon);
    icon.title = status.label;
    fragment.appendChild(icon);
  },
  getCellHighlight(sample) {
    const status = getSampleQcStatus(sample);
    return status.cellStatus || null;
  },
};

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
    qcStatusColumn,
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
    qcStatusColumn,
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
    qcStatusColumn,
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
    qcStatusColumn,
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
    qcStatusColumn,
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

export function getSampleQcStatus(sample: Sample): QcStatus {
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

function getFirstReviewStatus(sample: Sample) {
  if (sample.qcPassed === false) {
    return qcStatuses.failed;
  } else if (sample.qcPassed === true) {
    return qcStatuses.passed;
  } else if (sample.qcReason === "Top-up Required") {
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
