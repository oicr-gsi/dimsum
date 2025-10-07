import { Dropdown, DropdownOption, BasicDropdownOption } from "./dropdown";
import {
  makeCell,
  addColumnHeader,
  addIconButton,
  CellStatus,
  makeIcon,
  shadeElement,
  addActionButton,
} from "../util/html-utils";
import { toggleLegend } from "./legend";
import { post } from "../util/requests";
import { Pair } from "../util/pair";
import { TextInput } from "./text-input";
import { showErrorDialog } from "./dialog";
import { DateInput } from "./date-input";

type SortType = "number" | "text" | "date" | "custom";
type FilterType = "text" | "dropdown" | "date";

export interface ColumnDefinition<ParentType, ChildType> {
  title: string;
  headingClass?: string; // class to add to heading cell
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
  type: SortType;
}

export interface Sort extends SortDefinition {
  descending: boolean;
}

export interface FilterDefinition {
  title: string; // user friendly label
  key: string; // internal use
  type: FilterType; // either text or dropdown or date
  values?: string[]; // required for dropdown filters
  autocompleteUrl?: string; // required for text filters w/autocomplete
}

export interface StaticAction {
  title: string;
  handler: (
    filters: { key: string; value: string }[],
    baseFilter?: { key: string; value: string }
  ) => void;
}

export interface BulkAction<ParentType> {
  title: string;
  handler: (items: ParentType[]) => void;
}

export interface TableDefinition<ParentType, ChildType> {
  queryUrl?: string; // required if table is loaded via AJAX
  defaultSort?: Sort; // required if table is loaded via AJAX and page controls are NOT disabled
  nonColumnSorting?: SortDefinition[];
  filters?: FilterDefinition[];
  getSubheading?: (parent: ParentType) => string | null;
  getChildren?: (parent: ParentType) => ChildType[];
  generateColumns: (
    data?: ParentType[]
  ) => ColumnDefinition<ParentType, ChildType>[];
  getRowHighlight?: (object: ParentType) => CellStatus | null;
  staticActions?: StaticAction[];
  bulkActions?: BulkAction<ParentType>[];
  disablePageControls?: boolean;
  // when a parent object has no children, noChildrenWarning is displayed with warning highlight.
  // if noChildrenWarning is not provided, 'N/A' is displayed instead
  noChildrenWarning?: string;
  parentHeaders?: Array<{ title: string; colspan: number }>;
}

class AcceptedFilter {
  element: HTMLElement;
  key: string;
  value: string;
  valid: boolean = true;

  constructor(title: string, key: string, value: string, onRemove: () => void) {
    this.key = key;
    this.value = value;
    this.element = document.createElement("span");
    this.element.className =
      "font-inter font-medium text-12 text-black bg-grey-100 px-2 py-1 rounded-md cursor-default inline-block";
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
  }
}

export class TableBuilder<ParentType, ChildType> {
  definition: TableDefinition<ParentType, ChildType>;
  container: HTMLElement;
  table?: HTMLTableElement;
  thead?: HTMLTableSectionElement;
  pageDescription?: HTMLSpanElement;
  pageLeftButtonTop?: HTMLButtonElement;
  pageRightButtonTop?: HTMLButtonElement;
  pageLeftButtonBottom?: HTMLButtonElement;
  pageRightButtonBottom?: HTMLButtonElement;

  columns: ColumnDefinition<ParentType, ChildType>[];
  sortColumn?: string;
  sortDescending: boolean;
  pageSize: number = 50;
  pageNumber: number = 1;
  totalCount: number = 0;
  filteredCount: number = 0;
  baseFilterKey: string | null;
  baseFilterValue: string | null;
  acceptedFilters: AcceptedFilter[] = [];
  allItems: ParentType[] = [];
  selectedItems: Set<ParentType> = new Set<ParentType>();
  selectAllCheckbox?: HTMLInputElement;
  topSelectionCountElement?: HTMLElement;
  bottomSelectionCountElement?: HTMLElement;
  onFilterChange?: (key: string, value: string, add: boolean) => void;
  onLoad?: (data: ParentType[]) => boolean; // return true to remove handler after running
  lastClickedRowIndex: number | null = null;
  private isShiftKeyPressed: boolean = false;

