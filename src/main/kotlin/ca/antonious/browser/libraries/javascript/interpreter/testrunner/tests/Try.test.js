describe("Try/Catch", () => {
  it("only invokes try block if no error is thrown", () => {
    let invocationOrder = [];

    try {
      invocationOrder.push("try");
    } catch {
      invocationOrder.push("catch");
    }

    expect(invocationOrder).toBe(["try"]);
  });

  it("invokes catch block if an error is thrown", () => {
    let invocationOrder = [];

    try {
      invocationOrder.push("try");
      throw "error";
    } catch {
      invocationOrder.push("catch");
    }

    expect(invocationOrder).toBe(["try", "catch"]);
  });

  it("invokes catch block with correct catch parameter", () => {
    let invocationOrder = [];

    try {
      invocationOrder.push("try");
      throw "error";
    } catch (error) {
      invocationOrder.push("catch(" + error + ")");
    }

    expect(invocationOrder).toBe(["try", "catch(error)"]);
  });
});

describe("Try/Finally", () => {
  it("invokes both catch and finally block if no error is thrown", () => {
    let invocationOrder = [];

    try {
      invocationOrder.push("try");
    } finally {
      invocationOrder.push("finally");
    }

    expect(invocationOrder).toBe(["try", "finally"]);
  });

  it("invokes finally block if error is thrown in catch block", () => {
    let invocationOrder = [];

    try {
      try {
        invocationOrder.push("try");
        throw "error";
      } finally {
        invocationOrder.push("finally");
      }
    } catch {}

    expect(invocationOrder).toBe(["try", "finally"]);
  });
});

describe("Try/Catch/Finally", () => {
  it("only invokes try/finally blocks if no error is thrown", () => {
    let invocationOrder = [];

    try {
      invocationOrder.push("try");
    } catch {
      invocationOrder.push("catch");
    } finally {
      invocationOrder.push("finally");
    }

    expect(invocationOrder).toBe(["try", "finally"]);
  });

  it("invokes catch/finally blocks if an error is thrown", () => {
    let invocationOrder = [];

    try {
      invocationOrder.push("try");
      throw "error";
    } catch {
      invocationOrder.push("catch");
    } finally {
      invocationOrder.push("finally");
    }

    expect(invocationOrder).toBe(["try", "catch", "finally"]);
  });

  it("invokes catch block with correct catch parameter", () => {
    let invocationOrder = [];

    try {
      throw "error";
    } catch (error) {
      invocationOrder.push("catch");
    } finally {
      invocationOrder.push("finally");
    }

    expect(invocationOrder).toBe(["catch", "finally"]);
  });

  it("invokes finally block even if catch returns early", () => {
    let invocationOrder = [];

    function testCase() {
      try {
        throw "error";
      } catch (error) {
        invocationOrder.push("catch(" + error + ")");
        return "catch";
      } finally {
        invocationOrder.push("finally");
      }
    }

    let returnValue = testCase();
    expect(returnValue).toBe("catch");
    expect(invocationOrder).toBe(["catch(error)", "finally"]);
  });

  it("biases to return in finally block", () => {
    let invocationOrder = [];

    function testCase() {
      try {
        throw "error";
      } catch (error) {
        invocationOrder.push("catch");
        return "catch";
      } finally {
        invocationOrder.push("finally");
        return "finally";
      }
    }

    let returnValue = testCase();
    expect(returnValue).toBe("finally");
    expect(invocationOrder).toBe(["catch", "finally"]);
  });
});
