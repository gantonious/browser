describe("Array.prototype.filter", () => {
  it("returns array containing elements that pass callback condition", () => {
    expect([].filter(() => true)).toBe([]);
    expect([1, 2, 3].filter(() => false)).toBe([]);
    expect([1, 2, 3].filter(() => true)).toBe([1, 2, 3]);
  });

  it("calls callback with correct element for each item", () => {
    expect([1, 2, 3].filter((element) => element < 2)).toBe([1]);
  });

  it("calls callback with correct index for each item", () => {
    expect([1, 2, 3].filter((element, index) => index < 2)).toBe([1, 2]);
  });

  it("calls callback with correct array for each item", () => {
    const testArray = [1, 2, 3];

    expect(
      testArray.filter((element, index, array) => array == testArray)
    ).toBe(testArray);
  });

  it("supports passing explicit this", () => {
    const object = { a: 2 };
    const passedThisBindings = [];

    [1, 2, 3].filter(function () {
      passedThisBindings.push(this);
      return 0;
    }, object);

    expect(passedThisBindings).toBe([object, object, object]);
  });
});
