import { siteConfig } from "../util/site-config";
import { urls } from "../util/urls";
import { FilterDefinition, Sort } from "./table-builder";

const yesNoOptions: string[] = ["Yes", "No"];

export const latestActivitySort: Sort = {
  columnTitle: "Latest Activity",
  descending: true,
  type: "date",
};

const projectFilter: FilterDefinition = {
  title: "Project",
  key: "PROJECT",
  type: "text",
  autocompleteUrl: urls.rest.autocomplete.projectNames,
  showExternal: true,
};

const libraryDesignFilter: FilterDefinition = {
  title: "Library Design",
  key: "LIBRARY_DESIGN",
  type: "dropdown",
  values: siteConfig.libraryDesigns,
  showExternal: true,
};

export const caseFilters: FilterDefinition[] = [
  {
    title: "Assay",
    key: "ASSAY",
    type: "text",
    autocompleteUrl: urls.rest.autocomplete.assayNames,
    showExternal: true,
  },
  {
    title: "Donor",
    key: "DONOR",
    type: "text",
    autocompleteUrl: urls.rest.autocomplete.donorNames,
    showExternal: true,
  },
  {
    title: "Pending",
    key: "PENDING",
    type: "dropdown",
    values: siteConfig.pendingStates,
    showExternal: false,
  },
  {
    title: "Pending Release",
    key: "PENDING_RELEASE_DELIVERABLE",
    type: "dropdown",
    values: siteConfig.deliverables,
    showExternal: true,
  },
  {
    title: "Staged Release",
    key: "STAGED_DELIVERABLE",
    type: "dropdown",
    values: siteConfig.deliverables.concat(["any"]),
    showExternal: true,
  },
  {
    title: "Completed",
    key: "COMPLETED",
    type: "dropdown",
    values: siteConfig.completedGates,
    showExternal: true,
  },
  {
    title: "Incomplete",
    key: "INCOMPLETE",
    type: "dropdown",
    values: siteConfig.completedGates,
    showExternal: true,
  },
  {
    title: "Pipeline",
    key: "PIPELINE",
    type: "dropdown",
    values: siteConfig.pipelines,
    showExternal: true,
  },
  projectFilter,
  {
    title: "Requisition",
    key: "REQUISITION",
    type: "text",
    autocompleteUrl: urls.rest.autocomplete.requisitionNames,
    showExternal: true,
  },
  {
    title: "Test",
    key: "TEST",
    type: "text",
    autocompleteUrl: urls.rest.autocomplete.testNames,
    showExternal: true,
  },
  {
    title: "Stopped",
    key: "STOPPED",
    type: "dropdown",
    values: yesNoOptions,
    showExternal: true,
  },
  {
    title: "Paused",
    key: "PAUSED",
    type: "dropdown",
    values: yesNoOptions,
    showExternal: true,
  },
  libraryDesignFilter,
  {
    title: "Deliverable",
    key: "DELIVERABLE",
    type: "dropdown",
    values: siteConfig.deliverables,
    showExternal: true,
  },
  {
    title: "Started After",
    key: "STARTED_AFTER",
    type: "date",
    showExternal: true,
  },
  {
    title: "Started Before",
    key: "STARTED_BEFORE",
    type: "date",
    showExternal: true,
  },
  {
    title: "Completed After",
    key: "COMPLETED_AFTER",
    type: "date",
    showExternal: true,
  },
  {
    title: "Completed Before",
    key: "COMPLETED_BEFORE",
    type: "date",
    showExternal: true,
  },
  {
    title: "Archiving Status",
    key: "ARCHIVING_STATUS",
    type: "dropdown",
    values: ["Pending", "Started", "Paused", "Complete", "Deleted", "Expired"],
    showExternal: false,
  },
];

export const runLibraryFilters: FilterDefinition[] = [
  projectFilter,
  libraryDesignFilter,
];
