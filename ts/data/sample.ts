import {
  addNaText,
  CellStatus,
  makeIcon,
  makeNameDiv,
} from "../util/html-utils";
import { siteConfig } from "../util/site-config";
import {
  ColumnDefinition,
  SortDefinition,
  TableDefinition,
} from "../component/table-builder";
import { Tooltip } from "../component/tooltip";
import { urls } from "../util/urls";
import { Metric, MetricCategory, MetricSubcategory } from "./assay";
import { Donor, Qcable, Run } from "./case";
import { QcStatus, qcStatuses } from "./qc-status";

export interface Sample extends Qcable {
  id: string;
  name: string;
  requisitionId?: number;
  requisitionName?: string;
  assayId?: number;
  tissueOrigin: string;
  tissueType: string;
  tissueMaterial?: string;
  timepoint?: string;
  secondaryId?: string;
  groupId?: string;
  project: string;
  nucleicAcidType?: string;
  librarySize?: number;
  libraryDesignCode?: string;
  targetedSequencing?: string;
  createdDate: string;
  volume?: number;
  concentration?: number;
  concentrationUnits?: string;
  run?: Run;
  donor: Donor;
  meanInsertSize?: number;
  clustersPerSample?: number; // AKA "Pass Filter Clusters" for full-depth (call ready)
  duplicationRate?: number;
  meanCoverageDeduplicated?: number;
  rRnaContamination?: number;
  mappedToCoding?: number;
  rawCoverage?: number;
  onTargetReads?: number;
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
  generateColumns: function (data?: Sample[]) {
    return [
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
      ...generateMetricColumns("RECEIPT", data),
      latestActivityColumn,
    ];
  },
};

export const extractionDefinition: TableDefinition<Sample, void> = {
  queryUrl: urls.rest.extractions,
  defaultSort: defaultSort,
  generateColumns(data) {
    return [
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
      ...generateMetricColumns("EXTRACTION", data),
      latestActivityColumn,
    ];
  },
};

export const libraryPreparationDefinition: TableDefinition<Sample, void> = {
  queryUrl: urls.rest.libraryPreparations,
  defaultSort: defaultSort,
  generateColumns(data) {
    return [
      makeQcStatusColumn(false),
      makeNameColumn(false),
      tissueAttributesColumn,
      designColumn,
      ...generateMetricColumns("LIBRARY_PREP", data),
      latestActivityColumn,
    ];
  },
};

export function getLibraryQualificationsDefinition(
  queryUrl: string,
  includeSequencingAttributes: boolean
): TableDefinition<Sample, void> {
  return {
    queryUrl: queryUrl,
    defaultSort: defaultSort,
    generateColumns(data) {
      const columns: ColumnDefinition<Sample, void>[] = [
        makeQcStatusColumn(includeSequencingAttributes),
        makeNameColumn(includeSequencingAttributes),
        tissueAttributesColumn,
        designColumn,
        ...generateMetricColumns("LIBRARY_QUALIFICATION", data),
        latestActivityColumn,
      ];
      if (includeSequencingAttributes) {
        columns.splice(4, 0, sequencingAttributesColumn);
      }
      return columns;
    },
  };
}

export function getFullDepthSequencingsDefinition(
  queryUrl: string,
  includeSequencingAttributes: boolean
): TableDefinition<Sample, void> {
  return {
    queryUrl: queryUrl,
    defaultSort: defaultSort,
    generateColumns(data) {
      const columns: ColumnDefinition<Sample, void>[] = [
        makeQcStatusColumn(includeSequencingAttributes),
        makeNameColumn(includeSequencingAttributes),
        tissueAttributesColumn,
        designColumn,
        ...generateMetricColumns("FULL_DEPTH_SEQUENCING", data),
        latestActivityColumn,
      ];
      if (includeSequencingAttributes) {
        columns.splice(4, 0, sequencingAttributesColumn);
      }
      return columns;
    },
  };
}

