describe("Operators.NullishCoalescing", () => {
  it("test null", () => {
    expect(null ?? 2).toBe(2);
  });

  it("test undefined", () => {
    expect(undefined ?? 2).toBe(2);
  });

  it("test 0", () => {
    expect(0 ?? 2).toBe(0);
  });

  it("test false", () => {
    expect(false ?? 2).toBe(false);
  });

  it("test empty string", () => {
    expect("" ?? 2).toBe("");
  });

  it("test object", () => {
    expect({} ?? 2).toBe({});
  });
});
