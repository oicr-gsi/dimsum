// adds a header cell to a table header
export function styleColumnHeader(th: HTMLTableCellElement, header: string) {
  th.className = "p-4 text-white font-semibold bg-grey-300";
  th.appendChild(document.createTextNode(header));
}

// adds link-styled text to a div
export function styleNameLink(div: HTMLDivElement, name: string) {
  div.className = "text-green-200 font-bold hover:underline";
  div.appendChild(document.createTextNode(name));
}

// updates an empty col with corresponding style
export function styleEmptyColumn(td: HTMLTableCellElement) {
  td.className = "text-grey-300 bg-grey-200";
  td.appendChild(document.createTextNode("N/A"));
}

// updates an empty row with corresponding style
export function styleNoDataRow(cell: HTMLTableCellElement) {
  cell.className = "text-black bg-grey-100 font-bold";
  cell.appendChild(document.createTextNode("NO DATA"));
}
