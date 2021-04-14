describe("Array.prototype.slice", () => {
  test("returns full array if no arguments are passed", () => {
    expect([1, 2, 3, 4, 5].slice()).toBe([1, 2, 3, 4, 5]);
  });

  test("returns array starting from start index if no end index is passed", () => {
    expect([1, 2, 3, 4, 5].slice(2)).toBe([3, 4, 5]);
  });

  test("returns array starting from start index and ending at end index - 1", () => {
    expect([1, 2, 3, 4, 5].slice(2, 4)).toBe([3, 4]);
  });

  test("returns full array if start and end index match that of this array", () => {
    expect([1, 2, 3, 4, 5].slice(0, 5)).toBe([1, 2, 3, 4, 5]);
  });

  test("returns an empty array if start index > end index", () => {
    expect([1, 2, 3, 4, 5].slice(4, 3)).toBe([]);
  });

  test("returns array offset from end if negative start index is passed", () => {
    expect([1, 2, 3, 4, 5].slice(-3)).toBe([3, 4, 5]);
  });

  test("returns array offset from end if negative start index and end index is passed", () => {
    expect([1, 2, 3, 4, 5].slice(-3, -1)).toBe([3, 4]);
  });
});
