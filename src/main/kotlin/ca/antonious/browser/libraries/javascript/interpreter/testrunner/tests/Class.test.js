describe("Class", () => {
  it("supports empty body", () => {
    class Test {}
    const instance = new Test();

    expect(typeof Test).toBe("function");
    expect(instance).toBe({});
    expect(instance.__proto__).toBe(Test.prototype);
  });

  it("supports instance members", () => {
    const memberInvocationCount = 0;

    class Test {
      member = (() => {
        memberInvocationCount++;
        return 3;
      })();
    }

    expect(memberInvocationCount).toBe(0);

    const instance = new Test();
    expect(instance).toBe({ member: 3 });
    expect(memberInvocationCount).toBe(1);

    const instance2 = new Test();
    expect(memberInvocationCount).toBe(2);
  });

  it("supports static members", () => {
    const memberInvocationCount = 0;

    class Test {
      static staticMember = (() => {
        memberInvocationCount++;
        return 3;
      })();
    }

    expect(memberInvocationCount).toBe(1);
    expect(Test.staticMember).toBe(3);

    const instance = new Test();
    expect(instance).toBe({});
    expect(instance.staticMember).toBeUndefined();
    expect(memberInvocationCount).toBe(1);
  });

  it("supports constructors", () => {
    class Test {
      constructor(a) {
        this.a = a;
      }
    }

    const instance = new Test(5);
    expect(instance).toBe({ a: 5 });
  });

  it("supports methods", () => {
    class Test {
      method(a) {
        return a;
      }
    }

    const instance = new Test();
    expect(instance.method(54)).toBe(54);
  });

  it("supports static methods", () => {
    class Test {
      static method(a) {
        return a;
      }
    }

    const instance = new Test();
    expect(Test.method(54)).toBe(54);
    expect(instance.method).toBeUndefined();
  });

  it("instance methods are able to reference this properties", () => {
    class Test {
      constructor(a, b) {
        this.a = a;
        this.b = b;
      }

      returnA() {
        return a;
      }

      returnB() {
        return this.b;
      }
    }

    const instance = new Test(4, 7);
    expect(instance).toBe({ a: 4, b: 7 });
    expect(instance.returnA()).toBe(4);
    expect(instance.returnB()).toBe(7);
  });

  it("supports extending constructor", () => {
    let test1InvocationCount = 0;

    function Test1() {
      test1InvocationCount++;
      this.a = 2;
    }

    Test1.prototype.returnB = function () {
      return this.b;
    };

    class Test2 extends Test1 {
      b = 4;
    }

    expect(test1InvocationCount).toBe(0);

    const instance = new Test2();
    expect(instance).toBe({ a: 2, b: 4 });
    expect(instance instanceof Test2).toBeTrue();
    expect(instance instanceof Test1).toBeTrue();
    expect(instance instanceof Object).toBeTrue();
    expect(instance.returnB()).toBe(4);
    expect(test1InvocationCount).toBe(1);
  });

  it("uses return value of super constructor as this binding", () => {
    const thisValue = {
      a: 2,
    };

    function SuperClass() {
      return thisValue;
    }

    class Subclass extends SuperClass {}

    expect(new Subclass()).toBe(thisValue);
  });

  it("supports extending null", () => {
    class TestClass extends null {}

    const instance = new TestClass();
    expect(instance instanceof TestClass).toBeTrue();
    expect(instance instanceof Object).toBeFalse();
    expect(instance.valueOf).toBeUndefined();
  });

  it("throws type error if extending unsupported value", () => {
    expect(() => {
      class TestClass extends 5 {}
    }).toThrowError(
      new TypeError("Class extends value 5 is not a constructor or null")
    );
  });
});
