describe("Operators.Ternary", () => {
  it("returns first expression if condition is truthy", () => {
    expect(true ? 0 : 1).toBe(0);
  });

  it("returns second expression if condition is truthy", () => {
    expect(false ? 0 : 1).toBe(1);
  });

  it("supports nested ternary expression", () => {
    // prettier-ignore
    expect(true ? false ? 0 : 1 : true ? 2 : 3).toBe(1);
    // prettier-ignore
    expect(false ? false ? 0 : 1 : true ? 2 : 3).toBe(2);
  });

  it("supports nested assignment", () => {
    expect(true ? b=1 : 0).toBe(1);
  });
});
