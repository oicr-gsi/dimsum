import Plotly from "plotly.js-dist-min";
import { post } from "./util/requests";
import { getRequiredElementById } from "./util/html-utils";
import { toggleLegend } from "./component/legend";
import { getColorForGate } from "./util/color-mapping";
import {
  TableDefinition,
  TableBuilder,
  ColumnDefinition,
} from "./component/table-builder";
import { urls } from "./util/urls";
import { siteConfig } from "./util/site-config";

let jsonData: any[] = [];
const uirevision = "true";
let tableBuilder: TableBuilder<AssayMetrics, void> | null = null;
const NOT_AVAILABLE = "N/A";

interface AssayMetrics {
  assay: string;
  gate: string;
  timeRanges: Array<{
    group: string;
    avgDays: string | undefined;
    medianDays: string | undefined;
    caseCount: string | undefined;
  }>;
}

interface CaseCounts {
  [key: string]: number;
}

// constants for column names in the Case TAT Report
const COLUMN_NAMES = {
  ASSAY: "Assay",
  CASE_ID: "Case ID",
  RC_DAYS: "Receipt Days",
  EX_DAYS: "EX Days",
  LP_DAYS: "Library Prep. Days",
  LQ_DAYS: "LQ Total Days",
  FD_DAYS: "FD Total Days",
  ALL_DAYS: "ALL Total Days",
  RC_COMPLETED: "Receipt Completed",
  EX_COMPLETED: "Extraction (EX) Completed",
  LP_COMPLETED: "Library Prep. Completed",
  LQ_COMPLETED: "Library Qual. (LQ) Completed",
  FD_COMPLETED: "Full-Depth (FD) Completed",
};

function getCompletionColumnName(
  deliverableCategory: string,
  gate: string
): string {
  return `${deliverableCategory} ${gate} Completed`.trim();
}

function getDaysColumnName(deliverableCategory: string, gate: string): string {
  return `${deliverableCategory} ${gate} Days`.trim();
}

function generateColor(index: number): string {
  const colors = [
    "#4477AA",
    "#66CCEE",
    "#228833",
    "#CCBB44",
    "#EE6677",
    "#AA3377",
    "#BBBBBB",
    "#000000",
  ];
  return colors[index % colors.length];
}

interface AssayGroups {
  [assay: string]: {
    [gate: string]: { x: any[]; y: number[]; text: string[]; n: number };
  };
}

interface GroupDays {
  [group: string]: number[];
}

function getCompletedDateAndDays(
  row: any,
  gate: string,
  deliverableCategory: string
) {
  let completedDate: Date | null = null;
  let days: number = 0;

  switch (gate) {
    case "Receipt":
      completedDate = row[COLUMN_NAMES.RC_COMPLETED]
        ? new Date(row[COLUMN_NAMES.RC_COMPLETED])
        : null;
      days = row[COLUMN_NAMES.RC_DAYS] ?? 0;
      break;
    case "Extraction":
      completedDate = row[COLUMN_NAMES.EX_COMPLETED]
        ? new Date(row[COLUMN_NAMES.EX_COMPLETED])
        : null;
      days = row[COLUMN_NAMES.EX_DAYS] ?? 0;
      break;
    case "Library Prep":
      completedDate = row[COLUMN_NAMES.LP_COMPLETED]
        ? new Date(row[COLUMN_NAMES.LP_COMPLETED])
        : null;
      days = row[COLUMN_NAMES.LP_DAYS] ?? 0;
      break;
    case "Library Qual":
      completedDate = row[COLUMN_NAMES.LQ_COMPLETED]
        ? new Date(row[COLUMN_NAMES.LQ_COMPLETED])
        : null;
      days = row[COLUMN_NAMES.LQ_DAYS] ?? 0;
      break;
    case "Full-Depth":
      completedDate = row[COLUMN_NAMES.FD_COMPLETED]
        ? new Date(row[COLUMN_NAMES.FD_COMPLETED])
        : null;
      days = row[COLUMN_NAMES.FD_DAYS] ?? 0;
      break;
    case "Full Test":
      completedDate = row[
        getCompletionColumnName(deliverableCategory, "Release")
      ]
        ? new Date(row[getCompletionColumnName(deliverableCategory, "Release")])
        : null;
      days = row[getDaysColumnName(deliverableCategory, "Total")] ?? 0;
      break;
    default:
      completedDate = row[getCompletionColumnName(deliverableCategory, gate)]
        ? new Date(row[getCompletionColumnName(deliverableCategory, gate)])
        : null;
      days = row[getDaysColumnName(deliverableCategory, gate)] ?? 0;
  }

  return { completedDate, days };
}

