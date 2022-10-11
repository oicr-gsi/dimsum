import { caseDefinition } from "./data/case";
import { TableBuilder } from "./component/table-builder";
import { updateUrlParams } from "./util/urls";

// Parsing URL parameters should not be the responsibility of table-builder
// as we may not want the same behaviour in all tables. Instead, the process
// should look something like this:

//     1. Relevant page scripts (index.ts, details.ts, etc.) parse the URL parameters

//     2. Filters are passed as a list of key-value pairs to table-builder
//     either in the constructor, the build function, or something in-between

//     3. For each filter passed, table-builder checks if there's a matching key
//     in the TableDefinition, and if so, creates/adds an AcceptedFilter

// parse url search params and pass to table builder as map
var searchParams = new Map<string, string>();
new URL(document.location.href).searchParams.forEach((value, key) => {
  searchParams.set(key, value);
});

console.log("START BUILDING TABLE");
new TableBuilder(
  caseDefinition,
  "casesTableContainer",
  searchParams,
  updateUrlParams
).build();
