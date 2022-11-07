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
      "font-inter font-medium text-12 text-black bg-white px-2 py-1 rounded-md";
    button.textContent = table.value;
    button.onclick = () => {
      console.log(`clicked tab ${table.value}`);
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
  tables: Pair<any, string>[]; // where the key is the table definition, and the value is the table title
  defaultTable: string;
  tabBarContainer: HTMLElement;
  tableContainer: HTMLElement;
  tableTitleContainer?: HTMLElement;
  tabs: Tab[] = [];

  constructor(
    tables: Pair<any, string>[],
    defaultTable: string,
    tabBarContainerId: string,
    tableContainerId: string,
    tableTitleContainerId?: string
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
    if (tableTitleContainerId) {
      const tableTitleContainer = document.getElementById(
        tableTitleContainerId
      );
      if (tableTitleContainer === null) {
        throw Error(
          `Container ID "${tableTitleContainerId}" not found on page`
        );
      }
      this.tableTitleContainer = tableTitleContainer;
    }
    this.defaultTable = defaultTable;
    this.tables = tables;
  }

  public build() {
    const controlsContainer = document.createElement("span");
    controlsContainer.className =
      "flex-auto space-x-2 rounded-md px-2 py-2 bg-grey-100 inline-block";
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
    console.log(`reloading table, selected: ${table.value}`);
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
        if (this.tableTitleContainer) {
          this.tableTitleContainer.innerHTML = table.value;
        }
        console.log(`rebuild table: ${table.value}`);
      } else {
        tab.selected = false;
      }
      tab.styleButton();
    });
  }
}

/* TRY 2 --------------------------------------------------------------------
export class TabBar {
  // tableBuilders: Pair<TableBuilder<any, any>, string>[]; // table builders and titles
  // currentTable: Pair<TableBuilder<any, any>, string>; // title of the default table
  tables: Pair<any, string>[];
  tabBarContainer: HTMLElement;
  tableContainer: HTMLTableElement;
  tableTitleContainer: HTMLElement;
  tabs: Tab[] = [];

  // constructor(
  //   tableBuilders: Pair<TableBuilder<any, any>, string>[],
  //   defaultTable: string,
  //   containerId: string
  // ) {
  //   const tabBarContainer = document.getElementsById(tabBarContainerId);
  //   const container = document.getElementById(containerId);
  //   if (container === null) {
  //     throw Error(`Container ID "${containerId}" not found on page`);
  //   }
  //   this.tableBuilders = tableBuilders;
  //   this.currentTable = tableBuilders[0]; // temporarily set current table
  //   // set the default table as the current table
  //   tableBuilders.forEach((table) => {
  //     if (table.value === defaultTable) {
  //       this.currentTable = table;
  //     }
  //   });
  //   this.container = container;
  // }

  constructor(
    tables: Pair<any, string>,
    tabBarContainerId: string,
    tableContainerId: string,
    tabletitleContainerId?: string
  ) {
    const tabBarContainer = document.getElementsById(tabBarContainerId);
    const container = document.getElementById(containerId);
    if (container === null) {
      throw Error(`Container ID "${containerId}" not found on page`);
    }
    this.tableBuilders = tableBuilders;
    this.currentTable = tableBuilders[0]; // temporarily set current table
    // set the default table as the current table
    tableBuilders.forEach((table) => {
      if (table.value === defaultTable) {
        this.currentTable = table;
      }
    });
    this.container = container;
  }

  public build() {
    const controlsContainer = document.createElement("span");
    controlsContainer.className =
      "flex-auto space-x-2 rounded-md px-2 py-2 bg-grey-100 inline-block";
    // given all the table builders and titles, create the tab bar
    this.tableBuilders.forEach((table, idx) => {
      const reload = (id: string) => {
        this.reloadTable(id);
      };
      this.tabs.push(
        new Tab(
          table,
          reload,
          table.value === this.currentTable.value ? true : false
        )
      );
      controlsContainer.appendChild(this.tabs[idx].tabContainer);
    });

    this.container.append(controlsContainer);
    this.reloadTable(this.currentTable.value); // show the default table
  }

  public reloadTable(tableTitle: string) {
    this.tabs.forEach((tab) => {
      if (tab.table.value === tableTitle) {
        tab.selected = true;
        // destroy current table and add new table
        var oldTable = document.getElementById(
          this.currentTable.key.container.id
        );
        if (oldTable) {
          oldTable.innerHTML = "";
        }
        this.currentTable = tab.table;
        console.log(`rebuild table: ${this.currentTable.value}`);
        // todo: this will not apply the new filters
        // need to reconstruct the new table everytime
        // how do we store all the information the table needs to build itself
        this.currentTable.key.build();
      } else {
        tab.selected = false;
      }
      tab.styleButton();
    });
  }
}
*/

/* TRY 1 ---------------------------------------------------------------------
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
    if (selected) this.selected = selected; // is the default tab
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
    this.tabContainer = button;
    this.styleButton();
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

  // reload: given the current active table, reload all tables and buttons
  public reloadTables(tableContainerId: string) {
    console.log(`active table: ${tableContainerId}`);
    console.log("RELOADING TABLES . . . . .");
    // reset selected attribute of tabs to reflect current active table
    this.tabs.forEach((tab) => {
      console.log(`current tab: ${tab.tableContainerId}`);
      var table = document.getElementById(tab.tableContainerId);
      if (tab.tableContainerId !== tableContainerId) {
        console.log("hiding table . . .");
        tab.selected = false;
        if (!table?.classList.replace("visible", "hidden")) {
          table?.classList.add("hidden");
        }
      } else {
        console.log("showing table . . .");
        tab.selected = true;
        if (!table?.classList.replace("hidden", "visible"))
          table?.classList.add("visible");
      }
      tab.styleButton();
    });
  }
}
*/
