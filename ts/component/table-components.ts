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
};

const libraryDesignFilter: FilterDefinition = {
  title: "Library Design",
  key: "LIBRARY_DESIGN",
  type: "dropdown",
  values: siteConfig.libraryDesigns,
};

export const caseFilters: FilterDefinition[] = [
  {
    title: "Assay",
    key: "ASSAY",
    type: "text",
    autocompleteUrl: urls.rest.autocomplete.assayNames,
  },
  {
    title: "Donor",
    key: "DONOR",
    type: "text",
    autocompleteUrl: urls.rest.autocomplete.donorNames,
  },
  {
    title: "Pending",
    key: "PENDING",
    type: "dropdown",
    values: siteConfig.pendingStates,
  },
  {
    title: "Pending Release",
    key: "PENDING_RELEASE_DELIVERABLE",
    type: "dropdown",
    values: siteConfig.deliverables,
  },
  {
    title: "Completed",
    key: "COMPLETED",
    type: "dropdown",
    values: siteConfig.completedGates,
  },
  {
    title: "Incomplete",
    key: "INCOMPLETE",
    type: "dropdown",
    values: siteConfig.completedGates,
  },
  {
    title: "Pipeline",
    key: "PIPELINE",
    type: "dropdown",
    values: siteConfig.pipelines,
  },
  projectFilter,
  {
    title: "Requisition",
    key: "REQUISITION",
    type: "text",
    autocompleteUrl: urls.rest.autocomplete.requisitionNames,
  },
  {
    title: "Test",
    key: "TEST",
    type: "text",
    autocompleteUrl: urls.rest.autocomplete.testNames,
  },
  {
    title: "Stopped",
    key: "STOPPED",
    type: "dropdown",
    values: yesNoOptions,
  },
  {
    title: "Paused",
    key: "PAUSED",
    type: "dropdown",
    values: yesNoOptions,
  },
  libraryDesignFilter,
  {
    title: "Started After",
    key: "STARTED_AFTER",
    type: "text",
  },
  {
    title: "Started Before",
    key: "STARTED_BEFORE",
    type: "text",
  },
  {
    title: "Completed After",
    key: "COMPLETED_AFTER",
    type: "text",
  },
  {
    title: "Completed Before",
    key: "COMPLETED_BEFORE",
    type: "text",
  },
];

export const runLibraryFilters: FilterDefinition[] = [
  projectFilter,
  libraryDesignFilter,
];
