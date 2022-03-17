describe("ArrayDestructuring", () => {
  it("supports empty arrays", () => {
    let [a, , ...rest] = [];
    expect(a).toBe(undefined);
    expect(rest).toBe([]);
  });

  it("supports skipping values", () => {
    let [a, , b] = [1, 2, 3];
    expect(a).toBe(1);
    expect(b).toBe(3);
  });

  it("supports rest values", () => {
    let [a, ...rest] = [1, 2, 3];
    expect(a).toBe(1);
    expect(rest).toBe([2, 3]);
  });

  it("supports nested arrays", () => {
    let [a, [b, ...rest], d] = [1, [2, 3], 4];
    expect(a).toBe(1);
    expect(b).toBe(2);
    expect(rest).toBe([3]);
    expect(d).toBe(4);
  });

  it("supports nested objects", () => {
    let [a, { b, c = 5, ...rest }, d] = [1, { b: 2, e: 9 }, 4];
    expect(a).toBe(1);
    expect(b).toBe(2);
    expect(c).toBe(5);
    expect(rest).toBe({ e: 9 });
    expect(d).toBe(4);
  });

  it("supports default values", () => {
    let [a, b = 3, c = 7] = [1, undefined];
    expect(a).toBe(1);
    expect(b).toBe(3);
    expect(c).toBe(7);
  });

  it("throws TypeError if destructing a non iterable type", () => {
    expect(() => {
      let [a, b] = 2;
    }).toThrowError(new TypeError("2 is not iterable"));
  });
});
