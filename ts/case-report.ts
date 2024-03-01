import { AttributeDefinition, AttributeList } from "./component/attribute-list";
import { showAlertDialog } from "./component/dialog";
import { TableBuilder, TableDefinition } from "./component/table-builder";
import { Metric, MetricCategory, MetricSubcategory } from "./data/assay";
import {
  AnalysisQcGroup,
  Case,
  CaseDeliverable,
  Qcable,
  analysisMetricApplies,
  getAnalysisMetricCellHighlight,
  getAnalysisMetricValue,
  getDeliverableQcStatus,
  makeAnalysisMetricDisplay,
  subcategoryApplies as analysisSubcategoryApplies,
  Donor,
  deliverableTypeLabels,
} from "./data/case";
import { qcStatuses } from "./data/qc-status";
import { makeTextDiv } from "./util/html-utils";
import {
  addMetricValueContents,
  getFirstReviewStatus,
  getSampleMetricCellHighlight,
  metricApplies as sampleMetricApplies,
  RUN_METRIC_LABELS,
  Sample,
  subcategoryApplies as sampleSubcategoryApplies,
} from "./data/sample";
import { addTextDiv, makeNameDiv } from "./util/html-utils";
import { getMetricRequirementText } from "./util/metrics";
import { get } from "./util/requests";
import { siteConfig } from "./util/site-config";
import { urls } from "./util/urls";
import { BasicDropdownOption, Dropdown } from "./component/dropdown";

interface ReportSample {
  sample: Sample;
  allSamples?: Sample[]; // for run-level subcategories
  metricCategory: MetricCategory;
  metricSubcategory: MetricSubcategory;
  caseAssayId: number;
}

interface ReportAnalysisReview {
  kase: Case;
  qcGroup: AnalysisQcGroup;
  deliverable: CaseDeliverable;
  metricCategory: MetricCategory;
  metricSubcategory: MetricSubcategory;
}

const attributes: AttributeDefinition<Case>[] = [
  {
    title: "Case ID",
    addContents(object, fragment) {
      fragment.appendChild(
        makeNameDiv(object.id, undefined, urls.dimsum.case(object.id))
      );
    },
  },
  {
    title: "Requisition",
    addContents(object, fragment) {
      fragment.appendChild(
        makeNameDiv(
          object.requisition.name,
          urls.miso.requisition(object.requisition.id),
          urls.dimsum.requisition(object.requisition.id)
        )
      );
    },
  },
  {
    title: "Internal Name",
    addContents(object, fragment) {
      fragment.appendChild(
        makeNameDiv(
          object.donor.name,
          urls.miso.sample(object.donor.id),
          urls.dimsum.donor(object.donor.name)
        )
      );
    },
  },
  {
    title: "External Name",
    addContents(object, fragment) {
      addText(fragment, object.donor.externalName);
    },
  },
  {
    title: "Report Generated",
    addContents(object, fragment) {
      const date = new Date();
      const formatted =
        date.getFullYear() +
        "-" +
        pad(date.getMonth() + 1, 2) +
        "-" +
        pad(date.getDate(), 2) +
        " " +
        pad(date.getHours(), 2) +
        ":" +
        pad(date.getMinutes(), 2) +
        " ";
      addText(fragment, formatted);

      // Link to live view in print
      const a = document.createElement("a");
      a.setAttribute("href", window.location.href);
      a.className = "text-green-200 font-bold hover:underline print-only";
      a.innerHTML = "(See Live)";
      fragment.appendChild(a);
    },
  },
  {
    title: "Assay",
    addContents(object, fragment) {
      const assay = siteConfig.assaysById[object.assayId];
      const assayDiv = makeNameDiv(
        `${assay.description || assay.name} v${assay.version}`,
        urls.miso.assay(object.assayId),
        undefined
      );
      fragment.appendChild(assayDiv);
    },
  },
];

