const STATUS_NO_CONTENT = 204;

function doPost(url: string, body: any, extraHeaders?: any) {
  const headers = extraHeaders || {};
  headers["Content-Type"] = "application/json";

  const csrfHeader = getMetaContent("_csrf_header");
  const csrfValue = getMetaContent("_csrf_token");
  if (csrfHeader && csrfValue) {
    headers[csrfHeader] = csrfValue;
  }

  return fetch(url, {
    method: "POST",
    headers: headers,
    body: JSON.stringify(body),
  });
}

export function post(url: string, body: any) {
  return new Promise(
    (resolve: (result: any) => void, reject: (reason: string) => void) => {
      doPost(url, body)
        .then((response) => {
          if (response.ok) {
            if (response.status === STATUS_NO_CONTENT) {
              resolve(null);
              return;
            }
            response
              .json()
              .then((data) => resolve(data))
              .catch(() => reject("Unknown error"));
          } else {
            response
              .json()
              .then((errorJson) => reject(errorJson.message))
              .catch(() => reject("Unknown error"));
          }
        })
        .catch(() => {
          reject("Network error");
        });
    }
  );
}

export function postDownload(url: string, body: any) {
  doPost(url, body, {
    Accept: "application/octet-stream",
  }).then((response) => {
    const disposition = response.headers.get("Content-Disposition");
    if (!disposition) {
      throw new Error("Server did not set content disposition header");
    }
    const regexResult = /filename=(.*)$/.exec(disposition);
    if (!regexResult) {
      throw new Error("Server did not send filename");
    }
    const filename = regexResult[1];
    response.blob().then((blob) => {
      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = filename;
      a.click();
    });
  });
}

export function postNewWindow(url: string, body: any, targetWindow: Window) {
  doPost(url, body, {
    Accept: "application/json",
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to fetch data: " + response.statusText);
      }
      return response.json(); // parse JSON response directly
    })
    .then((jsonData) => {
      if (targetWindow) {
        targetWindow.postMessage({ type: "jsonData", content: jsonData }, "*");
      } else {
        alert(
          "Failed to open the target window. Please allow popups for this site."
        );
      }
    })
    .catch((error) => {
      if (error.message.includes("Failed to fetch data")) {
        alert(error.message);
      } else {
        alert("Unknown Error: " + error.message);
      }
    });
}

export function get(url: string, params?: Record<string, string>) {
  let headers: any = {
    "Content-Type": "application/json",
  };
  let fullUrl = params
    ? `${url}?${new URLSearchParams(params).toString()}`
    : url;
  return fetch(fullUrl, {
    method: "GET",
    headers: headers,
  });
}

function getMetaContent(name: string) {
  const tag = document.querySelector(`meta[name=${name}]`);
  if (!tag) {
    return null;
  }
  return tag.getAttribute("content");
}
