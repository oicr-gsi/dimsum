import * as Rest from "./rest-api";
import { Dropdown, DropdownOption, BasicDropdownOption } from "./dropdown";

import {
  makeCell,
  addColumnHeader,
  addIconButton,
  CellStatus,
  makeIcon,
  shadeElement,
} from "./html-utils";
import { TextInput } from "./text-input";

type SortType = "number" | "text" | "date";
type FilterType = "text" | "dropdown";

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

export interface FilterDefinition {
  title: string; // user friendly label
  key: string; // internal use
  type: FilterType; // either text or dropdown
  values?: string[]; // required for dropdown filters
}
export interface TableDefinition<ParentType, ChildType> {
  queryUrl: string;
  defaultSort: SortDefinition;
  filters?: FilterDefinition[];
  getChildren?: (parent: ParentType) => ChildType[];
  columns: ColumnDefinition<ParentType, ChildType>[];
  getRowHighlight?: (object: ParentType) => CellStatus | null;
}
class AcceptedFilter {
  element: HTMLElement;
  key: string;
  value: string;
  valid: boolean = true;

  constructor(
    title: string,
    key: string,
    value: string,
    container: HTMLElement,
    onRemove: () => {}
  ) {
    this.key = key;
    this.value = value;
    this.element = document.createElement("span");
    this.element.className =
      "font-inter font-medium text-12 text-black bg-grey-100 px-2 py-1 rounded-md cursor-default inline-block mr-2";
    this.element.innerHTML = `${title}: ${value}`;

    const destroyFilterIcon = makeIcon("xmark");
    destroyFilterIcon.classList.add(
      "text-black",
      "cursor-pointer",
      "ml-2",
      "hover:text-green-200"
    );
    destroyFilterIcon.onclick = () => {
      this.valid = false;
      this.element.remove();
      onRemove();
    };
    this.element.appendChild(destroyFilterIcon);

    container.appendChild(this.element);
  }
}

export class TableBuilder<ParentType, ChildType> {
  definition: TableDefinition<ParentType, ChildType>;
  container: HTMLElement;
  table?: HTMLTableElement;
  pageDescription?: HTMLSpanElement;
  pageLeftButton?: HTMLButtonElement;
  pageRightButton?: HTMLButtonElement;

  sortColumn: string;
  sortDescending: boolean;
  pageSize: number = 30;
  pageNumber: number = 1;
  totalCount: number = 0;
  filteredCount: number = 0;
  acceptedFilters: AcceptedFilter[] = [];

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
    const topControlsContainer = document.createElement("div");
    topControlsContainer.className = "flex mt-4 items-top space-x-2";
    this.addSortControls(topControlsContainer);
    this.addFilterControls(topControlsContainer);
    this.addPagingControls(topControlsContainer);
    this.container.appendChild(topControlsContainer);

    const tableContainer = document.createElement("div");
    tableContainer.className = "mt-4 overflow-auto";
    this.table = document.createElement("table");
    // set global default styling settings
    this.table.classList.add(
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

    tableContainer.appendChild(this.table);
    this.container.appendChild(tableContainer);
    this.load();
    // TODO: add action buttons
    this.reload();
  }

