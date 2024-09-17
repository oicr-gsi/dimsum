import Plotly from "plotly.js-dist-min";
import { post } from "./util/requests";
import { getRequiredElementById } from "./util/html-utils";
import { toggleLegend } from "./component/legend";
import { getColorForGate } from "./util/color-mapping";

let jsonData: any[] = [];
const uirevision = "true";

// constants for column names in the Case TAT Report
const COLUMN_NAMES = {
  ASSAY: "Assay",
  CASE_ID: "Case ID",
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
  CR_COMPLETED: "CR Release Completed",
  DR_COMPLETED: "DR Release Completed",
  ALL_COMPLETED: "ALL Release Completed",
};

// function to construct completion date column name
function getCompletionColumnName(dataType: string, gate: string): string {
  return `${DATA_PREFIX_MAPPING[dataType]} ${gate} Completed`.trim();
}

// function to construct days column name
function getDaysColumnName(dataType: string, gate: string): string {
  return `${DATA_PREFIX_MAPPING[dataType]} ${gate} Days`.trim();
}

const DATA_SELECTION = {
  CLINICAL_REPORT: "ClinicalReport",
  DATA_RELEASE: "DataRelease",
  ALL: "All",
};

const DATA_PREFIX_MAPPING: { [key: string]: string } = {
  [DATA_SELECTION.CLINICAL_REPORT]: "CR",
  [DATA_SELECTION.DATA_RELEASE]: "DR",
  [DATA_SELECTION.ALL]: "ALL",
};

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

function getCompletedDateAndDays(
  row: any,
  gate: string,
  selectedDataType: string
) {
  let completedDate: Date | null = null;
  let days: number = 0;

  switch (gate) {
    case "Receipt":
      completedDate = row[COLUMN_NAMES.RC_COMPLETED]
        ? new Date(row[COLUMN_NAMES.RC_COMPLETED])
        : null;
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
    case "Full Case":
      completedDate = row[getCompletionColumnName(selectedDataType, "Release")]
        ? new Date(row[getCompletionColumnName(selectedDataType, "Release")])
        : null;
      days = row[getDaysColumnName(selectedDataType, "Total")] ?? 0;
      break;
    default:
      completedDate = row[getCompletionColumnName(selectedDataType, gate)]
        ? new Date(row[getCompletionColumnName(selectedDataType, gate)])
        : null;
      days = row[getDaysColumnName(selectedDataType, gate)] ?? 0;
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

function groupData(
  jsonData: any[],
  selectedGrouping: string,
  selectedGates: string[],
  selectedDataType: string
): AssayGroups {
  const assayGroups: AssayGroups = {};

  jsonData.forEach((row: any) => {
    const assay = row[COLUMN_NAMES.ASSAY];
    const caseId = row[COLUMN_NAMES.CASE_ID];
    const completedKey =
      `${DATA_PREFIX_MAPPING[selectedDataType]}_COMPLETED` as keyof typeof COLUMN_NAMES;
    const caseCompletedDate = row[COLUMN_NAMES[completedKey]];

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
          selectedDataType
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

function plotData(
  jsonData: any[],
  selectedGrouping: string,
  selectedGates: string[],
  selectedDataType: string
): { newPlot: Partial<Plotly.PlotData>[]; layout: Partial<Plotly.Layout> } {
  const assayGroups = groupData(
    jsonData,
    selectedGrouping,
    selectedGates,
    selectedDataType
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
            N: %{customdata}
          `,
          customdata: Array(assayGroups[assay][gate].x.length).fill(
            assayGroups[assay][gate].n
          ),
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
  selectedDataType: string
) {
  const plotContainer = getRequiredElementById("plotContainer");
  plotContainer.textContent = "";

  const { newPlot, layout } = plotData(
    jsonData,
    selectedGrouping,
    selectedGates,
    selectedDataType
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
  selectedDataType: string
) {
  const { newPlot, layout } = plotData(
    jsonData,
    selectedGrouping,
    selectedGates,
    selectedDataType
  );
  Plotly.react("plotContainer", newPlot, layout);
}

function updatePlotWithLegend(
  selectedGrouping: string,
  jsonData: any[],
  selectedGates: string[],
  selectedDataType: string
) {
  updatePlot(selectedGrouping, jsonData, selectedGates, selectedDataType);
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

function getSelectedDataType(): string {
  const dataSelection = getRequiredElementById(
    "dataSelection"
  ) as HTMLSelectElement;
  return dataSelection.value;
}

window.addEventListener("load", () => {
  const params = parseUrlParams();
  const requestData = { filters: params.length > 0 ? params : [] };

  post("/rest/downloads/reports/case-tat-report/data", requestData)
    .then((data) => {
      jsonData = data; // update jsonData with fetched data
      newPlot(
        getSelectedGrouping(),
        jsonData,
        getSelectedGates(),
        getSelectedDataType()
      );
    })
    .catch((error) => {
      alert("Error fetching data: " + error);
    });

  const handlePlotUpdate = () => {
    const selectedGrouping = getSelectedGrouping();
    const selectedGates = getSelectedGates();
    const selectedDataType = getSelectedDataType();
    updatePlotWithLegend(
      selectedGrouping,
      jsonData,
      selectedGates,
      selectedDataType
    );
  };

  const handleNewPlot = (event: Event) => {
    const buttons = document.querySelectorAll("#groupingButtons button");
    buttons.forEach((button) => button.classList.remove("active"));
    (event.currentTarget as HTMLButtonElement).classList.add("active");

    handlePlotUpdate();
  };

  ["weekButton", "monthButton", "quarterButton", "yearButton"].forEach((id) => {
    getRequiredElementById(id).addEventListener("click", handleNewPlot);
  });

  ["dataSelection", "gatesCheckboxes", "toggleColors"].forEach((id) => {
    getRequiredElementById(id).addEventListener("change", handlePlotUpdate);
  });

  const legendButton = getRequiredElementById("legendButton");
  legendButton.addEventListener("click", () => toggleLegend("gate"));
});