  constructor(
    definition: TableDefinition<ParentType, ChildType>,
    containerId: string,
    filterParams?: Array<Pair<string, string>>,
    onFilterChange?: (key: string, value: string, add: boolean) => void,
    onLoad?: (data: ParentType[]) => boolean
  ) {
    this.definition = definition;
    if (definition.defaultSort) {
      this.sortColumn = definition.defaultSort.columnTitle;
    }
    this.sortDescending = definition.defaultSort
      ? definition.defaultSort.descending
      : false;
    const container = document.getElementById(containerId);
    if (container === null) {
      throw Error(`Container ID "${containerId}" not found on page`);
    }
    this.baseFilterKey = container.getAttribute("data-detail-type");
    this.baseFilterValue = container.getAttribute("data-detail-value");

    if (filterParams) {
      // create accepted filter if there is a matching key in table definition
      filterParams.forEach((p) => {
        this.addAcceptedFilter(p.key, p.value);
      });
    }
    this.onFilterChange = onFilterChange;
    this.onLoad = onLoad;
    this.container = container;
    this.columns = definition.generateColumns();
    document.addEventListener("keydown", (event: KeyboardEvent) => {
      if (event.key === "Shift") {
        this.isShiftKeyPressed = true;
      }
    });

    document.addEventListener("keyup", (event: KeyboardEvent) => {
      if (event.key === "Shift") {
        this.isShiftKeyPressed = false;
      }
    });
  }

  public applyFilter(key: string, value: string, add: boolean) {
    this.updateAcceptedFilter(key, value, add);
    this.reload(true);
  }

  /**
   * Removes any filters with the same keys as provided and adds new filters as provided
   *
   * @param filters new filters to add
   */
  public replaceFilters(filters: Array<Pair<string, string>>) {
    filters.forEach((filter) => {
      this.acceptedFilters = this.acceptedFilters.filter(
        (accepted) => accepted.key !== filter.key
      );
      if (filter.value) {
        this.addAcceptedFilter(filter.key, filter.value);
      }
    });
    this.reload(true);
  }

  private addAcceptedFilter(key: string, value: string) {
    this.definition.filters?.forEach((f) => {
      if (f.key === key) {
        // filter key is valid, create a new accepted filter
        const onRemove = () => {
          if (this.onFilterChange) this.onFilterChange(key, value, false);
          this.reload();
        };
        this.acceptedFilters.push(
          new AcceptedFilter(f.title, key, value, onRemove)
        );
      }
    });
  }

  private updateAcceptedFilter(key: string, value: string, add: boolean) {
    if (add) {
      this.addAcceptedFilter(key, value);
    } else {
      this.acceptedFilters = this.acceptedFilters.filter(
        (filter) => filter.key !== key || filter.value !== value
      );
    }
  }

  public clear() {
    this.container.innerHTML = "";
  }

