export class Tooltip {
  private static instance: Tooltip;
  private mouseOnIcon: boolean;
  private mouseOnToolTip: boolean;
  private tooltipContainer: HTMLElement;
  private activeTarget?: HTMLElement;

  private constructor() {
    this.mouseOnIcon = false;
    this.mouseOnToolTip = false;
    this.tooltipContainer = this.getTootipContainer();
    this.tooltipContainer.onmouseenter = () => {
      this.mouseOnToolTip = true;
      this.update();
    };
    this.tooltipContainer.onmouseleave = () => {
      this.mouseOnToolTip = false;
      this.update();
    };
  }

  private getTootipContainer = () => {
    let div = document.getElementById("tooltip");
    if (div) {
      return div;
    }
    div = document.createElement("div");
    div.id = "tooltip";
    div.classList.add(
      "absolute",
      "block",
      "-ml-8",
      "-mt-1",
      "bg-white",
      "w-fit",
      "h-fit",
      "p-2",
      "border-2",
      "border-dotted",
      "border-green-200",
      "font-inter",
      "text-14",
      "font-medium",
      "invisible"
    );
    document.body.appendChild(div);
    return div;
  };

  // https://stackoverflow.com/questions/442404/retrieve-the-position-x-y-of-an-html-element
  // used to get the position of an element relative to a scrolled window
  private getOffset(el: HTMLElement) {
    const rect = el.getBoundingClientRect();
    return {
      left: rect.left + window.scrollX,
      bottom: rect.bottom + window.scrollY,
    };
  }

  private update() {
    if (!this.activeTarget) {
      throw new Error("Tooltip called with no target elements");
    }
    if (this.mouseOnIcon || this.mouseOnToolTip) {
      this.tooltipContainer.classList.remove("invisible");
      this.activeTarget.classList.add("text-green-200");
    } else {
      this.tooltipContainer.classList.add("invisible");
      this.activeTarget.classList.remove("text-green-200");
    }
  }
  public static getInstance() {
    if (!Tooltip.instance) {
      Tooltip.instance = new Tooltip();
    }
    return Tooltip.instance;
  }

  public addTarget(target: HTMLElement, contents: Node) {
    target.onmouseenter = () => {
      this.mouseOnIcon = true;
      this.activeTarget = target;
      const targetHitBox = this.getOffset(target);
      const targetY = targetHitBox.bottom;
      const targetX = targetHitBox.left;
      // node needs to be cloned since adding to DOM removes node argument
      this.tooltipContainer.replaceChildren(contents.cloneNode(true));
      this.tooltipContainer.style.top = targetY + "px";
      this.tooltipContainer.style.left = targetX + "px";
      this.update();
    };
    target.onmouseleave = () => {
      this.mouseOnIcon = false;
      this.update();
    };
  }
}
