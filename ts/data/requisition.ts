import {
  addNaText,
  addTextDiv,
  CellStatus,
  makeIcon,
  makeNameDiv,
} from "../util/html-utils";
import {
  ColumnDefinition,
  legendAction,
  TableDefinition,
} from "../component/table-builder";
import { urls } from "../util/urls";
import { qcStatuses } from "./qc-status";
import {
  anyFail,
  getMetricNames,
  makeMetricDisplay,
  makeNotFoundIcon,
  nullIfUndefined,
} from "../util/metrics";
import { siteConfig } from "../util/site-config";
import { Metric, MetricSubcategory } from "./assay";
import { caseFilters, latestActivitySort } from "../component/table-components";
import { Donor } from "./case";

export interface RequisitionQcGroup {
  donor: Donor;
  tissueOrigin: string;
  tissueType: string;
  libraryDesignCode: string;
  groupId?: string;
  purity?: number;
  collapsedCoverage?: number;
  callability?: number;
}

export interface RequisitionQc {
  qcPassed: boolean;
  qcUser: string;
  qcDate: string;
}

export interface Requisition {
  id: number;
  name: string;
  assayId?: number;
  stopped: boolean;
  stopReason?: string;
  qcGroups: RequisitionQcGroup[];
  analysisReviews: RequisitionQc[];
  releaseApprovals: RequisitionQc[];
  releases: RequisitionQc[];
  latestActivityDate?: string;
}

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
  sortType: "text",
  addParentContents(requisition, fragment) {
    fragment.appendChild(
      makeNameDiv(
        requisition.name,
        urls.miso.requisition(requisition.id),
        urls.dimsum.requisition(requisition.id)
      )
    );
  },
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

export const analysisReviewDefinition: TableDefinition<Requisition, void> = {
  queryUrl: urls.rest.requisitions,
  defaultSort: latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: (data?: Requisition[]) => [
    qcStatusColumn((requisition) => requisition.analysisReviews),
    requisitionColumn,
    ...generateMetricColumns(data),
    latestActivityColumn,
  ],
};

export const releaseApprovalDefinition: TableDefinition<Requisition, void> = {
  queryUrl: urls.rest.requisitions,
  defaultSort: latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: () => [
    qcStatusColumn((requisition) => requisition.releaseApprovals),
    requisitionColumn,
    latestActivityColumn,
  ],
};

export const releaseDefinition: TableDefinition<Requisition, void> = {
  queryUrl: urls.rest.requisitions,
  defaultSort: latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: () => [
    qcStatusColumn((requisition) => requisition.releases),
    requisitionColumn,
    latestActivityColumn,
  ],
};

function generateMetricColumns(
  requisitions?: Requisition[]
): ColumnDefinition<Requisition, void>[] {
  if (!requisitions) {
    return [];
  }
  const assayIds = requisitions
    .map((requisition) => requisition.assayId || 0)
    .filter((assayId) => assayId > 0);
  const metricNames = getMetricNames("ANALYSIS_REVIEW", assayIds);
  return metricNames.map((metricName) => {
    return {
      title: metricName,
      addParentContents(requisition, fragment) {
        if (metricName === "Trimming; Minimum base quality Q") {
          fragment.appendChild(
            document.createTextNode("Standard pipeline removes reads below Q30")
          );
        } else if (!requisition.qcGroups.length) {
          fragment.appendChild(makeNotFoundIcon());
        } else {
          const groupsToInclude: RequisitionQcGroup[] = [];
          const metricsPerGroup: Metric[][] = [];
          requisition.qcGroups.forEach((qcGroup) => {
            const metrics = getMatchingMetrics(
              metricName,
              requisition,
              qcGroup
            );
            if (metrics && metrics.length) {
              groupsToInclude.push(qcGroup);
              metricsPerGroup.push(metrics);
            }
          });
          if (!groupsToInclude.length) {
            addNaText(fragment);
            return;
          }
          for (let i = 0; i < groupsToInclude.length; i++) {
            const qcGroup = groupsToInclude[i];
            const metrics = metricsPerGroup[i];
            const value = getMetricValue(metricName, qcGroup);
            const div = document.createElement("div");
            const prefix =
              groupsToInclude.length > 1
                ? `${makeQcGroupLabel(qcGroup)}: `
                : "";
            let detailsFragment = undefined;
            if (groupsToInclude.length > 1) {
              detailsFragment = document.createDocumentFragment();
              addTextDiv(
                `Tissue Origin: ${qcGroup.tissueOrigin}`,
                detailsFragment
              );
              addTextDiv(`Tissue Type: ${qcGroup.tissueType}`, detailsFragment);
              addTextDiv(
                `Design: ${qcGroup.libraryDesignCode}`,
                detailsFragment
              );
              if (qcGroup.groupId) {
                addTextDiv(`Group ID: ${qcGroup.groupId}`, detailsFragment);
              }
            }
            fragment.appendChild(
              makeRequisitionMetricDisplay(
                metricsPerGroup[i],
                qcGroup,
                true,
                prefix,
                detailsFragment
              )
            );
          }
        }
      },
      getCellHighlight(requisition) {
        if (metricName === "Trimming; Minimum base quality Q") {
          return null;
        } else if (!requisition.qcGroups.length) {
          return "warning";
        }
        let anyApplicable = false;
        for (let i = 0; i < requisition.qcGroups.length; i++) {
          const qcGroup = requisition.qcGroups[i];
          const metrics = getMatchingMetrics(metricName, requisition, qcGroup);
          if (!metrics || !metrics.length) {
            continue;
          }
          anyApplicable = true;
          const value = getMetricValue(metricName, qcGroup);
          if (value === null) {
            return "warning";
          } else if (anyFail(value, metrics)) {
            return "error";
          }
        }
        return anyApplicable ? null : "na";
      },
    };
  });
}

