describe("String.prototype.slice", () => {
  it("returns full string if no arguments are passed", () => {
    expect("Hello, world!".slice()).toBe("Hello, world!");
  });

  it("returns substring starting from start index if no end index is passed", () => {
    expect("Hello, world!".slice(7)).toBe("world!");
  });

  it("returns substring starting from start index and ending at end index - 1", () => {
    expect("Hello, world!".slice(7, 12)).toBe("world");
  });

  it("returns full string if start and end index match that of this string", () => {
    expect("Hello, world!".slice(0, 13)).toBe("Hello, world!");
  });

  it("returns substring offset from end if negative start index is passed", () => {
    expect("Hello, world!".slice(-6)).toBe("world!");
  });

  it("returns substring offset from end if negative start index and end index is passed", () => {
    expect("Hello, world!".slice(-6, -1)).toBe("world");
  });
});
