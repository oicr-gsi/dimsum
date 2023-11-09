import { AttributeDefinition, AttributeList } from "./component/attribute-list";

interface ErrorData {
  status: string;
  reason: string;
  message: string;
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
      fragment.textContent = errorData.message;
    },
  },
];

document.addEventListener("DOMContentLoaded", () => {
  const container = document.getElementById("error-attribute-list");

  if (container) {
    const errorData: ErrorData = {
      status: container.dataset.status ?? "N/A",
      reason: container.dataset.reason ?? "Unknown Reason",
      message: container.dataset.message ?? "",
    };

    // Filter out the "Message" attribute if the message is not available
    const filteredAttributes = errorData.message
      ? errorAttributes
      : errorAttributes.filter((attr) => attr.title !== "Message");

    new AttributeList<ErrorData>(
      "error-attribute-list",
      errorData,
      filteredAttributes
    );
  } else {
    console.error("Error container not found");
  }
});
