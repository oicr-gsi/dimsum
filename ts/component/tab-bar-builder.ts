class Tab {
  tabContainer: HTMLButtonElement;
  tableContainerId: string;
  tableTitle: string;
  selected: boolean = false;
  constructor(tableContainerId: string, tableTitle: string) {
    this.tableContainerId = tableContainerId;
    this.tableTitle = tableTitle;
    const button = document.createElement("button");
    button.className =
      "font-inter font-medium text-12 text-black bg-white px-2 py-1 rounded-md hover:ring-2 ring-green-200 ring-offset-1";
    button.textContent = tableTitle;

    button.onclick = () => {
      this.selected = !this.selected;
      if (this.selected) {
        button.classList.replace("text-black", "text-green-200");
        button.classList.add("ring-2", "ring-green-200", "ring-offset-1");
        // selecting a table should remove all other tables from view and create this table
        const table = document.getElementById(tableContainerId);
        if (table === null) {
          throw Error(`Container ID "${tableContainerId}" not found on page`);
        }
      } else {
        button.classList.replace("text-green-200", "text-black");
        // todo: remove green ring when not selected
      }
    };
    this.tabContainer = button;
  }
}

export class TabBar {
  tableTitles: string[];
  tableContainerIds: string[];
  container: HTMLElement;

  constructor(
    tableTitles: string[],
    tableContainerIds: string[],
    containerId: string
  ) {
    const container = document.getElementById(containerId);
    if (container === null) {
      throw Error(`Container ID "${containerId}" not found on page`);
    }
    this.tableTitles = tableTitles;
    this.tableContainerIds = tableContainerIds;
    this.container = container;
  }
  public build() {
    const controlsContainer = document.createElement("span");
    controlsContainer.className =
      "flex-auto space-x-2 rounded-md px-2 py-2 bg-grey-100 inline-block";
    this.tableContainerIds.forEach((table, idx) => {
      controlsContainer.append(
        new Tab(table, this.tableTitles[idx]).tabContainer
      );
    });
    this.container.append(controlsContainer);
  }

  public reload() {}
}