const sampleGateMetricsDefinition: TableDefinition<ReportSample, Metric> = {
  disablePageControls: true,
  getChildren(parent) {
    return parent.metricSubcategory.metrics
      .filter((metric) => sampleMetricApplies(metric, parent.sample))
      .sort((a, b) => (a.sortPriority || 0) - (b.sortPriority || 0));
  },
  getSubheading: (object) => object.metricSubcategory.name || null,
  noChildrenWarning: "Metrics missing",
  generateColumns: () => [
    {
      title: "Item",
      headingClass: "print-width-25",
      addParentContents(object, fragment) {
        if (object.allSamples) {
          // this is a run-level item
          const run = getRun(object.sample);
          const runName = run.name;
          fragment.appendChild(
            makeWrappingNameDiv(
              runName,
              urls.miso.run(runName),
              urls.dimsum.run(runName)
            )
          );
          object.allSamples.forEach((sample) => {
            fragment.appendChild(
              makeWrappingNameDiv(
                sample.name +
                  (sample.sequencingLane ? ` (L${sample.sequencingLane})` : ""),
                urls.miso.sample(sample.id)
              )
            );
          });
          return;
        }
        fragment.appendChild(
          makeWrappingNameDiv(
            object.sample.name,
            urls.miso.sample(object.sample.id)
          )
        );
        if (object.sample.run) {
          const runName = object.sample.run.name;
          const runNameDisplay =
            runName +
            (object.sample.sequencingLane
              ? " (L" + object.sample.sequencingLane + ")"
              : "");
          fragment.appendChild(
            makeWrappingNameDiv(
              runNameDisplay,
              urls.miso.run(runName),
              urls.dimsum.run(runName)
            )
          );
        }
      },
    },
    {
      title: "Metric",
      headingClass: "print-width-20",
      child: true,
      addChildContents(object, parent, fragment) {
        addText(fragment, object.name);
      },
    },
    {
      title: "Threshold",
      child: true,
      addChildContents(object, parent, fragment) {
        if (object.thresholdType === "BOOLEAN") {
          addText(fragment, "n/a");
        } else {
          addText(fragment, getMetricRequirementText(object));
        }
      },
    },
    {
      title: "Value",
      headingClass: "print-width-20",
      child: true,
      addChildContents(object, parent, fragment) {
        addMetricValueContents(parent.sample, [object], fragment, false, false);
      },
      getCellHighlight(reportSample, metric) {
        if (metric == null) {
          return "na";
        } else {
          return getSampleMetricCellHighlight(
            reportSample.sample,
            metric.name,
            reportSample.metricCategory
          );
        }
      },
    },
    {
      title: "QC Metric Sign-Off",
      addParentContents(object, fragment) {
        displayQcSignOff(fragment, getRunOrSampleLevel(object));
        addAssayMismatchText(fragment, object);
      },
      getCellHighlight(object) {
        const qcable = getRunOrSampleLevel(object);
        const reviewStatus = getFirstReviewStatus(qcable).cellStatus;
        if (reviewStatus) {
          return reviewStatus;
        } else if (object.sample.assayId !== object.caseAssayId) {
          return "warning";
        }
        return null;
      },
    },
    {
      title: "QC Stage Sign-Off",
      addParentContents(object, fragment) {
        if (object.sample.run) {
          displayDataReviewSignOff(fragment, getRunOrSampleLevel(object));
          addAssayMismatchText(fragment, object);
        } else {
          addText(fragment, "No second review required");
        }
      },
      getCellHighlight(object) {
        const assayStatus =
          object.sample.assayId === object.caseAssayId ? null : "warning";
        if (object.sample.run) {
          const qcable = getRunOrSampleLevel(object);
          if (!qcable.dataReviewDate) {
            return qcStatuses.dataReview.cellStatus;
          } else if (qcable.dataReviewPassed) {
            return assayStatus;
          } else {
            return qcStatuses.failed.cellStatus;
          }
        } else {
          return "na";
        }
      },
    },
  ],
};

function getRunOrSampleLevel(object: ReportSample): Qcable {
  if (object.allSamples) {
    // this is a run-level item
    return getRun(object.sample);
  } else {
    return object.sample;
  }
}

function getRun(sample: Sample) {
  if (!sample.run) {
    throw new Error("Multi-sample ReportSample must have a run");
  }
  return sample.run;
}

function makeWrappingNameDiv(
  name: string,
  misoUrl?: string,
  dimsumUrl?: string
) {
  const div = makeNameDiv(name.replaceAll("_", "_<wbr>"), misoUrl, dimsumUrl);
  div.classList.add("pl-2", "-indent-2"); // hanging indent
  return div;
}

function addAssayMismatchText(fragment: Node, object: ReportSample) {
  if (object.sample.assayId !== object.caseAssayId) {
    if (object.sample.assayId) {
      const sampleAssay = siteConfig.assaysById[object.sample.assayId];
      addBoldText(
        fragment,
        `(Assay: ${sampleAssay.name} v${sampleAssay.version})`
      );
    } else {
      addBoldText(fragment, `(Unspecified assay)`);
    }
  }
}

