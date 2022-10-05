import { makeClickout, makeIcon } from "../util/html-utils";
import { appendUrlParam } from "../util/urls";

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
    const invalidInput = () => {
      dropdownMenuContainer.classList.add(
        "ring-green-200",
        "ring-2",
        "ring-offset-1"
      );
    };
    dropdownButton.onclick = toggleMenu;
    const dropdownButtonText = document.createElement("div");
    dropdownButtonText.innerHTML = makeDisplayText(displayLabel, defaultOption);
    dropdownButton.appendChild(dropdownButtonText);
    dropdownButton.appendChild(makeIcon("caret-down"));
    dropdownClickout.onclick = defaultOption ? toggleMenu : invalidInput;

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
            if (displayLabel && displayTemporary) {
              const nextUrl = `${displayLabel.replace(
                " ",
                "+"
              )}=${option.text.replace(" ", "+")}`;
              const nextState = {
                info: `update url: append ${nextUrl}`,
              };
              const nextTitle = `update page: append ${nextUrl}`;
              // pushState will create a new entry in the browser's history, without reloading
              // append filters to url as appropriate
              window.history.replaceState(
                nextState,
                nextTitle,
                window.location.origin +
                  appendUrlParam(displayLabel, option.text)
              );
            }
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
