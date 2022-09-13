import { makeClickout } from "../util/html-utils";

export interface DropdownOption {
  selectable: boolean;
  text?: string; // text is required if option is selectable

  render(li: HTMLLIElement, dropdown: Dropdown): void;
}

export class BasicDropdownOption implements DropdownOption {
  selectable: boolean = true;
  text: string;
  handler: (dropdown: Dropdown) => void;

  constructor(text: string, handler: (dropdown: Dropdown) => void) {
    this.text = text;
    this.handler = handler;
  }

  render(li: HTMLLIElement, dropdown: Dropdown): void {
    li.className = "px-2 py-1 rounded-md hover:bg-green-200 hover:text-white";
    li.innerHTML = this.text;
    li.addEventListener("click", () => this.handler(dropdown));
  }
}

export class Dropdown {
  private dropdownContainer: HTMLElement;

  // displayTemporary: set to true if the created dropdown menu can be removed
  // (by user interaction or otherwise), else set to false (or undefined) if otherwise
  constructor(
    DropdownOptions: DropdownOption[],
    displaySelection: boolean,
    displayLabel?: string,
    defaultOption?: string,
    displayTemporary?: boolean
  ) {
    this.dropdownContainer = document.createElement("div");
    this.dropdownContainer.classList.add("inline-block");

    const dropdownButton = makeDropdownButton();
    const dropdownClickout = makeClickout();
    const dropdownMenuContainer = makeDropdownMenuContainer();
    const toggleMenu = () => {
      dropdownMenuContainer.classList.toggle("hidden");
      dropdownMenuContainer.classList.remove("ring-2");
      dropdownClickout.classList.toggle("hidden");
    };
    const toggleDropdownButton = () => {
      // toggle whether or not a labeled button is removed
      if (displayTemporary) {
        dropdownButton.classList.toggle("hidden");
      }
      toggleMenu();
    };
    const invalidInput = () => {
      dropdownMenuContainer.classList.add(
        "ring-green-200",
        "ring-2",
        "ring-offset-1"
      );
    };
    dropdownButton.onclick = toggleMenu;
    dropdownButton.innerHTML = makeDisplayText(displayLabel, defaultOption);
    // close dropdown menu by clicking outside of the menu or by hitting Esc
    dropdownClickout.onclick = toggleDropdownButton;
    dropdownClickout.addEventListener("keydown", (event) => {
      // only remove the button in question if it is not already hidden
      if (
        (event.key === "Escape" || event.key === "Esc") &&
        !dropdownButton.hidden &&
        !dropdownClickout.hidden &&
        !dropdownMenuContainer.hidden
      ) {
        toggleDropdownButton();
      }
    });

    // circumvent disappearing buttons (unwanted behaviour) by checking whether the dropdown menu is meant to be "temporary"
    DropdownOptions.forEach((option) => {
      const li = document.createElement("li");
      option.render(li, this);
      if (option.selectable) {
        li.addEventListener("click", () => {
          if (displaySelection) {
            if (!option.text) {
              throw new Error("Selectable option has no text to display");
            }
            dropdownButton.innerHTML = makeDisplayText(
              displayLabel,
              option.text
            );
          }
          toggleMenu();
        });
      }
      dropdownMenuContainer.appendChild(li);
    });
    if (!defaultOption) {
      toggleMenu();
    }
    this.dropdownContainer.appendChild(dropdownButton);
    this.dropdownContainer.appendChild(dropdownClickout);
    this.dropdownContainer.appendChild(dropdownMenuContainer);
  }

  public getContainerTag() {
    return this.dropdownContainer;
  }
}

function makeDisplayText(label?: string, text?: string) {
  return (label ? label + ": " : "") + (text || "");
}

function makeDropdownButton() {
  const button = document.createElement("button");
  button.className =
    "font-inter font-medium text-12 text-black bg-grey-100 px-2 py-1 rounded-md hover:ring-2 ring-green-200 ring-offset-1 flex space-x-2 items-center";
  return button;
}

function makeDropdownMenuContainer() {
  const dropdownMenuContainer = document.createElement("menu");
  dropdownMenuContainer.className =
    "absolute hidden mt-2 w-fit rounded-md p-1 bg-grey-100 font-inter font-medium text-black text-12 drop-shadow-lg cursor-pointer";
  return dropdownMenuContainer;
}
