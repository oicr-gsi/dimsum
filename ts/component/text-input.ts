import { makeClickout, makeIcon } from "../util/html-utils";
import { get } from "../util/requests";

export class TextInput {
  private container: HTMLElement;
  private datalist: HTMLDataListElement;
  private textField: HTMLInputElement;
  constructor(
    title: string,
    onClose: (values: string[]) => void,
    queryUrl: string
  ) {
    this.container = document.createElement("div");
    this.textField = document.createElement("input");
    this.datalist = document.createElement("datalist");
    this.textField.setAttribute("list", title);
    this.textField.setAttribute("autocomplete", "off");
    this.textField.setAttribute("placeholder", "item 1; item 2...");
    this.datalist.id = title;
    const label = document.createElement("span");
    label.innerHTML = `${title}:`;
    const submitIcon = makeIcon("check");
    const textInputClickout = makeClickout();
    textInputClickout.classList.toggle("hidden");

    const submitTextInput = () => {
      const inputValues = this.textField.value
        .split(";")
        .map((val) => val.trim())
        .filter((val) => val.length > 0);

      if (inputValues.length > 0) {
        onClose(inputValues);
        this.container.remove();
        document.removeEventListener("keydown", handleEsc);
        this.textField.value = ""; // reset field after all values are processed
      } else {
        this.textField.focus();
      }
    };

    submitIcon.onclick = submitTextInput;
    this.textField.addEventListener("keypress", (event) => {
      if (event.key === "Enter") {
        submitTextInput();
      }
    });

    // add paste event listener for reformatting
    this.textField.addEventListener("paste", (event: ClipboardEvent) => {
      event.preventDefault();
      const clipboardData = event.clipboardData;
      if (clipboardData) {
        const pastedData = clipboardData.getData("text");
        const formattedData = this.formatPastedData(pastedData);
        const currentValue = this.textField.value;
        const cursorPosition = this.textField.selectionStart || 0;
        const newValue =
          currentValue.slice(0, cursorPosition) +
          formattedData +
          currentValue.slice(cursorPosition);
        this.textField.value = newValue;
        this.styleValidity();
      }
    });

    this.textField.addEventListener("input", () => {
      this.styleValidity();
      this.textField.setAttribute(
        "size",
        Math.max(this.textField.value.length, 10).toString()
      );
      // if textfield was auto-filled, no need to redisplay autocomplete suggestion list
      if (this.textField.value.slice(-1) === "\u2063") {
        // remove invisible separator and treat as normal input from here on
        this.textField.value = this.textField.value.slice(0, -1);
      } else {
        // value was user-typed, display autcomplete suggestion list
        this.loadAutocomplete(queryUrl);
      }
    });

    var handleEsc = (event: KeyboardEvent) => {
      // only remove the text input box in question if it is not already hidden
      if (event.key === "Esc" || event.key === "Escape") {
        // remove text input field corresponding elements if they are visible
        this.container.remove();
        document.removeEventListener("keydown", handleEsc);
      }
    };
    // close text input field by clicking outside or hitting esc
    textInputClickout.onclick = () => {
      this.container.remove();
      document.removeEventListener("keydown", handleEsc);
    };

    document.addEventListener("keydown", handleEsc);

    this.container.className =
      "inline-block flex-auto rounded-md ring-red ring-2 ring-offset-1";
    var textInputContainer = document.createElement("div");
    textInputContainer.className =
      "font-inter font-medium text-12 text-black px-2 py-1 inline-block space-x-2 relative z-40";
    this.textField.className =
      "ring-0 border-0 outline-0 rounded-sm px-1 min-w-[150px]";
    this.textField.setAttribute("size", "10");
    submitIcon.classList.add("text-black", "hover:text-green");

    textInputContainer.append(label);
    textInputContainer.appendChild(this.textField);
    textInputContainer.append(this.datalist);
    textInputContainer.appendChild(submitIcon);
    this.container.appendChild(textInputContainer);
    this.container.appendChild(textInputClickout);
    // wait for browser to render element before setting focus
    window.setTimeout(() => this.textField.focus(), 0);
  }

  private styleValidity() {
    if (this.textField.value) {
      this.container.classList.replace("ring-red", "ring-green-200");
    } else {
      this.container.classList.replace("ring-green-200", "ring-red");
    }
  }

  private async loadAutocomplete(queryUrl: string) {
    const val = this.textField.value;
    this.datalist.replaceChildren(); // empties autocomplete
    if (val.length < 1) {
      // results are only displayed for input > 1 character
      return;
    }
    const response = await get(queryUrl, { q: val });
    if (!response.ok) {
      throw new Error(`Error loading autocomplete: ${response.status}`);
    }
    const data: string[] = await response.json();
    data.forEach((element) => {
      const opt = document.createElement("option");
      // Invisible separator is used to detect autocomplete vs user-typed input
      opt.innerHTML = `${element}&#8291;`;
      this.datalist.appendChild(opt);
    });
  }

  private formatPastedData(data: string): string {
    return data
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter((line) => line.length > 0)
      .join("; ");
  }

  public getContainerTag() {
    return this.container;
  }

  public getValue() {
    return this.textField.value;
  }
}
