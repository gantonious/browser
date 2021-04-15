describe("Array.prototype.splice", () => {
  test("removes and returns no elements from this array if no arguments are passed", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice()).toBe([]);
    expect(array).toBe([1, 2, 3, 4, 5]);
  });

  test("removes and returns no elements from this array if start > array.length", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice(30)).toBe([]);
    expect(array).toBe([1, 2, 3, 4, 5]);
  });

  test("removes and returns all elements if start < -array.length", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice(-30)).toBe([1, 2, 3, 4, 5]);
    expect(array).toBe([]);
  });

  test("removes and returns elements starting from start if start < array.length", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice(2)).toBe([3, 4, 5]);
    expect(array).toBe([1, 2]);
  });

  test("removes and returns elements starting from start is negative and abs(start) < array.length", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice(-2)).toBe([4, 5]);
    expect(array).toBe([1, 2, 3]);
  });

  test("removes and returns no elements if delete count is <= 0", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice(0, 0)).toBe([]);
    expect(array).toBe([1, 2, 3, 4, 5]);

    expect(array.splice(0, -2)).toBe([]);
    expect(array).toBe([1, 2, 3, 4, 5]);
  });

  test("removes and returns remaining elements if delete count > array.length - start", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice(1, 30)).toBe([2, 3, 4, 5]);
    expect(array).toBe([1]);
  });

  test("removes and returns delete count elements if delete count < array.length - start", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice(1, 2)).toBe([2, 3]);
    expect(array).toBe([1, 4, 5]);
  });

  test("after deleted elements adds additional passed arguments at start position", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice(1, 2, 10, 11, 12)).toBe([2, 3]);
    expect(array).toBe([1, 10, 11, 12, 4, 5]);
  });

  test("if start > array.length additional passed arguments are added to end of array", () => {
    let array = [1, 2, 3, 4, 5];
    expect(array.splice(10, 5, 10, 11, 12)).toBe([]);
    expect(array).toBe([1, 2, 3, 4, 5, 10, 11, 12]);
  });
});
