describe("Operators.NullishCoalescing", () => {
  test("test null", () => {
    expect(null ?? 2).toBe(2);
  });

  test("test undefined", () => {
    expect(undefined ?? 2).toBe(2);
  });

  test("test 0", () => {
    expect(0 ?? 2).toBe(0);
  });

  test("test false", () => {
    expect(false ?? 2).toBe(false);
  });

  test("test empty string", () => {
    expect("" ?? 2).toBe("");
  });

  test("test object", () => {
    expect({} ?? 2).toBe({});
  });
});