const analysisReviewMetricsDefinition: TableDefinition<
  ReportAnalysisReview,
  Metric
> = {
  disablePageControls: true,
  getChildren(parent) {
    return parent.metricSubcategory.metrics.filter((metric) =>
      analysisMetricApplies(metric, parent.qcGroup)
    );
  },
  getSubheading: (object) =>
    deliverableTypeLabels[object.deliverable.deliverableType],
  generateColumns: () => [
    {
      title: "Item",
      headingClass: "print-width-20",
      addParentContents(object, fragment) {
        addTextDiv(getQcGroupName(object.kase.donor, object.qcGroup), fragment);
        if (object.qcGroup.groupId) {
          addTextDiv(`Group ID: ${object.qcGroup.groupId}`, fragment);
        }
      },
    },
    {
      title: "Metric",
      headingClass: "print-width-20",
      child: true,
      addChildContents(object, parent, fragment) {
        addText(fragment, object.name);
      },
    },
    {
      title: "Threshold",
      child: true,
      addChildContents(object, parent, fragment) {
        if (object.thresholdType === "BOOLEAN") {
          addText(fragment, "n/a");
        } else {
          addText(fragment, getMetricRequirementText(object));
        }
      },
    },
    {
      title: "Value",
      headingClass: "print-width-20",
      child: true,
      addChildContents(object, parent, fragment) {
        if (object.name === "Trimming; Minimum base quality Q") {
          fragment.appendChild(
            document.createTextNode("Standard pipeline removes reads below Q30")
          );
        } else {
          const value = getAnalysisMetricValue(object.name, parent.qcGroup);
          fragment.appendChild(
            makeAnalysisMetricDisplay([object], parent.qcGroup, false)
          );
        }
      },
      getCellHighlight(object, metric) {
        if (metric) {
          return getAnalysisMetricCellHighlight(
            object.qcGroup,
            object.kase.requisition,
            metric.name
          );
        } else {
          return "na";
        }
      },
    },
    {
      title: "QC Metric Sign-Off",
      addParentContents(object, fragment) {
        const deliverable = object.deliverable;
        displaySignOff(
          fragment,
          deliverable.analysisReviewQcPassed,
          undefined,
          deliverable.analysisReviewQcUser,
          deliverable.analysisReviewQcDate
        );
      },
      getCellHighlight(object) {
        return getDeliverableQcStatus(object.deliverable.analysisReviewQcPassed)
          .cellStatus;
      },
    },
    {
      title: "QC Stage Sign-Off",
      addParentContents(object, fragment) {
        addText(fragment, "No second review required");
      },
      getCellHighlight(object) {
        return "na";
      },
    },
  ],
};

function displayQcSignOff(fragment: DocumentFragment, qcable: Qcable) {
  displaySignOff(
    fragment,
    qcable.qcPassed,
    qcable.qcReason,
    qcable.qcUser,
    qcable.qcDate,
    qcable.qcNote
  );
}

function displayDataReviewSignOff(fragment: DocumentFragment, qcable: Qcable) {
  displaySignOff(
    fragment,
    qcable.dataReviewPassed,
    undefined,
    qcable.dataReviewUser,
    qcable.dataReviewDate
  );
}

function displaySignOff(
  fragment: DocumentFragment,
  qcPassed?: boolean,
  qcReason?: string,
  qcUser?: string,
  qcDate?: string,
  qcNote?: string
) {
  if (qcDate) {
    if (!qcUser) {
      throw new Error("QC user expected if qc date is set");
    }
    addBoldText(fragment, qcReason || (qcPassed ? "Passed" : "Failed"));
    addTextDiv(qcUser, fragment);
    addTextDiv(qcDate, fragment);
    if (qcNote) {
      const noteDiv = makeTextDiv("Note: " + qcNote);
      noteDiv.classList.add("mt-1em");
      fragment.appendChild(noteDiv);
    }
  } else {
    addPendingText(fragment);
  }
}

function addPendingText(node: Node) {
  addBoldText(node, "Pending");
}

function addBoldText(node: Node, text: string) {
  const div = document.createElement("div");
  div.classList.add("font-bold");
  addText(div, text);
  node.appendChild(div);
}

