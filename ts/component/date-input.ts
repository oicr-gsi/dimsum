export class DateInput {
  private container: HTMLElement;
  private dateInput: HTMLInputElement;
  private keydownListener: (event: KeyboardEvent) => void;
  private mousedownListener: (event: MouseEvent) => void;

  constructor(labelText: string, onChange: (value: string) => void) {
    this.container = document.createElement("div");
    this.container.className =
      "font-inter font-medium text-12 text-black bg-grey-100 px-2 py-1 rounded-md hover:ring-2 ring-green-200 ring-offset-1 inline-flex";

    this.dateInput = document.createElement("input");
    this.dateInput.type = "date";
    this.dateInput.onchange = () => {
      if (this.dateInput.value) {
        onChange(this.dateInput.value);
        this.container.remove(); // remove the date input after selection
      }
    };

    const label = document.createElement("span");
    label.textContent = `${labelText}: `;

    this.container.appendChild(label);
    this.container.appendChild(this.dateInput);

    // remove date input on 'Esc' key press
    this.keydownListener = (event: KeyboardEvent) => {
      if (event.key === "Esc" || event.key === "Escape") {
        this.destroy();
      }
    };
    // remove date input when mousedown outside of the date input
    this.mousedownListener = (event: MouseEvent) => {
      if (event.target !== this.dateInput) {
        setTimeout(() => {
          this.destroy();
        }, 0);
      }
    };
    document.addEventListener("keydown", this.keydownListener);
    document.addEventListener("mousedown", this.mousedownListener);
  }

  destroy() {
    this.container.remove();
    document.removeEventListener("keydown", this.keydownListener);
    document.removeEventListener("mousedown", this.mousedownListener);
  }

  public getElement() {
    return this.container;
  }
}