export function getRequisitionMetricCellHighlight(
  requisition: Requisition,
  metricName: string
): CellStatus | null {
  if (metricName === "Trimming; Minimum base quality Q") {
    return null;
  } else if (!requisition.qcGroups.length) {
    return "warning";
  }
  let anyApplicable = false;
  for (let i = 0; i < requisition.qcGroups.length; i++) {
    const qcGroup = requisition.qcGroups[i];
    const metrics = getMatchingMetrics(metricName, requisition, qcGroup);
    if (!metrics || !metrics.length) {
      continue;
    }
    anyApplicable = true;
    const value = getMetricValue(metricName, qcGroup);
    if (value === null) {
      return "warning";
    } else if (anyFail(value, metrics)) {
      return "error";
    }
  }
  return anyApplicable ? null : "na";
}

export function makeRequisitionMetricDisplay(
  metrics: Metric[],
  qcGroup: RequisitionQcGroup,
  addTooltip: boolean,
  prefix?: string,
  tooltipAdditionalContents?: Node
): Node {
  if (metrics[0].name === "Trimming; Minimum base quality Q") {
    return document.createTextNode("Standard pipeline removes reads below Q30");
  }
  const div = document.createElement("div");
  const value = getMetricValue(metrics[0].name, qcGroup);
  if (value === null) {
    div.appendChild(makeNotFoundIcon(prefix, tooltipAdditionalContents));
  } else {
    div.appendChild(
      makeMetricDisplay(
        value,
        metrics,
        addTooltip,
        prefix,
        tooltipAdditionalContents
      )
    );
  }
  return div;
}

function makeQcGroupLabel(qcGroup: RequisitionQcGroup) {
  let label = `${qcGroup.tissueOrigin}_${qcGroup.tissueType}_${qcGroup.libraryDesignCode}`;
  if (qcGroup.groupId) {
    label += ` - ${qcGroup.groupId}`;
  }
  return label;
}

export function getMetricValue(
  metricName: string,
  qcGroup: RequisitionQcGroup
): number | null {
  switch (metricName) {
    case "Inferred Tumour Purity":
      return nullIfUndefined(qcGroup.purity);
    case "Collapsed Coverage":
      return nullIfUndefined(qcGroup.collapsedCoverage);
    case "Callability (exonic space); Target bases above 30X":
      return nullIfUndefined(qcGroup.callability);
    default:
      return null;
  }
}

function getMatchingMetrics(
  metricName: string,
  requisition: Requisition,
  qcGroup: RequisitionQcGroup
): Metric[] | null {
  if (!requisition.assayId) {
    return null;
  }
  return siteConfig.assaysById[requisition.assayId].metricCategories[
    "ANALYSIS_REVIEW"
  ]
    .filter((subcategory) => subcategoryApplies(subcategory, qcGroup))
    .flatMap((subcategory) => subcategory.metrics)
    .filter(
      (metric) => metric.name === metricName && metricApplies(metric, qcGroup)
    );
}

export function subcategoryApplies(
  subcategory: MetricSubcategory,
  qcGroup: RequisitionQcGroup
): boolean {
  return (
    !subcategory.libraryDesignCode ||
    subcategory.libraryDesignCode === qcGroup.libraryDesignCode
  );
}

export function metricApplies(
  metric: Metric,
  qcGroup: RequisitionQcGroup
): boolean {
  if (metric.tissueOrigin && metric.tissueOrigin !== qcGroup.tissueOrigin) {
    return false;
  }
  if (metric.tissueType) {
    if (metric.negateTissueType) {
      if (metric.tissueType === qcGroup.tissueType) {
        return false;
      }
    } else if (metric.tissueType !== qcGroup.tissueType) {
      return false;
    }
  }
  return true;
}

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
