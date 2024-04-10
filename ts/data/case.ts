import {
  ColumnDefinition,
  legendAction,
  TableDefinition,
} from "../component/table-builder";
import {
  makeIcon,
  styleText,
  makeNameDiv,
  addNaText,
  addTextDiv,
  makeTextDivWithTooltip,
  makeTextDiv,
  CellStatus,
} from "../util/html-utils";
import { urls } from "../util/urls";
import { siteConfig } from "../util/site-config";
import { getQcStatus, Sample } from "./sample";
import { qcStatuses } from "./qc-status";
import { Requisition } from "./requisition";
import { Tooltip } from "../component/tooltip";
import { caseFilters, latestActivitySort } from "../component/table-components";
import { AssayTargets, Metric, MetricSubcategory } from "./assay";
import {
  anyFail,
  getMetricNames,
  makeMetricDisplay,
  makeNotFoundIcon,
  nullIfUndefined,
} from "../util/metrics";
import {
  DropdownField,
  FormField,
  showAlertDialog,
  showErrorDialog,
  showFormDialog,
  TextField,
} from "../component/dialog";
import { post, postDownload } from "../util/requests";

export interface Project {
  name: string;
  pipeline: string;
}

export interface Donor {
  id: string;
  name: string;
  externalName: string;
}

export interface Qcable {
  qcPassed?: boolean;
  qcReason?: string;
  qcUser?: string;
  qcDate?: string;
  qcNote?: string;
  dataReviewPassed?: boolean;
  dataReviewUser?: string;
  dataReviewDate?: string;
}

export interface Lane {
  laneNumber: number;
  percentOverQ30Read1?: number;
  percentOverQ30Read2?: number;
  clustersPf?: number;
  percentPfixRead1: number;
  percentPfixRead2: number;
}

export interface Run extends Qcable {
  id: number;
  name: string;
  containerModel?: string;
  joinedLanes: boolean;
  sequencingParameters?: string;
  readLength?: number;
  readLength2?: number;
  completionDate?: string;
  percentOverQ30?: number;
  clustersPf?: number;
  lanes: Lane[];
}

export interface Test {
  name: string;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  groupId: string;
  targetedSequencing: string;
  extractionSkipped: boolean;
  libraryPreparationSkipped: boolean;
  libraryQualificationSkipped: boolean;
  extractions: Sample[];
  libraryPreparations: Sample[];
  libraryQualifications: Sample[];
  fullDepthSequencings: Sample[];
  latestActivityDate: string;
  extractionDaysSpent: number;
  libraryPreparationDaysSpent: number;
  libraryQualificationDaysSpent: number;
  fullDepthSequencingDaysSpent: number;
}

export interface CaseRelease {
  deliverable: string;
  qcPassed?: boolean;
  qcUser?: string;
  qcDate?: string;
  qcNote?: string;
}

export const deliverableTypes = ["DATA_RELEASE", "CLINICAL_REPORT"] as const;
export type DeliverableType = (typeof deliverableTypes)[number];

export const deliverableTypeLabels: Record<DeliverableType, string> = {
  DATA_RELEASE: "Data Release",
  CLINICAL_REPORT: "Clinical Report",
};

export interface CaseDeliverable {
  deliverableType: DeliverableType;
  analysisReviewQcPassed?: boolean;
  analysisReviewQcUser?: string;
  analysisReviewQcDate?: string;
  analysisReviewQcNote?: string;
  releaseApprovalQcPassed?: boolean;
  releaseApprovalQcUser?: string;
  releaseApprovalQcDate?: string;
  releaseApprovalQcNote?: string;
  releases: CaseRelease[];
}

export interface AnalysisQcGroup {
  tissueOrigin: string;
  tissueType: string;
  libraryDesignCode: string;
  groupId?: string;
  purity?: number;
  collapsedCoverage?: number;
  callability?: number;
}

export interface Case {
  id: string;
  projects: Project[];
  donor: Donor;
  assayId: number;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  receipts: Sample[];
  startDate?: string;
  tests: Test[];
  requisition: Requisition;
  qcGroups: AnalysisQcGroup[];
  deliverables: CaseDeliverable[];
  latestActivityDate?: string;
  receiptDaysSpent: number;
  analysisReviewDaysSpent: number;
  releaseApprovalDaysSpent: number;
  releaseDaysSpent: number;
  caseDaysSpent: number;
  pauseDays: number;
}

