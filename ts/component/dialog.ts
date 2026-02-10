import { addActionButton, makeIcon, makeTextDiv } from "../util/html-utils";
import { BasicDropdownOption, Dropdown, DropdownOption } from "./dropdown";

interface FormAction {
  title: string;
  handler: (
    resolve: (value: any) => void,
    reject: (reason: string) => void,
  ) => void;
}

/**
 * Creates and displays a dialog
 *
 * @param title
 * @param makeBody function to add contents to the dialog
 * @param closable whether to include an X button to dismiss the dialog
 * @param actions buttons to include
 * @param callback what to do after the dialog is closed
 * @returns close function that can be used to dismiss the dialog
 */
function showDialog(
  title: string,
  makeBody: (body: Node) => void,
  closable: boolean = true,
  actions?: FormAction[],
  callback?: (result?: any) => void,
): () => void {
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
  header.append(titleElement, spacer);

  if (closable) {
    const icon = makeIcon("xmark");
    icon.classList.add(
      "fa-solid",
      "fa-xmark",
      "text-grey-200",
      "hover:text-green-200",
      "text-24",
      "m-2",
      "mx-4",
      "cursor-pointer",
    );
    icon.addEventListener("click", () => {
      dialog.close();
      dialog.remove();
    });
    header.append(icon);
  } else {
    dialog.setAttribute("closedBy", "none");
  }

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
          },
        ),
      );
    }
    addActionButton(footer, "Cancel", () => {
      dialog.close();
      dialog.remove();
    });
    dialog.append(footer);
  } else {
    if (callback) {
      dialog.addEventListener("close", () => callback());
    }
  }
  document.body.appendChild(dialog);
  dialog.showModal();

  return () => {
    dialog.close();
    dialog.remove();
  };
}

export function showWorkingDialog(text: string) {
  return showDialog("Working...", (body) => addText(body, text), false);
}

export function showAlertDialog(
  title: string,
  text: string,
  additionalContent?: Node,
  callback?: () => void,
) {
  showDialog(
    title,
    (body) => {
      addText(body, text);
      if (additionalContent) {
        body.appendChild(additionalContent);
      }
    },
    true,
    undefined,
    callback,
  );
}

function addText(body: Node, text: string) {
  const textElement = document.createElement("p");
  textElement.className = "mb-2";
  textElement.innerText = text;
  body.appendChild(textElement);
}

export function showConfirmDialog(
  title: string,
  text: string,
  action: FormAction,
) {
  showDialog(title, (body) => addText(body, text), true, [action]);
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

export class CheckboxField extends FormField<boolean> {
  input?: HTMLInputElement;
  checked: boolean;

  constructor(title: string, resultProperty: string, checked?: boolean) {
    super(title, resultProperty, false);
    this.checked = checked || false;
  }

  layoutInput(container: Node): void {
    const input = document.createElement("input");
    input.type = "checkbox";
    input.checked = this.checked;
    this.input = input;
    container.appendChild(this.input);
  }

  getValue(): boolean | null {
    return this.input ? this.input.checked : null;
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
    defaultLabel?: string,
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
    if (!this.required || this.nullLabel || !this.defaultLabel) {
      dropdownOptions.push(
        new BasicDropdownOption(nullText, () => {
          this.selectedValue = null;
        }),
      );
    }
    for (let key of this.options.keys()) {
      dropdownOptions.push(
        new BasicDropdownOption(key, () => {
          const value = this.options.get(key);
          this.selectedValue = value == null ? null : value;
        }),
      );
    }
    const dropdown = new Dropdown(
      dropdownOptions,
      true,
      undefined,
      this.defaultLabel || nullText,
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
  submitLabel: string,
  callback: (result: any) => void,
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
    true,
    [
      {
        title: submitLabel,
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
    callback,
  );
}

export function showDownloadOptionsDialog(
  callback: (result: any) => void,
  additionalFields?: FormField<any>[],
) {
  const formatOptions = new Map<string, any>([
    [
      "Excel",
      {
        format: "excel",
      },
    ],
    [
      "CSV with headings",
      {
        format: "csv",
        includeHeadings: true,
      },
    ],
    [
      "CSV, no headings",
      {
        format: "csv",
        includeHeadings: false,
      },
    ],
    [
      "TSV with headings",
      {
        format: "tsv",
        includeHeadings: true,
      },
    ],
    [
      "TSV, no headings",
      {
        format: "tsv",
        includeHeadings: false,
      },
    ],
  ]);
  let fields: FormField<any>[] = [
    new DropdownField(
      "Format",
      formatOptions,
      "formatOptions",
      true,
      undefined,
      "Excel",
    ),
  ];
  if (additionalFields) {
    fields = fields.concat(additionalFields);
  }
  showFormDialog("Download Options", fields, "Download", callback);
}
