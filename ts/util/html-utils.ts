// adds a header cell to a table header
export function addColumnHeader(
  thead: HTMLTableRowElement,
  header: string,
  firstColumn: boolean
) {
  const th = document.createElement("th");
  th.className =
    "p-4 text-white font-semibold bg-grey-300 text-left align-text-top" +
    (firstColumn ? "" : " border-grey-200 border-l-1");

  // allow line-wrapping on "/" character
  header.split("/").forEach((part, index, arr) => {
    th.appendChild(document.createTextNode(`${index > 0 ? "/" : ""}${part}`));
    if (index < arr.length - 1) {
      th.appendChild(document.createElement("wbr"));
    }
  });
  thead.appendChild(th);
}

// adds a cell to a table row
export function makeCell(tr: HTMLTableRowElement, firstColumn: boolean) {
  const td = tr.insertCell();
  td.className =
    "p-3 border-grey-200 border-t-1 text-left align-text-top" +
    (firstColumn ? "" : " border-l-1");
  return td;
}

// adds a styled link to an element
export function addLink(container: Node, text: string, url: string) {
  const a = document.createElement("a");
  a.setAttribute("href", url);
  a.className = "text-green-200 font-bold hover:underline";
  a.innerHTML = text;
  container.appendChild(a);
}

// adds a miso icon w/link to an element
export function addMisoIcon(container: HTMLElement, url: string) {
  const a = document.createElement("a");
  a.setAttribute("href", url);
  a.setAttribute("target", "_blank");
  a.setAttribute("rel", "noopener noreferrer");
  const img = document.createElement("img");
  img.className = "max-w-none";
  img.src = "/img/miso_logo.svg";
  img.alt = "View in MISO";
  img.title = "View in MISO";
  a.appendChild(img);
  container.appendChild(a);
}

// all supported cell statuses
export type CellStatus = "na" | "stopped" | "warning" | "error";

// map cell status to styles
const highlightClasses: Record<CellStatus, string[]> = {
  na: ["text-grey-300", "bg-grey-150"],
  stopped: ["bg-grey-100"],
  warning: ["bg-yellow"],
  error: ["bg-red"],
};

// shade cell backgrounds
export function shadeElement(element: HTMLElement, status?: CellStatus | null) {
  if (status) {
    element.classList.add(...highlightClasses[status]);
  }
}

// all supported text styles
export type TextStyle = "error";

// styles container text to match appropriate text style
export function styleText(element: HTMLElement, style?: TextStyle | null) {
  if (style === "error") {
    element.classList.add("text-red", "font-bold");
  }
}

// makes an icon with the corresponding Font Awesome name
export function makeIcon(name: string) {
  const icon = document.createElement("i");
  icon.className = `fa-solid fa-${name}`;
  return icon;
}

// makes an icon button with the corresponding Font Awesome name
export function addIconButton(container: HTMLElement, iconName: string) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = `fa-solid fa-${iconName} px-2 py-1 rounded-md bg-grey-100 hover:bg-green-200 hover:text-white disabled:bg-grey-100 disabled:text-grey-200`;
  container.appendChild(button);
  return button;
}

// makes a transparent button used to click out of user inputs
export function makeClickout() {
  const clickout = document.createElement("button");
  clickout.className =
    "bg-transparent fixed inset-0 w-full h-full cursor-default hidden z-10";
  return clickout;
}

// makes a composite element with a MISO icon and a possibly hyperlinked label
export function makeNameDiv(name: string, misoUrl: string, dimsumUrl?: string) {
  const div = document.createElement("div");
  div.className = "flex flex-row space-x-2 items-center";
  if (dimsumUrl) {
    addLink(div, name, dimsumUrl);
  } else {
    const nameSpan = document.createElement("span");
    nameSpan.innerText = name;
    div.appendChild(nameSpan);
  }
  addMisoIcon(div, misoUrl);
  return div;
}

export function addNaText(fragment: DocumentFragment) {
  fragment.appendChild(document.createTextNode("N/A"));
}

export function addTextDiv(text: string, container: Node) {
  const divContainer = document.createElement("div");
  divContainer.appendChild(document.createTextNode(text));
  container.appendChild(divContainer);
}

// transform given string to title case
export function toTitleCase(text: string) {
  return text.replace(/\w\S*/g, function (text) {
    return text.charAt(0).toUpperCase() + text.substring(1).toLowerCase();
  });
}
