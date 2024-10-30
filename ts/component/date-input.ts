import { makeClickout, makeIcon } from "../util/html-utils";

export class DateInput {
  private container: HTMLElement;
  private dateInput: HTMLInputElement;
  private keydownListener: (event: KeyboardEvent) => void;

  constructor(labelText: string, onChange: (value: string) => void) {
    this.container = document.createElement("div");
    this.container.className =
      "inline-block flex-auto rounded-md ring-red ring-2 ring-offset-1";

    // remove date input on click outside of element
    const clickout = makeClickout();
    clickout.classList.remove("hidden");
    clickout.onclick = () => {
      this.destroy();
    };

    this.dateInput = document.createElement("input");
    this.dateInput.type = "date";
    this.dateInput.className = "ring-0 border-0 outline-0 rounded-sm px-1";
    this.dateInput.onchange = () => {
      this.styleValidity();
    };

    const submit = () => {
      if (this.dateInput.value) {
        onChange(this.dateInput.value);
        this.destroy();
      }
    };

    this.keydownListener = (event: KeyboardEvent) => {
      if (event.key === "Enter") {
        // submit on Enter (ignored if invalid)
        submit();
      } else if (event.key === "Esc" || event.key === "Escape") {
        // remove date input on 'Esc' key press
        this.destroy();
      }
    };
    document.addEventListener("keydown", this.keydownListener);

    const label = document.createElement("span");
    label.textContent = `${labelText}: `;

    const submitIcon = makeIcon("check");
    submitIcon.onclick = submit;
    submitIcon.classList.add("text-black", "hover:text-green");

    var innerContainer = document.createElement("div");
    innerContainer.className =
      "font-inter font-medium text-12 text-black px-2 py-1 inline-block space-x-2 relative z-40";
    innerContainer.append(label, this.dateInput, submitIcon);

    this.container.append(innerContainer, clickout);

    // wait for browser to render element before setting focus
    window.setTimeout(() => this.dateInput.focus(), 0);
  }

  private styleValidity() {
    if (this.dateInput.value) {
      this.container.classList.replace("ring-red", "ring-green-200");
    } else {
      this.container.classList.replace("ring-green-200", "ring-red");
    }
  }

  private destroy() {
    this.container.remove();
    document.removeEventListener("keydown", this.keydownListener);
  }

  public getElement() {
    return this.container;
  }
}
