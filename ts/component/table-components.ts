import { siteConfig } from "../util/site-config";
import { urls } from "../util/urls";
import { FilterDefinition, SortDefinition } from "./table-builder";

export const latestActivitySort: SortDefinition = {
  columnTitle: "Latest Activity",
  descending: true,
  type: "date",
};

export var caseFilters: FilterDefinition[] = [
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
    title: "Completed",
    key: "COMPLETED",
    type: "dropdown",
    values: siteConfig.completedGates,
  },
  {
    title: "Pipeline",
    key: "PIPELINE",
    type: "dropdown",
    values: siteConfig.pipelines,
  },
  {
    title: "Project",
    key: "PROJECT",
    type: "text",
    autocompleteUrl: urls.rest.autocomplete.projectNames,
  },
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
    values: siteConfig.stopStatus,
  },
];
