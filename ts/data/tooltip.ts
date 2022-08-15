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
      this.tooltipContainer.replaceChildren();
      this.tooltipContainer.appendChild(contents.cloneNode(true));
      this.tooltipContainer.style.top = targetY + "px";
      this.tooltipContainer.style.left = targetX + "px";
      this.update();
    };
    target.onmouseleave = () => {
      this.mouseOnIcon = false;
      this.update();
    };
    this.tooltipContainer.onmouseenter = () => {
      this.mouseOnToolTip = true;
      this.update();
    };
    this.tooltipContainer.onmouseleave = () => {
      this.mouseOnToolTip = false;
      this.update();
    };
    window.addEventListener("onscroll", () => {
      this.tooltipContainer.remove();
    });
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

  private update = () => {
    if (this.mouseOnIcon || this.mouseOnToolTip) {
      this.tooltipContainer.classList.remove("invisible");
    } else {
      this.tooltipContainer.classList.add("invisible");
    }
  };

  // https://stackoverflow.com/questions/442404/retrieve-the-position-x-y-of-an-html-element
  private getOffset(el: HTMLElement) {
    const rect = el.getBoundingClientRect();
    return {
      left: rect.left + window.scrollX,
      bottom: rect.bottom + window.scrollY,
    };
  }
}

export function makeTooltip(target: HTMLElement, contents: Node) {
  const tooltip = new Tooltip(target, contents);
  return target;
}
