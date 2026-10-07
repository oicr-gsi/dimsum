import {
  addNaText,
  addTextDiv,
  CellStatus,
  makeIcon,
  makeNameDiv,
  makeTextDiv,
} from "../util/html-utils";
import { ColumnDefinition, legendAction, TableDefinition } from "../component/table-builder";
import { Tooltip } from "../component/tooltip";
import { urls } from "../util/urls";
import { Metric, MetricCategory, MetricSubcategory } from "./assay";
import { addStatusTooltipText, Donor, Qcable, Run } from "./case";
import { QcStatus, qcStatuses } from "./qc-status";
import {
  addMetricRequirementText,
  anyFail,
  formatMetricValue,
  formatSampleMetricValue,
  getBooleanMetricHighlight,
  getBooleanMetricValueIcon,
  getDivisorUnit,
  getMetricNames,
  makeMetricDisplay,
  makeMetricRequirementsTooltip,
  makeNotFoundIcon,
  makeSampleMetricDisplay,
  makeStatusIcon,
} from "../util/metrics";
import { showDownloadOptionsDialog, showErrorDialog } from "../component/dialog";
import { caseFilters, latestActivitySort, runLibraryFilters } from "../component/table-components";
import { postDownload, postNavigate } from "../util/requests";
import { assertDefined, assertRequired, nullIfUndefined, nullOrUndefined } from "./data-utils";
import { getMetricCategory, internalUser } from "../util/site-config";

const METRIC_LABEL_Q30 = "Bases Over Q30";
const METRIC_LABEL_CLUSTERS_PF_1 = "Min Clusters (PF)";
const METRIC_LABEL_CLUSTERS_PF_2 = "Min Reads Delivered (PF)";
const METRIC_LABEL_PHIX = "PhiX Control";

export const RUN_METRIC_LABELS = [
  METRIC_LABEL_Q30,
  METRIC_LABEL_CLUSTERS_PF_1,
  METRIC_LABEL_CLUSTERS_PF_2,
  METRIC_LABEL_PHIX,
];

type ThresholdType = "BOOLEAN" | "LT" | "LE" | "GT" | "GE" | "BETWEEN";
type MetricLevel = "SAMPLE" | "RUN" | "LANE";

export interface SampleMetricLane {
  laneNumber: number;
  laneValue: number | null;
  read1Value: number | null;
  read2Value: number | null;
}

export interface SampleMetric {
  name: string;
  thresholdType: ThresholdType;
  minimum: number | null;
  maximum: number | null;
  metricLevel: MetricLevel;
  preliminary: boolean | null;
  value: number | null;
  laneValues: SampleMetricLane[] | null;
  qcPassed: boolean | null;
  units: string | null;
}

