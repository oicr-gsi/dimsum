export class DateInput {
  private container: HTMLElement;
  private dateInput: HTMLInputElement;

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
    document.addEventListener("keydown", (event) => {
      if (event.key === "Esc" || event.key === "Escape") {
        this.container.remove();
      }
    });
    // remove date input when mousedown outside of the date input
    document.addEventListener("mousedown", (event) => {
      if (event.target !== this.dateInput) {
        setTimeout(() => {
          this.container.remove();
        }, 0);
      }
    });
  }

  public getElement() {
    return this.container;
  }
}
