import { Tooltip } from "../component/tooltip";
import { Metric, MetricCategory, MetricSubcategory } from "../data/assay";
import { addTextDiv, makeIcon } from "./html-utils";
import { siteConfig } from "./site-config";

export function getMetricNames(
  category: MetricCategory,
  assayIds: number[]
): string[] {
  const assays = assayIds.filter(unique).map((assayId) => {
    if (!assayId) {
      throw new Error("Unexpected error (undefined should be filtered)");
    }
    return siteConfig.assaysById[assayId];
  });

  const subcategories = assays
    .filter(
      (assay) =>
        assay.metricCategories &&
        assay.metricCategories[category] &&
        assay.metricCategories[category].length
    )
    .flatMap((assay) => assay.metricCategories![category]);

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

export function makeNotFoundIcon(
  prefix?: string,
  tooltipAdditionalContents?: Node
) {
  return makeStatusIcon(
    "question",
    "Not Found",
    prefix,
    tooltipAdditionalContents
  );
}

export function makeStatusIcon(
  iconName: string,
  statusText: string,
  prefix?: string,
  tooltipAdditionalContents?: Node
) {
  const icon = makeIcon(iconName);
  let element: HTMLElement = icon;
  if (prefix) {
    element = document.createElement("div");
    const span = document.createElement("span");
    span.innerText = prefix;
    element.append(span, icon);
  }
  const tooltip = Tooltip.getInstance();
  const addContents = (fragment: DocumentFragment) => {
    addTextDiv(statusText, fragment);
    if (tooltipAdditionalContents) {
      fragment.appendChild(tooltipAdditionalContents);
    }
  };
  tooltip.addTarget(element, addContents);
  return element;
}

export function makeMetricDisplay(
  value: number,
  metrics: Metric[],
  addTooltip: boolean,
  prefix?: string,
  tooltipAdditionalContents?: Node
): HTMLSpanElement {
  const displayValue = formatMetricValue(value, metrics);
  const div = document.createElement("div");
  div.innerText = (prefix || "") + displayValue;
  if (addTooltip) {
    const addContents = (fragment: DocumentFragment) => {
      if (tooltipAdditionalContents) {
        fragment.appendChild(tooltipAdditionalContents);
      }
      addMetricRequirementText(metrics, fragment);
    };
    const tooltip = Tooltip.getInstance();
    tooltip.addTarget(div, addContents);
  }
  return div;
}

export function formatMetricValue(
  value: number,
  metrics: Metric[],
  divisorUnit?: string | null
) {
  const metricPlaces = Math.max(
    ...metrics.map((metric) =>
      Math.max(
        countDecimalPlaces(metric.minimum),
        countDecimalPlaces(metric.maximum)
      )
    )
  );
  if (divisorUnit) {
    value = value / getDivisor(divisorUnit);
  }
  if (metricPlaces === 0 && Number.isInteger(value)) {
    return formatDecimal(value, 0) + (divisorUnit || "");
  } else {
    return formatDecimal(value, metricPlaces + 1) + (divisorUnit || "");
  }
}

export function getDivisorUnit(metrics: Metric[]) {
  if (metrics.some((metric) => metric.units && metric.units.startsWith("K"))) {
    return "K";
  } else if (
    metrics.some((metric) => metric.units && metric.units.startsWith("M"))
  ) {
    return "M";
  } else if (
    metrics.some((metric) => metric.units && metric.units.startsWith("B"))
  ) {
    return "B";
  }
  return null;
}

export function getDivisor(unit: string | null) {
  switch (unit) {
    case "K":
      return 1000;
    case "M":
      return 1000000;
    case "B":
      return 1000000000;
    default:
      return 1;
  }
}

function formatDecimal(value: number, decimalPlaces?: number) {
  let val = value.toLocaleString("en-CA", {
    minimumFractionDigits: decimalPlaces,
  });
  if (decimalPlaces) {
    const decimalIndex = val.indexOf(".");
    if (decimalIndex > -1) {
      val = val.substring(0, decimalIndex + decimalPlaces + 1);
    }
  }
  return val;
}

function formatThreshold(value?: number) {
  if (value === undefined) {
    return "Unknown";
  }
  if (Number.isInteger(value)) {
    return formatDecimal(value, 0);
  } else {
    return formatDecimal(value);
  }
}

export function addMetricRequirementText(metrics: Metric[], container: Node) {
  const metricDiv = document.createElement("div");
  const requirements: Set<string> = new Set();
  metrics.forEach((metric) =>
    requirements.add(getMetricRequirementText(metric))
  );
  if (requirements.size === 1) {
    metricDiv.innerText = "Required: " + requirements.values().next().value;
  } else {
    const requirementsText = document.createElement("span");
    requirementsText.innerText = "Requirements:";
    const requirementsList = document.createElement("ul");
    requirements.forEach((requirement) => {
      const requirementListItem = document.createElement("li");
      requirementListItem.innerText = requirement;
      requirementsList.appendChild(requirementListItem);
    });
    metricDiv.append(requirementsText, requirementsList);
  }
  container.appendChild(metricDiv);
}

export function getMetricRequirementText(metric: Metric) {
  let text = null;
  switch (metric.thresholdType) {
    case "GT":
      text = `> ${formatThreshold(metric.minimum)}`;
      break;
    case "GE":
      text = `>= ${formatThreshold(metric.minimum)}`;
      break;
    case "LT":
      text = `< ${formatThreshold(metric.maximum)}`;
      break;
    case "LE":
      text = `<= ${formatThreshold(metric.maximum)}`;
      break;
    case "BETWEEN":
      text = `Between ${formatThreshold(metric.minimum)} and ${formatThreshold(
        metric.maximum
      )}`;
      break;
    default:
      throw new Error(`Unexpected threshold type: ${metric.thresholdType}`);
  }
  if (metric.units) {
    text += metric.units;
  }
  return text;
}

export function anyFail(value: number, metrics: Metric[]): boolean {
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

export function getBooleanMetricValueIcon(qcPassed: boolean | null) {
  // pass/fail based on QC status
  if (qcPassed) {
    return makeStatusIcon("check", "Passed");
  } else if (qcPassed === false) {
    return makeStatusIcon("xmark", "Failed");
  } else {
    return makeStatusIcon("magnifying-glass", "Pending QC");
  }
}

export function getBooleanMetricHighlight(qcPassed: boolean | null) {
  if (qcPassed) {
    return null;
  } else if (qcPassed === false) {
    return "error";
  } else {
    return "warning";
  }
}