  private addSortControls(container: HTMLElement) {
    const sortContainer = document.createElement("div");
    sortContainer.classList.add("flex-none", "space-x-2");
    container.appendChild(sortContainer);

    const icon = makeIcon("sort");
    icon.title = "Sort";
    icon.classList.add("text-black");
    sortContainer.appendChild(icon);

    // adds all dropdown items
    let dropdownOptions: DropdownOption[] = [];
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
    const sortDropdown = new Dropdown(
      dropdownOptions,
      true,
      undefined,
      defaultOption
    );
    sortContainer.appendChild(sortDropdown.getTag());
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

  private addFilterControls(container: HTMLElement) {
    const filterContainer = document.createElement("div");
    const labelContainer = document.createElement("div");
    labelContainer.innerHTML = " ";
    filterContainer.classList.add("flex-auto", "items-center");
    labelContainer.className = "flex-none items-center inline-block";
    if (!this.definition.filters) {
      // no filters for this table
      return;
    }

    const filterIcon = makeIcon("filter");
    filterIcon.classList.add("text-black", "mr-2");
    filterContainer.appendChild(filterIcon);

    let filterOptions: DropdownOption[] = [];
    const reload = () => this.reload();
    this.definition.filters.forEach((filter) => {
      switch (filter.type) {
        case "dropdown":
          filterOptions.push(
            this.makeDropdownFilter(filter, labelContainer, reload)
          );
          break;
        case "text":
          filterOptions.push(
            this.makeTextInputFilter(filter, labelContainer, reload)
          );
          break;
        default:
          throw new Error(`Unhandled filter type: ${filter.type}`);
      }
    });

    const addFilterDropdown = new Dropdown(
      filterOptions,
      false,
      undefined,
      "+ filter"
    );
    filterContainer.append(labelContainer);
    filterContainer.appendChild(addFilterDropdown.getTag());
    container.appendChild(filterContainer);
  }

  private makeDropdownFilter(
    filter: FilterDefinition,
    labelContainer: HTMLElement,
    reload: () => {}
  ) {
    if (!filter.values || !filter.values.length) {
      throw new Error(`Dropdown filter ${filter.key} has no dropdown options`);
    }
    // create all submenu options
    const filterSuboptions = filter.values.map(
      (value) =>
        new BasicDropdownOption(value, (dropdown: Dropdown) => {
          dropdown.getTag().remove();
          const filterLabel = new AcceptedFilter(
            filter.title,
            filter.key,
            value,
            labelContainer,
            reload
          );
          // add filter to the menu bar
          labelContainer.appendChild(filterLabel.element);
          this.acceptedFilters.push(filterLabel);
          reload();
        })
    );
    // add filter (& its submenu) to the parent filter menu

    return new BasicDropdownOption(filter.title, () => {
      const filterSuboptionsDropdown = new Dropdown(
        filterSuboptions,
        true,
        filter.title
      );
      labelContainer.appendChild(filterSuboptionsDropdown.getTag());
    });
  }

  private makeTextInputFilter(
    filter: FilterDefinition,
    labelContainer: HTMLElement,
    reload: () => {}
  ) {
    const onClose = (textInput: TextInput) => {
      const filterLabel = new AcceptedFilter(
        filter.title,
        filter.key,
        textInput.getValue(),
        labelContainer,
        reload
      );
      textInput.getTag().remove();
      labelContainer.appendChild(filterLabel.element);
      this.acceptedFilters.push(filterLabel);
      reload();
    };
    return new BasicDropdownOption(filter.title, () => {
      const filterTextInput = new TextInput(filter.title, onClose);
      labelContainer.appendChild(filterTextInput.getTag());
    });
  }

  private addPagingControls(container: HTMLElement) {
    const pagingContainer = document.createElement("div");
    pagingContainer.classList.add("flex-none", "space-x-2");
    container.appendChild(pagingContainer);

    const pageSizeOptions = [10, 30, 50, 75, 100, 250, 500, 1000].map(
      (pageSize) =>
        new BasicDropdownOption(pageSize.toString(), () => {
          const topItemNumber = (this.pageNumber - 1) * this.pageSize + 1;
          this.pageSize = pageSize;
          // keep current top item in view
          this.pageNumber = Math.floor(topItemNumber / pageSize) + 1;
          this.reload();
        })
    );
    const pageSizeSelectDropdown = new Dropdown(
      pageSizeOptions,
      true,
      "Items per page",
      this.pageSize.toString()
    );
    pagingContainer.appendChild(pageSizeSelectDropdown.getTag());

    this.pageDescription = document.createElement("span");
    this.pageDescription.className =
      "font-inter font-medium text-black text-12";
    pagingContainer.appendChild(this.pageDescription);

    this.pageLeftButton = addIconButton(pagingContainer, "angle-left");
    this.pageLeftButton.disabled = true;
    this.pageLeftButton.onclick = (event) => {
      this.pageNumber--;
      this.reload();
    };
    this.pageRightButton = addIconButton(pagingContainer, "angle-right");
    this.pageRightButton.disabled = true;
    this.pageRightButton.onclick = (event) => {
      this.pageNumber++;
      this.reload();
    };
  }

  private makePageSizeOption(pageSize: number) {
    return new BasicDropdownOption(pageSize.toString(), () => {
      this.pageSize = pageSize;
      this.pageNumber = 1; // TODO: calculate current page to include previous top item?
      this.reload();
    });
  }

  private load(data?: ParentType[]) {
    const table = getElement(this.table);
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
    this.showLoading();
    this.acceptedFilters = this.acceptedFilters.filter(
      (filter) => filter.valid
    );
    const response = await Rest.post(Rest.cases.query, {
      pageSize: this.pageSize,
      pageNumber: this.pageNumber,
      filters: this.acceptedFilters.map((filter) => {
        return { key: filter.key, value: filter.value };
      }),
      sortColumn: this.sortColumn,
      descending: this.sortDescending,
    });
    if (!response.ok) {
      throw new Error(`Error reloading table: ${response.status}`);
    }
    const data = await response.json();
    this.load(data.items);
    this.showLoaded(data);
  }

  private showLoading() {
    // TODO: Disable all inputs, show indeterminate progress
    getElement(this.pageLeftButton).disabled = true;
    getElement(this.pageRightButton).disabled = true;
  }

  private showLoaded(data: any) {
    if (this.pageNumber > 1) {
      getElement(this.pageLeftButton).disabled = false;
    }
    if (data.filteredCount > this.pageSize * this.pageNumber) {
      getElement(this.pageRightButton).disabled = false;
    }
    const pageStart = this.pageSize * (this.pageNumber - 1) + 1;
    const pageEnd = Math.min(
      this.pageSize * this.pageNumber,
      data.filteredCount
    );
    // TODO: if filtered, show totalCount too
    getElement(
      this.pageDescription
    ).textContent = `${pageStart}-${pageEnd} of ${data.filteredCount}`;
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

function getElement<Type>(element?: Type) {
  if (element) {
    return element;
  } else {
    throw new Error("Missing element");
  }
}