export const caseDefinition: TableDefinition<Case, Test> = {
  queryUrl: urls.rest.cases.list,
  defaultSort: {
    columnTitle: "Urgency",
    type: "number",
    descending: true,
  },
  nonColumnSorting: [
    {
      columnTitle: "Urgency",
      type: "number",
    },
  ],
  filters: caseFilters,
  getChildren: (parent) => parent.tests,
  getRowHighlight: (kase) => {
    if (kase.requisition.stopped || kase.requisition.paused) {
      return "disabled";
    }
    return null;
  },
  staticActions: [
    legendAction,
    {
      title: "TAT Report",
      handler: (
        filters: { key: string; value: string }[],
        baseFilter: { key: string; value: string } | undefined
      ) => {
        const joinedFiltersObj: { [key: string]: any } = {};
        if (baseFilter !== undefined) {
          joinedFiltersObj[baseFilter.key] = baseFilter.value;
        }
        filters.forEach((filter) => {
          joinedFiltersObj[filter.key] = filter.value;
        });
        postDownload(
          urls.rest.downloads.reports("case-tat-report"),
          joinedFiltersObj
        );
      },
    },
  ],
  bulkActions: [
    {
      title: "Download",
      handler: showDownloadDialog,
    },
    {
      title: "Sign Off",
      handler: showSignoffDialog,
    },
  ],
  generateColumns: () => [
    ...makeBaseColumns(),
    {
      title: "Start Date",
      sortType: "date",
      addParentContents(kase, fragment) {
        if (!kase.startDate) {
          return;
        }
        const dateDiv = document.createElement("div");
        dateDiv.appendChild(document.createTextNode(kase.startDate));
        fragment.appendChild(dateDiv);

        if (caseComplete(kase)) {
          addTextDiv(`Completed in ${kase.caseDaysSpent} days`, fragment);
          return;
        }

        addTextDiv(`${kase.caseDaysSpent}d spent`, fragment);
        const targets = getTargets(kase);
        const overdue = caseOverdue(kase, targets);
        if (overdue) {
          const overdueDiv = makeTextDivWithTooltip(
            "OVERDUE",
            `Case target: ${targets.caseDays} days`
          );
          styleText(overdueDiv, "bold");
          fragment.appendChild(overdueDiv);
        } else if (targets.caseDays) {
          const remainingDiv = makeTextDivWithTooltip(
            `${targets.caseDays - kase.caseDaysSpent}d remain`,
            `Case target: ${targets.caseDays} days`
          );
          fragment.appendChild(remainingDiv);
        }
        if (!overdue) {
          const overdueStep = getOverdueStep(kase, targets);
          if (overdueStep) {
            const behindDiv = makeTextDivWithTooltip(
              "BEHIND SCHEDULE",
              `${overdueStep.stepLabel} target: ${overdueStep.targetDays} days`
            );
            styleText(behindDiv, "error");
            fragment.appendChild(behindDiv);
          }
        }
      },
      getCellHighlight(kase) {
        if (caseComplete(kase)) {
          // Never show overdue/behind warning/error for completed cases
          return null;
        }
        const targets = getTargets(kase);
        if (caseOverdue(kase, targets)) {
          return "error";
        } else if (getOverdueStep(kase, targets)) {
          return "warning";
        }
        return null;
      },
    },
    {
      title: "Receipt/Inspection",
      addParentContents(kase, fragment) {
        addSampleIcons(kase.assayId, kase.receipts, fragment);
        if (samplePhasePendingWorkOrQc(kase.receipts)) {
          if (
            samplePhasePendingWork(kase.receipts) &&
            !kase.requisition.paused
          ) {
            addConstructionIcon("receipt", fragment);
          }
          if (!kase.requisition.stopped) {
            const targets = getTargets(kase);
            addTurnAroundTimeInfo(
              kase.caseDaysSpent,
              targets.receiptDays,
              fragment
            );
          }
        }
      },
      getCellHighlight(kase) {
        return getSamplePhaseHighlight(kase.requisition, kase.receipts);
      },
    },
    {
      title: "Test",
      child: true,
      addChildContents(test, kase, fragment) {
        const testNameDiv = document.createElement("div");
        testNameDiv.appendChild(document.createTextNode(test.name));
        fragment.appendChild(testNameDiv);

        if (test.groupId) {
          const groupIdDiv = document.createElement("div");
          groupIdDiv.appendChild(document.createTextNode(test.groupId));
          const tooltipInstance = Tooltip.getInstance();
          const addContents = (fragment: DocumentFragment) =>
            fragment.appendChild(document.createTextNode("Group ID"));
          tooltipInstance.addTarget(groupIdDiv, addContents);
          fragment.appendChild(groupIdDiv);
        }
      },
    },
    {
      title: "Extraction",
      child: true,
      addChildContents(test, kase, fragment) {
        if (handleNaSamplePhase(kase.requisition, test.extractions, fragment)) {
          return;
        }
        if (!test.extractions.length && test.extractionSkipped) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(kase.assayId, test.extractions, fragment);
        if (samplePhaseComplete(kase.receipts)) {
          if (samplePhasePendingWorkOrQc(test.extractions)) {
            if (
              samplePhasePendingWork(test.extractions) &&
              !kase.requisition.paused
            ) {
              if (test.extractions.length) {
                addSpace(fragment);
              }
              addConstructionIcon("extraction", fragment);
            }
            if (!kase.requisition.stopped) {
              const targets = getTargets(kase);
              addTurnAroundTimeInfo(
                kase.caseDaysSpent,
                targets.extractionDays,
                fragment
              );
            }
          }
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        if (!test.extractions.length && test.extractionSkipped) {
          return "na";
        }
        return getSamplePhaseHighlight(kase.requisition, test.extractions);
      },
    },
    {
      title: "Library Preparation",
      child: true,
      addChildContents(test, kase, fragment) {
        if (
          handleNaSamplePhase(
            kase.requisition,
            test.libraryPreparations,
            fragment
          )
        ) {
          return;
        }
        if (
          !test.libraryPreparations.length &&
          test.libraryPreparationSkipped
        ) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(kase.assayId, test.libraryPreparations, fragment);
        if (test.extractionSkipped || samplePhaseComplete(test.extractions)) {
          if (samplePhasePendingWorkOrQc(test.libraryPreparations)) {
            if (
              samplePhasePendingWork(test.libraryPreparations) &&
              !kase.requisition.paused
            ) {
              if (test.libraryPreparations.length) {
                addSpace(fragment);
              }
              addConstructionIcon("library preparation", fragment);
            }
            if (!kase.requisition.stopped) {
              const targets = getTargets(kase);
              addTurnAroundTimeInfo(
                kase.caseDaysSpent,
                targets.libraryPreparationDays,
                fragment
              );
            }
          }
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        if (
          !test.libraryPreparations.length &&
          test.libraryPreparationSkipped
        ) {
          return "na";
        }
        return getSamplePhaseHighlight(
          kase.requisition,
          test.libraryPreparations
        );
      },
    },
    {
      title: "Library Qualification",
      child: true,
      addChildContents(test, kase, fragment) {
        if (
          handleNaSamplePhase(
            kase.requisition,
            test.libraryQualifications,
            fragment
          )
        ) {
          return;
        }
        if (
          !test.libraryQualifications.length &&
          test.libraryQualificationSkipped
        ) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(kase.assayId, test.libraryQualifications, fragment);
        if (
          test.libraryPreparationSkipped ||
          samplePhaseComplete(test.libraryPreparations)
        ) {
          if (samplePhasePendingWorkOrQc(test.libraryQualifications)) {
            if (
              samplePhasePendingWork(test.libraryQualifications) &&
              !kase.requisition.paused
            ) {
              if (test.libraryQualifications.length) {
                addSpace(fragment);
              }
              addConstructionIcon("library qualification", fragment);
            }
            if (!kase.requisition.stopped) {
              const targets = getTargets(kase);
              addTurnAroundTimeInfo(
                kase.caseDaysSpent,
                targets.libraryQualificationDays,
                fragment
              );
            }
          }
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        if (
          !test.libraryQualifications.length &&
          test.libraryQualificationSkipped
        ) {
          return "na";
        }
        return getSamplePhaseHighlight(
          kase.requisition,
          test.libraryQualifications
        );
      },
    },
    {
      title: "Full-Depth Sequencing",
      child: true,
      addChildContents(test, kase, fragment) {
        if (
          handleNaSamplePhase(
            kase.requisition,
            test.fullDepthSequencings,
            fragment
          )
        ) {
          return;
        }
        addSampleIcons(kase.assayId, test.fullDepthSequencings, fragment);
        if (
          test.libraryQualificationSkipped ||
          samplePhaseComplete(test.libraryQualifications)
        ) {
          if (samplePhasePendingWorkOrQc(test.fullDepthSequencings)) {
            if (
              samplePhasePendingWork(test.fullDepthSequencings) &&
              !kase.requisition.paused
            ) {
              if (test.fullDepthSequencings.length) {
                addSpace(fragment);
              }
              addConstructionIcon("full-depth sequencing", fragment);
            }
            if (!kase.requisition.stopped) {
              const targets = getTargets(kase);
              addTurnAroundTimeInfo(
                kase.caseDaysSpent,
                targets.fullDepthSequencingDays,
                fragment
              );
            }
          }
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        return getSamplePhaseHighlight(
          kase.requisition,
          test.fullDepthSequencings
        );
      },
    },
    makeDeliverableTypePhaseColumn(
      "Analysis Review",
      true,
      (kase, deliverableType) =>
        kase.tests.every((test) =>
          samplePhaseComplete(test.fullDepthSequencings)
        ),
      (targets) => targets.analysisReviewDays,
      (deliverable) => deliverable.analysisReviewQcPassed
    ),
    makeDeliverableTypePhaseColumn(
      "Release Approval",
      false,
      (kase, deliverableType) =>
        kase.deliverables
          .filter((x) => x.deliverableType == deliverableType)
          .some((x) => x.analysisReviewQcPassed),
      (targets) => targets.releaseApprovalDays,
      (deliverable) => deliverable.releaseApprovalQcPassed
    ),
    {
      title: "Release",
      addParentContents(kase, fragment) {
        const tooltipInstance = Tooltip.getInstance();
        if (!kase.deliverables.length) {
          addNoDeliverablesIcon(fragment, tooltipInstance);
          return;
        }
        const anyPreviousComplete = kase.deliverables.some(
          (deliverable) => deliverable.releaseApprovalQcPassed
        );
        const anyQcSet = kase.deliverables
          .flatMap((deliverable) => deliverable.releases)
          .some((release) => release.qcPassed != null);
        if (anyPreviousComplete || anyQcSet) {
          addReleaseIcons(kase.deliverables, fragment, tooltipInstance);
        }
        if (anyPreviousComplete && !caseComplete(kase)) {
          addTurnAroundTimeInfo(
            kase.caseDaysSpent,
            getTargets(kase).releaseDays,
            fragment
          );
        }
      },
      getCellHighlight(kase) {
        if (!kase.deliverables.length) {
          return "error";
        }
        if (kase.requisition.paused || caseComplete(kase)) {
          return null;
        } else {
          return "warning";
        }
      },
    },
    makeLatestActivityColumn(),
  ],
};

export const analysisReviewDefinition: TableDefinition<Case, void> = {
  queryUrl: urls.rest.cases.list,
  defaultSort: latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: (data?: Case[]) => [
    makeDeliverableTypeQcStatusColumn(
      "QC Status",
      (deliverable) => deliverable.analysisReviewQcPassed
    ),
    ...makeBaseColumns(),
    ...generateAnalysisReviewMetricColumns(data),
    makeLatestActivityColumn(),
  ],
};

export const releaseApprovalDefinition: TableDefinition<Case, void> = {
  queryUrl: urls.rest.cases.list,
  defaultSort: latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: () => [
    makeDeliverableTypeQcStatusColumn(
      "QC Status",
      (deliverable) => deliverable.releaseApprovalQcPassed
    ),
    ...makeBaseColumns(),
    makeLatestActivityColumn(),
  ],
};

export const releaseDefinition: TableDefinition<Case, void> = {
  queryUrl: urls.rest.cases.list,
  defaultSort: latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: () => [
    makeReleaseQcStatusColumn("QC Status"),
    ...makeBaseColumns(),
    makeLatestActivityColumn(),
  ],
};

function makeBaseColumns<ChildType>(): ColumnDefinition<Case, ChildType>[] {
  return [
    {
      title: "Project",
      addParentContents(kase, fragment) {
        kase.projects.forEach((project) => {
          fragment.appendChild(
            makeNameDiv(
              project.name,
              urls.miso.project(project.name),
              urls.dimsum.project(project.name)
            )
          );

          const pipelineDiv = document.createElement("div");
          pipelineDiv.appendChild(document.createTextNode(project.pipeline));
          fragment.appendChild(pipelineDiv);
        });
      },
    },
    {
      title: "Donor",
      sortType: "text",
      addParentContents(kase, fragment) {
        const nameDiv = makeNameDiv(
          kase.donor.name,
          urls.miso.sample(kase.donor.id),
          urls.dimsum.donor(kase.donor.name),
          kase.donor.name
        );
        fragment.appendChild(nameDiv);

        const externalNameDiv = makeTextDivWithTooltip(
          kase.donor.externalName,
          "External Name",
          true
        );
        fragment.appendChild(externalNameDiv);

        const tumourDetailDiv = document.createElement("div");
        tumourDetailDiv.appendChild(
          document.createTextNode(
            `${kase.tissueOrigin} ${kase.tissueType}` +
              (kase.timepoint ? " " + kase.timepoint : "")
          )
        );
        const addContents = (fragment: DocumentFragment) => {
          addTextDiv(`Tissue Origin: ${kase.tissueOrigin}`, fragment);
          addTextDiv(`Tissue Type: ${kase.tissueType}`, fragment);
          if (kase.timepoint) {
            addTextDiv(`Timepoint: ${kase.timepoint}`, fragment);
          }
        };
        const tooltipInstance = Tooltip.getInstance();
        tooltipInstance.addTarget(tumourDetailDiv, addContents);
        fragment.appendChild(tumourDetailDiv);
      },
    },
    {
      title: "Assay",
      sortType: "text",
      addParentContents(kase, fragment) {
        const assay = siteConfig.assaysById[kase.assayId];
        const assayDiv = makeNameDiv(assay.name, urls.miso.assay(kase.assayId));

        fragment.appendChild(assayDiv);

        const requisition = kase.requisition;
        if (kase.requisition.stopped) {
          const stoppedDiv = makeTextDivWithTooltip(
            "CASE STOPPED",
            `Stop reason: ${requisition.stopReason || "Unspecified"}`
          );
          styleText(stoppedDiv, "error");
          fragment.appendChild(stoppedDiv);
        }

        if (kase.requisition.paused) {
          const pausedDiv = makeTextDivWithTooltip(
            "CASE PAUSED",
            `Pause reason: ${requisition.pauseReason || "Unspecified"}`
          );
          styleText(pausedDiv, "error");
          fragment.appendChild(pausedDiv);
        }

        fragment.appendChild(
          makeNameDiv(
            requisition.name,
            urls.miso.requisition(requisition.id),
            urls.dimsum.requisition(requisition.id),
            requisition.name
          )
        );
        fragment.appendChild(
          makeNameDiv("Case Details", undefined, urls.dimsum.case(kase.id))
        );
      },
    },
  ];
}

function makeLatestActivityColumn<ChildType>(): ColumnDefinition<
  Case,
  ChildType
> {
  return {
    title: "Latest Activity",
    sortType: "date",
    addParentContents(kase, fragment) {
      if (!kase.latestActivityDate) {
        return;
      }
      fragment.appendChild(document.createTextNode(kase.latestActivityDate));
    },
  };
}

function makeDeliverableTypePhaseColumn(
  title: string,
  stoppable: boolean,
  isPreviousComplete: (kase: Case, deliverableType: DeliverableType) => boolean,
  getTarget: (targets: AssayTargets) => number | null,
  getQcPassed: (deliverable: CaseDeliverable) => boolean | undefined
): ColumnDefinition<Case, Test> {
  return {
    title: title,
    addParentContents(kase, fragment) {
      const tooltipInstance = Tooltip.getInstance();
      if (!kase.deliverables.length) {
        addNoDeliverablesIcon(fragment, tooltipInstance);
        return;
      }
      const anyQcSet = kase.deliverables.some(
        (deliverable) => getQcPassed(deliverable) != null
      );
      if (stoppable && kase.requisition.stopped && !anyQcSet) {
        addNaText(fragment);
        return;
      }
      const anyPreviousComplete = kase.deliverables.some((x) =>
        isPreviousComplete(kase, x.deliverableType)
      );
      if (
        (!stoppable && kase.requisition.stopped) ||
        anyPreviousComplete ||
        anyQcSet
      ) {
        addDeliverableTypeIcons(kase, getQcPassed, fragment, tooltipInstance);
      }
      const allQcPassed = kase.deliverables.every((deliverable) =>
        getQcPassed(deliverable)
      );
      const targets = getTargets(kase);
      if (
        ((!stoppable && kase.requisition.stopped) || anyPreviousComplete) &&
        !allQcPassed
      ) {
        addTurnAroundTimeInfo(kase.caseDaysSpent, getTarget(targets), fragment);
      }
    },
    getCellHighlight(kase) {
      return getDeliverableTypePhaseHighlight(
        kase,
        (deliverable) => getQcPassed(deliverable),
        stoppable
      );
    },
  };
}

function makeDeliverableTypeQcStatusColumn<ChildType>(
  title: string,
  getQcPassed: (deliverable: CaseDeliverable) => boolean | undefined
): ColumnDefinition<Case, ChildType> {
  return {
    title: title,
    addParentContents(kase, fragment) {
      const tooltipInstance = Tooltip.getInstance();
      if (!kase.deliverables.length) {
        addNoDeliverablesIcon(fragment, tooltipInstance);
        return;
      }
      addDeliverableTypeIcons(
        kase,
        (deliverable) => deliverable.analysisReviewQcPassed,
        fragment,
        tooltipInstance
      );
    },
    getCellHighlight(kase) {
      if (!kase.deliverables.length) {
        return "error";
      }
      const statuses = kase.deliverables.map((deliverable) =>
        getDeliverableQcStatus(getQcPassed(deliverable))
      );
      if (statuses.some((x) => x.cellStatus === "error")) {
        return "error";
      } else if (statuses.some((x) => x.cellStatus === "warning")) {
        return "warning";
      }
      return null;
    },
  };
}

function makeReleaseQcStatusColumn<ChildType>(
  title: string
): ColumnDefinition<Case, ChildType> {
  return {
    title: title,
    addParentContents(kase, fragment) {
      const tooltipInstance = Tooltip.getInstance();
      if (!kase.deliverables.length) {
        addNoDeliverablesIcon(fragment, tooltipInstance);
        return;
      }
      addReleaseIcons(kase.deliverables, fragment, tooltipInstance);
    },
    getCellHighlight(kase) {
      if (!kase.deliverables.length) {
        return "error";
      }
      const statuses = kase.deliverables
        .flatMap((deliverable) => deliverable.releases)
        .map((release) => getDeliverableQcStatus(release.qcPassed));
      if (statuses.some((x) => x.cellStatus === "error")) {
        return "error";
      } else if (statuses.some((x) => x.cellStatus === "warning")) {
        return "warning";
      }
      return null;
    },
  };
}

function generateAnalysisReviewMetricColumns(
  cases?: Case[]
): ColumnDefinition<Case, void>[] {
  if (!cases) {
    return [];
  }
  const assayIds = cases
    .map((kase) => kase.requisition.assayId || 0)
    .filter((assayId) => assayId > 0);
  const metricNames = getMetricNames("ANALYSIS_REVIEW", assayIds);
  return metricNames.map((metricName) => {
    return {
      title: metricName,
      addParentContents(kase, fragment) {
        if (metricName === "Trimming; Minimum base quality Q") {
          fragment.appendChild(
            document.createTextNode("Standard pipeline removes reads below Q30")
          );
        } else if (!kase.qcGroups.length) {
          fragment.appendChild(makeNotFoundIcon());
        } else {
          const groupsToInclude: AnalysisQcGroup[] = [];
          const metricsPerGroup: Metric[][] = [];
          kase.qcGroups.forEach((qcGroup) => {
            const metrics = getMatchingAnalysisReviewMetrics(
              metricName,
              kase.requisition,
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
            const value = getAnalysisMetricValue(metricName, qcGroup);
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
              makeAnalysisMetricDisplay(
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
      getCellHighlight(kase) {
        if (metricName === "Trimming; Minimum base quality Q") {
          return null;
        } else if (!kase.qcGroups.length) {
          return "warning";
        }
        let anyApplicable = false;
        for (let i = 0; i < kase.qcGroups.length; i++) {
          const qcGroup = kase.qcGroups[i];
          const metrics = getMatchingAnalysisReviewMetrics(
            metricName,
            kase.requisition,
            qcGroup
          );
          if (!metrics || !metrics.length) {
            continue;
          }
          anyApplicable = true;
          const value = getAnalysisMetricValue(metricName, qcGroup);
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

function getMatchingAnalysisReviewMetrics(
  metricName: string,
  requisition: Requisition,
  qcGroup: AnalysisQcGroup
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
      (metric) =>
        metric.name === metricName && analysisMetricApplies(metric, qcGroup)
    );
}

export function subcategoryApplies(
  subcategory: MetricSubcategory,
  qcGroup: AnalysisQcGroup
): boolean {
  return (
    !subcategory.libraryDesignCode ||
    subcategory.libraryDesignCode === qcGroup.libraryDesignCode
  );
}

export function analysisMetricApplies(
  metric: Metric,
  qcGroup: AnalysisQcGroup
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

export function getAnalysisMetricValue(
  metricName: string,
  qcGroup: AnalysisQcGroup
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

function makeQcGroupLabel(qcGroup: AnalysisQcGroup) {
  let label = `${qcGroup.tissueOrigin}_${qcGroup.tissueType}_${qcGroup.libraryDesignCode}`;
  if (qcGroup.groupId) {
    label += ` - ${qcGroup.groupId}`;
  }
  return label;
}

export function makeAnalysisMetricDisplay(
  metrics: Metric[],
  qcGroup: AnalysisQcGroup,
  addTooltip: boolean,
  prefix?: string,
  tooltipAdditionalContents?: Node
): Node {
  if (metrics[0].name === "Trimming; Minimum base quality Q") {
    return document.createTextNode("Standard pipeline removes reads below Q30");
  }
  const div = document.createElement("div");
  const value = getAnalysisMetricValue(metrics[0].name, qcGroup);
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

function addNoDeliverablesIcon(
  fragment: DocumentFragment,
  tooltipInstance: Tooltip
) {
  const icon = makeIcon("question");
  tooltipInstance.addTarget(
    icon,
    (tooltip) => (tooltip.textContent = "Project deliverables not configured")
  );
  fragment.appendChild(icon);
}

export function getDeliverableQcStatus(qcPassed?: boolean) {
  if (qcPassed == null) {
    return qcStatuses.qc;
  } else if (qcPassed) {
    return qcStatuses.passed;
  } else {
    return qcStatuses.failed;
  }
}

export function assertNotNull<Type>(object: Type | null) {
  if (object === null) {
    throw new Error("Unexpected null value");
  }
  return object;
}

export function handleNaSamplePhase(
  requisition: Requisition,
  samples: Sample[],
  fragment: DocumentFragment
) {
  if (requisition.stopped && !samples.length) {
    addNaText(fragment);
    return true;
  }
  return false;
}

export function samplePhaseComplete(samples: Sample[]) {
  // consider incomplete if any are pending QC or data review
  // pending statuses besides "Top-up Required" are still considered pending QC
  if (
    samples.some(
      (sample) =>
        (sample.qcPassed === null &&
          sample.qcReason !== qcStatuses.topUp.label) ||
        (sample.run && !sample.dataReviewDate)
    )
  ) {
    return false;
  } else {
    // consider complete if at least one is passed QC, and data review if applicable
    return samples.some(
      (sample) => sample.qcPassed && (!sample.run || sample.dataReviewPassed)
    );
  }
}

function deliverableTypePhaseComplete(
  kase: Case,
  getStatus: (deliverable: CaseDeliverable) => boolean | undefined
) {
  if (!kase.deliverables.length) {
    return false;
  }
  return kase.deliverables.map(getStatus).every((x) => x);
}

export function samplePhasePendingWork(samples: Sample[]) {
  // pending if there are no samples
  if (!samples.length) {
    return true;
  }
  // NOT pending if there are samples needing QC or data review
  if (
    samples.some(
      (sample) => !sample.qcDate || (sample.run && !sample.dataReviewDate)
    )
  ) {
    return false;
  }
  // NOT pending if there are samples with passed QC and data review (if applicable)
  return !samples.some(
    (sample) => sample.qcPassed && (!sample.run || sample.dataReviewPassed)
  );
}

function samplePhasePendingWorkOrQc(samples: Sample[]) {
  // pending if there are no samples
  if (!samples.length) {
    return true;
  }
  // pending if any sample needs QC or data review
  if (
    samples.some(
      (sample) => !sample.qcDate || (sample.run && !sample.dataReviewDate)
    )
  ) {
    return true;
  }
  // pending if there are no samples with passed QC and data review (if applicable)
  return !samples.some(
    (sample) => sample.qcPassed && (!sample.run || sample.dataReviewPassed)
  );
}

export function getSamplePhaseHighlight(
  requisition: Requisition,
  samples: Sample[]
) {
  if (samplePhaseComplete(samples) || requisition.paused) {
    return null;
  } else if (requisition.stopped) {
    return samples.length ? null : "na";
  } else {
    return "warning";
  }
}

function getDeliverableTypePhaseHighlight(
  kase: Case,
  getQcPassed: (deliverable: CaseDeliverable) => boolean | undefined,
  stoppable: boolean
) {
  if (!kase.deliverables.length) {
    return "error";
  }
  if (
    kase.requisition.paused ||
    kase.deliverables.every((deliverable) => getQcPassed(deliverable))
  ) {
    return null;
  } else if (stoppable && kase.requisition.stopped) {
    if (
      kase.deliverables.some((deliverable) => getQcPassed(deliverable) != null)
    ) {
      return null;
    } else {
      return "na";
    }
  } else {
    return "warning";
  }
}

export function addSpace(fragment: DocumentFragment) {
  fragment.appendChild(document.createTextNode(" "));
}

export function addConstructionIcon(phase: string, fragment: DocumentFragment) {
  const icon = makeIcon(qcStatuses.construction.icon);
  icon.title = `Pending ${phase}`;
  fragment.appendChild(icon);
}

export function addSampleIcons(
  assayId: number,
  samples: Sample[],
  fragment: DocumentFragment
) {
  samples.forEach((sample, i) => {
    let status = getQcStatus(sample);
    if (status === qcStatuses.passed && sample.assayId !== assayId) {
      status = qcStatuses.passedDifferentAssay;
    }
    const icon = makeIcon(status.icon);
    const tooltipInstance = Tooltip.getInstance();
    tooltipInstance.addTarget(icon, makeSampleTooltip(sample));
    fragment.appendChild(icon);
    if (i < samples.length - 1) {
      addSpace(fragment);
    }
  });
}

export function makeSampleTooltip(sample: Sample) {
  return (fragment: DocumentFragment) => {
    const topContainer = document.createElement("div");
    topContainer.className = "flex flex-col space-y-1 text-black";
    const bottomContainer = document.createElement("div");
    bottomContainer.className =
      "grid grid-cols-2 grid-flow-row gap-y-1 font-inter font-medium font-14 text-black mt-3";
    // sample run links
    if (sample.run) {
      topContainer.appendChild(
        makeNameDiv(
          sample.sequencingLane
            ? sample.run.name + " (L" + sample.sequencingLane + ")"
            : sample.run.name,
          urls.miso.run(sample.run.name),
          urls.dimsum.run(sample.run.name),
          sample.run.name
        )
      );
    }
    // sample name links
    const sampleNameContainer = makeNameDiv(
      sample.name,
      urls.miso.sample(sample.id)
    );
    topContainer.appendChild(sampleNameContainer);

    // project links
    addTooltipRow(
      bottomContainer,
      "Project",
      sample.project,
      urls.miso.project(sample.project),
      urls.dimsum.project(sample.project)
    );
    // donor links
    addTooltipRow(
      bottomContainer,
      "Donor",
      sample.donor.name,
      urls.miso.sample(sample.donor.id),
      urls.dimsum.donor(sample.donor.name),
      sample.donor.externalName
    );
    // requisition links
    if (sample.requisitionId && sample.requisitionName) {
      addTooltipRow(
        bottomContainer,
        "Requisition",
        sample.requisitionName,
        urls.miso.requisition(sample.requisitionId),
        urls.dimsum.requisition(sample.requisitionId),
        undefined,
        sample.requisitionName
      );
    }
    if (sample.assayId) {
      const assay = siteConfig.assaysById[sample.assayId];
      addTooltipRow(bottomContainer, "Assay", assay.name);
    }
    fragment.appendChild(topContainer);
    fragment.appendChild(bottomContainer);
  };
}

function addTooltipRow(
  container: HTMLElement, // must be a two column grid
  label: string,
  name: string,
  misoUrl?: string,
  dimsumUrl?: string,
  additionalText?: string,
  copyText?: string
) {
  const labelContainer = document.createElement("span");
  labelContainer.innerHTML = `${label}:`;
  const valueContainer = document.createElement("div");
  valueContainer.appendChild(makeNameDiv(name, misoUrl, dimsumUrl, copyText));
  if (additionalText) {
    const externalDonorNameContainer = document.createElement("span");
    externalDonorNameContainer.className = "block";
    externalDonorNameContainer.innerHTML = additionalText;
    valueContainer.appendChild(externalDonorNameContainer);
  }
  container.appendChild(labelContainer);
  container.appendChild(valueContainer);
}

function addDeliverableTypeIcons(
  kase: Case,
  getStatus: (deliverable: CaseDeliverable) => boolean | undefined,
  fragment: DocumentFragment,
  tooltipInstance: Tooltip
) {
  kase.deliverables.forEach((deliverable, i) => {
    const status = getDeliverableQcStatus(getStatus(deliverable));
    const icon = makeIcon(status.icon);
    tooltipInstance.addTarget(icon, (tooltip) => {
      tooltip.appendChild(
        makeTextDiv(deliverableTypeLabels[deliverable.deliverableType])
      );
      tooltip.appendChild(makeTextDiv("Status: " + status.label));
    });
    fragment.appendChild(icon);
    if (i < kase.deliverables.length - 1) {
      addSpace(fragment);
    }
  });
}

function addReleaseIcons(
  deliverables: CaseDeliverable[],
  fragment: DocumentFragment,
  tooltipInstance: Tooltip
) {
  deliverables.forEach((deliverable, i) => {
    deliverable.releases.forEach((release, j) => {
      const status = getDeliverableQcStatus(release.qcPassed);
      const icon = makeIcon(status.icon);
      tooltipInstance.addTarget(icon, (tooltip) => {
        let deliverableLabel =
          deliverableTypeLabels[deliverable.deliverableType];
        if (release.deliverable !== deliverableLabel) {
          deliverableLabel += " - " + release.deliverable;
        }
        tooltip.appendChild(makeTextDiv(deliverableLabel));
        tooltip.appendChild(makeTextDiv("Status: " + status.label));
      });
      fragment.appendChild(icon);
      if (j < deliverable.releases.length - 1) {
        addSpace(fragment);
      }
    });
    if (i < deliverables.length - 1) {
      addSpace(fragment);
    }
  });
}

function caseComplete(kase: Case) {
  return (
    kase.deliverables.length &&
    kase.deliverables.every((deliverable) =>
      deliverable.releases.every((release) => release.qcPassed)
    )
  );
}

function caseOverdue(kase: Case, targets: AssayTargets) {
  return targets.caseDays && kase.caseDaysSpent > targets.caseDays;
}

interface OverdueStep {
  stepLabel: string;
  targetDays: number | null;
}

function getOverdueStep(kase: Case, targets: AssayTargets): OverdueStep | null {
  // Check later steps first to emphasize if we are multiple steps overdue
  if (releasePhaseBehind(kase, targets.releaseDays)) {
    return {
      stepLabel: "Release",
      targetDays: targets.releaseDays,
    };
  } else if (
    deliverableTypePhaseBehind(
      kase,
      (x) => x.releaseApprovalQcPassed,
      targets.releaseApprovalDays
    )
  ) {
    return {
      stepLabel: "Release approval",
      targetDays: targets.releaseApprovalDays,
    };
  }
  if (kase.requisition.stopped) {
    return null;
  } else if (
    deliverableTypePhaseBehind(
      kase,
      (x) => x.analysisReviewQcPassed,
      targets.analysisReviewDays
    )
  ) {
    return {
      stepLabel: "Analysis review",
      targetDays: targets.analysisReviewDays,
    };
  } else if (
    kase.tests.some((test) =>
      samplePhaseBehind(
        test.fullDepthSequencings,
        kase.caseDaysSpent,
        targets.fullDepthSequencingDays
      )
    )
  ) {
    return {
      stepLabel: "Full-depth sequencing",
      targetDays: targets.fullDepthSequencingDays,
    };
  } else if (
    kase.tests.some((test) =>
      samplePhaseBehind(
        test.libraryQualifications,
        kase.caseDaysSpent,
        targets.libraryQualificationDays
      )
    )
  ) {
    return {
      stepLabel: "Library qualification",
      targetDays: targets.libraryQualificationDays,
    };
  } else if (
    kase.tests.some(
      (test) =>
        !test.libraryPreparationSkipped &&
        samplePhaseBehind(
          test.libraryPreparations,
          kase.caseDaysSpent,
          targets.libraryPreparationDays
        )
    )
  ) {
    return {
      stepLabel: "Library preparation",
      targetDays: targets.libraryPreparationDays,
    };
  } else if (
    kase.tests.some(
      (test) =>
        !test.extractionSkipped &&
        samplePhaseBehind(
          test.extractions,
          kase.caseDaysSpent,
          targets.extractionDays
        )
    )
  ) {
    return {
      stepLabel: "Extraction",
      targetDays: targets.extractionDays,
    };
  } else if (
    samplePhaseBehind(kase.receipts, kase.caseDaysSpent, targets.receiptDays)
  ) {
    return {
      stepLabel: "Receipt",
      targetDays: targets.receiptDays,
    };
  }
  return null;
}

function samplePhaseBehind(
  samples: Sample[],
  caseDaysSpent: number,
  stepTarget: number | null
) {
  return (
    stepTarget && !samplePhaseComplete(samples) && caseDaysSpent > stepTarget
  );
}

function deliverableTypePhaseBehind(
  kase: Case,
  getStatus: (deliverable: CaseDeliverable) => boolean | undefined,
  stepTarget: number | null
) {
  return (
    stepTarget &&
    !deliverableTypePhaseComplete(kase, getStatus) &&
    kase.caseDaysSpent > stepTarget
  );
}

function releasePhaseBehind(kase: Case, stepTarget: number | null) {
  return stepTarget && !caseComplete(kase) && kase.caseDaysSpent > stepTarget;
}

function addTurnAroundTimeInfo(
  daysSpent: number,
  target: number | null,
  fragment: DocumentFragment
) {
  if (!target) {
    return;
  }
  if (daysSpent > target) {
    const tatDiv = makeTextDivWithTooltip("OVERDUE", `Target: ${target} days`);
    styleText(tatDiv, "error");
    fragment.appendChild(tatDiv);
  } else {
    const daysRemaining = target - daysSpent;
    const tatDiv = makeTextDivWithTooltip(
      `${daysRemaining}d remain`,
      `Target: ${target} days`
    );
    fragment.appendChild(tatDiv);
  }
}

function getTargets(kase: Case) {
  return siteConfig.assaysById[kase.assayId].targets;
}

export function getAnalysisMetricCellHighlight(
  qcGroup: AnalysisQcGroup,
  requisition: Requisition,
  metricName: string
): CellStatus | null {
  if (metricName === "Trimming; Minimum base quality Q") {
    return null;
  }
  let anyApplicable = false;
  const metrics = getMatchingAnalysisReviewMetrics(
    metricName,
    requisition,
    qcGroup
  );
  if (!metrics || !metrics.length) {
    return null;
  }
  anyApplicable = true;
  const value = getAnalysisMetricValue(metricName, qcGroup);
  if (value === null) {
    return "warning";
  } else if (anyFail(value, metrics)) {
    return "error";
  }
  return anyApplicable ? null : "na";
}

function showSignoffDialog(items: Case[]) {
  const commonDeliverableTypes = new Map<string, DeliverableType>();
  for (let deliverableType of deliverableTypes) {
    if (
      items.every((item) =>
        item.deliverables.some(
          (deliverable) => deliverable.deliverableType === deliverableType
        )
      )
    ) {
      commonDeliverableTypes.set(
        deliverableTypeLabels[deliverableType],
        deliverableType
      );
    }
  }
  if (!commonDeliverableTypes.size) {
    showErrorDialog("The selected cases have no deliverable type in common");
    return;
  }

  const nabuQcSteps = [
    "ANALYSIS_REVIEW",
    "RELEASE_APPROVAL",
    "RELEASE",
  ] as const;
  type NabuQcStep = (typeof nabuQcSteps)[number];

  const nabuQcStepLabels: Record<NabuQcStep, string> = {
    ANALYSIS_REVIEW: "Analysis Review",
    RELEASE_APPROVAL: "Release Approval",
    RELEASE: "Release",
  };

  const qcStepOptions = new Map<string, string>(
    nabuQcSteps.map((step) => [nabuQcStepLabels[step], step])
  );

  showFormDialog(
    "Case QC",
    [
      new DropdownField("QC Step", qcStepOptions, "signoffStepName", true),
      new DropdownField(
        "Deliverable Type",
        commonDeliverableTypes,
        "deliverableType",
        true,
        undefined,
        commonDeliverableTypes.size === 1
          ? commonDeliverableTypes.keys().next().value
          : undefined
      ),
    ],
    "Next",
    (result1) => {
      const formFields: FormField<any>[] = [
        new DropdownField(
          "QC Status",
          new Map<string, boolean | null>([
            ["Approved", true],
            ["Failed", false],
          ]),
          "qcPassed",
          false,
          "Pending"
        ),
        new TextField("Note", "comment"),
      ];

      if (result1.signoffStepName === "RELEASE") {
        const commonDeliverables = new Map<string, string>();
        items[0].deliverables
          .filter(
            (deliverable) =>
              deliverable.deliverableType === result1.deliverableType
          )
          .flatMap((deliverableType) => deliverableType.releases)
          .map((release) => release.deliverable)
          .filter((deliverable) =>
            items.every((item) => hasDeliverable(item, deliverable))
          )
          .forEach((deliverable) =>
            commonDeliverables.set(deliverable, deliverable)
          );
        if (!commonDeliverables.size) {
          const deliverableTypeLabel =
            deliverableTypeLabels[result1.deliverableType as DeliverableType];
          showErrorDialog(
            `The selected cases have no ${deliverableTypeLabel} deliverable in common`
          );
          return;
        }
        formFields.unshift(
          new DropdownField(
            "Deliverable",
            commonDeliverables,
            "deliverable",
            true,
            undefined,
            commonDeliverables.size === 1
              ? commonDeliverables.keys().next().value
              : undefined
          )
        );
      }

      const qcStepLabel =
        nabuQcStepLabels[result1.signoffStepName as NabuQcStep];
      const deliverableTypeLabel =
        deliverableTypeLabels[result1.deliverableType as DeliverableType];
      showFormDialog(
        `${qcStepLabel} QC - ${deliverableTypeLabel}`,
        formFields,
        "Submit",
        (result2) => {
          const data = {
            caseIdentifiers: items.map((item) => item.id),
            signoffStepName: result1.signoffStepName,
            deliverableType: result1.deliverableType,
            deliverable: result2.deliverable || null,
            qcPassed: result2.qcPassed,
            comment: result2.comment || null,
          };
          post(urls.rest.cases.bulkSignoff, data)
            .then(() => {
              showAlertDialog(
                "Success",
                "Sign-off has been recorded in Nabu. Refreshing view.",
                undefined,
                () => window.location.reload()
              );
            })
            .catch((reason) => showErrorDialog(reason));
        }
      );
    }
  );
}

function hasDeliverable(kase: Case, deliverable: string) {
  return kase.deliverables
    .flatMap((deliverableType) => deliverableType.releases)
    .some((release) => release.deliverable === deliverable);
}

const REPORT_FULL_DEPTH_SUMMARY = "full-depth-summary";
const REPORT_DARE_INPUT_SHEET = "dare-input-sheet";

function showDownloadDialog(items: Case[]) {
  const reportOptions = new Map<string, string>([
    ["Full-Depth Summary", REPORT_FULL_DEPTH_SUMMARY],
    ["DARE Input Sheet", REPORT_DARE_INPUT_SHEET],
  ]);
  const formatOptions = new Map<string, any>([
    [
      "Excel",
      {
        format: "excel",
      },
    ],
    [
      "CSV with headings",
      {
        format: "csv",
        includeHeadings: false,
      },
    ],
    [
      "CSV, no headings",
      {
        format: "csv",
        includeHeadings: false,
      },
    ],
    [
      "TSV with headings",
      {
        format: "tsv",
        includeHeadings: false,
      },
    ],
    [
      "TSV, no headings",
      {
        format: "tsv",
        includeHeadings: false,
      },
    ],
  ]);
  const reportSelect = new DropdownField(
    "Report",
    reportOptions,
    "report",
    true
  );
  const formatSelect = new DropdownField(
    "Format",
    formatOptions,
    "formatOptions",
    true,
    undefined,
    "Excel"
  );
  showFormDialog(
    "Download Case Data",
    [reportSelect, formatSelect],
    "Download",
    (result) => {
      switch (result.report) {
        case REPORT_FULL_DEPTH_SUMMARY:
        case REPORT_DARE_INPUT_SHEET:
          downloadCaseReport(result.report, result.formatOptions, items);
          break;
        default:
          throw new Error(`Invalid report: ${result.report}`);
      }
    }
  );
}

function downloadCaseReport(report: string, formatOptions: any, items: Case[]) {
  const params = Object.assign({}, formatOptions);
  params.caseIds = items.map((kase) => kase.id).join(", ");
  postDownload(urls.rest.downloads.reports(report), params);
}
