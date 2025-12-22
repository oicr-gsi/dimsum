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
import {
  getAnalysisReviewQcStatus,
  getMetricCategory,
  getReleaseApprovalQcStatus,
  getReleaseQcStatus,
  internalUser,
  siteConfig,
} from "../util/site-config";
import {
  getQcStatus,
  getQcStatusWithDataReview,
  getSampleQcStatus,
  Sample,
} from "./sample";
import { QcStatus, qcStatuses } from "./qc-status";
import { Requisition } from "./requisition";
import { Tooltip } from "../component/tooltip";
import { caseFilters, latestActivitySort } from "../component/table-components";
import { AssayTargets, Metric, MetricSubcategory } from "./assay";
import {
  anyFail,
  getBooleanMetricHighlight,
  getBooleanMetricValueIcon,
  getMetricNames,
  makeMetricDisplay,
  makeNotFoundIcon,
} from "../util/metrics";
import {
  DropdownField,
  FormField,
  showAlertDialog,
  showDownloadOptionsDialog,
  showErrorDialog,
  showFormDialog,
  TextField,
} from "../component/dialog";
import { post, postDownload } from "../util/requests";
import { assertDefined, assertNotNull, nullIfUndefined } from "./data-utils";

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
  qcPassed: boolean | null;
  qcReason: string | null;
  qcUser?: string | null;
  qcDate: string | null;
  qcNote?: string | null;
  dataReviewPassed: boolean | null;
  dataReviewUser?: string | null;
  dataReviewDate: string | null;
}

export interface Lane {
  laneNumber: number;
  percentOverQ30Read1: number | null;
  percentOverQ30Read2: number | null;
  clustersPf: number | null;
  percentPfixRead1: number | null;
  percentPfixRead2: number | null;
}

export interface Run extends Qcable {
  id: number;
  name: string;
  containerModel?: string;
  joinedLanes?: boolean;
  sequencingParameters?: string | null;
  readLength?: number | null;
  readLength2?: number | null;
  completionDate: string | null;
  percentOverQ30?: number | null;
  clustersPf?: number | null;
  lanes?: Lane[];
}

export interface Test {
  name: string;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string | null;
  groupId: string | null;
  targetedSequencing: string | null;
  extractionSkipped: boolean;
  libraryPreparationSkipped: boolean;
  libraryQualificationSkipped: boolean;
  extractions: Sample[];
  libraryPreparations: Sample[];
  libraryQualifications: Sample[];
  fullDepthSequencings: Sample[];
  latestActivityDate: string | null;
  extractionDaysSpent?: number;
  libraryPreparationDaysSpent?: number;
  libraryQualificationDaysSpent?: number;
  fullDepthSequencingDaysSpent?: number;
}

export interface CaseRelease {
  deliverable: string;
  qcStatus: string | null;
  qcUser?: string | null;
  qcDate: string | null;
  qcNote?: string | null;
}

export interface CaseQc {
  name: string;
  label: string;
  qcPassed: boolean | null;
  release: boolean | null;
}

export interface CaseDeliverable {
  deliverableCategory: string;
  analysisReviewSkipped: boolean;
  analysisReviewQcStatus: string | null;
  analysisReviewQcUser?: string | null;
  analysisReviewQcDate: string | null;
  analysisReviewQcNote?: string | null;
  releaseApprovalQcStatus: string | null;
  releaseApprovalQcUser?: string | null;
  releaseApprovalQcDate: string | null;
  releaseApprovalQcNote?: string | null;
  releases: CaseRelease[];
  analysisReviewDaysSpent?: number;
  releaseApprovalDaysSpent?: number;
  releaseDaysSpent?: number;
  deliverableDaysSpent?: number;
}

export interface AnalysisQcGroup {
  tissueOrigin: string;
  tissueType: string;
  libraryDesignCode: string;
  groupId: string | null;
  purity: number | null;
  collapsedCoverage: number | null;
  callability: number | null;
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
  startDate: string;
  tests: Test[];
  requisition: Requisition;
  qcGroups?: AnalysisQcGroup[];
  deliverables: CaseDeliverable[];
  latestActivityDate: string | null;
  receiptDaysSpent?: number;
  analysisReviewDaysSpent?: number;
  releaseApprovalDaysSpent?: number;
  releaseDaysSpent?: number;
  caseDaysSpent?: number;
  pauseDays?: number;
}

