this.__testContext = {
  results: [],
  describeContext: null,
};

function AssertionError(reason) {
  this.reason = reason;
}

function AssertionContext(testValue) {
  this.testValue = testValue;
}

AssertionContext.prototype.toEqual = function (value) {
  if (!deepEquals(value, this.testValue)) {
    const message = "Expected: " + value + "\nReceived: " + testValue;
    throw new AssertionError(message);
  }
};

AssertionContext.prototype.toBe = function (value) {
  if (!deepEquals(value, this.testValue)) {
    const message = "Expected: " + value + "\nReceived: " + testValue;
    throw new AssertionError(message);
  }
};

AssertionContext.prototype.toBeFalse = function () {
  if (testValue) {
    const message = "Expected " + testValue + " to be false";
    throw new AssertionError(message);
  }
};

AssertionContext.prototype.toBeTrue = function () {
  if (!testValue) {
    const message = "Expected " + testValue + " to be true";
    throw new AssertionError(message);
  }
};

AssertionContext.prototype.toBeUndefined = function () {
  if (testValue !== undefined) {
    const message = "Expected " + testValue + " to be undefined";
    throw new AssertionError(message);
  }
};

AssertionContext.prototype.toBeNull = function () {
  if (testValue !== null) {
    const message = "Expected " + testValue + " to be null";
    throw new AssertionError(message);
  }
};

AssertionContext.prototype.toThrowError = function (value) {
  if (typeof this.testValue !== "function") {
    throw new TypeError("toThrowError must be called on function value");
  }

  try {
    this.testValue();
  } catch (error) {
    if (value instanceof Error) {
      if (error.name !== value.name || error.message !== value.message) {
        const message = "Expected: " + value + "\nReceived: " + error;
        throw new AssertionError(message);
      }
    } else {
      if (!deepEquals(value, error)) {
        const message = "Expected: " + value + "\nReceived: " + error;
        throw new AssertionError(message);
      }
    }
  }
};

function expect(testValue) {
  return new AssertionContext(testValue);
}

function describe(name, block) {
  const describeContext = {
    type: "describe",
    name: name,
    results: [],
    runTime: 0,
  };

  this.__testContext.describeContext = describeContext;

  block();

  describeContext.results.forEach(
    (test) => (describeContext.runTime += test.runTime)
  );

  this.__testContext.results.push(describeContext);
  this.__testContext.describeContext = null;
}

function test(name, tests) {
  const testStartTime = new Date();

  function pushTestResult(result) {
    if (__testContext.describeContext === null) {
      __testContext.results.push(result);
    } else {
      __testContext.describeContext.results.push(result);
    }
  }

  try {
    tests();
    pushTestResult({
      type: "test",
      status: "pass",
      testName: name,
      runTime: new Date() - testStartTime,
    });
  } catch (error) {
    const errorMessage =
      error instanceof AssertionError
        ? error.reason
        : "Unexpected error: " + error;

    pushTestResult({
      type: "test",
      status: "fail",
      testName: name,
      message: errorMessage,
      runTime: new Date() - testStartTime,
    });
  }
}

function deepEquals(a, b) {
  if (typeof a === "object" && typeof b === "object") {
    if (Array.isArray(a) && Array.isArray(b)) {
      return arrayDeepEquals(a, b);
    }

    let keysOfA = Object.keys(a);
    let keysOfB = Object.keys(b);

    if (keysOfA.length !== keysOfB.length) {
      return false;
    }

    for (let i = 0; i < keysOfA.length; i++) {
      let key = keysOfA[i];
      if (!deepEquals(a[key], b[key])) {
        return false;
      }
    }

    return true;
  }

  return a === b;
}

function arrayDeepEquals(a, b) {
  if (a.length !== b.length) {
    return false;
  }

  for (let i = 0; i < a.length; i++) {
    if (!deepEquals(a[i], b[i])) {
      return false;
    }
  }

  return true;
}
