import * as XLSX from "xlsx";
import Plotly from "plotly.js-dist-min";
import { postDownloadNewWindow } from "./util/requests";

let jsonData: any[] = []; // Declare jsonData at the top

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
    [gate: string]: { x: any[]; y: number[]; text: string[] };
  };
}

function getCompletedDateAndDays(
  row: any,
  gate: string,
  selectedDataType: string
) {
  let completedDate: Date | null = null;
  let days: number | null = null;

  const isCommonGate = ["Start Date", "Receipt"].includes(gate);

  switch (gate) {
    case "Extraction":
      completedDate = row["Extraction (EX) Completed"]
        ? new Date(row["Extraction (EX) Completed"])
        : null;
      days = row["EX Days"];
      break;
    case "Library Prep":
      completedDate = row["Library Prep. Completed"]
        ? new Date(row["Library Prep. Completed"])
        : null;
      days = row["Library Prep. Days"];
      break;
    case "Library Qual":
      completedDate = row["Library Qual. (LQ) Completed"]
        ? new Date(row["Library Qual. (LQ) Completed"])
        : null;
      days = row["LQ Total Days"];
      break;
    case "Full-Depth":
      completedDate = row["Full-Depth (FD) Completed"]
        ? new Date(row["Full-Depth (FD) Completed"])
        : null;
      days = row["FD Total Days"];
      break;
    case "All Completed":
      const completedDates = [
        row["CR Analysis Review Completed"],
        row["CR Release Approval Completed"],
        row["CR Release Completed"],
        row["DR Analysis Review Completed"],
        row["DR Release Approval Completed"],
        row["DR Release Completed"],
      ]
        .map((date) => (date ? new Date(date) : null))
        .filter((date) => date !== null);
      completedDate = completedDates.length
        ? new Date(Math.max(...completedDates.map((date) => date!.getTime())))
        : null;
      days =
        selectedDataType === "CR" ? row["CR Total Days"] : row["DR Total Days"];
      break;
    default:
      completedDate = isCommonGate
        ? row[`${gate} Completed`]
          ? new Date(row[`${gate} Completed`])
          : null
        : row[`${selectedDataType} ${gate} Completed`]
        ? new Date(row[`${selectedDataType} ${gate} Completed`])
        : row[`${gate} Completed`]
        ? new Date(row[`${gate} Completed`])
        : null;
      days = isCommonGate
        ? row[`${gate} Days`]
        : row[`${selectedDataType} ${gate} Days`] || row[`${gate} Days`];
      break;
  }

  return { completedDate, days };
}

function getGroupKey(date: Date, selectedGrouping: string): string {
  const year = date.getFullYear();
  const quarter = Math.floor(date.getMonth() / 3 + 1);

  switch (selectedGrouping) {
    case "week":
      return getWeek(date);
    case "month":
      return `${year}-${String(date.getMonth() + 1).padStart(2, "0")}`;
    case "fiscalQuarter":
      return `${year} Q${quarter}`;
    case "fiscalYear":
      return year.toString();
    default:
      return `${year} Q${quarter}`;
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
    const assay = row["Assay"];
    const totalDays = row[`${selectedDataType} Total Days`];
    const caseId = row["Case ID"];
    const releaseCompletedDate = row[`${selectedDataType} Release Completed`];

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

        if ((completedDate && days != null) || gate === "All Completed") {
          const date = new Date(
            gate === "All Completed"
              ? releaseCompletedDate || new Date()
              : completedDate
          );
          const groupKey = getGroupKey(date, selectedGrouping);

          if (!assayGroups[assay][gate]) {
            assayGroups[assay][gate] = { x: [], y: [], text: [] };
          }

          assayGroups[assay][gate].x.push(groupKey);
          assayGroups[assay][gate].y.push(
            gate === "All Completed" ? totalDays : days
          );
          assayGroups[assay][gate].text.push(
            `${caseId} (${gate === "All Completed" ? "All Completed" : gate})`
          );
        }
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
    let colorIndex = 0;

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
            boxpoints: "all",
            jitter: 0.3,
            pointpos: 0,
            hoverinfo: "text+y",
            hovertemplate: `
            %{text}<br>
            Days: %{y}
            `,
            marker: {
              size: 6,
              color: assayColors[assay],
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
  if (event.data.type === "excelData") {
    const trendReportContainer = document.getElementById(
      "trendReportContainer"
    );
    if (trendReportContainer) {
      fetch(event.data.content)
        .then((response) => response.arrayBuffer())
        .then((data) => {
          const workbook = XLSX.read(data, { type: "array" });
          const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
          jsonData = XLSX.utils.sheet_to_json(firstSheet); // Update jsonData

          plotData("fiscalQuarter", jsonData, getSelectedGates(), "CR");
        });
    }
  }
});

function parseUrlParams() {
  const params: { key: string; value: string }[] = [];
  window.location.search
    .substr(1)
    .split("&")
    .forEach((item) => {
      const [key, value] = item.split("=");
      if (key && value) {
        params.push({
          key: decodeURIComponent(key),
          value: decodeURIComponent(value).replace(/\+/g, " "),
        });
      }
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
  postDownloadNewWindow(
    "/rest/downloads/reports/case-tat-report",
    requestData,
    window
  );

  const updatePlot = () =>
    plotData(
      getSelectedGrouping(),
      jsonData,
      getSelectedGates(),
      getSelectedDataType()
    );

  document.getElementById("weekButton")?.addEventListener("click", updatePlot);
  document.getElementById("monthButton")?.addEventListener("click", updatePlot);
  document
    .getElementById("quarterButton")
    ?.addEventListener("click", updatePlot);
  document.getElementById("yearButton")?.addEventListener("click", updatePlot);
  document
    .getElementById("dataSelection")
    ?.addEventListener("change", updatePlot);
  document
    .getElementById("gatesCheckboxes")
    ?.addEventListener("change", updatePlot);
});
