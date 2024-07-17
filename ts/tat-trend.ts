import Plotly from "plotly.js-dist-min";
import { postNewWindow } from "./util/requests";

let jsonData: any[] = [];

function generateColor(index: number): string {
  const colors = [
    "#1f77b4",
    "#ff7f0e",
    "#2ca02c",
    "#d62728",
    "#9467bd",
    "#8c564b",
    "#e377c2",
    "#7f7f7f",
    "#bcbd22",
    "#17becf",
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
    completedDate = row["Extraction (EX) Completed"]
      ? new Date(row["Extraction (EX) Completed"])
      : null;
    days = row["EX Days"] ?? 0;
  } else if (gate === "Library Prep") {
    completedDate = row["Library Prep. Completed"]
      ? new Date(row["Library Prep. Completed"])
      : null;
    days = row["Library Prep. Days"] ?? 0;
  } else if (gate === "Library Qual") {
    completedDate = row["Library Qual. (LQ) Completed"]
      ? new Date(row["Library Qual. (LQ) Completed"])
      : null;
    days = row["LQ Total Days"] ?? 0;
  } else if (gate === "Full-Depth") {
    completedDate = row["Full-Depth (FD) Completed"]
      ? new Date(row["Full-Depth (FD) Completed"])
      : null;
    days = row["FD Total Days"] ?? 0;
  } else if (gate === "All Completed") {
    if (selectedDataType === "AL") {
      completedDate = row["ALL Release Completed"]
        ? new Date(row["ALL Release Completed"])
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

function getGroupKey(date: Date, selectedGrouping: string): string {
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
      row[`${selectedDataType} Release Completed`] ??
      row["ALL Release Completed"];
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
        const date = new Date(caseCompletedDate || completedDate || new Date());
        const groupKey = getGroupKey(date, selectedGrouping);
        if (!assayGroups[assay][gate]) {
          assayGroups[assay][gate] = { x: [], y: [], text: [], n: 0 };
        }
        assayGroups[assay][gate].x.push(groupKey);
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
  const toggleAnnotations = document.getElementById(
    "toggleAnnotations"
  ) as HTMLInputElement;
  return toggleAnnotations ? toggleAnnotations.checked : false;
}

function plotData(
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
    const assayGroups = groupData(
      jsonData,
      selectedGrouping,
      selectedGates,
      selectedDataType
    );
    const plotData: Partial<Plotly.PlotData>[] = [];
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
          plotData.push({
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
            showlegend: !plotData.some((d) => d.legendgroup === assay),
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
      width: window.innerWidth - 50,
      height: window.innerHeight - 250,
    };

    Plotly.newPlot("plotContainer", plotData, layout);

    window.addEventListener("resize", () => {
      Plotly.relayout("plotContainer", {
        width: window.innerWidth,
        height: window.innerHeight,
      });
    });
  }
}

window.addEventListener("message", (event) => {
  if (event.data.type === "jsonData") {
    const trendReportContainer = document.getElementById(
      "trendReportContainer"
    );
    if (trendReportContainer) {
      jsonData = event.data.content; // update jsonData when new data is received
      plotData(
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
  const selectedButton = document.querySelector(
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

  const updatePlot = (grouping: string) =>
    plotData(grouping, jsonData, getSelectedGates(), getSelectedDataType());

  const weekButton = document.getElementById("weekButton");
  const monthButton = document.getElementById("monthButton");
  const quarterButton = document.getElementById("quarterButton");
  const yearButton = document.getElementById("yearButton");

  weekButton?.addEventListener("click", () => {
    updatePlot("week");
  });

  monthButton?.addEventListener("click", () => {
    updatePlot("month");
  });

  quarterButton?.addEventListener("click", () => {
    updatePlot("fiscalQuarter");
  });

  yearButton?.addEventListener("click", () => {
    updatePlot("fiscalYear");
  });

  document
    .getElementById("dataSelection")
    ?.addEventListener("change", () => updatePlot(getSelectedGrouping()));
  document
    .getElementById("gatesCheckboxes")
    ?.addEventListener("change", () => updatePlot(getSelectedGrouping()));
  document
    .getElementById("toggleAnnotations")
    ?.addEventListener("change", () => updatePlot(getSelectedGrouping()));

  // initial plot generation
  plotData(
    getSelectedGrouping(),
    jsonData,
    getSelectedGates(),
    getSelectedDataType()
  );
});
