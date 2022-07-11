import * as Rest from "./rest-api";
import { makeDropdownMenu, BasicDropdownOption } from "./dropdown";

import {
  makeCell,
  addColumnHeader,
  CellStatus,
  makeIcon,
  shadeElement,
} from "./html-utils";

type SortType = "number" | "text" | "date";

export interface ColumnDefinition<ParentType, ChildType> {
  title: string;
  sortType?: SortType;
  child?: boolean;
  addParentContents?: (object: ParentType, fragment: DocumentFragment) => void;
  addChildContents?: (
    object: ChildType,
    parent: ParentType,
    fragment: DocumentFragment
  ) => void;
  getCellHighlight?: (
    object: ParentType,
    child: ChildType | null
  ) => CellStatus | null;
}

export interface SortDefinition {
  columnTitle: string;
  descending: boolean;
  type: SortType;
}
export interface TableDefinition<ParentType, ChildType> {
  queryUrl: string;
  defaultSort: SortDefinition;
  getChildren?: (parent: ParentType) => ChildType[];
  columns: ColumnDefinition<ParentType, ChildType>[];
  getRowHighlight?: (object: ParentType) => CellStatus | null;
}

export class TableBuilder<ParentType, ChildType> {
  definition: TableDefinition<ParentType, ChildType>;
  container: HTMLElement;
  sortColumn: string;
  sortDescending: boolean;

  constructor(
    definition: TableDefinition<ParentType, ChildType>,
    containerId: string
  ) {
    this.definition = definition;
    this.sortColumn = definition.defaultSort.columnTitle;
    this.sortDescending = definition.defaultSort.descending;
    const container = document.getElementById(containerId);
    if (container === null) {
      throw Error(`Container ID "${containerId}" not found on page`);
    }
    this.container = container;
  }

  public build() {
    this.addSortControls();
    // TODO: add filtering controls
    // TODO: add paging controls
    const tableContainer = document.createElement("div");
    tableContainer.className = "mt-4 overflow-auto";
    const table = document.createElement("table");
    // set global default styling settings
    table.classList.add(
      "w-full",
      "text-14",
      "text-black",
      "font-medium",
      "font-inter",
      "border-separate",
      "border-spacing-0",
      "border-grey-200",
      "border-2",
      "rounded-xl",
      "overflow-hidden"
    );

    tableContainer.appendChild(table);
    this.container.appendChild(tableContainer);
    this.load();
    // TODO: add action buttons
    this.reload();
  }

  private addSortControls() {
    // makes outer wrapper div
    const sortContainer = document.createElement("div");
    sortContainer.classList.add(
      "sortContainer",
      "flex",
      "space-x-2",
      "items-center",
      "mt-4"
    );
    this.container.appendChild(sortContainer);
    // adds sort icon
    const icon = makeIcon("sort");
    icon.title = "Sort";
    icon.classList.add("text-grey-200");
    sortContainer.appendChild(icon);

    // adds all dropdown items
    let dropdownOptions: BasicDropdownOption[] = [];

    this.definition.columns
      .filter((column) => column.sortType)
      .forEach((column) => {
        dropdownOptions.push(this.addSortOption(column, false));
        dropdownOptions.push(this.addSortOption(column, true));
      });

    const defaultOption =
      this.definition.defaultSort.columnTitle +
      " - " +
      this.getSortDescriptor(
        this.definition.defaultSort.type,
        this.definition.defaultSort.descending
      );
    sortContainer.appendChild(makeDropdownMenu(dropdownOptions, defaultOption));
  }

  private addSortOption(
    column: ColumnDefinition<ParentType, ChildType>,
    descending: boolean
  ) {
    const label =
      column.title +
      " - " +
      this.getSortDescriptor(column.sortType, descending);

    return new BasicDropdownOption(label, () => {
      this.sortColumn = column.title;
      this.sortDescending = descending;
      this.reload();
    });
  }

  private getSortDescriptor(
    sortType: SortType | undefined,
    descending: boolean
  ) {
    switch (sortType) {
      case "date":
        return descending ? "Latest First" : "Latest Last";
      case "number":
        return descending ? "High to Low" : "Low to High";
      case "text":
        return descending ? "Z to A" : "A to Z";
      default:
        throw new Error(`Unhandled sort type: ${sortType}`);
    }
  }