function getGroup(date: Date, selectedGrouping: string): string {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  let fiscalYear = year;
  let fiscalQuarter: number;

  if (month >= 4) {
    fiscalYear = year; // current fiscal year
    fiscalQuarter = Math.floor((month - 4) / 3) + 1;
  } else {
    fiscalYear = year - 1; // previous fiscal year
    fiscalQuarter = 4; // last quarter
  }
  const fiscalYearString = `FY${fiscalYear}/${String(fiscalYear + 1).slice(
    -2
  )}`;
  switch (selectedGrouping) {
    case "week":
      return getWeek(date);
    case "month":
      return `${year}-${String(month).padStart(2, "0")}`;
    case "fiscalQuarter":
      return `${fiscalYearString} Q${fiscalQuarter}`;
    case "fiscalYear":
      return fiscalYearString;
    default:
      throw new Error(`Unsupported grouping type: ${selectedGrouping}`);
  }
}

function selectAllGates(onUpdate: () => void) {
  const selectAllCheckboxId = "selectAllGates";
  const checkboxesContainerId = "gatesCheckboxes";

  const selectAllCheckbox = getRequiredElementById(
    selectAllCheckboxId
  ) as HTMLInputElement;
  const checkboxes = document.querySelectorAll<HTMLInputElement>(
    `#${checkboxesContainerId} input[type='checkbox']:not(#${selectAllCheckboxId})`
  );
  selectAllCheckbox.addEventListener("change", () => {
    const isChecked = selectAllCheckbox.checked;
    checkboxes.forEach((checkbox) => {
      checkbox.checked = isChecked;
    });
    onUpdate();
  });
  checkboxes.forEach((checkbox) => {
    checkbox.addEventListener("change", () => {
      const allChecked = Array.from(checkboxes).every((cb) => cb.checked);
      selectAllCheckbox.checked = allChecked;
      onUpdate();
    });
  });
}

function groupData(
  jsonData: any[],
  selectedGrouping: string,
  selectedGates: string[],
  deliverableCategory: string
): AssayGroups {
  const assayGroups: AssayGroups = {};

  jsonData.forEach((row: any) => {
    const assay = row[COLUMN_NAMES.ASSAY];
    const caseId = row[COLUMN_NAMES.CASE_ID];
    const caseCompletedDate =
      row[getCompletionColumnName(deliverableCategory, "Release")];

    // skip if case completed date is missing
    if (!caseCompletedDate) {
      return;
    }
    if (assay && caseId != null) {
      if (!assayGroups[assay]) {
        assayGroups[assay] = {};
      }
      selectedGates.forEach((gate) => {
        const { completedDate, days } = getCompletedDateAndDays(
          row,
          gate,
          deliverableCategory
        );
        if (!completedDate) {
          return;
        }
        const date = new Date(caseCompletedDate);
        const group = getGroup(date, selectedGrouping);
        if (!assayGroups[assay][gate]) {
          assayGroups[assay][gate] = { x: [], y: [], text: [], n: 0 };
        }
        assayGroups[assay][gate].x.push(group);
        assayGroups[assay][gate].y.push(days);
        assayGroups[assay][gate].text.push(`${caseId} (${gate})`);
        assayGroups[assay][gate].n += 1;
      });
    }
  });
  return assayGroups;
}

function getWeek(date: Date): string {
  const onejan = new Date(date.getFullYear(), 0, 1);
  const week = Math.ceil(
    ((date.getTime() - onejan.getTime()) / 86400000 + onejan.getDay() + 1) / 7
  );
  return `${date.getFullYear()}-W${String(week).padStart(2, "0")}`;
}

function getColorByGate(): boolean {
  const toggleColors = getRequiredElementById(
    "toggleColors"
  ) as HTMLInputElement;
  return toggleColors.checked;
}

function getCaseCount(groups: any[]): { [key: string]: number } {
  const groupCounts: { [key: string]: number } = {};
  groups.forEach((group) => {
    if (!groupCounts[group]) {
      groupCounts[group] = 0;
    }
    groupCounts[group] += 1;
  });
  return groupCounts;
}