function generateMetricColumns(
  category: MetricCategory,
  samples?: Sample[]
): ColumnDefinition<Sample, void>[] {
  if (!samples) {
    return [];
  }
  const metricNames = getMetricNames(category, samples);
  return metricNames.map((metricName) => {
    return {
      title: metricName,
      addParentContents(sample, fragment) {
        const metrics = getMatchingMetrics(metricName, category, sample);
        if (!metrics || !metrics.length) {
          addNaText(fragment);
          return;
        }
        if (metrics.every((metric) => metric.thresholdType === "BOOLEAN")) {
          // pass/fail based on QC status
          if (sample.qcPassed) {
            fragment.append(makeStatusIcon("check", "Passed"));
          } else if (sample.qcPassed === false) {
            fragment.append(makeStatusIcon("xmark", "Failed"));
          } else {
            fragment.append(makeStatusIcon("magnifying-glass", "Pending QC"));
          }
          return;
        }
        if (/^Adaptor Contamination/.test(metricName)) {
          fragment.append(
            makeNameDiv("See attachment in MISO", urls.miso.sample(sample.id))
          );
          return;
        }
        const value = getMetricValue(metricName, sample);
        if (value === null) {
          if (
            [
              "Bases Over Q30",
              "Min Clusters (PF)",
              "PhiX Control",
              "Min Reads Delivered (PF)",
            ].includes(metricName)
          ) {
            addTodoIcon(fragment); // TODO: remove after Run Scanner metrics are integrated
          } else {
            if (sample.run) {
              const status = sample.run.completionDate
                ? qcStatuses.analysis
                : qcStatuses.sequencing;
              fragment.appendChild(makeStatusIcon(status.icon, status.label));
            } else {
              fragment.appendChild(makeStatusIcon("question", "Not Found"));
            }
          }
        } else {
          fragment.append(makeMetricDisplay(value, metrics));
        }
      },
      getCellHighlight(sample) {
        const metrics = getMatchingMetrics(metricName, category, sample);
        if (!metrics || !metrics.length) {
          return "na";
        }
        if (metrics.every((metric) => metric.thresholdType === "BOOLEAN")) {
          if (sample.qcPassed) {
            return null;
          } else if (sample.qcPassed === false) {
            return "error";
          } else {
            return "warning";
          }
        }
        if (/^Adaptor Contamination/.test(metricName)) {
          return null;
        }
        const value = getMetricValue(metricName, sample);
        if (value == null) {
          return "warning";
        }
        if (anyFail(value, metrics)) {
          return "error";
        }
        return null;
      },
    };
  });

  function formatMetricValue(value: number, metrics: Metric[]) {
    const metricPlaces = Math.max(
      ...metrics.map((metric) =>
        Math.max(
          countDecimalPlaces(metric.minimum),
          countDecimalPlaces(metric.maximum)
        )
      )
    );
    if (metricPlaces === 0 && Number.isInteger(value)) {
      return formatDecimal(value, 0);
    } else {
      return formatDecimal(value, metricPlaces + 1);
    }
  }

  function formatDecimal(value: number, decimalPlaces?: number) {
    return value.toLocaleString("en-CA", {
      minimumFractionDigits: decimalPlaces,
      maximumFractionDigits: decimalPlaces,
    });
  }

  function formatThreshold(value?: number) {
    if (!value) {
      return "Unknown";
    }
    if (Number.isInteger(value)) {
      return formatDecimal(value, 0);
    } else {
      return formatDecimal(value);
    }
  }

  function getMetricRequirementText(metric: Metric) {
    switch (metric.thresholdType) {
      case "GT":
        return `> ${formatThreshold(metric.minimum)}`;
      case "GE":
        return `>= ${formatThreshold(metric.minimum)}`;
      case "LT":
        return `< ${formatThreshold(metric.maximum)}`;
      case "LE":
        return `<= ${formatThreshold(metric.maximum)}`;
      case "BETWEEN":
        return `Between ${formatThreshold(
          metric.minimum
        )} and ${formatThreshold(metric.maximum)}`;
      default:
        throw new Error(`Unexpected threshold type: ${metric.thresholdType}`);
    }
  }

  function makeMetricDisplay(value: number, metrics: Metric[]) {
    const displayValue = formatMetricValue(value, metrics);
    const span = document.createElement("span");
    span.innerText = displayValue;
    const tooltipFragment = document.createDocumentFragment();
    metrics.forEach((metric) => {
      const div = document.createElement("div");
      div.innerText = `Required: ${getMetricRequirementText(metric)}${
        metric.units || ""
      }`;
      tooltipFragment.appendChild(div);
    });
    const tooltip = Tooltip.getInstance();
    tooltip.addTarget(span, tooltipFragment);
    return span;
  }

  function anyFail(value: number, metrics: Metric[]): boolean {
    for (let i = 0; i < metrics.length; i++) {
      switch (metrics[i].thresholdType) {
        case "LT": {
          const max = metrics[i].maximum;
          if (max !== undefined && max !== null) {
            if (value >= max) {
              return true;
            }
          }
          break;
        }
        case "LE": {
          const max = metrics[i].maximum;
          if (max !== undefined && max !== null) {
            if (value > max) {
              return true;
            }
          }
          break;
        }
        case "GT": {
          const min = metrics[i].minimum;
          if (min !== undefined && min !== null) {
            if (value <= min) {
              return true;
            }
          }
          break;
        }
        case "GE": {
          const min = metrics[i].minimum;
          if (min !== undefined && min !== null) {
            if (value < min) {
              return true;
            }
          }
          break;
        }
        case "BETWEEN": {
          const min = metrics[i].minimum;
          if (min !== undefined && min !== null) {
            if (value < min) {
              return true;
            }
          }
          const max = metrics[i].maximum;
          if (max !== undefined && max !== null) {
            if (value > max) {
              return true;
            }
          }
          break;
        }
        default:
          throw new Error(
            `Unexpected threshold type: ${metrics[i].thresholdType}`
          );
      }
    }
    return false;
  }

  function countDecimalPlaces(num?: number) {
    if (!num) {
      return 0;
    }
    const string = num + "";
    if (!string.includes(".")) {
      return 0;
    }
    const i = string.indexOf(".");
    return string.length - i - 1;
  }

  function getMatchingMetrics(
    metricName: string,
    category: MetricCategory,
    sample: Sample
  ): Metric[] | null {
    if (!sample.assayId) {
      return null;
    }
    return siteConfig.assaysById[sample.assayId].metricCategories[category]
      .filter((subcategory) => subcategoryApplies(subcategory, sample))
      .flatMap((subcategory) => subcategory.metrics)
      .filter(
        (metric) => metric.name === metricName && metricApplies(metric, sample)
      );
  }
}

