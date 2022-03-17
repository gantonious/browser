describe("Array.prototype.shift", () => {
  it("returns undefined if array is empty", () => {
    expect([].shift()).toBeUndefined();
  });

  it("returns first element of non empty array and removes it from the array", () => {
    const array = [1, 2, 3];
    expect(array.shift()).toBe(1);
    expect(array).toBe([2, 3]);
  });
});
