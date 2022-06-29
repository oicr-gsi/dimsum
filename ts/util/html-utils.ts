// adds a header cell to a table header
export function addColumnHeader(
  thead: HTMLTableRowElement,
  header: string,
  index: number
) {
  const th = document.createElement("th");
  th.className = "p-4 text-white font-semibold bg-grey-300";
  if (index > 0) {
    th.classList.add("border-grey-200", "border-l-2");
  }
  th.appendChild(document.createTextNode(header));
  thead.appendChild(th);
}

// adds link-styled text to a div
export function addLink(container: HTMLElement, text: string, url: string) {
  const a = document.createElement("a");
  a.setAttribute("href", url);
  a.className = "text-green-200 font-bold hover:underline";
  a.innerHTML = text;
  container.appendChild(a);
}

// updates an empty col with corresponding style
export function styleEmptyColumn(td: HTMLTableCellElement) {
  td.className = "text-grey-300 bg-grey-200 border-grey-200 border-t-2";
  td.appendChild(document.createTextNode("N/A"));
}

// updates an empty row with corresponding style
export function styleNoDataRow(cell: HTMLTableCellElement) {
  cell.className =
    "text-black bg-grey-100 font-bold border-grey-200 border-t-2";
  cell.appendChild(document.createTextNode("NO DATA"));
}
