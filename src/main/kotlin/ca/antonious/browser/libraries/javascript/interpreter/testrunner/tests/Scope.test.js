describe("Scopes", () => {
  it("nested scopes can access variable in closest parent scope", () => {
    let test = 3;

    if (true) {
      if (true) {
        expect(test).toBe(3);
      }
    }
  });

  it("nested scopes can update variable in closest parent scope", () => {
    let test = 3;

    if (true) {
      if (true) {
        test = 4;
      }
    }

    expect(test).toBe(4);
  });
});