function calcMedian(arr: number[]): number {
  const sorted = arr.slice().sort((a, b) => a - b);
  const mid = Math.floor(sorted.length / 2);
  return sorted.length % 2 !== 0
    ? sorted[mid]
    : (sorted[mid - 1] + sorted[mid]) / 2;
}

function parseDateValue(timeRange: string | undefined): number {
  if (!timeRange) return 0;
  if (timeRange.includes("FY") && timeRange.includes("Q")) {
    const [fiscalYearStart, fiscalYearEnd, quarter] =
      timeRange.match(/\d+/g) ?? [];
    const fiscalYear =
      parseInt(fiscalYearStart ?? "0", 10) * 10000 +
      parseInt(fiscalYearEnd ?? "0", 10) * 100 +
      parseInt(quarter ?? "0");
    return fiscalYear;
  }
  if (timeRange.includes("W")) {
    const [year, week] = timeRange.split("-W").map(Number);
    return year * 100 + week;
  }
  if (timeRange.includes("-")) {
    const [year, month] = timeRange.split("-").map(Number);
    return year * 100 + month;
  }
  const yearMatch = timeRange.match(/\d{4}/);
  return yearMatch ? parseInt(yearMatch[0], 10) * 100 : 0;
}

function sortTimeRanges(timeRanges: string[]): string[] {
  return timeRanges.sort((a, b) => parseDateValue(a) - parseDateValue(b));
}

function plotData(
  jsonData: any[],
  selectedGrouping: string,
  selectedGates: string[],
  selectedDeliverableCategory: string
): { newPlot: Partial<Plotly.PlotData>[]; layout: Partial<Plotly.Layout> } {
  const assayGroups = groupData(
    jsonData,
    selectedGrouping,
    selectedGates,
    selectedDeliverableCategory
  );
  const newPlot: Partial<Plotly.PlotData>[] = [];
  const assayColors: { [assay: string]: string } = {};
  const gateColors: { [gate: string]: string } = {};
  let colorIndex = 0;
  const colorByGate = getColorByGate();
  if (colorByGate) {
    selectedGates.forEach((gate, index) => {
      gateColors[gate] = generateColor(index);
    });
  }
  Object.keys(assayGroups).forEach((assay) => {
    if (!assayColors[assay]) {
      assayColors[assay] = generateColor(colorIndex++);
    }
    selectedGates.forEach((gate) => {
      if (assayGroups[assay][gate]) {
        newPlot.push({
          x: assayGroups[assay][gate].x,
          y: assayGroups[assay][gate].y,
          type: "box",
          name: `${assay}`,
          text: assayGroups[assay][gate].text,
          hoverinfo: "text+y",
          hovertemplate: `
            %{text}<br>
            Days: %{y}<br>
          `,
          boxpoints: "all",
          jitter: 0.3,
          pointpos: 0,
          marker: {
            size: 6,
            color: colorByGate ? getColorForGate(gate) : assayColors[assay],
          },
          boxmean: true,
          legendgroup: assay,
          showlegend: !newPlot.some((d) => d.legendgroup === assay),
        } as unknown as Partial<Plotly.PlotData>);
      }
    });
  });

  const layout: Partial<Plotly.Layout> = {
    xaxis: {
      title:
        selectedGrouping === "week"
          ? "Week"
          : selectedGrouping === "month"
          ? "Month"
          : selectedGrouping === "fiscalQuarter"
          ? "Fiscal Quarter"
          : "Fiscal Year",
      tickformat:
        selectedGrouping === "week"
          ? "%Y-W%U"
          : selectedGrouping === "month"
          ? "%Y-%m"
          : selectedGrouping === "fiscalQuarter"
          ? "%Y Q%q"
          : "%Y",
      categoryorder: "category ascending",
    },
    yaxis: {
      title: "Days",
      zeroline: false,
      range: [0, undefined],
    },
    boxmode: "group",
    autosize: true,
    uirevision,
    width: Math.max(window.innerWidth - 50, 800),
    height: Math.max(window.innerHeight - 250, 400),
  };

  return { newPlot, layout };
}

function newPlot(
  selectedGrouping: string,
  jsonData: any[],
  selectedGates: string[],
  selectedDeliverableCategory: string
) {
  const plotContainer = getRequiredElementById("plotContainer");
  plotContainer.textContent = "";

  const { newPlot, layout } = plotData(
    jsonData,
    selectedGrouping,
    selectedGates,
    selectedDeliverableCategory
  );
  Plotly.newPlot("plotContainer", newPlot, layout);

  window.addEventListener("resize", () => {
    Plotly.relayout("plotContainer", {
      width: Math.max(window.innerWidth - 50, 800),
      height: Math.max(window.innerHeight - 250, 400),
    });
  });
}

