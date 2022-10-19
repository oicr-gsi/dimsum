class Tab {
  tabContainer: HTMLButtonElement;
  tableContainerId: string;
  tableTitle: string;
  selected: boolean = false;
  onSelect: (tableContainerId: string) => void;
  constructor(
    tableContainerId: string,
    tableTitle: string,
    onSelect: (tableContainerId: string) => void,
    selected?: boolean
  ) {
    this.tableContainerId = tableContainerId;
    this.tableTitle = tableTitle;
    this.onSelect = onSelect;
    const button = document.createElement("button");
    button.className =
      "font-inter font-medium text-12 text-black bg-white px-2 py-1 rounded-md";
    button.textContent = tableTitle;
    button.onclick = () => {
      if (!this.selected) {
        this.selected = !this.selected;
        // selecting a table should remove all other tables from view and create this table
        const table = document.getElementById(tableContainerId);
        if (table === null) {
          throw Error(`Container ID "${tableContainerId}" not found on page`);
        }
        this.styleButton();
        onSelect(tableContainerId);
      }
    };
    this.styleButton();
    this.tabContainer = button;
  }

  // modifies the tab styling according to whether or not it has been selected
  public styleButton() {
    if (this.selected) {
      this.tabContainer.classList.replace("text-black", "text-green-200");
      this.tabContainer.classList.add(
        "ring-2",
        "ring-green-200",
        "ring-offset-1"
      );
    } else {
      this.tabContainer.classList.replace("text-green-200", "text-black");
      this.tabContainer.classList.remove(
        "ring-2",
        "ring-green-200",
        "ring-offset-1"
      );
      this.tabContainer.classList.add(
        "hover:ring-2",
        "ring-green-200",
        "ring-offset-1"
      );
    }
  }
}

export class TabBar {
  tableTitles: string[];
  tableContainerIds: string[];
  defaultTableId: string;
  container: HTMLElement;
  tabs: Tab[] = [];

  constructor(
    tableTitles: string[],
    tableContainerIds: string[],
    defaultTableId: string,
    containerId: string
  ) {
    const container = document.getElementById(containerId);
    if (container === null) {
      throw Error(`Container ID "${containerId}" not found on page`);
    }
    this.tableTitles = tableTitles;
    this.tableContainerIds = tableContainerIds;
    this.defaultTableId = defaultTableId;
    this.container = container;
  }
  public build() {
    const controlsContainer = document.createElement("span");
    controlsContainer.className =
      "flex-auto space-x-2 rounded-md px-2 py-2 bg-grey-100 inline-block";
    this.tableContainerIds.forEach((table, idx) => {
      const reload = (id: string) => this.reloadTables(id);
      const length = this.tabs.push(
        new Tab(
          table,
          this.tableTitles[idx],
          reload,
          table === this.defaultTableId ? true : false
        )
      );
      controlsContainer.append(this.tabs[length - 1].tabContainer);
    });
    this.container.append(controlsContainer);
    this.reloadTables(this.defaultTableId); // show the default table
  }

  // reload the shown tables
  public reloadTables(tableContainerId: string) {
    console.log("RELOADING TABLES . . .");
    this.tabs.forEach((tab) => {
      // not the selected tab, hide table and deselect
      if (tab.tableContainerId !== tableContainerId) {
        const table = document.getElementById(tab.tableContainerId);
        table?.classList.replace("visible", "hidden");
      } else {
        const table = document.getElementById(tableContainerId);
        table?.classList.replace("hidden", "visible");
      }
    });

    this.tableContainerIds.forEach((id) => {
      const table = document.getElementById(id);
      if (id === tableContainerId) {
        table?.classList.replace("hidden", "visible");
      } else {
        table?.classList.replace("visible", "hidden");
      }
    });
  }
}
