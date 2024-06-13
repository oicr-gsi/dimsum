import { legendAction, TableDefinition } from "../component/table-builder";
import {
  addLink,
  styleText,
  addMisoIcon,
  makeNameDiv,
  addNaText,
  addTextDiv,
  makeTextDivWithTooltip,
} from "../util/html-utils";
import { siteConfig } from "../util/site-config";
import { urls } from "../util/urls";
import {
  Test,
  Donor,
  Project,
  samplePhaseComplete,
  addConstructionIcon,
  addSpace,
  assertNotNull,
  samplePhasePendingWork,
  getSamplePhaseHighlight,
  handleNaSamplePhase,
  addSampleIcons,
} from "./case";
import { Sample } from "./sample";
import { Requisition } from "./requisition";
import { Tooltip } from "../component/tooltip";
import { caseFilters, latestActivitySort } from "../component/table-components";

export interface TestTableView {
  test: Test;
  caseId: string;
  requisition: Requisition;
  donor: Donor;
  projects: Project[];
  assayName: string;
  assayId: number;
  tissueOrigin: string;
  tissueType: string;
  timepoint: string;
  receipts: Sample[];
  latestActivityDate: string;
}

export const testDefinition: TableDefinition<TestTableView, void> = {
  queryUrl: urls.rest.tests,
  defaultSort: latestActivitySort,
  filters: caseFilters,
  staticActions: [legendAction],
  generateColumns: () => [
    {
      title: "Project",
      addParentContents(testTableView, fragment) {
        testTableView.projects.forEach((project) => {
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
      addParentContents(testTableView, fragment) {
        fragment.appendChild(
          makeNameDiv(
            testTableView.donor.name,
            urls.miso.sample(testTableView.donor.id),
            urls.dimsum.donor(testTableView.donor.name)
          )
        );
        fragment.appendChild(
          makeTextDivWithTooltip(
            testTableView.donor.externalName,
            "External Name"
          )
        );

        const tumourDetailDiv = document.createElement("div");
        tumourDetailDiv.appendChild(
          document.createTextNode(
            `${testTableView.tissueOrigin} ${testTableView.tissueType}` +
              (testTableView.timepoint ? " " + testTableView.timepoint : "")
          )
        );
        const addContents = (fragment: DocumentFragment) => {
          addTextDiv(`Tissue Origin: ${testTableView.tissueOrigin}`, fragment);
          addTextDiv(`Tissue Type: ${testTableView.tissueType}`, fragment);
          if (testTableView.timepoint) {
            addTextDiv(`Timepoint: ${testTableView.timepoint}`, fragment);
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
      addParentContents(testTableView, fragment) {
        const assayDiv = document.createElement("div");
        const assay = siteConfig.assaysById[testTableView.assayId];
        addLink(assayDiv, assay.name, urls.dimsum.case(testTableView.caseId));
        fragment.appendChild(assayDiv);

        const requisition = testTableView.requisition;
        if (requisition.stopped) {
          const stoppedDiv = makeTextDivWithTooltip(
            "CASE STOPPED",
            `Stop reason: ${requisition.stopReason || "Unspecified"}`
          );
          styleText(stoppedDiv, "error");
          fragment.appendChild(stoppedDiv);
        }
        const requisitionDiv = document.createElement("div");
        addLink(
          requisitionDiv,
          requisition.name,
          urls.dimsum.requisition(requisition.id)
        );
        addMisoIcon(requisitionDiv, urls.miso.requisition(requisition.id));
        fragment.appendChild(requisitionDiv);
      },
    },
    {
      title: "Test",
      sortType: "text",
      addParentContents(testTableView, fragment) {
        const testNameDiv = document.createElement("div");
        testNameDiv.appendChild(makeNameDiv(testTableView.test.name));
        fragment.appendChild(testNameDiv);

        if (testTableView.test.groupId) {
          const groupIdDiv = document.createElement("div");
          groupIdDiv.appendChild(
            document.createTextNode(testTableView.test.groupId)
          );
          const tooltipInstance = Tooltip.getInstance();
          tooltipInstance.addTarget(groupIdDiv, (fragment) =>
            fragment.appendChild(document.createTextNode("Group ID"))
          );
          fragment.appendChild(groupIdDiv);
        }
      },
    },
    {
      title: "Extraction",
      addParentContents(testTableView, fragment) {
        if (
          handleNaSamplePhase(
            testTableView.requisition,
            testTableView.test.extractions,
            fragment
          )
        ) {
          return;
        }
        if (
          !testTableView.test.extractions.length &&
          testTableView.test.extractionSkipped
        ) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(
          testTableView.assayId,
          testTableView.test.extractions,
          fragment,
          true
        );
        if (
          samplePhaseComplete(testTableView.receipts) &&
          samplePhasePendingWork(testTableView.test.extractions)
        ) {
          if (testTableView.test.extractions.length) {
            addSpace(fragment);
          }
          addConstructionIcon("extraction", fragment);
        }
      },
      getCellHighlight(testTableView) {
        testTableView.test = assertNotNull(testTableView.test);
        if (
          !testTableView.test.extractions.length &&
          testTableView.test.extractionSkipped
        ) {
          return "na";
        }
        return getSamplePhaseHighlight(
          testTableView.requisition,
          testTableView.test.extractions,
          true
        );
      },
    },
    {
      title: "Library Preparation",
      addParentContents(testTableView, fragment) {
        if (
          handleNaSamplePhase(
            testTableView.requisition,
            testTableView.test.libraryPreparations,
            fragment
          )
        ) {
          return;
        }
        if (
          !testTableView.test.libraryPreparations.length &&
          testTableView.test.libraryPreparationSkipped
        ) {
          addNaText(fragment);
          return;
        }
        addSampleIcons(
          testTableView.assayId,
          testTableView.test.libraryPreparations,
          fragment
        );
        if (
          samplePhaseComplete(testTableView.test.extractions, true) &&
          samplePhasePendingWork(testTableView.test.libraryPreparations)
        ) {
          if (testTableView.test.libraryPreparations.length) {
            addSpace(fragment);
          }
          addConstructionIcon("library preparation", fragment);
        }
      },
      getCellHighlight(testTableView) {
        testTableView.test = assertNotNull(testTableView.test);
        if (
          !testTableView.test.libraryPreparations.length &&
          testTableView.test.libraryPreparationSkipped
        ) {
          return "na";
        }
        return getSamplePhaseHighlight(
          testTableView.requisition,
          testTableView.test.libraryPreparations
        );
      },
    },
    {
      title: "Library Qualification",
      addParentContents(testTableView, fragment) {
        if (
          handleNaSamplePhase(
            testTableView.requisition,
            testTableView.test.libraryQualifications,
            fragment
          )
        ) {
          return;
        }
        addSampleIcons(
          testTableView.assayId,
          testTableView.test.libraryQualifications,
          fragment
        );
        if (
          samplePhaseComplete(testTableView.test.libraryPreparations) &&
          samplePhasePendingWork(testTableView.test.libraryQualifications)
        ) {
          if (testTableView.test.libraryQualifications.length) {
            addSpace(fragment);
          }
          addConstructionIcon("library qualification", fragment);
        }
      },
      getCellHighlight(testTableView) {
        testTableView.test = assertNotNull(testTableView.test);
        return getSamplePhaseHighlight(
          testTableView.requisition,
          testTableView.test.libraryQualifications
        );
      },
    },
    {
      title: "Full-Depth Sequencing",
      addParentContents(testTableView, fragment) {
        if (
          handleNaSamplePhase(
            testTableView.requisition,
            testTableView.test.fullDepthSequencings,
            fragment
          )
        ) {
          return;
        }
        addSampleIcons(
          testTableView.assayId,
          testTableView.test.fullDepthSequencings,
          fragment
        );
        if (
          samplePhaseComplete(testTableView.test.libraryQualifications) &&
          samplePhasePendingWork(testTableView.test.fullDepthSequencings)
        ) {
          if (testTableView.test.fullDepthSequencings.length) {
            addSpace(fragment);
          }
          addConstructionIcon("full-depth sequencing", fragment);
        }
      },
      getCellHighlight(testTableView) {
        testTableView.test = assertNotNull(testTableView.test);
        return getSamplePhaseHighlight(
          testTableView.requisition,
          testTableView.test.fullDepthSequencings
        );
      },
    },
    {
      title: "Latest Activity",
      sortType: "date",
      addParentContents(testTableView, fragment) {
        fragment.appendChild(
          document.createTextNode(testTableView.latestActivityDate)
        );
      },
    },
  ],
};