function addText(node: Node, text: string) {
  node.appendChild(document.createTextNode(text));
}

function pad(value: number, length: number): string {
  return value.toString().padStart(length, "0");
}

function getQcGroupName(donor: Donor, qcGroup: AnalysisQcGroup) {
  return `${donor.name}_${qcGroup.tissueOrigin}_${qcGroup.tissueType}_${qcGroup.libraryDesignCode}`;
}

async function loadCase(caseId: string) {
  const response = await get(urls.rest.cases.get(caseId));
  if (!response.ok) {
    throw new Error(`Error loading case: ${response.status}`);
  }
  const data: Case = await response.json();
  new AttributeList<Case>("caseAttributesContainer", data, attributes);

  const receipts = getReportSamples(data, data.receipts, "RECEIPT");
  new TableBuilder(sampleGateMetricsDefinition, "receiptTableContainer").build(
    receipts
  );

  const extractions = getReportSamples(
    data,
    data.tests.flatMap((test) => test.extractions),
    "EXTRACTION"
  );
  new TableBuilder(
    sampleGateMetricsDefinition,
    "extractionTableContainer"
  ).build(extractions);

  const libraryPreps = getReportSamples(
    data,
    data.tests.flatMap((test) => test.libraryPreparations),
    "LIBRARY_PREP"
  );
  new TableBuilder(
    sampleGateMetricsDefinition,
    "libraryPreparationTableContainer"
  ).build(libraryPreps);

  const libraryQualifications = getReportSamples(
    data,
    data.tests.flatMap((test) => test.libraryQualifications),
    "LIBRARY_QUALIFICATION"
  );
  new TableBuilder(
    sampleGateMetricsDefinition,
    "libraryQualificationTableContainer"
  ).build(libraryQualifications);

  const fullDepths = getReportSamples(
    data,
    data.tests.flatMap((test) => test.fullDepthSequencings),
    "FULL_DEPTH_SEQUENCING"
  );
  new TableBuilder(
    sampleGateMetricsDefinition,
    "fullDepthSequencingTableContainer"
  ).build(fullDepths);
  const analysisReviews = getReportAnalysisReviews(data);
  const analysisReviewTable = new TableBuilder(
    analysisReviewMetricsDefinition,
    "analysisReviewTableContainer"
  ).build(analysisReviews);

  addDeliverablesMenu(analysisReviewTable, analysisReviews);
  setupPrint(data);
}

function addDeliverablesMenu(
  analysisReviewTable: TableBuilder<ReportAnalysisReview, Metric>,
  analysisReviews: ReportAnalysisReview[]
) {
  const deliverableTypes = analysisReviews
    .map((review) => review.deliverable.deliverableType)
    .filter(
      (deliverable, index, array) => array.indexOf(deliverable) === index
    );

  if (deliverableTypes.length <= 1) {
    // Only one option - don't show menu
    return;
  }
  const deliverableMenuContainerDiv = document.getElementById(
    "deliverableMenuContainer"
  );
  if (!deliverableMenuContainerDiv) {
    throw new Error("Deliverable menu container missing");
  }
  const deliverableOptions = deliverableTypes.map(
    (deliverable) =>
      new BasicDropdownOption(deliverableTypeLabels[deliverable], () => {
        analysisReviewTable.clear();
        analysisReviewTable.build(
          analysisReviews.filter(
            (review) => review.deliverable.deliverableType === deliverable
          )
        );
      })
  );
  deliverableOptions.unshift(
    new BasicDropdownOption("All", () => {
      analysisReviewTable.clear();
      analysisReviewTable.build(analysisReviews);
    })
  );
  const deliverableDropdown = new Dropdown(
    deliverableOptions,
    true,
    "Deliverables",
    "All"
  );
  deliverableMenuContainerDiv.appendChild(
    deliverableDropdown.getContainerTag()
  );
}