function updatePlot(
  selectedGrouping: string,
  jsonData: any[],
  selectedGates: string[],
  selectedDeliverableCategory: string
) {
  const { newPlot, layout } = plotData(
    jsonData,
    selectedGrouping,
    selectedGates,
    selectedDeliverableCategory
  );
  Plotly.react("plotContainer", newPlot, layout);
}

function updatePlotWithLegend(
  selectedGrouping: string,
  jsonData: any[],
  selectedGates: string[],
  selectedDeliverableCategory: string
) {
  updatePlot(
    selectedGrouping,
    jsonData,
    selectedGates,
    selectedDeliverableCategory
  );
  const legendButton = document.getElementById("legendButton");
  if (getColorByGate()) {
    // show the Legend button
    if (legendButton) {
      legendButton.classList.remove("hidden");
    }
  } else {
    // hide the Legend button and close the legend if it's open
    if (legendButton) {
      legendButton.classList.add("hidden");
    }
    const legendElement = document.getElementById("legend-container");
    if (legendElement) {
      legendElement.remove(); // hide the legend if 'color by gate' is deselected
    }
  }
}

function updateMetricsTable(
  jsonData: any[],
  selectedGrouping: string,
  selectedGates: string[],
  selectedDeliverableCategory: string
) {
  // build the table data and get the time ranges from the metrics
  const { tableData, timeRanges } = buildTableFromMetrics(
    jsonData,
    selectedGrouping,
    selectedGates,
    selectedDeliverableCategory
  );
  const sortedTimeRanges = sortTimeRanges(timeRanges);
  const dynamicColumns = generateDynamicColumns(sortedTimeRanges);
  const parentHeaders = [
    { title: "", colspan: 1 },
    { title: "", colspan: 1 },
  ].concat(
    sortedTimeRanges.map((timeRange) => ({
      title: timeRange,
      colspan: 3,
    }))
  );
  const caseTatTableDefinition: TableDefinition<AssayMetrics, void> = {
    generateColumns: () => dynamicColumns,
    parentHeaders, // pass parent headers for multi-level header support
    disablePageControls: true,
  };
  // reuse or create a new TableBuilder instance
  if (tableBuilder) {
    tableBuilder.clear();
  }
  tableBuilder = new TableBuilder(caseTatTableDefinition, "metricContainer");
  tableBuilder.build(tableData);
}

function generateDynamicColumns(
  timeRanges: string[]
): ColumnDefinition<AssayMetrics, void>[] {
  const dynamicColumns: ColumnDefinition<AssayMetrics, void>[] = [];
  dynamicColumns.push({
    title: "Assay",
    sortType: "text",
    addParentContents(assayMetrics: AssayMetrics, fragment: DocumentFragment) {
      fragment.appendChild(document.createTextNode(assayMetrics.assay));
    },
  });
  dynamicColumns.push({
    title: "Step",
    sortType: "text",
    addParentContents(assayMetrics: AssayMetrics, fragment: DocumentFragment) {
      fragment.appendChild(document.createTextNode(assayMetrics.gate));
    },
  });
  timeRanges.forEach((timeRangeLabel) => {
    dynamicColumns.push({
      title: `Mean Days`,
      addParentContents(
        assayMetrics: AssayMetrics,
        fragment: DocumentFragment
      ) {
        const timeRangeData = assayMetrics.timeRanges.find(
          (tr) => tr.group === timeRangeLabel
        );
        const value = timeRangeData?.avgDays || NOT_AVAILABLE;
        fragment.appendChild(document.createTextNode(value));
      },
      getCellHighlight(assayMetrics) {
        const timeRangeData = assayMetrics.timeRanges.find(
          (tr) => tr.group === timeRangeLabel
        );
        return timeRangeData ? null : "na";
      },
    });
    dynamicColumns.push({
      title: `Median Days`,
      addParentContents(
        assayMetrics: AssayMetrics,
        fragment: DocumentFragment
      ) {
        const timeRangeData = assayMetrics.timeRanges.find(
          (tr) => tr.group === timeRangeLabel
        );
        const value = timeRangeData?.medianDays || NOT_AVAILABLE;
        fragment.appendChild(document.createTextNode(value));
      },
      getCellHighlight(assayMetrics) {
        const timeRangeData = assayMetrics.timeRanges.find(
          (tr) => tr.group === timeRangeLabel
        );
        return timeRangeData ? null : "na";
      },
    });
    dynamicColumns.push({
      title: `Test Count`,
      addParentContents(
        assayMetrics: AssayMetrics,
        fragment: DocumentFragment
      ) {
        const timeRangeData = assayMetrics.timeRanges.find(
          (tr) => tr.group === timeRangeLabel
        );
        const value = timeRangeData?.caseCount || NOT_AVAILABLE;
        fragment.appendChild(document.createTextNode(value));
      },
      getCellHighlight(assayMetrics) {
        const timeRangeData = assayMetrics.timeRanges.find(
          (tr) => tr.group === timeRangeLabel
        );
        return timeRangeData ? null : "na";
      },
    });
  });
  return dynamicColumns;
}

