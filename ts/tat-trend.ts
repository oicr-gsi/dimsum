import Plotly from "plotly.js-dist-min";
import { postNewWindow } from "./util/requests";

let jsonData: any[] = [];
const uirevision = "true"; // Variable to maintain revision state

const GATE = {
  EX_COMPLETED: "Extraction (EX) Completed",
  LP_COMPLETED: "Library Prep. Completed",
  LQ_COMPLETED: "Library Qual. (LQ) Completed",
  FD_COMPLETED: "Full-Depth (FD) Completed",
  ALL_COMPLETED: "ALL Release Completed",
  TOTAL_DAYS: "Total Days",
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
  let days: number | null = null;

  if (gate === "Extraction") {
    completedDate = row[GATE.EX_COMPLETED]
      ? new Date(row[GATE.EX_COMPLETED])
      : null;
    days = row["EX Days"] ?? 0;
  } else if (gate === "Library Prep") {
    completedDate = row[GATE.LP_COMPLETED]
      ? new Date(row[GATE.LP_COMPLETED])
      : null;
    days = row["Library Prep. Days"] ?? 0;
  } else if (gate === "Library Qual") {
    completedDate = row[GATE.LQ_COMPLETED]
      ? new Date(row[GATE.LQ_COMPLETED])
      : null;
    days = row["LQ Total Days"] ?? 0;
  } else if (gate === "Full-Depth") {
    completedDate = row[GATE.FD_COMPLETED]
      ? new Date(row[GATE.FD_COMPLETED])
      : null;
    days = row["FD Total Days"] ?? 0;
  } else if (gate === "All Completed") {
    if (selectedDataType === "AL") {
      completedDate = row[GATE.ALL_COMPLETED]
        ? new Date(row[GATE.ALL_COMPLETED])
        : null;
      days = row["ALL Total Days"] ?? 0;
    } else {
      completedDate = row[`${selectedDataType} Release Completed`]
        ? new Date(row[`${selectedDataType} Release Completed`])
        : null;
      days = row[`${selectedDataType} Total Days`] ?? 0;
    }
  } else {
    completedDate = row[`${selectedDataType} ${gate} Completed`]
      ? new Date(row[`${selectedDataType} ${gate} Completed`])
      : row[`${gate} Completed`]
      ? new Date(row[`${gate} Completed`])
      : null;
    days = row[`${selectedDataType} ${gate} Days`] ?? row[`${gate} Days`] ?? 0;
  }

  return { completedDate, days };
}

