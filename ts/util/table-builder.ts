import * as Rest from "./rest-api";

import {
  addColumnHeader,
  styleEmptyColumn,
  styleNoDataRow,
} from "./html-utils";
export interface ColumnDefinition<ParentType, ChildType> {
  title: string;
  child?: boolean;
  addParentContents?: (object: ParentType, fragment: DocumentFragment) => void;
  addChildContents?: (object: ChildType, fragment: DocumentFragment) => void;
}

export interface TableDefinition<ParentType, ChildType> {
  queryUrl: string;
  getChildren?: (parent: ParentType) => ChildType[];
  columns: ColumnDefinition<ParentType, ChildType>[];
}

export class TableBuilder<ParentType, ChildType> {
  definition: TableDefinition<ParentType, ChildType>;
  container: HTMLElement;

  constructor(
    definition: TableDefinition<ParentType, ChildType>,
    containerId: string
  ) {
    this.definition = definition;
    const container = document.getElementById(containerId);
    if (container === null) {
      throw Error(`Container ID "${containerId}" not found on page`);
    }
    this.container = container;
  }

  public build() {
    // TODO: add filtering controls
    // TODO: add paging controls
    const table = document.createElement("table");
    // set global default styling settings
    table.className =
      "border-spacing-0 w-full text-14 font-medium font-inter border-separate rounded-lg border-grey-200 border-2 overflow-hidden";
    this.container.appendChild(table);
    this.load();
    // TODO: add action buttons
    this.reload();
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
    row.className = "text-left text-align-top";
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
    const tr = tbody.insertRow();
    tr.className = "text-left align-text-top";
    this.definition.columns.forEach((column, i) => {
      if (column.child) {
        if (children.length) {
          this.addChildCell(tr, column, children[0], i);
        } else {
          const td = tr.insertCell();
          styleEmptyColumn(td);
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
        const tr = tbody.insertRow();
        tr.className = "text-left align-text-top";
        this.definition.columns.forEach((column) => {
          if (column.child) {
            this.addChildCell(tr, column, child, i);
          }
        });
      });
    }
  }

  private addNoDataRow(tbody: HTMLTableSectionElement) {
    const row = tbody.insertRow();
    const cell = row.insertCell();
    cell.colSpan = this.definition.columns.length;
    styleNoDataRow(cell);
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
    const td = tr.insertCell();
    td.className = "p-4 border-grey-200 border-t-2";
    if (index > 0) {
      td.classList.add("border-l-2");
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
    index: number
  ) {
    if (!column.addChildContents) {
      throw new Error(
        `Column "${column.title}" specified as child, but doesn't define getChildContents`
      );
    }
    const td = tr.insertCell();
    td.className = "p-4 border-grey-200 border-t-2";
    if (index > 0) {
      td.classList.add("border-l-2");
    }
    const fragment = document.createDocumentFragment();
    column.addChildContents(child, fragment);
    td.appendChild(fragment);
  }
}
