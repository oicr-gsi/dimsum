import { Pair } from "../util/pair";

class Tab {
  title: string;
  tabButton: HTMLButtonElement;
  selected: boolean;

  constructor(
    element: Pair<string, () => void>,
    selected: boolean,
    onSelect: (title: string) => void
  ) {
    this.selected = selected;
    this.title = element.key;
    const button = document.createElement("button");
    button.className =
      "flex-auto font-inter font-medium text-12 text-black bg-white px-2 py-1 rounded-md ring-green-200 ring-offset-1 ring-2";
    button.textContent = element.key;
    button.onclick = () => {
      if (!this.selected) {
        onSelect(element.key);
        element.value();
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
  elements: Pair<string, () => void>[];
  defaultElement: string;
  tabBarContainer: HTMLElement;
  tabs: Tab[] = [];

  constructor(
    elements: Pair<string, () => void>[],
    defaultElement: string,
    tabBarContainerId: string
  ) {
    const tabBarContainer = document.getElementById(tabBarContainerId);
    if (tabBarContainer === null) {
      throw Error(`Container ID "${tabBarContainerId}" not found on page`);
    }
    this.tabBarContainer = tabBarContainer;
    this.elements = elements;
    this.defaultElement = defaultElement;
  }

  public build() {
    const controlsContainer = document.createElement("span");
    controlsContainer.className =
      "inline-flex flex-wrap space-x-2 gap-y-2 rounded-md px-2 py-2 bg-grey-100";
    // given all the tables and their titles, create the tab bar
    this.elements.forEach((element, idx) => {
      this.tabs.push(
        new Tab(
          element,
          element.key === this.defaultElement ? true : false,
          () => this.onTabSelect(element.key)
        )
      );
      controlsContainer.appendChild(this.tabs[idx].tabButton);
      if (element.key === this.defaultElement) this.elements[idx].value();
    });
    this.tabBarContainer.append(controlsContainer);
  }

  private onTabSelect(title: string) {
    this.tabs.forEach((tab) => {
      tab.selected = tab.title === title ? true : false;
      tab.styleButton();
    });
  }
}
