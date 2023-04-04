export interface AttributeDefinition<Type> {
  title: string;
  addContents: (object: Type, fragment: DocumentFragment) => void;
}

export class AttributeList<Type> {
  container: HTMLElement;

  constructor(
    containerId: string,
    object: Type,
    attributes: AttributeDefinition<Type>[]
  ) {
    const container = document.getElementById(containerId);
    if (container === null) {
      throw Error(`Container ID "${containerId}" not found on page`);
    }
    this.container = container;
    container.replaceChildren();

    const table = document.createElement("table");
    const tbody = table.createTBody();
    table.classList.add(
      "attribute-list",
      "w-full",
      "text-14",
      "text-black",
      "font-medium",
      "font-inter",
      "border-separate",
      "border-spacing-0",
      "border-grey-200",
      "border-2",
      "rounded-xl",
      "overflow-hidden",
      "mt-8"
    );
    attributes.forEach((attribute, i) => {
      const row = tbody.insertRow();
      const th = document.createElement("th");
      th.classList.add(
        "p-4",
        "text-white",
        "font-semibold",
        "bg-grey-300",
        "text-left",
        "align-text-top",
        "whitespace-nowrap"
      );
      if (i === 0) {
        th.classList.add("w-0", "min-w-[200px]");
      } else {
        th.classList.add("border-grey-200", "border-t-1");
      }
      th.append(document.createTextNode(attribute.title));

      const td = row.insertCell();
      td.classList.add(
        "p-4",
        "border-grey-200",
        "border-l-1",
        "text-left",
        "align-text-top"
      );
      if (i > 0) {
        td.classList.add("border-t-1");
      }
      const fragment = document.createDocumentFragment();
      attribute.addContents(object, fragment);
      td.appendChild(fragment);

      row.append(th, td);
    });

    container.appendChild(table);
  }
}
