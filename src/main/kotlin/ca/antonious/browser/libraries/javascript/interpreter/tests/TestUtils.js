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
  if (value != testValue) {
    const message = "Expected: " + value + "\nReceived: " + testValue;
    throw new AssertionError(message);
  }
};

AssertionContext.prototype.toBe = function (value) {
  if (value !== testValue) {
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
    __testContext.results.push({
      status: "fail",
      testName: name,
      message: error.reason || error + "",
      runTime: new Date() - testStartTime,
    });
  }
}
