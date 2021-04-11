describe("Functions", () => {
  test("when a function in isolation at the top level, this is set to the global object", () => {
    let thisBinding;

    function Test() {
      thisBinding = this;
    }

    Test();
    expect(thisBinding).toBe(global);
  });

  test("when a function in isolation within a context with a non-global this binding, this is set to the global object", () => {
    let thisBinding;
    let object = { test: Test };

    function Test() {
      return function () {
        thisBinding = this;
      };
    }

    let testFunction = object.test();
    testFunction();
    expect(thisBinding).toBe(global);
  });

  test("when calling function as member, this is set to the target object", () => {
    let thisBinding;
    let object = { test: Test };

    function Test() {
      thisBinding = this;
    }

    object.test();

    expect(thisBinding).toBe(object);
  });
});
