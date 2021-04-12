describe("Array.prototype.map", () => {
  test("returns output of callback", () => {
    expect([].map(() => 0)).toBe([]);
    expect([1, 2, 3].map(() => 0)).toBe([0, 0, 0]);
  });

  test("calls callback with correct element for each item", () => {
    expect([1, 2, 3].map((element) => element * 2)).toBe([2, 4, 6]);
  });

  test("calls callback with correct index for each item", () => {
    expect([1, 2, 3].map((element, index) => index)).toBe([0, 1, 2]);
  });

  test("calls callback with correct array for each item", () => {
    const testArray = [1, 2, 3];

    expect(testArray.map((element, index, array) => array)).toBe([
      testArray,
      testArray,
      testArray,
    ]);
  });

  test("supports passing explicit this", () => {
    const object = { a: 2 };
    const passedThisBindings = [];

    [1, 2, 3].map(function () {
      passedThisBindings.push(this);
      return 0;
    }, object);

    expect(passedThisBindings).toBe([object, object, object]);
  });
});