  /**
   * Activates the table
   *
   * @param data if provided, this static data is added to the table; otherwise the
   * TableDefinition's queryUrl is used to fetch the data
   */
  public build(data?: ParentType[]) {
    if (!data && !this.definition.queryUrl) {
      throw new Error("Query url is required for loading table via AJAX");
    }
    const topControlsContainer = document.createElement("div");
    topControlsContainer.className =
      "flex justify-end mt-4 items-top space-x-2";

    if (!this.definition.disablePageControls) {
      this.addSortControls(topControlsContainer);
      this.addFilterControls(topControlsContainer);
    }
    this.topSelectionCountElement = document.createElement("span");
    this.addSelectionCount(topControlsContainer, this.topSelectionCountElement);
    if (this.definition.bulkActions || this.definition.staticActions) {
      this.addActionButtons(topControlsContainer);
    }
    if (!this.definition.disablePageControls) {
      this.addPagingControls(topControlsContainer);
    }
    if (topControlsContainer.children.length > 0) {
      this.container.appendChild(topControlsContainer);
    }

    const tableContainer = document.createElement("div");
    tableContainer.className = "mt-4 overflow-auto";
    this.table = document.createElement("table");
    // set global default styling settings
    this.table.classList.add(
      "data-table",
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

    const bottomControlsContainer = document.createElement("div");
    this.bottomSelectionCountElement = document.createElement("span");
    this.addSelectionCount(
      bottomControlsContainer,
      this.bottomSelectionCountElement
    );
    if (this.definition.bulkActions || this.definition.staticActions) {
      bottomControlsContainer.className =
        "flex justify-end mt-4 items-top space-x-2";
      this.addActionButtons(bottomControlsContainer);
    }
    if (!this.definition.disablePageControls) {
      this.addBottomPageControls(bottomControlsContainer);
    }
    if (bottomControlsContainer.children.length) {
      this.container.appendChild(bottomControlsContainer);
    }

    this.load(data);
    this.setupScrollListener();
    if (data) {
      this.triggerOnLoad(data);
    } else {
      this.reload();
    }
    return this;
  }

  private triggerOnLoad(data: ParentType[]) {
    if (this.onLoad) {
      const remove = this.onLoad(data);
      if (remove) {
        this.onLoad = undefined;
      }
    }
  }

  private addSelectionCount(container: HTMLElement, countElement: HTMLElement) {
    countElement.className =
      "selection-count font-inter font-medium text-12 text-black py-1";
    countElement.style.display = "none";
    container.appendChild(countElement);
  }

  private addActionButtons(container: HTMLElement) {
    if (this.definition.bulkActions) {
      this.definition.bulkActions.forEach((action) => {
        addActionButton(container, action.title, () => {
          if (!this.selectedItems.size) {
            showErrorDialog("No items selected");
            return;
          }
          action.handler(Array.from(this.selectedItems));
        });
      });
    }
    if (this.definition.staticActions) {
      this.definition.staticActions.forEach((action) => {
        addActionButton(container, action.title, () => {
          const baseFilter =
            this.baseFilterKey && this.baseFilterValue
              ? { key: this.baseFilterKey, value: this.baseFilterValue }
              : undefined;
          action.handler(this.acceptedFilters, baseFilter);
        });
      });
    }
  }

  private addSortControls(container: HTMLElement) {
    const sortContainer = document.createElement("div");
    sortContainer.classList.add("flex-none", "space-x-2");
    container.appendChild(sortContainer);
    // add sort icon
    const icon = makeIcon("sort");
    icon.title = "Sort";
    icon.classList.add("text-black");
    sortContainer.appendChild(icon);

    // adds all dropdown items
    let dropdownOptions: DropdownOption[] = [];
    this.columns
      .filter((column) => column.sortType)
      .forEach((column) => {
        dropdownOptions.push(
          this.addSortOption(column.title, column.sortType, false)
        );
        dropdownOptions.push(
          this.addSortOption(column.title, column.sortType, true)
        );
      });
    if (this.definition.nonColumnSorting) {
      for (let nonColumnSort of this.definition.nonColumnSorting) {
        dropdownOptions.push(
          this.addSortOption(
            nonColumnSort.columnTitle,
            nonColumnSort.type,
            false
          )
        );
        dropdownOptions.push(
          this.addSortOption(
            nonColumnSort.columnTitle,
            nonColumnSort.type,
            true
          )
        );
      }
    }

    const defaultOption = this.definition.defaultSort
      ? this.definition.defaultSort.columnTitle +
        " - " +
        this.getSortDescriptor(
          this.definition.defaultSort.type,
          this.definition.defaultSort.descending
        )
      : "undefined";
    const sortDropdown = new Dropdown(
      dropdownOptions,
      true,
      undefined,
      defaultOption
    );
    sortContainer.appendChild(sortDropdown.getContainerTag());
  }

  private addSortOption(
    title: string,
    sortType: SortType | undefined,
    descending: boolean
  ) {
    const label = title + " - " + this.getSortDescriptor(sortType, descending);

    return new BasicDropdownOption(label, () => {
      this.sortColumn = title;
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
      case "custom":
        return descending ? "Descending" : "Ascending";
      default:
        throw new Error(`Unhandled sort type: ${sortType}`);
    }
  }

  private addFilterControls(container: HTMLElement) {
    const filterContainer = document.createElement("div");
    filterContainer.classList.add("flex-auto", "items-center", "space-x-2");
    container.appendChild(filterContainer);
    if (!this.definition.filters) {
      // no filters for this table
      return;
    }
    // add filter icon
    const filterIcon = makeIcon("filter");
    filterIcon.title = "Filter";
    filterIcon.classList.add("text-black");
    filterContainer.appendChild(filterIcon);

    // add pre-table build filters
    for (var filter of this.acceptedFilters) {
      filterContainer.appendChild(filter.element);
    }

    let filterOptions: DropdownOption[] = [];
    this.definition.filters.forEach((filter) => {
      switch (filter.type) {
        case "dropdown":
          filterOptions.push(this.makeDropdownFilter(filter, filterContainer));
          break;
        case "text":
          filterOptions.push(this.makeTextInputFilter(filter, filterContainer));
          break;
        case "date":
          filterOptions.push(this.makeDateInputFilter(filter, filterContainer));
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
    filterContainer.appendChild(addFilterDropdown.getContainerTag());
  }

  private makeDateInputFilter(
    filter: FilterDefinition,
    filterContainer: HTMLElement
  ): DropdownOption {
    return new BasicDropdownOption(filter.title, () => {
      const dateInput = new DateInput(filter.title, (value: string) => {
        if (value) {
          const onRemove = () => {
            if (this.onFilterChange)
              this.onFilterChange(filter.key, value, false);
            this.reload(true);
          };
          const filterLabel = new AcceptedFilter(
            filter.title,
            filter.key,
            value,
            onRemove
          );
          filterContainer.insertBefore(
            filterLabel.element,
            filterContainer.lastChild
          );
          this.acceptedFilters.push(filterLabel);
          // update params
          if (this.onFilterChange) {
            this.onFilterChange(filter.key, value, true);
          }
          this.reload(true); // apply new filter
        }
      });
      filterContainer.insertBefore(
        dateInput.getElement(),
        filterContainer.lastChild
      );
    });
  }

  private makeDropdownFilter(
    filter: FilterDefinition,
    filterContainer: HTMLElement
  ) {
    if (!filter.values || !filter.values.length) {
      throw new Error(`Dropdown filter ${filter.key} has no dropdown options`);
    }
    // create all submenu options
    const filterSuboptions = filter.values.map(
      (value) =>
        new BasicDropdownOption(value, (dropdown: Dropdown) => {
          dropdown.getContainerTag().remove();
          const onRemove = () => {
            if (this.onFilterChange)
              this.onFilterChange(filter.key, value, false);
            this.reload(true);
          };
          const filterLabel = new AcceptedFilter(
            filter.title,
            filter.key,
            value,
            onRemove
          );
          // add filter label to the menu bar
          filterContainer.insertBefore(
            filterLabel.element,
            filterContainer.lastChild
          );
          this.acceptedFilters.push(filterLabel);
          // update params
          if (this.onFilterChange) {
            this.onFilterChange(filter.key, value, true);
          }
          this.reload(true); // apply new filter
        })
    );
    // add filter (& its submenu) to the parent filter menu
    return new BasicDropdownOption(filter.title, () => {
      const filterSuboptionsDropdown = new Dropdown(
        filterSuboptions,
        true,
        filter.title,
        undefined,
        true
      );
      filterContainer.insertBefore(
        filterSuboptionsDropdown.getContainerTag(),
        filterContainer.lastChild
      );
    });
  }

  private makeTextInputFilter(
    filter: FilterDefinition,
    filterContainer: HTMLElement
  ) {
    const onClose = (values: string[]) => {
      values.forEach((value) => {
        const onRemove = () => {
          if (this.onFilterChange) {
            this.onFilterChange(filter.key, value, false);
          }
          this.reload(true);
        };
        const filterLabel = new AcceptedFilter(
          filter.title,
          filter.key,
          value,
          onRemove
        );
        filterContainer.insertBefore(
          filterLabel.element,
          filterContainer.lastChild
        );
        this.acceptedFilters.push(filterLabel);
        // update params
        if (this.onFilterChange) {
          this.onFilterChange(filter.key, value, true);
        }
      });
      this.reload(true); // apply new filter(s)
    };
    return new BasicDropdownOption(filter.title, () => {
      if (!filter.autocompleteUrl) {
        throw new Error(
          `Text input filter ${filter.title} has no autocomplete rest URL`
        );
      }
      const filterTextInput = new TextInput(
        filter.title,
        onClose,
        filter.autocompleteUrl
      );
      filterContainer.insertBefore(
        filterTextInput.getContainerTag(),
        filterContainer.lastChild
      );
    });
  }

  private addPagingControls(container: HTMLElement) {
    const pagingContainer = document.createElement("div");
    pagingContainer.classList.add("flex-none", "space-x-2");
    container.appendChild(pagingContainer);

    const pagingIcon = makeIcon("book-open");
    pagingIcon.title = "Page";
    pagingIcon.classList.add("text-black");
    pagingContainer.appendChild(pagingIcon);

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
    pagingContainer.appendChild(pageSizeSelectDropdown.getContainerTag());

    this.pageDescription = document.createElement("span");
    this.pageDescription.className =
      "font-inter font-medium text-black text-12";
    pagingContainer.appendChild(this.pageDescription);

    this.pageLeftButtonTop = this.addPageButton(pagingContainer, false);
    this.pageRightButtonTop = this.addPageButton(pagingContainer, true);
  }

  private addPageButton(container: HTMLElement, forward: boolean) {
    const button = addIconButton(
      container,
      forward ? "angle-right" : "angle-left"
    );
    button.disabled = true;
    const step = forward ? 1 : -1;
    button.onclick = (event) => {
      this.pageNumber += step;
      this.reload();
      if (this.container.getBoundingClientRect().y < 0) {
        this.container.scrollIntoView();
      }
    };
    return button;
  }

  private addBottomPageControls(container: HTMLElement) {
    this.pageLeftButtonBottom = this.addPageButton(container, false);
    this.pageRightButtonBottom = this.addPageButton(container, true);
  }

  private load(data?: ParentType[]) {
    this.columns = this.definition.generateColumns(data);
    const table = getElement(this.table);
    table.replaceChildren();
    this.addTableHead(table);
    this.addTableBody(table, data);
    this.allItems = data || [];
    this.selectedItems = new Set<ParentType>();
    this.updateSelectionCount();
  }

  private addTableHead(table: HTMLTableElement) {
    this.thead = table.createTHead();
    this.thead.className = "relative";
    const addHeaderRow = (isParent: boolean) => {
      if (!this.thead) {
        throw new Error("Table head (thead) is not defined");
      }
      const row = this.thead.insertRow();
      if (isParent && this.definition.bulkActions) {
        // add a blank header cell for alignment
        const th = document.createElement("th");
        th.className = "p-4 bg-grey-300";
        row.appendChild(th);
      }
      return row;
    };
    if (this.definition.parentHeaders) {
      // create the first row with parent headers
      const parentRow = addHeaderRow(true);
      this.definition.parentHeaders.forEach((parentHeader) => {
        addColumnHeader(
          parentRow,
          parentHeader.title,
          false,
          undefined,
          "bg-grey-300",
          parentHeader.colspan
        );
      });
    }
    // create child or single row headers
    const row = addHeaderRow(false);
    if (this.definition.bulkActions) {
      this.addSelectAllHeader(row);
    }
    this.columns.forEach((column, i) => {
      const isFirstColumn = i === 0 && !this.definition.bulkActions;
      const isChildHeader = !!this.definition.parentHeaders;
      const combinedClass = isChildHeader ? ["text-black"] : ["text-white"];
      if (column.headingClass) {
        combinedClass.push(column.headingClass);
      }
      const bgColor = isChildHeader ? "bg-grey-100" : "bg-grey-300";
      addColumnHeader(row, column.title, isFirstColumn, combinedClass, bgColor);
    });
  }

  private addSelectAllHeader(thead: HTMLTableRowElement) {
    const th = document.createElement("th");
    th.className =
      "p-4 text-white font-semibold bg-grey-300 text-left align-text-top";
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.onchange = (event) => {
      this.toggleSelectAll(checkbox.checked);
    };
    this.selectAllCheckbox = checkbox;
    th.appendChild(checkbox);
    thead.appendChild(th);
  }

  private toggleSelectAll(select: boolean) {
    if (select) {
      this.allItems.forEach((item) => this.selectedItems.add(item));
    } else {
      this.selectedItems = new Set<ParentType>();
    }
    const rowSelects = this.container.getElementsByClassName("row-select");
    Array.prototype.forEach.call(
      rowSelects,
      (rowSelect) => (rowSelect.checked = select)
    );
    this.updateSelectionCount();
  }

  private addTableBody(table: HTMLTableElement, data?: ParentType[]) {
    if (data && data.length) {
      let previousSubheading: string | null = null;
      data.forEach((parent, index) => {
        if (this.definition.getSubheading) {
          const currentSubheading = this.definition.getSubheading(parent);
          if (currentSubheading !== previousSubheading) {
            this.addSubheadingRow(table, currentSubheading);
            previousSubheading = currentSubheading;
          }
        }
        this.addDataRow(table, parent, index);
      });
    } else {
      this.addNoDataRow(table);
    }
  }

  private setupScrollListener() {
    document.addEventListener("scroll", (event) => {
      const table = getElement(this.table);
      const thead = getElement(this.thead);
      const tableRect = table.getBoundingClientRect();
      if (tableRect.y >= 0) {
        thead.style.top = "0";
        return;
      }
      const headRect = thead.getBoundingClientRect();
      if (headRect.height - 2 > tableRect.bottom) {
        thead.style.top = tableRect.height - headRect.height + "px";
      } else {
        thead.style.top = tableRect.top * -1 - 2 + "px";
      }
    });
  }

  private async reload(resetPage?: boolean) {
    if (!this.definition.queryUrl) {
      throw new Error("Cannot reload without query URL");
    }
    this.showLoading();
    if (resetPage) {
      this.pageNumber = 1;
    }
    this.acceptedFilters = this.acceptedFilters.filter(
      (filter) => filter.valid
    );
    try {
      const data = await post(this.definition.queryUrl, {
        pageSize: this.pageSize,
        pageNumber: this.pageNumber,
        baseFilter: this.baseFilterKey
          ? { key: this.baseFilterKey, value: this.baseFilterValue }
          : null,
        filters: this.acceptedFilters.map((filter) => {
          return { key: filter.key, value: filter.value };
        }),
        sortColumn: this.sortColumn,
        descending: this.sortDescending,
      });
      this.load(data.items);
      this.showLoaded(data);
      this.triggerOnLoad(data.items);
    } catch (reason) {
      showErrorDialog("Error reloading table - " + reason);
    }
  }

  private showLoading() {
    // TODO: Disable all inputs, show indeterminate progress
    if (!this.definition.disablePageControls) {
      [
        this.pageLeftButtonTop,
        this.pageRightButtonTop,
        this.pageLeftButtonBottom,
        this.pageRightButtonBottom,
      ].forEach((button) => (getElement(button).disabled = true));
    }
  }

  private showLoaded(data: any) {
    if (!this.definition.disablePageControls) {
      if (this.pageNumber > 1) {
        getElement(this.pageLeftButtonTop).disabled = false;
        getElement(this.pageLeftButtonBottom).disabled = false;
      }
      if (data.filteredCount > this.pageSize * this.pageNumber) {
        getElement(this.pageRightButtonTop).disabled = false;
        getElement(this.pageRightButtonBottom).disabled = false;
      }
      const pageStart = this.pageSize * (this.pageNumber - 1) + 1;
      const pageEnd = Math.min(
        this.pageSize * this.pageNumber,
        data.filteredCount
      );
      let pageDescriptionText = `${pageStart}-${pageEnd} of ${data.filteredCount}`;
      if (data.filteredCount < data.totalCount) {
        pageDescriptionText += ` (filtered from ${data.totalCount})`;
      }
      getElement(this.pageDescription).textContent = pageDescriptionText;
    }
  }

  private addSubheadingRow(table: HTMLTableElement, text: string | null) {
    const tbody = table.createTBody();
    tbody.classList.add("keep-with-next");
    const row = tbody.insertRow();
    const th = row.insertCell();
    th.className =
      "p-3 border-grey-200 border-t-1 text-left align-text-top font-semibold bg-grey-100";
    th.colSpan = this.columns.length + (this.definition.bulkActions ? 1 : 0);
    // Null heading should always be first and not display anything, but if it's later for some
    // reason, call it "Other"
    th.appendChild(document.createTextNode(text || "Other"));
  }

  private addDataRow(
    table: HTMLTableElement,
    parent: ParentType,
    rowIndex: number
  ) {
    let children: ChildType[] = [];
    if (this.definition.getChildren) {
      children = this.definition.getChildren(parent);
    }
    // generate parent row, which includes the first child (if applicable)
    const tbody = table.createTBody();
    tbody.classList.add("nobreak");
    const tr = this.addBodyRow(tbody, parent);
    if (this.definition.bulkActions) {
      this.addRowSelectCell(tr, parent, children, rowIndex);
    }
    this.columns.forEach((column, i) => {
      if (column.child) {
        if (children.length) {
          this.addChildCell(tr, column, children[0], parent, i);
        } else {
          const td = makeCell(tr, i == 0 && !this.definition.bulkActions);
          td.classList.add("font-bold");
          shadeElement(
            td,
            this.definition.noChildrenWarning ? "warning" : "na"
          );
          td.appendChild(
            document.createTextNode(this.definition.noChildrenWarning || "N/A")
          );
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
        this.columns.forEach((column) => {
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

  private addNoDataRow(table: HTMLTableElement) {
    const tbody = table.createTBody();
    const row = tbody.insertRow();
    const cell = makeCell(row, true);
    cell.colSpan = this.columns.length + (this.definition.bulkActions ? 1 : 0);
    cell.classList.add("bg-grey-100", "font-bold");
    cell.appendChild(document.createTextNode("NO DATA"));
  }

  private addRowSelectCell(
    tr: HTMLTableRowElement,
    item: ParentType,
    children: ChildType[],
    rowIndex: number
  ) {
    const td = makeCell(tr, true);
    const checkbox = document.createElement("input");
    checkbox.className = "row-select";
    checkbox.type = "checkbox";

    checkbox.onchange = (event) => {
      const currentRowIndex = rowIndex;

      if (this.isShiftKeyPressed && this.lastClickedRowIndex !== null) {
        this.toggleRange(
          this.lastClickedRowIndex,
          currentRowIndex,
          checkbox.checked
        );
      } else {
        if (checkbox.checked) {
          this.selectedItems.add(item);
        } else {
          if (this.selectAllCheckbox) {
            this.selectAllCheckbox.checked = false;
            this.selectedItems.delete(item);
          }
        }
      }
      this.lastClickedRowIndex = currentRowIndex;
      this.updateSelectionCount();
    };

    if (children.length > 1) {
      td.rowSpan = children.length;
    }
    td.appendChild(checkbox);
  }

  private toggleRange(
    startIndex: number,
    endIndex: number,
    isSelected: boolean
  ) {
    const [minIndex, maxIndex] = [
      Math.min(startIndex, endIndex),
      Math.max(startIndex, endIndex),
    ];

    const rowSelects = this.container.getElementsByClassName("row-select");
    for (let i = minIndex; i <= maxIndex; i++) {
      const rowSelect = rowSelects[i] as HTMLInputElement;
      if (rowSelect) {
        rowSelect.checked = isSelected;
        const item = this.allItems[i];
        if (isSelected) {
          this.selectedItems.add(item);
        } else {
          this.selectedItems.delete(item);
        }
      }
    }
    this.updateSelectionCount();
  }

  private updateSelectionCount() {
    const count = this.selectedItems.size;
    const itemText = count === 1 ? "item" : "items";

    if (this.topSelectionCountElement) {
      this.topSelectionCountElement.textContent = `Selected ${count} ${itemText}`;
      this.topSelectionCountElement.style.display =
        count > 0 ? "inline" : "none";
    }
    if (this.bottomSelectionCountElement) {
      this.bottomSelectionCountElement.textContent = `Selected ${count} ${itemText}`;
      this.bottomSelectionCountElement.style.display =
        count > 0 ? "inline" : "none";
    }
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
    const td = makeCell(tr, index == 0 && !this.definition.bulkActions);
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
    const td = makeCell(tr, index == 0 && !this.definition.bulkActions);
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

export const legendAction: StaticAction = {
  title: "Legend",
  handler: () => toggleLegend("qc"),
};
