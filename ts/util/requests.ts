import {
  showConfirmDialog,
  showErrorDialog,
  showWorkingDialog,
} from "../component/dialog";

const STATUS_NO_CONTENT = 204;
const STATUS_FORBIDDEN = 403;

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
            if (response.status === STATUS_FORBIDDEN) {
              showSessionTimeoutError();
              return;
            }
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

function showSessionTimeoutError() {
  showConfirmDialog("Error", "Request failed. Your session may have expired.", {
    title: "Refresh",
    handler() {
      location.reload();
    },
  });
}

export function postDownload(url: string, body: any, workingText: string) {
  const closeDialog = showWorkingDialog(workingText);
  doPost(url, body, {
    Accept: "application/octet-stream",
  }).then((response) => {
    closeDialog();
    if (response.ok) {
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
    } else {
      if (response.status === STATUS_FORBIDDEN) {
        showSessionTimeoutError();
      } else {
        showErrorDialog("Unknown error.");
      }
    }
  });
}

export function postNavigate(url: string, data: any, newTab: boolean) {
  const form = document.createElement("form");
  form.style.display = "none";
  form.action = url;
  form.method = "POST";
  form.target = "_blank";
  const input = document.createElement("input");
  input.type = "hidden";
  input.name = "data";
  input.value = JSON.stringify(data);
  form.appendChild(input);
  document.body.appendChild(form);
  form.submit();
  form.remove();
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
