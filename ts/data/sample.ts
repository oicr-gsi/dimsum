import {
  addNaText,
  addTextDiv,
  CellStatus,
  makeIcon,
  makeNameDiv,
  makeTextDiv,
} from "../util/html-utils";
import {
  ColumnDefinition,
  legendAction,
  TableDefinition,
} from "../component/table-builder";
import { Tooltip } from "../component/tooltip";
import { urls } from "../util/urls";
import { Metric, MetricCategory, MetricSubcategory } from "./assay";
import { addStatusTooltipText, Donor, Qcable, Run } from "./case";
import { QcStatus, qcStatuses } from "./qc-status";
import {
  addMetricRequirementText,
  anyFail,
  formatMetricValue,
  getBooleanMetricHighlight,
  getBooleanMetricValueIcon,
  getDivisor,
  getDivisorUnit,
  getMetricNames,
  makeMetricDisplay,
  makeNotFoundIcon,
  makeStatusIcon,
} from "../util/metrics";
import {
  showDownloadOptionsDialog,
  showErrorDialog,
} from "../component/dialog";
import {
  caseFilters,
  latestActivitySort,
  runLibraryFilters,
} from "../component/table-components";
import { postDownload, postNavigate } from "../util/requests";
import {
  assertDefined,
  assertRequired,
  nullIfUndefined,
  nullOrUndefined,
} from "./data-utils";
import { getMetricCategory, internalUser } from "../util/site-config";

const METRIC_LABEL_Q30 = "Bases Over Q30";
const METRIC_LABEL_CLUSTERS_PF_1 = "Min Clusters (PF)";
const METRIC_LABEL_CLUSTERS_PF_2 = "Min Reads Delivered (PF)";
const METRIC_LABEL_PHIX = "PhiX Control";
const METRIC_LABELS_CLUSTERS = [
  "Clusters Per Sample",
  "Pass Filter Clusters",
  "Total Clusters (Passed Filter)",
  "Pipeline Filtered Clusters",
];
const METRIC_LABEL_COVERAGE = "Mean Coverage Deduplicated";
export const RUN_METRIC_LABELS = [
  METRIC_LABEL_Q30,
  METRIC_LABEL_CLUSTERS_PF_1,
  METRIC_LABEL_CLUSTERS_PF_2,
  METRIC_LABEL_PHIX,
];

type ThresholdType = "BOOLEAN" | "LT" | "LE" | "GT" | "GE" | "BETWEEN";
type MetricLevel = "SAMPLE" | "RUN" | "LANE";

export interface SampleMetric {
  // minimal fields implemented for now; others commented
  name: string;
  thresholdType: ThresholdType;
  // minimum: number | null;
  // maximum: number | null;
  metricLevel: MetricLevel;
  preliminary: boolean | null;
  value: number | null;
  // laneValues:
  qcPassed: boolean | null;
  // units: string | null;
}

export interface Sample extends Qcable {
  id: string;
  name: string;
  requisitionId: number | null;
  requisitionName: string | null;
  assayIds: number[];
  tissueOrigin: string;
  tissueType: string;
  tissueMaterial: string | null;
  timepoint: string | null;
  secondaryId: string | null;
  groupId: string | null;
  project: string;
  nucleicAcidType: string | null;
  librarySize?: number | null;
  libraryDesignCode: string | null;
  dv200?: number | null;
  targetedSequencing?: string | null;
  createdDate: string;
  volume?: number;
  concentration?: number;
  concentrationUnits?: string;
  run: Run | null;
  donor: Donor;
  transferDate?: string | null;
  meanInsertSize?: number | null;
  medianInsertSize?: number | null;
  clustersPerSample?: number | null;
  preliminaryClustersPerSample?: number | null;
  duplicationRate?: number | null;
  meanCoverageDeduplicated?: number | null;
  preliminaryMeanCoverageDeduplicated?: number | null;
  rRnaContamination?: number | null;
  mappedToCoding?: number | null;
  rawCoverage?: number | null;
  onTargetReads?: number | null;
  collapsedCoverage?: number | null;
  lambdaMethylation?: number | null;
  lambdaClusters?: number | null;
  puc19Methylation?: number | null;
  puc19Clusters?: number | null;
  latestActivityDate: string;
  sequencingLane: string | null;
  relativeCpgInRegions?: number | null;
  methylationBeta?: number | null;
  peReads?: number | null;
  metrics: SampleMetric[];
}