export const caseDefinition: TableDefinition<Case, Test> = {
  queryUrl: urls.rest.cases.list,
  getDefaultSort: () =>
    internalUser
      ? {
          columnTitle: "Urgency",
          type: "number",
          descending: true,
        }
      : {
          columnTitle: "Latest Activity",
          type: "date",
          descending: true,
        },
  getNonColumnSorting: () =>
    internalUser
      ? [
          {
            columnTitle: "Urgency",
            type: "number",
          },
        ]
      : [],
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
      handler: showTatReportDialog,
      view: "internal",
    },
    {
      title: "TAT Trend",
      handler: showTatTrendPage,
      view: "internal",
    },
  ],
  bulkActions: [
    {
      title: "Download",
      handler: showDownloadDialog,
      view: "internal",
    },
    {
      title: "Sign Off",
      handler: showSignoffDialog,
      view: "internal",
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

        const targets = getTargets(kase);
        if (targets) {
          assertDefined(kase.caseDaysSpent);
          if (caseComplete(kase)) {
            addTextDiv(`Completed in ${kase.caseDaysSpent} days`, fragment);
            return;
          }

          addTextDiv(`${kase.caseDaysSpent}d spent`, fragment);
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
        }
      },
      getCellHighlight(kase) {
        if (caseComplete(kase)) {
          // Never show overdue/behind warning/error for completed cases
          return null;
        }
        const targets = getTargets(kase);
        if (targets) {
          if (caseOverdue(kase, targets)) {
            return "error";
          } else if (getOverdueStep(kase, targets)) {
            return "warning";
          }
        }
        return null;
      },
    },
    {
      title: "Receipt/Inspection",
      addParentContents(kase, fragment) {
        addSampleIcons(kase.assayId, kase.receipts, fragment);
        if (samplePhasePendingWorkQcOrTransfer(kase.receipts)) {
          if (
            samplePhasePendingWork(kase.receipts) &&
            !kase.requisition.paused
          ) {
            addConstructionIcon("receipt", fragment);
          }
          if (!kase.requisition.stopped) {
            const targets = getTargets(kase);
            if (targets) {
              assertDefined(kase.caseDaysSpent);
              addTurnAroundTimeInfo(
                kase.caseDaysSpent,
                targets.receiptDays,
                fragment
              );
            }
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
        addSampleIcons(kase.assayId, test.extractions, fragment, internalUser);
        if (samplePhaseComplete(kase.receipts)) {
          if (
            samplePhasePendingWorkQcOrTransfer(test.extractions, internalUser)
          ) {
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
              if (targets) {
                assertDefined(kase.caseDaysSpent);
                addTurnAroundTimeInfo(
                  kase.caseDaysSpent,
                  targets.extractionDays,
                  fragment
                );
              }
            }
          }
        }
      },
      getCellHighlight(kase, test) {
        test = assertNotNull(test);
        if (!test.extractions.length && test.extractionSkipped) {
          return "na";
        }
        return getSamplePhaseHighlight(
          kase.requisition,
          test.extractions,
          internalUser
        );
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
        if (
          test.extractionSkipped ||
          samplePhaseComplete(test.extractions, internalUser)
        ) {
          if (samplePhasePendingWorkQcOrTransfer(test.libraryPreparations)) {
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
              if (targets) {
                assertDefined(kase.caseDaysSpent);
                addTurnAroundTimeInfo(
                  kase.caseDaysSpent,
                  targets.libraryPreparationDays,
                  fragment
                );
              }
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
          if (samplePhasePendingWorkQcOrTransfer(test.libraryQualifications)) {
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
              if (targets) {
                assertDefined(kase.caseDaysSpent);
                addTurnAroundTimeInfo(
                  kase.caseDaysSpent,
                  targets.libraryQualificationDays,
                  fragment
                );
              }
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
          if (samplePhasePendingWorkQcOrTransfer(test.fullDepthSequencings)) {
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
              if (targets) {
                assertDefined(kase.caseDaysSpent);
                addTurnAroundTimeInfo(
                  kase.caseDaysSpent,
                  targets.fullDepthSequencingDays,
                  fragment
                );
              }
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
      (deliverable) =>
        getAnalysisReviewQcStatus(deliverable.analysisReviewQcStatus),
      (deliverable) => deliverable.analysisReviewQcUser,
      (deliverable) => deliverable.analysisReviewQcNote
    ),
    makeDeliverableTypePhaseColumn(
      "Release Approval",
      false,
      (kase, deliverableType) => {
        if (
          kase.deliverables.every(
            (deliverable) => deliverable.analysisReviewSkipped
          )
        ) {
          return kase.tests.every((test) =>
            samplePhaseComplete(test.fullDepthSequencings)
          );
        }
        return kase.deliverables
          .filter((x) => x.deliverableCategory == deliverableType)
          .some((x) =>
            caseQcComplete(getAnalysisReviewQcStatus(x.analysisReviewQcStatus))
          );
      },
      (targets) => targets.releaseApprovalDays,
      (deliverable) =>
        getReleaseApprovalQcStatus(deliverable.releaseApprovalQcStatus),
      (deliverable) => deliverable.releaseApprovalQcUser,
      (deliverable) => deliverable.releaseApprovalQcNote
    ),
    {
      title: "Release",
      addParentContents(kase, fragment) {
        const tooltipInstance = Tooltip.getInstance();
        if (!kase.deliverables.length) {
          addNoDeliverablesIcon(fragment, tooltipInstance);
          return;
        }
        const anyPreviousComplete = kase.deliverables.some((deliverable) =>
          caseQcComplete(
            getReleaseApprovalQcStatus(deliverable.releaseApprovalQcStatus)
          )
        );
        const anyQcSet = kase.deliverables
          .flatMap((deliverable) => deliverable.releases)
          .some((release) =>
            caseQcComplete(getReleaseQcStatus(release.qcStatus))
          );
        if (anyPreviousComplete || anyQcSet) {
          addReleaseIcons(kase.deliverables, fragment, tooltipInstance);
        }
        if (anyPreviousComplete && !caseComplete(kase)) {
          const targets = getTargets(kase);
          if (targets) {
            assertDefined(kase.caseDaysSpent);
            addTurnAroundTimeInfo(
              kase.caseDaysSpent,
              targets.releaseDays,
              fragment
            );
          }
        }
      },
      getCellHighlight(kase) {
        if (!kase.deliverables.length) {
          return "error";
        } else if (
          kase.deliverables
            .flatMap((deliverable) => deliverable.releases)
            .map((release) => getReleaseQcStatus(release.qcStatus))
            .some((qcStatus) => caseQcFailed(qcStatus))
        ) {
          return "error";
        } else if (kase.requisition.paused || caseComplete(kase)) {
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
  getDefaultSort: () => latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: (data?: Case[]) => [
    makeDeliverableTypeQcStatusColumn(
      "QC Status",
      (deliverable) =>
        getAnalysisReviewQcStatus(deliverable.analysisReviewQcStatus),
      (deliverable) => deliverable.analysisReviewQcUser,
      (deliverable) => deliverable.analysisReviewQcNote
    ),
    ...makeBaseColumns(),
    ...generateAnalysisReviewMetricColumns(data),
    makeLatestActivityColumn(),
  ],
};

export const releaseApprovalDefinition: TableDefinition<Case, void> = {
  queryUrl: urls.rest.cases.list,
  getDefaultSort: () => latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: () => [
    makeDeliverableTypeQcStatusColumn(
      "QC Status",
      (deliverable) =>
        getReleaseApprovalQcStatus(deliverable.releaseApprovalQcStatus),
      (deliverable) => deliverable.releaseApprovalQcUser,
      (deliverable) => deliverable.releaseApprovalQcNote
    ),
    ...makeBaseColumns(),
    makeLatestActivityColumn(),
  ],
};

export const releaseDefinition: TableDefinition<Case, void> = {
  queryUrl: urls.rest.cases.list,
  getDefaultSort: () => latestActivitySort,
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
  analysisReview: boolean,
  isPreviousComplete: (kase: Case, deliverableType: string) => boolean,
  getTarget: (targets: AssayTargets) => number | null,
  getQcStatus: (deliverable: CaseDeliverable) => CaseQc | null,
  getQcUser: (deliverable: CaseDeliverable) => string | null | undefined,
  getQcNote: (deliverable: CaseDeliverable) => string | null | undefined
): ColumnDefinition<Case, Test> {
  return {
    title: title,
    addParentContents(kase, fragment) {
      const tooltipInstance = Tooltip.getInstance();
      if (!kase.deliverables.length) {
        addNoDeliverablesIcon(fragment, tooltipInstance);
        return;
      }
      const anyQcSet = kase.deliverables.some((deliverable) =>
        caseQcComplete(getQcStatus(deliverable))
      );
      if (analysisReview && !anyQcSet) {
        if (
          kase.requisition.stopped ||
          kase.deliverables.every(
            (deliverable) => deliverable.analysisReviewSkipped
          )
        ) {
          addNaText(fragment);
          return;
        }
      }
      const anyPreviousComplete = kase.deliverables.some((x) =>
        isPreviousComplete(kase, x.deliverableCategory)
      );
      if (
        (!analysisReview && kase.requisition.stopped) ||
        anyPreviousComplete ||
        anyQcSet
      ) {
        addDeliverableTypeIcons(
          kase,
          getQcStatus,
          getQcUser,
          getQcNote,
          fragment,
          tooltipInstance
        );
      }
      const allQcComplete = kase.deliverables.every((deliverable) =>
        caseQcComplete(getQcStatus(deliverable))
      );
      const targets = getTargets(kase);
      if (
        targets &&
        !allQcComplete &&
        ((!analysisReview && kase.requisition.stopped) || anyPreviousComplete)
      ) {
        assertDefined(kase.caseDaysSpent);
        addTurnAroundTimeInfo(kase.caseDaysSpent, getTarget(targets), fragment);
      }
    },
    getCellHighlight(kase) {
      return getDeliverableTypePhaseHighlight(
        kase,
        getQcStatus,
        analysisReview
      );
    },
  };
}

function makeDeliverableTypeQcStatusColumn<ChildType>(
  title: string,
  getQcStatus: (deliverable: CaseDeliverable) => CaseQc | null,
  getQcUser: (deliverable: CaseDeliverable) => string | null | undefined,
  getQcNote: (deliverable: CaseDeliverable) => string | null | undefined
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
        getQcStatus,
        getQcUser,
        getQcNote,
        fragment,
        tooltipInstance
      );
    },
    getCellHighlight(kase) {
      if (!kase.deliverables.length) {
        return "error";
      }
      const statuses = kase.deliverables.map((deliverable) =>
        getDeliverableQcStatus(getQcStatus(deliverable))
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
        .map((release) =>
          getDeliverableQcStatus(getReleaseQcStatus(release.qcStatus))
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

function generateAnalysisReviewMetricColumns(
  cases?: Case[]
): ColumnDefinition<Case, void>[] {
  if (!cases) {
    return [];
  }
  const assayIds = cases.map((kase) => kase.assayId);
  const metricNames = getMetricNames("ANALYSIS_REVIEW", assayIds);
  return metricNames.map((metricName) => {
    return {
      title: metricName,
      addParentContents(kase, fragment) {
        assertDefined(kase.qcGroups);
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
              kase,
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
            const qcStatuses = !kase.deliverables
              ? []
              : kase.deliverables.map((deliverable) => {
                  const qcStatus = getAnalysisReviewQcStatus(
                    deliverable.analysisReviewQcStatus
                  );
                  return qcStatus ? qcStatus.qcPassed : null;
                });
            fragment.appendChild(
              makeAnalysisMetricDisplay(
                metricsPerGroup[i],
                qcGroup,
                qcStatuses,
                true,
                prefix,
                detailsFragment
              )
            );
          }
        }
      },
      getCellHighlight(kase) {
        assertDefined(kase.qcGroups);
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
            kase,
            qcGroup
          );
          if (!metrics || !metrics.length) {
            continue;
          }
          if (metrics[0].thresholdType === "BOOLEAN") {
            // Not specific to QC groups
            if (!kase.deliverables) {
              return "warning";
            } else if (
              kase.deliverables.every((x) => {
                const qcStatus = getAnalysisReviewQcStatus(
                  x.analysisReviewQcStatus
                );
                return caseQcPassed(qcStatus) || caseQcNa(qcStatus);
              })
            ) {
              return null;
            } else if (
              kase.deliverables.some((x) => {
                const qcStatus = getAnalysisReviewQcStatus(
                  x.analysisReviewQcStatus
                );
                return caseQcFailed(qcStatus);
              })
            ) {
              return "error";
            } else {
              return "warning";
            }
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
  kase: Case,
  qcGroup: AnalysisQcGroup
): Metric[] | null {
  return getMetricCategory(kase.assayId, "ANALYSIS_REVIEW")
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
  deliverableTypeQcStatuses: (boolean | null)[],
  addTooltip: boolean,
  prefix?: string,
  tooltipAdditionalContents?: Node
): Node {
  if (metrics[0].name === "Trimming; Minimum base quality Q") {
    return document.createTextNode("Standard pipeline removes reads below Q30");
  } else if (metrics[0].thresholdType === "BOOLEAN") {
    const div = document.createElement("div");
    div.className = "flex space-x-1 items-center";
    if (prefix) {
      const label = document.createElement("span");
      label.innerText = prefix;
      div.appendChild(label);
    }
    deliverableTypeQcStatuses.forEach((qcStatus) =>
      div.appendChild(getBooleanMetricValueIcon(qcStatus))
    );
    return div;
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

export function getDeliverableQcStatus(qcStatus: CaseQc | null) {
  if (qcStatus == null) {
    return internalUser ? qcStatuses.qc : qcStatuses.construction;
  } else if (qcStatus.qcPassed == null) {
    if (qcStatus.release == false) {
      return qcStatuses.na;
    } else {
      // explicitly marked as pending
      return internalUser ? qcStatuses.qc : qcStatuses.construction;
    }
  } else if (qcStatus.qcPassed) {
    return qcStatuses.passed;
  } else {
    return qcStatuses.failed;
  }
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

export function samplePhaseComplete(
  samples: Sample[],
  requiresTransfer: boolean = false
) {
  // consider incomplete if any are pending QC or data review
  // pending statuses besides "Top-up Required" are still considered pending QC
  for (let sample of samples) {
    const run = sample.run;
    if (run) {
      if (run.qcPassed === false && run.dataReviewDate) {
        // Run failed; consider QC complete even if run-library isn't QCed
        continue;
      } else if (!sample.dataReviewDate || !run.dataReviewDate) {
        // pending QC or data review
        return false;
      }
    } else {
      if (
        sample.qcPassed === null &&
        sample.qcReason !== qcStatuses.topUp.label
      ) {
        // pending QC
        return false;
      }
    }
  }
  // consider complete if at least one is passed QC, data review if applicable,
  // and has been transferred if applicable
  return samples.some(
    (sample) =>
      sample.qcPassed &&
      (!sample.run || sample.dataReviewPassed) &&
      (!requiresTransfer || sample.transferDate)
  );
}

function caseQcComplete(qc: CaseQc | null) {
  if (qc == null) {
    return false;
  }
  if (qc.qcPassed == null && qc.release == null) {
    // QC explicitly set to pending
    return false;
  }
  return true;
}

function caseQcPassed(qc: CaseQc | null) {
  if (qc == null) {
    return false;
  }
  return qc.qcPassed || false;
}

function caseQcFailed(qc: CaseQc | null) {
  if (qc == null) {
    return false;
  }
  return qc.qcPassed == false;
}

export function caseQcNa(qc: CaseQc | null) {
  if (qc == null) {
    return false;
  }
  if (qc.qcPassed == null && qc.release == false) {
    return true;
  }
  return false;
}

function deliverableTypePhaseComplete(
  kase: Case,
  getStatus: (deliverable: CaseDeliverable) => CaseQc | null
) {
  if (!kase.deliverables.length) {
    return false;
  }
  return kase.deliverables.map(getStatus).every((x) => caseQcComplete(x));
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

function samplePhasePendingWorkQcOrTransfer(
  samples: Sample[],
  requiresTransfer: boolean = false
) {
  // pending if there are no samples
  if (!samples.length) {
    return true;
  }
  // pending if any sample needs QC or data review
  if (
    samples.some((sample) =>
      [qcStatuses.qc, qcStatuses.dataReview].includes(getQcStatus(sample))
    )
  ) {
    return true;
  }
  // pending if there are no samples with passed QC, data review if applicable,
  // and transfer if applicable
  return !samples.some(
    (sample) =>
      sample.qcPassed &&
      (!sample.run || sample.dataReviewPassed) &&
      (!requiresTransfer || sample.transferDate)
  );
}

export function getSamplePhaseHighlight(
  requisition: Requisition,
  samples: Sample[],
  requiresTransfer: boolean = false
) {
  if (samplePhaseComplete(samples, requiresTransfer) || requisition.paused) {
    return null;
  } else if (requisition.stopped) {
    return samples.length ? null : "na";
  } else {
    return "warning";
  }
}

function getDeliverableTypePhaseHighlight(
  kase: Case,
  getQcStatus: (deliverable: CaseDeliverable) => CaseQc | null,
  analysisReview: boolean
) {
  if (!kase.deliverables.length) {
    return "error";
  }
  if (
    kase.requisition.paused ||
    kase.deliverables.every((deliverable) => {
      const qcStatus = getQcStatus(deliverable);
      return caseQcPassed(qcStatus) || caseQcNa(qcStatus);
    })
  ) {
    return null;
  } else if (
    analysisReview &&
    (kase.requisition.stopped ||
      kase.deliverables.every(
        (deliverable) => deliverable.analysisReviewSkipped
      ))
  ) {
    if (
      kase.deliverables.some((deliverable) => {
        const qcStatus = getQcStatus(deliverable);
        return caseQcComplete(qcStatus);
      })
    ) {
      return null;
    } else {
      return "na";
    }
  } else if (
    kase.deliverables.some((deliverable) => {
      const qcStatus = getQcStatus(deliverable);
      return caseQcFailed(qcStatus);
    })
  ) {
    return "error";
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
  fragment: DocumentFragment,
  transferRequired: boolean = false
) {
  const phaseComplete = samplePhaseComplete(samples, transferRequired);
  samples.forEach((sample, i) => {
    let status = getQcStatus(sample);
    if (
      internalUser &&
      status === qcStatuses.passed &&
      !sample.assayIds?.includes(assayId)
    ) {
      status = qcStatuses.passedDifferentAssay;
    }
    if (
      transferRequired &&
      !phaseComplete &&
      [qcStatuses.passed, qcStatuses.passedDifferentAssay].includes(status) &&
      !sample.transferDate
    ) {
      status = qcStatuses.transfer;
    }
    if (
      sample.run &&
      !sample.run.completionDate &&
      [qcStatuses.qc, qcStatuses.dataReview].includes(status)
    ) {
      status = qcStatuses.sequencing;
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
      const runNameContainer = makeNameDiv(
        sample.sequencingLane
          ? sample.run.name + " (L" + sample.sequencingLane + ")"
          : sample.run.name,
        urls.miso.run(sample.run.name),
        internalUser ? urls.dimsum.run(sample.run.name) : undefined,
        sample.run.name
      );
      if (!internalUser) {
        runNameContainer.classList.add("font-bold");
      }
      topContainer.appendChild(runNameContainer);
      const runStatus = getQcStatusWithDataReview(sample.run);
      addStatusTooltipText(
        topContainer,
        runStatus,
        sample.run.qcReason,
        sample.run.qcUser,
        sample.run.qcNote,
        "Run status"
      );
    }
    // sample name links
    const sampleNameContainer = makeNameDiv(
      sample.name,
      urls.miso.sample(sample.id)
    );
    sampleNameContainer.classList.add("font-bold");
    topContainer.appendChild(sampleNameContainer);
    const sampleStatus = getSampleQcStatus(sample);
    addStatusTooltipText(
      topContainer,
      sampleStatus,
      sample.qcReason,
      sample.qcUser,
      sample.qcNote
    );

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
    sample.assayIds?.forEach((assayId, index) => {
      const assay = siteConfig.assaysById[assayId];
      addTooltipRow(bottomContainer, index === 0 ? "Assay" : "", assay.name);
    });
    fragment.appendChild(topContainer);
    fragment.appendChild(bottomContainer);
  };
}

export function addStatusTooltipText(
  tooltip: Node,
  status: QcStatus,
  qcReason: string | null,
  qcUser?: string | null,
  qcNote?: string | null,
  alternateLabel?: string
) {
  if (status === qcStatuses.dataReview) {
    // prioritize pending data review over QC reason
    tooltip.appendChild(makeTextDiv("Status: " + status.label));
  } else {
    let statusText =
      (alternateLabel || "Status") + ": " + (qcReason || status.label);
    if (qcUser) {
      statusText += ` (${qcUser})`;
    }
    tooltip.appendChild(makeTextDiv(statusText));
  }
  if (qcNote) {
    tooltip.appendChild(makeTextDiv("Note: " + qcNote));
  }
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
  getQcStatus: (deliverable: CaseDeliverable) => CaseQc | null,
  getQcUser: (deliverable: CaseDeliverable) => string | null | undefined,
  getQcNote: (deliverable: CaseDeliverable) => string | null | undefined,
  fragment: DocumentFragment,
  tooltipInstance: Tooltip
) {
  kase.deliverables.forEach((deliverable, i) => {
    const qcStatus = getQcStatus(deliverable);
    const status = getDeliverableQcStatus(qcStatus);
    const icon = makeIcon(status.icon);
    const user = getQcUser(deliverable);
    const note = getQcNote(deliverable);
    const qcReason = qcStatus?.label || null;
    tooltipInstance.addTarget(icon, (tooltip) => {
      const deliverableTypeLabel = makeTextDiv(deliverable.deliverableCategory);
      deliverableTypeLabel.classList.add("font-bold");
      tooltip.appendChild(deliverableTypeLabel);
      addStatusTooltipText(tooltip, status, qcReason, user, note);
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
      const caseQcStatus = getReleaseQcStatus(release.qcStatus);
      const status = getDeliverableQcStatus(caseQcStatus);
      const icon = makeIcon(status.icon);
      tooltipInstance.addTarget(icon, (tooltip) => {
        let deliverableLabel = deliverable.deliverableCategory;
        if (release.deliverable !== deliverableLabel) {
          deliverableLabel += " - " + release.deliverable;
        }
        const releaseLabel = makeTextDiv(deliverableLabel);
        releaseLabel.classList.add("font-bold");
        tooltip.appendChild(releaseLabel);
        addStatusTooltipText(
          tooltip,
          status,
          caseQcStatus?.label || null,
          release.qcUser,
          release.qcNote
        );
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
      deliverable.releases.every((release) =>
        caseQcComplete(getReleaseQcStatus(release.qcStatus))
      )
    )
  );
}

function caseOverdue(kase: Case, targets: AssayTargets) {
  assertDefined(kase.caseDaysSpent);
  return targets.caseDays && kase.caseDaysSpent > targets.caseDays;
}

interface OverdueStep {
  stepLabel: string;
  targetDays: number | null;
}

function getOverdueStep(kase: Case, targets: AssayTargets): OverdueStep | null {
  const caseDaysSpent = kase.caseDaysSpent;
  assertDefined(caseDaysSpent);
  // Check later steps first to emphasize if we are multiple steps overdue
  if (releasePhaseBehind(kase, targets.releaseDays)) {
    return {
      stepLabel: "Release",
      targetDays: targets.releaseDays,
    };
  } else if (
    deliverableTypePhaseBehind(
      kase,
      (x) => getReleaseApprovalQcStatus(x.releaseApprovalQcStatus),
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
      (x) => getAnalysisReviewQcStatus(x.analysisReviewQcStatus),
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
        caseDaysSpent,
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
        caseDaysSpent,
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
          caseDaysSpent,
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
          caseDaysSpent,
          targets.extractionDays
        )
    )
  ) {
    return {
      stepLabel: "Extraction",
      targetDays: targets.extractionDays,
    };
  } else if (
    samplePhaseBehind(kase.receipts, caseDaysSpent, targets.receiptDays)
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
  getStatus: (deliverable: CaseDeliverable) => CaseQc | null,
  stepTarget: number | null
) {
  assertDefined(kase.caseDaysSpent);
  return (
    stepTarget &&
    !deliverableTypePhaseComplete(kase, getStatus) &&
    kase.caseDaysSpent > stepTarget
  );
}

function releasePhaseBehind(kase: Case, stepTarget: number | null) {
  assertDefined(kase.caseDaysSpent);
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
  kase: Case,
  metric: Metric,
  qcPassed: boolean | null
): CellStatus | null {
  if (metric.name === "Trimming; Minimum base quality Q") {
    return null;
  } else if (metric.thresholdType === "BOOLEAN") {
    return getBooleanMetricHighlight(qcPassed);
  }
  let anyApplicable = false;
  const metrics = getMatchingAnalysisReviewMetrics(metric.name, kase, qcGroup);
  if (!metrics || !metrics.length) {
    return null;
  }
  anyApplicable = true;
  const value = getAnalysisMetricValue(metric.name, qcGroup);
  if (value === null) {
    return "warning";
  } else if (anyFail(value, metrics)) {
    return "error";
  }
  return anyApplicable ? null : "na";
}

function showSignoffDialog(items: Case[]) {
  const commonDeliverableTypes = new Map<string, string>();
  items[0].deliverables
    .map((deliverable) => deliverable.deliverableCategory)
    .filter((deliverableType) =>
      items.every((item) =>
        item.deliverables.some(
          (deliverable) => deliverable.deliverableCategory === deliverableType
        )
      )
    )
    .forEach((deliverableType) =>
      commonDeliverableTypes.set(deliverableType, deliverableType)
    );
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
      const statuses = getStatusesForStep(result1.signoffStepName);
      const formFields: FormField<any>[] = [
        new DropdownField(
          "QC Status",
          new Map<string, CaseQc | null>(
            Object.values(statuses).map((status) => [status.label, status])
          ),
          "qcStatus",
          true,
          undefined,
          "Pending"
        ),
        new TextField("Note", "comment"),
      ];
      if (result1.signoffStepName === "RELEASE") {
        const commonDeliverables = new Map<string, string>();
        items[0].deliverables
          .filter(
            (deliverable) =>
              deliverable.deliverableCategory === result1.deliverableType
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
          showErrorDialog(
            `The selected cases have no ${result1.deliverableType} deliverable in common`
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
      showFormDialog(
        `${qcStepLabel} QC - ${result1.deliverableType}`,
        formFields,
        "Submit",
        (result2) => {
          const data = {
            caseIdentifiers: items.map((item) => item.id),
            signoffStepName: result1.signoffStepName,
            deliverableType: result1.deliverableType,
            deliverable: result2.deliverable || null,
            qcPassed: result2.qcStatus.qcPassed,
            release: result2.qcStatus.release,
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

function getStatusesForStep(stepName: string) {
  let statuses;
  switch (stepName) {
    case "ANALYSIS_REVIEW":
      statuses = siteConfig.analysisReviewQcStatuses;
      break;
    case "RELEASE_APPROVAL":
      statuses = siteConfig.releaseApprovalQcStatuses;
      break;
    case "RELEASE":
      statuses = siteConfig.releaseQcStatuses;
      break;
    default:
      throw new Error("Invalid step name: " + stepName);
  }
  return Object.values(statuses).sort((a, b) => {
    const aPriority = getQcStatusSortPriority(a);
    const bPriority = getQcStatusSortPriority(b);
    return aPriority - bPriority;
  });
}

function getQcStatusSortPriority(status: CaseQc) {
  // Sort by QC status, then release status
  // true > false > null
  let priority = 0;
  switch (status.qcPassed) {
    case true:
      priority += 100;
      break;
    case false:
      priority += 200;
      break;
    case null:
      if (status.release == null) {
        // exception: pending at top
        return 0;
      }
      priority += 300;
      break;
  }
  switch (status.release) {
    case true:
      priority += 10;
      break;
    case false:
      priority += 20;
      break;
  }
  return priority;
}

function hasDeliverable(kase: Case, deliverable: string) {
  return kase.deliverables
    .flatMap((deliverableType) => deliverableType.releases)
    .some((release) => release.deliverable === deliverable);
}

const REPORT_FULL_DEPTH_SUMMARY = "full-depth-summary";
const REPORT_DARE_INPUT_SHEET = "dare-input-sheet";
const DONOR_ASSAY_REPORT = "donor-assay-report";

function showDownloadDialog(items: Case[]) {
  const reportOptions = new Map<string, string>([
    ["Full-Depth Summary", REPORT_FULL_DEPTH_SUMMARY],
    ["DARE Input Sheet", REPORT_DARE_INPUT_SHEET],
    ["Donor Assay Report", DONOR_ASSAY_REPORT],
  ]);

  const reportSelect = new DropdownField(
    "Report",
    reportOptions,
    "report",
    true
  );
  showFormDialog("Download Case Data", [reportSelect], "Next", (result) => {
    switch (result.report) {
      case REPORT_FULL_DEPTH_SUMMARY:
      case REPORT_DARE_INPUT_SHEET:
      case DONOR_ASSAY_REPORT:
        showDownloadOptionsDialogX(result.report, items);
        break;
      default:
        throw new Error(`Invalid report: ${result.report}`);
    }
  });
}

function showDownloadOptionsDialogX(report: string, items: Case[]) {
  const additionalOptions: FormField<any>[] | undefined =
    report === REPORT_DARE_INPUT_SHEET
      ? [
          new DropdownField(
            "Include Supplemental",
            new Map<string, boolean>([
              ["Yes", true],
              ["No", false],
            ]),
            "includeSupplemental",
            true,
            undefined,
            "Yes"
          ),
        ]
      : undefined;
  const callback = (result: any) => {
    const options = result.formatOptions;
    if (report === REPORT_DARE_INPUT_SHEET) {
      options.includeSupplemental = result.includeSupplemental;
    }
    downloadCaseReport(report, options, items);
  };
  showDownloadOptionsDialog(report, items, callback, additionalOptions);
}

function downloadCaseReport(report: string, params: any, items: Case[]) {
  params.caseIds = items.map((kase) => kase.id).join(", ");
  postDownload(
    urls.rest.downloads.reports(report),
    params,
    "Generating report."
  );
}

function showTatReportDialog(
  filters: { key: string; value: string }[],
  baseFilter: { key: string; value: string } | undefined
) {
  const joinedFilters = [...filters];
  if (baseFilter !== undefined) {
    joinedFilters.push(baseFilter);
  }
  const params = {
    filters: joinedFilters,
  };
  postDownload(
    urls.rest.downloads.reports("case-tat-report"),
    params,
    "Generating report."
  );
}

function showTatTrendPage(
  filters: { key: string; value: string }[],
  baseFilter: { key: string; value: string } | undefined
) {
  const joinedFilters = [...filters];
  if (baseFilter !== undefined) {
    joinedFilters.push(baseFilter);
  }
  const urlParams = new URLSearchParams();
  joinedFilters.forEach((filter) => {
    urlParams.append(filter.key, filter.value);
  });
  const targetUrl = `/tat-trend?${urlParams.toString()}`;
  window.open(targetUrl, "_blank");
}
