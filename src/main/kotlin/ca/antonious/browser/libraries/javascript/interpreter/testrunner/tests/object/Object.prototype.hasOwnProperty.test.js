describe("Object.prototype.hasOwnProperty", () => {
  test("returns false if no arguments are passed in", () => {
    const object = { a: 2 };
    expect(object.hasOwnProperty()).toBeFalse();
  });

  test("returns true if key is in object", () => {
    const object = { a: 2 };
    expect(object.hasOwnProperty("a")).toBeTrue();
  });

  test("returns true if key is in object and is not enumerable", () => {
    const object = {};
    Object.defineProperty(object, "a", { value: 2, enumerable: false });
    expect(object.hasOwnProperty("a")).toBeTrue();
  });

  test("returns false if key is not in object", () => {
    const object = {};
    expect(object.hasOwnProperty("a")).toBeFalse();
  });

  test("returns false if key is inherited", () => {
    function Test() {}
    Test.prototype.a = 2;

    const object = new Test();
    expect(object.hasOwnProperty("a")).toBeFalse();
  });
});
