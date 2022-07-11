interface DropdownOption {
  render(li: HTMLLIElement): void;
}
export class BasicDropdownOption implements DropdownOption {
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
  DropdownOptions: BasicDropdownOption[],
  defaultOption: string
) {
  const dropdownButton = makeDropdownButton();
  dropdownButton.innerHTML = defaultOption;
  const dropdownContainer = document.createElement("div");
  const dropdownClickout = makeDropdownClickout();

  const dropdownMenuContainer = makeDropdownMenuContainer();
  DropdownOptions.forEach((option) => {
    const li = document.createElement("li");
    option.render(li);
    li.addEventListener(
      "click",
      () => (dropdownButton.innerHTML = option.text)
    );
    dropdownMenuContainer.appendChild(li);
  });

  const toggleMenu = () => {
    dropdownMenuContainer.classList.toggle("hidden");
    dropdownClickout.classList.toggle("hidden");
  };
  dropdownButton.onclick = toggleMenu;
  dropdownClickout.onclick = toggleMenu;

  dropdownContainer.appendChild(dropdownButton);
  dropdownContainer.appendChild(dropdownClickout);
  dropdownContainer.appendChild(dropdownMenuContainer);
  return dropdownContainer;
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
