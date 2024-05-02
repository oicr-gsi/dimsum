import { replaceUrlParams, updateUrlParams } from "../util/urls";

export class DateInput {
  direction: string;
  onChangeCallback: (key: string, value: string) => void;
  container: HTMLElement;
  lastValue: string | null = null;

  constructor(
    direction: string,
    onChangeCallback: (key: string, value: string) => void
  ) {
    this.direction = this.formatLabel(direction);
    this.onChangeCallback = onChangeCallback;
    this.container = this.createDateFilterControl();
  }

  private createDateFilterControl(): HTMLElement {
    const dateDiv = document.createElement("div");
    dateDiv.className =
      "font-inter font-medium text-12 text-black bg-grey-100 px-2 py-1 rounded-md hover:ring-2 ring-green-200 ring-offset-1";

    const dateLabel = document.createElement("label");
    dateLabel.htmlFor = `${this.direction.toLowerCase()}Date`;
    dateLabel.textContent = `${this.direction}:`;

    const dateSelect = this.makeDateSelect();
    const dateInput = this.makeDateInput();

    const updateUrlAndFilter = () => {
      const selectedDirection = this.direction.toUpperCase();
      const selectedType = dateSelect.value.toUpperCase();
      const selectedDate = dateInput.value;
      const key = `${selectedDirection}_${selectedType}`;

      if (selectedDate !== this.lastValue) {
        this.lastValue = selectedDate; // Update lastValue to the current

        if (selectedDate) {
          replaceUrlParams(key, selectedDate);
        } else {
          updateUrlParams(key, "", false);
        }
        this.onChangeCallback(key, selectedDate);
      }
    };

    dateSelect.addEventListener("change", updateUrlAndFilter);
    dateInput.addEventListener("input", updateUrlAndFilter);
    dateInput.addEventListener("change", updateUrlAndFilter);

    dateDiv.appendChild(dateLabel);
    dateDiv.appendChild(dateSelect);
    dateDiv.appendChild(dateInput);

    return dateDiv;
  }

  private makeDateSelect(): HTMLSelectElement {
    const dateSelect = document.createElement("select");
    dateSelect.id = `${this.direction.toLowerCase()}Date`;
    dateSelect.className =
      "text-black bg-grey-100 px-2 py-1 rounded-md hover:ring-2 ring-green-200 ring-offset-1";

    const dateTypes = ["before", "after"];
    dateTypes.forEach((type) => {
      const option = document.createElement("option");
      option.value = type.toLowerCase();
      option.textContent = type;
      dateSelect.appendChild(option);
    });

    return dateSelect;
  }

  private makeDateInput(): HTMLInputElement {
    const dateInput = document.createElement("input");
    dateInput.type = "date";
    dateInput.id = `${this.direction.toLowerCase()}DateInput`;
    return dateInput;
  }

  private formatLabel(label: string): string {
    return label.charAt(0).toUpperCase() + label.slice(1).toLowerCase();
  }
}
