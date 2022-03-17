describe("TestUtils", () => {
  it("deepEquals supports primitives", () => {
    expect(deepEquals(1, 1)).toBeTrue();
    expect(deepEquals("str", "str")).toBeTrue();
    expect(deepEquals(true, true)).toBeTrue();
    expect(deepEquals(false, false)).toBeTrue();
    expect(deepEquals(undefined, undefined)).toBeTrue();
    expect(deepEquals(null, null)).toBeTrue();

    expect(deepEquals(1, 2)).toBeFalse();
    expect(deepEquals(1, "1")).toBeFalse();
    expect(deepEquals("str", "str2")).toBeFalse();
    expect(deepEquals(true, false)).toBeFalse();
    expect(deepEquals(undefined, null)).toBeFalse();
  });

  it("deepEquals supports object", () => {
    expect(deepEquals({}, {})).toBeTrue();
    expect(deepEquals({ a: 2 }, { a: 2 })).toBeTrue();
    expect(deepEquals({ a: 2, b: 3 }, { a: 2, b: 3 })).toBeTrue();
    expect(deepEquals({ a: 2, b: 3 }, { b: 3, a: 2 })).toBeTrue();
    expect(deepEquals({ b: { c: [1, 2] } }, { b: { c: [1, 2] } })).toBeTrue();
    expect(deepEquals({ a: 2, b: { c: 3 } }, { b: { c: 3 }, a: 2 })).toBeTrue();

    expect(deepEquals({ a: 2 }, 4)).toBeFalse();
    expect(deepEquals({ a: 2 }, {})).toBeFalse();
    expect(deepEquals({ a: 2, b: 3 }, { a: 2, b: "3" })).toBeFalse();
    expect(deepEquals({ b: { c: [1, 2] } }, { b: { c: [2, 1] } })).toBeFalse();
    expect(
      deepEquals({ a: 2, b: { c: 3 } }, { b: { d: 3 }, a: 2 })
    ).toBeFalse();
  });

  it("deepEquals supports arrays", () => {
    expect(deepEquals([], [])).toBeTrue();
    expect(deepEquals([1, 2, 3], [1, 2, 3])).toBeTrue();
    expect(
      deepEquals([1, { a: 2, b: [1, 2] }, 3], [1, { b: [1, 2], a: 2 }, 3])
    ).toBeTrue();

    expect(deepEquals([], [1])).toBeFalse();
    expect(deepEquals([1, 2, 3], [1, 3, 1])).toBeFalse();
    expect(
      deepEquals([1, { a: 2, b: [1, 2] }, 3], [1, { b: [1, 2], a: 4 }, 3])
    ).toBeFalse();
  });
});