function getReportSamples(
  kase: Case,
  samples: Sample[],
  category: MetricCategory
): ReportSample[] {
  // make one record per applicable sample+subcategory combination
  return samples
    .flatMap((sample: Sample): ReportSample[] => {
      if (!sample.assayId) {
        throw new Error("Samples must have assays");
      }
      const assay = siteConfig.assaysById[sample.assayId];
      if (!assay.metricCategories[category]) {
        return [
          {
            sample: sample,
            metricCategory: category,
            metricSubcategory: {
              metrics: [],
            },
            caseAssayId: kase.assayId,
          },
        ];
      }
      return assay.metricCategories[category]
        .filter((subcategory) => sampleSubcategoryApplies(subcategory, sample))
        .map((subcategory) => {
          return {
            sample: sample,
            metricCategory: category,
            metricSubcategory: subcategory,
            caseAssayId: kase.assayId,
          };
        });
    })
    .sort((a, b) => {
      // sort by subcategory sortOrder > run name > sample name
      const aSubcategorySort = a.metricSubcategory.sortPriority || 0;
      const bSubcategorySort = b.metricSubcategory.sortPriority || 0;
      if (aSubcategorySort !== bSubcategorySort) {
        return aSubcategorySort - bSubcategorySort;
      }
      if (
        a.sample.run &&
        b.sample.run &&
        a.sample.run.name !== b.sample.run.name
      ) {
        return a.sample.run.name.localeCompare(b.sample.run.name);
      }
      return a.sample.name.localeCompare(b.sample.name);
    })
    .reduce((accumulator: ReportSample[], current: ReportSample, i: number) => {
      if (
        current.metricSubcategory.metrics &&
        current.metricSubcategory.metrics.length &&
        current.metricSubcategory.metrics.every((metric) =>
          RUN_METRIC_LABELS.includes(metric.name)
        )
      ) {
        // This is a run-level subcategory. If the previous item is the same subcategory for the
        // same run with the same assay, combine this item into the previous item
        current.allSamples = [current.sample];
        const previous = i > 0 ? accumulator[accumulator.length - 1] : null;
        if (
          previous &&
          current.sample.run &&
          previous.sample.run &&
          current.sample.run.id === previous.sample.run.id &&
          current.sample.assayId === previous.sample.assayId &&
          current.metricSubcategory.name === previous.metricSubcategory.name
        ) {
          previous.allSamples?.push(current.sample);
          return accumulator;
        }
      }
      accumulator.push(current);
      return accumulator;
    }, []);
}

function getReportAnalysisReviews(kase: Case) {
  const assay = siteConfig.assaysById[kase.assayId];
  return kase.deliverables
    .flatMap((deliverable) => {
      return kase.qcGroups.flatMap((qcGroup) => {
        return assay.metricCategories.ANALYSIS_REVIEW.filter((subcategory) =>
          analysisSubcategoryApplies(subcategory, qcGroup)
        ).map((subcategory): ReportAnalysisReview => {
          return {
            kase: kase,
            qcGroup: qcGroup,
            deliverable: deliverable,
            metricCategory: "ANALYSIS_REVIEW",
            metricSubcategory: subcategory,
          };
        });
      });
    })
    .sort((a, b) => {
      // sort by deliverable type > subcategory > item name
      if (a.deliverable.deliverableType < b.deliverable.deliverableType) {
        return -1;
      } else if (
        a.deliverable.deliverableType > b.deliverable.deliverableType
      ) {
        return 1;
      }
      const aSubcategorySort = a.metricSubcategory.sortPriority || 0;
      const bSubcategorySort = b.metricSubcategory.sortPriority || 0;
      if (aSubcategorySort !== bSubcategorySort) {
        return aSubcategorySort - bSubcategorySort;
      }
      const aName = getQcGroupName(a.kase.donor, a.qcGroup);
      const bName = getQcGroupName(b.kase.donor, b.qcGroup);
      return aName.localeCompare(bName);
    });
}

function setupPrint(kase: Case) {
  const printButton = document.getElementById("printButton");
  if (printButton) {
    printButton.onclick = async (event) => {
      if (!(window as any).chrome) {
        await showAlertDialog(
          "Warning",
          "Printing is optimized for Google Chrome"
        );
      }
      // update title temporarily to set default filename for printing to PDF
      const pageTitle = document.title;
      const tumourReceipts = kase.receipts.filter(
        (sample) => sample.tissueType !== "R"
      );
      if (tumourReceipts && tumourReceipts[0].groupId) {
        document.title = `${tumourReceipts[0].groupId}_qc`;
      } else {
        document.title = `${kase.requisition.name}_qc`;
      }
      window.print();
      document.title = pageTitle;
    };
  }
}

// load page
const contentDiv = document.getElementById("content");
if (!contentDiv) {
  throw new Error("Content container missing");
}
const caseId = contentDiv.getAttribute("data-case-id");
if (!caseId) {
  throw new Error("Missing case ID data attribute");
}
loadCase(caseId);
