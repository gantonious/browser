describe("Array.prototype.flatMap", () => {
  test("returns flattened output of callback", () => {
    expect([].flatMap(() => [])).toBe([]);
    expect([[]].flatMap(() => [])).toBe([]);
    expect([1, 2, 3].flatMap(() => [1, 2])).toBe([1, 2, 1, 2, 1, 2]);
  });

  test("calls callback with correct element for each item and only flattens returned array", () => {
    expect([1, [2, [3, 4]], 5].flatMap((element) => element)).toBe([
      1,
      2,
      [3, 4],
      5,
    ]);
  });

  test("calls callback with correct index for each item", () => {
    expect([1, 2, 3].flatMap((element, index) => [element, index])).toBe([
      1,
      0,
      2,
      1,
      3,
      2,
    ]);
  });

  test("calls callback with correct array for each item", () => {
    const testArray = [1, 2, 3];

    expect(
      testArray.flatMap((element, index, array) => [element, array])
    ).toBe([1, testArray, 2, testArray, 3, testArray]);
  });

  test("supports passing explicit this", () => {
    const object = { a: 2 };
    const passedThisBindings = [];

    [1, 2, 3].flatMap(function () {
      passedThisBindings.push(this);
      return [];
    }, object);

    expect(passedThisBindings).toBe([object, object, object]);
  });
});
