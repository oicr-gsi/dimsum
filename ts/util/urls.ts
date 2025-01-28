import { internalUser, siteConfig } from "./site-config";
import { Pair } from "./pair";

function getRestBaseUrl(common?: boolean) {
  if (common) {
    return "/rest/common";
  } else if (internalUser) {
    return "/rest/internal";
  } else {
    return "/rest/external";
  }
}

export const urls = {
  dimsum: {
    case: (id: string) => `/cases/${id}`,
    caseQcReport: (id: string) => `/cases/${id}/report`,
    donor: (name: string) => `/donors/${name}`,
    project: (name: string) => `/projects/${name}`,
    requisition: (id: number) => `/requisitions/${id}`,
    run: (name: string) => `/runs/${name}`,
  },
  rest: {
    cases: {
      bulkSignoff: `${getRestBaseUrl()}/cases/bulk-signoff`,
      get: (caseId: string) => `${getRestBaseUrl()}/cases/${caseId}`,
      list: `${getRestBaseUrl()}/cases`,
    },
    receipts: `${getRestBaseUrl()}/receipts`,
    extractions: `${getRestBaseUrl()}/extractions`,
    libraryPreparations: `${getRestBaseUrl()}/library-preparations`,
    libraryQualifications: `${getRestBaseUrl()}/library-qualifications`,
    fullDepthSequencings: `${getRestBaseUrl()}/full-depth-sequencings`,
    requisitions: `${getRestBaseUrl()}/requisitions`,
    runs: {
      libraryQualifications: (runName: string) =>
        `${getRestBaseUrl()}/runs/${runName}/library-qualifications`,
      fullDepthSequencings: (runName: string) =>
        `${getRestBaseUrl()}/runs/${runName}/full-depth-sequencings`,
      omissions: (runName: string) =>
        `${getRestBaseUrl()}/runs/${runName}/omissions`,
      list: `${getRestBaseUrl()}/runs`,
    },
    autocomplete: {
      assayNames: `${getRestBaseUrl(true)}/autocomplete/assay-names`,
      requisitionNames: `${getRestBaseUrl(
        true
      )}/autocomplete/requisition-names`,
      projectNames: `${getRestBaseUrl(true)}/autocomplete/project-names`,
      donorNames: `${getRestBaseUrl(true)}/autocomplete/donor-names`,
      runNames: `${getRestBaseUrl()}/autocomplete/run-names`,
      testNames: `${getRestBaseUrl(true)}/autocomplete/test-names`,
    },
    notifications: `${getRestBaseUrl()}/notifications`,
    omissions: `${getRestBaseUrl()}/omissions`,
    projects: {
      summary: (projecttName: string) =>
        `${getRestBaseUrl()}/projects/${projecttName}/summary`,
      list: `${getRestBaseUrl()}/projects`,
    },
    tests: `${getRestBaseUrl()}/tests`,
    downloads: {
      reports: (reportName: string) =>
        `${getRestBaseUrl()}/downloads/reports/${reportName}`,
    },
  },
  miso: {
    sample: function (sampleId: string) {
      const match = sampleId.match("^\\d+_\\d+_LDI(\\d+)$");
      if (match) {
        return makeMisoUrl("libraryaliquot", match[1]);
      }
      const prefix = sampleId.substring(0, 3);
      const id = parseInt(sampleId.substring(3));
      switch (prefix) {
        case "SAM":
          return makeMisoUrl("sample", id);
        case "LIB":
          return makeMisoUrl("library", id);
        case "LDI":
          return makeMisoUrl("libraryaliquot", id);
        default:
          throw new Error(`Unhandled ID pattern: ${sampleId}`);
      }
    },
    project: (shortName: string) => makeMisoUrl("project/shortname", shortName),
    run: (runName: string) => makeMisoUrl("run/alias", runName),
    requisition: (requisitionId: number) =>
      makeMisoUrl("requisition", requisitionId),
    qcRunLibraries: `${siteConfig.misoUrl}/runlibraries/metrics`,
    assay: (assayId: number) => makeMisoUrl("assay", assayId),
  },
  dashi: {
    project: {
      callReadyRna: (project: string) =>
        makeDashiProjectUrl("call-ready-rna", project),
      callReadyTar: (project: string) =>
        makeDashiProjectUrl("call-ready-tar", project),
      callReadyWgs: (project: string) =>
        makeDashiProjectUrl("call-ready-wgs", project),
      singleLaneCfMeDip: (project: string) =>
        makeDashiProjectUrl("single-lane-cfmedip", project),
      singleLaneRna: (project: string) =>
        makeDashiProjectUrl("single-lane-rna", project),
      singleLaneTar: (project: string) =>
        makeDashiProjectUrl("single-lane-tar", project),
      singleLaneWgs: (project: string) =>
        makeDashiProjectUrl("single-lane-wgs", project),
    },
    run: {
      singleLaneCfMeDip: (runName: string) =>
        makeDashiRunUrl("single-lane-cfmedip", runName),
      singleLaneRna: (runName: string) =>
        makeDashiRunUrl("single-lane-rna", runName),
      singleLaneTar: (runName: string) =>
        makeDashiRunUrl("single-lane-tar", runName),
      singleLaneWgs: (runName: string) =>
        makeDashiRunUrl("single-lane-wgs", runName),
    },
  },
  jira: {
    issue: (key: string) => `${siteConfig.jiraUrl}/browse/${key}`,
  },
};

function makeMisoUrl(type: string, id: number | string) {
  return `${siteConfig.misoUrl}/${type}/${id}`;
}

function makeDashiProjectUrl(report: string, project: string) {
  return `${siteConfig.dashiUrl}/${report}?project=${project}`;
}

function makeDashiRunUrl(report: string, runName: string) {
  return `${siteConfig.dashiUrl}/${report}?run=${runName}`;
}

export function getBaseUrl() {
  return window.location.origin + window.location.pathname;
}

export function getSearchParams() {
  var searchParams = new Array<Pair<string, string>>();
  new URL(document.location.href).searchParams.forEach((value, key) => {
    var param: Pair<string, string> = { key: key, value: value };
    searchParams.push(param);
  });
  return searchParams;
}

// append url param to current url
export function appendUrlParam(key: string, value: string) {
  var params = new URL(document.location.href).searchParams;
  params.append(key, value);
  return `?${params.toString()}`;
}

// remove url param from current url
export function removeUrlParam(key: string, value: string) {
  var params = new URL(document.location.href).searchParams;
  var newParams = new URLSearchParams();
  var paramCount = 0;
  for (const [k, v] of params.entries()) {
    if (k !== key || v !== value) {
      // create new search params list, excluding the one we would like to delete
      newParams.append(k, v);
      ++paramCount;
    }
  }
  if (paramCount > 0) {
    return `?${newParams.toString()}`;
  }
  return `${newParams.toString()}`;
}

// update the current url search params (add or remove as specified)
export function updateUrlParams(key: string, value: string, add?: boolean) {
  // append chosen filter option to url
  const uriEncode = encodeURIComponent(key);
  var info = "update page: " + (add ? "append " : "remove ") + uriEncode;
  const nextState = { info: info };
  const nextTitle = info;
  window.history.replaceState(
    nextState,
    nextTitle,
    getBaseUrl() +
      (add ? appendUrlParam(key, value) : removeUrlParam(key, value))
  );
}

// create/replace the query param with key to value
export function replaceUrlParams(key: string, value: string) {
  const url = new URL(document.location.href);
  const found = getSearchParams().find((param) => param.key === key);
  if (found !== undefined) updateUrlParams(found.key, found.value, false);
  updateUrlParams(key, value, true);
}
