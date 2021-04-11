this.__testContext = {
  results: [],
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

function expect(testValue) {
  return new AssertionContext(testValue);
}

function test(name, tests) {
  const testStartTime = new Date();

  try {
    tests();
    __testContext.results.push({
      status: "pass",
      testName: name,
      runTime: new Date() - testStartTime,
    });
  } catch (error) {
    const errorMessage =
      error instanceof AssertionError
        ? error.reason
        : "Unexpected error: " + error;

    __testContext.results.push({
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