function buildTableFromMetrics(
  jsonData: any[],
  selectedGrouping: string,
  selectedGates: string[],
  selectedDeliverableCategory: string
) {
  const groupedData = groupData(
    jsonData,
    selectedGrouping,
    selectedGates,
    selectedDeliverableCategory
  );
  const tableData: AssayMetrics[] = [];
  const timeRanges: string[] = [];
  Object.keys(groupedData).forEach((assay) => {
    selectedGates.forEach((gate) => {
      if (groupedData[assay][gate]) {
        const { x: groups, y: daysArray } = groupedData[assay][gate];
        const caseCounts: CaseCounts = getCaseCount(groups);
        const groupDays: GroupDays = {};
        groups.forEach((group, index) => {
          const days = Number(daysArray[index]);
          if (!groupDays[group]) {
            groupDays[group] = [];
          }
          groupDays[group].push(days);
          if (!timeRanges.includes(group)) {
            timeRanges.push(group);
          }
        });
        const assayMetrics: AssayMetrics = {
          assay,
          gate,
          timeRanges: [],
        };
        Object.keys(caseCounts).forEach((timeRange) => {
          const totalCases = caseCounts[timeRange];
          const daysForTimeRange = groupDays[timeRange];
          const totalDays = daysForTimeRange.reduce((a, b) => a + b, 0);
          const averageDays = totalDays / totalCases;
          const medianDays = calcMedian(daysForTimeRange);

          assayMetrics.timeRanges.push({
            group: timeRange,
            avgDays: averageDays.toFixed(1),
            medianDays: medianDays.toFixed(1),
            caseCount: totalCases.toString(),
          });
        });
        tableData.push(assayMetrics);
      }
    });
  });
  return { tableData, timeRanges };
}

function parseUrlParams(): { key: string; value: string }[] {
  const params: { key: string; value: string }[] = [];
  const searchParams = new URLSearchParams(window.location.search);
  searchParams.forEach((value, key) => {
    params.push({ key, value });
  });
  return params;
}

function getSelectedGates(): string[] {
  const gatesCheckboxes = document.querySelectorAll<HTMLInputElement>(
    "#gatesCheckboxes input[type='checkbox']:checked"
  );
  return Array.from(gatesCheckboxes).map((checkbox) => checkbox.value);
}

function getSelectedGrouping(): string {
  const selectedButton = document.querySelector<HTMLButtonElement>(
    "#groupingButtons button.active"
  );
  return selectedButton ? selectedButton.dataset.grouping! : "fiscalQuarter";
}

function getSelectedDeliverableCategory(): string {
  const deliverableCategorySelect = getRequiredElementById(
    "deliverableCategorySelect"
  ) as HTMLSelectElement;
  return deliverableCategorySelect.value;
}

function addDeliverableCategoryOptions() {
  const deliverableCategorySelect = getRequiredElementById(
    "deliverableCategorySelect"
  ) as HTMLSelectElement;
  deliverableCategorySelect.innerHTML = "";
  siteConfig.deliverableCategories.forEach((category) =>
    deliverableCategorySelect.options.add(
      makeDeliverableCategoryOption(category)
    )
  );
  const allOption = makeDeliverableCategoryOption("ALL");
  allOption.selected = true;
  deliverableCategorySelect.options.add(allOption);
}

function makeDeliverableCategoryOption(deliverableCategory: string) {
  const option = document.createElement("option");
  option.value = deliverableCategory;
  option.text = deliverableCategory;
  return option;
}

