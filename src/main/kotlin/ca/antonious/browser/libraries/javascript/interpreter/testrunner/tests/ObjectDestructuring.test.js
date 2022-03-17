describe("ObjectDestructuring", () => {
  it("supports empty objects", () => {
    let { a, ...rest } = {};
    expect(a).toBeUndefined();
    expect(rest).toBe([]);
  });

  it("supports rest values", () => {
    let { a, ...rest } = { a: 2, b: 4, c: 4 };
    expect(a).toBe(2);
    expect(rest).toBe({ b: 4, c: 4 });
  });

  it("supports default values", () => {
    let { a = "default" } = { b: 4, c: 4 };
    expect(a).toBe("default");
  });

  it("supports renaming values", () => {
    let { a: renamed } = { a: 2, b: 4, c: 4 };
    expect(a).toBeUndefined();
    expect(renamed).toBe(2);
  });

  it("supports renaming and default values", () => {
    let { a: b = "default" } = { b: 4, c: 4 };
    expect(a).toBeUndefined();
    expect(b).toBe("default");
  });

  it("supports nested objects", () => {
    let {
      a: { b },
    } = { a: { b: 1, c: [2, 3] } };

    expect(b).toBe(1);
  });

  it("supports nested arrays", () => {
    let {
      a: [a, b],
    } = { a: [2, 3] };

    expect(a).toBe(2);
    expect(b).toBe(3);
  });

  it("supports defaults for nested object", () => {
    let { a: { b } = { b: 2 } } = { c: 3 };
    expect(b).toBe(2);
  });
});