function subcategoryApplies(
  subcategory: MetricSubcategory,
  sample: Sample
): boolean {
  if (
    subcategory.libraryDesignCode &&
    subcategory.libraryDesignCode !== sample.libraryDesignCode
  ) {
    return false;
  }
  return true;
}

function metricApplies(metric: Metric, sample: Sample): boolean {
  if (
    metric.tissueMaterial &&
    sample.tissueMaterial !== metric.tissueMaterial
  ) {
    return false;
  }
  if (metric.tissueOrigin && metric.tissueOrigin !== sample.tissueOrigin) {
    return false;
  }
  if (metric.tissueType) {
    if (metric.negateTissueType) {
      if (metric.tissueType === sample.tissueType) {
        return false;
      }
    } else if (metric.tissueType !== sample.tissueType) {
      return false;
    }
  }
  if (
    metric.nucleicAcidType &&
    metric.nucleicAcidType !== sample.nucleicAcidType
  ) {
    return false;
  }
  if (metric.containerModel) {
    if (!sample.run || sample.run.containerModel !== metric.containerModel) {
      return false;
    }
  }
  if (metric.readLength) {
    if (
      !sample.run ||
      !sample.run.readLength ||
      Math.abs(metric.readLength - sample.run.readLength) > 1
    ) {
      return false;
    } else if (
      metric.readLength2 &&
      (!sample.run.readLength2 ||
        Math.abs(metric.readLength2 - sample.run.readLength2) > 1)
    ) {
      return false;
    }
  }
  if (metric.name === "Concentration (Qubit)") {
    switch (metric.units) {
      case "ng/\u03bcL":
        if (sample.concentrationUnits !== "NANOGRAMS_PER_MICROLITRE") {
          return false;
        }
        break;
      case "nM":
        if (sample.concentrationUnits !== "NANOMOLAR") {
          return false;
        }
        break;
    }
  }
  return true;
}