export interface RelatedSample {
  id: string;
  name: string;
  qcPassed: boolean | null;
  qcReason: string | null;
  runName: string;
  runQcPassed: boolean | null;
  sequencingLane: string;
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
  collapsedCoverage?: number | null;
  latestActivityDate: string;
  sequencingLane: string | null;
  metrics: SampleMetric[];
  analysisSkipped?: boolean | null;
  relatedSamples?: RelatedSample[];
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

function makeQcStatusColumn(includeRun: boolean): ColumnDefinition<Sample, void> {
  const getStatus = includeRun ? getQcStatus : getSampleQcStatus;
  return {
    title: "QC Status",
    sortType: "custom",
    addParentContents(sample, fragment) {
      const status = getStatus(sample);
      const icon = makeIcon(status.icon);
      const tooltipInstance = Tooltip.getInstance();
      tooltipInstance.addTarget(icon, (tooltip) => {
        addStatusTooltipText(tooltip, status, sample.qcReason, sample.qcUser, sample.qcNote);
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
        (!includeRun && sample.sequencingLane ? " (L" + sample.sequencingLane + ")" : "");
      const nameDiv = makeNameDiv(
        name,
        urls.miso.sample(sample.id),
        undefined,
        sample.name,
        sample.run && internalUser ? sample.id : undefined,
      );
      if (sample.relatedSamples && sample.relatedSamples.length) {
        const relatedSamples = sample.relatedSamples;
        Tooltip.getInstance().addTarget(nameDiv, (fragment) => {
          relatedSamples.sort((a, b) => {
            // sort samples on same run to the top
            if (a.runName === sample.run?.name) {
              return b.runName === a.runName ? 0 : -1;
            } else if (b.runName === sample.run?.name) {
              return 1;
            }
            return (
              a.runName.localeCompare(b.runName) ||
              a.name.localeCompare(b.name) ||
              a.sequencingLane.localeCompare(b.sequencingLane)
            );
          });
          const headingDiv = makeTextDiv("Related:");
          headingDiv.classList.add("font-bold");
          fragment.append(headingDiv);
          let runName = null;
          for (const related of relatedSamples) {
            if (related.runName !== runName) {
              runName = related.runName;
              let runNameDiv;
              if (runName === sample.run?.name) {
                runNameDiv = makeNameDiv("This Run");
                runNameDiv.classList.add("font-bold");
              } else {
                runNameDiv = makeNameDiv(
                  runName,
                  urls.miso.run(runName),
                  urls.dimsum.run(runName),
                  runName,
                );
              }
              const runStatus = getRelatedQcStatus(related.runQcPassed);
              const runStatusIcon = makeIcon(runStatus.icon);
              runStatusIcon.title = "Status: " + runStatus.label;
              runNameDiv.prepend(runStatusIcon);
              fragment.appendChild(runNameDiv);
            }
            const sampleNameDiv = makeNameDiv(
              `${related.name} (L${related.sequencingLane})`,
              urls.miso.sample(related.id),
              undefined,
              related.name,
              related.id,
            );
            sampleNameDiv.classList.add("ml-4");
            const sampleStatus = getRelatedQcStatus(related.qcPassed, related.qcReason);
            const sampleStatusIcon = makeIcon(sampleStatus.icon);
            sampleStatusIcon.title = "Status: " + sampleStatus.label;
            sampleNameDiv.prepend(sampleStatusIcon);
            fragment.appendChild(sampleNameDiv);
          }
        });
      }
      fragment.appendChild(nameDiv);

      if (includeRun && sample.run) {
        const runName = sample.run.name;
        const nameDiv = makeNameDiv(
          sample.sequencingLane ? runName + " (L" + sample.sequencingLane + ")" : runName,
          urls.miso.run(runName),
          internalUser ? urls.dimsum.run(runName) : undefined,
          runName,
        );
        fragment.appendChild(nameDiv);
      }
    },
    sortType: "text",
  };
}

function getRelatedQcStatus(qcPassed: boolean | null, qcReason?: string | null) {
  if (qcPassed === false) {
    return qcStatuses.failed;
  } else if (qcPassed === true) {
    return qcStatuses.passed;
  } else if (qcReason === "Top-up Required") {
    return qcStatuses.topUp;
  } else {
    return qcStatuses.qc;
  }
}

const tissueAttributesColumn: ColumnDefinition<Sample, void> = {
  title: "Tissue Attributes",
  addParentContents(sample, fragment) {
    const tumourDetailDiv = document.createElement("div");
    tumourDetailDiv.appendChild(
      document.createTextNode(
        `${sample.tissueOrigin} ${sample.tissueType}` +
          (sample.timepoint ? " " + sample.timepoint : ""),
      ),
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
        document.createTextNode(`Flow cell: ${sample.run.containerModel || "unknown"}`),
      );
      fragment.appendChild(flowCellContainer);
      const parametersContainer = document.createElement("div");
      parametersContainer.appendChild(
        document.createTextNode(`Parameters: ${sample.run.sequencingParameters || "unknown"}`),
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
                urls.dimsum.requisition(sample.requisitionId),
              ),
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
  runName?: string,
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
          downloadSampleMetrics(filters, baseFilter, "LIBRARY_QUALIFICATION", runName);
        },
        view: "internal",
      },
    ],
    bulkActions: [
      {
        title: "QC in MISO",
        handler: qcInMiso,
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
  runName?: string,
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
          downloadSampleMetrics(filters, baseFilter, "FULL_DEPTH_SEQUENCING", runName);
        },
        view: "internal",
      },
    ],
    bulkActions: [
      {
        title: "QC in MISO",
        handler: qcInMiso,
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
  runName?: string,
) {
  const callback = (result: any) => {
    const options = result.formatOptions;
    options.category = category;
    options.baseFilter = baseFilter;
    options.filters = filters;
    if (runName) {
      options.runName = runName;
    }
    postDownload(urls.rest.downloads.reports("sample-metrics"), options, "Generating report.");
  };
  showDownloadOptionsDialog(callback);
}

function qcInMiso(items: Sample[]) {
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
  openQcInMiso(items);
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

function openQcInMiso(samples: Sample[]) {
  const request: QcInMisoRequest = {
    report: "Dimsum",
    library_aliquots: generateMetricData(samples),
  };
  postNavigate(urls.miso.qcRunLibraries, request, true);
}

function generateMetricData(samples: Sample[]): MisoRunLibrary[] {
  const data: MisoRunLibrary[] = [];
  samples.forEach((sample) => {
    if (!sample.assayIds?.length) {
      throw new Error(`Sample ${sample.id} has no assay`);
    }
    if (!sample.run) {
      throw new Error(`Sample ${sample.id} has no run`);
    }
    const sequencingLane = sample.sequencingLane;
    assertRequired(sequencingLane);
    data.push({
      name: extractLibraryName(sample.id),
      run_id: sample.run.id,
      partition: parseInt(sequencingLane),
      metrics: getSampleMetrics(sample),
    });
  });
  return data;
}

function getSampleMetrics(sample: Sample): MisoRunLibraryMetric[] {
  return sample.metrics
    .filter((metric) => metric.metricLevel === "SAMPLE" && metric.thresholdType !== "BOOLEAN")
    .map((metric) => {
      const threshold1 = nullOrUndefined(metric.minimum) ? metric.maximum : metric.minimum;
      assertRequired(threshold1);

      const misoMetric: MisoRunLibraryMetric = {
        title: metric.name,
        threshold_type: metric.thresholdType.toLowerCase(),
        threshold: threshold1,
        value: metric.value,
      };
      if (!nullOrUndefined(metric.minimum) && !nullOrUndefined(metric.maximum)) {
        misoMetric.threshold_2 = metric.maximum;
      }
      return misoMetric;
    });
}

function generateMetricColumns(
  category: MetricCategory,
  samples?: Sample[],
): ColumnDefinition<Sample, void>[] {
  if (!samples) {
    return [];
  }
  const assayIds: number[] = samples.flatMap((sample) => sample.assayIds || []).filter(unique);
  const metricNames = getMetricNames(category, assayIds);
  return metricNames
    .filter((metricName) =>
      samples.some((sample) => {
        // filter out metrics that are n/a for all samples
        if (sample.metrics.find((metric) => metric.name === metricName)) {
          return true;
        }
        // Handle metrics not in sample.metrics yet
        const metrics = getMatchingMetrics(metricName, category, sample);
        return metrics && metrics.length;
      }),
    )
    .map((metricName) => {
      return {
        title: metricName,
        addParentContents(sample, fragment) {
          // generate entirely from sample.metrics instead of using the assay metric. At some
          // point, this should be used for all metrics
          const sampleMetric = sample.metrics.find((metric) => metric.name === metricName);
          if (sampleMetric) {
            addMetricValueContentsNew(sample, sampleMetric, fragment, true, true);
            return;
          }
          // handle metrics that aren't in sample.metrics yet, and metrics that are N/A for the
          // sample
          const metrics = getMatchingMetrics(metricName, category, sample);
          if (!metrics || !metrics.length) {
            addNaText(fragment);
            return;
          }
          addMetricValueContents(sample, metrics, fragment, true);
        },
        getCellHighlight(sample) {
          // Similarly use sample.metrics if available
          const sampleMetric = sample.metrics.find((metric) => metric.name === metricName);
          if (sampleMetric) {
            return getSampleMetricHighlightNew(sampleMetric);
          }
          return getSampleMetricCellHighlight(sample, metricName, category);
        },
      };
    });
}

export function getSampleMetricHighlightNew(metric: SampleMetric): CellStatus | null {
  // Preliminary = warning
  if (metric.preliminary) {
    return "warning";
  }
  // Unhandled metrics
  if (/^Assigned/.test(metric.name) || metric.name === "Empty") {
    return null;
  }
  if (nullOrUndefined(metric.qcPassed)) {
    return "warning";
  }
  return metric.qcPassed ? null : "error";
}

export function getSampleMetricCellHighlight(
  sample: Sample,
  metricName: string,
  category: MetricCategory,
): CellStatus | null {
  const metrics = getMatchingMetrics(metricName, category, sample);
  if (!metrics || !metrics.length) {
    return "na";
  }
  if (metrics.every((metric) => metric.thresholdType === "BOOLEAN")) {
    return getBooleanMetricHighlight(sample.qcPassed);
  }
  if (/^Adaptor Contamination/.test(metricName) || /^AUC between/.test(metricName)) {
    return null;
  }

  const value = getMetricValue(metricName, sample);
  if (nullOrUndefined(value)) {
    return "warning";
  }
  if (anyFail(value, metrics)) {
    return "error";
  }
  return null;
}

export function addMetricValueContentsNew(
  sample: Sample,
  metric: SampleMetric,
  fragment: DocumentFragment,
  addTooltip: boolean,
  shouldCollapse: boolean,
) {
  // Unhandled metrics
  if (/^Assigned/.test(metric.name) || metric.name === "Empty") {
    addTextDiv("Manual check required", fragment);
    return;
  }
  if (valueMissing(metric)) {
    if (sample.run) {
      if (sample.analysisSkipped) {
        fragment.appendChild(makeAnalysisSkippedIcon());
      } else {
        const status = sample.run.completionDate ? qcStatuses.analysis : qcStatuses.sequencing;
        fragment.appendChild(makeStatusIcon(status.icon, status.label));
      }
    } else {
      fragment.appendChild(makeNotFoundIcon());
    }
    return;
  }

  if (metric.thresholdType === "BOOLEAN") {
    fragment.append(getBooleanMetricValueIcon(metric.qcPassed));
    return;
  }

  let mainContents;
  if (nullOrUndefined(metric.value) && metric.laneValues && metric.laneValues.length) {
    const readLevel =
      nullOrUndefined(metric.laneValues[0].laneValue) &&
      !nullOrUndefined(metric.laneValues[0].read1Value);
    if (metric.laneValues.length === 1 && !readLevel) {
      // display lane 1 value only
      assertRequired(metric.laneValues[0].laneValue);
      mainContents = makeSampleMetricDisplay(metric.laneValues[0].laneValue, metric, addTooltip);
    } else {
      // display min/max per lane/read
      mainContents = makeRunLevelSummary(metric, readLevel);
    }
  } else {
    assertRequired(metric.value);
    mainContents = makeSampleMetricDisplay(
      metric.value,
      metric,
      addTooltip && metric.metricLevel != "LANE",
    );
  }
  if (metric.preliminary) {
    const icon = makeIcon("pen-ruler");
    icon.classList.add("mr-1");
    mainContents.prepend(icon);
  }

  fragment.append(mainContents);
  addLaneValues(metric, fragment, mainContents, addTooltip, shouldCollapse);
}

function makeRunLevelSummary(metric: SampleMetric, readLevel: boolean) {
  assertRequired(metric.laneValues);
  const laneValues = readLevel
    ? (metric.laneValues
        .flatMap((lane) => [lane.read1Value, lane.read2Value])
        .filter((x) => !nullOrUndefined(x)) as number[])
    : (metric.laneValues
        .map((lane) => lane.laneValue)
        .filter((x) => !nullOrUndefined(x)) as number[]);
  assertRequired(laneValues);
  switch (metric.thresholdType) {
    case "GT":
    case "GE": {
      const valueText = formatSampleMetricValue(Math.min(...laneValues), metric);
      return makeTextDiv(valueText + "+/" + (readLevel ? "R" : "L"));
    }
    case "LT":
    case "LE": {
      const valueText = formatSampleMetricValue(Math.max(...laneValues), metric);
      return makeTextDiv(valueText + "-/" + (readLevel ? "R" : "L"));
    }
    default: {
      return makeTextDiv("See lanes");
    }
  }
}

function showLaneValues(metric: SampleMetric): boolean {
  if (!metric.laneValues || !metric.laneValues.length) {
    return false;
  }
  if (metric.laneValues.length > 1) {
    return true;
  }
  const singleLane = metric.laneValues[0];
  if (
    nullOrUndefined(singleLane.read1Value) &&
    (nullOrUndefined(metric.value) || metric.value == singleLane.laneValue)
  ) {
    // The single lane value is already shown in place of the run value (or is the same anyway)
    return false;
  }
  return true;
}

function addLaneValues(
  metric: SampleMetric,
  fragment: DocumentFragment,
  mainContents: HTMLElement,
  addTooltip: boolean,
  shouldCollapse: boolean,
) {
  if (!showLaneValues(metric)) {
    return;
  }
  const laneContentWrapper = document.createElement("div");
  const laneCount = metric.laneValues ? metric.laneValues.length : 0;
  assertRequired(metric.laneValues);
  const tooltip = Tooltip.getInstance();
  metric.laneValues
    .sort((a, b) => a.laneNumber - b.laneNumber)
    .forEach((lane) => {
      const laneDiv = document.createElement("div");
      laneDiv.classList.add("whitespace-nowrap", "print-hanging");

      if (laneCount > 1) {
        const laneLabel = document.createTextNode(`L${lane.laneNumber}: `);
        laneDiv.appendChild(laneLabel);
      }

      if (!nullOrUndefined(lane.laneValue)) {
        const text = formatSampleMetricValue(lane.laneValue, metric);
        laneDiv.appendChild(document.createTextNode(text));
        if (addTooltip) {
          tooltip.addTarget(laneDiv, (fragment) => makeMetricRequirementsTooltip(fragment, metric));
        }
      } else if (nullOrUndefined(lane.read1Value)) {
        laneDiv.appendChild(makeNotFoundIcon());
      } else {
        const text =
          `R1: ${formatSampleMetricValue(lane.read1Value, metric)}` +
          (!nullOrUndefined(lane.read2Value)
            ? `; R2: ${formatSampleMetricValue(lane.read2Value, metric)}`
            : "");
        const textNode = document.createTextNode(text);
        laneDiv.appendChild(textNode);

        if (addTooltip) {
          tooltip.addTarget(laneDiv, (fragment) => makeMetricRequirementsTooltip(fragment, metric));
        }
      }
      laneContentWrapper.appendChild(laneDiv);
    });

  handleCollapse(mainContents, laneContentWrapper, fragment, shouldCollapse);
}

function valueMissing(metric: SampleMetric): boolean {
  if (metric.thresholdType === "BOOLEAN") {
    return nullOrUndefined(metric.qcPassed);
  }
  if (metric.metricLevel === "LANE") {
    if (
      !metric.laneValues ||
      !metric.laneValues.length ||
      metric.laneValues.some(
        (lane) => nullOrUndefined(lane.laneValue) && nullOrUndefined(lane.read1Value),
      )
    ) {
      return true;
    }
  } else if (metric.value == null) {
    return true;
  }
  return false;
}

export function addMetricValueContents(
  sample: Sample,
  metrics: Metric[],
  fragment: DocumentFragment,
  addTooltip: boolean,
) {
  const metricNames = metrics
    .map((metric) => metric.name)
    .filter((name, i, arr) => i === arr.indexOf(name));
  if (metricNames.length !== 1) {
    throw new Error("No common metric name found");
  }
  const metricName = metricNames[0];
  if (metrics.every((metric) => metric.thresholdType === "BOOLEAN")) {
    fragment.append(getBooleanMetricValueIcon(sample.qcPassed));
    return;
  }
  if (/^Adaptor Contamination/.test(metricName) || /^AUC between/.test(metricName)) {
    fragment.append(makeNameDiv("See attachment in MISO", urls.miso.sample(sample.id)));
    return;
  }
  const value = getMetricValue(metricName, sample);
  if (value === null) {
    if (sample.run) {
      if (sample.analysisSkipped) {
        fragment.appendChild(makeAnalysisSkippedIcon());
      } else {
        const status = sample.run.completionDate ? qcStatuses.analysis : qcStatuses.sequencing;
        fragment.appendChild(makeStatusIcon(status.icon, status.label));
      }
    } else {
      fragment.appendChild(makeNotFoundIcon());
    }
  } else {
    let additionalTooltip = undefined;
    const contents = makeMetricDisplay(value, metrics, addTooltip, undefined, additionalTooltip);
    fragment.append(contents);
  }
}

function makeAnalysisSkippedIcon() {
  return makeStatusIcon("triangle-exclamation", "Analysis Skipped");
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
  shouldCollapse: boolean,
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

function getMatchingMetrics(
  metricName: string,
  category: MetricCategory,
  sample: Sample,
): Metric[] | null {
  if (!sample.assayIds?.length) {
    return null;
  }
  return sample.assayIds
    .flatMap((assayId) => getMetricCategory(assayId, category) || [])
    .filter((subcategory) => subcategoryApplies(subcategory, sample))
    .flatMap((subcategory) => subcategory.metrics)
    .filter((metric) => metric.name === metricName && metricApplies(metric, sample));
}

export function subcategoryApplies(subcategory: MetricSubcategory, sample: Sample): boolean {
  if (subcategory.libraryDesignCode && subcategory.libraryDesignCode !== sample.libraryDesignCode) {
    return false;
  }
  if (
    subcategory.metrics.every((metric) => RUN_METRIC_LABELS.includes(metric.name)) &&
    !sample.run
  ) {
    // This is a run subcategory and the sample has no run
    return false;
  }
  return true;
}

export function metricApplies(metric: Metric, sample: Sample): boolean {
  if (metric.tissueMaterial && sample.tissueMaterial !== metric.tissueMaterial) {
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
  if (metric.nucleicAcidType && metric.nucleicAcidType !== sample.nucleicAcidType) {
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
      (!sample.run.readLength2 || Math.abs(metric.readLength2 - sample.run.readLength2) > 1)
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
      return nullOrUndefined(sample.volume) || nullOrUndefined(sample.concentration)
        ? null
        : sample.volume * sample.concentration;
    case "DV200":
      return nullIfUndefined(sample.dv200);
    case "Coverage (Raw)":
    case "Quantitative PCR (qPCR)":
      // Must be in nM
      if (sample.concentrationUnits === "NANOMOLAR") {
        return nullIfUndefined(sample.concentration);
      } else {
        return null;
      }
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
    return runStatus.priority < sampleStatus.priority ? runStatus : sampleStatus;
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

export function extractLibraryName(runLibraryId: string): string {
  const match = runLibraryId.match("^\\d+_\\d+_(LDI\\d+)$");
  if (!match) {
    throw new Error(`Sample ${runLibraryId} is not a run-library`);
  }
  return match[1];
}
