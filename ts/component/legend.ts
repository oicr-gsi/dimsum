import { qcStatuses } from "../data/qc-status";
import { makeIcon } from "../util/html-utils";

const legendId = "legend-container";
class Legend {
  private container: HTMLElement;
  constructor() {
    // outer container
    this.container = document.createElement("div");
    this.container.className =
      "text-black block flex flex-col bg-white rounded-xl fixed border-2 border-grey-200 z-40 drop-shadow-lg invisible";
    this.container.id = legendId;
    // header bar w/title and close window icon
    const header = document.createElement("div");
    header.className =
      "flex flex-row items-center border-b-1 border-grey-200 cursor-move";
    const title = document.createElement("span");
    title.className = "font-sarabun font-thin text-green-200 text-24 my-2 mx-4";
    title.innerHTML = "Legend";
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
      "z-50",
      "relative",
      "cursor-pointer"
    );
    icon.addEventListener("click", () => this.container.remove());
    header.appendChild(title);
    header.appendChild(spacer);
    header.appendChild(icon);
    // grid of legend labels
    const body = document.createElement("div");
    body.className = "m-4 grid grid-rows-4 grid-flow-col gap-2";
    for (const qcStatus of Object.values(qcStatuses)) {
      const labelContainer = document.createElement("div");
      labelContainer.className =
        "flex items-center space-x-2 bg-grey-100 rounded-md font-inter font-medium p-2 text-12";
      const icon = makeIcon(qcStatus.icon);
      icon.classList.add("text-black");
      const label = document.createElement("span");
      label.innerHTML = qcStatus.label;
      labelContainer.appendChild(icon);
      labelContainer.appendChild(label);
      body.appendChild(labelContainer);
    }

    this.container.append(header);
    this.container.append(body);

    // add dragging functionality
    this.makeDraggable(header);

    // centering window, setTimeout is required for offsetHeight and offsetWidth to load
    window.setTimeout(() => {
      this.container.style.top =
        (window.innerHeight - this.container.offsetHeight) / 2 + "px";
      this.container.style.left =
        (window.innerWidth - this.container.offsetWidth) / 2 + "px";
      this.container.classList.toggle("invisible");
    }, 0);
  }

  // A modified code snippet from https://codepen.io/marcusparsons/pen/NMyzgR
  private makeDraggable(windowContainerTop: HTMLElement) {
    //state
    let diffX = 0,
      diffY = 0,
      previousPosX = 0,
      previousPosY = 0;

    const dragMouseDown = (e: MouseEvent) => {
      // Prevent any default action on this element
      e.preventDefault();
      // Get the mouse cursor position and set the initial previous positions to begin
      previousPosX = e.clientX;
      previousPosY = e.clientY;
      // When the mouse is let go, call the closing event
      document.onmouseup = closeDragElement;
      // call a function whenever the cursor moves
      document.onmousemove = elementDrag;
    };

    const elementDrag = (e: MouseEvent) => {
      // Prevent any default action on this element
      e.preventDefault();

      // disable window moving if the cursor is out of window
      if (
        e.clientX < 0 ||
        e.clientX > window.innerWidth ||
        e.clientY < 0 ||
        e.clientY > window.innerHeight
      ) {
        return;
      }

      // Calculate the new cursor position by using the previous x and y positions of the mouse
      diffX = previousPosX - e.clientX;
      diffY = previousPosY - e.clientY;
      // Replace the previous positions with the new x and y positions of the mouse
      previousPosX = e.clientX;
      previousPosY = e.clientY;
      // Set the element's new position
      this.container.style.top =
        this.clampY(this.container.offsetTop - diffY) + "px";
      this.container.style.left =
        this.clampX(this.container.offsetLeft - diffX) + "px";
    };

    // move container if it gets clipped in window resizing
    const onResize = (event: UIEvent) => {
      const bottom = this.container.offsetHeight + this.container.offsetTop;
      const right = this.container.offsetWidth + this.container.offsetLeft;
      if (bottom > window.innerHeight) {
        this.container.style.top =
          Math.max(0, window.innerHeight - this.container.offsetHeight) + "px";
      }
      if (right > window.innerWidth) {
        this.container.style.left =
          Math.max(0, window.innerWidth - this.container.offsetWidth) + "px";
      }
    };

    const closeDragElement = () => {
      // Stop moving when mouse button is released and release events
      document.onmouseup = null;
      document.onmousemove = null;
    };

    windowContainerTop.onmousedown = dragMouseDown;
    window.onresize = onResize;
  }

  // clamp x-coord between 0 & window width
  private clampX(n: number) {
    return Math.min(
      Math.max(n, 0),
      // container width - window width
      window.innerWidth - this.container.offsetWidth
    );
  }
  // clamp y-coord between 0 and window height
  private clampY(n: number) {
    return Math.min(
      Math.max(n, 0),
      // container height - window height
      window.innerHeight - this.container.offsetHeight
    );
  }

  public getTag() {
    return this.container;
  }
}

export function toggleLegend() {
  const legendWindow = document.getElementById(legendId);
  if (!legendWindow) {
    const legendContainer = new Legend();
    document.body.appendChild(legendContainer.getTag());
  } else {
    legendWindow.remove();
  }
}
