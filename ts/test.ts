/// <reference types="jquery" />

export function getText(name: string) {
  return "Hello " + name + "!";
}

export function setText() {
  $('#test2').text('Hello again!');
}
