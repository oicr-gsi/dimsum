import { makeIcon } from "../util/html-utils";

function showAlertDialog(
  title: string,
  text: string,
  additionalContent?: Node
) {
  const dialog = document.createElement("dialog");
  dialog.className =
    "text-black bg-white rounded-xl border-2 border-grey-200 drop-shadow-lg";
  const header = document.createElement("div");
  header.className = "flex flex-row items-center border-b-1 border-grey-200";
  const titleElement = document.createElement("span");
  titleElement.className =
    "font-sarabun font-thin text-green-200 text-24 my-2 mx-4";
  titleElement.innerText = title;
  const spacer = document.createElement("div");
  spacer.className = "flex-auto";
  const icon = makeIcon("xmark");
  icon.classList.add(
    "fa-solid",
    "fa-xmark",
    "text-grey-200",
    "hover:text-green-200",
    "text-24",
    "m-2",
    "mx-4",
    "cursor-pointer"
  );
  icon.addEventListener("click", () => {
    (dialog as any).close();
    dialog.remove();
  });
  header.append(titleElement, spacer, icon);
  // grid of legend labels
  const body = document.createElement("div");
  body.className = "m-4";
  const textElement = document.createElement("p");
  textElement.className = "mb-2";
  textElement.innerText = text;
  body.appendChild(textElement);
  if (additionalContent) {
    body.appendChild(additionalContent);
  }
  dialog.append(header, body);
  document.body.appendChild(dialog);
  (dialog as any).showModal();
}

export function showErrorDialog(text: string, additionalContent?: Node) {
  showAlertDialog("Error", text, additionalContent);
}
