import { siteConfig } from "./site-config";

export const urls = {
  miso: {
    sample: function (sampleId: string) {
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
          throw new Error(`Unhandled ID prefix: ${prefix}`);
      }
    },
    project: (shortName: string) => makeMisoUrl("project/shortname", shortName),
    run: (runId: number) => makeMisoUrl("run", runId),
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

function makeDashiRunUrl(report: string, runName: string) {
  return `${siteConfig.dashiUrl}/${report}?run=${runName}`;
}