function getGroup(date: Date, selectedGrouping: string): string {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  let fiscalYear = year;
  let fiscalQuarter: number;

  if (month >= 4) {
    fiscalYear = year + 1; // next fiscal year
    fiscalQuarter = Math.floor((month - 4) / 3) + 1;
  } else {
    fiscalYear = year; // current fiscal year
    fiscalQuarter = Math.floor((month + 12 - 4) / 3) + 1;
  }
  const fiscalYearString = `FY${fiscalYear - 1}/${String(fiscalYear).slice(
    -2
  )}`;
  switch (selectedGrouping) {
    case "week":
      return getWeek(date);
    case "month":
      return `${fiscalYearString} - ${String(month).padStart(2, "0")}`;
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
  selectedDataType: string,
  includeIncomplete: boolean = false
): AssayGroups {
  const assayGroups: AssayGroups = {};

  jsonData.forEach((row: any) => {
    const assay = row["Assay"];
    const caseId = row["Case ID"];
    const caseCompletedDate =
      row[`${selectedDataType} Release Completed`] ?? row[GATE.ALL_COMPLETED];
    const totalDays =
      row[`${selectedDataType} Total Days`] ?? row["ALL Total Days"];

    // skip if case completion date is missing and includeIncomplete is false
    if (!caseCompletedDate && !includeIncomplete) {
      return;
    }
    if (assay && totalDays != null && caseId != null) {
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
        const date = new Date(caseCompletedDate || completedDate);
        const group = getGroup(date, selectedGrouping);
        if (!assayGroups[assay][gate]) {
          assayGroups[assay][gate] = { x: [], y: [], text: [], n: 0 };
        }
        assayGroups[assay][gate].x.push(group);
        assayGroups[assay][gate].y.push(days ?? totalDays);
        assayGroups[assay][gate].text.push(
          `${caseId} (${gate === "All Completed" ? "All Completed" : gate})`
        );
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
  const toggleColors = document.getElementById(
    "toggleColors"
  ) as HTMLInputElement;
  return toggleColors ? toggleColors.checked : false;
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
            color: colorByGate ? gateColors[gate] : assayColors[assay],
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
    width: window.innerWidth - 50,
    height: window.innerHeight - 250,
  };

  return { newPlot, layout };
}

function newPlot(
  selectedGrouping: string,
  jsonData: any[],
  selectedGates: string[],
  selectedDataType: string
) {
  const trendReportContainer = document.getElementById("trendReportContainer");

  if (trendReportContainer) {
    const plotContainer = document.getElementById("plotContainer");
    if (plotContainer) {
      plotContainer.textContent = "";
    }

    const { newPlot, layout } = plotData(
      jsonData,
      selectedGrouping,
      selectedGates,
      selectedDataType
    );
    Plotly.newPlot("plotContainer", newPlot, layout);

    window.addEventListener("resize", () => {
      Plotly.relayout("plotContainer", {
        width: window.innerWidth,
        height: window.innerHeight,
      });
    });
  }
}

function updatePlot(
  selectedGrouping: string,
  jsonData: any[],
  selectedGates: string[],
  selectedDataType: string
) {
  const trendReportContainer = document.getElementById("trendReportContainer");

  if (trendReportContainer) {
    const plotContainer = document.getElementById("plotContainer");

    const { newPlot, layout } = plotData(
      jsonData,
      selectedGrouping,
      selectedGates,
      selectedDataType
    );
    Plotly.react("plotContainer", newPlot, layout);
  }
}

window.addEventListener("message", (event) => {
  if (event.data.type === "jsonData") {
    const trendReportContainer = document.getElementById(
      "trendReportContainer"
    );
    if (trendReportContainer) {
      jsonData = event.data.content; // update jsonData when new data is received
      newPlot(
        getSelectedGrouping(),
        jsonData,
        getSelectedGates(),
        getSelectedDataType()
      );
    }
  }
});

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
  return selectedButton
    ? selectedButton.id.replace("Button", "")
    : "fiscalQuarter";
}

function getSelectedDataType(): string {
  const dataSelection = document.getElementById(
    "dataSelection"
  ) as HTMLSelectElement;
  return dataSelection ? dataSelection.value : "CR";
}

document.addEventListener("DOMContentLoaded", () => {
  const params = parseUrlParams();
  const requestData = { filters: params.length > 0 ? params : [] };
  postNewWindow(
    "/rest/downloads/reports/case-tat-report/data",
    requestData,
    window
  );

  const handlePlotUpdate = () => {
    const selectedGates = getSelectedGates();
    const selectedDataType = getSelectedDataType();
    updatePlot(
      getSelectedGrouping(),
      jsonData,
      selectedGates,
      selectedDataType
    );
  };

  const handleNewPlot = (event: Event) => {
    const buttons = document.querySelectorAll("#groupingButtons button");
    buttons.forEach((button) => button.classList.remove("active"));
    (event.currentTarget as HTMLButtonElement).classList.add("active");

    const selectedGrouping = (event.currentTarget as HTMLButtonElement).dataset
      .grouping;
    const selectedGates = getSelectedGates();
    const selectedDataType = getSelectedDataType();
    newPlot(selectedGrouping!, jsonData, selectedGates, selectedDataType);
  };

  const weekButton = document.getElementById("weekButton");
  const monthButton = document.getElementById("monthButton");
  const quarterButton = document.getElementById("quarterButton");
  const yearButton = document.getElementById("yearButton");

  weekButton?.addEventListener("click", handleNewPlot);
  monthButton?.addEventListener("click", handleNewPlot);
  quarterButton?.addEventListener("click", handleNewPlot);
  yearButton?.addEventListener("click", handleNewPlot);

  document
    .getElementById("dataSelection")
    ?.addEventListener("change", handlePlotUpdate);
  document
    .getElementById("gatesCheckboxes")
    ?.addEventListener("change", handlePlotUpdate);
  document
    .getElementById("toggleColors")
    ?.addEventListener("change", handlePlotUpdate);

  // initial plot generation
  newPlot(
    getSelectedGrouping(),
    jsonData,
    getSelectedGates(),
    getSelectedDataType()
  );
});
