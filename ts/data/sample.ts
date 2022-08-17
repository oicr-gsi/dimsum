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
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  groupId: string;
  project: string;
  targetedSequencing: string;
  createdDate: string;
  volume?: number;
  concentration?: number;
  run: Run;
  donor: Donor;
  qcPassed: boolean;
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

const nameColumn: ColumnDefinition<Sample, void> = {
  title: "Name",
  addParentContents(sample, fragment) {
    fragment.appendChild(makeNameDiv(sample.name, urls.miso.sample(sample.id)));
    if (sample.run) {
      const runName = sample.run.name;
      fragment.appendChild(
        makeNameDiv(runName, urls.miso.run(runName), urls.dimsum.run(runName))
      );
      // TODO: add Dashi icon link
    }
  },
  sortType: "text",
};

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
    addTodoIcon(fragment); // TODO
  },
  getCellHighlight: todoHighlight,
};

const sequencingAttributesColumn: ColumnDefinition<Sample, void> = {
  title: "Sequencing Attributes",
  addParentContents(sample, fragment) {
    if (sample.run) {
      addTodoIcon(fragment); // TODO
    } else {
      fragment.appendChild(document.createTextNode("N/A"));
    }
  },
  getCellHighlight: todoHighlight,
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
    nameColumn,
    {
      title: "Requisition",
      addParentContents(sample, fragment) {
        addTodoIcon(fragment); // TODO
      },
      getCellHighlight: todoHighlight,
    },
    {
      title: "Secondary ID",
      addParentContents(sample, fragment) {
        addTodoIcon(fragment); // TODO
      },
      getCellHighlight: todoHighlight,
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
    nameColumn,
    tissueAttributesColumn,
    {
      title: "Nucleic Acid Type",
      addParentContents(sample, fragment) {
        addTodoIcon(fragment); // TODO
      },
      getCellHighlight: todoHighlight,
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
    nameColumn,
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
  queryUrl: string
): TableDefinition<Sample, void> {
  return {
    queryUrl: queryUrl,
    defaultSort: defaultSort,
    columns: [
      qcStatusColumn,
      nameColumn,
      tissueAttributesColumn,
      designColumn,
      sequencingAttributesColumn,
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
}

export function getFullDepthSequencingsDefinition(
  queryUrl: string
): TableDefinition<Sample, void> {
  return {
    queryUrl: queryUrl,
    defaultSort: defaultSort,
    columns: [
      qcStatusColumn,
      nameColumn,
      tissueAttributesColumn,
      designColumn,
      sequencingAttributesColumn,
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
