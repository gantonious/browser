describe("Operators.Ternary", () => {
  test("returns first expression if condition is truthy", () => {
    expect(true ? 0 : 1).toBe(0);
  });

  test("returns second expression if condition is truthy", () => {
    expect(false ? 0 : 1).toBe(1);
  });

  test("supports nested ternary expression", () => {
    // prettier-ignore
    expect(true ? false ? 0 : 1 : true ? 2 : 3).toBe(1);
    // prettier-ignore
    expect(false ? false ? 0 : 1 : true ? 2 : 3).toBe(2);
  });

  test("supports nested assignment", () => {
    expect(true ? b=1 : 0).toBe(1);
  });
});
