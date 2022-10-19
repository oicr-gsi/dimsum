class Tab {
  tabContainer: HTMLButtonElement;
  element: HTMLTableElement;
  tableId: string;
  tableTitle: string;
  visible: boolean = true;
  constructor(element: HTMLTableElement, tableId: string, tableTitle: string) {
    this.element = element;
    this.tableId = tableId;
    this.tableTitle = tableTitle;
    const button = document.createElement("button");
    button.className =
      "font-inter font-medium text-12 text-black px-2 py-1 rounded-md cursor-default inline-block";
    if (this.visible) button.classList.add("visible");
    button.textContent = tableTitle;
    button.onclick = () => {
      const table = document.getElementById(tableId);
      if (table === null) {
        throw Error(`Container ID "${tableId}" not found on page`);
      }
      table.classList.toggle("hidden");
    };
    this.tabContainer = button;
  }
}

export class TabBar {
  tableContainers: HTMLTableElement[];
  tableIds: string[];
  container: HTMLElement;

  constructor(
    tableContainers: HTMLTableElement[],
    tableIds: string[],
    containerId: string
  ) {
    const container = document.getElementById(containerId);
    if (container === null) {
      throw Error(`Container ID "${containerId}" not found on page`);
    }
    this.tableContainers = tableContainers;
    this.tableIds = tableIds;
    this.container = container;
  }
  public build() {
    const controlsContainer = document.createElement("div");
    controlsContainer.className = "space-x-2 rounded-md px-2 py-1 bg-grey-100";
    this.tableContainers.forEach((table, idx) => {
      controlsContainer.append(
        new Tab(table, `tableId: ${idx}`, `tableTitle: ${idx}`).tabContainer
      );
    });
  }
}
