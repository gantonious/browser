describe("Operators.Addition", () => {
  it("test numeric addition", () => {
    function CustomValueOf() {}
    CustomValueOf.prototype.valueOf = function () {
      return 2;
    };

    expect(2 + 2).toBe(4);
    expect(2 + true).toBe(3);
    expect(2 + false).toBe(2);
    expect(2 + null).toBe(2);
    expect(2 + new CustomValueOf()).toBe(4);
  });

  it("test string concatenation", () => {
    function CustomValueOf() {}
    CustomValueOf.prototype.valueOf = function () {
      return " world!";
    };

    expect("Hello" + " " + "world!").toBe("Hello world!");
    expect("Hello" + new CustomValueOf()).toBe("Hello world!");
    expect("Hello " + true).toBe("Hello true");
    expect(true + " world!").toBe("true world!");
  });
});