interface MisoRunLibraryMetric {
  title: string;
  threshold_type: string;
  threshold: number;
  threshold_2?: number;
  value: number | null;
}

interface MisoRunLibrary {
  name: string;
  run_id: number;
  partition: number;
  metrics: MisoRunLibraryMetric[];
}

interface QcInMisoRequest {
  report: string;
  library_aliquots: MisoRunLibrary[];
}

function makeQcStatusColumn(
  includeRun: boolean
): ColumnDefinition<Sample, void> {
  const getStatus = includeRun ? getQcStatus : getSampleQcStatus;
  return {
    title: "QC Status",
    sortType: "custom",
    addParentContents(sample, fragment) {
      const status = getStatus(sample);
      const icon = makeIcon(status.icon);
      const tooltipInstance = Tooltip.getInstance();
      tooltipInstance.addTarget(icon, (tooltip) => {
        addStatusTooltipText(
          tooltip,
          status,
          sample.qcReason,
          sample.qcUser,
          sample.qcNote
        );
      });
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
      const name =
        sample.name +
        (!includeRun && sample.sequencingLane
          ? " (L" + sample.sequencingLane + ")"
          : "");
      fragment.appendChild(
        makeNameDiv(name, urls.miso.sample(sample.id), undefined, sample.name)
      );
      if (includeRun && sample.run) {
        const runName = sample.run.name;
        fragment.appendChild(
          makeNameDiv(
            sample.sequencingLane
              ? runName + " (L" + sample.sequencingLane + ")"
              : runName,
            urls.miso.run(runName),
            internalUser ? urls.dimsum.run(runName) : undefined,
            runName
          )
        );
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
  getCellHighlight(sample) {
    return sample.run ? null : "na";
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
  getDefaultSort: () => latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
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
  getDefaultSort: () => latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
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
  getDefaultSort: () => latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
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
  includeSequencingAttributes: boolean,
  runName?: string
): TableDefinition<Sample, void> {
  return {
    queryUrl: queryUrl,
    getDefaultSort: () => latestActivitySort,
    filters: includeSequencingAttributes ? caseFilters : runLibraryFilters,
    staticActions: [
      legendAction,
      {
        title: "Download",
        handler(filters, baseFilter) {
          downloadSampleMetrics(
            filters,
            baseFilter,
            "LIBRARY_QUALIFICATION",
            runName
          );
        },
      },
    ],
    bulkActions: [
      {
        title: "QC in MISO",
        handler(items) {
          qcInMiso(items, "LIBRARY_QUALIFICATION");
        },
        view: "internal",
      },
    ],
    generateColumns(data) {
      const columns: ColumnDefinition<Sample, void>[] = [
        makeQcStatusColumn(includeSequencingAttributes),
        makeNameColumn(includeSequencingAttributes),
        tissueAttributesColumn,
        designColumn,
        ...generateMetricColumns("LIBRARY_QUALIFICATION", data),
        latestActivityColumn,
      ];
      if (includeSequencingAttributes && internalUser) {
        columns.splice(4, 0, sequencingAttributesColumn);
      }
      return columns;
    },
  };
}

export function getFullDepthSequencingsDefinition(
  queryUrl: string,
  includeSequencingAttributes: boolean,
  runName?: string
): TableDefinition<Sample, void> {
  return {
    queryUrl: queryUrl,
    getDefaultSort: () => latestActivitySort,
    filters: includeSequencingAttributes ? caseFilters : runLibraryFilters,
    staticActions: [
      legendAction,
      {
        title: "Download",
        handler(filters, baseFilter) {
          downloadSampleMetrics(
            filters,
            baseFilter,
            "FULL_DEPTH_SEQUENCING",
            runName
          );
        },
      },
    ],
    bulkActions: [
      {
        title: "QC in MISO",
        handler(items) {
          qcInMiso(items, "FULL_DEPTH_SEQUENCING");
        },
        view: "internal",
      },
    ],
    generateColumns(data) {
      const columns: ColumnDefinition<Sample, void>[] = [
        makeQcStatusColumn(includeSequencingAttributes),
        makeNameColumn(includeSequencingAttributes),
        tissueAttributesColumn,
        designColumn,
        ...generateMetricColumns("FULL_DEPTH_SEQUENCING", data),
        latestActivityColumn,
      ];
      if (includeSequencingAttributes && internalUser) {
        columns.splice(4, 0, sequencingAttributesColumn);
      }
      return columns;
    },
  };
}

function downloadSampleMetrics(
  filters: { key: string; value: string }[],
  baseFilter: { key: string; value: string } | undefined,
  category: MetricCategory,
  runName?: string
) {
  const callback = (result: any) => {
    const options = result.formatOptions;
    options.category = category;
    options.baseFilter = baseFilter;
    options.filters = filters;
    if (runName) {
      options.runName = runName;
    }
    postDownload(urls.rest.downloads.reports("sample-metrics"), options);
  };
  showDownloadOptionsDialog("sample-metrics", [], callback);
}

function qcInMiso(items: Sample[], category: MetricCategory) {
  const missingRun = items.filter((x) => !x.run).map((x) => x.name);
  if (missingRun.length) {
    const list = makeList(missingRun);
    showErrorDialog("Some items are not run-libraries:", list);
    return;
  }
  const missingAssay = items
    .filter((x) => !x.assayIds?.length)
    .map((x) => x.name)
    .filter(unique);
  if (missingAssay.length) {
    const list = makeList(missingAssay);
    showErrorDialog("Some libraries have no assay:", list);
    return;
  }
  openQcInMiso(items, category);
}

function unique<Type>(item: Type, index: number, array: Type[]) {
  return array.indexOf(item) === index;
}

function makeList<Type>(items: string[]): HTMLElement {
  const list = document.createElement("ul");
  list.className = "list-disc list-inside";
  items.forEach((value) => {
    const li = document.createElement("li");
    li.innerText = value;
    list.appendChild(li);
  });
  return list;
}

function openQcInMiso(samples: Sample[], category: MetricCategory) {
  const request: QcInMisoRequest = {
    report: "Dimsum",
    library_aliquots: generateMetricData(category, samples),
  };
  postNavigate(urls.miso.qcRunLibraries, request, true);
}

function generateMetricData(
  category: MetricCategory,
  samples: Sample[]
): MisoRunLibrary[] {
  const data: MisoRunLibrary[] = [];
  samples.forEach((sample) => {
    if (!sample.assayIds?.length) {
      throw new Error(`Sample ${sample.id} has no assay`);
    }
    if (!sample.run) {
      throw new Error(`Sample ${sample.id} has no run`);
    }
    const metricNames = getMetricNames(category, sample.assayIds).filter(
      (x) => RUN_METRIC_LABELS.indexOf(x) === -1
    );
    const sequencingLane = sample.sequencingLane;
    assertRequired(sequencingLane);
    data.push({
      name: extractLibraryName(sample.id),
      run_id: sample.run.id,
      partition: parseInt(sequencingLane),
      metrics: getSampleMetrics(sample, metricNames, category),
    });
  });
  return data;
}

function getSampleMetrics(
  sample: Sample,
  metricNames: string[],
  category: MetricCategory
): MisoRunLibraryMetric[] {
  return metricNames
    .flatMap(
      (metricName) => getMatchingMetrics(metricName, category, sample) || []
    )
    .filter((metric) => metric.thresholdType !== "BOOLEAN")
    .map((metric) => {
      const sampleMetric = sample.metrics.find(
        (sampleMetric) =>
          sampleMetric.name === metric.name &&
          sampleMetric.metricLevel == "SAMPLE"
      );
      const value = sampleMetric
        ? sampleMetric.value
        : getMetricValue(metric.name, sample);
      const misoMetric: MisoRunLibraryMetric = {
        title: metric.name,
        threshold_type: metric.thresholdType.toLowerCase(),
        threshold: getSingleThreshold(metric),
        value: value,
      };
      if (metric.thresholdType == "BETWEEN") {
        if (metric.maximum === undefined) {
          throw new Error("Metric is missing maximum value");
        }
        misoMetric.threshold_2 = metric.maximum;
      }
      return misoMetric;
    });
}

export function getSingleThreshold(metric: Metric) {
  switch (metric.thresholdType) {
    case "GT":
    case "GE":
    case "BETWEEN":
      if (metric.minimum === undefined) {
        throw new Error("Metric is missing minimum value");
      }
      return metric.minimum;
    case "LT":
    case "LE":
      if (metric.maximum === undefined) {
        throw new Error("Metric is missing maximum value");
      }
      return metric.maximum;
    default:
      throw new Error("Unhandled threshold type: " + metric.thresholdType);
  }
}

function generateMetricColumns(
  category: MetricCategory,
  samples?: Sample[]
): ColumnDefinition<Sample, void>[] {
  if (!samples) {
    return [];
  }
  const assayIds: number[] = samples
    .flatMap((sample) => sample.assayIds || [])
    .filter(unique);
  const metricNames = getMetricNames(category, assayIds);
  return metricNames
    .filter((metricName) =>
      samples.some((sample) => {
        // filter out metrics that are n/a for all samples
        const metrics = getMatchingMetrics(metricName, category, sample);
        return metrics && metrics.length;
      })
    )
    .map((metricName) => {
      return {
        title: metricName,
        addParentContents(sample, fragment) {
          const metrics = getMatchingMetrics(metricName, category, sample);
          if (!metrics || !metrics.length) {
            addNaText(fragment);
            return;
          }
          addMetricValueContents(sample, metrics, fragment, true);
        },
        getCellHighlight(sample) {
          return getSampleMetricCellHighlight(sample, metricName, category);
        },
      };
    });
}

export function getSampleMetricCellHighlight(
  sample: Sample,
  metricName: string,
  category: MetricCategory
): CellStatus | null {
  const metrics = getMatchingMetrics(metricName, category, sample);
  if (!metrics || !metrics.length) {
    return "na";
  }
  // handle metrics that are checked against multiple values
  switch (metricName) {
    case METRIC_LABEL_CLUSTERS_PF_1:
    case METRIC_LABEL_CLUSTERS_PF_2:
      return getClustersPfHighlight(sample, metrics);
    case METRIC_LABEL_PHIX:
      return getPhixHighlight(sample, metrics);
  }
  if (metricName === "Sample Authenticated") {
    const sampleMetric = sample.metrics.find(
      (metric) => metric.name === metricName
    );
    const qcPassed = sampleMetric ? sampleMetric.qcPassed : null;
    return getBooleanMetricHighlight(qcPassed);
  } else if (metrics.every((metric) => metric.thresholdType === "BOOLEAN")) {
    return getBooleanMetricHighlight(sample.qcPassed);
  }
  if (
    /^Adaptor Contamination/.test(metricName) ||
    /^AUC between/.test(metricName) ||
    /^Assigned/.test(metricName) ||
    metricName === "Empty"
  ) {
    return null;
  }

  // TODO: handle run and lane level metrics (not populated at time of writing)
  const sampleMetric = sample.metrics.find(
    (metric) => metric.name === metricName && metric.metricLevel == "SAMPLE"
  );
  // handle metrics that may be preliminary
  const preliminary = sampleMetric
    ? sampleMetric.preliminary
    : isPreliminary(metricName, sample);
  if (preliminary) {
    return "warning";
  }
  const value = sampleMetric
    ? sampleMetric.value
    : getMetricValue(metricName, sample);
  if (nullOrUndefined(value)) {
    return "warning";
  }
  if (anyFail(value, metrics)) {
    return "error";
  }
  return null;
}

function isPreliminary(metricName: string, sample: Sample) {
  if (METRIC_LABELS_CLUSTERS.includes(metricName)) {
    if (
      nullOrUndefined(sample.clustersPerSample) &&
      !nullOrUndefined(sample.preliminaryClustersPerSample)
    ) {
      return true;
    }
  } else if (metricName === METRIC_LABEL_COVERAGE) {
    if (
      nullOrUndefined(sample.meanCoverageDeduplicated) &&
      !nullOrUndefined(sample.preliminaryMeanCoverageDeduplicated)
    ) {
      return true;
    }
  }
  return false;
}

export function addMetricValueContents(
  sample: Sample,
  metrics: Metric[],
  fragment: DocumentFragment,
  addTooltip: boolean,
  shouldCollapse: boolean = true
) {
  const metricNames = metrics
    .map((metric) => metric.name)
    .filter((name, i, arr) => i === arr.indexOf(name));
  if (metricNames.length !== 1) {
    throw new Error("No common metric name found");
  }
  const metricName = metricNames[0];
  // handle metrics that have multiple values
  switch (metricName) {
    case METRIC_LABEL_Q30:
      addQ30Contents(sample, metrics, fragment, addTooltip, shouldCollapse);
      return;
    case METRIC_LABEL_CLUSTERS_PF_1:
    case METRIC_LABEL_CLUSTERS_PF_2:
      addClustersPfContents(
        sample,
        metrics,
        fragment,
        addTooltip,
        shouldCollapse
      );
      return;
    case METRIC_LABEL_PHIX:
      addPhixContents(sample, metrics, fragment, addTooltip, shouldCollapse);
      return;
  }

  if (metricName === "Sample Authenticated") {
    const sampleMetric = sample.metrics.find(
      (metric) => metric.name === metricName
    );
    const qcPassed = sampleMetric ? sampleMetric.qcPassed : null;
    if (qcPassed == null) {
      // Show pending analysis rather than pending QC
      fragment.append(
        makeStatusIcon(qcStatuses.analysis.icon, qcStatuses.analysis.label)
      );
    } else {
      fragment.append(getBooleanMetricValueIcon(qcPassed));
    }
    return;
  } else if (metrics.every((metric) => metric.thresholdType === "BOOLEAN")) {
    fragment.append(getBooleanMetricValueIcon(sample.qcPassed));
    return;
  }
  if (
    /^Adaptor Contamination/.test(metricName) ||
    /^AUC between/.test(metricName)
  ) {
    fragment.append(
      makeNameDiv("See attachment in MISO", urls.miso.sample(sample.id))
    );
    return;
  }
  if (/^Assigned/.test(metricName) || metricName === "Empty") {
    addTextDiv("Manual check required", fragment);
    return;
  }
  // TODO: handle run and lane level metrics (not populated at time of writing)
  const sampleMetric = sample.metrics.find(
    (metric) => metric.name === metricName && metric.metricLevel == "SAMPLE"
  );
  const value = sampleMetric
    ? sampleMetric.value
    : getMetricValue(metricName, sample);
  const preliminary = sampleMetric
    ? sampleMetric.preliminary
    : isPreliminary(metricName, sample);
  if (value === null) {
    if (sample.run) {
      const status = sample.run.completionDate
        ? qcStatuses.analysis
        : qcStatuses.sequencing;
      fragment.appendChild(makeStatusIcon(status.icon, status.label));
    } else {
      fragment.appendChild(makeNotFoundIcon());
    }
  } else {
    let additionalTooltip = undefined;
    if (preliminary) {
      additionalTooltip = makeTextDiv("PRELIMINARY VALUE ONLY");
      additionalTooltip.classList.add("font-bold");
    }
    const contents = makeMetricDisplay(
      value,
      metrics,
      addTooltip,
      undefined,
      additionalTooltip
    );
    if (preliminary) {
      const icon = makeIcon("pen-ruler");
      icon.classList.add("mr-1");
      contents.prepend(icon);
    }
    fragment.append(contents);
  }
}

function createCollapseButton(contentWrapper: HTMLElement): HTMLButtonElement {
  const toggleButton = document.createElement("button");
  toggleButton.classList.add("fa-solid", "fa-caret-down", "text-sm");
  toggleButton.classList.add("active:text-green-200");

  toggleButton.addEventListener("click", () => {
    const isExpanded = contentWrapper.classList.toggle("hidden");
    toggleButton.classList.toggle("fa-caret-down", isExpanded);
    toggleButton.classList.toggle("fa-caret-up", !isExpanded);
  });

  return toggleButton;
}

function handleCollapse(
  metricDisplay: HTMLElement,
  contentWrapper: HTMLElement,
  fragment: DocumentFragment,
  shouldCollapse: boolean
) {
  const metricWrapper = document.createElement("div");
  metricWrapper.className = "flex space-x-1";

  metricWrapper.appendChild(metricDisplay);

  if (shouldCollapse) {
    const toggleButton = createCollapseButton(contentWrapper);
    metricWrapper.appendChild(toggleButton);
    contentWrapper.classList.add("hidden");
  }

  fragment.appendChild(metricWrapper);
  fragment.appendChild(contentWrapper);
}

function addQ30Contents(
  sample: Sample,
  metrics: Metric[],
  fragment: DocumentFragment,
  addTooltip: boolean,
  shouldCollapse: boolean = true
) {
  // run-level value is checked, but run and lane-level are both displayed
  if (!sample.run || nullOrUndefined(sample.run.percentOverQ30)) {
    if (sample.run && !sample.run.completionDate) {
      fragment.appendChild(makeSequencingIcon());
    } else {
      fragment.appendChild(makeNotFoundIcon());
    }
    return;
  }

  const metricDisplay = makeMetricDisplay(
    sample.run.percentOverQ30,
    metrics,
    addTooltip
  );

  const contentWrapper = document.createElement("div");

  assertDefined(sample.run.lanes);
  const lanes = sample.run.lanes;
  lanes.forEach((lane) => {
    if (nullOrUndefined(lane.percentOverQ30Read1)) {
      return;
    }
    let text = lanes.length === 1 ? "" : `L${lane.laneNumber} `;
    text += `R1: ${lane.percentOverQ30Read1}`;
    if (lane.percentOverQ30Read2) {
      text += `; R2: ${lane.percentOverQ30Read2}`;
    }
    const div = document.createElement("div");
    div.classList.add("whitespace-nowrap", "print-hanging");
    div.appendChild(document.createTextNode(text));
    contentWrapper.appendChild(div);
  });

  handleCollapse(metricDisplay, contentWrapper, fragment, shouldCollapse);
}

function addClustersPfContents(
  sample: Sample,
  metrics: Metric[],
  fragment: DocumentFragment,
  addTooltip: boolean,
  shouldCollapse: boolean = true
) {
  // For joined flowcells, run-level is checked
  // For non-joined, each lane is checked
  // Metric is sometimes specified "/lane", sometimes per run
  if (!sample.run || nullOrUndefined(sample.run.clustersPf)) {
    if (sample.run && !sample.run.completionDate) {
      fragment.appendChild(makeSequencingIcon());
    } else {
      fragment.appendChild(makeNotFoundIcon());
    }
    return;
  }

  const separatedMetrics = separateRunVsLaneMetrics(metrics, sample.run);
  const perRunMetrics = separatedMetrics[0];
  const perLaneMetrics = separatedMetrics[1];
  const tooltip = Tooltip.getInstance();
  const runDiv = document.createElement("div");
  const divisorUnit = getDivisorUnit(metrics);

  runDiv.innerText = formatMetricValue(
    sample.run.clustersPf,
    metrics,
    divisorUnit
  );

  if (addTooltip && perRunMetrics.length) {
    // whether originally or not, these metrics are per run
    const addContents = (fragment: DocumentFragment) => {
      addMetricRequirementText(perRunMetrics, fragment);
    };
    tooltip.addTarget(runDiv, addContents);
  }

  assertDefined(sample.run.lanes);
  if (sample.run.lanes.length > 1) {
    const contentWrapper = document.createElement("div");
    const addContents = (fragment: DocumentFragment) => {
      // these metrics are per lane
      addMetricRequirementText(perLaneMetrics, fragment);
    };

    sample.run.lanes.forEach((lane) => {
      if (!nullOrUndefined(lane.clustersPf)) {
        const laneDiv = document.createElement("div");
        laneDiv.classList.add("whitespace-nowrap", "print-hanging");
        laneDiv.innerText = `L${lane.laneNumber}: ${formatMetricValue(
          lane.clustersPf,
          metrics,
          divisorUnit
        )}`;
        if (addTooltip && perLaneMetrics.length) {
          tooltip.addTarget(laneDiv, addContents);
        }
        contentWrapper.appendChild(laneDiv);
      }
    });

    handleCollapse(runDiv, contentWrapper, fragment, shouldCollapse);
  } else {
    fragment.appendChild(runDiv);
  }
}

function getClustersPfHighlight(
  sample: Sample,
  metrics: Metric[]
): CellStatus | null {
  if (!sample.run || nullOrUndefined(sample.run.clustersPf)) {
    return "warning";
  }
  const separatedMetrics = separateRunVsLaneMetrics(metrics, sample.run);
  const perRunMetrics = separatedMetrics[0];
  const perLaneMetrics = separatedMetrics[1];

  const divisorUnit = getDivisorUnit(metrics);
  const divisor = getDivisor(divisorUnit);

  if (
    perRunMetrics.length &&
    anyFail(sample.run.clustersPf / divisor, perRunMetrics)
  ) {
    return "error";
  }

  if (perLaneMetrics.length) {
    let highlight: CellStatus | null = null;
    assertDefined(sample.run.lanes);
    for (let i = 0; i < sample.run.lanes.length; i++) {
      const lane = sample.run.lanes[i];
      if (nullOrUndefined(lane.clustersPf)) {
        highlight = "warning";
      } else if (anyFail(lane.clustersPf / divisor, perLaneMetrics)) {
        return "error";
      }
    }
    return highlight;
  }

  return null;
}

function separateRunVsLaneMetrics(metrics: Metric[], run: Run) {
  let perLaneMetrics = metrics.filter(
    (metric) => metric.units && metric.units.endsWith("/lane")
  );
  let perRunMetrics = metrics.filter(
    (metric) => !metric.units || !metric.units.endsWith("/lane")
  );
  if (run.joinedLanes) {
    // ALL metrics are per run. If specified per lane, multiply by lane count
    perLaneMetrics.forEach((metric) => {
      const perRunMetric = makePerRunFromLaneMetric(metric, run);
      perRunMetrics.push(perRunMetric);
    });
    perLaneMetrics = [];
  }
  assertDefined(run.lanes);
  if (run.lanes.length === 1) {
    // Treat all as per run since we won't show the lane metrics separately
    perRunMetrics = metrics;
    perLaneMetrics = [];
  }
  return [perRunMetrics, perLaneMetrics];
}

function makePerRunFromLaneMetric(perLaneMetric: Metric, run: Run) {
  assertDefined(run.lanes);
  const perRunMetric: Metric = Object.assign({}, perLaneMetric);
  if (perRunMetric.minimum) {
    perRunMetric.minimum *= run.lanes.length;
  }
  if (perRunMetric.maximum) {
    perRunMetric.maximum *= run.lanes.length;
  }
  if (!perRunMetric.units) {
    throw new Error("Unexpected missing units");
  }
  const match = /^(.*)\/lane$/.exec(perRunMetric.units);
  if (!match) {
    throw new Error(`Unexpected metric units: ${perRunMetric.units}`);
  }
  perRunMetric.units = match[1];
  return perRunMetric;
}

function addPhixContents(
  sample: Sample,
  metrics: Metric[],
  fragment: DocumentFragment,
  addTooltip: boolean,
  shouldCollapse: boolean = true
) {
  // There is no run-level metric, so we check each read of each lane
  if (
    !sample.run ||
    !sample.run.lanes ||
    !sample.run.lanes.length ||
    sample.run.lanes.every((lane) => nullOrUndefined(lane.percentPfixRead1))
  ) {
    if (sample.run && !sample.run.completionDate) {
      fragment.appendChild(makeSequencingIcon());
    } else {
      fragment.appendChild(makeNotFoundIcon());
    }
    return;
  }

  const tooltip = Tooltip.getInstance();
  const addContents = (fragment: DocumentFragment) => {
    addMetricRequirementText(metrics, fragment);
  };

  const multipleLanes = sample.run.lanes.length > 1;

  const minPhixValue = Math.min(
    ...sample.run.lanes.flatMap((lane) => {
      const values = [];
      if (lane.percentPfixRead1 !== null) {
        values.push(lane.percentPfixRead1);
      }
      if (lane.percentPfixRead2 !== null) {
        values.push(lane.percentPfixRead2);
      }
      return values;
    })
  );

  const contentWrapper = document.createElement("div");

  sample.run.lanes.forEach((lane) => {
    const laneDiv = document.createElement("div");
    laneDiv.classList.add("whitespace-nowrap", "print-hanging");

    if (multipleLanes) {
      const laneLabel = document.createTextNode(`L${lane.laneNumber}: `);
      laneDiv.appendChild(laneLabel);
    }

    if (nullOrUndefined(lane.percentPfixRead1)) {
      laneDiv.appendChild(makeNotFoundIcon());
    } else {
      const text =
        `R1: ${lane.percentPfixRead1}` +
        (!nullOrUndefined(lane.percentPfixRead2)
          ? `; R2: ${lane.percentPfixRead2}`
          : "");
      const textNode = document.createTextNode(text);
      laneDiv.appendChild(textNode);

      if (addTooltip) {
        tooltip.addTarget(laneDiv, addContents);
      }
    }
    contentWrapper.appendChild(laneDiv);
  });

  const minPhixDiv = document.createElement("div");
  minPhixDiv.innerText = `${minPhixValue.toFixed(2)}+/R`;

  handleCollapse(minPhixDiv, contentWrapper, fragment, shouldCollapse);
}

function getPhixHighlight(
  sample: Sample,
  metrics: Metric[]
): CellStatus | null {
  if (
    !sample.run ||
    !sample.run.lanes ||
    !sample.run.lanes.length ||
    sample.run.lanes.some((lane) => nullOrUndefined(lane.percentPfixRead1))
  ) {
    return "warning";
  }
  if (
    sample.run.lanes.some((lane) => {
      assertRequired(lane.percentPfixRead1);
      return (
        anyFail(lane.percentPfixRead1, metrics) ||
        (!nullOrUndefined(lane.percentPfixRead2) &&
          anyFail(lane.percentPfixRead2, metrics))
      );
    })
  ) {
    return "error";
  }
  return null;
}

function getMatchingMetrics(
  metricName: string,
  category: MetricCategory,
  sample: Sample
): Metric[] | null {
  if (!sample.assayIds?.length) {
    return null;
  }
  return sample.assayIds
    .flatMap((assayId) => getMetricCategory(assayId, category) || [])
    .filter((subcategory) => subcategoryApplies(subcategory, sample))
    .flatMap((subcategory) => subcategory.metrics)
    .filter(
      (metric) => metric.name === metricName && metricApplies(metric, sample)
    );
}

export function subcategoryApplies(
  subcategory: MetricSubcategory,
  sample: Sample
): boolean {
  if (
    subcategory.libraryDesignCode &&
    subcategory.libraryDesignCode !== sample.libraryDesignCode
  ) {
    return false;
  }
  if (
    subcategory.metrics.every((metric) =>
      RUN_METRIC_LABELS.includes(metric.name)
    ) &&
    !sample.run
  ) {
    // This is a run subcategory and the sample has no run
    return false;
  }
  return true;
}

export function metricApplies(metric: Metric, sample: Sample): boolean {
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

// Note: sample.metrics should be checked before this. This function should
// eventually be removed as sample.metrics will include all metrics
function getMetricValue(metricName: string, sample: Sample): number | null {
  switch (metricName) {
    case "Appropriate volume":
    case "Volume":
      return nullIfUndefined(sample.volume);
    case "Yield":
    case "Yield (Qubit)":
      return nullOrUndefined(sample.volume) ||
        nullOrUndefined(sample.concentration)
        ? null
        : sample.volume * sample.concentration;
    case "DV200":
      return nullIfUndefined(sample.dv200);
    case "Mean Insert Size":
      return nullIfUndefined(sample.meanInsertSize);
    case "Median Insert Size":
      return nullIfUndefined(sample.medianInsertSize);
    case "Duplication Rate":
      return nullIfUndefined(sample.duplicationRate);
    case METRIC_LABELS_CLUSTERS[0]:
    case METRIC_LABELS_CLUSTERS[1]:
    case METRIC_LABELS_CLUSTERS[2]:
    case METRIC_LABELS_CLUSTERS[3]:
      if (nullOrUndefined(sample.clustersPerSample)) {
        return nullIfUndefined(sample.preliminaryClustersPerSample);
      } else {
        return sample.clustersPerSample;
      }
    case "rRNA Contamination":
      return nullIfUndefined(sample.rRnaContamination);
    case METRIC_LABEL_COVERAGE:
      if (nullOrUndefined(sample.meanCoverageDeduplicated)) {
        return nullIfUndefined(sample.preliminaryMeanCoverageDeduplicated);
      } else {
        return sample.meanCoverageDeduplicated;
      }
    case "Coverage (Raw)":
    case "Mean Bait Coverage":
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
    case METRIC_LABEL_Q30:
      return sample.run ? nullIfUndefined(sample.run.percentOverQ30) : null;
    case "Lambda Methylation":
      return nullIfUndefined(sample.lambdaMethylation);
    case "Lambda Clusters":
      return nullIfUndefined(sample.lambdaClusters);
    case "pUC19 Methylation":
      return nullIfUndefined(sample.puc19Methylation);
    case "pUC19 Clusters":
      return nullIfUndefined(sample.puc19Clusters);
    case "Relative CpG Frequency in Regions vs Reference":
      return nullIfUndefined(sample.relativeCpgInRegions);
    case "Methylation Beta":
      return nullIfUndefined(sample.methylationBeta);
    case "PE Reads":
      return nullIfUndefined(sample.peReads);
    case "Collapsed Coverage":
      return nullIfUndefined(sample.collapsedCoverage);
  }
  if (/^Concentration/.test(metricName)) {
    return nullIfUndefined(sample.concentration);
  } else if (/^Avg Size Distribution/.test(metricName)) {
    return nullIfUndefined(sample.librarySize);
  }
  return null;
}

export function getQcStatus(sample: Sample): QcStatus {
  const sampleStatus = getSampleQcStatus(sample);
  if (sample.run) {
    const runStatus = getQcStatusWithDataReview(sample.run);
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

export function getQcStatusWithDataReview(run: Qcable): QcStatus {
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

export function getFirstReviewStatus(qcable: Qcable) {
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

function makeSequencingIcon() {
  return makeStatusIcon(
    qcStatuses.sequencing.icon,
    qcStatuses.sequencing.label
  );
}

export function extractLibraryName(runLibraryId: string): string {
  const match = runLibraryId.match("^\\d+_\\d+_(LDI\\d+)$");
  if (!match) {
    throw new Error(`Sample ${runLibraryId} is not a run-library`);
  }
  return match[1];
}
