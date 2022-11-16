import { Pair } from "../util/pair";

class Tab {
  tabButton: HTMLButtonElement;
  selected: boolean;

  constructor(
    owner: TabBar,
    table: Pair<Pair<any, string>, () => void>,
    selected: boolean
  ) {
    this.selected = selected;
    const button = document.createElement("button");
    button.className =
      "flex-auto font-inter font-medium text-12 text-black bg-white px-2 py-1 rounded-md ring-green-200 ring-offset-1 ring-2";
    button.textContent = table.key.value;
    button.onclick = () => {
      if (!this.selected) {
        // should destroy current table and create new table
        this.selected = true;
        owner.current = table.key.value;
        this.styleButton();
        table.value();
      }
    };
    this.tabButton = button;
    this.styleButton();
  }

  // modifies the tab styling according to whether or not it has been selected
  public styleButton() {
    if (this.selected) {
      this.tabButton.classList.replace("text-black", "text-green-200");
      this.tabButton.classList.replace("hover:ring-2", "ring-2");
    } else {
      this.tabButton.classList.replace("text-green-200", "text-black");
      this.tabButton.classList.replace("ring-2", "hover:ring-2");
    }
  }
}

export class TabBar {
  tables: Pair<Pair<any, string>, () => void>[];
  current: string;
  tabBarContainer: HTMLElement;
  tableContainer: HTMLElement;
  tabs: Tab[] = [];

  constructor(
    tables: Pair<Pair<any, string>, () => void>[],
    defaultTable: string,
    tabBarContainerId: string,
    tableContainerId: string
  ) {
    // get all containers
    const tabBarContainer = document.getElementById(tabBarContainerId);
    if (tabBarContainer === null) {
      throw Error(`Container ID "${tabBarContainerId}" not found on page`);
    }
    this.tabBarContainer = tabBarContainer;
    const tableContainer = document.getElementById(tableContainerId);
    if (tableContainer === null) {
      throw Error(`Container ID "${tableContainerId}" not found on page`);
    }
    this.tableContainer = tableContainer;
    this.current = defaultTable;
    this.tables = tables;
  }

  public build() {
    const controlsContainer = document.createElement("span");
    controlsContainer.className =
      "inline-flex flex-wrap space-x-2 gap-y-2 rounded-md px-2 py-2 bg-grey-100";
    // given all the tables and their titles, create the tab bar
    this.tables.forEach((table, idx) => {
      this.tabs.push(
        new Tab(this, table, table.key.value === this.current ? true : false)
      );
      controlsContainer.appendChild(this.tabs[idx].tabButton);
    });
    this.tabBarContainer.append(controlsContainer);
    this.tables[0].value(); // reload
  }
}
