// adds a header cell to a table header
export function makeColumnHeader(
  thead: HTMLTableRowElement,
  header: string,
  index: number
) {
  const th = document.createElement("th");
  th.className =
    "p-4 text-white font-semibold bg-grey-300 text-left align-text-top" +
    (index > 0 ? " border-grey-200 border-l-1" : "");
  th.appendChild(document.createTextNode(header));
  thead.appendChild(th);
}

// adds a cell to a table row
export function makeCell(tr: HTMLTableRowElement, index: number) {
  const td = tr.insertCell();
  td.className =
    "p-4 border-grey-200 border-t-1 text-left align-text-top" +
    (index > 0 ? " border-l-1" : "");
  return td;
}

// adds a styled link to an element
export function makeLink(container: HTMLElement, text: string, url: string) {
  const a = document.createElement("a");
  a.setAttribute("href", url);
  a.className = "text-green-200 font-bold hover:underline";
  a.innerHTML = text;
  container.appendChild(a);
}

export function addMisoIcon(container: HTMLElement, url: string) {
  const a = document.createElement("a");
  a.setAttribute("href", url);
  const img = document.createElement("img");
  img.src = "/img/miso_logo.svg";
  img.alt = "View in MISO";
  a.appendChild(img);
  container.appendChild(a);
}

export type CellStatus = "na" | "stopped" | "warning" | "error";

// map cell status to styles
const highlightClasses: Record<CellStatus, string[]> = {
  na: ["text-grey-300", "bg-grey-150"],
  stopped: ["bg-grey-100"],
  warning: ["bg-yellow"],
  error: ["bg-red"],
};

// shade cell backgrounds
export function shadeElement(element: HTMLElement, status?: CellStatus | null) {
  if (status) {
    element.classList.add(...highlightClasses[status]);
  }
}

export type TextStyle = "error";

// styles container text to match appropriate text style
export function styleText(element: HTMLElement, style?: TextStyle | null) {
  if (style === "error") {
    element.classList.add("text-red", "font-bold");
  }
}

export function makeIcon(name: string) {
  const icon = document.createElement("i");
  icon.className = `fa-solid fa-${name}`;
  return icon;
}

export function makeDropdownButton() {
  const button = document.createElement("button");
  button.className =
    "font-inter font-medium text-12 text-black bg-grey-100 px-2 py-1 rounded-md hover:ring-2 ring-green-200 ring-offset-1";
  return button;
}

export function makeDropdownMenuContainer() {
  const dropdownMenuContainer = document.createElement("menu");
  dropdownMenuContainer.className =
    "absolute hidden mt-1 w-fit rounded-md p-1 bg-grey-100 font-inter font-medium text-black text-12 drop-shadow-lg cursor-pointer";
  return dropdownMenuContainer;
}

export function makeDropdownClickout() {
  const clickout = document.createElement("button");
  clickout.className =
    "bg-transparent fixed inset-0 w-full h-full cursor-default hidden";
  return clickout;
}
