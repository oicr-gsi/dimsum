export function post(url: string, body: any, extraHeaders?: any) {
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

export function postDownload(url: string, body: any) {
  post(url, body, {
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
