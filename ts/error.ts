import { AttributeDefinition, AttributeList } from "./component/attribute-list";

interface ErrorData {
  status: string;
  reason: string;
  message?: string;
}

const errorAttributes: AttributeDefinition<ErrorData>[] = [
  {
    title: "Code",
    addContents: (errorData, fragment) => {
      fragment.textContent = errorData.status || "N/A";
    },
  },
  {
    title: "Error",
    addContents: (errorData, fragment) => {
      fragment.textContent = errorData.reason;
    },
  },
  {
    title: "Message",
    addContents: (errorData, fragment) => {
      if (errorData.message !== undefined) {
        fragment.textContent = errorData.message;
      }
    },
  },
];

document.addEventListener("DOMContentLoaded", () => {
  const container = document.getElementById("error-attribute-list");

  if (container) {
    const errorData: ErrorData = {
      status: container.dataset.status || "Unknown",
      reason: container.dataset.reason || "Unknown Reason",
      message: container.dataset.message,
    };

    // Remove the Message attribute definition if the message is not available
    const filteredAttributes = errorAttributes.filter((attr) => {
      return !(attr.title === "Message" && errorData.message === undefined);
    });

    new AttributeList<ErrorData>(
      "error-attribute-list",
      errorData,
      filteredAttributes
    );
  } else {
    console.error("Error container not found");
  }
});
