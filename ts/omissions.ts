import { showAlertDialog } from "./component/dialog";
import { TableBuilder } from "./component/table-builder";
import { omittedSampleDefinition } from "./data/omitted-sample";
import { getSearchParams, updateUrlParams } from "./util/urls";

new TableBuilder(
  omittedSampleDefinition,
  "omissionTableContainer",
  getSearchParams(),
  updateUrlParams
).build();

document.getElementById("omissionsInfo")?.addEventListener("click", () => {
  const fragment = document.createDocumentFragment();
  const list = document.createElement("ol");
  list.className = "list-decimal pl-8";
  [
    "The sample is included in a requisition",
    "The requisition specifies an assay",
    "The assay is defined with tests",
  ].forEach((text) => {
    const item = document.createElement("li");
    item.innerText = text;
    list.appendChild(item);
  });
  const note = document.createElement("p");
  note.innerText =
    "Note: samples from projects that are inactive or have pipeline 'Sample Tracking only' will " +
    "not be shown here";
  note.className = "mt-2";
  fragment.append(list, note);
  showAlertDialog(
    "Omissions",
    "These samples and their children will not be included in other parts of Dimsum because they do" +
      " not have a well-defined assay. To have a well-defined assay means:",
    fragment
  );
});