function getMetricNames(category: MetricCategory, samples: Sample[]): string[] {
  const assays = samples
    .map((sample) => sample.assayId)
    .filter((assayId) => assayId)
    .filter(unique)
    .map((assayId) => {
      if (!assayId) {
        throw new Error("Unexpected error (undefined should be filtered)");
      }
      return siteConfig.assaysById[assayId];
    });

  const subcategories = assays
    .filter(
      (assay) =>
        assay.metricCategories[category] &&
        assay.metricCategories[category].length
    )
    .flatMap((assay) => assay.metricCategories[category]);

  const subcategoryNames = subcategories
    .sort(byPriority)
    .map((subcategory) => subcategory.name || "")
    .filter(unique);

  const metricNames: string[] = [];
  subcategoryNames
    .map((subcategoryName) =>
      // get all metrics from all matching subcategories
      subcategories
        .filter((subcategory) => (subcategory.name || "") === subcategoryName)
        .flatMap((subcategory) => subcategory.metrics)
    )
    .forEach((metrics) => {
      metrics.sort(byPriority).forEach((metric) => {
        if (!metricNames.includes(metric.name)) {
          metricNames.push(metric.name);
        }
      });
    });

  return metricNames;
}

function unique(item: any, index: number, arr: any[]) {
  return arr.indexOf(item) === index;
}

function byPriority(
  a: MetricSubcategory | Metric,
  b: MetricSubcategory | Metric
) {
  const sortPriorityA = a.sortPriority || -1;
  const sortPriorityB = b.sortPriority || -1;
  return sortPriorityA - sortPriorityB;
}

function getMetricValue(metricName: string, sample: Sample): number | null {
  switch (metricName) {
    case "Appropriate volume":
      return nullIfUndefined(sample.volume);
    case "Yield":
      return sample.volume && sample.concentration
        ? sample.volume * sample.concentration
        : null;
    case "Mean Insert Size":
      return nullIfUndefined(sample.meanInsertSize);
    case "Duplication Rate":
      return nullIfUndefined(sample.duplicationRate);
    case "Clusters Per Sample":
    case "Pass Filter Clusters":
      return nullIfUndefined(sample.clustersPerSample);
    case "rRNA Contamination":
      return nullIfUndefined(sample.rRnaContamination);
    case "Mean Coverage Deduplicated":
      return nullIfUndefined(sample.meanCoverageDeduplicated);
    case "Coverage (Raw)":
      return nullIfUndefined(sample.rawCoverage);
    case "Mapped to Coding":
      return nullIfUndefined(sample.mappedToCoding);
    case "Quantitative PCR (qPCR)":
      // Must be in nM
      if (sample.concentrationUnits === "NANOMOLAR") {
        return nullIfUndefined(sample.concentration);
      } else {
        return null;
      }
    case "On Target Reads":
      return nullIfUndefined(sample.onTargetReads);
  }
  if (/^Concentration/.test(metricName)) {
    return nullIfUndefined(sample.concentration);
  } else if (/^Avg Size Distribution/.test(metricName)) {
    return nullIfUndefined(sample.librarySize);
  }
  if (sample.run) {
    switch (metricName) {
      case "Bases Over Q30":
        return null; // TODO: from Run Scanner
      case "Min Clusters (PF)":
        return null; // TODO: from Run Scanner
      case "PhiX Control":
        return null; // TODO: from Run Scanner
      case "Min Reads Delivered (PF)":
        return null; // TODO: from Run Scanner
    }
  }
  return null;
}
function nullIfUndefined(value: any) {
  return value === undefined ? null : value;
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
  const icon = makeStatusIcon("person-digging", "We're working on it!");
  fragment.appendChild(icon);
}

function makeStatusIcon(iconName: string, statusText: string) {
  const icon = makeIcon(iconName);
  const tooltip = Tooltip.getInstance();
  tooltip.addTarget(icon, document.createTextNode(statusText));
  return icon;
}
