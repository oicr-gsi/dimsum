import { makeClickout, makeIcon } from "../util/html-utils";

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

    const dropdownButtonText = document.createElement("div");
    dropdownButtonText.innerHTML = makeDisplayText(displayLabel, defaultOption);
    dropdownButton.appendChild(dropdownButtonText);
    dropdownButton.appendChild(makeIcon("caret-down"));

    const dropdownClickout = makeClickout();
    const dropdownMenuContainer = makeDropdownMenuContainer();
    const toggleMenu = () => {
      dropdownMenuContainer.classList.toggle("hidden");
      dropdownMenuContainer.classList.remove("ring-2");
      dropdownClickout.classList.toggle("hidden");
    };

    dropdownButton.onclick = toggleMenu;
    // close dropdown menu by clicking outside of the menu or by hitting Esc
    dropdownClickout.onclick = () => {
      if (displayTemporary && !dropdownButton.classList.contains("hidden")) {
        dropdownButton.remove();
      }
      toggleMenu();
    };
    document.addEventListener(
      "keydown",
      (event) => {
        // only remove the button in question if it is not already hidden
        if (event.key === "Esc" || event.key === "Escape") {
          const menuHidden = dropdownMenuContainer.classList.contains("hidden");
          const clickoutHidden = dropdownClickout.classList.contains("hidden");
          if (displayTemporary) {
            // dropdown button is temporary, remove it and its corresponding elements
            if (
              !dropdownButton.classList.contains("hidden") &&
              !menuHidden &&
              !clickoutHidden
            ) {
              dropdownMenuContainer.remove();
              dropdownButton.remove();
            }
          } else {
            // button is NOT temporary, hide its corresponding elements if they are visible
            if (!menuHidden && !clickoutHidden) {
              toggleMenu();
            }
          }
        }
      },
      displayTemporary ? { once: true } : false
    );

    DropdownOptions.forEach((option) => {
      const li = document.createElement("li");
      option.render(li, this);
      if (option.selectable) {
        li.addEventListener("click", () => {
          if (displaySelection) {
            if (!option.text) {
              throw new Error("Selectable option has no text to display");
            }
            dropdownButtonText.innerHTML = makeDisplayText(
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
    "absolute hidden mt-2 w-fit rounded-md p-1 bg-grey-100 font-inter font-medium text-black text-12 drop-shadow-lg cursor-pointer z-40";
  return dropdownMenuContainer;
}
