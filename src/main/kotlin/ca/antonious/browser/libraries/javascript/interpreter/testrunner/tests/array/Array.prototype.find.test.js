describe("Array.prototype.find", () => {
  it("returns element passing callback condition", () => {
    expect([].find(() => true)).toBeUndefined();
    expect([1, 2, 3].find(() => false)).toBeUndefined();
    expect([1, 2, 3].find(() => true)).toBe(1);
  });

  it("calls callback with correct element for each item", () => {
    expect([1, 2, 3].find((element) => element > 2)).toBe(3);
  });

  it("calls callback with correct index for each item", () => {
    expect([1, 2, 3].find((element, index) => index == 1)).toBe(2);
  });

  it("calls callback with correct array for each item", () => {
    const testArray = [1, 2, 3];
    const passedArrays = [];

    testArray.filter(
      (element, index, array) => passedArrays.push(array),
      false
    );

    expect(passedArrays).toBe([testArray, testArray, testArray]);
  });

  it("supports passing explicit this", () => {
    const object = { a: 2 };
    const passedThisBindings = [];

    [1, 2, 3].find(function () {
      passedThisBindings.push(this);
      return false;
    }, object);

    expect(passedThisBindings).toBe([object, object, object]);
  });
});
