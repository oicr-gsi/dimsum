import { makeDropdownMenuContainer } from "./html-utils";

export interface DropdownOption {
  selectable: boolean;
  render(li: HTMLLIElement): void;
}
export class BasicDropdownOption implements DropdownOption {
  text: string;
  selectable: boolean;
  handler: () => void;

  constructor(text: string, handler: () => void) {
    this.text = text;
    this.handler = handler;
    this.selectable = true;
  }

  render(li: HTMLLIElement): void {
    li.className = "px-2 py-1 rounded-md hover:bg-green-200 hover:text-white";
    li.innerHTML = this.text;
    li.addEventListener("click", this.handler);
  }
}

export function makeDropdownMenu(
  DropdownOptions: BasicDropdownOption[],
  ParentOption: HTMLButtonElement | HTMLLIElement
) {
  const dropdownMenuContainer = makeDropdownMenuContainer();
  DropdownOptions.forEach((option) => {
    const li = document.createElement("li");
    option.render(li);
    if (ParentOption instanceof HTMLButtonElement && option.selectable) {
      li.addEventListener(
        "click",
        () => (ParentOption.innerHTML = option.text)
      );
    }
    dropdownMenuContainer.appendChild(li);
  });
  return dropdownMenuContainer;
}
