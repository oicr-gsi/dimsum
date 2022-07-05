// adds a header cell to a table header
export function addColumnHeader(
  thead: HTMLTableRowElement,
  header: string,
  index: number
) {
  const th = document.createElement("th");
  th.className =
    "p-4 text-white font-semibold bg-grey-300 text-left align-text-top";
  if (index > 0) {
    th.classList.add("border-grey-200", "border-l-1");
  }
  th.appendChild(document.createTextNode(header));
  thead.appendChild(th);
}

// adds a cell to a table row
export function addCell(tr: HTMLTableRowElement, index: number) {
  const td = tr.insertCell();
  td.className = "p-4 border-grey-200 border-t-1 text-left align-text-top";
  if (index > 0) {
    td.classList.add("border-l-1");
  }
  return td;
}

// adds a styled link to an element
export function addLink(container: HTMLElement, text: string, url: string) {
  const a = document.createElement("a");
  a.setAttribute("href", url);
  a.className = "text-green-200 font-bold hover:underline";
  a.innerHTML = text;
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
