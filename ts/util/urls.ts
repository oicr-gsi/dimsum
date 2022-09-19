import { siteConfig } from "./site-config";
import { toTitleCase } from "./html-utils";

const restBaseUrl = "/rest";

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
    },
    autocomplete: {
      assayNames: `${restBaseUrl}/autocomplete/assay-names`,
      requisitionNames: `${restBaseUrl}/autocomplete/requisition-names`,
      projectNames: `${restBaseUrl}/autocomplete/project-names`,
      donorNames: `${restBaseUrl}/autocomplete/donor-names`,
    },
    notifications: `${restBaseUrl}/notifications`,
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
  },
  dashi: {
    singleLaneTar: (runName: string) => makeDashiSingleLaneUrl("tar", runName),
    singleLaneWgs: (runName: string) => makeDashiSingleLaneUrl("wgs", runName),
  },
};

const misoBaseUrl = `${siteConfig.misoUrl}/miso`;

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

// append a filter query to url
export function appendFilter(url: string, key: string, value: string) {
  if (url.includes("?")) return `${url}&${key}=${value}`;
  return `${url}?${key}=${value}`;
}

// append page number to url
export function appendPageNumber(url: string, key: string, value: string) {
  return "";
}

// append page size
export function appendPageSize(url: string, key: string, value: string) {
  return "";
}

export function removeFilter(url: string, key: string, value: string) {
  // ensure filter passed in exists in the url
  if (url.includes(value)) {
    return url.replace(toTitleCase(key) + `=${value}`, "");
  }
  // TODO: Remove question mark if no filters are being applied
  return url; // return original url if filter does not exist
}
