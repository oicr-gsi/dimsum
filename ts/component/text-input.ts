import { makeClickout, makeIcon } from "../util/html-utils";
import { get } from "../util/requests";

export class TextInput {
  private container: HTMLElement;
  private datalist: HTMLDataListElement;
  private textField: HTMLInputElement;
  constructor(
    title: string,
    onClose: (textInput: TextInput) => void,
    queryUrl: string
  ) {
    this.container = document.createElement("div");
    this.textField = document.createElement("input");
    this.datalist = document.createElement("datalist");
    this.textField.setAttribute("list", title);
    this.textField.setAttribute("autocomplete", "off");
    this.datalist.id = title;
    const label = document.createElement("span");
    label.innerHTML = `${title}:`;
    const submitIcon = makeIcon("check");
    const textInputClickout = makeClickout();
    textInputClickout.classList.toggle("hidden");

    this.container.className =
      "font-inter font-medium text-12 text-black px-2 py-1 rounded-md ring-2 ring-offset-1 ring-red inline-block flex-auto items-center space-x-2";
    this.textField.className =
      "ring-0 border-0 outline-0 rounded-sm relative z-10 px-1 min-w-[150px]";
    this.textField.setAttribute("size", "10");
    submitIcon.classList.add(
      "text-black",
      "hover:text-green",
      "relative",
      "z-10"
    );
    const submitTextInput = () => {
      if (this.textField.value) {
        textInputClickout.classList.toggle("hidden");
        onClose(this);
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
    // close text input field by clicking outside or hitting esc
    textInputClickout.onclick = () => {
      this.container.remove();
    };

    document.addEventListener(
      "keydown",
      (event) => {
        // only remove the text input box in question if it is not already hidden
        if (event.key === "Esc" || event.key === "Escape") {
          const containerHidden = this.container.classList.contains("hidden");
          const textFieldHidden = this.textField.classList.contains("hidden");
          // remove text input field corresponding elements if they are visible
          if (!containerHidden && !textFieldHidden) {
            this.container.remove();
          }
        }
      },
      { once: true } // remove event listener immediately once event occurs
    );

    this.container.append(label);
    this.container.appendChild(this.textField);
    this.container.append(this.datalist);
    this.container.appendChild(submitIcon);
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

  public getContainerTag() {
    return this.container;
  }

  public getValue() {
    return this.textField.value;
  }
}
