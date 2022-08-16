class Tooltip {
  private mouseOnIcon: boolean;
  private mouseOnToolTip: boolean;
  private tooltipContainer: HTMLElement;

  constructor(target: HTMLElement, contents: Node) {
    this.mouseOnIcon = false;
    this.mouseOnToolTip = false;
    this.tooltipContainer = this.getTootipContainer();
    target.onmouseenter = () => {
      this.mouseOnIcon = true;
      const targetY = this.getOffset(target).bottom;
      const targetX = this.getOffset(target).left;
      // node needs to be cloned since adding to DOM removes node argument
      this.tooltipContainer.replaceChildren(contents.cloneNode(true));
      this.tooltipContainer.style.top = targetY + "px";
      this.tooltipContainer.style.left = targetX + "px";
      update();
    };
    target.onmouseleave = () => {
      this.mouseOnIcon = false;
      update();
    };
    this.tooltipContainer.onmouseenter = () => {
      this.mouseOnToolTip = true;
      update();
    };
    this.tooltipContainer.onmouseleave = () => {
      this.mouseOnToolTip = false;
      update();
    };

    const update = () => {
      console.log("update");
      if (this.mouseOnIcon || this.mouseOnToolTip) {
        this.tooltipContainer.classList.remove("invisible");
        target.classList.add("text-green-200");
      } else {
        this.tooltipContainer.classList.add("invisible");
        target.classList.remove("text-green-200");
      }
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
      "font-14",
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
}

// A wrapper function since we don't use the tooltip class itself anywhere ATM
export function makeTooltip(target: HTMLElement, contents: Node) {
  const tooltip = new Tooltip(target, contents);
  return target;
}
