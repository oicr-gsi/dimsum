export function post(url: string, body: any) {
  let headers: any = {
    "Content-Type": "application/json",
  };
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

export function get(url: string, params?: Record<string, string>) {
  let headers: any = {
    "Content-Type": "application/json",
  };
  return fetch(url + (params ? new URLSearchParams(params).toString() : ""), {
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
