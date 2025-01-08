export function assertDefined<T>(value?: T): asserts value is T {
  if (value === undefined) {
    throw new Error("Value unexpectedly undefined");
  }
}

export function assertNotNull<Type>(object: Type | null) {
  if (object === null) {
    throw new Error("Unexpected null value");
  }
  return object;
}

export function assertRequired<T>(value?: T): asserts value is NonNullable<T> {
  if (value == null) {
    throw new Error("Required value missing");
  }
}

export function nullOrUndefined(value: unknown): value is null | undefined {
  return value === undefined || value === null;
}

export function nullIfUndefined(value: any) {
  return value === undefined ? null : value;
}
