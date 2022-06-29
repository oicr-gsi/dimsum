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
    th.classList.add("border-grey-200", "border-l-2");
  }
  th.appendChild(document.createTextNode(header));
  thead.appendChild(th);
}

// adds a cell to a table row
export function addCell(tr: HTMLTableRowElement) {
  const td = tr.insertCell();
  td.className = "p-4 border-grey-200 border-t-2 text-left align-text-top";
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

// applies N/A style shading to a cell
export function shadeNotApplicable(td: HTMLTableCellElement) {
  td.className = "text-grey-300 bg-grey-200 border-grey-200 border-t-2";
}