  private getTable(): HTMLTableElement {
    const elements = this.container.getElementsByTagName("table");
    let table = null;
    if (elements.length === 1) {
      table = elements.item(0);
    }
    if (table === null) {
      throw new Error("Error trying to find table element");
    }
    return table;
  }

  private load(data?: ParentType[]) {
    const table = this.getTable();
    while (table.lastChild) {
      table.removeChild(table.lastChild);
    }
    this.addTableHead(table);
    this.addTableBody(table, data);
  }

  private addTableHead(table: HTMLTableElement) {
    const thead = table.createTHead();
    const row = thead.insertRow();
    this.definition.columns.forEach((column, i) => {
      addColumnHeader(row, column.title, i);
    });
  }

  private addTableBody(table: HTMLTableElement, data?: ParentType[]) {
    const tbody = table.createTBody();
    if (data) {
      data.forEach((parent) => {
        this.addDataRow(tbody, parent);
      });
    } else {
      this.addNoDataRow(tbody);
    }
  }

  private async reload() {
    this.showLoading(true);
    const response = await Rest.post(Rest.cases.query, {
      pageSize: 30,
      pageNumber: 1,
      sortColumn: this.sortColumn,
      descending: this.sortDescending,
    });
    if (!response.ok) {
      throw new Error(`Error reloading table: ${response.status}`);
    }
    const data = await response.json();
    this.load(data.items);
    this.showLoading(false);
  }

  private showLoading(loading: boolean) {
    // TODO: Disable all inputs, show indeterminate progress
  }

  private addDataRow(tbody: HTMLTableSectionElement, parent: ParentType) {
    // TODO: keep track of "hideIfEmpty" columns
    let children: ChildType[] = [];
    if (this.definition.getChildren) {
      children = this.definition.getChildren(parent);
    }
    // generate parent row, which includes the first child (if applicable)
    const tr = this.addBodyRow(tbody, parent);
    this.definition.columns.forEach((column, i) => {
      if (column.child) {
        if (children.length) {
          this.addChildCell(tr, column, children[0], parent, i);
        } else {
          const td = makeCell(tr, i);
          shadeElement(td, "na");
          td.appendChild(document.createTextNode("N/A"));
        }
      } else {
        this.addParentCell(tr, column, parent, children, i);
      }
    });
    if (children.length > 1) {
      // generate additional child rows
      children.forEach((child, i) => {
        if (i === 0) {
          // first child already added with parent
          return;
        }
        const tr = this.addBodyRow(tbody, parent);
        this.definition.columns.forEach((column) => {
          if (column.child) {
            this.addChildCell(tr, column, child, parent, i);
          }
        });
      });
    }
  }

  private addBodyRow(tbody: HTMLTableSectionElement, parent: ParentType) {
    const tr = tbody.insertRow();
    if (this.definition.getRowHighlight) {
      shadeElement(tr, this.definition.getRowHighlight(parent));
    }
    return tr;
  }

  private addNoDataRow(tbody: HTMLTableSectionElement) {
    const row = tbody.insertRow();
    const cell = makeCell(row, 0);
    cell.colSpan = this.definition.columns.length;
    cell.classList.add("bg-grey-100", "font-bold");
    cell.appendChild(document.createTextNode("NO DATA"));
  }

  private addParentCell(
    tr: HTMLTableRowElement,
    column: ColumnDefinition<ParentType, ChildType>,
    parent: ParentType,
    children: ChildType[],
    index: number
  ) {
    if (!column.addParentContents) {
      throw new Error(
        `Column "${column.title}" specified as parent, but doesn't define addParentContents`
      );
    }
    const td = makeCell(tr, index);
    if (column.getCellHighlight) {
      shadeElement(td, column.getCellHighlight(parent, null));
    }
    const fragment = document.createDocumentFragment();
    column.addParentContents(parent, fragment);
    td.appendChild(fragment);
    if (children.length > 1) {
      td.rowSpan = children.length;
    }
  }

  private addChildCell(
    tr: HTMLTableRowElement,
    column: ColumnDefinition<ParentType, ChildType>,
    child: ChildType,
    parent: ParentType,
    index: number
  ) {
    if (!column.addChildContents) {
      throw new Error(
        `Column "${column.title}" specified as child, but doesn't define getChildContents`
      );
    }
    const td = makeCell(tr, index);
    if (column.getCellHighlight) {
      shadeElement(td, column.getCellHighlight(parent, child));
    }
    const fragment = document.createDocumentFragment();
    column.addChildContents(child, parent, fragment);
    td.appendChild(fragment);
  }
}
