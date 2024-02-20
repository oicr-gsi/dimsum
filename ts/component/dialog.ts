import { addActionButton, makeIcon, makeTextDiv } from "../util/html-utils";
import { BasicDropdownOption, Dropdown, DropdownOption } from "./dropdown";

interface FormAction {
  title: string;
  handler: (
    resolve: (value: any) => void,
    reject: (reason: string) => void
  ) => void;
}

function showDialog(
  title: string,
  makeBody: (body: Node) => void,
  actions?: FormAction[],
  callback?: (result: any) => void
) {
  const dialog = document.createElement("dialog");
  dialog.className =
    "noprint text-black bg-white rounded-xl border-2 border-grey-200 drop-shadow-lg";
  const header = document.createElement("div");
  header.className = "flex flex-row items-center border-b-1 border-grey-200";
  const titleElement = document.createElement("span");
  titleElement.className =
    "font-sarabun font-thin text-green-200 text-24 my-2 mx-4";
  titleElement.innerText = title;
  const spacer = document.createElement("div");
  spacer.className = "flex-auto";
  const icon = makeIcon("xmark");
  icon.classList.add(
    "fa-solid",
    "fa-xmark",
    "text-grey-200",
    "hover:text-green-200",
    "text-24",
    "m-2",
    "mx-4",
    "cursor-pointer"
  );
  icon.addEventListener("click", () => {
    dialog.close();
    dialog.remove();
  });
  header.append(titleElement, spacer, icon);
  const body = document.createElement("div");
  body.className = "m-4 font-inter pb-7";
  const bodyFragment = document.createDocumentFragment();
  makeBody(bodyFragment);
  body.appendChild(bodyFragment);
  dialog.append(header, body);
  if (actions) {
    const footer = document.createElement("div");
    footer.className =
      "border-t-1 border-grey-200 pt-2 flex justify-end space-x-1";
    for (let action of actions) {
      addActionButton(footer, action.title, () =>
        action.handler(
          (result) => {
            dialog.close();
            dialog.remove();
            if (callback) {
              callback(result);
            }
          },
          (reason) => {
            dialog.close();
            dialog.remove();
            showErrorDialog(reason);
          }
        )
      );
    }
    addActionButton(footer, "Cancel", () => {
      dialog.close();
      dialog.remove();
    });
    dialog.append(footer);
  }
  document.body.appendChild(dialog);
  dialog.showModal();
}

export function showAlertDialog(
  title: string,
  text: string,
  additionalContent?: Node
) {
  showDialog(title, (body) => {
    const textElement = document.createElement("p");
    textElement.className = "mb-2";
    textElement.innerText = text;
    body.appendChild(textElement);
    if (additionalContent) {
      body.appendChild(additionalContent);
    }
  });
}

export function showErrorDialog(text: string, additionalContent?: Node) {
  showAlertDialog("Error", text, additionalContent);
}

export abstract class FormField<Type> {
  title: string;
  required?: boolean;
  resultProperty: string;
  validationCell?: Node;

  constructor(title: string, resultProperty: string, required?: boolean) {
    this.title = title;
    this.resultProperty = resultProperty;
    this.required = required;
  }

  abstract layoutInput(container: Node): void;

  abstract getValue(): Type | null;

  validate() {
    this.clearValidation();
    const value = this.getValue();
    let valid = true;
    if (this.required && value == null) {
      valid = false;
      this.addValidationError("This field is required.");
    }
    return valid;
  }

  private clearValidation() {
    while (this.validationCell?.firstChild) {
      this.validationCell.lastChild?.remove();
    }
  }

  private addValidationError(message: string) {
    const div = makeTextDiv(message);
    div.className = "text-12 text-red";
    this.validationCell?.appendChild(div);
  }
}

export class TextField extends FormField<string> {
  input?: HTMLInputElement;

  constructor(title: string, resultProperty: string, required?: boolean) {
    super(title, resultProperty, required);
  }

  layoutInput(container: Node) {
    const input = document.createElement("input");
    input.type = "text";
    input.className = "border border-grey-300 w-48";
    this.input = input;
    container.appendChild(this.input);
  }

  getValue() {
    return this.input == null ||
      this.input.value == null ||
      !this.input.value.length
      ? null
      : this.input.value;
  }
}

export class DropdownField<FieldType> extends FormField<FieldType> {
  options: Map<string, FieldType | null>;
  selectedValue: FieldType | null = null;
  nullLabel?: string;
  defaultLabel?: string;

  constructor(
    title: string,
    options: Map<string, FieldType | null>,
    resultProperty: string,
    required?: boolean,
    nullLabel?: string,
    defaultLabel?: string
  ) {
    super(title, resultProperty, required);
    this.options = options;
    this.nullLabel = nullLabel;
    this.defaultLabel = defaultLabel;
    if (defaultLabel) {
      this.selectedValue = options.get(defaultLabel) || null;
    }
  }

  layoutInput(container: Node) {
    const dropdownOptions: DropdownOption[] = [];
    const nullText = this.nullLabel || (this.required ? "SELECT" : "None");
    dropdownOptions.push(
      new BasicDropdownOption(nullText, () => {
        this.selectedValue = null;
      })
    );
    for (let key of this.options.keys()) {
      dropdownOptions.push(
        new BasicDropdownOption(key, () => {
          const value = this.options.get(key);
          this.selectedValue = value == null ? null : value;
        })
      );
    }
    const dropdown = new Dropdown(
      dropdownOptions,
      true,
      undefined,
      this.defaultLabel || nullText
    );
    const element = dropdown.getContainerTag();
    element.classList.add("w-48");
    container.appendChild(element);
  }

  getValue() {
    return this.selectedValue;
  }
}

export function showFormDialog(
  title: string,
  fields: FormField<any>[],
  callback: (result: any) => void
) {
  showDialog(
    title,
    (body) => {
      const table = document.createElement("table");
      const tbody = table.createTBody();
      for (let field of fields) {
        const inputRow = tbody.insertRow();
        const labelCell = inputRow.insertCell();
        labelCell.textContent = field.title + (field.required ? "*" : "") + ":";
        const inputCell = inputRow.insertCell();
        field.layoutInput(inputCell);
        const validationRow = tbody.insertRow();
        validationRow.insertCell();
        field.validationCell = validationRow.insertCell();
      }
      body.appendChild(table);
    },
    [
      {
        title: "Submit",
        handler(resolve, reject) {
          const result: any = {};
          let success = true;
          for (let field of fields) {
            if (!field.validate()) {
              success = false;
            }
            result[field.resultProperty] = field.getValue();
          }
          if (success) {
            resolve(result);
          }
        },
      },
    ],
    callback
  );
}
