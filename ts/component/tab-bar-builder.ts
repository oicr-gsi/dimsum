import { Pair } from "../util/pair";
import { getSearchParams, updateUrlParams } from "../util/urls";
import { TableBuilder } from "./table-builder";

class Tab {
  tabContainer: HTMLButtonElement;
  table: Pair<any, string>;
  selected: boolean = false;
  onSelect: (table: Pair<any, string>) => void;
  constructor(
    table: Pair<any, string>,
    onSelect: (table: Pair<any, string>) => void,
    selected?: boolean
  ) {
    this.table = table;
    this.onSelect = onSelect;
    const button = document.createElement("button");
    if (selected) this.selected = selected; // is the default tab
    button.className =
      "flex-auto font-inter font-medium text-12 text-black bg-white px-2 py-1 rounded-md ring-green-200 ring-offset-1 ring-2";
    button.textContent = table.value;
    button.onclick = () => {
      if (!this.selected) {
        this.selected = !this.selected;
        this.styleButton();
        onSelect(this.table);
      }
    };
    this.tabContainer = button;
    this.styleButton();
  }

  // modifies the tab styling according to whether or not it has been selected
  public styleButton() {
    if (this.selected) {
      this.tabContainer.classList.replace("text-black", "text-green-200");
      this.tabContainer.classList.replace("hover:ring-2", "ring-2");
    } else {
      this.tabContainer.classList.replace("text-green-200", "text-black");
      this.tabContainer.classList.replace("ring-2", "hover:ring-2");
    }
  }
}

export class TabBar {
  tables: Pair<any, string>[]; // where the key is the table definition, and the value is the table title
  defaultTable: string;
  tabBarContainer: HTMLElement;
  tableContainer: HTMLElement;
  tabs: Tab[] = [];

  constructor(
    tables: Pair<any, string>[],
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
    this.defaultTable = defaultTable;
    this.tables = tables;
  }

  public build() {
    const controlsContainer = document.createElement("span");
    controlsContainer.className =
      "inline-flex flex-wrap space-x-2 gap-y-2 rounded-md px-2 py-2 bg-grey-100";
    // given all the tables and their titles, create the tab bar
    this.tables.forEach((table, idx) => {
      const reload = (tb: Pair<any, string>) => {
        this.reloadTable(tb);
      };
      this.tabs.push(
        new Tab(table, reload, table.value === this.defaultTable ? true : false)
      );
      controlsContainer.appendChild(this.tabs[idx].tabContainer);
    });

    this.tabBarContainer.append(controlsContainer);
    // build the default table
    this.tables.forEach((tb) => {
      if (tb.value === this.defaultTable) {
        this.reloadTable(tb);
      }
    });
  }

  public reloadTable(table: Pair<any, string>) {
    this.tabs.forEach((tab) => {
      if (tab.table.value === table.value) {
        tab.selected = true;
        // destroy current table and construct new table
        this.tableContainer.innerHTML = "";
        new TableBuilder(
          table.key,
          this.tableContainer.id,
          getSearchParams(),
          updateUrlParams
        ).build();
      } else {
        tab.selected = false;
      }
      tab.styleButton();
    });
  }
}
