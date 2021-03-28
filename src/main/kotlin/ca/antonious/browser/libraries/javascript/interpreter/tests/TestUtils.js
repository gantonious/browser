this.__testContext = {
  results: [],
};

function TestFailureError(reason) {
  this.reason = reason;
}

function AssertionContext(testCase, testValue) {
  this.testCase = testCase;
  this.testValue = testValue;
}

AssertionContext.prototype.toEqual = function (value) {
  if (value != testValue) {
    const message = "Expected: " + value + "\nReceived: " + testValue;
    throw new TestFailureError(message);
  }
};

AssertionContext.prototype.toBe = function (value) {
  if (value !== testValue) {
    const message = "Expected: " + value + "\nReceived: " + testValue;
    throw new TestFailureError(message);
  }
};

AssertionContext.prototype.toBeFalse = function () {
  if (testValue) {
    const message = "Expected " + testValue + " to be false";
    throw new TestFailureError(message);
  }
};

AssertionContext.prototype.toBeTrue = function () {
  if (!testValue) {
    const message = "Expected " + testValue + " to be true";
    throw new TestFailureError(message);
  }
};

AssertionContext.prototype.toBeUndefined = function () {
  if (testValue !== undefined) {
    const message = "Expected " + testValue + " to be undefined";
    throw new TestFailureError(message);
  }
};

AssertionContext.prototype.toBeNull = function () {
  if (testValue !== null) {
    const message = "Expected " + testValue + " to be null";
    throw new TestFailureError(message);
  }
};

function TestCaseContext(name, testStartTime) {
  this.name = name;
  this.testStartTime = testStartTime;

  this.expect = function (testValue) {
    return new AssertionContext(this, testValue);
  };
}

function test(name, tests) {
  const testStartTime = new Date();
  const testCase = new TestCaseContext(name, testStartTime);

  try {
    tests.call(testCase);
    __testContext.results.push({
      status: "pass",
      testName: name,
      runTime: new Date() - testStartTime,
    });
  } catch (error) {
    __testContext.results.push({
      status: "fail",
      testName: name,
      message: error.reason,
      runTime: new Date() - testStartTime,
    });
  }
}
