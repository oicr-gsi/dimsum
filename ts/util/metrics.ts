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
  const fragment = document.createDocumentFragment();
  addTextDiv(statusText, fragment);
  if (tooltipAdditionalContents) {
    fragment.appendChild(tooltipAdditionalContents);
  }
  tooltip.addTarget(element, fragment);
  return element;
}

export function nullIfUndefined(value: any) {
  return value === undefined ? null : value;
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
    const tooltipFragment = document.createDocumentFragment();
    if (tooltipAdditionalContents) {
      tooltipFragment.appendChild(tooltipAdditionalContents);
    }
    metrics.forEach((metric) => {
      const div = document.createElement("div");
      div.innerText = "Required: " + getMetricRequirementText(metric);
      tooltipFragment.appendChild(div);
    });
    const tooltip = Tooltip.getInstance();
    tooltip.addTarget(div, tooltipFragment);
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

export function getSingleThreshold(metric: Metric) {
  switch (metric.thresholdType) {
    case "GT":
    case "GE":
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
      throw new Error("Metric does not have a single threshold");
  }
}
