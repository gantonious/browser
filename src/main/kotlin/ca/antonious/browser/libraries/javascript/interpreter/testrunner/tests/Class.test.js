describe("Class", () => {
  test("supports empty body", () => {
    class Test {}
    const instance = new Test();

    expect(typeof Test).toBe("function");
    expect(instance).toBe({});
    expect(instance.__proto__).toBe(Test.prototype);
  });

  test("supports instance members", () => {
    const memberInvocations = [];

    class Test {
      member = (() => {
        memberInvocations.push(null);
        return 3;
      })();
    }

    expect(memberInvocations.length).toBe(0);

    const instance = new Test();
    expect(instance).toBe({ member: 3 });
    expect(memberInvocations.length).toBe(1);

    const instance2 = new Test();
    expect(memberInvocations.length).toBe(2);
  });

  test("supports static members", () => {
    const memberInvocations = [];

    class Test {
      static staticMember = (() => {
        memberInvocations.push(null);
        return 3;
      })();
    }

    expect(memberInvocations.length).toBe(1);
    expect(Test.staticMember).toBe(3);

    const instance = new Test();
    expect(instance).toBe({});
    expect(instance.staticMember).toBeUndefined();
    expect(memberInvocations.length).toBe(1);
  });

  test("supports constructors", () => {
    class Test {
      constructor(a) {
        this.a = a;
      }
    }

    const instance = new Test(5);
    expect(instance).toBe({ a: 5 });
  });

  test("supports methods", () => {
    class Test {
      method(a) {
        return a;
      }
    }

    const instance = new Test();
    expect(instance.method(54)).toBe(54);
  });

  test("supports static methods", () => {
    class Test {
      static method(a) {
        return a;
      }
    }

    const instance = new Test();
    expect(Test.method(54)).toBe(54);
    expect(instance.method).toBeUndefined();
  });

  test("instance methods are able to reference this properties", () => {
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
});