window.addEventListener("load", () => {
  addDeliverableCategoryOptions();

  const params = parseUrlParams();
  const requestData = { filters: params.length > 0 ? params : [] };

  post(urls.rest.downloads.reportData("case-tat-report"), requestData)
    .then((data) => {
      jsonData = data; // update jsonData with fetched data
      newPlot(
        getSelectedGrouping(),
        jsonData,
        getSelectedGates(),
        getSelectedDeliverableCategory()
      );
      updateMetricsTable(
        jsonData,
        getSelectedGrouping(),
        getSelectedGates(),
        getSelectedDeliverableCategory()
      );
    })
    .catch((error) => {
      alert("Error fetching data: " + error);
    });

  const handlePlotUpdate = () => {
    const selectedGrouping = getSelectedGrouping();
    const selectedGates = getSelectedGates();
    const selectedDeliverableCategory = getSelectedDeliverableCategory();
    updatePlotWithLegend(
      selectedGrouping,
      jsonData,
      selectedGates,
      selectedDeliverableCategory
    );
    updateMetricsTable(
      jsonData,
      selectedGrouping,
      selectedGates,
      selectedDeliverableCategory
    );
  };
  selectAllGates(handlePlotUpdate);

  const handleNewPlot = (event: Event) => {
    const buttons = document.querySelectorAll("#groupingButtons button");
    buttons.forEach((button) => button.classList.remove("active"));
    (event.currentTarget as HTMLButtonElement).classList.add("active");

    handlePlotUpdate();
  };

  ["weekButton", "monthButton", "quarterButton", "yearButton"].forEach((id) => {
    getRequiredElementById(id).addEventListener("click", handleNewPlot);
  });

  ["deliverableCategorySelect", "gatesCheckboxes", "toggleColors"].forEach(
    (id) => {
      getRequiredElementById(id).addEventListener("change", handlePlotUpdate);
    }
  );

  const metricsButton = getRequiredElementById("metricsButton");
  const metricContainer = getRequiredElementById("metricContainer");
  metricContainer.classList.add("hidden");
  metricsButton.addEventListener("click", () => {
    metricContainer.classList.toggle("hidden");
  });
  const legendButton = getRequiredElementById("legendButton");
  legendButton.addEventListener("click", () => toggleLegend("gate"));

  function generateCSV(
    tableData: AssayMetrics[],
    timeRanges: string[]
  ): string {
    const csvRows: string[] = [];
    const parentHeaders = ["", ""];
    timeRanges.forEach((timeRange) => {
      parentHeaders.push(timeRange, "", "");
    });
    csvRows.push(parentHeaders.join(","));
    const subHeaders = ["Assay", "Step"];
    timeRanges.forEach(() => {
      subHeaders.push("Mean Days", "Median Days", "Test Count");
    });
    csvRows.push(subHeaders.join(","));
    tableData.forEach((row) => {
      const rowData: string[] = [row.assay, row.gate];
      timeRanges.forEach((timeRangeLabel) => {
        const timeRangeData = row.timeRanges.find(
          (tr) => tr.group === timeRangeLabel
        );
        rowData.push(
          timeRangeData ? timeRangeData.avgDays ?? NOT_AVAILABLE : NOT_AVAILABLE
        );
        rowData.push(
          timeRangeData
            ? timeRangeData.medianDays ?? NOT_AVAILABLE
            : NOT_AVAILABLE
        );
        rowData.push(
          timeRangeData
            ? timeRangeData.caseCount ?? NOT_AVAILABLE
            : NOT_AVAILABLE
        );
      });
      csvRows.push(rowData.join(","));
    });
    return csvRows.join("\n");
  }

  function downloadCSV(content: string, filename: string) {
    const blob = new Blob([content], { type: "text/csv;charset=utf-8;" });
    const link = document.createElement("a");
    const url = URL.createObjectURL(blob);

    link.setAttribute("href", url);
    link.setAttribute("download", filename);
    link.style.visibility = "hidden";

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
  const metricsDownloadButton = getRequiredElementById("metricsDownload");
  metricsDownloadButton.addEventListener("click", () => {
    const { tableData, timeRanges } = buildTableFromMetrics(
      jsonData,
      getSelectedGrouping(),
      getSelectedGates(),
      getSelectedDeliverableCategory()
    );
    const csvContent = generateCSV(tableData, sortTimeRanges(timeRanges));
    downloadCSV(csvContent, "metrics.csv");
  });
});
