describe("Array.prototype.forEach", () => {
  const testArray = [1, "2", true];

  test("returns undefined", () => {
    expect([].forEach(() => {})).toBeUndefined();
    expect([1, 2, 3].forEach(() => {})).toBeUndefined();
  });

  test("never invokes callback for an empty array", () => {
    const timesCalled = 0;
    [].forEach(() => timesCalled++);
    expect(timesCalled).toBe(0);
  });

  test("calls callback once for each item", () => {
    const timesCalled = 0;
    testArray.forEach(() => timesCalled++);
    expect(timesCalled).toBe(3);
  });

  test("calls callback with correct element for each item", () => {
    const passedElements = [];
    testArray.forEach(passedElements.push);
    expect(passedElements).toBe(testArray);
  });

  test("calls callback with correct index for each item", () => {
    const passedIndices = [];
    testArray.forEach((element, index) => passedIndices.push(index));
    expect(passedIndices).toBe([0, 1, 2]);
  });

  test("calls callback with correct array for each item", () => {
    const passedArrays = [];
    testArray.forEach((element, index, array) => passedArrays.push(array));
    expect(passedArrays).toBe([testArray, testArray, testArray]);
  });

  test("supports passing explicit this", () => {
    const object = { a: 2 };
    const passedThisBindings = [];

    testArray.forEach(function () {
      passedThisBindings.push(this);
    }, object);

    expect(passedThisBindings).toBe([object, object, object]);
  });
});
