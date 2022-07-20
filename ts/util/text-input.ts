import { makeClickout, makeIcon } from "./html-utils";

export class TextInput {
  private container: HTMLElement;
  private textField: HTMLInputElement;
  constructor(title: string, onClose: (textInput: TextInput) => void) {
    this.container = document.createElement("div");
    this.textField = document.createElement("input");
    const label = document.createElement("span");
    label.innerHTML = `${title}:`;
    const submitIcon = makeIcon("check");
    const textInputClickout = makeClickout();
    textInputClickout.classList.toggle("hidden");

    this.container.className =
      "font-inter font-medium text-12 text-black px-2 py-1 rounded-md ring-2 ring-offset-1 ring-red inline-block flex-auto items-center space-x-2 mr-2";
    this.textField.className =
      "ring-0 border-0 outline-0 rounded-sm relative z-10 px-1 w-4";
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
    textInputClickout.onclick = submitTextInput;
    this.textField.addEventListener("keypress", (event) => {
      if (event.key === "Enter") {
        submitTextInput();
      }
    });
    this.textField.addEventListener("input", () => {
      this.styleValidity();
      this.textField.style.width = this.textField.value.length + 2 + "ch";
    });
    this.container.append(label);
    this.container.appendChild(this.textField);
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

  public getTag() {
    return this.container;
  }
  public getValue() {
    return this.textField.value;
  }
}
