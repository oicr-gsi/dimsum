export interface ColumnDefinition<ParentType, ChildType> {
  title: string;
  child?: boolean;
  addParentContents?: (object: ParentType, fragment: DocumentFragment) => void;
  addChildContents?: (object: ChildType, fragment: DocumentFragment) => void;
}

export interface TableDefinition<ParentType, ChildType> {
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

  public async build() {
    // TODO: add filtering controls
    // TODO: add paging controls
    const table = document.createElement("table");
    this.container.appendChild(table);
    this.load();
    // TODO: add action buttons
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

  // TODO: make private once TableBuilder controls fetching/reloading
  public load(data?: ParentType[]) {
    const table = this.getTable();
    while (table.lastChild) {
      table.removeChild(table.lastChild);
    }
    this.addTableHead(table);
    const tbody = table.createTBody();
    if (data) {
      data.forEach((parent) => {
        this.addDataRow(tbody, parent);
      });
    } else {
      this.addNoDataRow(tbody);
    }
  }

  private addTableHead(table: HTMLTableElement) {
    const thead = table.createTHead();
    const row = thead.insertRow();
    this.definition.columns.forEach((column) => {
      const th = document.createElement("th");
      const text = document.createTextNode(column.title);
      th.appendChild(text);
      row.appendChild(th);
    });
  }

  private addDataRow(tbody: HTMLTableSectionElement, parent: ParentType) {
    // TODO: keep track of "hideIfEmpty" columns
    let children: ChildType[] = [];
    if (this.definition.getChildren) {
      children = this.definition.getChildren(parent);
    }
    const tr = tbody.insertRow();
    this.definition.columns.forEach((column) => {
      if (column.child) {
        if (children.length) {
          this.addChildCell(tr, column, children[0]);
        }
      } else {
        this.addParentCell(tr, column, parent, children);
      }
    });
    if (children.length > 1) {
      // add additional child rows
      children.forEach((child, i) => {
        if (i === 0) {
          // first child already added with parent
          return;
        }
        const tr = tbody.insertRow();
        this.definition.columns.forEach((column) => {
          if (column.child) {
            this.addChildCell(tr, column, child);
          }
        });
      });
    }
  }

  private addNoDataRow(tbody: HTMLTableSectionElement) {
    const row = tbody.insertRow();
    const cell = row.insertCell();
    cell.colSpan = this.definition.columns.length;
    const text = document.createTextNode("No data");
    cell.appendChild(text);
  }

  private addParentCell(
    tr: HTMLTableRowElement,
    column: ColumnDefinition<ParentType, ChildType>,
    parent: ParentType,
    children: ChildType[]
  ) {
    if (!column.addParentContents) {
      throw new Error(
        `Column "${column.title}" specified as parent, but doesn't define addParentContents`
      );
    }
    const td = tr.insertCell();
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
    child: ChildType
  ) {
    if (!column.addChildContents) {
      throw new Error(
        `Column "${column.title}" specified as child, but doesn't define getChildContents`
      );
    }
    const td = tr.insertCell();
    const fragment = document.createDocumentFragment();
    column.addChildContents(child, fragment);
    td.appendChild(fragment);
  }
}
