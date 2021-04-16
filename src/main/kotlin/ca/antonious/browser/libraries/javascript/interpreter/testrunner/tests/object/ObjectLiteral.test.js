describe("ObjectLiteral", () => {
  test("supports defining empty object", () => {
    const obj = {};
    expect(Object.keys(obj)).toBe([]);
    expect(Object.values(obj)).toBe([]);
  });

  test("supports defining flat object", () => {
    const obj = { a: 2, b: "str" };
    expect(Object.keys(obj)).toBe(["a", "b"]);
    expect(Object.values(obj)).toBe([2, "str"]);
  });

  test("supports defining nested object", () => {
    const obj = { a: 2, b: { c: false } };
    expect(Object.keys(obj)).toBe(["a", "b"]);
    expect(Object.values(obj)).toBe([2, { c: false }]);
  });

  test("supports defining object with getter", () => {
    let thisBinding;

    const obj = {
      get a() {
        thisBinding = this;
        return 1;
      },
    };

    expect(Object.keys(obj)).toBe(["a"]);
    expect(Object.values(obj)).toBe([1]);
    expect(obj.a).toBe(1);
    expect(thisBinding).toBe(obj);

    // Shouldn't error even though no setter is set
    obj.a = 1;
  });

  test("supports defining object with setter", () => {
    let thisBinding;
    let setterInvocation;

    const obj = {
      set a(value) {
        thisBinding = this;
        setterInvocation = value;
      },
    };

    expect(Object.keys(obj)).toBe(["a"]);
    expect(Object.values(obj)).toBe([undefined]);

    obj.a = 1;
    expect(obj.a).toBeUndefined();
    expect(setterInvocation).toBe(1);
    expect(thisBinding).toBe(obj);
  });

  test("supports defining object with getter + setter for same property", () => {
    const obj = {
      _a: 0,
      get a() {
        return this._a;
      },
      set a(value) {
        this._a = value;
      },
    };

    expect(Object.keys(obj)).toBe(["_a", "a"]);
    expect(Object.values(obj)).toBe([0, 0]);

    expect(obj.a).toBe(0);
    obj.a = 1;
    expect(obj.a).toBe(1);
  });

  test("supports properties named get/set", () => {
    const obj = {
      get: 0,
      set: 1,
    };

    expect(Object.keys(obj)).toBe(["get", "set"]);
    expect(Object.values(obj)).toBe([0, 1]);
  });
});
