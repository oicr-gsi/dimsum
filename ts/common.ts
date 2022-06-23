/// <reference types="jquery" />

function setCsrfToken() {
  $(function () {
    let token: string = $("meta[name='_csrf']").attr("content")!;
    let header: string = $("meta[name='_csrf_header']").attr("content")!;
    $(document).ajaxSend(function (e, xhr, options) {
      xhr.setRequestHeader(header, token);
    });
  });
}

setCsrfToken();
