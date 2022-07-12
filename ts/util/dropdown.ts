import { makeIcon } from "./html-utils";

export interface DropdownOption {
  selectable: boolean;
  text?: string; // text is required if option is selectable

  render(li: HTMLLIElement, dropdown: dropdown): void;
}

export class BasicDropdownOption implements DropdownOption {
  selectable: boolean = true;
  text: string;
  handler: (dropdown: dropdown) => void;

  constructor(text: string, handler: (dropdown: dropdown) => void) {
    this.text = text;
    this.handler = handler;
  }

  render(li: HTMLLIElement, dropdown: dropdown): void {
    li.className = "px-2 py-1 rounded-md hover:bg-green-200 hover:text-white";
    li.innerHTML = this.text;
    li.addEventListener("click", () => this.handler(dropdown));
  }
}

export class dropdown {
  private dropdownContainer: HTMLElement;

  constructor(
    DropdownOptions: DropdownOption[],
    displaySelection: boolean,
    displayLabel?: string,
    defaultOption?: string
  ) {
    this.dropdownContainer = document.createElement("div");
    this.dropdownContainer.classList.add("inline-block");

    const dropdownButton = makeDropdownButton();
    const dropdownClickout = makeDropdownClickout();
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
    dropdownButton.innerHTML = makeDisplayText(displayLabel, defaultOption);
    dropdownClickout.onclick = defaultOption ? toggleMenu : invalidInput;

    DropdownOptions.forEach((option) => {
      const li = document.createElement("li");
      if (option.selectable) {
        // basic option
        option.render(li, this); // new
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

  public getTag() {
    return this.dropdownContainer;
  }
}

export function makeDisplayText(label?: string, option?: string) {
  const formatText = (text?: string) => (text ? `${text}` : ``);
  const formatLabel = (label?: string) => (label ? `${label}:` : ``);
  return `${formatLabel(label)} ${formatText(option)}`;
}

function makeDropdownButton() {
  const button = document.createElement("button");
  button.className =
    "font-inter font-medium text-12 text-black bg-grey-100 px-2 py-1 rounded-md hover:ring-2 ring-green-200 ring-offset-1 flex space-x-2 items-center";

  //doesnt work???
  const dropdownOpenIcon = makeIcon("check");
  dropdownOpenIcon.classList.add("text-black");
  const dropdownCloseIcon = makeIcon("xmark");
  dropdownCloseIcon.classList.add("text-black", "hidden");
  button.appendChild(dropdownOpenIcon);
  button.appendChild(dropdownCloseIcon);
  button.addEventListener("click", () => {
    dropdownOpenIcon.classList.toggle("hidden");
    dropdownCloseIcon.classList.toggle("hidden");
  });

  return button;
}

function makeDropdownMenuContainer() {
  const dropdownMenuContainer = document.createElement("menu");
  dropdownMenuContainer.className =
    "absolute hidden mt-2 w-fit rounded-md p-1 bg-grey-100 font-inter font-medium text-black text-12 drop-shadow-lg cursor-pointer";
  return dropdownMenuContainer;
}

function makeDropdownClickout() {
  const clickout = document.createElement("button");
  clickout.className =
    "bg-transparent fixed inset-0 w-full h-full cursor-default hidden";
  return clickout;
}
