import { siteConfig } from "./site-config";
import { toTitleCase } from "./html-utils";
import { Pair } from "./pair";

const restBaseUrl = "/rest";
const misoBaseUrl = `${siteConfig.misoUrl}/miso`;

export const urls = {
  dimsum: {
    case: (id: string) => `/cases/${id}`,
    donor: (name: string) => `/donors/${name}`,
    project: (name: string) => `/projects/${name}`,
    requisition: (id: number) => `/requisitions/${id}`,
    run: (name: string) => `/runs/${name}`,
  },
  rest: {
    cases: `${restBaseUrl}/cases`,
    receipts: `${restBaseUrl}/receipts`,
    extractions: `${restBaseUrl}/extractions`,
    libraryPreparations: `${restBaseUrl}/library-preparations`,
    libraryQualifications: `${restBaseUrl}/library-qualifications`,
    fullDepthSequencings: `${restBaseUrl}/full-depth-sequencings`,
    requisitions: `${restBaseUrl}/requisitions`,
    runs: {
      libraryQualifications: (runName: string) =>
        `${restBaseUrl}/runs/${runName}/library-qualifications`,
      fullDepthSequencings: (runName: string) =>
        `${restBaseUrl}/runs/${runName}/full-depth-sequencings`,
      list: `${restBaseUrl}/runs`,
    },
    autocomplete: {
      assayNames: `${restBaseUrl}/autocomplete/assay-names`,
      requisitionNames: `${restBaseUrl}/autocomplete/requisition-names`,
      projectNames: `${restBaseUrl}/autocomplete/project-names`,
      donorNames: `${restBaseUrl}/autocomplete/donor-names`,
      runNames: `${restBaseUrl}/autocomplete/run-names`,
      testNames: `${restBaseUrl}/autocomplete/test-names`,
    },
    notifications: `${restBaseUrl}/notifications`,
    omissions: `${restBaseUrl}/omissions`,
    projects: {
      projectSummary: (projecttName: string) =>
        `${restBaseUrl}/runs/${projecttName}/summary`,
      list: `${restBaseUrl}/projects`,
    },
    tests: `${restBaseUrl}/tests`,
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
    qcRunLibraries: `${misoBaseUrl}/runlibraries/metrics`,
  },
  dashi: {
    singleLaneTar: (runName: string) => makeDashiSingleLaneUrl("tar", runName),
    singleLaneWgs: (runName: string) => makeDashiSingleLaneUrl("wgs", runName),
  },
  jira: {
    issue: (key: string) => `${siteConfig.jiraUrl}/browse/${key}`,
  },
};

function makeMisoUrl(type: string, id: number | string) {
  return `${misoBaseUrl}/${type}/${id}`;
}

function makeDashiSingleLaneUrl(designCode: string, runName: string) {
  switch (designCode) {
    case "WG":
      return makeDashiRunUrl("single-lane-wgs", runName);
    case "WT":
      return makeDashiRunUrl("single-lane-ran", runName);
    case "TS":
      return makeDashiRunUrl("single-lane-tar", runName);
    default:
      return siteConfig.dashiUrl;
  }
}

export function makeDashiRunUrl(report: string, runName: string) {
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
