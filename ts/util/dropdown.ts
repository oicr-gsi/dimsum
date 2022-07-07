interface DropdownOption {
  selectable: boolean;
  text?: string; // text is required if option is selectable
  render(li: HTMLLIElement): void;
}

export class BasicDropdownOption implements DropdownOption {
  selectable: boolean = true;
  text: string;
  handler: () => void;

  constructor(text: string, handler: () => void) {
    this.text = text;
    this.handler = handler;
  }

  render(li: HTMLLIElement): void {
    li.className = "px-2 py-1 rounded-md hover:bg-green-200 hover:text-white";
    li.innerHTML = this.text;
    li.addEventListener("click", this.handler);
  }
}

export function makeDropdownMenu(
  DropdownOptions: DropdownOption[],
  defaultOption: string,
  displaySelection: boolean,
  displayLabel?: string
) {
  const dropdownButton = makeDropdownButton();
  dropdownButton.innerHTML = makeDisplayText(defaultOption, displayLabel);
  const dropdownContainer = document.createElement("div");
  dropdownContainer.classList.add("inline-block");
  const dropdownClickout = makeDropdownClickout();
  const dropdownMenuContainer = makeDropdownMenuContainer();
  const toggleMenu = () => {
    dropdownMenuContainer.classList.toggle("hidden");
    dropdownClickout.classList.toggle("hidden");
  };

  DropdownOptions.forEach((option) => {
    const li = document.createElement("li");
    option.render(li);
    if (option.selectable) {
      li.addEventListener("click", () => {
        if (displaySelection) {
          if (!option.text) {
            throw new Error("Selectable option has no text to display");
          }
          dropdownButton.innerHTML = makeDisplayText(option.text, displayLabel);
        }
        toggleMenu();
      });
    }
    dropdownMenuContainer.appendChild(li);
  });

  dropdownButton.onclick = toggleMenu;
  dropdownClickout.onclick = toggleMenu;

  dropdownContainer.appendChild(dropdownButton);
  dropdownContainer.appendChild(dropdownClickout);
  dropdownContainer.appendChild(dropdownMenuContainer);
  return dropdownContainer;
}

function makeDisplayText(text: string, label?: string) {
  return label ? `${label}: ${text}` : text;
}

function makeDropdownButton() {
  const button = document.createElement("button");
  button.className =
    "font-inter font-medium text-12 text-black bg-grey-100 px-2 py-1 rounded-md hover:ring-2 ring-green-200 ring-offset-1";
  return button;
}

function makeDropdownMenuContainer() {
  const dropdownMenuContainer = document.createElement("menu");
  dropdownMenuContainer.className =
    "absolute hidden mt-1 w-fit rounded-md p-1 bg-grey-100 font-inter font-medium text-black text-12 drop-shadow-lg cursor-pointer";
  return dropdownMenuContainer;
}

function makeDropdownClickout() {
  const clickout = document.createElement("button");
  clickout.className =
    "bg-transparent fixed inset-0 w-full h-full cursor-default hidden";
  return clickout;
}
