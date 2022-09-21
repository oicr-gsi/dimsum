import { siteConfig } from "./site-config";
import { toTitleCase } from "./html-utils";
import { StringMappingType } from "typescript";

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

// create the option url appendment (given the next url)
export function makeUrlOption(nextUrl: string) {
  // if there exists other filters, append current filter
  if (window.location.href.includes("?")) {
    return `&${nextUrl}`;
  }
  // else, no other filters exist, append current filter as the first
  return `?${nextUrl}`;
}

// append url param to current url
export function appendUrlParam(key: string, value: string) {
  console.log(`ADDING URL OPTION (key, value): (${key}, ${value})`);
  var params = new URL(document.location.href).searchParams;
  params.append(key, value);
  return `?${params.toString()}`;
}

// remove url param from current url
export function removeUrlParam(key: string, value: string) {
  console.log(`REMOVING URL OPTION (key, value): (${key}, ${value})`);
  var params = new URL(document.location.href).searchParams;

  console.log("BEFORE REMOVAL");
  params.forEach((k, v) => {
    console.log(`${k}, ${v}`);
  });

  var newParams = new URLSearchParams();
  var paramCount = 0;
  for (const [k, v] of params.entries()) {
    if (k !== toTitleCase(key) || v !== value) {
      // create new search params list, excluding the one we would like to delete
      newParams.append(k, v);
      ++paramCount;
    }
  }

  console.log("AFTER REMOVAL");
  newParams.forEach((k, v) => {
    console.log(`${k}, ${v}`);
  });
  console.log(`paramCount: ${paramCount}`);

  if (paramCount > 0) {
    return `?${newParams.toString()}`;
  }
  return `${newParams.toString()}`;

  // const values = params.getAll(toTitleCase(key));

  // console.log(`VALUES ASSOCIATED WITH ${toTitleCase(key)} (key):`);
  // values.forEach((v) => {
  //   console.log(v);
  // });

  // const index = values.indexOf(value, 0);
  // console.log(`INDEX OF ${value} (value): ${index}`);

  // // ensure key-value pair exists prior to removal
  // if (index > -1) {
  //   values.splice(index, 1); // remove appropriate value
  //   // reset key-value(s) pair in url
  //   values.forEach((v) => {
  //     params.set(key, v);
  //   });
  // }
  // console.log(`FINAL: ${params.toString()}`);
  // return `${params.toString()}`;
}

// MANUALLY APPEND AND REMOVE FILTER OPTIONS
// export function appendUrlOption(url: string, key: string, value: string) {
//   // if there exists other filters, append current filter
//   if (window.location.href.includes("?")) {
//     return `${url}&${key.replace(" ", "%")}=${value.replace(" ", "%")}`;
//   }
//   // else, no other filters exist, append current filter as the first
//   return `${url}?${key.replace(" ", "%")}=${value.replace(" ", "%")}`;
// }

// export function removeUrlOption(url: string, key: string, value: string) {
//   // ensure option passed in exists in the url and remove it
//   if (url.includes(value.replace(" ", "%"))) {
//     const option = `${toTitleCase(key.replace(" ", "%"))}=${value.replace(
//       " ",
//       "%"
//     )}`;

//     console.log(option);

//     var searchParams = new URLSearchParams(window.location.href);

//     console.log(searchParams.entries);
//     console.log(searchParams.entries.length);

//     if (searchParams.entries.length - 1 > 0) {
//       // there exist other params besides the one we wish to remove,
//       // remove the & appendment
//       return url.replace(`&${option}`, "");
//     } else if (searchParams.entries.length - 1 == 0) {
//       // only this search param exists, remove ?
//       return url.replace(`?${option}`, "");
//     }
//   }
//   // return original url if option does not exist
//   return url;
// }
